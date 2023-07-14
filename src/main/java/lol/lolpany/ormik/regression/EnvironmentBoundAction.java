package lol.lolpany.ormik.regression;

import java.util.concurrent.Callable;

public abstract class EnvironmentBoundAction<V> implements Callable<V> {
    private Integer envNumber;

    public EnvironmentBoundAction(Integer envNumber) {
        this.envNumber = envNumber;
    }

    public V call() throws Exception {
        return runOn(envNumber);
    }

    protected abstract V runOn(Integer envNumber) throws Exception;
}
