package lol.lolpany.ormik.regression;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class ActionsExecutor<V> {

    // todo use int controlEnv, int testedEnv, Config config parameters for deduplication
    public Pair<V, V> run(EnvironmentBoundAction<V> actionOnControl, EnvironmentBoundAction<V> actionOnTested)
            throws InterruptedException, ExecutionException {
        ExecutorService executorService = new ThreadPoolExecutor(2, 2, 0, TimeUnit.SECONDS, new ArrayBlockingQueue<>(2));
        List<Future<V>> futures = executorService.invokeAll(new ArrayList<EnvironmentBoundAction<V>>() {{
            add(actionOnControl);
            add(actionOnTested);
        }});
        executorService.shutdown();
        return new ImmutablePair<>(futures.get(0).get(), futures.get(1).get());
    }
}
