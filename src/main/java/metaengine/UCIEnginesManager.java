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
            ExecutorService pool =
              Executors.newCachedThreadPool();
              List<Configuration.EngineConfiguration> engineConfigs =
                conf.getEngineConfigurations();

            List<Future<EngineRecord>> futureEngines =
              new ArrayList<Future<EngineRecord>>();
            for (Configuration.EngineConfiguration engineConf : engineConfigs) {
                Callable<EngineRecord> constructEngine = () -> {
                    return new EngineRecord(new UCIEngine(
                      engineConf.getEngineArgv()), engineConf);
                };
                futureEngines.add(pool.submit(constructEngine));
            }

            List<EngineRecord> engines = new ArrayList<EngineRecord>();
            for (Future<EngineRecord> future : futureEngines) {
                engines.add(future.get());
            }

            pool.shutdown();
            if (!pool.awaitTermination(1, TimeUnit.SECONDS)) {
                pool.shutdownNow();
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

    private class SearchThread extends Thread {
        private final SearchInfo info;
        private final UCIGo goInfo;
        public SearchThread(SearchInfo info, UCIGo goInfo) {
            this.info = info;
            this.goInfo = goInfo;
        }

        @Override
        public void run() {
            UCIEngine timerEngine = timerRecord.getEngine();
            //timerEngine.go();
            long timerStart = System.nanoTime();
            for (EngineRecord rec : recomenderRecords) {
                Configuration.EngineConfiguration conf = rec.getConfig();

                // TODO: We're not in Kansas anymore
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
        new SearchThread(result, params).run();
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
