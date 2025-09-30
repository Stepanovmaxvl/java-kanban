package main.javakanban.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import main.javakanban.exception.NotFoundException;
import main.javakanban.exception.TimeIntervalConflictException;
import main.javakanban.manager.task.TaskManager;
import main.javakanban.model.Subtask;

import java.io.IOException;
import java.util.List;

public class SubtaskHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;

    public SubtaskHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            switch (method) {
                case "GET":
                    handleGet(exchange, path);
                    break;
                case "POST":
                    handlePost(exchange);
                    break;
                case "DELETE":
                    handleDelete(exchange, path);
                    break;
                default:
                    sendNotFound(exchange);
            }
        } catch (NotFoundException e) {
            sendNotFound(exchange);
        } catch (TimeIntervalConflictException e) {
            sendHasInteractions(exchange);
        } catch (Exception e) {
            System.err.println("Ошибка в handle(): " + e.getMessage());
            e.printStackTrace();
            sendInternalError(exchange);
        }
    }

    private void handleGet(HttpExchange exchange, String path) throws IOException {
        if (path.equals("/subtasks")) {
            List<Subtask> subtasks = taskManager.getSubtasks();
            String response = toJsonArray(subtasks);
            sendText(exchange, response);
        } else if (path.startsWith("/subtasks/")) {
            String idString = path.substring("/subtasks/".length());
            int id = parsePathId(idString);
            if (id == -1) {
                sendNotFound(exchange);
                return;
            }
            Subtask subtask = taskManager.getSubtaskByID(id);
            String response = toJson(subtask);
            sendText(exchange, response);
        } else {
            sendNotFound(exchange);
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        try {
            String body = readText(exchange);

            Subtask subtask = parseSubtaskFromJson(body);

            if (subtask.getId() == null) {
                Integer id = taskManager.addSubtask(subtask);
                subtask.setId(id);
                String response = toJson(subtask);
                sendCreated(exchange, response);
            } else {
                Subtask updatedSubtask = taskManager.updateSubtask(subtask);
                String response = toJson(updatedSubtask);
                sendCreated(exchange, response);
            }
        } catch (Exception e) {
            System.err.println("Ошибка в handlePost: " + e.getMessage());
            e.printStackTrace();
            sendInternalError(exchange);
        }
    }

    private Subtask parseSubtaskFromJson(String json) {
        String name = extractField(json, "name");
        String description = extractField(json, "description");
        String statusStr = extractField(json, "status");
        String epicIdStr = extractField(json, "epicId");
        String idStr = extractField(json, "id");

        int epicId = epicIdStr != null ? Integer.parseInt(epicIdStr) : 0;
        main.javakanban.model.Status status = statusStr != null ?
                main.javakanban.model.Status.valueOf(statusStr) : main.javakanban.model.Status.NEW;

        Subtask subtask = new Subtask(name != null ? name : "",
                description != null ? description : "",
                status, epicId);

        if (idStr != null) {
            subtask.setId(Integer.parseInt(idStr));
        }

        return subtask;
    }

    private String extractField(String json, String fieldName) {
        String pattern = "\"" + fieldName + "\":\"([^\"]+)\"";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(json);
        if (m.find()) {
            return m.group(1);
        }

        pattern = "\"" + fieldName + "\":(\\d+)";
        p = java.util.regex.Pattern.compile(pattern);
        m = p.matcher(json);
        if (m.find()) {
            return m.group(1);
        }

        return null;
    }

    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        if (path.equals("/subtasks")) {
            taskManager.deleteSubtasks();
            sendText(exchange, "{\"message\": \"All subtasks deleted\"}");
        } else if (path.startsWith("/subtasks/")) {
            String idString = path.substring("/subtasks/".length());
            int id = parsePathId(idString);
            if (id == -1) {
                sendNotFound(exchange);
                return;
            }
            taskManager.deleteSubtaskByID(id);
            sendText(exchange, "{\"message\": \"Subtask deleted\"}");
        } else {
            sendNotFound(exchange);
        }
    }

    private String toJson(Subtask subtask) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"id\":").append(subtask.getId() != null ? subtask.getId() : "null").append(",");
        json.append("\"name\":\"").append(escapeJson(subtask.getName())).append("\",");
        json.append("\"description\":\"").append(escapeJson(subtask.getDescription())).append("\",");
        json.append("\"status\":\"").append(subtask.getStatus()).append("\",");
        json.append("\"type\":\"").append(subtask.getType()).append("\",");
        json.append("\"epicId\":").append(subtask.getEpicId());
        json.append("}");
        return json.toString();
    }

    private String toJsonArray(List<? extends Subtask> subtasks) {
        StringBuilder json = new StringBuilder();
        json.append("[");
        for (int i = 0; i < subtasks.size(); i++) {
            if (i > 0) json.append(",");
            json.append(toJson(subtasks.get(i)));
        }
        json.append("]");
        return json.toString();
    }

    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }
}