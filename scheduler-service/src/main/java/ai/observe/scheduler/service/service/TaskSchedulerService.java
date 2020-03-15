package ai.observe.scheduler.service.service;

import ai.observe.scheduler.entities.*;
import ai.observe.scheduler.entities.dao.ExecutionDao;
import ai.observe.scheduler.models.TaskPriority;
import ai.observe.scheduler.models.TaskType;
import ai.observe.scheduler.service.helper.TaskTransformer;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;

import java.util.Date;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Singleton
@Slf4j
public class TaskSchedulerService {
    private Queue<Task> highPriority = new ConcurrentLinkedQueue<>();
    private Queue<Task> mediumPriority = new ConcurrentLinkedQueue<>();
    private Queue<Task> lowPriority = new ConcurrentLinkedQueue<>();
    private final TaskService taskService;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final ExecutorService starvationExecutor = Executors.newSingleThreadExecutor();
    private static final Integer MAX_TIME_IN_QUEUE = 5;
    private final TaskCache taskCache;
    private final ExecutionDao executionDao;

    @Inject
    public TaskSchedulerService(TaskService taskService, TaskCache taskCache, ExecutionDao executionDao) {
        this.taskService = taskService;
        this.taskCache = taskCache;
        this.executionDao = executionDao;
    }

    public void addToHighPriorityQueue(Task task) {
        highPriority.add(task);
    }

    public void addToMediumPriorityQueue(Task task) {
        mediumPriority.add(task);
    }

    public void addToLowPriorityQueue(Task task) {
        lowPriority.add(task);
    }

    public void run() {
        Runnable runnable = this::schedule;
        executorService.submit(runnable);
    }

    public void checkStarvation() {
        Runnable runnable = this::checkAndUpdateQueue;
        starvationExecutor.submit(runnable);
    }

    private void schedule() {
        while (true) {
            Task taskToBeScheduled = null;
            if (!highPriority.isEmpty()) {
                taskToBeScheduled = highPriority.poll();
            } else if (!mediumPriority.isEmpty()) {
                taskToBeScheduled = mediumPriority.poll();
            } else if (!lowPriority.isEmpty()) {
                taskToBeScheduled = lowPriority.poll();
            }
            if (taskToBeScheduled != null) {
                try {
                    log.info("Executing task :: " + taskToBeScheduled.getTaskId());
                    Thread.sleep(taskToBeScheduled.getDurationInSec() * 1000);
                    log.info("Finished executing task :: " + taskToBeScheduled.getTaskId());
                } catch (InterruptedException e) {
                    log.info("Thread interrupted!");
                }
                Execution execution = new Execution();
                execution.setTaskId(taskToBeScheduled.getTaskId());
                execution.setTimeStamp(new Date());
                executionDao.create(execution);
                if (taskToBeScheduled.getTaskType() == TaskType.A) {
                    taskToBeScheduled.setTaskStatus(TaskStatus.COMPLETED);
                    taskService.updateTask(taskToBeScheduled);
                } else if(taskToBeScheduled.getTaskType() == TaskType.B) {
                    DateTime oldExecutionTime = new DateTime(taskToBeScheduled.getExecutionTime());
                    Long newExecutionTime = TaskTransformer.executionTimeTransformer(oldExecutionTime, taskToBeScheduled.getSchedule());
                    taskToBeScheduled.setExecutionTime(newExecutionTime);
                    taskService.updateTask(taskToBeScheduled);
                    taskCache.add(newExecutionTime, taskToBeScheduled.getTaskId(), taskToBeScheduled.getTaskPriority());
                }
            }
        }
    }

    private void checkAndUpdateQueue() {
        while(true) {
            if(!lowPriority.isEmpty()) {
                Task task = lowPriority.peek();
                if(task != null &&
                        new DateTime(task.getExecutionTime()).plusMinutes(MAX_TIME_IN_QUEUE).getMillis() >
                        new DateTime().getMillis()) {
                    task = lowPriority.poll();
                    if(task != null) {
                        mediumPriority.add(task);
                    }
                }
            }
            if(!mediumPriority.isEmpty()) {
                Task task = mediumPriority.peek();
                if(task != null &&
                        ((task.getTaskPriority() == TaskPriority.MEDIUM &&
                        new DateTime(task.getExecutionTime()).plusMinutes(MAX_TIME_IN_QUEUE).getMillis() >
                                new DateTime().getMillis()) ||
                        (task.getTaskPriority() == TaskPriority.LOW &&
                                new DateTime(task.getExecutionTime()).plusMinutes(2*MAX_TIME_IN_QUEUE).getMillis() >
                                        new DateTime().getMillis()))
                        ) {
                    task = mediumPriority.poll();
                    if(task != null) {
                        highPriority.add(task);
                    }
                }
            }
        }
    }

}
