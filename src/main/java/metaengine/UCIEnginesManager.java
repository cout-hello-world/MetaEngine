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

    // Member variables
    private final List<EngineRecord> enginesList;
    private final List<EngineRecord> recomenderRecords = new ArrayList<>();
    private final List<EngineRecord> judgeRecords = new ArrayList<>();
    private EngineRecord timerRecord = null;

    public static UCIEnginesManager create(Configuration conf)
      throws InvalidConfigurationException {
        try {
            List<Configuration.EngineConfiguration> engineConfigs =
                conf.getEngineConfigurations();

            List<Future<EngineRecord>> futureEngines =
              new ArrayList<Future<EngineRecord>>();
            for (Configuration.EngineConfiguration engineConf : engineConfigs) {
                Callable<EngineRecord> constructEngine = () -> {
                    return new EngineRecord(new UCIEngine(
                      engineConf.getEngineArgv()), engineConf);
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
                 timerRecord = rec;
             } else {
                 if (roles.isRecomender()) {
                     recomenderRecords.add(rec);
                 }
                 if (roles.isJudge()) {
                     judgeRecords.add(rec);
                 }
            }
        }

        // There is one timer which is also implicitly a recomender
        if (timerRecord == null) {
            throw new InvalidConfigurationException("There must be a timer");
        }
        if (recomenderRecords.size() + 1 != judgeRecords.size()) {
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

    private class Searcher implements Runnable {
        private final SearchInfo info;
        private final UCIGo goInfo;
        public Searcher(SearchInfo info, UCIGo goInfo) {
            this.info = info;
            this.goInfo = goInfo;
        }

        @Override
        public void run() {
            try {
                UCIGo timerGo = goInfo.getConvertedForTimer();
                UCIEngine timerEngine = timerRecord.getEngine();

                Future<GoResult> timerFuture = Main.threadPool.submit(() -> {
                    return timerEngine.go(timerGo);
                });
                long timerStart = System.nanoTime();

                List<Future<GoResult>> recommenderFutures = new ArrayList<>();
                for (EngineRecord rec : recomenderRecords) {
                    Configuration.EngineConfiguration conf = rec.getConfig();
                    UCIEngine engine = rec.getEngine();
                    recommenderFutures.add(Main.threadPool.submit(() -> {
                        return engine.go(UCIGo.INFINITE);
                    }));
                }
                GoResult timerResult = timerFuture.get();
                long timerTime = System.nanoTime() - timerStart;
                for (EngineRecord rec : recomenderRecords) {
                    rec.getEngine().stop();
                }
                List<GoResult> recResults = new ArrayList<>();
                for (Future<GoResult> fut : recommenderFutures) {
                    recResults.add(fut.get());
                }

                // It must be the case that
                // 1 + recomenderRecords.length == judgeRecords.length
                // Get judge scores with searchmoves

                // Finally, compare scores adjusted for bias.
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
    }


    public SearchInfo search(UCIGo params) {
        SearchInfo result = new SearchInfo();
        Main.threadPool.submit(new Searcher(result, params));
        return result;
    }

    public void synchronizeAll() {
        for (EngineRecord rec : enginesList) {
            rec.getEngine().synchronize();
        }
    }

    public void quitAll() {
        for (EngineRecord rec : enginesList) {
            rec.getEngine().quit();
        }
    }
}
