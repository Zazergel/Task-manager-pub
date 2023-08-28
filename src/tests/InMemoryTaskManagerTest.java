package tests;

import exceptions.NotFoundException;
import exceptions.TaskValidationException;
import logic.managers.InMemoryTaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.StatusOfTask;
import tasks.SubTask;
import tasks.Task;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {

    @BeforeEach
    public void setMngAndTasks() throws IOException, InterruptedException {
        mng = new InMemoryTaskManager();
        addTasks();
    }

    @Test
    public void prioritizedTasksCheck() {
        Task taskWithoutTime = new Task("Task1", "Task1 description");
        mng.createTask(taskWithoutTime);
        taskWithoutTime.setStartTime(null);
        Task newTask = new Task("TestNewTask", "TestNewTask description", LocalDateTime.now().plusMinutes(30), 15);
        mng.createTask(newTask);
        List<Task> taskList = new ArrayList<>(mng.getPrioritizedTasks());
        assertEquals(mng.getAllTasks().get(1), taskList.get(0));
        assertEquals(mng.getAllSubTaskFromAllEpics().get(3), taskList.get(1));
        assertEquals(mng.getAllTasks().get(4), taskList.get(3));
        assertEquals(4, taskList.size());

        mng.delTaskById(1); //Удаляем одну из задачек
        //Она должна была удалиться из приоритетного списка задач
        List<Task> taskList1 = new ArrayList<>(mng.getPrioritizedTasks());
        assertEquals(3, taskList1.size(), "Задача не была удалена из приоритетных");

    }

    @Test
    public void taskCrossingTimeCheck() {
        Task crossTask = new Task("CrossTask", "CrossTask desc", mng.getAllTasks().get(1).getStartTime(), 15);
        assertThrows(TaskValidationException.class, () -> mng.createTask(crossTask));
    }

    @Test
    public void subtaskCrossingTimeCheck() {
        SubTask crossSubtask = new SubTask("CrossSubtask", "CrossSubtask desc", mng.getAllSubTaskFromAllEpics().get(3).getStartTime(), 15, mng.getAllSubTaskFromAllEpics().get(3).getEpicId());
        assertThrows(TaskValidationException.class, () -> mng.createSubTask(crossSubtask));
    }

    @Test
    void createTask() {
        mng.getAllTasks().clear();
        mng.getEpics().clear();
        mng.getAllSubTaskFromAllEpics().clear();
        mng.setIdGen(1);
        final int taskId = mng.createTask(task).getId();
        final Task savedTask = mng.getTaskById(taskId);

        assertNotNull(savedTask, "Задача не найдена.");
        assertEquals(task, savedTask, "Задачи не совпадают.");

        final HashMap<Integer, Task> tasks = mng.getAllTasks();

        assertNotNull(tasks, "Задачи не возвращаются.");
        assertEquals(1, tasks.size(), "Неверное количество задач.");
        assertEquals(task, tasks.get(1), "Задачи не совпадают.");
    }

    @Test
    void createEpic() {
        mng.getAllTasks().clear();
        mng.getEpics().clear();
        mng.getAllSubTaskFromAllEpics().clear();
        mng.setIdGen(1);
        final int epicId = mng.createEpic(epic).getId();
        final Epic savedEpic = mng.getEpicById(epicId);

        assertNotNull(savedEpic, "Задача не найдена");
        assertEquals(epic, savedEpic, "Задачи не совпадают");

        final HashMap<Integer, Epic> epics = mng.getEpics();

        assertNotNull(epics, "Задачи не возвращаются");
        assertEquals(1, epics.size(), "Неверное количество задач");
        assertEquals(epic, epics.get(1), "Задачи не совпадают");
    }

    @Test
    void createSubtask()  {
        mng.getAllTasks().clear();
        mng.delAllSubTasksFromAllEpics();
        mng.getAllSubTaskFromAllEpics().clear();
        mng.setIdGen(1);

        final int subtaskId = mng.createSubTask(subtask).getId();
        final SubTask savedSubtask = mng.getSubTaskById(subtaskId);

        assertNotNull(subtask, "Задача не найдена");
        assertEquals(subtask, savedSubtask, "Задачи не совпадают");

        final HashMap<Integer, SubTask> subtasks = mng.getAllSubTaskFromAllEpics();

        assertNotNull(subtasks, "Задачи не возвращаются");
        assertEquals(1, subtasks.size(), "Неверное количество задач");
        assertEquals(subtask, subtasks.get(1), "Задачи не совпадают");

    }

    @Test
    void deleteTaskById() {
        mng.delTaskById(1);
        assertEquals(0, mng.getAllTasks().size());

    }

    @Test
    void deleteTaskByIdEmptyList() {
        mng.delTaskById(1);
        assertThrows(NotFoundException.class, () -> mng.delTaskById(5));
        assertEquals(0, mng.getAllTasks().size());
    }

    @Test
    void deleteTaskByIdWrongId() {
        assertThrows(NotFoundException.class, () -> mng.delTaskById(5));
        assertEquals(1, mng.getAllTasks().size());
    }

    @Test
    void deleteAllTasks() {
        mng.delAllTasks();
        assertEquals(0, mng.getAllTasks().size());
    }

    @Test
    void deleteAllTasksEmptyList() {
        mng.delAllTasks();
        mng.delAllTasks();
        assertEquals(0, mng.getAllTasks().size());
    }

    @Test
    void updateTask() {
        Task newTask = new Task(1, "New Task", StatusOfTask.NEW, "desc", 30, LocalDateTime.now(), LocalDateTime.now().plus(Duration.ofMinutes(30)));
        mng.updateTask(newTask);
        assertEquals(newTask, mng.getAllTasks().get(newTask.getId()));
    }

    @Test
    void updateEpic() {
        Epic newEpic = new Epic(2, "New EN1", StatusOfTask.NEW, "ED1", 10, LocalDateTime.now().plusMinutes(10), LocalDateTime.now().plusMinutes(20));
        mng.updateEpic(newEpic);
        assertEquals(newEpic, mng.getEpics().get(newEpic.getId()));
        assertEquals(newEpic.getSubTasks(), mng.getEpics().get(newEpic.getId()).getSubTasks());
    }

    @Test
    void updateNotExistEpic() {
        Epic newEpic = new Epic(10, "New EN10", StatusOfTask.NEW, "ED10", 10, LocalDateTime.now().plusMinutes(10), LocalDateTime.now().plusMinutes(20));
        assertThrows(NotFoundException.class, () -> mng.updateEpic(newEpic));
    }
}