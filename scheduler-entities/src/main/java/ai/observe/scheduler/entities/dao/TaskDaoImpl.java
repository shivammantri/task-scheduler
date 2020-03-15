package ai.observe.scheduler.entities.dao;

import ai.observe.scheduler.entities.Task;
import ai.observe.scheduler.entities.TaskStatus;
import com.google.inject.Inject;
import com.google.inject.Provider;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

public class TaskDaoImpl extends BaseDaoImpl<Task, Long> implements TaskDao {
    @Inject
    public TaskDaoImpl(Provider<EntityManager> entityManagerProvider) {
        super(entityManagerProvider);
    }

    @Override
    public Optional<Task> getTaskById(String taskId) {
        Session session = getEntityManager().unwrap(Session.class);
        if(!session.isOpen()) {
            session = session.getSessionFactory().openSession();
        }
        Criteria criteria = session.createCriteria(getEntityClass());
        criteria.add(Restrictions.and(Restrictions.eq("taskId", taskId)));
        Task task = (Task) criteria.uniqueResult();
        session.close();
        return Optional.ofNullable(task);
    }

    @Override
    public List<Task> getTaskByStatus(TaskStatus taskStatus) {
        Criteria criteria = getEntityManager().unwrap(Session.class).createCriteria(getEntityClass());
        criteria.add(Restrictions.and(Restrictions.eq("taskStatus", taskStatus)));
        List<Task> tasks = criteria.list();
        return tasks;
    }
}
