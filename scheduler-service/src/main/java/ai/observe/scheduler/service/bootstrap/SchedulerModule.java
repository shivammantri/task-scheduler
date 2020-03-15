package ai.observe.scheduler.service.bootstrap;

import ai.observe.scheduler.entities.InMemoryTask;
import ai.observe.scheduler.entities.TaskCache;
import ai.observe.scheduler.entities.dao.ExecutionDao;
import ai.observe.scheduler.entities.dao.ExecutionDaoImpl;
import ai.observe.scheduler.entities.dao.TaskDao;
import ai.observe.scheduler.entities.dao.TaskDaoImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.persist.jpa.JpaPersistModule;

public class SchedulerModule extends AbstractModule {
    private final ObjectMapper objectMapper;

    public SchedulerModule(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    protected void configure() {
        bind(ObjectMapper.class).toInstance(objectMapper);
        install(new JpaPersistModule("sql"));
        bind(TaskDao.class).to(TaskDaoImpl.class);
        bind(ExecutionDao.class).to(ExecutionDaoImpl.class);
        bind(TaskCache.class).to(InMemoryTask.class);
    }
}
