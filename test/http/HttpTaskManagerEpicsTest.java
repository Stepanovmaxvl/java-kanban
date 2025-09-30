package test.http;

import com.google.gson.Gson;
import main.javakanban.http.HttpTaskServer;
import main.javakanban.manager.task.InMemoryTaskManager;
import main.javakanban.manager.task.TaskManager;
import main.javakanban.model.Epic;
import main.javakanban.model.Subtask;
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

public class HttpTaskManagerEpicsTest {

    private TaskManager manager;
    private HttpTaskServer taskServer;
    private final Gson gson = new Gson();

    public HttpTaskManagerEpicsTest() throws IOException {
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
    public void testAddEpic() throws IOException, InterruptedException {
        String epicJson = "{\"name\":\"Test Epic\",\"description\":\"Testing epic\",\"status\":\"NEW\"}";

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        List<Epic> epicsFromManager = manager.getEpics();

        assertNotNull(epicsFromManager, "Эпики не возвращаются");
        assertEquals(1, epicsFromManager.size(), "Некорректное количество эпиков");
        assertEquals("Test Epic", epicsFromManager.get(0).getName(), "Некорректное имя эпика");
    }

    @Test
    public void testGetEpics() throws IOException, InterruptedException {
        Epic epic = new Epic("Test Epic", "Testing epic");
        manager.addEpic(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        assertTrue(response.body().contains("Test Epic"), "Ответ не содержит ожидаемый эпик");
    }

    @Test
    public void testGetEpicById() throws IOException, InterruptedException {
        Epic epic = new Epic("Test Epic", "Testing epic");
        Epic addedEpic = manager.addEpic(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/" + addedEpic.getId());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        assertTrue(response.body().contains("Test Epic"), "Ответ не содержит ожидаемый эпик");
    }

    @Test
    public void testGetEpicByIdNotFound() throws IOException, InterruptedException {

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/999");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    @Test
    public void testGetEpicSubtasks() throws IOException, InterruptedException {

        String epicJson = "{\"name\":\"Test Epic\",\"description\":\"Testing epic\",\"status\":\"NEW\"}";
        HttpClient client = HttpClient.newHttpClient();

        URI epicUrl = URI.create("http://localhost:8080/epics");
        HttpRequest epicRequest = HttpRequest.newBuilder()
                .uri(epicUrl)
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> epicResponse = client.send(epicRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, epicResponse.statusCode());

        String epicResponseBody = epicResponse.body();
        String idPattern = "\"id\":(\\d+)";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(idPattern);
        java.util.regex.Matcher matcher = pattern.matcher(epicResponseBody);
        Integer epicId = null;
        if (matcher.find()) {
            epicId = Integer.parseInt(matcher.group(1));
        }

        String subtaskJson = "{\"name\":\"Test Subtask\",\"description\":\"Testing subtask\",\"status\":\"NEW\",\"epicId\":" + epicId + "}";

        URI subtaskUrl = URI.create("http://localhost:8080/subtasks");
        HttpRequest subtaskRequest = HttpRequest.newBuilder()
                .uri(subtaskUrl)
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> subtaskResponse = client.send(subtaskRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, subtaskResponse.statusCode());

        URI url = URI.create("http://localhost:8080/epics/" + epicId + "/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        assertTrue(response.body().contains("Test Subtask"), "Ответ не содержит ожидаемую подзадачу");
    }

    @Test
    public void testGetEpicSubtasksNotFound() throws IOException, InterruptedException {

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/999/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    @Test
    public void testUpdateEpic() throws IOException, InterruptedException {

        Epic epic = new Epic("Original Epic", "Original description");
        Epic addedEpic = manager.addEpic(epic);

        addedEpic.setName("Updated Epic");
        addedEpic.setDescription("Updated description");
        addedEpic.setStatus(Status.IN_PROGRESS);

        String epicJson = "{\"id\":" + addedEpic.getId() + ",\"name\":\"Updated Epic\",\"description\":\"Updated description\",\"status\":\"IN_PROGRESS\"}";

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        Epic updatedEpic = manager.getEpicByID(addedEpic.getId());
        assertEquals("Updated Epic", updatedEpic.getName(), "Имя эпика не обновилось");
        assertEquals("Updated description", updatedEpic.getDescription(), "Описание эпика не обновилось");
        assertEquals(Status.IN_PROGRESS, updatedEpic.getStatus(), "Статус эпика не обновился");
    }

    @Test
    public void testDeleteEpic() throws IOException, InterruptedException {

        Epic epic = new Epic("Test Epic", "Testing epic");
        Epic addedEpic = manager.addEpic(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/" + addedEpic.getId());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        List<Epic> epicsFromManager = manager.getEpics();
        assertEquals(0, epicsFromManager.size(), "Эпик не удалился");
    }

    @Test
    public void testDeleteAllEpics() throws IOException, InterruptedException {
        manager.addEpic(new Epic("Epic 1", "Description 1"));
        manager.addEpic(new Epic("Epic 2", "Description 2"));

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        List<Epic> epicsFromManager = manager.getEpics();
        assertEquals(0, epicsFromManager.size(), "Не все эпики удалились");
    }
}

