package ai.observe.scheduler.service.helper;

import ai.observe.scheduler.entities.Task;
import ai.observe.scheduler.entities.TaskStatus;
import ai.observe.scheduler.models.AddTaskRequest;
import ai.observe.scheduler.models.Schedule;
import ai.observe.scheduler.models.TaskType;
import org.joda.time.DateTime;

import java.util.Date;


public class TaskTransformer {
    public static Task transformToEntity(AddTaskRequest request) {
        Task task = new Task();
        DateTime currentTime = new DateTime();
        DateTime timeToBeSet = new DateTime(currentTime.getYear(), currentTime.getMonthOfYear(), currentTime.getDayOfMonth(),
                currentTime.getHourOfDay(), (currentTime.getMinuteOfHour()+1)%60);
        task.setTimeStamp(new Date(currentTime.getMillis()));
        task.setTaskId(request.getTaskId());
        task.setTaskType(request.getTaskType());
        task.setTaskPriority(request.getTaskPriority());
        task.setDurationInSec(request.getDurationInSec());
        if(task.getTaskType() == TaskType.A) {
            task.setExecutionTime(timeToBeSet.plusMinutes(request.getExecutionTimeFromNow()).getMillis());
        }
        if(task.getTaskType() == TaskType.B) {
            task.setSchedule(request.getSchedule());
            task.setExecutionTime(TaskTransformer.executionTimeTransformer(timeToBeSet, task.getSchedule()));
        }
        task.setTaskStatus(TaskStatus.ACTIVE);
        return task;
    }

    public static Long executionTimeTransformer(DateTime currentTime, Schedule schedule) {
        Integer minuteOfHour = null;
        Integer hourOfDay = null;
        Integer dayOfWeek = null;
        DateTime executionTime = currentTime;
        if(!schedule.getDay().equals("*")) {
            dayOfWeek = Integer.parseInt(schedule.getDay());
            if(dayOfWeek < currentTime.getDayOfWeek()) {
                dayOfWeek += 7;
            }
            executionTime = executionTime.plusDays(dayOfWeek-currentTime.getDayOfWeek());
        }
        if(!schedule.getHour().equals("*")) {
            hourOfDay = Integer.parseInt(schedule.getHour());
            executionTime = executionTime.withHourOfDay(hourOfDay);
        }
        if(!schedule.getMinute().equals("*")) {
            minuteOfHour = Integer.parseInt(schedule.getMinute());
            executionTime = executionTime.withMinuteOfHour(minuteOfHour);
        }
        if(executionTime.getMillis() <= currentTime.getMillis()) {
            if(minuteOfHour == null) {
                executionTime = executionTime.plusMinutes(1);
            }
            if(executionTime.getMillis() <= currentTime.getMillis()) {
                if(minuteOfHour == null) {
                    executionTime = executionTime.withMinuteOfHour(0);
                }
                if(hourOfDay == null) {
                    executionTime = executionTime.plusHours(1);
                }
                if(executionTime.getMillis() <= currentTime.getMillis()) {
                    if(hourOfDay == null) {
                        executionTime = executionTime.withHourOfDay(0);
                    }
                    if(dayOfWeek == null) {
                        executionTime = executionTime.plusDays(1);
                    }
                    else {
                        executionTime = executionTime.plusDays(7);
                    }
                }
            }
        }
        return  executionTime.getMillis();
    }
}
