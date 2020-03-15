package ai.observe.scheduler.service.service;

import ai.observe.scheduler.entities.*;
import ai.observe.scheduler.entities.dao.ExecutionDao;
import ai.observe.scheduler.entities.dao.TaskDao;
import ai.observe.scheduler.models.AddTaskRequest;
import ai.observe.scheduler.models.exceptions.SchedulerException;
import ai.observe.scheduler.service.helper.TaskTransformer;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class TaskService {
    private final TaskDao taskDao;
    private final TaskCache taskCache;
    private final ExecutionDao executionDao;

    @Inject
    public TaskService(TaskDao taskDao, TaskCache taskCache, ExecutionDao executionDao) {
        this.taskDao = taskDao;
        this.taskCache = taskCache;
        this.executionDao = executionDao;
    }

    @Transactional
    public void addTask(AddTaskRequest request) {
        Task task = TaskTransformer.transformToEntity(request);
        taskDao.create(task);
        taskCache.add(task.getExecutionTime(), task.getTaskId(), task.getTaskPriority());
    }

    @Transactional
    public void inactiveTask(String taskId) {
        Optional<Task> task = taskDao.getTaskById(taskId);
        if(!task.isPresent()) {
            throw new SchedulerException("Task not found :: " + taskId, Response.Status.NOT_FOUND);
        }
        taskCache.remove(task.get().getExecutionTime(), taskId, task.get().getTaskPriority());
        task.get().setTaskStatus(TaskStatus.INACTIVE);
        taskDao.update(task.get());
    }

    @Transactional
    public void updateTask(Task task) {
        try {
            taskDao.merge(task);
        } catch (Exception e) {
            log.info("Exception occured while updating." + e);
        }
    }

    public List<String> getTasksByStatus(TaskStatus taskStatus) {
        return taskDao.getTaskByStatus(taskStatus).stream()
                .map(Task::getTaskId)
                .collect(Collectors.toList());
    }

    public List<String> getTasksExecutionByTime(Date min, Date max) {
        return executionDao.getExecutionsByTime(min, max).stream()
                .map(Execution::getTaskId)
                .collect(Collectors.toList());
    }
}
