package ai.observe.scheduler.entities.dao;

import ai.observe.scheduler.entities.Execution;

import java.util.Date;
import java.util.List;

public interface ExecutionDao extends BaseDao<Execution, Long> {
    List<Execution> getExecutionsByTime(Date min, Date max);
}
