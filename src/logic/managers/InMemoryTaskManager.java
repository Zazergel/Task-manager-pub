package logic.managers;

import exceptions.NotFoundException;
import exceptions.TaskValidationException;
import history.HistoryManager;
import tasks.Epic;
import tasks.StatusOfTask;
import tasks.SubTask;
import tasks.Task;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;

public class InMemoryTaskManager implements TaskManager {
    protected final HashMap<Integer, Task> listOfTasks = new HashMap<>();
    protected final HashMap<Integer, SubTask> listOfSubTasks = new HashMap<>();
    protected final HashMap<Integer, Epic> listOfEpics = new HashMap<>();
    protected HistoryManager historyManager = Managers.getDefaultHistory();
    int idGen = 1;
    private final TreeSet<Task> prioritizedTasks = new TreeSet<>(); //тут будем хранить задачи по приоритету

    //Получаем списочек задач по приоритету
    @Override
    public TreeSet<Task> getPrioritizedTasks() {
        return prioritizedTasks;
    }

    public void setIdGen(int idGen) {
        this.idGen = idGen;
    }

    //Получаем историю просмотров
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public HistoryManager getHistoryManager() {
        return historyManager;
    }

    //Получаем список всех Эпиков
    @Override
    public HashMap<Integer, Epic> getEpics() {
        return listOfEpics;
    }

    /* Метод проверяет время старта новой задачи со временем старта какой-либо из существующих задач такого же типа,
     если найдено совпадение - бросает исключение */
    private void checkTaskTime(Task task) {
        final LocalDateTime startTime = task.getStartTime();
        if (startTime == null) {
            return;
        }
        final boolean checkResult = Stream.concat(getAllTasks().values().stream(),
                        getAllSubTaskFromAllEpics().values().stream()).
                anyMatch(value -> value.getStartTime() != null && value.getStartTime().isEqual(startTime));
        if (checkResult) {
            throw new TaskValidationException("Задача с таким временем старта уже существует");
        }
    }

    //Создаем новую задачу
    @Override
    public Task createTask(Task newTask) {
        checkTaskTime(newTask);
        newTask.setId(idGen);
        listOfTasks.put(idGen, newTask);
        prioritizedTasks.add(newTask);
        idGen++;
        return newTask;
    }

    //Получаем список всех задач
    @Override
    public HashMap<Integer, Task> getAllTasks() {
        return listOfTasks;
    }

    //Очищаем список задач
    @Override
    public void delAllTasks() {
        if (!listOfTasks.isEmpty()) {
            idGen = 0;
            List<Task> allTasks = new ArrayList<>(listOfTasks.values());
            for (Task task : allTasks) {
                prioritizedTasks.remove(task);
            }
            listOfTasks.clear();
        }
    }

    //Удаляем задачу по идентификатору
    @Override
    public void delTaskById(int taskId) {
        if (!getAllTasks().containsKey(taskId)) {
            throw new NotFoundException("Задачи с id " + taskId + " не существует или она уже была удалена!");
        }
        if (historyManager.getHistory().contains(getAllTasks().get(taskId))) {
            historyManager.remove(taskId);
        }
        if (getAllTasks().containsKey(taskId)) {
            prioritizedTasks.remove(getAllTasks().get(taskId));
            getAllTasks().remove(taskId);
        }
    }

    //Получаем задачу по идентификатору
    @Override
    public Task getTaskById(int taskId) {
        if (!listOfTasks.containsKey(taskId)) {
            throw new NotFoundException("Задачи с таким Id не существует!");
        }
        final Task task = listOfTasks.get(taskId);
        historyManager.add(task);
        return task;
    }

    //Создаем новый эпик
    @Override
    public Epic createEpic(Epic newEpic) {
        if (newEpic.getDuration() != 0) {
            checkTaskTime(newEpic);
        }
        newEpic.setId(idGen);
        listOfEpics.put(idGen, newEpic);
        idGen++;
        return newEpic;
    }

    //Получаем эпик по идентификатору
    @Override
    public Epic getEpicById(int id) {
        if (!listOfEpics.containsKey(id)) {
            throw new NotFoundException("Эпика с таким id не существует!");
        }
        Epic epic = listOfEpics.get(id);
        historyManager.add(epic);
        return epic;
    }

    //Вспомогательный метод для расчета статуса эпика
    public int checkEpicStatus(Epic epic) {
        int checkForNew = 0;
        int checkForDone = 0;
        for (int i = 0; i < listOfEpics.get(epic.getId()).getSubTasks().size(); i++) {
            if (listOfEpics.get(epic.getId()).getSubTasks().get(i).getStatus().equals(StatusOfTask.NEW)) {
                checkForNew += 1;
            } else if (listOfEpics.get(epic.getId()).getSubTasks().get(i).getStatus().equals(StatusOfTask.DONE)) {
                checkForDone += 1;
            }
        }
        if (checkForDone == 0 && checkForNew != 0) {
            return 1;
        } else if (checkForNew == 0 && checkForDone != 0) {
            return 2;
        } else return 3;
    }

    public void resetEpicDuration(Epic epic) {
        long sumDuration = 0;
        if (epic.getSubTasks().isEmpty()) {
            epic.setDuration(0);
        } else {
            for (SubTask sb : epic.getSubTasks()) {
                sumDuration += sb.getDuration();
                epic.setDuration(sumDuration);
            }
        }
    }

    //Проверяем статусы подзадач в эпике и устанавливаем на их основе статус эпику
    public void resetEpicStatus(Epic epic) {
        if (listOfEpics.get(epic.getId()).getSubTasks().isEmpty() || checkEpicStatus(epic) == 1) {
            listOfEpics.get(epic.getId()).setStatus(StatusOfTask.NEW);//если у эпика нет подзадач или все они имеют статус NEW, то статус должен быть NEW.
        } else if (checkEpicStatus(epic) == 2) {
            listOfEpics.get(epic.getId()).setStatus(StatusOfTask.DONE); //если все подзадачи имеют статус DONE, то и эпик считается завершённым — со статусом DONE.
        } else if (checkEpicStatus(epic) == 3) {
            listOfEpics.get(epic.getId()).setStatus(StatusOfTask.IN_PROGRESS);
        }
        resetEpicDuration(epic);
    }

    //Обновляем конкретную задачу
    @Override
    public void updateTask(Task task) {
        if (!getAllTasks().containsKey(task.getId())) {
            throw new NotFoundException("Такой задачи не существует!");
        }
        listOfTasks.put(task.getId(), task);
    }

    //Обновляем конкретный эпик
    @Override
    public void updateEpic(Epic epic) {
        if (!listOfEpics.containsKey(epic.getId())) {
            throw new NotFoundException("Такого эпика не существует!");
        }
        ArrayList<SubTask> subTasks = listOfEpics.get(epic.getId()).getSubTasks();
        epic.setSubTasks(subTasks);
        listOfEpics.put(epic.getId(), epic);
        resetEpicStatus(epic);
        listOfEpics.get(epic.getId());
    }

    //Метод удаляет из списка с подзадачами старую версию подзадачи и добавляет на ее место обновленную версию
    @Override
    public Integer updateSubTask(SubTask subTask) {
        Epic epic = listOfEpics.get(subTask.getEpicId());
        if (epic == null) {
            throw new NotFoundException("Такого эпика не существует!");
        }
        epic.getSubTasks().removeIf(sb -> subTask.getId().equals(sb.getId()));
        epic.setUpdateSubTask(subTask);
        resetEpicStatus(epic);
        listOfSubTasks.put(subTask.getId(), subTask);
        return subTask.getId();
    }

    //Удаляем эпик по ID
    @Override
    public void delEpicById(int epicId) {
        if (listOfEpics.containsKey(epicId)) {
            delAllSubTasksByEpic(listOfEpics.get(epicId));
            historyManager.remove(epicId);
            listOfEpics.remove(epicId);
        } else {
            throw new NotFoundException("Эпика с id " + epicId + " не существует!");
        }
    }

    //Создаем новую подзадачу
    @Override
    public SubTask createSubTask(SubTask newSubTask) {
        checkTaskTime(newSubTask);
        newSubTask.setId(idGen);
        listOfSubTasks.put(idGen, newSubTask);
        Epic epic = listOfEpics.get(newSubTask.getEpicId());
        epic.addSubTasks(newSubTask);
        resetEpicStatus(epic);
        prioritizedTasks.add(newSubTask);
        idGen++;
        epic.setDuration(epic.getCalcDuration());
        epic.setStartTime(epic.getCalcStartTime());
        epic.setEndTime(epic.getCalcEndTime());
        return newSubTask;
    }

    // Получаем список подзадач конкретного эпика
    @Override
    public ArrayList<SubTask> getAllSubTasksByEpic(Epic epic) {
        if (listOfEpics.containsKey(epic.getId())) {
            return listOfEpics.get(epic.getId()).getSubTasks();
        } else {
            throw new NotFoundException("Такого эпика не существует!");
        }

    }

    //Получаем все подзадачи из всех эпиков
    @Override
    public HashMap<Integer, SubTask> getAllSubTaskFromAllEpics() {
        for (Epic epic : listOfEpics.values()) {
            for (SubTask subTask : epic.getSubTasks()) {
                listOfSubTasks.put(subTask.getId(), subTask);
            }
        }
        return listOfSubTasks;
    }

    //Удаляем все подзадачи из всех Эпиков
    @Override
    public void delAllSubTasksFromAllEpics() {
        if (getEpics().isEmpty()) {
            throw new NotFoundException("Список эпиков пуст!");
        }
        for (Epic epic : listOfEpics.values()) {
            if (!epic.getSubTasks().isEmpty()) {
                epic.getSubTasks().clear();
            }
        }
        List<SubTask> allSubTasks = new ArrayList<>(getAllSubTaskFromAllEpics().values());
        for (SubTask sb : allSubTasks) {
            prioritizedTasks.remove(sb);
        }
        listOfSubTasks.clear();
    }

    //Удаляем подзадачу по id
    @Override
    public HashMap<Integer, SubTask> delSubTaskById(int sbId) {
        if (!listOfSubTasks.containsKey(sbId)) {
            throw new NotFoundException("Задачи с id " + sbId + " не существует или она уже удалена!");
        }
        for (Map.Entry<Integer, Epic> pair : listOfEpics.entrySet()) {
            if (!pair.getValue().getSubTasks().isEmpty()) {
                for (SubTask subTask : pair.getValue().getSubTasks()) {
                    if (subTask.getId() == sbId) {
                        pair.getValue().deleteSubTask(subTask);
                        resetEpicStatus(pair.getValue());
                        return delSubTaskById(sbId);
                    }
                }
            }
        }
        historyManager.remove(sbId);
        prioritizedTasks.remove(listOfSubTasks.get(sbId));
        listOfSubTasks.remove(sbId);
        return listOfSubTasks;
    }

    //Получаем конкретную подзадачу по id
    @Override
    public SubTask getSubTaskById(int id) {
        return getAllSubTaskFromAllEpics()
                .values()
                .stream()
                .filter(s -> s.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Подзадачи с таким id " + id + " не существует!"));
    }

    //Удаляем все подзадачи конкретного Эпика
    @Override
    public void delAllSubTasksByEpic(Epic epic) {
        if (!getEpics().containsKey(epic.getId())) {
            throw new NotFoundException("Такого эпика не существует!");
        }
        if (!listOfEpics.get(epic.getId()).getSubTasks().isEmpty()) {
            listOfEpics.get(epic.getId()).getSubTasks().clear();
            if (!listOfSubTasks.isEmpty()) {
                for (Integer id : listOfSubTasks.keySet()) {
                    if (historyManager.getHistory().contains(listOfSubTasks.get(id))) {
                        historyManager.remove(id);
                    }
                    if (listOfSubTasks.get(id).getEpicId() == epic.getId()) {
                        prioritizedTasks.remove(listOfSubTasks.get(id));
                        listOfSubTasks.remove(id);
                    }
                }
            }
        }
    }

    @Override
    public List<String> showAllTasks() {
        List<String> taskList = new ArrayList<>();
        for (Map.Entry<Integer, Epic> pair : listOfEpics.entrySet()) {
            taskList.add(pair.getValue().getName());
        }
        for (Map.Entry<Integer, SubTask> pair : listOfSubTasks.entrySet()) {
            taskList.add(pair.getValue().getName());
        }
        for (Map.Entry<Integer, Task> pair : listOfTasks.entrySet()) {
            taskList.add(pair.getValue().getName());
        }
        return taskList;
    }

    public int getIdGen() {
        return idGen;
    }
}

