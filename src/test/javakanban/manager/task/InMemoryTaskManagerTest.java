package test.javakanban.manager.task;

import main.javakanban.manager.task.InMemoryTaskManager;
import main.javakanban.manager.task.TaskManager;
import main.javakanban.model.Epic;
import main.javakanban.model.Status;
import main.javakanban.model.Subtask;
import main.javakanban.model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryTaskManagerTest {

    private static TaskManager taskManager;

    @BeforeEach
    public void beforeEach() {
        taskManager = new InMemoryTaskManager();
    }

    @Test
    void addTask_returnSameId_taskIsAdded() {
        //проверяем, что InMemoryTaskManager добавляет задачи и может найти их по id;
        final Task task = taskManager.addTask(new Task("Задача 1", "Сделать Задачу 1"));
        final Task savedTask = taskManager.getTaskByID(task.getId());
        assertNotNull(savedTask, "Задача не найдена.");
        assertEquals(task, savedTask, "Задачи не совпадают.");
    }
    @Test
    void getTasks_addNewTask_idTaskAreSame(){
        final Task task = taskManager.addTask(new Task("Задача 1", "Сделать Задачу 1"));
        final List<Task> tasks = taskManager.getTasks();
        assertNotNull(tasks, "Задачи не возвращаются.");
        assertEquals(1, tasks.size(), "Неверное количество задач.");
        assertEquals(task, tasks.getFirst(), "Задачи не совпадают.");
    }

    @Test
    void addEpic_returnSameId_epicIsAdded() {
        //проверяем, что InMemoryTaskManager добавляет эпики и подзадачи и может найти их по id;
        final Epic flatRenovation = taskManager.addEpic(new Epic("Сделать ремонт",
                "Нужно успеть за отпуск"));
        final Epic savedEpic = taskManager.getEpicByID(flatRenovation.getId());
        assertNotNull(savedEpic, "Эпик не найден.");
        assertEquals(flatRenovation, savedEpic, "Эпики не совпадают.");

        final List<Epic> epics = taskManager.getEpics();
        assertNotNull(epics, "Эпики не возвращаются.");
        assertEquals(1, epics.size(), "Неверное количество эпиков.");
        assertEquals(flatRenovation, epics.getFirst(), "Эпики не совпадают.");

    }

    @Test
    void addSubtask_returnSameId_subtaskIsAdded() {
        final Epic flatRenovation = taskManager.addEpic(new Epic("Сделать ремонт",
                "Нужно успеть за отпуск"));
        final Subtask flatRenovationSubtask1 = taskManager.addSubtask(new Subtask("Поклеить обои",
                "Обязательно светлые!", Status.NEW, flatRenovation.getId()));
        final Subtask flatRenovationSubtask2 = taskManager.addSubtask(new Subtask("Установить новую технику",
                "Старую продать на Авито",Status.NEW, flatRenovation.getId()));
        final Subtask flatRenovationSubtask3 = taskManager.addSubtask(new Subtask("Заказать книжный шкаф", "Из темного дерева",Status.NEW,
                flatRenovation.getId()));
        final Subtask savedSubtask1 = taskManager.getSubtaskByID(flatRenovationSubtask1.getId());
        final Subtask savedSubtask2 = taskManager.getSubtaskByID(flatRenovationSubtask2.getId());
        final Subtask savedSubtask3 = taskManager.getSubtaskByID(flatRenovationSubtask3.getId());
        assertNotNull(savedSubtask2, "Подзадача не найдена.");
        assertEquals(flatRenovationSubtask1, savedSubtask1, "Подзадачи не совпадают.");
        assertEquals(flatRenovationSubtask3, savedSubtask3, "Подзадачи не совпадают.");
        final List<Subtask> subtasks = taskManager.getSubtasks();
        assertNotNull(subtasks, "Подзадачи не возвращаются.");
        assertEquals(3, subtasks.size(), "Неверное количество подзадач.");
        assertEquals(savedSubtask1, subtasks.getFirst(), "Подзадачи не совпадают.");
    }

    @Test
    public void updateTask_returnUpdatedTask_taskIsUpdated()  {
        final Task expected = new Task("имя", "описание");
        taskManager.addTask(expected);
        final Task updatedTask = new Task(expected.getId(), "новое имя", "новое описание", Status.DONE);
        final Task actual = taskManager.updateTask(updatedTask);
        assertEquals(expected, actual, "Вернулась задачи с другим id");
    }

    @Test
    public void updateEpic_returnUpdatedEpic_epicIsUpdated () {
        final Epic expected = new Epic("имя", "описание");
        taskManager.addEpic(expected);
        final Epic updatedEpic = new Epic(expected.getId(), "новое имя", "новое описание", Status.DONE);
        final Epic actual = taskManager.updateEpic(updatedEpic);
        assertEquals(expected, actual, "Вернулся эпик с другим id");
    }

    @Test
    public void updateSubtask_returnSameId_subtaskIsUpdated () {
        final Epic epic = new Epic("имя", "описание");
        taskManager.addEpic(epic);
        final Subtask expected = new Subtask("имя", "описание",Status.NEW, epic.getId());
        taskManager.addSubtask(expected);
        final Subtask updatedSubtask = new Subtask( "новое имя", "новое описание",
                Status.DONE, epic.getId());
        final Subtask actual = taskManager.updateSubtask(expected);
        assertEquals(expected, actual, "Вернулась подзадача с другим id");
    }

    @Test
    public void deleteTask_returnEmptyList_tasksAreDeleted() {
        taskManager.addTask(new Task("Купить книги", "Список в заметках"));
        taskManager.addTask(new Task("Помыть полы", "С новым средством"));
        taskManager.deleteTasks();
        List<Task> tasks = taskManager.getTasks();
        assertTrue(tasks.isEmpty(), "После удаления задач список должен быть пуст.");
    }

    @Test
    public void deleteEpic_returnEmptyList_epicsAreDeleted() {
        taskManager.addEpic(new Epic("Сделать ремонт", "Нужно успеть за отпуск"));
        taskManager.deleteEpics();
        List<Epic> epics = taskManager.getEpics();
        assertTrue(epics.isEmpty(), "После удаления эпиков список должен быть пуст.");
    }

    @Test
    public void deleteSubtask_returnEmptyList_subtasksAreDeleted() {
        Epic flatRenovation = new Epic("Сделать ремонт", "Нужно успеть за отпуск");
        taskManager.addEpic(flatRenovation);
        taskManager.addSubtask(new Subtask("Поклеить обои", "Обязательно светлые!", Status.NEW,
                flatRenovation.getId()));
        taskManager.addSubtask(new Subtask("Установить новую технику", "Старую продать на Авито",Status.NEW,
                flatRenovation.getId()));
        taskManager.addSubtask(new Subtask("Заказать книжный шкаф", "Из темного дерева",Status.NEW,
                flatRenovation.getId()));

        taskManager.deleteSubtasks();
        List<Subtask> subtasks = taskManager.getSubtasks();
        assertTrue(subtasks.isEmpty(), "После удаления подзадач список должен быть пуст.");
    }

    @Test
    public void deleteTask_returnNull_keyIsMissing() {
        taskManager.addTask(new Task(1, "Купить книги", "Список в заметках", Status.NEW));
        taskManager.addTask(new Task(2, "Помыть полы", "С новым средством", Status.DONE));
        assertNull(taskManager.deleteTaskByID(3));
    }

    @Test
    public void deleteEpic_returnNull_keyIsMissing() {
        taskManager.addEpic(new Epic(1, "Сделать ремонт", "Нужно успеть за отпуск", Status.IN_PROGRESS));
        taskManager.deleteEpicByID(1);
        assertNull(taskManager.deleteTaskByID(1));
    }


    @Test
    void addTask_addSameTask_taskIsAdded() {
        Task expected = new Task(1, "Помыть полы", "С новым средством", Status.DONE);
        taskManager.addTask(expected);
        List<Task> list = taskManager.getTasks();
        Task actual = list.getFirst();
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getDescription(), actual.getDescription());
        assertEquals(expected.getStatus(), actual.getStatus());
    }
}