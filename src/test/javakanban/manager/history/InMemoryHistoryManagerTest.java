package test.javakanban.manager.history;

import main.javakanban.manager.history.InMemoryHistoryManager;
import main.javakanban.manager.history.HistoryManager;
import main.javakanban.manager.task.InMemoryTaskManager;
import main.javakanban.model.Epic;
import main.javakanban.model.Status;
import main.javakanban.model.Subtask;
import main.javakanban.model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static test.javakanban.manager.task.InMemoryTaskManagerTest.*;

class InMemoryHistoryManagerTest {

    @Test
    public void add_deleteFirstTask_addedMoreThen10Task() {
        InMemoryHistoryManager historyManager = new InMemoryHistoryManager();

        for (int i = 0; i < 20; i++) {
            Task task = new Task("Задача " + i, "Описание " + i);
            historyManager.add(task);
        }

        List<Task> history = historyManager.getHistory();
        assertEquals(10, history.size(), "Неверное количество элементов в истории");
    }

    @Test
    public void add_addCopyOfTask() throws CloneNotSupportedException {
        InMemoryHistoryManager historyManager = new InMemoryHistoryManager();
        // Создаем задачу и добавляем в историю
        Task task = new Task("Задача", "Описание");
        historyManager.add(task);
        task.setName("Измененная задача");
        Task taskInHistory = historyManager.getHistory().get(0);
        assertNotEquals(task.getName(), taskInHistory.getName(), "Имя задачи в истории изменилось!");
        task.setDescription("Новое описание");
        assertNotEquals(task.getDescription(), taskInHistory.getDescription(), "Описание задачи в истории изменилось!");
    }
}