package main.javakanban.http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpServer;
import main.javakanban.manager.task.TaskManager;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpTaskServer {
    private static final int PORT = 8080;
    private final HttpServer server;
    private final TaskManager taskManager;

    public HttpTaskServer(TaskManager taskManager) throws IOException {
        this.taskManager = taskManager;
        this.server = HttpServer.create(new InetSocketAddress(PORT), 0);
        
        Gson gson = GsonConfig.createGson();

        server.createContext("/tasks", new TaskHandler(taskManager, gson));
        server.createContext("/subtasks", new SubtaskHandler(taskManager, gson));
        server.createContext("/epics", new EpicHandler(taskManager, gson));
        server.createContext("/history", new HistoryHandler(taskManager, gson));
        server.createContext("/prioritized", new PrioritizedHandler(taskManager, gson));
    }

    public void start() {
        server.start();
        System.out.println("HTTP Task Server запущен на порту " + PORT);
    }

    public void stop() {
        server.stop(0);
        System.out.println("HTTP Task Server остановлен");
    }

    public static void main(String[] args) {
        try {
            HttpTaskServer server = new HttpTaskServer(main.javakanban.manager.Managers.getDefault());

            server.start();

            Runtime.getRuntime().addShutdownHook(new Thread(server::stop));

            Thread.currentThread().join();
        } catch (IOException | InterruptedException e) {
            System.err.println("Ошибка запуска сервера: " + e.getMessage());
            e.printStackTrace();
        }
    }
}