package ai.observe.scheduler.service.resource;

import ai.observe.scheduler.entities.TaskStatus;
import ai.observe.scheduler.models.AddTaskRequest;
import ai.observe.scheduler.models.exceptions.SchedulerException;
import ai.observe.scheduler.service.service.TaskService;
import com.google.inject.Inject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Path("/task")
@Produces(MediaType.APPLICATION_JSON)
public class TaskResource {
    private final TaskService taskService;

    @Inject
    public TaskResource(TaskService taskService) {
        this.taskService = taskService;
    }

    @POST
    @Path("/schedule")
    @Consumes(MediaType.APPLICATION_JSON)
    public void addTask(AddTaskRequest request) {
        taskService.addTask(request);
    }

    @POST
    @Path("/inactive/{taskId}")
    public void inactiveTask(@PathParam("taskId") String taskId) {
        taskService.inactiveTask(taskId);
    }

    @GET
    @Path("/{taskStatus}")
    public List<String> getTaskByStatus(@PathParam("taskStatus") TaskStatus taskStatus) {
        return taskService.getTasksByStatus(taskStatus);
    }

    @GET
    @Path("/executions")
    public List<String> getExecutionsByTime(@QueryParam("min") String min, @QueryParam("max") String max) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date minDate = null;
        Date maxDate = null;
        try {
            minDate = formatter.parse(min);
            maxDate = formatter.parse(max);
        } catch (ParseException e) {
            throw new SchedulerException("Error parsing date. Bad request. Format yyyy-MM-dd'T'HH:mm:ss", Response.Status.BAD_REQUEST);
        }
        return taskService.getTasksExecutionByTime(minDate, maxDate);
    }
}
