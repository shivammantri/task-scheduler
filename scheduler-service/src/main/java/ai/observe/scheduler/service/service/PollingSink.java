package ai.observe.scheduler.service.service;

import ai.observe.scheduler.entities.TaskCache;
import ai.observe.scheduler.entities.TaskStatus;
import ai.observe.scheduler.entities.dao.TaskDao;
import ai.observe.scheduler.models.TaskPriority;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Singleton
public class PollingSink {
    private final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(1);
    private final TaskCache taskCache;
    private final TaskDao taskDao;
    private final TaskSchedulerService taskSchedulerService;

    @Inject
    public PollingSink(TaskCache taskCache, TaskSchedulerService taskSchedulerService, TaskDao taskDao) {
        this.taskCache = taskCache;
        this.taskSchedulerService = taskSchedulerService;
        this.taskDao = taskDao;
    }

    public void poll() {
        final Runnable poller = () -> {
            DateTime currentTime = new DateTime();
            DateTime timeToGet = new DateTime(currentTime.getYear(), currentTime.getMonthOfYear(), currentTime.getDayOfMonth(),
                    currentTime.getHourOfDay(), currentTime.getMinuteOfHour());
            Map<TaskPriority, List<String>> taskPriorityListMap = taskCache.get(timeToGet.getMillis());
            log.info("Scheduling batch at " + timeToGet.getMillis() + " taskIds :: " + taskPriorityListMap);
            schedule(taskPriorityListMap);
        };
        scheduler.scheduleAtFixedRate(poller,0,1,TimeUnit.MINUTES);
    }

    private void schedule(Map<TaskPriority, List<String>> taskPriorityListMap) {
        if(taskPriorityListMap.containsKey(TaskPriority.HIGH)) {
            taskPriorityListMap.get(TaskPriority.HIGH).stream()
                    .map(taskDao::getTaskById)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .filter(task -> task.getTaskStatus() != TaskStatus.INACTIVE)
                    .forEach(taskSchedulerService::addToHighPriorityQueue);
        }

        if(taskPriorityListMap.containsKey(TaskPriority.MEDIUM)) {
            taskPriorityListMap.get(TaskPriority.MEDIUM).stream()
                    .map(taskDao::getTaskById)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .filter(task -> task.getTaskStatus() != TaskStatus.INACTIVE)
                    .forEach(taskSchedulerService::addToMediumPriorityQueue);
        }

        if(taskPriorityListMap.containsKey(TaskPriority.LOW)) {
            taskPriorityListMap.get(TaskPriority.LOW).stream()
                    .map(taskDao::getTaskById)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .filter(task -> task.getTaskStatus() != TaskStatus.INACTIVE)
                    .forEach(taskSchedulerService::addToLowPriorityQueue);
        }
    }
}
