package logic.managers;

import exceptions.ManagerSaveException;
import logic.CSVTaskFormat;
import tasks.Epic;
import tasks.SubTask;
import tasks.Task;
import tasks.TypeOfTask;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class FileBackedTasksManager extends InMemoryTaskManager {
    private final File file;

    public FileBackedTasksManager(File file) {
        this.file = file;
    }

    public static FileBackedTasksManager loadDataFromFile(File file) {
        FileBackedTasksManager mng = new FileBackedTasksManager(file);
        try (BufferedReader b = new BufferedReader(new FileReader("history.csv"))) {
            b.readLine();
            while (b.ready()) {
                String[] lines = b.readLine().split(",");
                if (lines.length > 1) {
                    if (lines[1].equals(TypeOfTask.TASK.toString())) {
                        mng.createTask(((CSVTaskFormat.taskFromString(lines))));
                    } else if (lines[1].equals(TypeOfTask.EPIC.toString())) {
                        mng.createEpic((Epic) CSVTaskFormat.taskFromString(lines));
                    } else if (lines[1].equals(TypeOfTask.SUBTASK.toString())) {
                        mng.createSubTask((SubTask) CSVTaskFormat.taskFromString(lines));
                    }
                }
                if (lines.length == 1) {
                    String idString = b.readLine();
                    List<Integer> idList = CSVTaskFormat.historyFromString(idString);
                    for (Integer integer : idList) {
                        if (mng.getAllTasks().containsKey(integer)) {
                            mng.getTaskById(integer);
                        }
                        if (mng.getAllSubTaskFromAllEpics().containsKey(integer)) {
                            mng.getSubtaskById(integer);
                        }
                        if (mng.getEpics().containsKey(integer)) {
                            mng.getEpicById(integer);
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Не могу прочитать файл:" + file.getName(), e);
        }
        return mng;
    }

    @Override
    public Task createTask(Task task) {
        super.createTask(task);
        save();
        return task;
    }

    @Override
    public Epic createEpic(Epic epic) {
        super.createEpic(epic);
        save();
        return epic;
    }

    @Override
    public SubTask createSubTask(SubTask subtask) {
        super.createSubTask(subtask);
        save();
        return subtask;
    }

    @Override
    public Integer updateSubTask(SubTask subTask) {
        return super.updateSubTask(subTask);

    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
    }

    @Override
    public Task getTaskById(int id) {
        Task task = super.getTaskById(id);
        save();
        return task;
    }

    public void getSubtaskById(int id) {
        super.listOfSubTasks.get(id);
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = super.getEpicById(id);
        save();
        return epic;
    }

    @Override
    public void delTaskById(int id) {
        super.delTaskById(id);
        save();
    }


    @Override
    public void delEpicById(int id) {
        super.delEpicById(id);
        save();
    }

    public void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(CSVTaskFormat.getHeader());
            writer.newLine();
            for (Map.Entry<Integer, Task> entry : getAllTasks().entrySet()) {
                final Task task = entry.getValue();
                writer.write(CSVTaskFormat.toString(task));
                writer.newLine();
            }
            for (Map.Entry<Integer, Epic> entry : getEpics().entrySet()) {
                final Epic epic = entry.getValue();
                writer.write(CSVTaskFormat.toString(epic));
                writer.newLine();
            }
            for (Map.Entry<Integer, SubTask> entry : getAllSubTaskFromAllEpics().entrySet()) {
                final SubTask subTask = entry.getValue();
                writer.write(CSVTaskFormat.toString(subTask));
                writer.newLine();
            }
            writer.newLine();
            writer.write(CSVTaskFormat.historyToString(getHistoryManager()));
            writer.newLine();
        } catch (IOException e) {
            throw new ManagerSaveException("Не могу сохранить файл: " + file.getName(), e);
        }
    }

    @Override
    public HashMap<Integer, SubTask> delSubTaskById(int id) {
        super.delSubTaskById(id);
        save();
        return listOfSubTasks;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileBackedTasksManager that = (FileBackedTasksManager) o;
        return Objects.equals(file, that.file);
    }

    @Override
    public int hashCode() {
        return Objects.hash(file);
    }
}