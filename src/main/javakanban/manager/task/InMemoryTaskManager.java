package main.javakanban.manager.task;

import main.javakanban.exception.TimeIntervalConflictException;
import main.javakanban.exception.NotFoundException;
import main.javakanban.manager.history.HistoryManager;
import main.javakanban.manager.history.InMemoryHistoryManager;
import main.javakanban.model.Epic;
import main.javakanban.model.Status;
import main.javakanban.model.Subtask;
import main.javakanban.model.Task;
import main.javakanban.model.TaskType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public class InMemoryTaskManager implements TaskManager {

    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, Epic> epics = new HashMap<>();
    private final Map<Integer, Subtask> subtasks = new HashMap<>();
    private final HistoryManager historyManager = new InMemoryHistoryManager();
    private final Map<LocalDateTime, Integer> slots;
    private final TreeSet<Task> prioritized = new TreeSet<>(
            Comparator
                    .comparing(Task::getStartTime, Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparing(Task::getId)
    );
    private int id = 1;

    private int getNextId() {
        return id++;
    }

    protected void updateIdCounter(Integer providedId) {
        if (providedId != null && providedId >= id) {
            id = providedId + 1;
        }
    }

    public InMemoryTaskManager() {
        slots = new HashMap<>();
    }

    //создаем тип "Задача"
    @Override
    public Task addTask(Task task) {
        if (task.getId() == null) {
            task.setId(getNextId());
        } else {
            updateIdCounter(task.getId());
        }

        validateTask(task);
        tasks.put(task.getId(), task);
        registerTask(task);
        return task;
    }

    //создаем тип "Эпик"
    @Override
    public Epic addEpic(Epic epic) {
        if (epic.getId() == null) {
            epic.setId(getNextId());
        } else {
            updateIdCounter(epic.getId());
        }
        epics.put(epic.getId(), epic);
        return epic;
    }

    //создаем тип "Подзадача"
    @Override
    public Integer addSubtask(Subtask subtask) {
        int epicId = subtask.getEpicId();
        Epic epic = epics.get(epicId);
        if (epic == null) {
            System.out.println("Эпик не найден. Подзадача не добавлена.");
            return null;
        }

        if (subtask.getId() == null) {
            subtask.setId(getNextId());
        } else {
            updateIdCounter(subtask.getId());
        }

        validateTask(subtask);
        subtasks.put(subtask.getId(), subtask);
        epic.addSubtask(subtask.getId());
        updateEpicStatus(epicId);
        registerTask(subtask);
        return subtask.getId();
    }

    //обновляем тип "Задача"
    @Override
    public Task updateTask(Task task) {
        Integer taskId = task.getId();
        if (taskId == null || !tasks.containsKey(taskId)) {
            return null;
        }
        Task old = tasks.get(taskId);

        validateTask(task);
        removeTaskFromAllStructures(old);
        registerTask(task);

        tasks.replace(taskId, task);
        return task;
    }

    //обновляем тип "Эпик"
    @Override
    public Epic updateEpic(Epic epic) {
        if (epic == null || !epics.containsKey(epic.getId())) {
            return null;
        }
        Epic epic1 = epics.get(epic.getId());
        if (epic1 == null) {
            return null;
        }
        epic1.setName(epic.getName());
        epic1.setDescription(epic.getDescription());
        return epic1;
    }

    //обновляем тип "Подзадача"
    @Override
    public Subtask updateSubtask(Subtask subtask) {
        if (subtask == null || !subtasks.containsKey(subtask.getId())) {
            return null;
        }
        Subtask old = subtasks.get(subtask.getId());

        validateTask(subtask);
        removeTaskFromAllStructures(old);
        registerTask(subtask);

        subtasks.put(subtask.getId(), subtask);
        updateEpicStatus(subtask.getEpicId());
        return subtask;
    }

    @Override
    public Task getTaskByID(int id) {
        Task task = tasks.get(id);
        if (task == null) {
            throw new NotFoundException("Task with id " + id + " not found");
        }
        historyManager.add(task);
        return task;
    }

    @Override
    public Epic getEpicByID(int id) {
        Epic epic = epics.get(id);
        if (epic == null) {
            throw new NotFoundException("Epic with id " + id + " not found");
        }
        historyManager.add(epic);
        return epic;
    }

    @Override
    public Subtask getSubtaskByID(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask == null) {
            throw new NotFoundException("Subtask with id " + id + " not found");
        }
        historyManager.add(subtask);
        return subtask;
    }

    @Override
    public List<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public ArrayList<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public List<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public ArrayList<Subtask> getEpicSubtasks(int id) {
        Epic epic = epics.get(id);
        if (epic == null) {
            throw new NotFoundException("Epic with id " + id + " not found");
        }
        ArrayList<Integer> subtaskId = epic.getSubtaskId();
        return subtaskId.stream()
                .map(subtasks::get)
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
    }

    @Override
    public void deleteTasks() {
        for (Task t : tasks.values()) {
            removeTaskFromAllStructures(t);
        }
        tasks.clear();
    }

    @Override
    public void deleteEpics() {
        epics.clear();
        for (Subtask s : subtasks.values()) {
            removePrioritizedTask(s);
        }
        subtasks.clear();
    }

    @Override
    public void deleteSubtasks() {
        for (Subtask s : subtasks.values()) {
            removeTaskFromAllStructures(s);
        }
        subtasks.clear();
        epics.values().forEach(epic -> {
            epic.clearSubtasks();
            epic.setStatus(Status.NEW);
        });
    }

    @Override
    public Object deleteTaskByID(int id) {
        Task removed = tasks.remove(id);
        removeTaskFromAllStructures(removed);
        return null;
    }

    @Override
    public void deleteEpicByID(int id) {
        epics.get(id).getSubtaskId().forEach(subtaskId -> {
            Subtask removed = subtasks.remove(subtaskId);
            removePrioritizedTask(removed);
        });
        epics.remove(id);
    }

    @Override
    public Object deleteSubtaskByID(int id) {
        int epicId = subtasks.get(id).getEpicId();
        epics.get(epicId).removeEpicSubtask(id);
        Subtask removed = subtasks.remove(id);
        removeTaskFromAllStructures(removed);
        updateEpicStatus(epicId);
        return null;
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    protected void updateEpicStatus(int epicId) {
        ArrayList<Integer> subtaskIdArray = epics.get(epicId).getSubtaskId();
        if (subtaskIdArray.isEmpty()) {
            epics.get(epicId).setStatus(Status.NEW);
            return;
        }
        List<Subtask> subtasksArray = subtaskIdArray.stream().map(subtasks::get).toList();
        boolean allDone = subtasksArray.stream().allMatch(s -> s.getStatus() == Status.DONE);
        boolean allNew = subtasksArray.stream().allMatch(s -> s.getStatus() == Status.NEW);
        if (allDone) {
            epics.get(epicId).setStatus(Status.DONE);
        } else if (allNew) {
            epics.get(epicId).setStatus(Status.NEW);
        } else {
            epics.get(epicId).setStatus(Status.IN_PROGRESS);
        }
    }

    protected Map<Integer, Task> getTasksMap() {
        return tasks;
    }

    protected Map<Integer, Epic> getEpicsMap() {
        return epics;
    }

    protected Map<Integer, Subtask> getSubtasksMap() {
        return subtasks;
    }

    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritized);
    }

    protected TreeSet<Task> getPrioritized() {
        return prioritized;
    }

    private void validateTask(Task task) {
        if (task == null) return;
        if (task.getType() == TaskType.EPIC) return;

        checkTimeIntersection(task);
    }

    private void registerTask(Task task) {
        if (task == null) return;
        if (task.getType() == TaskType.EPIC) return;

        addPrioritizedTask(task);
    }

    private void removeTaskFromAllStructures(Task task) {
        if (task == null) return;

        removePrioritizedTask(task);
    }

    private void checkTimeIntersection(Task task) {
        if (task == null) return;
        if (task.getType() == TaskType.EPIC) return;
        if (task.getStartTime() == null || task.getEndTime() == null) return;

        LocalDateTime start = task.getStartTime();
        LocalDateTime end = task.getEndTime();
        LocalDateTime current = start;

        while (current.isBefore(end)) {
            Integer occupiedByTaskId = slots.get(current);
            if (occupiedByTaskId != null && !occupiedByTaskId.equals(task.getId())) {
                throw new TimeIntervalConflictException("Задача пересекается по времени с существующими задачами");
            }
            current = current.plusMinutes(15);
        }
    }

    protected void addPrioritizedTask(Task task) {
        if (task == null) return;
        if (task.getType() == TaskType.EPIC) return;

        prioritized.add(task);
        if (task.getStartTime() != null && task.getEndTime() != null) {
            addTimeSlots(task.getId(), task.getStartTime(), task.getEndTime());
        }
    }

    private void removePrioritizedTask(Task task) {
        if (task == null) return;
        if (task.getType() == TaskType.EPIC) return;

        prioritized.remove(task);
        if (task.getStartTime() != null && task.getEndTime() != null) {
            clearTimeSlots(task.getStartTime(), task.getEndTime());
        }
    }

    private void addTimeSlots(Integer id, LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) return;

        LocalDateTime current = startTime;
        while (current.isBefore(endTime)) {
            slots.put(current, id);
            current = current.plusMinutes(15);
        }
    }

    private void clearTimeSlots(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) return;

        LocalDateTime current = startTime;
        while (current.isBefore(endTime)) {
            slots.remove(current);
            current = current.plusMinutes(15);
        }
    }
}