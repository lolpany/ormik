package lol.lolpany.ormik.beans;

import java.util.Map;

public abstract class Cache {

    protected abstract Map<Long, ? extends ICachable> getCache();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Cache that = (Cache) o;
        if (getCache().size() != that.getCache().size()) {
            return false;
        }
        for (Map.Entry<Long, ? extends ICachable> e : that.getCache().entrySet()) {
            if (e.getValue().equalsFully(getCache().get(e.getKey()))) {
                return false;
            }
        }
        return true;
    }
}
