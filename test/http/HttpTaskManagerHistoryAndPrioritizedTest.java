package test.http;

import main.javakanban.http.HttpTaskServer;
import main.javakanban.manager.task.InMemoryTaskManager;
import main.javakanban.manager.task.TaskManager;
import main.javakanban.model.Epic;
import main.javakanban.model.Subtask;
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

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskManagerHistoryAndPrioritizedTest {

    private TaskManager manager;
    private HttpTaskServer taskServer;

    public HttpTaskManagerHistoryAndPrioritizedTest() throws IOException {
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
    public void testGetHistory() throws IOException, InterruptedException {
        Task task1 = new Task("Task 1", "Description 1");
        Task addedTask1 = manager.addTask(task1);
        manager.getTaskByID(addedTask1.getId());

        Task task2 = new Task("Task 2", "Description 2");
        Task addedTask2 = manager.addTask(task2);
        manager.getTaskByID(addedTask2.getId());

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/history");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        assertTrue(response.body().contains("Task 1"), "Ответ не содержит первую задачу из истории");
        assertTrue(response.body().contains("Task 2"), "Ответ не содержит вторую задачу из истории");
    }

    @Test
    public void testGetHistoryEmpty() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/history");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        assertEquals("[]", response.body(), "История должна быть пустой");
    }

    @Test
    public void testGetPrioritizedTasks() throws IOException, InterruptedException {
        Task task1 = new Task("Task 1", "Description 1");
        manager.addTask(task1);

        Task task2 = new Task("Task 2", "Description 2");
        manager.addTask(task2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/prioritized");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        assertTrue(response.body().contains("Task 1"), "Ответ не содержит первую задачу");
        assertTrue(response.body().contains("Task 2"), "Ответ не содержит вторую задачу");
    }

    @Test
    public void testGetPrioritizedTasksEmpty() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/prioritized");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        assertEquals("[]", response.body(), "Список приоритизированных задач должен быть пустым");
    }

    @Test
    public void testGetPrioritizedTasksWithTasksAndSubtasks() throws IOException, InterruptedException {
        Task task = new Task("Test Task", "Testing task");
        task.setStartTime(java.time.LocalDateTime.now());
        manager.addTask(task);

        Epic epic = new Epic("Test Epic", "Testing epic");
        manager.addEpic(epic);

        Subtask subtask = new Subtask("Test Subtask", "Testing subtask", Status.NEW, epic.getId());
        subtask.setStartTime(java.time.LocalDateTime.now().plusHours(1));
        manager.addSubtask(subtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/prioritized");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        assertTrue(response.body().contains("Test Task"), "Ответ не содержит задачу");
        assertTrue(response.body().contains("Test Subtask"), "Ответ не содержит подзадачу");
    }

    @Test
    public void testHistoryEndpointNotFound() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/history");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString("{}"))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(405, response.statusCode());
    }

    @Test
    public void testPrioritizedEndpointNotFound() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/prioritized");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString("{}"))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(405, response.statusCode());
    }
}


