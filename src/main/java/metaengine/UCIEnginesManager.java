package metaengine;

import java.util.List;
import java.util.ArrayList;

import java.util.concurrent.Future;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import java.io.IOException;

public class UCIEnginesManager {

    private static final boolean DEBUG = false;

    // Member variables
    private final List<UCIEngine> enginesList;
    private final List<UCIEngine> generators = new ArrayList<>();
    private final List<UCIEngine> evaluators = new ArrayList<>();
    private UCIPosition currentPosition = UCIPosition.STARTPOS;

    public static UCIEnginesManager create(Configuration conf)
      throws InvalidConfigurationException {
        try {
            List<Configuration.EngineConfiguration> engineConfigs =
                conf.getEngineConfigurations();

            List<Future<UCIEngine>> futureEngines = new ArrayList<>();
            for (Configuration.EngineConfiguration engineConf : engineConfigs) {
                Callable<UCIEngine> constructEngine = () -> {
                    return new UCIEngine(engineConf);
                };
                futureEngines.add(Main.threadPool.submit(constructEngine));
            }

            List<UCIEngine> engines = new ArrayList<>();
            for (Future<UCIEngine> future : futureEngines) {
                engines.add(future.get());
            }

            return new UCIEnginesManager(engines);
        } catch (InvalidConfigurationException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("This is for testing purposes only", e);
        }
    }

    private UCIEnginesManager(List<UCIEngine> toManage)
      throws InvalidConfigurationException {
        enginesList = toManage;
         for (UCIEngine engine : enginesList) {
             EngineRoles roles = engine.getRoles();
             if (roles.isRecomender()) {
                 generators.add(engine);
             }
             if (roles.isJudge()) {
                 evaluators.add(engine);
             }
         }

        if (generators.size() != evaluators.size()) {
            throw new InvalidConfigurationException(
              "There must be the same number of generators as evaluators");
        }
    }

    public List<UCIOptionBundle> getUCIOptions() {
        List<UCIOptionBundle> ret = new ArrayList<UCIOptionBundle>();
        for (UCIEngine engine : enginesList) {
            ret.add(new UCIOptionBundle(engine.getOptions(),
              engine.getName(), engine.getIndex()));
        }
        return ret;
    }

    public void dispatchOption(SetoptionInfo setoptionInfo) {
        for (UCIEngine engine : enginesList) {
            if (engine.getIndex() == setoptionInfo.getEngineIndex()) {
                List<UCIOption> opts = engine.getOptions();
                for (UCIOption opt : opts) {
                    if (opt.getName().equals(setoptionInfo.getNameString())) {
                        UCIOption.Value val =
                          new UCIOption.Value(setoptionInfo.getValueString(),
                                              opt.getValueType());
                        opt.setValue(val);
                        engine.sendOption(opt);
                    }
                }
            }
        }
    }

    private class Searcher implements Callable<UCIMove> {
        private final UCIGo goInfo;
        public Searcher(UCIGo goInfo) {
            this.goInfo = goInfo;
        }

        @Override
        public UCIMove call() {
            try {
                if (DEBUG) {
                    System.err.println("DEBUG: goInfo: " +
                        goInfo.toString());
                }
                UCIGo timerGo = goInfo.getConvertedForTimer();
                if (DEBUG) {
                    System.err.println("DEBUG: timerGo: " + timerGo.toString());
                }
                UCIEngine timerEngine = generators.get(0);

                Future<GoResult> timerFuture = Main.threadPool.submit(() -> {
                    return timerEngine.go(timerGo);
                });
                long timerStart = System.nanoTime();

                boolean firstIter = true;
                List<Future<GoResult>> generatorFutures = new ArrayList<>();
                for (UCIEngine engine : generators) {
                    if (firstIter) {
                        firstIter = false;
                        continue;
                    }
                    generatorFutures.add(Main.threadPool.submit(() -> {
                        return engine.go(UCIGo.INFINITE);
                    }));
                }
                List<GoResult> generatorResults = new ArrayList<>();
                generatorResults.add(timerFuture.get());
                long timerTime = System.nanoTime() - timerStart;
                for (UCIEngine engine : generators) {
                    engine.stop();
                }
                for (Future<GoResult> fut : generatorFutures) {
                    generatorResults.add(fut.get());
                }

                // It must be the case that
                // generators.length() == evaluators.length()
                // Get evaluator scores with searchmoves
                List<Future<GoResult>> evaluatorFutures = new ArrayList<>();
                for (int i = 0; i != evaluators.size(); ++i) {
                    UCIEngine evaluator = evaluators.get(i);
                    String[] goCtorParam = {
                        "go", "movetime",
                        Long.toString(UCIUtils.convertTimerTime(timerTime))
                    };
                    UCIPosition evaluatorPosition =
                        currentPosition.plus(generatorResults.get(i).getMove());
                    evaluatorFutures.add(Main.threadPool.submit(() -> {
                        evaluator.position(evaluatorPosition);
                        return evaluator.go(new UCIGo(goCtorParam));
                    }));
                }

                List<GoResult> evaluatorResults = new ArrayList<>();
                for (Future<GoResult> fut : evaluatorFutures) {
                    evaluatorResults.add(fut.get());
                }

                int bestIndex = 0;
                GoResult.Score bestScore =
                    new GoResult.Score(Integer.MIN_VALUE);
                for (int i = 0; i != evaluatorResults.size(); ++i) {
                    GoResult.Score origScore =
                        evaluatorResults.get(i).getScore().flip();
                    GoResult.Score score = origScore.getBiasedScore(
                        generators.get(i).getBias());
                    if (score.compareTo(bestScore) >= 0) {
                        bestScore = score;
                        bestIndex = i;
                    }
                }

                return generatorResults.get(bestIndex).getMove();
            } catch (Exception e) { // TODO: Real exception handling?
                throw new RuntimeException(
                    "Unexpected Exception in Searcher.run()", e);
            }
        }
    }

    public void setPosition(UCIPosition pos) {
        for (UCIEngine engine : enginesList) {
            engine.position(pos);
        }
        currentPosition = pos;
    }

    public Future<UCIMove> search(UCIGo params) {
        return Main.threadPool.submit(new Searcher(params));
    }

    public void synchronizeAll() {
        for (UCIEngine engine : enginesList) {
            engine.synchronize();
        }
    }

    public void ucinewgameAll() {
        for (UCIEngine engine : enginesList) {
            engine.sendUcinewgame();
        }
    }

    public void quitAll() {
        for (UCIEngine engine : enginesList) {
            engine.quit();
        }
    }
}
