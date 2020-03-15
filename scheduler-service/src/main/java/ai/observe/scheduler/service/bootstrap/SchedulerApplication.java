package ai.observe.scheduler.service.bootstrap;

import ai.observe.scheduler.models.exceptions.SchedulerExceptionMapper;
import ai.observe.scheduler.service.resource.TaskResource;
import ai.observe.scheduler.service.service.PollingSink;
import ai.observe.scheduler.service.service.TaskSchedulerService;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.persist.PersistService;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import lombok.extern.slf4j.Slf4j;

import java.util.TimeZone;

@Slf4j
public class SchedulerApplication extends Application<SchedulerConfiguration>{
    private ObjectMapper objectMapper;


    public static void main(String[] args) throws Exception{
        new SchedulerApplication().run(args);
    }

    public void run(SchedulerConfiguration schedulerConfiguration, Environment environment) {
        Injector injector = Guice.createInjector(new SchedulerModule(objectMapper));
        environment.jersey().register(injector.getInstance(TaskResource.class));
        environment.jersey().register(injector.getInstance(SchedulerExceptionMapper.class));
        injector.getInstance(PersistService.class).start();
        PollingSink pollingSink = injector.getInstance(PollingSink.class);
        pollingSink.poll();
        TaskSchedulerService taskSchedulerService = injector.getInstance(TaskSchedulerService.class);
        taskSchedulerService.run();
        taskSchedulerService.checkStarvation();
        log.info("Application is up!");
        //On shutdown stop executor of polling sink
    }

    @Override
    public void initialize(Bootstrap<SchedulerConfiguration> bootstrap) {
        objectMapper = bootstrap.getObjectMapper();
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        SimpleModule simpleModule = new SimpleModule();
        objectMapper.registerModule(simpleModule);
        objectMapper.setTimeZone(TimeZone.getDefault());
    }
}