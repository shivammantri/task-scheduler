package ai.observe.scheduler.entities.dao;

import ai.observe.scheduler.entities.Task;
import ai.observe.scheduler.entities.TaskStatus;

import java.util.List;
import java.util.Optional;

public interface TaskDao extends BaseDao<Task, Long> {
    Optional<Task> getTaskById(String taskId);
    List<Task> getTaskByStatus(TaskStatus taskStatus);
}
