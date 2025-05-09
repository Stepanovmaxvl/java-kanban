package main.javakanban.model;

public class Subtask extends Task {

    private final int epicId;

    public Subtask(String name, String description, Status status, int epicID) {
        super(name, description);
        this.epicId = epicID;
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public String toString() {
        return "task.Subtask{" +
                "name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", id=" + getId() +
                ", epicID=" + epicId +
                ", status=" + getStatus() +
                '}';
    }
}