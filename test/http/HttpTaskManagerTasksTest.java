package test.http;

import main.javakanban.http.HttpTaskServer;
import main.javakanban.manager.task.InMemoryTaskManager;
import main.javakanban.manager.task.TaskManager;
import main.javakanban.model.Task;
import main.javakanban.model.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskManagerTasksTest {

    private TaskManager manager;
    private HttpTaskServer taskServer;

    public HttpTaskManagerTasksTest() throws IOException {
        manager = new InMemoryTaskManager();
        taskServer = new HttpTaskServer(manager);
    }

    @BeforeEach
    public void setUp() {
        manager.deleteTasks();
        manager.deleteSubtasks();
        manager.deleteEpics();
        taskServer.start();
    }

    @AfterEach
    public void shutDown() {
        taskServer.stop();
    }

    @Test
    public void testAddTask() throws IOException, InterruptedException {
        Task task = new Task("Test Task", "Testing task");
        task.setStatus(Status.NEW);

        String taskJson = "{\"name\":\"Test Task\",\"description\":\"Testing task\",\"status\":\"NEW\"}";

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        List<Task> tasksFromManager = manager.getTasks();

        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Test Task", tasksFromManager.get(0).getName(), "Некорректное имя задачи");
    }

    @Test
    public void testGetTasks() throws IOException, InterruptedException {
        Task task = new Task("Test Task", "Testing task");
        manager.addTask(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        assertTrue(response.body().contains("Test Task"), "Ответ не содержит ожидаемую задачу");
    }

    @Test
    public void testGetTaskById() throws IOException, InterruptedException {
        Task task = new Task("Test Task", "Testing task");
        Task addedTask = manager.addTask(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/" + addedTask.getId());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        assertTrue(response.body().contains("Test Task"), "Ответ не содержит ожидаемую задачу");
    }

    @Test
    public void testGetTaskByIdNotFound() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/999");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    @Test
    public void testUpdateTask() throws IOException, InterruptedException {
        Task task = new Task("Original Task", "Original description");
        Task addedTask = manager.addTask(task);

        addedTask.setName("Updated Task");
        addedTask.setDescription("Updated description");
        addedTask.setStatus(Status.IN_PROGRESS);

        String taskJson = "{\"id\":" + addedTask.getId() + ",\"name\":\"Updated Task\",\"description\":\"Updated description\",\"status\":\"IN_PROGRESS\"}";

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        Task updatedTask = manager.getTaskByID(addedTask.getId());
        assertEquals("Updated Task", updatedTask.getName(), "Имя задачи не обновилось");
        assertEquals("Updated description", updatedTask.getDescription(), "Описание задачи не обновилось");
        assertEquals(Status.IN_PROGRESS, updatedTask.getStatus(), "Статус задачи не обновился");
    }

    @Test
    public void testDeleteTask() throws IOException, InterruptedException {
        Task task = new Task("Test Task", "Testing task");
        Task addedTask = manager.addTask(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/" + addedTask.getId());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        List<Task> tasksFromManager = manager.getTasks();
        assertEquals(0, tasksFromManager.size(), "Задача не удалилась");
    }

    @Test
    public void testDeleteAllTasks() throws IOException, InterruptedException {
        manager.addTask(new Task("Task 1", "Description 1"));
        manager.addTask(new Task("Task 2", "Description 2"));

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        List<Task> tasksFromManager = manager.getTasks();
        assertEquals(0, tasksFromManager.size(), "Не все задачи удалились");
    }
}

