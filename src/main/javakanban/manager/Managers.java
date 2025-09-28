package main.javakanban.manager;

import main.javakanban.manager.task.InMemoryTaskManager;

public class Managers {
    public static InMemoryTaskManager getDefault() {
        return new InMemoryTaskManager();
    }


}
