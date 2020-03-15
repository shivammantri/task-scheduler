package ai.observe.scheduler.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.MappedSuperclass;
import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@MappedSuperclass
public class Base implements Serializable {
    private Date timeStamp;
}
