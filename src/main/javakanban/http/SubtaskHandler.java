package main.javakanban.http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import main.javakanban.exception.NotFoundException;
import main.javakanban.exception.TimeIntervalConflictException;
import main.javakanban.manager.task.TaskManager;
import main.javakanban.model.Subtask;

import java.io.IOException;
import java.util.List;

public class SubtaskHandler extends BaseHttpHandler implements HttpHandler {

    public SubtaskHandler(TaskManager taskManager, Gson gson) {
        super(taskManager, gson);
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
            String response = gson.toJson(subtasks);
            sendText(exchange, response);
        } else if (path.startsWith("/subtasks/")) {
            String idString = path.substring("/subtasks/".length());
            int id = parsePathId(idString);
            if (id == -1) {
                sendNotFound(exchange);
                return;
            }
            Subtask subtask = taskManager.getSubtaskByID(id);
            String response = gson.toJson(subtask);
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
                String response = gson.toJson(subtask);
                sendCreated(exchange, response);
            } else {
                Subtask updatedSubtask = taskManager.updateSubtask(subtask);
                String response = gson.toJson(updatedSubtask);
                sendCreated(exchange, response);
            }
        } catch (Exception e) {
            System.err.println("Ошибка в handlePost: " + e.getMessage());
            e.printStackTrace();
            sendInternalError(exchange);
        }
    }

    private Subtask parseSubtaskFromJson(String json) {
        return gson.fromJson(json, Subtask.class);
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

}