package main.javakanban.model;

public class Subtask extends Task {

    private final int epicId;

    public Subtask(String name, String description, Status status, int epicId) {
        super(name, description);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public void setId(int id) {
        if (id != epicId) {
            super.setId(id);
        }
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