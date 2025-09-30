package main.javakanban.http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import main.javakanban.manager.task.TaskManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public abstract class BaseHttpHandler {
    protected final TaskManager taskManager;
    protected final Gson gson;

    public BaseHttpHandler(TaskManager taskManager, Gson gson) {
        this.taskManager = taskManager;
        this.gson = gson;
    }

    protected void sendResponse(HttpExchange exchange, String text, int statusCode) throws IOException {
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        exchange.sendResponseHeaders(statusCode, resp.length);
        exchange.getResponseBody().write(resp);
        exchange.close();
    }

    protected void sendText(HttpExchange exchange, String text) throws IOException {
        sendResponse(exchange, text, 200);
    }

    protected void sendCreated(HttpExchange exchange, String text) throws IOException {
        sendResponse(exchange, text, 201);
    }

    protected void sendNotFound(HttpExchange exchange) throws IOException {
        sendResponse(exchange, "{\"error\": \"Not Found\"}", 404);
    }

    protected void sendMethodNotAllowed(HttpExchange exchange) throws IOException {
        sendResponse(exchange, "{\"error\": \"Method Not Allowed\"}", 405);
    }

    protected void sendHasInteractions(HttpExchange exchange) throws IOException {
        sendResponse(exchange, "{\"error\": \"Task overlaps with existing ones\"}", 406);
    }

    protected void sendInternalError(HttpExchange exchange) throws IOException {
        sendResponse(exchange, "{\"error\": \"Internal Server Error\"}", 500);
    }

    protected String readText(HttpExchange exchange) throws IOException {
        return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    protected int parsePathId(String path) {
        try {
            return Integer.parseInt(path);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    protected int extractIdFromPath(String path, String prefix) {
        return Integer.parseInt(path.replaceFirst(prefix, ""));
    }

    protected void sendBadRequest(HttpExchange exchange) throws IOException {
        sendResponse(exchange, "{\"error\": \"Bad Request\"}", 400);
    }

    protected void sendOk(HttpExchange exchange) throws IOException {
        sendResponse(exchange, "{\"message\": \"OK\"}", 200);
    }
}