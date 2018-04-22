package metaengine;

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.Callable;

public class ThreadPool {
    private final ExecutorService pool;

    public ThreadPool() {
        pool = Executors.newCachedThreadPool();
    }

    public <T> Future<T> submit(Callable<T> task) {
        pool.submit(task);
    }
}
