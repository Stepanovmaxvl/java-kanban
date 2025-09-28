package manager.history;

import main.javakanban.manager.history.InMemoryHistoryManager;
import main.javakanban.model.Status;
import main.javakanban.model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryHistoryManagerTest {

    private InMemoryHistoryManager historyManager;

    @BeforeEach
    public void setUp() {
        historyManager = new InMemoryHistoryManager();
    }

    @Test
    public void emptyHistory_returnsEmptyList() {
        assertTrue(historyManager.getHistory().isEmpty());
    }

    @Test
    public void duplication_recordsOnlyOnceAndMovesToEnd() {
        Task task1 = new Task(1, "Задача 1", "Описание 1", Status.NEW);
        Task task2 = new Task(2, "Задача 2", "Описание 2", Status.NEW);
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task1);

        List<Task> hist = historyManager.getHistory();
        assertEquals(2, hist.size());
        assertEquals(2, hist.get(0).getId());
        assertEquals(1, hist.get(1).getId());
    }

    @Test
    public void remove_beginning_middle_end_updatesHistory() {
        Task task1 = new Task(1, "Задача 1", "Описание 1", Status.NEW);
        Task task2 = new Task(2, "Задача 2", "Описание 2", Status.NEW);
        Task task3 = new Task(3, "Задача 3", "Описание 3", Status.NEW);
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(1); // remove beginning
        assertEquals(2, historyManager.getHistory().size());
        assertEquals(2, historyManager.getHistory().get(0).getId());
        assertEquals(3, historyManager.getHistory().get(1).getId());

        historyManager.remove(2); // remove middle (now beginning)
        assertEquals(1, historyManager.getHistory().size());
        assertEquals(3, historyManager.getHistory().get(0).getId());

        historyManager.remove(3); // remove end
        assertTrue(historyManager.getHistory().isEmpty());
    }

    @Test
    public void add_getHistory_taskIsAdded() {
        Task task1 = new Task(1, "Задача 1", "Описание 1", Status.NEW);
        Task task2 = new Task(2, "Задача 2", "Описание 2",Status.NEW);

        historyManager.add(task1);
        historyManager.add(task2);

        List<Task> tasks = historyManager.getHistory();
        assertEquals(2, tasks.size());
        assertTrue(tasks.contains(task1));
        assertTrue(tasks.contains(task2));
    }

    @Test
    public void remove_getHistory_taskIsRemove() {
        Task task1 = new Task(1, "Задача 1", "Описание 1", Status.NEW);
        Task task2 = new Task(2, "Задача 2", "Описание 2",Status.NEW);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.remove(1);

        List<Task> tasks = historyManager.getHistory();
        assertEquals(1, tasks.size());
        assertFalse(tasks.contains(task1));
        assertTrue(tasks.contains(task2));
    }

    @Test
    public void getHistory_addSameTasks_tasksAdd_() {
        Task task1 = new Task(1, "Задача 1", "Описание 1",Status.NEW);
        Task task2 = new Task(2, "Задача 2", "Описание 2",Status.NEW);
        Task task3 = new Task(3, "Задача 3", "Описание 3",Status.NEW);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        List<Task> tasks = historyManager.getHistory();
        assertEquals(3, tasks.size());
        assertEquals(List.of(task1, task2, task3), tasks);
    }

    @Test
    public void remove_getHistory_removeNonExistentTask() {
        Task task1 = new Task(1, "Задача 1", "Описание 1",Status.NEW);
        historyManager.add(task1);

        historyManager.remove(999); // Удаляем несуществующую задачу

        List<Task> tasks = historyManager.getHistory();
        assertEquals(1, tasks.size());
        assertTrue(tasks.contains(task1));
    }
}