package manager.task;

import main.javakanban.manager.task.FileBackedTaskManager;
import main.javakanban.model.Epic;
import main.javakanban.model.Status;
import main.javakanban.model.Subtask;
import main.javakanban.model.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class FileBackedTaskManagerTest {

    private FileBackedTaskManager manager;
    private File tempFile;

    @BeforeEach
    public void setUp() throws IOException {
        tempFile = File.createTempFile("taskstest", ".csv");
        tempFile.deleteOnExit();
        manager = new FileBackedTaskManager(tempFile);
    }

    @Test
    public void loadFromFile_getTasks_emptyFile() {
        assertDoesNotThrow(() -> {
            manager.initialize();
        });
        assertTrue(manager.getTasks().isEmpty());
        assertTrue(manager.getEpics().isEmpty());
        assertTrue(manager.getSubtasks().isEmpty());
    }

    @Test
    public void loadFromFile_getTasks_onlyOneTask() {
        Task task1 = new Task(null, "Первое задание", "Описание первого задания", Status.NEW);
        Task task2 = new Task(null, "Второе задание", "Описание второго задания", Status.NEW);

        manager.addTask(task1);
        manager.addTask(task2);
        manager.deleteTaskByID(task1.getId());

        FileBackedTaskManager newManager = new FileBackedTaskManager(tempFile);

        assertEquals(1, newManager.getTasks().size());
        assertEquals("Второе задание", newManager.getTasks().get(0).getName());
        assertEquals("Описание второго задания", newManager.getTasks().get(0).getDescription());
        assertEquals(Status.NEW, newManager.getTasks().get(0).getStatus());
    }

    @Test
    public void addTaskAndEpic_returnSameTasks_taskIsAdded() {
        Task task = new Task(null, "Первое задание", "Описание первого задания", Status.NEW);
        Epic epic = new Epic(0, "Первый эпик", "Описание первого эпика", Status.NEW); // Используйте null
        manager.addEpic(epic); // Сначала добавляем эпик
        Subtask subtask = new Subtask("Первая подзадача", "Описание первой подзадачи", Status.NEW, epic.getId());

        manager.addTask(task);
        manager.addSubtask(subtask);
        manager.deleteTaskByID(task.getId());

        FileBackedTaskManager newManager = new FileBackedTaskManager(tempFile);

        assertEquals(1, newManager.getEpics().size());
        Epic loadedEpic = newManager.getEpics().get(0);
        assertEquals(epic.getName(), loadedEpic.getName());
        assertEquals(epic.getDescription(), loadedEpic.getDescription());
        assertEquals(epic.getStatus(), loadedEpic.getStatus());

        assertEquals(1, newManager.getSubtasks().size());
        Subtask loadedSubtask = newManager.getSubtasks().get(0);
        assertEquals(subtask.getName(), loadedSubtask.getName());
        assertEquals(subtask.getDescription(), loadedSubtask.getDescription());
        assertEquals(subtask.getStatus(), loadedSubtask.getStatus());
        assertEquals(epic.getId(), loadedSubtask.getEpicId());
    }

    @Test
    public void addEpicAndTask_returnSameTasks_taskIsAdded() {
        Epic epic1 = manager.addEpic(new Epic(0, "Первый эпик", "Описание первого эпика", Status.NEW));
        assertNotNull(epic1, "Эпик не должен быть null.");

        Task task1 = manager.addTask(new Task(null, "Первое задание", "Описание первого задания", Status.NEW));
        assertNotNull(task1, "Задача не должна быть null.");

        Integer subtask1Id = manager.addSubtask(new Subtask("Первая подзадача", "Описание первой подзадачи", Status.NEW, epic1.getId())
        );
        assertNotNull(manager.getSubtaskByID(subtask1Id), "Сабтаск не должен быть null.");

        FileBackedTaskManager newManager = new FileBackedTaskManager(tempFile);

        Epic loadedEpic = newManager.getEpicByID(epic1.getId());
        Task loadedTask = newManager.getTaskByID(task1.getId());
        Subtask loadedSubtask = newManager.getSubtaskByID(subtask1Id);

        assertNotNull(loadedEpic, "Загруженный эпик не должен быть null.");
        assertEquals(epic1.getId(), loadedEpic.getId(), "ID эпиков не совпадают.");
        assertEquals(epic1.getName(), loadedEpic.getName(), "Имена эпиков не совпадают.");

        assertNotNull(loadedTask, "Загруженная задача не должна быть null.");
        assertEquals(task1.getId(), loadedTask.getId(), "ID задач не совпадают.");
        assertEquals(task1.getName(), loadedTask.getName(), "Имена задач не совпадают.");

        assertNotNull(loadedSubtask, "Загруженный сабтаск не должен быть null.");
        assertEquals(subtask1Id, loadedSubtask.getId(), "ID подзадач не совпадают.");
    }

    @Test
    public void addTasks_loadFromFile_taskIsAdded() {
        Task task1 = new Task(null, "Первое задание", "Описание первого задания", Status.NEW);
        Task task2 = new Task(null, "Второе задание", "Описание второго задания", Status.NEW);
        manager.addTask(task1);
        manager.addTask(task2);
        int initialTaskCount = manager.getTasks().size();
        assertEquals(2, initialTaskCount, "Должно быть 2 задачи в менеджере после первого добавления");

        FileBackedTaskManager newManager = new FileBackedTaskManager(tempFile);

        Task task3 = new Task(null, "Третье задание", "Описание третьего задания", Status.NEW);
        Task task4 = new Task(null, "Четвертое задание", "Описание четвертого задания", Status.NEW);
        newManager.addTask(task3);
        newManager.addTask(task4);

        int totalTaskCount = newManager.getTasks().size();
        assertEquals(4, totalTaskCount, "Должно быть 4 задачи в менеджере после второго добавления");

        assertNotNull(manager.getTasks().toArray());
        assertEquals("Первое задание", manager.getTasks().get(0).getName());
        assertEquals("Второе задание", manager.getTasks().get(1).getName());
    }

    @AfterEach
    public void tearDown() {
        if (tempFile.exists()) {
            tempFile.delete();
        }
    }
}