package manager.task;

import main.javakanban.manager.task.TaskManager;
import main.javakanban.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public abstract class TaskManagerTest<T extends TaskManager> {

    protected T manager;

    protected abstract T createManager();

    @BeforeEach
    public void setup() {
        manager = createManager();
    }

    @Test
    public void subtaskHasLinkedEpic_epicSubtasksContainIt() {
        Epic epic = manager.addEpic(new Epic(0, "Эпик", "Описание", Status.NEW));
        Subtask sub = new Subtask("S", "SD", Status.NEW, epic.getId());
        manager.addSubtask(sub);
        assertEquals(epic.getId(), manager.getSubtaskByID(sub.getId()).getEpicId());
        assertTrue(manager.getEpicSubtasks(epic.getId()).stream().anyMatch(s -> s.getId().equals(sub.getId())));
    }

    @Test
    public void epicStatus_fromSubtasksMixed_isInProgress() {
        Epic epic = manager.addEpic(new Epic(0, "Эпик", "Описание", Status.NEW));
        manager.addSubtask(new Subtask("S1", "", Status.NEW, epic.getId()));
        manager.addSubtask(new Subtask("S2", "", Status.DONE, epic.getId()));
        assertEquals(Status.IN_PROGRESS, manager.getEpicByID(epic.getId()).getStatus());
    }

    @Test
    public void prioritizedTasks_sortedByStartTime_nullsLast() {
        Task t1 = new Task(null, "A", "", Status.NEW);
        t1.setStartTime(LocalDateTime.of(2025, 1, 1, 10, 0));
        Task t2 = new Task(null, "B", "", Status.NEW);
        t2.setStartTime(LocalDateTime.of(2025, 1, 1, 9, 0));
        Task t3 = new Task(null, "C", "", Status.NEW);
        manager.addTask(t1);
        manager.addTask(t2);
        manager.addTask(t3);
        List<Task> pr = manager.getPrioritizedTasks();
        assertEquals("B", pr.get(0).getName());
        assertEquals("A", pr.get(1).getName());
        assertEquals("C", pr.get(pr.size() - 1).getName());
    }

    @Test
    public void timeIntervals_overlap_throwsException() {
        Task t1 = new Task(null, "T1", "", Status.NEW);
        t1.setStartTime(LocalDateTime.of(2025, 1, 1, 10, 0));
        t1.setDuration(Duration.ofMinutes(60));
        manager.addTask(t1);

        Task t2 = new Task(null, "T2", "", Status.NEW);
        t2.setStartTime(LocalDateTime.of(2025, 1, 1, 10, 30));
        t2.setDuration(Duration.ofMinutes(30));
        
        assertThrows(IllegalArgumentException.class, () -> manager.addTask(t2),
                "Пересекающиеся задачи должны вызывать исключение");
    }
}


