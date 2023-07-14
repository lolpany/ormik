package lol.lolpany.ormik.persistence;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;


public class EntityManagerFactoryHolder {

    private static class Holder {
        private static final EntityManagerFactory ENTITY_MANAGER_FACTORY = Persistence.createEntityManagerFactory("production");
    }

    public static EntityManagerFactory getEntityManagerFactory() {
        return Holder.ENTITY_MANAGER_FACTORY;
    }

}
