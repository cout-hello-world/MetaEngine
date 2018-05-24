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
    private final List<EngineRecord> enginesList;
    private final List<EngineRecord> recomenderRecords = new ArrayList<>();
    private final List<EngineRecord> judgeRecords = new ArrayList<>();
    private UCIPosition currentPosition = UCIPosition.STARTPOS;

    public static UCIEnginesManager create(Configuration conf)
      throws InvalidConfigurationException {
        try {
            List<Configuration.EngineConfiguration> engineConfigs =
                conf.getEngineConfigurations();

            List<Future<EngineRecord>> futureEngines =
              new ArrayList<Future<EngineRecord>>();
            for (Configuration.EngineConfiguration engineConf : engineConfigs) {
                Callable<EngineRecord> constructEngine = () -> {
                    return new EngineRecord(
                      new UCIEngine(engineConf.getEngineArgv(),
                                    engineConf.getIndex()),
                      engineConf);
                };
                futureEngines.add(Main.threadPool.submit(constructEngine));
            }

            List<EngineRecord> engines = new ArrayList<EngineRecord>();
            for (Future<EngineRecord> future : futureEngines) {
                engines.add(future.get());
            }

            return new UCIEnginesManager(engines);
        } catch (InvalidConfigurationException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("This is for testing purposes only", e);
        }
    }

    private static class EngineRecord {
        private final UCIEngine engine;
        private final Configuration.EngineConfiguration conf;
        public EngineRecord(UCIEngine engine, Configuration.EngineConfiguration config) {
            this.engine = engine;
            this.conf = config;
        }

        public UCIEngine getEngine() {
            return engine;
        }

        public int getIndex() {
            return conf.getIndex();
        }

        public Configuration.EngineConfiguration getConfig() {
            return conf;
        }
    }

    private UCIEnginesManager(List<EngineRecord> toManage)
      throws InvalidConfigurationException {
        enginesList = toManage;
        for (EngineRecord rec : enginesList) {
             EngineRoles roles = rec.getConfig().getEngineRoles();

             if (roles.isTimer()) {
                 recomenderRecords.add(rec);
                 break;
             }
         }
         for (EngineRecord rec : enginesList) {
             EngineRoles roles = rec.getConfig().getEngineRoles();
             if (roles.isTimer()) {
                 continue;
             }
             if (roles.isRecomender()) {
                 recomenderRecords.add(rec);
             } else if (roles.isJudge()) {
                 judgeRecords.add(rec);
             }
         }

        if (recomenderRecords.size() != judgeRecords.size()) {
            throw new InvalidConfigurationException(
              "There must be the same number of recomenders as judges");
        }
    }

    public List<UCIOptionBundle> getUCIOptions() {
        List<UCIOptionBundle> ret = new ArrayList<UCIOptionBundle>();
        for (EngineRecord rec : enginesList) {
            UCIEngine engine = rec.getEngine();
            ret.add(new UCIOptionBundle(engine.getOptions(),
              engine.getName(), rec.getIndex()));
        }
        return ret;
    }

    public void dispatchOption(SetoptionInfo setoptionInfo) {
        for (EngineRecord rec : enginesList) {
            if (rec.getIndex() == setoptionInfo.getEngineIndex()) {
                UCIEngine engine = rec.getEngine();
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
                UCIEngine timerEngine = recomenderRecords.get(0).getEngine();

                Future<GoResult> timerFuture = Main.threadPool.submit(() -> {
                    return timerEngine.go(timerGo);
                });
                long timerStart = System.nanoTime();

                boolean firstIter = true;
                List<Future<GoResult>> recommenderFutures = new ArrayList<>();
                for (EngineRecord rec : recomenderRecords) {
                    if (firstIter) {
                        firstIter = false;
                        continue;
                    }
                    Configuration.EngineConfiguration conf = rec.getConfig();
                    UCIEngine engine = rec.getEngine();
                    recommenderFutures.add(Main.threadPool.submit(() -> {
                        return engine.go(UCIGo.INFINITE);
                    }));
                }
                List<GoResult> recResults = new ArrayList<>();
                recResults.add(timerFuture.get());
                long timerTime = System.nanoTime() - timerStart;
                for (EngineRecord rec : recomenderRecords) {
                    rec.getEngine().stop();
                }
                for (Future<GoResult> fut : recommenderFutures) {
                    recResults.add(fut.get());
                }

                // It must be the case that
                // recomenderRecords.length == judgeRecords.length
                // Get judge scores with searchmoves
                List<Future<GoResult>> judgeFutures = new ArrayList<>();
                for (int i = 0; i != judgeRecords.size(); ++i) {
                    UCIEngine judge = judgeRecords.get(i).getEngine();
                    String[] goCtorParam = {
                        "go", "movetime",
                        Long.toString(UCIUtils.convertTimerTime(timerTime))
                    };
                    UCIPosition judgePosition =
                        currentPosition.plus(recResults.get(i).getMove());
                    judgeFutures.add(Main.threadPool.submit(() -> {
                        judge.position(judgePosition);
                        return judge.go(new UCIGo(goCtorParam));
                    }));
                }

                List<GoResult> judgeResults = new ArrayList<>();
                for (Future<GoResult> fut : judgeFutures) {
                    judgeResults.add(fut.get());
                }

                int bestIndex = 0;
                GoResult.Score bestScore =
                    new GoResult.Score(Integer.MIN_VALUE);
                for (int i = 0; i != judgeResults.size(); ++i) {
                    GoResult.Score origScore =
                        judgeResults.get(i).getScore().flip();
                    GoResult.Score score = origScore.getBiasedScore(
                        recomenderRecords.get(i).getConfig().getBias());
                    if (score.compareTo(bestScore) >= 0) {
                        bestScore = score;
                        bestIndex = i;
                    }
                }

                return recResults.get(bestIndex).getMove();
            } catch (Exception e) { // TODO: Real exception handling?
                throw new RuntimeException(
                    "Unexpected Exception in Searcher.run()", e);
            }
        }
    }

    public void setPosition(UCIPosition pos) {
        for (EngineRecord rec : enginesList) {
            UCIEngine engine = rec.getEngine();
            engine.position(pos);
        }
        currentPosition = pos;
    }


    public Future<UCIMove> search(UCIGo params) {
        return Main.threadPool.submit(new Searcher(params));
    }

    public void synchronizeAll() {
        for (EngineRecord rec : enginesList) {
            rec.getEngine().synchronize();
        }
    }

    public void ucinewgameAll() {
        for (EngineRecord rec : enginesList) {
            rec.getEngine().sendUcinewgame();
        }
    }

    public void quitAll() {
        for (EngineRecord rec : enginesList) {
            rec.getEngine().quit();
        }
    }
}
