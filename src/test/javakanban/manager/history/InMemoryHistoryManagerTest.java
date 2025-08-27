package test.javakanban.manager.history;

import main.javakanban.manager.Managers;
import main.javakanban.manager.task.TaskManager;
import main.javakanban.model.Epic;
import main.javakanban.model.Status;
import main.javakanban.model.Subtask;
import main.javakanban.model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static test.javakanban.manager.task.InMemoryTaskManagerTest.*;

class InMemoryHistoryManagerTest {
    private static TaskManager taskManager;

    @BeforeEach
    public void beforeEach() {
        taskManager = Managers.getDefault();
    }

    @Test
    public void addTasks_getHistory_taskIsAddOnlyOne() {
        // Создаем задачу, эпик и подзадачу
        final Task task = new Task(null, "Новая задача", "Описание задачи", Status.NEW);
        final int taskId = taskManager.addTask(task).getId();
        final Epic epic = new Epic(0, "Новый эпик", "Описание эпика", Status.NEW);
        final int epicId = taskManager.addEpic(epic).getId();
        final Subtask subtask = new Subtask(null,"Описание подзадачи", Status.NEW, epicId);
        final int subtaskId = taskManager.addSubtask(subtask).getId();

        // Добавляем задачу, эпик и подзадачу в историю
        taskManager.getTaskByID(taskId);
        taskManager.getEpicByID(epicId);
        taskManager.getSubtaskByID(subtaskId);

        // Добавляем объекты второй раз
        taskManager.getTaskByID(taskId);
        taskManager.getEpicByID(epicId);
        taskManager.getSubtaskByID(subtaskId);

        // Проверяем, что в истории нет дубликатов
        final List<Task> history = taskManager.getHistory();
        assertEquals(3, history.size(), "Размер истории не совпадает.");
    }

    @Test
    public void historyShouldBeEmptyWhenNoTasksAdded() {
        // Получаем историю и проверяем, что история пуста
        final List<Task> history = taskManager.getHistory();
        assertTrue(history.isEmpty(), "История должна быть пустой.");
    }

    @Test
    public void deleteTasks_getHistory_taskIsDelete() {
        // Создаем новую задачу, эпик и подзадачу
        final Task task = new Task(null, "Новая задача", "Описание задачи", Status.NEW);
        final int taskId = taskManager.addTask(task).getId();
        final Epic epic = new Epic(0, "Новый эпик", "Описание эпика", Status.NEW);
        final int epicId = taskManager.addEpic(epic).getId();
        final Subtask subtask = new Subtask(null,"Описание подзадачи", Status.NEW, epicId);
        final int subtaskId = taskManager.addSubtask(subtask).getId();

        // Удаляем задачу из репозитория, проверяем что история пуста
        taskManager.deleteTasks();
        List<Task> history = taskManager.getHistory();
        assertTrue(history.isEmpty(), "История должна быть пустой.");

        // Удаляем подзадачу из репозитория, проверяем что история пуста
        taskManager.deleteSubtasks();
        history = taskManager.getHistory();
        assertTrue(history.isEmpty(), "История должна быть пустой.");

        // Удаляем эпик из репозитория, проверяем что история пуста
        taskManager.deleteEpics();
        history = taskManager.getHistory();
        assertTrue(history.isEmpty(), "История должна быть пустой.");
    }
}
