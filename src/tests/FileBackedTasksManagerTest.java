package tests;

import logic.managers.FileBackedTasksManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.StatusOfTask;
import tasks.SubTask;
import tasks.Task;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;


class FileBackedTasksManagerTest {

    private static File file = new File("history.csv");
    private static FileBackedTasksManager fb2;

    @BeforeEach
    public void setUp() throws IOException, InterruptedException {
        file.delete();
        file = new File("history.csv");
        FileBackedTasksManager fb1 = new FileBackedTasksManager(file);
        fb1.setIdGen(1);
        Task task = new Task("TestTask", "TestTask description", LocalDateTime.now(), 15);
        fb1.createTask(task);
        Epic epic = new Epic(2, "EN1", StatusOfTask.NEW, "ED1", 10, LocalDateTime.now().plusMinutes(10), LocalDateTime.now().plusMinutes(20));
        fb1.createEpic(epic);
        SubTask subtask = new SubTask("Test Subtask", "Test description", LocalDateTime.now().plusMinutes(10), 30, epic.getId());
        fb1.createSubTask(subtask);
        fb1.getTaskById(1);
        fb1.getEpicById(2);
        fb2 = FileBackedTasksManager.loadDataFromFile(file);
    }

    @Test
    public void allTasksNotNull() {
        Assertions.assertNotNull(fb2.getAllTasks());
    }

    @Test
    public void historyNotNull() {
        Assertions.assertNotNull(fb2.getHistoryManager().getHistory(), "История не пустая");
    }

    @Test
    public void task1Equality() {
        Assertions.assertEquals("TestTask", fb2.getAllTasks().get(1).getName(),
                "Обнаружено несовпадение имени задачи");
    }
}