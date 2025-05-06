package main.javakanban.model;

import java.util.ArrayList;

public class Epic extends Task {

    private ArrayList<Integer> subtasksId = new ArrayList<>();

    public Epic(String name, String description) {
        super(name, description);
    }

    public Epic(int id, String name, String description, Status status) {
        super(id, name, description, status);
    }

    public void addSubtask(int subtask) {
        subtasksId.add(subtask);
    }

    public void clearSubtasks() {
        subtasksId.clear();
    }

    public ArrayList<Integer> getSubtaskId() {
        return subtasksId;
    }

    public void setSubtaskList(ArrayList<Integer> subtaskId) {
        this.subtasksId = subtaskId;
    }

    public void removeEpicSubtask(Integer idSubtask) {
        subtasksId.remove(idSubtask);
    }

    @Override
    public String toString() {
        return "task.Epic{" +
                "name= " + getName() + '\'' +
                ", description = " + getDescription() + '\'' +
                ", id=" + getId() +
                ", subtaskList.size = " + subtasksId.size() +
                ", status = " + getStatus() +
                '}';
    }
}
