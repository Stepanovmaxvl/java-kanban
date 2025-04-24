import java.util.*;

public class TaskManager {
    private final Map<Integer, Task> tasksMap = new HashMap<>();
    private final Map<Integer, Epic> epicsMap = new HashMap<>();
    private final Map<Integer, Subtask> subtasksMap = new HashMap<>();

    private int countID = 1;

    private int generateId() {
        return countID++;
    }

    //создаем тип "Задача"
    public Task addTask(Task task) {
        task.setId(generateId());
        tasksMap.put(task.getId(), task);
        return task;
    }

    //создаем тип "Эпик"
    public Epic addEpic(Epic epic) {
        epic.setId(generateId());
        epicsMap.put(epic.getId(), epic);
        return epic;
    }

    //создаем тип "Подзадача"
    public Subtask addSubtask(Subtask subtask) {
        subtask.setId(generateId());
        Epic epic = epicsMap.get(subtask.getEpicID());
        epic.addSubtask(subtask);
        subtasksMap.put(subtask.getId(), subtask);
        return subtask;
    }

    //обновляем тип "Задача"
    public Task updateTask(Task task) {
        Integer taskID = task.getId();
        if (taskID == null || !tasksMap.containsKey(taskID)) {
            return null;
        }
        tasksMap.replace(taskID, task);
        return task;
    }

    //обновляем тип "Эпик"
    public Epic updateEpic(Epic epic) {
        Integer epicID = epic.getId();
        if (epicID == null || !epicsMap.containsKey(epicID)) {
            return null;
        }

        Epic oldEpic = epicsMap.get(epicID);
        ArrayList<Subtask> oldEpicSubtaskList = oldEpic.getSubtaskList();
        if (!oldEpicSubtaskList.isEmpty()) {
            for (Subtask subtask : oldEpicSubtaskList) {
                subtasksMap.remove(subtask.getId());
            }
        }
        epicsMap.replace(epicID, epic);

        ArrayList<Subtask> newEpicSubtaskList = epic.getSubtaskList();
        if (!newEpicSubtaskList.isEmpty()) {
            for (Subtask subtask : newEpicSubtaskList) {
                subtasksMap.put(subtask.getId(), subtask);
            }
        }
        return epic;
    }

    //обновляем тип "Подзадача"
    public Subtask updateSubtask(Subtask subtask) {
        Integer subtaskID = subtask.getId();
        if (subtaskID == null || !subtasksMap.containsKey(subtaskID)) {
            return null;
        }
        int epicID = subtask.getEpicID();
        Subtask oldSubtask = subtasksMap.get(subtaskID);
        subtasksMap.replace(subtaskID, subtask);

        Epic epic = epicsMap.get(epicID);
        ArrayList<Subtask> subtaskList = epic.getSubtaskList();
        subtaskList.remove(oldSubtask);
        subtaskList.add(subtask);
        epic.setSubtaskList(subtaskList);
        return subtask;
    }

    public Task getTaskByID(int id) {
        return tasksMap.get(id);
    }

    public Epic getEpicByID(int id) {
        return epicsMap.get(id);
    }

    public Subtask getSubtaskByID(int id) {
        return subtasksMap.get(id);
    }

    public ArrayList<Task> getTasks() {
        return new ArrayList<>(tasksMap.values());
    }

    public ArrayList<Epic> getEpics() {
        return new ArrayList<>(epicsMap.values());
    }

    public ArrayList<Subtask> getSubtasks() {
        return new ArrayList<>(subtasksMap.values());
    }

    public ArrayList<Subtask> getEpicSubtasks(Epic epic) {
        return epic.getSubtaskList();
    }

    public void deleteTasks() {
        tasksMap.clear();
    }

    public void deleteEpics() {
        epicsMap.clear();
        subtasksMap.clear();
    }

    public void deleteSubtasks() {
        subtasksMap.clear();
        for (Epic epic : epicsMap.values()) {
            epic.clearSubtasks();
            epic.setStatus(Status.NEW);
        }
    }

    public void deleteTaskByID(int id) {
        tasksMap.remove(id);
    }

    public void deleteEpicByID(int id) {
        ArrayList<Subtask> epicSubtasks = epicsMap.get(id).getSubtaskList();
        epicsMap.remove(id);
        for (Subtask subtask : epicSubtasks) {
            subtasksMap.remove(subtask.getId());
        }
    }

    public void deleteSubtaskByID(int id) {
        Subtask subtask = subtasksMap.get(id);
        int epicID = subtask.getEpicID();
        subtasksMap.remove(id);

        Epic epic = epicsMap.get(epicID);
        ArrayList<Subtask> subtaskList = epic.getSubtaskList();
        subtaskList.remove(subtask);
        epic.setSubtaskList(subtaskList);
    }
}
