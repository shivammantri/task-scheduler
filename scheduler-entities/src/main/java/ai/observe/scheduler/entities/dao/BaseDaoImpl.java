package ai.observe.scheduler.entities.dao;

import com.google.common.base.Preconditions;
import com.google.inject.Provider;
import org.hibernate.exception.ConstraintViolationException;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Optional;

public class BaseDaoImpl<ENTITY, IDENTIFIER extends Serializable> implements BaseDao<ENTITY, IDENTIFIER> {
    private final Provider<EntityManager> entityManagerProvider;

    public BaseDaoImpl(Provider<EntityManager> entityManagerProvider) {
        this.entityManagerProvider = entityManagerProvider;
    }

    @Override
    public Optional<ENTITY> find(IDENTIFIER id) {
        return Optional.ofNullable(getEntityManager().find(getEntityClass(), id));
    }

    @Override
    public void create(ENTITY entity) {
        try {
            getEntityManager().persist(entity);
        }catch (PersistenceException e){
            handlePersistenceException(e);
        }
    }

    protected Class<ENTITY> getEntityClass() {
        ParameterizedType genericSuperclass = (ParameterizedType) getGenericSuperClass();
        return (Class<ENTITY>) genericSuperclass
                .getActualTypeArguments()[0];
    }

    protected Type getGenericSuperClass() {
        Class klass = getClass();
        while (klass != null
                && klass.getSuperclass() != null
                && !klass.getSuperclass().isAssignableFrom(BaseDaoImpl.class)) {
            klass = klass.getSuperclass();
        }
        Preconditions.checkNotNull(klass);
        return klass.getGenericSuperclass();
    }

    private void handlePersistenceException(PersistenceException e) {
        Throwable cause = e.getCause();
        Class clazz = getEntityClass();
        if(e.getCause() != null && cause instanceof ConstraintViolationException){
            ConstraintViolationException constraintViolationException = (ConstraintViolationException) e.getCause();
            Throwable constraintViolationCause = constraintViolationException.getSQLException();
            if(constraintViolationCause != null && constraintViolationCause instanceof SQLIntegrityConstraintViolationException){
                throw new RuntimeException("Error while persisting " + clazz.getSimpleName() + ", error is : " + constraintViolationCause.getMessage(), e);
            }
        }
        throw new RuntimeException("Error while persisting " + clazz.getSimpleName() + ", error is : " + e.getLocalizedMessage(), e);
    }

    @Override
    public ENTITY merge(ENTITY entity) {
        return getEntityManager().merge(entity);
    }

    @Override
    public void update(ENTITY entity) {
        getEntityManager().persist(entity);
    }

    @Override
    public EntityManager getEntityManager() {
        return entityManagerProvider.get();
    }
}
