package main.javakanban.manager.task;

import main.javakanban.manager.history.HistoryManager;
import main.javakanban.manager.history.InMemoryHistoryManager;
import main.javakanban.model.Epic;
import main.javakanban.model.Status;
import main.javakanban.model.Subtask;
import main.javakanban.model.Task;

import java.time.LocalDateTime;
import java.time.Month;
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
    private final Map<LocalDateTime, Boolean> slots;
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
        LocalDateTime current = LocalDateTime.of(2024, Month.MAY, 1, 0, 0);
        final LocalDateTime endOfYear = current.plusYears(2); // Cover 2024 and 2025
        while (current.isBefore(endOfYear)) {
            slots.put(current, false);
            current = current.plusMinutes(15);
        }
    }

    //создаем тип "Задача"
    @Override
    public Task addTask(Task task) {
        if (task.getId() == null) {
            task.setId(getNextId());
        } else {
            updateIdCounter(task.getId());
        }

        // Проверка пересечения временных интервалов
        if (task.getStartTime() != null && task.getEndTime() != null &&
                isTimeIntervalBusy(task.getStartTime(), task.getEndTime())) {
            throw new IllegalArgumentException("Задача пересекается по времени с существующими задачами");
        }

        // Бронирование временного интервала
        if (task.getStartTime() != null && task.getEndTime() != null) {
            bookTimeInterval(task.getStartTime(), task.getEndTime());
        }

        tasks.put(task.getId(), task);
        registerInPrioritized(task);
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

        if (subtask.getStartTime() != null && subtask.getEndTime() != null &&
                isTimeIntervalBusy(subtask.getStartTime(), subtask.getEndTime())) {
            throw new IllegalArgumentException("Подзадача пересекается по времени с существующими задачами");
        }

        if (subtask.getStartTime() != null && subtask.getEndTime() != null) {
            bookTimeInterval(subtask.getStartTime(), subtask.getEndTime());
        }

        subtasks.put(subtask.getId(), subtask);
        epic.addSubtask(subtask.getId());
        updateEpicStatus(epicId);
        registerInPrioritized(subtask);
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
        removeFromPrioritized(old);
        tasks.replace(taskId, task);
        registerInPrioritized(task);
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
        removeFromPrioritized(old);
        subtasks.put(subtask.getId(), subtask);
        updateEpicStatus(subtask.getEpicId());
        registerInPrioritized(subtask);
        return subtask;
    }

    @Override
    public Task getTaskByID(int id) {
        Task task = tasks.get(id);
        if (task == null) {
            return task;
        }
        historyManager.add(task);
        return task;
    }

    @Override
    public Epic getEpicByID(int id) {
        Epic epic = epics.get(id);
        if (epic == null) {
            return epic;
        }
        historyManager.add(epic);
        return epic;
    }

    @Override
    public Subtask getSubtaskByID(int id) {

        Subtask subtask = subtasks.get(id);
        if (subtask == null) {
            return subtask;
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
        ArrayList<Integer> subtaskId = epics.get(id).getSubtaskId();
        return subtaskId.stream()
                .map(subtasks::get)
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
    }

    @Override
    public void deleteTasks() {
        for (Task t : tasks.values()) {
            if (t.getStartTime() != null && t.getEndTime() != null) {
                freeTimeInterval(t.getStartTime(), t.getEndTime());
            }
            removeFromPrioritized(t);
        }
        tasks.clear();
    }

    @Override
    public void deleteEpics() {
        epics.clear();
        for (Subtask s : subtasks.values()) {
            removeFromPrioritized(s);
        }
        subtasks.clear();
    }

    @Override
    public void deleteSubtasks() {
        for (Subtask s : subtasks.values()) {
            if (s.getStartTime() != null && s.getEndTime() != null) {
                freeTimeInterval(s.getStartTime(), s.getEndTime());
            }
            removeFromPrioritized(s);
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
        if (removed != null && removed.getStartTime() != null && removed.getEndTime() != null) {
            freeTimeInterval(removed.getStartTime(), removed.getEndTime());
        }
        removeFromPrioritized(removed);
        return null;
    }

    @Override
    public void deleteEpicByID(int id) {
        epics.get(id).getSubtaskId().forEach(subtaskId -> {
            Subtask removed = subtasks.remove(subtaskId);
            removeFromPrioritized(removed);
        });
        epics.remove(id);
    }

    @Override
    public Object deleteSubtaskByID(int id) {
        int epicId = subtasks.get(id).getEpicId();
        epics.get(epicId).removeEpicSubtask(id);
        Subtask removed = subtasks.remove(id);
        if (removed != null && removed.getStartTime() != null && removed.getEndTime() != null) {
            freeTimeInterval(removed.getStartTime(), removed.getEndTime());
        }
        removeFromPrioritized(removed);
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

    protected void registerInPrioritized(Task task) {
        if (task == null) return;
        if (task instanceof Epic) return;
        prioritized.add(task);
    }

    protected void removeFromPrioritized(Task task) {
        if (task == null) return;
        if (task instanceof Epic) return;
        prioritized.remove(task);
    }

    private boolean isTimeIntersection(Task newTask, List<Task> existedTasks) {
        if (newTask.getStartTime() == null || newTask.getEndTime() == null) {
            return false;
        }
        return existedTasks.stream().anyMatch(existedTask -> isTimeIntersection(newTask, existedTask));
    }

    private boolean isTimeIntersection(Task newTask, Task existedTask) {
        return newTask.getStartTime().isBefore(existedTask.getEndTime()) &&
                newTask.getEndTime().isAfter(existedTask.getStartTime());
    }

    protected boolean isTimeIntervalBusy(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return false;
        }
        LocalDateTime current = start;
        while (current.isBefore(end)) {

            if (slots.containsKey(current) && slots.get(current)) {
                return true;
            }
            current = current.plusMinutes(15);
        }
        return false;
    }

    protected void bookTimeInterval(LocalDateTime start, LocalDateTime end) {
        LocalDateTime current = start;
        while (current.isBefore(end)) {
            slots.put(current, true);
            current = current.plusMinutes(15);
        }
    }

    private void freeTimeInterval(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return;
        }
        LocalDateTime current = start;
        while (current.isBefore(end)) {
            slots.put(current, false);
            current = current.plusMinutes(15);
        }
    }
}
