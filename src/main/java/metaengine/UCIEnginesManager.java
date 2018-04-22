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

        List<Future<UCIEngine>> futureEngines =
          new ArrayList<Future<UCIEngine>>();
        for (List<String> argsList : listOfArgLists) {
            Callable<UCIEngine> constructEngine = () -> {
                return new UCIEngine(argsList);
            };
            futureEngines.add(pool.submit(constructEngine));
        }

        List<UCIEngine> engines = new ArrayList<UCIEngine>();
        for (Future<UCIEngine> future : futureEngines) {
            engines.add(future.get());
        }

        for (UCIEngine engine : engines) {
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

    private final List<UCIEngine> enginesList;

    private UCIEnginesManager(List<UCIEngine> toManage) {
        enginesList = toManage;
    }
}
