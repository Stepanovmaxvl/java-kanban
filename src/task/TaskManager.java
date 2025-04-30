package task;

import java.util.*;

public class TaskManager {

    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, Epic> epics = new HashMap<>();
    private final Map<Integer, Subtask> subtasks = new HashMap<>();

    private int nextID = 1;

    private int getNextID() {
        return nextID++;
    }

    //создаем тип "Задача"
    public Task addTask(Task task) {
        task.setId(getNextID());
        tasks.put(task.getId(), task);
        return task;
    }

    //создаем тип "Эпик"
    public Epic addEpic(Epic epic) {
        epic.setId(getNextID());
        epics.put(epic.getId(), epic);
        return epic;
    }

    //создаем тип "Подзадача"
    public Subtask addSubtask(Subtask subtask) {
        if (subtask != null) {
            int subtaskId = getNextID();
            subtask.setId(subtaskId);
            subtasks.put(subtaskId, subtask);

            int epicId = subtask.getEpicID();
            epics.get(epicId).addSubtask(subtaskId);
            updateEpicStatus(epicId);

        }
        return subtask;
    }

    //обновляем тип "Задача"
    public Task updateTask(Task task) {
        Integer taskID = task.getId();
        if (taskID == null || !tasks.containsKey(taskID)) {
            return null;
        }
        tasks.replace(taskID, task);
        return task;
    }

    //обновляем тип "Эпик"
    public void updateEpic(Epic epic) {

        if (epic != null && epics.containsKey(epic.getId())) {

            Epic epic1 = epics.get(epic.getId());
            if (epic1 == null) {
                return;
            }
            epic1.setName(epic.getName());
            epic1.setDescription(epic.getDescription());
        }
    }

    //обновляем тип "Подзадача"
    public void updateSubtask(Subtask subtask) {
        if (subtask != null && subtasks.containsKey(subtask.getId())) {
            subtasks.put(subtask.getId(), subtask);

            updateEpicStatus(subtask.getEpicID());
        }
    }

    public Task getTaskByID(int id) {
        return tasks.get(id);
    }

    public Epic getEpicByID(int id) {
        return epics.get(id);
    }

    public Subtask getSubtaskByID(int id) {
        return subtasks.get(id);
    }

    public ArrayList<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    public ArrayList<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    public Subtask getSubtasks(int id) {
        return subtasks.get(id);
    }

    public Epic getEpicSubtasks(int id) {
        return epics.get(id);
    }

    public void deleteTasks() {
        tasks.clear();
    }

    public void deleteEpics() {
        epics.clear();
        subtasks.clear();
    }

    public void deleteSubtasks() {
        subtasks.clear();
        for (Epic epic : epics.values()) {
            epic.clearSubtasks();
            epic.setStatus(Status.NEW);
        }
    }

    public void deleteTaskByID(int id) {
        tasks.remove(id);
    }

    public void deleteEpicByID(int id) {
        for (Integer subtaskId : epics.get(id).getSubtaskId()) {
            subtasks.remove(subtaskId);
        }
        epics.remove(id);
    }

    public void deleteSubtaskByID(int id) {
        int epicId = subtasks.get(id).getEpicID();
        epics.get(epicId).removeEpicSubtask(id);
        subtasks.remove(id);
        updateEpicStatus(epicId);
    }

    //метод для контроля статуса эпика
    private void updateEpicStatus(int epicId) {
        ArrayList<Integer> subtaskIdArray = epics.get(epicId).getSubtaskId();
        ArrayList<Subtask> subtasksArray = new ArrayList<>();
        for (int id : subtaskIdArray) {
            subtasksArray.add(subtasks.get(id));
        }
        boolean selectorDone = true;
        boolean selectorNew = true;


        if (subtaskIdArray.isEmpty()) {
            epics.get(epicId).setStatus(Status.NEW);
        } else {
            for (Subtask subtask : subtasksArray) {
                if (!subtask.getStatus().equals(Status.DONE)) {
                    selectorDone = false;
                }
                if (!subtask.getStatus().equals(Status.NEW)) {
                    selectorNew = false;
                }
                if (!selectorNew && !selectorDone) {
                    break;
                }

            }
            if (selectorDone) {
                epics.get(epicId).setStatus(Status.DONE);
            } else if (selectorNew) {
                epics.get(epicId).setStatus(Status.NEW);
            } else {
                epics.get(epicId).setStatus(Status.IN_PROGRESS);
            }
        }
    }
}