package main.javakanban.http;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import main.javakanban.exception.NotFoundException;
import main.javakanban.exception.TimeIntervalConflictException;
import main.javakanban.manager.task.TaskManager;
import main.javakanban.model.Epic;
import main.javakanban.model.Subtask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class EpicHandler extends BaseHttpHandler implements HttpHandler {

    public EpicHandler(TaskManager taskManager, Gson gson) {
        super(taskManager, gson);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        System.out.println("Началась обработка /epics запроса от клиента.");
        try (exchange) {
            String path = exchange.getRequestURI().getPath();
            String requestMethod = exchange.getRequestMethod();

            switch (requestMethod) {
                case "GET":
                    if (Pattern.matches("^/epics$", path)) {
                        handleGetEpics(exchange);
                    } else if (Pattern.matches("^/epics/\\d+$", path)) {
                        handleGetEpicById(exchange, path);
                    } else if (Pattern.matches("^/epics/\\d+/subtasks$", path)) {
                        handleGetEpicSubtasks(exchange, path);
                    } else {
                        sendBadRequest(exchange);
                    }
                    break;

                case "POST":
                    if (Pattern.matches("^/epics$", path)) {
                        handleAddOrUpdateEpic(exchange);
                    } else {
                        sendBadRequest(exchange);
                    }
                    break;

                case "DELETE":
                    if (Pattern.matches("^/epics$", path)) {
                        handleDeleteAllEpics(exchange);
                    } else if (Pattern.matches("^/epics/\\d+$", path)) {
                        handleDeleteEpicById(exchange, path);
                    } else {
                        sendBadRequest(exchange);
                    }
                    break;
                default:
                    sendMethodNotAllowed(exchange);
            }
        } catch (Exception e) {
            System.err.println("Ошибка в handle(): " + e.getMessage());
            e.printStackTrace();
            sendInternalError(exchange);
        }
    }

    private void handleGetEpics(HttpExchange exchange) throws IOException {
        System.out.println("Клиент запросил список всех эпиков");
        List<Epic> epics = taskManager.getEpics();
        String response = gson.toJson(epics);
        sendText(exchange, response);
    }

    private void handleGetEpicById(HttpExchange exchange, String path) throws IOException {
        int id = extractIdFromPath(path, "/epics/");
        System.out.println("Запрошен эпик с id=" + id);
        try {
            Epic epic = taskManager.getEpicByID(id);
            String response = gson.toJson(epic);
            sendText(exchange, response);
        } catch (NotFoundException e) {
            sendNotFound(exchange);
        }
    }

    private void handleGetEpicSubtasks(HttpExchange exchange, String path) throws IOException {
        String pathId = path.replaceFirst("/epics/", "").replace("/subtasks", "");
        int id = Integer.parseInt(pathId);
        System.out.println("Запрошены подзадачи эпика с id=" + id);
        try {
            ArrayList<Subtask> subtasks = taskManager.getEpicSubtasks(id);
            String response = gson.toJson(subtasks);
            sendText(exchange, response);
        } catch (NotFoundException e) {
            sendNotFound(exchange);
        }
    }

    private void handleAddOrUpdateEpic(HttpExchange exchange) throws IOException {
        String json = readText(exchange);
        JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
        if (!isValidEpicJson(jsonObject)) {
            sendBadRequest(exchange);
            return;
        }
        Epic epic = parseEpicFromJson(jsonObject);
        System.out.println("Десериализовали эпик: " + epic);
        try {
            if (epic.getId() == null) {
                Epic createdEpic = taskManager.addEpic(epic);
                System.out.println("Добавлен новый эпик c id=" + createdEpic.getId());
                String response = gson.toJson(createdEpic);
                sendCreated(exchange, response);
            } else {
                Epic updatedEpic = taskManager.updateEpic(epic);
                System.out.println("Обновили эпик c id=" + updatedEpic.getId());
                String response = gson.toJson(updatedEpic);
                sendCreated(exchange, response);
            }
        } catch (TimeIntervalConflictException e) {
            sendHasInteractions(exchange);
        }
    }

    private void handleDeleteAllEpics(HttpExchange exchange) throws IOException {
        System.out.println("Удаляем все эпики");
        taskManager.deleteEpics();
        sendText(exchange, "{\"message\": \"All epics deleted\"}");
    }

    private void handleDeleteEpicById(HttpExchange exchange, String path) throws IOException {
        int id = extractIdFromPath(path, "/epics/");
        System.out.println("Удаляем эпик с id=" + id);
        taskManager.deleteEpicByID(id);
        sendText(exchange, "{\"message\": \"Epic deleted\"}");
    }

    private Epic parseEpicFromJson(JsonObject jsonObject) {
        Integer id = null;
        if (jsonObject.has("id") && !jsonObject.get("id").isJsonNull()) {
            id = jsonObject.get("id").getAsInt();
        }
        String name = jsonObject.get("name").getAsString();
        String description = jsonObject.get("description").getAsString();
        Epic epic = new Epic(name, description);
        if (id != null) {
            epic.setId(id);
        }
        return epic;
    }

    private boolean isValidEpicJson(JsonObject jsonObject) {
        return jsonObject.has("name") && jsonObject.has("description");
    }


}