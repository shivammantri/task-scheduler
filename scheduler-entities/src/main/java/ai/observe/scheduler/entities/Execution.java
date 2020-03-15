package ai.observe.scheduler.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "Execution", indexes = {
        @Index(columnList = "timeStamp")
})
public class Execution extends Base{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String taskId;
}
