package tests;

import logic.http.KVServer;
import logic.managers.HttpTaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HttpTaskManagerTest extends TaskManagerTest<HttpTaskManager> {
    private final KVServer server = new KVServer();

    public HttpTaskManagerTest() throws IOException {
    }

    @BeforeEach
    void setUp() throws IOException, InterruptedException {
        server.start();
        mng = new HttpTaskManager(8078);
        addTasks();
    }

    @AfterEach
    void tearDown() {
        server.stop();
    }

    @Test
    void shouldBeTestSaveEmptyTasksToServer() {
        mng.save();
        HttpTaskManager restoredManager = new HttpTaskManager(8078, true);
        assertEquals(mng, restoredManager, "Менеджеры не совпадает");
    }

    @Test
    void shouldBeTestOnlyAddTaskToServer()  {
        HttpTaskManager loadedTaskManager = new HttpTaskManager(8078, true);
        assertEquals(mng, loadedTaskManager, "Менеджеры не совпадают");
    }

    @Test
    void shouldBeTestAddAndGetTaskAndHistoryServer() throws IOException, InterruptedException {
        mng.getTaskById(task.getId());
        HttpTaskManager loadedTaskManager = new HttpTaskManager(8078, true);
        assertEquals(mng, loadedTaskManager, "Менеджеры не совпадают");
    }

    @Test
    void shouldBeTestAllTasksMap() throws IOException, InterruptedException {
        mng.getTaskById(task.getId());
        mng.getSubtaskById(subtask.getId());
        mng.getEpicById(epic.getId());
        HttpTaskManager loadedTaskManager = new HttpTaskManager(8078, true);
        assertEquals(mng.getAllTasks(), loadedTaskManager.getAllTasks(), "Task HashMap не совпадает");
        assertEquals(mng.getEpics(), loadedTaskManager.getEpics(), "Epic HashMap не совпадает");
        assertEquals(mng.getAllSubTaskFromAllEpics(), loadedTaskManager.getAllSubTaskFromAllEpics(), "Subtask HashMap не совпадает");
        assertEquals(mng.getPrioritizedTasks(), loadedTaskManager.getPrioritizedTasks(),
                "Сортированный список не совпадает");
        assertEquals(mng.getHistory(), loadedTaskManager.getHistory(), "Истории не совпадают");
    }

}