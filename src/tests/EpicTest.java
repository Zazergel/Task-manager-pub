package tests;

import logic.managers.InMemoryTaskManager;
import logic.managers.TaskManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.StatusOfTask;
import tasks.SubTask;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;

class EpicTest {
    protected static TaskManager mng = new InMemoryTaskManager();

    @BeforeEach
    public void beforeEach() throws IOException, InterruptedException {
        mng.getEpics().clear();
        mng.getAllSubTaskFromAllEpics().clear();
        mng.getPrioritizedTasks().clear();
        mng.setIdGen(1);
        Epic epic1 = new Epic(1, "EN1", StatusOfTask.NEW, "ED1", 10, LocalDateTime.now().plusMinutes(10), LocalDateTime.now().plusMinutes(20));
        mng.createEpic(epic1);
        SubTask subtask1 = new SubTask("Test Subtask 1", "Test description of SubTask 1", LocalDateTime.now().plusMinutes(10), 30, epic1.getId());
        mng.createSubTask(subtask1);
        SubTask subtask2 = new SubTask("Test Subtask 2", "Test description of SubTask 2", LocalDateTime.now().plusMinutes(20), 30, epic1.getId());
        mng.createSubTask(subtask2);
        SubTask subtask3 = new SubTask("Test Subtask 3", "Test description of SubTask 3", LocalDateTime.now().plusMinutes(30), 30, epic1.getId());
        mng.createSubTask(subtask3);
    }

    @Test
    public void epicDurationCheck() {
        Duration sumDuration = Duration.ofMinutes(0);
        for (SubTask sb : mng.getEpics().get(1).getSubTasks()) {
            sumDuration = sumDuration.plus(Duration.ofMinutes(sb.getDuration()));
        }
        mng.resetEpicStatus(mng.getEpics().get(1));
        Assertions.assertEquals(sumDuration, Duration.ofMinutes(mng.getEpics().get(1).getDuration()));
    }

    @Test
    public void epicStartTimeCheck() {
        Epic epic = mng.getEpics().get(1);
        Assertions.assertEquals(epic.getSubTasks().get(0).getStartTime(), epic.getStartTime());
    }

    @Test
    public void epicEndTimeCheck() {
        Epic epic = mng.getEpics().get(1);
        Assertions.assertEquals(epic.getSubTaskById(epic.getSubTasks().size() - 1).getEndTime(), epic.getEndTime());
    }

    @Test
    public void emptySubtasksListStatusCheck() {
        mng.getAllSubTaskFromAllEpics().clear();
        Assertions.assertEquals("NEW", mng.getEpics().get(1).getStatus().toString());
    }

    @Test
    public void allSubtasksAreNewCheck() {
        Assertions.assertEquals(StatusOfTask.NEW, mng.getEpics().get(1).getStatus());
    }

    @Test
    public void allSubtasksAreDoneCheck() throws IOException, InterruptedException {
        for (Integer key : mng.getAllSubTaskFromAllEpics().keySet()) {
            mng.getAllSubTaskFromAllEpics().get(key).setStatus(StatusOfTask.DONE);
        }
        mng.resetEpicStatus(mng.getEpicById(1));
        Assertions.assertEquals(StatusOfTask.DONE, mng.getEpics().get(1).getStatus());
    }

    @Test
    public void someSubtasksAreDoneOrNewCheck() throws IOException, InterruptedException {
        mng.getAllSubTaskFromAllEpics().get(2).setStatus(StatusOfTask.DONE);
        mng.resetEpicStatus(mng.getEpicById(1));
        Assertions.assertEquals(StatusOfTask.IN_PROGRESS, mng.getEpics().get(1).getStatus());
    }

    @Test
    public void allSubtasksAreInProgressStatusCheck() throws IOException, InterruptedException {
        for (Integer key : mng.getAllSubTaskFromAllEpics().keySet()) {
            mng.getAllSubTaskFromAllEpics().get(key).setStatus(StatusOfTask.IN_PROGRESS);
        }
        mng.resetEpicStatus(mng.getEpicById(1));
        Assertions.assertEquals(StatusOfTask.IN_PROGRESS, mng.getEpics().get(1).getStatus());
    }
}