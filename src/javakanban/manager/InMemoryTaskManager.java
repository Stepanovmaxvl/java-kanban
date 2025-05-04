package javakanban.manager;

import javakanban.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryTaskManager implements TaskManager {

    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, Epic> epics = new HashMap<>();
    private final Map<Integer, Subtask> subtasks = new HashMap<>();
    private final HistoryManager historyManager = Managers.getDefaultHistory();

    private int id = 1;

    public int getNextId() {
        return id++;
    }

    //создаем тип "Задача"
    @Override
    public Task addTask(Task task) {
        task.setId(getNextId());
        tasks.put(task.getId(), task);
        return task;
    }

    //создаем тип "Эпик"
    @Override
    public Epic addEpic(Epic epic) {
        epic.setId(getNextId());
        epics.put(epic.getId(), epic);
        return epic;
    }

    //создаем тип "Подзадача"
    @Override
    public Subtask addSubtask(Subtask subtask) {
        if (subtask == null) {
            return subtask;
        }

        int epicId = subtask.getEpicId();
        Epic epic = epics.get(epicId);

        // Проверяем наличие эпика и если его нет - выходим из метода
        if (epic == null) {
            System.out.println("Эпик не найден. Подзадача не добавлена.");
            return subtask;
        }

        // Если эпик найден, получаем id подзадачи и устанавливаем его
        int subtaskId = getNextId();
        subtask.setId(subtaskId);
        subtasks.put(subtaskId, subtask);

        // Добавляем подзадачу в эпик и обновляем статус эпика
        epic.addSubtask(subtaskId);
        updateEpicStatus(epicId);
        return subtask;
    }


    //обновляем тип "Задача"
    @Override
    public Task updateTask(Task task) {
        Integer taskId = task.getId();
        if (taskId == null || !tasks.containsKey(taskId)) {
            return null;
        }
        tasks.replace(taskId, task);
        return task;
    }

    //обновляем тип "Эпик"
    @Override
    public Epic updateEpic(Epic epic) {
        if (epic == null && !epics.containsKey(epic.getId())) {
            return epic;
        }
        Epic epic1 = epics.get(epic.getId());
        if (epic1 == null) {
            return epic;
        }
        epic1.setName(epic.getName());
        epic1.setDescription(epic.getDescription());
        return epic;
    }

    //обновляем тип "Подзадача"
    @Override
    public Subtask updateSubtask(Subtask subtask) {
        if (subtask == null && !subtasks.containsKey(subtask.getId())) {
            return subtask;
        }
        subtasks.put(subtask.getId(), subtask);
        updateEpicStatus(subtask.getEpicId());
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
        ArrayList<Subtask> subtasksArray = new ArrayList<>();
        for (int idEpic : subtaskId) {
            subtasksArray.add(subtasks.get(id));
        }
        return subtasksArray;
    }

    @Override
    public void deleteTasks() {
        tasks.clear();
    }

    @Override
    public void deleteEpics() {
        epics.clear();
        subtasks.clear();
    }

    @Override
    public void deleteSubtasks() {
        subtasks.clear();
        for (Epic epic : epics.values()) {
            epic.clearSubtasks();
            epic.setStatus(Status.NEW);
        }
    }

    @Override
    public Object deleteTaskByID(int id) {
        tasks.remove(id);
        return null;
    }

    @Override
    public void deleteEpicByID(int id) {
        for (Integer subtaskId : epics.get(id).getSubtaskId()) {
            subtasks.remove(subtaskId);
        }
        epics.remove(id);
    }

    @Override
    public Object deleteSubtaskByID(int id) {
        int epicId = subtasks.get(id).getEpicId();
        epics.get(epicId).removeEpicSubtask(id);
        subtasks.remove(id);
        updateEpicStatus(epicId);
        return null;
    }


    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
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
