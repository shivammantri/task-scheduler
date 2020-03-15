package ai.observe.scheduler.entities;

import ai.observe.scheduler.models.TaskPriority;

import java.util.List;
import java.util.Map;

public interface TaskCache {
    void add(Long timeStamp, String taskId, TaskPriority taskPriority);
    Map<TaskPriority, List<String>> get(Long timeStamp);
    void remove(Long timeStamp, String taskId, TaskPriority taskPriority);
}
