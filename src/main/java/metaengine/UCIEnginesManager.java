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
    private final Object enginesListMutex = new Object();
    private final List<EngineRecord> enginesList;

    public static UCIEnginesManager create(Configuration conf) {
        try { // FIXME: Use real exception handling
        ExecutorService pool =
          Executors.newCachedThreadPool();
        List<List<String>> listOfArgLists =
          conf.getUnmodifiableEngineArguments();

        List<Future<EngineRecord>> futureEngines =
          new ArrayList<Future<EngineRecord>>();
        int idx = 0;
        for (List<String> argsList : listOfArgLists) {
            int tempIdx = idx;
            Callable<EngineRecord> constructEngine = () -> {
                return new EngineRecord(new UCIEngine(argsList), tempIdx);
            };
            futureEngines.add(pool.submit(constructEngine));
            ++idx;
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
        } catch (Exception e) {
            throw new RuntimeException("This is for testing purposes only", e);
        }
    }

    private static class EngineRecord {
        private final UCIEngine engine;
        private final int index;
        public EngineRecord(UCIEngine engine, int idx) {
            this.engine = engine;
            index = idx;
        }

        public UCIEngine getEngine() {
            return engine;
        }

        public int getIndex() {
            return index;
        }
    }

    private UCIEnginesManager(List<EngineRecord> toManage) {
        enginesList = toManage;
    }

    public List<UCIOptionBundle> getUCIOptions() {
        synchronized (enginesListMutex) {
            List<UCIOptionBundle> ret = new ArrayList<UCIOptionBundle>();
            for (EngineRecord rec : enginesList) {
                UCIEngine engine = rec.getEngine();
                ret.add(new UCIOptionBundle(engine.getOptions(),
                  engine.getName(), rec.index));
            }
            return ret;
        }
    }

    public void dispatchOption(SetoptionInfo setoptionInfo) {
        synchronized (enginesListMutex) {
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
    }

    private static class SearchThread extends Thread {
        private final SearchInfo info;
        public SearchThread(SearchInfo info) {
            this.info = info;
        }

        @Override
        public void run() {
            info.setBestMove(new UCIMove("e2e4"));
        }
    }


    public SearchInfo search(UCIGo params) {
        SearchInfo result = new SearchInfo();
        new SearchThread(result).run();
        return result;
    }

    public void synchronizeAll() {
        synchronized (enginesListMutex) {
            for (EngineRecord rec : enginesList) {
                rec.getEngine().synchronize();
            }
        }
    }

    public void quitAll() {
        synchronized (enginesListMutex) {
            for (EngineRecord rec : enginesList) {
                rec.getEngine().quit();
            }
        }
    }
}
