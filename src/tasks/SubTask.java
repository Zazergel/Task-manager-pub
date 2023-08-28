package tasks;

import java.time.LocalDateTime;
import java.util.Objects;

public class SubTask extends Task {

    protected int epicId;


    public SubTask(String name, String description, LocalDateTime startTime, long duration, int epicId) {
        super(name, description, startTime, duration);
        this.epicId = epicId;
        this.type = TypeOfTask.SUBTASK;
    }

    public SubTask(int id, String name, StatusOfTask status, String description, int epicId, long duration, LocalDateTime startTime, LocalDateTime endTime) {
        super(id, name, status, description, duration, startTime, endTime);
        this.epicId = epicId;
        this.type = TypeOfTask.SUBTASK;
    }

    public SubTask(String name, String description, int id, StatusOfTask status, int epicId,
                   long duration, LocalDateTime startTime) {
        super(name, description, id, status, duration, startTime);
        this.epicId = epicId;
        this.type = TypeOfTask.SUBTASK;
    }

    public int getEpicId() {
        return this.epicId;
    }

    @Override
    public String getType() {
        return "SUBTASK";
    }

    @Override
    public String toString() {
        return "\n tasks.SubTask{" +
                "name='" + name +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", id=" + id +
                ", duration=" + duration + '\'' +
                ", startTime=" + startTime + '\'' +
                ", endTime=" + endTime +
                ", epicId=" + epicId +
                '}';
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        SubTask subTask = (SubTask) o;
        return epicId == subTask.epicId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), epicId);
    }
}


