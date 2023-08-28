package logic;

import history.HistoryManager;
import tasks.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public class CSVTaskFormat {
    //Печатаем заголовок-указатель в начале save-файла
    public static String getHeader() {
        return "id,type,name,status,description,epic,duration,startTime,endTime";
    }

    //Превращаем строку в историю
    public static List<Integer> historyFromString(String s) {
        String[] lines = s.split(",");
        List<Integer> history = new ArrayList<>(lines.length);
        for (String line : lines) {
            history.add(Integer.parseInt(line));
        }
        return history;
    }

    //Превращаем историю в строку
    public static String historyToString(HistoryManager manager) {
        final List<Task> history = manager.getHistory();
        if (history.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(history.get(0).getId());
        for (int i = 1; i < history.size(); i++) {
            Task task = history.get(i);
            sb.append(",");
            sb.append(task.getId());
        }
        return sb.toString();
    }

    //Превращаем строку в задачу
    public static Task taskFromString(String[] value) {
        if (TypeOfTask.valueOf(value[1]).equals(TypeOfTask.TASK)) {
            return new Task(Integer.parseInt(value[0]), value[2], StatusOfTask.valueOf(value[3]), value[4], Integer.parseInt(value[6]), LocalDateTime.parse(value[7]), LocalDateTime.parse(value[8]));
        }
        if (TypeOfTask.valueOf(value[1]).equals(TypeOfTask.EPIC)) {
            return new Epic(Integer.parseInt(value[0]), value[2], StatusOfTask.valueOf(value[3]), value[4], Integer.parseInt(value[6]), LocalDateTime.parse(value[7]), LocalDateTime.parse(value[8]));
        }
        if (TypeOfTask.valueOf(value[1]).equals(TypeOfTask.SUBTASK)) {
            return new SubTask(Integer.parseInt(value[0]), value[2], StatusOfTask.valueOf(value[3]), value[4], (Integer.parseInt(value[5])), Integer.parseInt(value[6]), LocalDateTime.parse(value[7]), LocalDateTime.parse(value[8]));
        }
        return null;
    }

    //Превращаем задачу в строку
    public static String toString(Task task) {
        String line;
        line = task.getId() + "," + task.getType() + "," + task.getName() + "," + task.getStatus() + "," + task.getDescription() + "," + (task instanceof SubTask ? ((SubTask) task).getEpicId() : "notSbT") + "," + task.getDuration() + "," + task.getStartTime() + "," + task.getEndTime();
        return line;
    }
}


