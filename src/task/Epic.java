package task;

import java.util.ArrayList;

public class Epic extends Task {

    private ArrayList<Integer> subtaskId = new ArrayList<>();

    public Epic(String name, String description) {
        super(name, description);
    }

    public Epic(int id, String name, String description, Status status) {
        super(id, name, description, status);
    }

    public void addSubtask(int subtask) {
        subtaskId.add(subtask);
    }

    public void clearSubtasks() {
        subtaskId.clear();
    }

    public ArrayList<Integer> getSubtaskId() {
        return subtaskId;
    }

    public void setSubtaskList(ArrayList<Integer> subtaskId) {
        this.subtaskId = subtaskId;
    }

    public void removeEpicSubtask(Integer idSubtask) {
        subtaskId.remove(idSubtask);
    }

    @Override
    public String toString() {
        return "task.Epic{" +
                "name= " + getName() + '\'' +
                ", description = " + getDescription() + '\'' +
                ", id=" + getId() +
                ", subtaskList.size = " + subtaskId.size() +
                ", status = " + getStatus() +
                '}';
    }
}
