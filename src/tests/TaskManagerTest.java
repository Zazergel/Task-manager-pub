package tests;

import logic.managers.TaskManager;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.StatusOfTask;
import tasks.SubTask;
import tasks.Task;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

abstract class TaskManagerTest<T extends TaskManager> {
    protected T mng;
    Task task;
    Epic epic;
    SubTask subtask;

    protected void addTasks() throws IOException, InterruptedException {
        mng.setIdGen(1);
        task = new Task("TestTask", "TestTask description", LocalDateTime.now(), 15);
        mng.createTask(task);
        epic = new Epic(2, "EN1", StatusOfTask.NEW, "ED1", 10, LocalDateTime.now().plusMinutes(10), LocalDateTime.now().plusMinutes(20));
        mng.createEpic(epic);
        subtask = new SubTask("Test Subtask", "Test description", LocalDateTime.now().plusMinutes(10), 30, epic.getId());
        mng.createSubTask(subtask);
    }

    @Test
    void getTasks() {
        final HashMap<Integer, Task> tasks = mng.getAllTasks();
        final HashMap<Integer, Epic> epics = mng.getEpics();
        final HashMap<Integer, SubTask> subtasks = mng.getAllSubTaskFromAllEpics();

        assertNotNull(tasks, "Задачи не возвращаются");
        assertNotNull(epics, "Эпики не возвращаются");
        assertNotNull(subtasks, "Подзадачи не возвращаются");
        assertEquals(1, tasks.size(), "Не верное количество задач");
        assertEquals(1, epics.size(), "Не верное количество задач");
        assertEquals(1, subtasks.size(), "Не верное количество задач");
        assertEquals(mng.getAllTasks().get(1), tasks.get(1), "Задачи не совпадают");
        assertEquals(mng.getEpics().get(2), epics.get(2), "Задачи не совпадают");
        assertEquals(mng.getAllSubTaskFromAllEpics().get(3), subtasks.get(3), "Задачи не совпадают");
    }

}