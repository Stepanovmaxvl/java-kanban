package main.javakanban.manager.history;

import main.javakanban.model.Task;

import java.util.List;

public interface HistoryManager {
    void add(Task task);
    void remove(int id);//реализовано ранее
    List<Task> getHistory();
}
