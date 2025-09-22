package main.javakanban.converter;

import main.javakanban.model.*;

public class CsvConverter {

    public static String toCsv(Task task) {
        if (task instanceof Subtask) {
            return toCsv((Subtask) task);
        } else if (task instanceof Epic) {
            return toCsv((Epic) task);
        }
        return toCsvDetails(task);
    }

    private static String toCsvDetails(Task task) {
        return String.format("%d,%s,%s,%s,%s,%s",
                task.getId(),
                task.getType(),
                task.getName(),
                task.getStatus().name(),
                task.getDescription(),
                "");
    }

    public static String toCsv(Epic epic) {
        return String.format("%d,%s,%s,%s,%s,%s",
                epic.getId(),
                epic.getType(),
                epic.getName(),
                epic.getStatus().name(),
                epic.getDescription(),
                "");
    }

    public static String toCsv(Subtask subtask) {
        return String.format("%d,%s,%s,%s,%s,%s",
                subtask.getId(),
                subtask.getType(),
                subtask.getName(),
                subtask.getStatus().name(),
                subtask.getDescription(),
                subtask.getEpicId());
    }

    public static Task fromCsv(String line) {
        String[] parts = line.split(",");

        if (parts.length < 5) {
            throw new IllegalArgumentException("Недостаточно данных в строке: " + line);
        }

        int id = Integer.parseInt(parts[0]);
        TaskType type = TaskType.valueOf(parts[1]);
        String name = parts[2];
        Status status = Status.valueOf(parts[3]);
        String description = parts[4];
        Integer epicId = parts.length > 5 && !parts[5].isEmpty() ? Integer.parseInt(parts[5]) : null;

        switch (type) {
            case TASK:
                return new Task(id, name, description, status);
            case EPIC:
                return new Epic(id, name, description, status);
            case SUBTASK:
                if (epicId == null) {
                    throw new IllegalArgumentException("Недостаточно данных для подзадачи: " + line);
                }
                Subtask subtask = new Subtask(name, description, status, epicId);
                subtask.setId(id);
                return subtask;
            default:
                throw new IllegalArgumentException("Неизвестный тип задачи: " + type);
        }
    }
}