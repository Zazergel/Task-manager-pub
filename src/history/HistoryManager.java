package history;

import tasks.Task;

import java.util.HashMap;
import java.util.List;


public interface HistoryManager {

    void add(Task task);

    void remove(int id);

    List<Task> getHistory();

    HashMap<Integer, InMemoryHistoryManager.Node> getTaskHashMap();

    InMemoryHistoryManager.CustomLinkedList getTaskCustomLinkedList();

}