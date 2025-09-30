package main.javakanban.manager.task;

import main.javakanban.exception.ManagerSaveException;
import main.javakanban.model.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import main.javakanban.converter.CsvConverter;

import static main.javakanban.model.TaskType.*;

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
            writer.write("id,type,name,status,description,duration,startTime,epic\n");
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

    public static FileBackedTaskManager loadFromFile(File file) {
        return new FileBackedTaskManager(file);
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

            int maxId = 0;

            for (String line : lines.subList(1, lines.size())) {
                Task parsed = CsvConverter.fromCsv(line);
                switch (parsed.getType()) {
                    case TASK: {
                        Task task = (Task) parsed;
                        getTasksMap().put(task.getId(), task);
                        registerTaskFromFile(task);
                        if (task.getId() != null && task.getId() > maxId) {
                            maxId = task.getId();
                        }
                        break;
                    }
                    case EPIC: {
                        Epic epic = (Epic) parsed;
                        getEpicsMap().put(epic.getId(), epic);
                        if (epic.getId() != null && epic.getId() > maxId) {
                            maxId = epic.getId();
                        }
                        break;
                    }
                    default:
                        break;
                }
            }

            for (String line : lines.subList(1, lines.size())) {
                Task parsed = CsvConverter.fromCsv(line);
                if (parsed.getType() == SUBTASK) {
                    Subtask subtask = (Subtask) parsed;
                    getSubtasksMap().put(subtask.getId(), subtask);
                    registerTaskFromFile(subtask);
                    Epic epic = getEpicsMap().get(subtask.getEpicId());
                    if (epic != null) {
                        epic.addSubtask(subtask.getId());
                    } else {
                        System.out.println("Эпик с id=" + subtask.getEpicId() + " не найден для подзадачи id=" + subtask.getId());
                    }
                    if (subtask.getId() != null && subtask.getId() > maxId) {
                        maxId = subtask.getId();
                    }
                }
            }

            updateIdCounter(maxId);

            for (Epic epic : getEpics()) {
                if (epic.getId() != null) {
                    updateEpicStatus(epic.getId());
                }
            }
        } catch (IOException e) {
            System.out.println("Ошибка при загрузке данных: " + e.getMessage());
        }
    }

    private void registerTaskFromFile(Task task) {
        if (task == null) return;
        if (task.getType() == TaskType.EPIC) return;
        addPrioritizedTask(task);
    }

    public static void main(String[] args) {
        File file = new File("tasks.csv");

        if (file.exists()) {
            file.delete();
        }

        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        Task task1 = new Task(null, "Первое задание", "Описание первого задания", Status.NEW);
        task1.setDuration(Duration.ofMinutes(90));
        task1.setStartTime(LocalDateTime.of(2025, 9, 25, 10, 0));
        manager.addTask(task1);

        Epic epic1 = new Epic(null, "Первый эпик", "Описание первого эпика", Status.NEW);
        manager.addEpic(epic1);

        Subtask subtask1 = new Subtask("Первая подзадача", "Описание первой подзадачи", Status.NEW, epic1.getId());
        subtask1.setDuration(Duration.ofMinutes(30));
        subtask1.setStartTime(LocalDateTime.of(2025, 9, 26, 11, 0));
        manager.addSubtask(subtask1);

        System.out.println("Список задач:");
        System.out.println(manager.getTasks());
        System.out.println(manager.getEpics());
        System.out.println(manager.getSubtasks());
    }
}
