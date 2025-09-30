package main.javakanban.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import main.javakanban.exception.NotFoundException;
import main.javakanban.exception.TimeIntervalConflictException;
import main.javakanban.manager.task.TaskManager;
import main.javakanban.model.Task;

import java.io.IOException;
import java.util.List;

public class TaskHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;

    public TaskHandler(TaskManager taskManager) {
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
        if (path.equals("/tasks")) {
            List<Task> tasks = taskManager.getTasks();
            String response = toJsonArray(tasks);
            sendText(exchange, response);
        } else if (path.startsWith("/tasks/")) {
            String idString = path.substring("/tasks/".length());
            int id = parsePathId(idString);
            if (id == -1) {
                sendNotFound(exchange);
                return;
            }
            Task task = taskManager.getTaskByID(id);
            String response = toJson(task);
            sendText(exchange, response);
        } else {
            sendNotFound(exchange);
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        try {
            String body = readText(exchange);

            Task task = parseTaskFromJson(body);

            if (task.getId() == null) {
                Task createdTask = taskManager.addTask(task);
                String response = toJson(createdTask);
                sendCreated(exchange, response);
            } else {
                Task updatedTask = taskManager.updateTask(task);
                String response = toJson(updatedTask);
                sendCreated(exchange, response);
            }
        } catch (Exception e) {
            System.err.println("Ошибка в handlePost: " + e.getMessage());
            e.printStackTrace();
            sendInternalError(exchange);
        }
    }

    private Task parseTaskFromJson(String json) {
        String name = extractField(json, "name");
        String description = extractField(json, "description");
        String statusStr = extractField(json, "status");
        String idStr = extractField(json, "id");

        Task task = new Task(name != null ? name : "", description != null ? description : "");

        if (statusStr != null) {
            task.setStatus(main.javakanban.model.Status.valueOf(statusStr));
        }

        if (idStr != null) {
            task.setId(Integer.parseInt(idStr));
        }

        return task;
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
        if (path.equals("/tasks")) {
            taskManager.deleteTasks();
            sendText(exchange, "{\"message\": \"All tasks deleted\"}");
        } else if (path.startsWith("/tasks/")) {
            String idString = path.substring("/tasks/".length());
            int id = parsePathId(idString);
            if (id == -1) {
                sendNotFound(exchange);
                return;
            }
            taskManager.deleteTaskByID(id);
            sendText(exchange, "{\"message\": \"Task deleted\"}");
        } else {
            sendNotFound(exchange);
        }
    }

    private String toJson(Task task) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"id\":").append(task.getId() != null ? task.getId() : "null").append(",");
        json.append("\"name\":\"").append(escapeJson(task.getName())).append("\",");
        json.append("\"description\":\"").append(escapeJson(task.getDescription())).append("\",");
        json.append("\"status\":\"").append(task.getStatus()).append("\",");
        json.append("\"type\":\"").append(task.getType()).append("\"");
        json.append("}");
        return json.toString();
    }

    private String toJsonArray(List<? extends Task> tasks) {
        StringBuilder json = new StringBuilder();
        json.append("[");
        for (int i = 0; i < tasks.size(); i++) {
            if (i > 0) json.append(",");
            json.append(toJson(tasks.get(i)));
        }
        json.append("]");
        return json.toString();
    }

    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }
}