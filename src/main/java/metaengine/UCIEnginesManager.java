package metaengine;

import java.util.List;
import java.util.ArrayList;

import java.util.concurrent.Future;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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

        for (EngineRecord engineRecord : engines) {
            UCIEngine engine = engineRecord.getEngine();
            System.out.println("Name: " + engine.getInvokedName());
            System.out.println("uid: " + engine.getUniqueId());
            for (UCIOption opt : engine.getOptions()) {
                System.out.println(opt.getOptionString());
                System.out.println(opt.getSetoptionString());
            }
            engine.quit();
            System.out.println();
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
    }

    private final List<EngineRecord> enginesList;

    private UCIEnginesManager(List<EngineRecord> toManage) {
        enginesList = toManage;
    }
}
