package ai.observe.scheduler.entities;

import ai.observe.scheduler.models.TaskPriority;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Singleton;

import java.util.List;
import java.util.Map;

@Singleton
public class InMemoryTask implements TaskCache{
    private Map<Long, Map<TaskPriority, List<String>>> taskMap = Maps.newHashMap();

    public void add(Long timeStamp, String taskId, TaskPriority taskPriority) {
        if(taskMap.containsKey(timeStamp)) {
            Map<TaskPriority, List<String>> priorityListMap = taskMap.get(timeStamp);
            if(priorityListMap.containsKey(taskPriority)) {
                priorityListMap.get(taskPriority).add(taskId);
            }
            else {
                priorityListMap.put(taskPriority, Lists.newArrayList(taskId));
            }
        }
        else {
            taskMap.put(timeStamp, Maps.newHashMap());
            taskMap.get(timeStamp).put(taskPriority, Lists.newArrayList(taskId));
        }
    }

    public Map<TaskPriority, List<String>> get(Long timeStamp) {
        if (taskMap.containsKey(timeStamp)) {
            Map<TaskPriority, List<String>> values = taskMap.get(timeStamp);
            taskMap.remove(timeStamp);
            return values;
        }
        else {
            return Maps.newHashMap();
        }
    }

    public void remove(Long timeStamp, String taskId, TaskPriority taskPriority) {
        if(taskMap.containsKey(timeStamp)) {
            Map<TaskPriority, List<String>> priorityListMap = taskMap.get(timeStamp);
            priorityListMap.get(taskPriority).remove(taskId);
            if(priorityListMap.get(taskPriority).size() == 0) {
                priorityListMap.remove(taskPriority);
            }
            if(priorityListMap.size() == 0) {
                taskMap.remove(timeStamp);
            }

        }
    }
}
