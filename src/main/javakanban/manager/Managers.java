package main.javakanban.manager;

import main.javakanban.manager.history.HistoryManager;
import main.javakanban.manager.history.InMemoryHistoryManager;
import main.javakanban.manager.task.InMemoryTaskManager;

public class Managers {
    public static InMemoryTaskManager getDefault() {
        return new InMemoryTaskManager();
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}
