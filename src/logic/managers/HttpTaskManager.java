package logic.managers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import logic.http.KVTaskClient;
import tasks.Epic;
import tasks.SubTask;
import tasks.Task;
import tasks.TypeOfTask;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class HttpTaskManager extends FileBackedTasksManager {
    private final KVTaskClient client;
    private final Gson gson;
    public HttpTaskManager(int port) {
        this(port, false);
    }

    public HttpTaskManager(int port, boolean load) {
        super(null);
        gson = Managers.getGson();
        client = new KVTaskClient(port);
        if (load) {
            load();
        }
    }

    //Отбираем поводья у FileBackedTasksManager'a и начинаем сохранять
    @Override
    public void save() {
        String jsonTasks = gson.toJson(new ArrayList<>(listOfTasks.values()));
        client.put("tasks", jsonTasks);
        String jsonSubtasks = gson.toJson(new ArrayList<>(getAllSubTaskFromAllEpics().values()));
        client.put("subtasks", jsonSubtasks);
        String jsonEpics = gson.toJson(new ArrayList<>(getEpics().values()));
        client.put("epics", jsonEpics);
        String jsonHistory = gson.toJson(historyManager.getHistory().stream().map(Task::getId).collect(Collectors.toList()));
        client.put("history", jsonHistory);
    }
    private void load() {
        ArrayList<Task> tasks = gson.fromJson(client.load("tasks"), new TypeToken<ArrayList<Task>>() {
        }.getType());
        addTasks(tasks);
        ArrayList<Epic> epics = gson.fromJson(client.load("epics"), new TypeToken<ArrayList<Epic>>() {
        }.getType());
        addTasks(epics);
        ArrayList<SubTask> subtasks = gson.fromJson(client.load("subtasks"), new TypeToken<ArrayList<SubTask>>() {
        }.getType());
        addTasks(subtasks);
        List<Integer> history = gson.fromJson(client.load("history"), new TypeToken<ArrayList<Integer>>() {
        }.getType());
        for (Integer taskId : history) {
            historyManager.add(findTask(taskId));
        }
    }
    protected Task findTask(Integer id) {
        final Task task = listOfTasks.get(id);
        if (task != null) {
            return task;
        }
        final SubTask subtask = listOfSubTasks.get(id);
        if (subtask != null) {
            return subtask;
        }
        return listOfEpics.get(id);
    }
    protected void addTasks(List<? extends Task> tasks) {
        for (Task task : tasks) {
            final int id = task.getId();
            if (id > getIdGen()) {
                setIdGen(id);
            }
            TypeOfTask type = TypeOfTask.valueOf(task.getType());
            if (type == TypeOfTask.TASK) {
                this.listOfTasks.put(id, task);
                getPrioritizedTasks().add(task);
            } else if (type == TypeOfTask.SUBTASK) {
                listOfSubTasks.put(id, (SubTask) task);
                getPrioritizedTasks().add(task);
            } else if (type == TypeOfTask.EPIC) {
                listOfEpics.put(id, (Epic) task);
            }
        }
    }

}