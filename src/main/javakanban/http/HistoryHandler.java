package main.javakanban.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import main.javakanban.exception.NotFoundException;
import main.javakanban.manager.task.TaskManager;
import main.javakanban.model.Task;

import java.io.IOException;
import java.util.List;

public class HistoryHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;

    public HistoryHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();

            if ("GET".equals(method)) {
                List<Task> history = taskManager.getHistory();
                String response = toJsonArray(history);
                sendText(exchange, response);
            } else {
                sendNotFound(exchange);
            }
        } catch (NotFoundException e) {
            sendNotFound(exchange);
        } catch (Exception e) {
            sendInternalError(exchange);
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