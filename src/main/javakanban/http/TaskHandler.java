package main.javakanban.http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import main.javakanban.exception.NotFoundException;
import main.javakanban.exception.TimeIntervalConflictException;
import main.javakanban.manager.task.TaskManager;
import main.javakanban.model.Task;

import java.io.IOException;
import java.util.List;

public class TaskHandler extends BaseHttpHandler implements HttpHandler {

    public TaskHandler(TaskManager taskManager, Gson gson) {
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
        if (path.equals("/tasks")) {
            List<Task> tasks = taskManager.getTasks();
            String response = gson.toJson(tasks);
            sendText(exchange, response);
        } else if (path.startsWith("/tasks/")) {
            String idString = path.substring("/tasks/".length());
            int id = parsePathId(idString);
            if (id == -1) {
                sendNotFound(exchange);
                return;
            }
            Task task = taskManager.getTaskByID(id);
            String response = gson.toJson(task);
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
                String response = gson.toJson(createdTask);
                sendCreated(exchange, response);
            } else {
                Task updatedTask = taskManager.updateTask(task);
                String response = gson.toJson(updatedTask);
                sendCreated(exchange, response);
            }
        } catch (Exception e) {
            System.err.println("Ошибка в handlePost: " + e.getMessage());
            e.printStackTrace();
            sendInternalError(exchange);
        }
    }

    private Task parseTaskFromJson(String json) {
        return gson.fromJson(json, Task.class);
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

}