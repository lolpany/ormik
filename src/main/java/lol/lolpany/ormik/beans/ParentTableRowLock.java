package lol.lolpany.ormik.beans;

import static lol.lolpany.ormik.beans.LockingResult.ALREADY_LOCKED;

public class ParentTableRowLock<I> {

    private final LockingResult  lockingResult;
    private final I bean;

    public ParentTableRowLock(LockingResult lockingResult, I bean) {
        this.lockingResult = lockingResult;
        this.bean = bean;
    }

    public I get() {
        if (lockingResult == ALREADY_LOCKED) {
            throw new RuntimeException("Row is locked. Result is unavailable");
        }
        return bean;
    }

    public LockingResult getLockingResult() {
        return lockingResult;
    }
}