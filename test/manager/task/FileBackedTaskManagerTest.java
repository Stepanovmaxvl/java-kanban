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
        manager.initialize();
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
    public void add_load_saveTasks() {
        Task task1 = new Task(null, "Первое задание", "Описание первого задания", Status.NEW);
        Task task2 = new Task(null, "Второе задание", "Описание второго задания", Status.NEW);
        Epic epic1 = new Epic(0, "Первый эпик", "Описание первого эпика", Status.NEW);

        manager.addTask(task1);
        manager.addTask(task2);
        manager.addEpic(epic1);

        manager.save();

        FileBackedTaskManager loadedManager = new FileBackedTaskManager(tempFile);

        assertEquals(2, loadedManager.getTasks().size());
        assertEquals(1, loadedManager.getEpics().size());
    }

    @Test
    public void save_getTasks_loadMultipleTasks() {
        Task task1 = new Task(null, "Задача 1", "Описание 1", Status.NEW);
        Subtask subtask1 = new Subtask("Подзадача 1", "Описание Подзадачи 1", Status.NEW, 0);
        Epic epic1 = new Epic(1, "Эпик 1", "Описание Эпика 1", Status.NEW);

        manager.addTask(task1);
        manager.addSubtask(subtask1);
        manager.addEpic(epic1);
        manager.save();

        FileBackedTaskManager loadedManager = new FileBackedTaskManager(tempFile);

        assertEquals(1, loadedManager.getTasks().size());
        assertEquals(1, loadedManager.getEpics().size());
    }

    @AfterEach
    public void tearDown() {
        if (tempFile.exists()) {
            tempFile.delete();
        }
    }
}