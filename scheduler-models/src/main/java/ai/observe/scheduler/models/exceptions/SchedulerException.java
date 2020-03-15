package ai.observe.scheduler.models.exceptions;

import lombok.Data;

import javax.ws.rs.core.Response;

@Data
public class SchedulerException extends RuntimeException{
    private Response.Status status;
    private String message;

    public SchedulerException() {
        super();
    }

    public SchedulerException(String message) {
        super(message);
        this.message=message;
        status=Response.Status.INTERNAL_SERVER_ERROR;
    }

    public SchedulerException(String message, Response.Status status) {
        super(message);
        this.message=message;
        this.status=status;
    }

    public SchedulerException(String message, Throwable cause) {
        super(message,cause);
        this.message=message;
        this.status=Response.Status.INTERNAL_SERVER_ERROR;
    }

    protected SchedulerException(String message, Response.Status status, Throwable cause) {
        super(message, cause);
        this.message=message;
        this.status=status;
    }

}
