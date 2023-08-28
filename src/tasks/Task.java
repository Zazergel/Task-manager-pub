package tasks;

import java.time.LocalDateTime;
import java.util.Objects;

public class Task implements Comparable<Task> {
    protected String name;
    protected String description;
    protected StatusOfTask status; //Статус задачи через перечисление
    protected int id;
    protected long duration; //Продолжительность теперь лонг
    protected LocalDateTime startTime;
    protected LocalDateTime endTime;
    protected TypeOfTask type;


    public Task(String name, String description) {
        this.name = name;
        this.description = description;
        this.duration = 0;
        this.status = StatusOfTask.NEW;
        this.type = TypeOfTask.TASK;
    }

    public Task(String name, String description, LocalDateTime startTime, long duration) {
        this.name = name;
        this.description = description;
        this.duration = duration;
        this.status = StatusOfTask.NEW;
        this.startTime = startTime;
        this.type = TypeOfTask.TASK;
        this.endTime = getStartTime().plusMinutes(duration);
    }

    public Task(String name, String description, LocalDateTime startTime) {
        this.name = name;
        this.description = description;
        this.status = StatusOfTask.NEW;
        this.startTime = startTime;
        this.type = TypeOfTask.TASK;
    }

    public Task(String name, String description, int id, StatusOfTask status, long duration, LocalDateTime startTime) {
        this.name = name;
        this.description = description;
        this.id = id;
        this.status = status;
        this.duration = duration;
        this.startTime = startTime;
        this.endTime = getStartTime().plusMinutes(duration);
        this.type = TypeOfTask.TASK;
    }

    public Task(int id, String name, StatusOfTask status, String description, long duration, LocalDateTime startTime, LocalDateTime endTime) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.description = description;
        this.duration = duration;
        this.startTime = startTime;
        this.endTime = endTime;
        this.type = TypeOfTask.TASK;
    }

    public Task(String name, String description, int id, StatusOfTask status) {
        this.name = name;
        this.description = description;
        this.id = id;
        this.status = status;
        this.type = TypeOfTask.TASK;

    }

    public void setId(int taskId) {
        id = taskId;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public StatusOfTask getStatus() {
        return status;
    }

    public void setStatus(StatusOfTask status) {
        this.status = status;
    }

    public String getType() {
        return "TASK";
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public long getDuration() {
        return duration;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    @Override
    public String toString() {
        return "\n tasks.Task{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status='" + status + '\'' +
                ", id=" + id + '\'' +
                ", duration=" + duration + '\'' +
                ", startTime=" + startTime + '\'' +
                ", endTime=" + endTime + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id && Objects.equals(name, task.name) && Objects.equals(description, task.description) && status == task.status && Objects.equals(duration, task.duration) && Objects.equals(startTime, task.startTime) && Objects.equals(endTime, task.endTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, id, status, duration, startTime, endTime);
    }

    @Override
    public int compareTo(Task o) {
        if (o.getStartTime() == null && this.getStartTime() != null) {
            return -1;
        } else if (o.getStartTime() != null && this.getStartTime() == null) {
            return 1;
        } else {
            if (o.getStartTime() != null && this.getStartTime() != null && this.getStartTime().isBefore(o.getStartTime())) {
                return -1;
            }
            if (o.getStartTime() != null && this.getStartTime() != null && this.getStartTime().isEqual(o.getStartTime())) {
                return 0;
            }
        }
        return 1;
    }
}




