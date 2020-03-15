package ai.observe.scheduler.entities.dao;

import ai.observe.scheduler.entities.Execution;
import com.google.inject.Inject;
import com.google.inject.Provider;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import javax.persistence.EntityManager;
import java.util.Date;
import java.util.List;

public class ExecutionDaoImpl extends BaseDaoImpl<Execution, Long> implements ExecutionDao {
    @Inject
    public ExecutionDaoImpl(Provider<EntityManager> entityManagerProvider) {
        super(entityManagerProvider);
    }

    @Override
    public List<Execution> getExecutionsByTime(Date min, Date max) {
        Criteria criteria = getEntityManager().unwrap(Session.class).createCriteria(getEntityClass());
        criteria.add(Restrictions.and(Restrictions.le("timeStamp", max),
                Restrictions.ge("timeStamp", min)));
        List<Execution> executions = criteria.list();
        return executions;
    }
}
