package main.javakanban.http;

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

public class EpicHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;

    public EpicHandler(TaskManager taskManager) {
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
        if (path.equals("/epics")) {
            List<Epic> epics = taskManager.getEpics();
            String response = toJsonArray(epics);
            sendText(exchange, response);
        } else if (path.startsWith("/epics/")) {
            String[] pathParts = path.substring("/epics/".length()).split("/");
            int id = parsePathId(pathParts[0]);
            if (id == -1) {
                sendNotFound(exchange);
                return;
            }

            if (pathParts.length == 1) {
                Epic epic = taskManager.getEpicByID(id);
                String response = toJson(epic);
                sendText(exchange, response);
            } else if (pathParts.length == 2 && pathParts[1].equals("subtasks")) {
                ArrayList<Subtask> subtasks = taskManager.getEpicSubtasks(id);
                String response = toJsonArraySubtasks(subtasks);
                sendText(exchange, response);
            } else {
                sendNotFound(exchange);
            }
        } else {
            sendNotFound(exchange);
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        try {
            String body = readText(exchange);
            System.out.println("Received JSON: " + body);

            Epic epic = parseEpicFromJson(body);
            System.out.println("Parsed Epic: " + epic.getName() + ", " + epic.getDescription() + ", " + epic.getStatus());

            if (epic.getId() == null) {
                Epic createdEpic = taskManager.addEpic(epic);
                String response = toJson(createdEpic);
                sendCreated(exchange, response);
            } else {
                Epic updatedEpic = taskManager.updateEpic(epic);
                String response = toJson(updatedEpic);
                sendCreated(exchange, response);
            }
        } catch (Exception e) {
            System.err.println("Ошибка в handlePost: " + e.getMessage());
            e.printStackTrace();
            sendInternalError(exchange);
        }
    }

    private Epic parseEpicFromJson(String json) {
        try {
            System.out.println("Parsing JSON: " + json);

            Epic epic = new Epic("", "");

            String namePattern = "\"name\":\"([^\"]+)\"";
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(namePattern);
            java.util.regex.Matcher matcher = pattern.matcher(json);
            if (matcher.find()) {
                epic.setName(matcher.group(1));
                System.out.println("Found name: " + matcher.group(1));
            }

            String descPattern = "\"description\":\"([^\"]+)\"";
            pattern = java.util.regex.Pattern.compile(descPattern);
            matcher = pattern.matcher(json);
            if (matcher.find()) {
                epic.setDescription(matcher.group(1));
                System.out.println("Found description: " + matcher.group(1));
            }

            String statusPattern = "\"status\":\"([^\"]+)\"";
            pattern = java.util.regex.Pattern.compile(statusPattern);
            matcher = pattern.matcher(json);
            if (matcher.find()) {
                String statusStr = matcher.group(1);
                epic.setStatus(main.javakanban.model.Status.valueOf(statusStr));
                System.out.println("Found status: " + statusStr);
            }

            String idPattern = "\"id\":(\\d+)";
            pattern = java.util.regex.Pattern.compile(idPattern);
            matcher = pattern.matcher(json);
            if (matcher.find()) {
                epic.setId(Integer.parseInt(matcher.group(1)));
                System.out.println("Found id: " + matcher.group(1));
            }

            System.out.println("Final Epic: " + epic.getName() + ", " + epic.getDescription() + ", " + epic.getStatus());
            return epic;
        } catch (Exception e) {
            throw e;
        }
    }

    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        if (path.equals("/epics")) {
            taskManager.deleteEpics();
            sendText(exchange, "{\"message\": \"All epics deleted\"}");
        } else if (path.startsWith("/epics/")) {
            String idString = path.substring("/epics/".length());
            int id = parsePathId(idString);
            if (id == -1) {
                sendNotFound(exchange);
                return;
            }
            taskManager.deleteEpicByID(id);
            sendText(exchange, "{\"message\": \"Epic deleted\"}");
        } else {
            sendNotFound(exchange);
        }
    }

    private String toJson(Epic epic) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"id\":").append(epic.getId() != null ? epic.getId() : "null").append(",");
        json.append("\"name\":\"").append(escapeJson(epic.getName())).append("\",");
        json.append("\"description\":\"").append(escapeJson(epic.getDescription())).append("\",");
        json.append("\"status\":\"").append(epic.getStatus()).append("\",");
        json.append("\"type\":\"").append(epic.getType()).append("\"");
        json.append("}");
        return json.toString();
    }

    private String toJsonArray(List<? extends Epic> epics) {
        StringBuilder json = new StringBuilder();
        json.append("[");
        for (int i = 0; i < epics.size(); i++) {
            if (i > 0) json.append(",");
            json.append(toJson(epics.get(i)));
        }
        json.append("]");
        return json.toString();
    }

    private String toJsonArraySubtasks(List<? extends Subtask> subtasks) {
        StringBuilder json = new StringBuilder();
        json.append("[");
        for (int i = 0; i < subtasks.size(); i++) {
            if (i > 0) json.append(",");
            json.append(toJsonSubtask(subtasks.get(i)));
        }
        json.append("]");
        return json.toString();
    }

    private String toJsonSubtask(Subtask subtask) {
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

    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }
}