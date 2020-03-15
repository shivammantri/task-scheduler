package ai.observe.scheduler.entities;

import ai.observe.scheduler.models.Schedule;
import ai.observe.scheduler.models.TaskPriority;
import ai.observe.scheduler.models.TaskType;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;

@Getter
@Setter
@DynamicUpdate
@Entity
@Table(name = "Task", indexes = {
        @Index(columnList = "taskStatus")
})
public class Task extends Base {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(unique = true)
    private String taskId;
    @Enumerated(value = EnumType.STRING)
    private TaskType taskType;
    @Enumerated(value = EnumType.STRING)
    private TaskPriority taskPriority;
    private Integer durationInSec;
    private Long executionTime;
    @Embedded
    private Schedule schedule;
    @Enumerated(value = EnumType.STRING)
    private TaskStatus taskStatus;
}
