package main.javakanban.manager.history;

import main.javakanban.model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryHistoryManager implements HistoryManager {

    private final Map<Integer, Node> history = new HashMap<>();
    private Node first;
    private Node last;

    @Override
    public void add(Task task) {
        if (task == null) {
            return;
        }

        remove(task.getId());
        linkLast(task);
        history.put(task.getId(), last);
    }

    @Override
    public void remove(int id) {
        final Node existingNode = history.remove(id);
        if (existingNode != null) {
            removeNode(existingNode);
        }
    }

    @Override
    public List<Task> getHistory() {

        return getTasks();
    }

    private void linkLast(Task task) {
        final Node newNode = new Node(last, task);
        if (first == null) {
            first = newNode;
        } else {
            last.next = newNode;
            newNode.prev = last;
        }
        last = newNode;
    }

    private List<Task> getTasks() {
        final List<Task> tasks = new ArrayList<>(history.size());
        Node currentNode = first;
        while (currentNode != null) {
            tasks.add(currentNode.element);
            currentNode = currentNode.next;
        }
        return tasks;
    }

    private void removeNode(Node node) {
        final Node prev = node.prev;
        final Node next = node.next;

        if (prev == null) {
            first = next;
        } else {
            prev.next = next;
        }

        if (next == null) {
            last = prev;
        } else {
            next.prev = prev;
        }
    }

    private static class Node {
        Node prev;
        Task element;
        Node next;

        Node(Node prev, Task element) {
            this.prev = prev;
            this.element = element;
        }
    }
}
