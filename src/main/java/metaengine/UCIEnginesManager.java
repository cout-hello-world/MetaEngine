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

    private final List<EngineRecord> enginesList;

    private UCIEnginesManager(List<EngineRecord> toManage) {
        enginesList = toManage;
    }

    public List<UCIOptionBundle> getUCIOptions() {
        List<UCIOptionBundle> ret = new ArrayList<UCIOptionBundle>();
        for (EngineRecord rec : enginesList) {
            UCIEngine engine = rec.getEngine();
            ret.add(new UCIOptionBundle(engine.getOptions(),
              engine.getName(), rec.index));
        }
        return ret;
    }

    public void quitAll() throws IOException {
        for (EngineRecord rec : enginesList) {
            rec.getEngine().quit();
        }
    }
}
