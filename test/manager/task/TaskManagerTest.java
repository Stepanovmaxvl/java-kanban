package test.manager.task;

import main.javakanban.exception.TimeIntervalConflictException;
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

        assertThrows(TimeIntervalConflictException.class, () -> manager.addTask(t2),
                "Пересекающиеся задачи должны вызывать исключение");
    }

    @Test
    public void updateTask_timeConflictPreservesOldTask_oldTaskRemainsUnchanged() {
        Task task1 = new Task(null, "Задача 1", "Описание задачи 1", Status.NEW);
        task1.setDuration(Duration.ofMinutes(60));
        task1.setStartTime(LocalDateTime.of(2024, 1, 1, 10, 0)); // 10:00-11:00

        Task task2 = new Task(null, "Задача 2", "Описание задачи 2", Status.NEW);
        task2.setDuration(Duration.ofMinutes(60));
        task2.setStartTime(LocalDateTime.of(2024, 1, 1, 12, 0)); // 12:00-13:00

        manager.addTask(task1);
        manager.addTask(task2);

        LocalDateTime originalStartTime = task1.getStartTime();
        String originalName = task1.getName();
        Status originalStatus = task1.getStatus();

        Task updatedTask1 = new Task(task1.getId(), "Обновленная задача 1", "Новое описание", Status.IN_PROGRESS);
        updatedTask1.setDuration(Duration.ofMinutes(60));
        updatedTask1.setStartTime(LocalDateTime.of(2024, 1, 1, 12, 30));

        assertThrows(TimeIntervalConflictException.class, () -> manager.updateTask(updatedTask1),
                "Ожидалось исключение TimeIntervalConflictException");

        Task taskFromMap = manager.getTaskByID(task1.getId());
        assertNotNull(taskFromMap, "Задача должна остаться в мапе");
        assertEquals(originalStartTime, taskFromMap.getStartTime(), "Время начала должно остаться прежним");
        assertEquals(originalName, taskFromMap.getName(), "Название должно остаться прежним");
        assertEquals(originalStatus, taskFromMap.getStatus(), "Статус должен остаться прежним");

        assertEquals(2, manager.getPrioritizedTasks().size(),
                "Количество приоритизированных задач должно остаться прежним");

        Task conflictingTask = new Task(null, "Конфликтующая задача", "Описание", Status.NEW);
        conflictingTask.setDuration(Duration.ofMinutes(30));
        conflictingTask.setStartTime(LocalDateTime.of(2024, 1, 1, 10, 30)); // Пересекается с task1

        assertThrows(TimeIntervalConflictException.class, () -> manager.addTask(conflictingTask),
                "Слоты должны быть заняты под старую задачу");
    }
}