package main.javakanban.manager.task;

import main.javakanban.model.Epic;
import main.javakanban.model.Status;
import main.javakanban.model.Subtask;
import main.javakanban.model.Task;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;

    public FileBackedTaskManager(File file) {
        super();
        this.file = file;
        loadFromFile();
    }

    @Override
    public Subtask addSubtask(Subtask subtask) {
        super.addSubtask(subtask);
        save();
        return subtask;
    }

    @Override
    public Epic addEpic(Epic epic) {
        super.addEpic(epic);
        save();
        return epic;
    }

    @Override
    public Task addTask(Task task) {
        super.addTask(task);
        save();
        return task;
    }

    @Override
    public Task updateTask(Task task) {
        super.updateTask(task);
        save();
        return task;
    }

    @Override
    public Epic updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
        return epic;
    }

    @Override
    public Subtask updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
        return subtask;
    }

    public void save() {
        try (FileWriter writer = new FileWriter(file)) {
            // Записываем заголовки
            writer.write("id,type,name,status,description,epic\n");
            for (Task task : getTasks()) {
                writer.write(toCsv(task) + "\n");
            }
            for (Epic epic : getEpics()) {
                writer.write(toCsv(epic) + "\n");
            }
            for (Subtask subtask : getSubtasks()) {
                writer.write(toCsv(subtask) + "\n");
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при сохранении данных", e);
        }
    }

    private String toCsv(Task task) {
        return String.format("%d,%s,%s,%s,%s,%s",
                task.getId(),
                task.getType(),
                task.getName(),
                task.getStatus().name(),
                task.getDescription(),
                task instanceof Subtask ? ((Subtask) task).getEpicId() : "");
    }

    public void initialize() {
        loadFromFile();
    }

    private void loadFromFile() {
        try {
            if (!file.exists() || file.length() == 0) {
                System.out.println("Файл не существует или пуст: " + file.getAbsolutePath());
                return;
            }

            List<String> lines = Files.readAllLines(Paths.get(file.getAbsolutePath()));
            if (lines.size() < 2) {
                System.out.println("Файл пуст или содержит только заголовок: " + file.getAbsolutePath());
                return;
            }

            for (String line : lines.subList(1, lines.size())) {
                fromCsv(line);
            }
        } catch (IOException e) {
            System.out.println("Ошибка при загрузке данных: " + e.getMessage());
        }
    }

    private void fromCsv(String line) {
        String[] parts = line.split(",");

        if (parts.length < 5) {
            System.out.println("Недостаточно данных в строке: " + line);
            return;
        }

        int id = Integer.parseInt(parts[0]);
        String type = parts[1];
        String name = parts[2];
        String status = parts[3];
        String description = parts[4];
        Integer epic = parts.length > 5 && !parts[5].isEmpty() ? Integer.parseInt(parts[5]) : null;

        switch (type) {
            case "TASK":
                Task task = new Task(id, name, description, Status.valueOf(status));
                addTask(task);
                break;
            case "EPIC":
                Epic epicObj = new Epic(id, name, description, Status.valueOf(status));
                addEpic(epicObj);
                break;
            case "SUBTASK":
                if (epic == null) {
                    System.out.println("Недостаточно данных для подзадачи: " + line);
                    return;
                }
        }
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        return new FileBackedTaskManager(file);
    }

    public class ManagerSaveException extends RuntimeException {
        public ManagerSaveException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static void main(String[] args) {

        File file = new File("tasks.csv");

        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        Task task1 = new Task(null, "Первое задание", "Описание первого задания", Status.NEW);
        manager.addTask(task1);

        Epic epic1 = new Epic(0, "Первый эпик", "Описание первого эпика", Status.NEW);
        manager.addEpic(epic1);

        Subtask subtask1 = new Subtask("Первая подзадача", "Описание первой подзадачи", Status.NEW, epic1.getId());
        manager.addSubtask(subtask1);

        System.out.println("Список задач:");
        System.out.println(manager.getTasks());
        System.out.println(manager.getEpics());
        System.out.println(manager.getSubtasks());
    }
}