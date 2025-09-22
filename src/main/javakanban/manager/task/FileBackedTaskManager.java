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

import main.javakanban.converter.CsvConverter;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;

    public FileBackedTaskManager(File file) {
        super();
        this.file = file;
        loadFromFile();
    }

    @Override
    public Integer addSubtask(Subtask subtask) {
        Integer newSubtaskId = super.addSubtask(subtask);
        save();
        return newSubtaskId;
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

    @Override
    public Integer deleteSubtaskByID(int id) {
        super.deleteSubtaskByID(id);
        save();
        return null;
    }

    @Override
    public Integer deleteTaskByID(int id) {
        super.deleteTaskByID(id);
        save();
        return null;
    }

    @Override
    public void deleteEpicByID(int id) {
        super.deleteEpicByID(id);
        save();
    }

    private void save() {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("id,type,name,status,description,epic\n");
            for (Task task : getTasks()) {
                writer.write(CsvConverter.toCsv(task) + "\n");
            }
            for (Epic epic : getEpics()) {
                writer.write(CsvConverter.toCsv(epic) + "\n");
            }
            for (Subtask subtask : getSubtasks()) {
                writer.write(CsvConverter.toCsv(subtask) + "\n");
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при сохранении данных", e);
        }
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
                Task task = CsvConverter.fromCsv(line);
                if (task instanceof Epic) {
                    addEpic((Epic) task);
                } else if (task instanceof Subtask) {
                    addSubtask((Subtask) task);
                } else {
                    addTask(task);
                }
            }
        } catch (IOException e) {
            System.out.println("Ошибка при загрузке данных: " + e.getMessage());
        }
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

        Epic epic1 = new Epic(0, "Первый эпик", "Описание первого эпика", Status.NEW); // Передаем null
        manager.addEpic(epic1);

        Subtask subtask1 = new Subtask("Первая подзадача", "Описание первой подзадачи", Status.NEW, epic1.getId()); // Передаем null
        manager.addSubtask(subtask1);

        System.out.println("Список задач:");
        System.out.println(manager.getTasks());
        System.out.println(manager.getEpics());
        System.out.println(manager.getSubtasks());
    }
}