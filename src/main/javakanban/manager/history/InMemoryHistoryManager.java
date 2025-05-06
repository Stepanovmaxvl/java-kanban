package main.javakanban.manager.history;

import main.javakanban.manager.history.HistoryManager;
import main.javakanban.model.Task;

import java.util.*;

public class InMemoryHistoryManager implements HistoryManager {

    private static final int MAX_HISTORY_STORAGE = 10;
    private final List<Task> historyList = new ArrayList<>();

    @Override
    public void add(Task task) {
        if (historyList.size() == MAX_HISTORY_STORAGE) {
            historyList.removeFirst();
        }
        historyList.add(task);
    }

    public void update(Task newTask) {
           int index = historyList.indexOf(newTask);

        if (index != -1) {
            Task previousTask = historyList.get(index);
            if (previousTask != null) {
                add(previousTask);
            }

            historyList.set(index, newTask);
        } else {
            add(newTask);
        }
    }

    @Override
    public List<Task> getHistory() {
        return historyList;
    }
}
