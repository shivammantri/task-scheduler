package ai.observe.scheduler.models;

import lombok.Data;

@Data
public class AddTaskRequest {
    private String taskId;
    private TaskType taskType;
    private TaskPriority taskPriority;
    private Integer durationInSec;
    private Integer executionTimeFromNow;
    private Schedule schedule;
}
