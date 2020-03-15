package ai.observe.scheduler.entities.dao;

import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.Optional;

public interface BaseDao<ENTITY, IDENTIFIER extends Serializable> {
    Optional<ENTITY> find(IDENTIFIER id);
    void create(ENTITY entity);
    ENTITY merge(ENTITY entity);
    void update(ENTITY entity);
    EntityManager getEntityManager();
}
