package logic.managers;

import history.HistoryManager;
import tasks.Epic;
import tasks.SubTask;
import tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;


public interface TaskManager {
    HashMap<Integer, Epic> getEpics();

    Task createTask(Task newTask) throws IOException, InterruptedException;

    void updateTask(Task task);

    HashMap<Integer, Task> getAllTasks();

    void delAllTasks();

    void delTaskById(int taskId) throws IOException, InterruptedException;

    Task getTaskById(int id) throws IOException, InterruptedException;

    Epic createEpic(Epic newEpic) throws IOException, InterruptedException;

    void updateEpic(Epic epic);

    void delEpicById(int id) throws IOException, InterruptedException;

    void resetEpicStatus(Epic epic);

    SubTask createSubTask(SubTask newSubTask) throws IOException, InterruptedException;

    Integer updateSubTask(SubTask subTask);

    ArrayList<SubTask> getAllSubTasksByEpic(Epic epic);

    HashMap<Integer, SubTask> getAllSubTaskFromAllEpics();

    void delAllSubTasksFromAllEpics();

    HashMap<Integer, SubTask> delSubTaskById(int codeOfTask) throws IOException, InterruptedException;

    SubTask getSubTaskById(int id);

    void delAllSubTasksByEpic(Epic epic);

    Epic getEpicById(int id) throws IOException, InterruptedException;

    TreeSet<Task> getPrioritizedTasks();

    List<Task> getHistory();

    HistoryManager getHistoryManager();

    void setIdGen(int i);

    List<String> showAllTasks();
}
