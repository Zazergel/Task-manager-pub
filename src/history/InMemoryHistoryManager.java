package history;


import tasks.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;


public class InMemoryHistoryManager implements HistoryManager {
    private final CustomLinkedList taskCustomLinkedList;
    private final HashMap<Integer, Node> taskHashMap;

    public InMemoryHistoryManager() {

        taskCustomLinkedList = new CustomLinkedList();

        taskHashMap = new HashMap<>();

    }

    public class CustomLinkedList {

        private Node head;

        private Node tail;


        public void insert(tasks.Task data) {

            if (head == null) {

                head = new Node(data, null, null);

                tail = head;

            } else {

                Node oldTail = tail;

                tail = new Node(data, null, oldTail);

                oldTail.setNextNode(tail);

            }

        }


        public void removeNode(Node node) {

            Node oldPrevious = node.getPreviousNode();

            Node oldNext = node.getNextNode();

            if (node == head) {

                if (oldNext == null) {

                    head = null;

                    tail = null;

                } else {

                    oldNext.setPreviousNode(null);

                    head = oldNext;

                }

            } else if (oldNext != null) {

                oldPrevious.setNextNode(oldNext);

                oldNext.setPreviousNode(oldPrevious);

            } else {

                oldPrevious.setNextNode(null);

                tail = oldPrevious;

            }

        }


        public void removeAll() {

            head = null;

        }


        public Node getHead() {

            return head;

        }


        public Node getTail() {

            return tail;

        }

    }

    public class Node {

        private final Task data;

        private Node next;

        private Node previous;


        @Override

        public String toString() {

            return "Node{" +

                    "task=" + data;

        }


        public Node getPreviousNode() {

            return previous;

        }


        public void setPreviousNode(Node previous) {

            this.previous = previous;

        }


        public Node(Task task, Node next, Node previous) {

            this.data = task;

            this.next = next;

            this.previous = previous;

        }


        public Task getData() {

            return data;

        }


        public Node getNextNode() {

            return next;

        }


        public void setNextNode(Node nextNode) {

            this.next = nextNode;

        }


        @Override

        public boolean equals(Object o) {

            if (this == o) return true;

            if (o == null || getClass() != o.getClass()) return false;

            Node node = (Node) o;

            return Objects.equals(data, node.data) && Objects.equals(next, node.next) && Objects.equals(previous, node.previous);

        }


        @Override

        public int hashCode() {

            return Objects.hash(data, next, previous);

        }

    }

    private ArrayList<Task> getAllTasks() {

        ArrayList<Task> taskHistory = new ArrayList<>();

        if (taskCustomLinkedList.getHead() == null) {

            return taskHistory;

        } else {

            Node currentNode = taskCustomLinkedList.getHead();

            while (currentNode != null) {

                taskHistory.add(currentNode.getData());

                currentNode = currentNode.getNextNode();

            }

        }

        return taskHistory;

    }

    @Override
    public void add(Task task) {
        if (taskHashMap.containsKey(task.getId())) {
            taskCustomLinkedList.removeNode(taskHashMap.get(task.getId()));
        }
        taskCustomLinkedList.insert(task);
        taskHashMap.put(task.getId(), taskCustomLinkedList.getTail());
    }

    @Override
    public void remove(int id) {
        taskCustomLinkedList.removeNode(taskHashMap.get(id));
        taskHashMap.remove(id);

    }

    @Override
    public ArrayList<Task> getHistory() {
        return getAllTasks();
    }

    @Override
    public HashMap<Integer, Node> getTaskHashMap() {
        return taskHashMap;
    }

    @Override
    public CustomLinkedList getTaskCustomLinkedList() {
        return taskCustomLinkedList;
    }

}



