package main.javakanban.converter;

import main.javakanban.model.*;

import java.time.Duration;
import java.time.LocalDateTime;

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
        return String.format("%d,%s,%s,%s,%s,%s,%s,%s",
                task.getId(),
                task.getType(),
                task.getName(),
                task.getStatus().name(),
                task.getDescription(),
                (task.getDuration() == null ? "" : task.getDuration().toMinutes()),
                (task.getStartTime() == null ? "" : task.getStartTime()),
                "");
    }

    public static String toCsv(Epic epic) {
        return String.format("%d,%s,%s,%s,%s,%s,%s,%s",
                epic.getId(),
                epic.getType(),
                epic.getName(),
                epic.getStatus().name(),
                epic.getDescription(),
                (epic.getDuration() == null ? "" : epic.getDuration().toMinutes()),
                (epic.getStartTime() == null ? "" : epic.getStartTime()),
                "");
    }

    public static String toCsv(Subtask subtask) {
        return String.format("%d,%s,%s,%s,%s,%s,%s,%s",
                subtask.getId(),
                subtask.getType(),
                subtask.getName(),
                subtask.getStatus().name(),
                subtask.getDescription(),
                (subtask.getDuration() == null ? "" : subtask.getDuration().toMinutes()),
                (subtask.getStartTime() == null ? "" : subtask.getStartTime()),
                subtask.getEpicId());
    }

    public static Task fromCsv(String line) {
        String[] parts = line.split(",", -1);

        if (parts.length < 5) {
            throw new IllegalArgumentException("Недостаточно данных в строке: " + line);
        }

        int id = Integer.parseInt(parts[0]);
        TaskType type = TaskType.valueOf(parts[1]);
        String name = parts[2];
        Status status = Status.valueOf(parts[3]);
        String description = parts[4];
        Duration duration = null;
        LocalDateTime startTime = null;
        Integer epicId = null;

        if (parts.length >= 8) {
            duration = (!parts[5].isEmpty() && !parts[5].equals("null"))
                    ? Duration.ofMinutes(Long.parseLong(parts[5]))
                    : null;
            startTime = (!parts[6].isEmpty() && !parts[6].equals("null"))
                    ? LocalDateTime.parse(parts[6])
                    : null;
            epicId = (!parts[7].isEmpty()) ? Integer.parseInt(parts[7]) : null;
        } else if (parts.length == 7) {
            // Heuristic: for SUBTASK legacy format, column 5 is epicId; otherwise 5/6 may be duration/start
            if (type == TaskType.SUBTASK && !parts[5].isEmpty()) {
                try {
                    epicId = Integer.parseInt(parts[5]);
                } catch (NumberFormatException ignored) {
                    // Not an epic id; try treat as duration/start
                    duration = (!parts[5].isEmpty() && !parts[5].equals("null"))
                            ? Duration.ofMinutes(Long.parseLong(parts[5]))
                            : null;
                    startTime = (!parts[6].isEmpty() && !parts[6].equals("null"))
                            ? LocalDateTime.parse(parts[6])
                            : null;
                }
            } else {
                duration = (!parts[5].isEmpty() && !parts[5].equals("null"))
                        ? Duration.ofMinutes(Long.parseLong(parts[5]))
                        : null;
                startTime = (!parts[6].isEmpty() && !parts[6].equals("null"))
                        ? LocalDateTime.parse(parts[6])
                        : null;
            }
        } else if (parts.length == 6) {
            // Oldest format: 6th column is epicId for subtasks; duration only for tasks/epics
            if (type == TaskType.SUBTASK && !parts[5].isEmpty() && !parts[5].equals("null")) {
                epicId = Integer.parseInt(parts[5]);
            } else {
                duration = (!parts[5].isEmpty() && !parts[5].equals("null"))
                        ? Duration.ofMinutes(Long.parseLong(parts[5]))
                        : null;
            }
        }

        switch (type) {
            case TASK:
                Task task = new Task(id, name, description, status);
                task.setDuration(duration);
                task.setStartTime(startTime);
                return task;
            case EPIC:
                Epic epic = new Epic(id, name, description, status);
                epic.setDuration(duration);
                epic.setStartTime(startTime);
                return epic;
            case SUBTASK:
                if (epicId == null) {
                    throw new IllegalArgumentException("Недостаточно данных для подзадачи: " + line);
                }
                Subtask subtask = new Subtask(name, description, status, epicId);
                subtask.setId(id);
                subtask.setStatus(status);
                subtask.setDuration(duration);
                subtask.setStartTime(startTime);
                return subtask;
            default:
                throw new IllegalArgumentException("Неизвестный тип задачи: " + type);
        }
    }
}