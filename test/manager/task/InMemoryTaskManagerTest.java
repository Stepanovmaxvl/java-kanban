package test.manager.task;

import main.javakanban.manager.task.InMemoryTaskManager;
import main.javakanban.model.Epic;
import main.javakanban.model.Status;
import main.javakanban.model.Subtask;
import main.javakanban.model.Task;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryTaskManagerTest extends test.manager.task.TaskManagerTest<InMemoryTaskManager> {

    @Override
    protected InMemoryTaskManager createManager() {
        return new InMemoryTaskManager();
    }

    @Test
    void addTask_returnSameId_taskIsAdded() {
        final Task task = manager.addTask(new Task("Задача 1", "Сделать Задачу 1"));
        final Task savedTask = manager.getTaskByID(task.getId());
        assertNotNull(savedTask, "Задача не найдена.");
        assertEquals(task, savedTask, "Задачи не совпадают.");
    }

    @Test
    void getTasks_addNewTask_idTaskAreSame() {
        final Task task = manager.addTask(new Task("Задача 1", "Сделать Задачу 1"));
        final List<Task> tasks = manager.getTasks();
        assertNotNull(tasks, "Задачи не возвращаются.");
        assertEquals(1, tasks.size(), "Неверное количество задач.");
        assertEquals(task, tasks.getFirst(), "Задачи не совпадают.");
    }

    @Test
    void addEpic_returnSameId_epicIsAdded() {
        //проверяем, что InMemoryTaskManager добавляет эпики и подзадачи и может найти их по id;
        final Epic flatRenovation = manager.addEpic(new Epic("Сделать ремонт",
                "Нужно успеть за отпуск"));
        final Epic savedEpic = manager.getEpicByID(flatRenovation.getId());
        assertNotNull(savedEpic, "Эпик не найден.");
        assertEquals(flatRenovation, savedEpic, "Эпики не совпадают.");

        final List<Epic> epics = manager.getEpics();
        assertNotNull(epics, "Эпики не возвращаются.");
        assertEquals(1, epics.size(), "Неверное количество эпиков.");
        assertEquals(flatRenovation, epics.getFirst(), "Эпики не совпадают.");

    }

    @Test
    public void updateTask_returnUpdatedTask_taskIsUpdated() {
        final Task expected = new Task("имя", "описание");
        manager.addTask(expected);
        final Task updatedTask = new Task(expected.getId(), "новое имя", "новое описание", Status.DONE);
        final Task actual = manager.updateTask(updatedTask);
        assertEquals(expected, actual, "Вернулась задачи с другим id");
    }

    @Test
    public void updateEpic_returnUpdatedEpic_epicIsUpdated() {
        final Epic expected = new Epic("имя", "описание");
        manager.addEpic(expected);
        final Epic updatedEpic = new Epic(expected.getId(), "новое имя", "новое описание", Status.DONE);
        final Epic actual = manager.updateEpic(updatedEpic);
        assertEquals(expected, actual, "Вернулся эпик с другим id");
    }

    @Test
    public void updateSubtask_returnSameId_subtaskIsUpdated() {
        final Epic epic = new Epic("имя", "описание");
        manager.addEpic(epic);
        final Subtask expected = new Subtask("имя", "описание", Status.NEW, epic.getId());
        manager.addSubtask(expected);
        final Subtask updatedSubtask = new Subtask("новое имя", "новое описание",
                Status.DONE, epic.getId());
        final Subtask actual = manager.updateSubtask(expected);
        assertEquals(expected, actual, "Вернулась подзадача с другим id");
    }

    @Test
    public void deleteTask_returnEmptyList_tasksAreDeleted() {
        manager.addTask(new Task("Купить книги", "Список в заметках"));
        manager.addTask(new Task("Помыть полы", "С новым средством"));
        manager.deleteTasks();
        List<Task> tasks = manager.getTasks();
        assertTrue(tasks.isEmpty(), "После удаления задач список должен быть пуст.");
    }

    @Test
    public void deleteEpic_returnEmptyList_epicsAreDeleted() {
        manager.addEpic(new Epic("Сделать ремонт", "Нужно успеть за отпуск"));
        manager.deleteEpics();
        List<Epic> epics = manager.getEpics();
        assertTrue(epics.isEmpty(), "После удаления эпиков список должен быть пуст.");
    }

    @Test
    public void deleteSubtask_returnEmptyList_subtasksAreDeleted() {
        Epic flatRenovation = new Epic("Сделать ремонт", "Нужно успеть за отпуск");
        manager.addEpic(flatRenovation);
        manager.addSubtask(new Subtask("Поклеить обои", "Обязательно светлые!", Status.NEW,
                flatRenovation.getId()));
        manager.addSubtask(new Subtask("Установить новую технику", "Старую продать на Авито", Status.NEW,
                flatRenovation.getId()));
        manager.addSubtask(new Subtask("Заказать книжный шкаф", "Из темного дерева", Status.NEW,
                flatRenovation.getId()));

        manager.deleteSubtasks();
        List<Subtask> subtasks = manager.getSubtasks();
        assertTrue(subtasks.isEmpty(), "После удаления подзадач список должен быть пуст.");
    }

    @Test
    public void deleteTask_returnNull_keyIsMissing() {
        manager.addTask(new Task(1, "Купить книги", "Список в заметках", Status.NEW));
        manager.addTask(new Task(2, "Помыть полы", "С новым средством", Status.DONE));
        assertNull(manager.deleteTaskByID(3));
    }

    @Test
    public void deleteEpic_returnNull_keyIsMissing() {
        manager.addEpic(new Epic(1, "Сделать ремонт", "Нужно успеть за отпуск", Status.IN_PROGRESS));
        manager.deleteEpicByID(1);
        assertNull(manager.deleteTaskByID(1));
    }


    @Test
    void addTask_addSameTask_taskIsAdded() {
        Task expected = new Task(1, "Помыть полы", "С новым средством", Status.DONE);
        manager.addTask(expected);
        List<Task> list = manager.getTasks();
        Task actual = list.getFirst();
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getDescription(), actual.getDescription());
        assertEquals(expected.getStatus(), actual.getStatus());
    }

    @Test
    public void getTaskById_addTask_checkHistory() {
        InMemoryTaskManager manager = new InMemoryTaskManager();

        Task task = new Task("Тестовая задача", "Описание тестовой задачи");
        manager.addTask(task);

        manager.getTaskByID(task.getId());

        List<Task> history = manager.getHistory();
        assertFalse(history.isEmpty(), "История должна содержать элементы");
        assertTrue(history.contains(task), "История должна содержать добавленную задачу");
    }

    @Test
    public void updateTask_returnOldTask_checkHistory() {
        InMemoryTaskManager manager = new InMemoryTaskManager();
        Task washFloor = new Task("Помыть полы", "С новым средством");
        manager.addTask(washFloor);
        manager.getTaskByID(washFloor.getId());
        manager.updateTask(new Task(washFloor.getId(), "Не забыть помыть полы",
                "Можно и без средства", Status.IN_PROGRESS));
        List<Task> tasks = manager.getHistory();
        Task oldTask = tasks.getFirst();
        assertEquals(washFloor.getName(), oldTask.getName(), "В истории не сохранилась старая версия задачи");
        assertEquals(washFloor.getDescription(), oldTask.getDescription(),
                "В истории не сохранилась старая версия задачи");
    }

    @Test
    public void addSubtask_notAddSubtask_subtaskIdSameWithEpicId() {
        InMemoryTaskManager manager = new InMemoryTaskManager();
        Epic epic = new Epic(1, "Эпик", "Описание эпика", Status.NEW);
        manager.addEpic(epic);
        Subtask subtask = new Subtask("Сабтаск", "Описание сабтаска", Status.NEW, epic.getId());
        subtask.setId(epic.getId());
        assertFalse(epic.equals(subtask));
    }
}