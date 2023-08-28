import logic.http.HttpTaskServer;
import logic.http.KVServer;
import logic.managers.HttpTaskManager;
import logic.managers.TaskManager;
import tasks.Epic;
import tasks.SubTask;
import tasks.Task;

import java.io.IOException;
import java.time.LocalDateTime;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        new KVServer().start();
        TaskManager mng = new HttpTaskManager(8078);
        Task task = new Task("TestTask", "TestTask description", LocalDateTime.now(), 15);
        mng.createTask(task);
        Epic epic = new Epic("TestEpic", "EpicDesc");
        mng.createEpic(epic);
        SubTask subtask = new SubTask("Test Subtask", "Test description", LocalDateTime.now().plusMinutes(10), 30, epic.getId());
        mng.createSubTask(subtask);
        System.out.println(mng.getAllTasks());
        System.out.println(mng.getEpics());
        System.out.println(mng.getAllSubTaskFromAllEpics());
        HttpTaskServer server = new HttpTaskServer(mng);
        server.start();
    }
}







