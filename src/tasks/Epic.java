package tasks;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Objects;

public class Epic extends Task {
    protected ArrayList<SubTask> subTasks; //Тут хранятся сабтаски конкретного эпика

    public Epic(String name, String description) {
        super(name, description);
        this.subTasks = new ArrayList<>();
        this.startTime = getCalcStartTime();
        this.duration = getCalcDuration();
        this.type = TypeOfTask.EPIC;
        this.endTime = getCalcEndTime();
    }

    public Epic(int id, String name, StatusOfTask status, String description, long duration, LocalDateTime startTime, LocalDateTime endTime) {
        super(id, name, status, description, duration, startTime, endTime);
        this.subTasks = new ArrayList<>();
        this.type = TypeOfTask.EPIC;
    }

    public Epic(String nameOfTask, String taskDescription, int id, long duration, LocalDateTime startTime) {
        super(nameOfTask, taskDescription, id, StatusOfTask.NEW, duration, startTime);
        this.type = TypeOfTask.EPIC;
        this.endTime = getCalcEndTime();
    }

    public Epic(String name, String taskDescription, int id) {
        super(name, taskDescription, id, StatusOfTask.NEW);
        this.type = TypeOfTask.EPIC;
    }

    public ArrayList<SubTask> getSubTasks() {
        return subTasks;
    }

    public SubTask getSubTaskById(int id) {
        return this.subTasks.get(id);
    }


    //Метод добавляет новую подзадачу в список с подзадачами
    public void addSubTasks(SubTask subTask) {
        this.subTasks.add(subTask);
    }

    //Метод удаляет подзадачу из локального списка подзадач
    public void deleteSubTask(SubTask subTask) {
        subTasks.remove(subTask);
    }

    //Метод помещает обновленную версию подзадачи на то место в списке, где стояла ее старая версия
    public void setUpdateSubTask(SubTask subTask) {
        this.subTasks.remove(subTask);
        this.subTasks.add(subTask);
    }

    public void setSubTasks(ArrayList<SubTask> subTasks) {
        this.subTasks = subTasks;
    }

    //Метод рассчитывает общую продолжительность Эпика
    public long getCalcDuration() {
        if (subTasks.isEmpty()) {
            return 0;
        }
        long duration = 0;
        for (SubTask subtask : this.getSubTasks()) {
            duration += subtask.getDuration();
        }
        return duration;
    }

    //Метод рассчитывает время начала Эпика
    public LocalDateTime getCalcStartTime() {
        if (this.subTasks.isEmpty()) {
            return LocalDateTime.MIN;
        }
        return this.getSubTasks().get(0).getStartTime();
    }

    //Метод рассчитывает время окончания Эпика
    public LocalDateTime getCalcEndTime() {
        if (this.subTasks.isEmpty()) {
            return LocalDateTime.MAX;
        }
        LocalDateTime endTime = getCalcStartTime();
        for (SubTask sb : this.subTasks) {
            if (sb.getEndTime().isAfter(this.getStartTime())) {
                endTime = sb.getEndTime();
            }
        }
        return endTime;
    }

    @Override
    public String getType() {
        return "EPIC";
    }

    @Override
    public String toString() {
        return "\n tasks.Epic{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status='" + status + '\'' +
                ", id=" + id + '\'' +
                ", Duration=" + duration + '\'' +
                ", Start=" + startTime + '\'' +
                ", End=" + endTime + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Epic epic = (Epic) o;
        return Objects.equals(subTasks, epic.subTasks) && Objects.equals(duration, epic.duration) && Objects.equals(startTime, epic.startTime) && Objects.equals(endTime, epic.endTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), subTasks);
    }
}

