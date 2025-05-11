package test.javakanban.model;

import main.javakanban.manager.task.InMemoryTaskManager;
import main.javakanban.model.Epic;
import main.javakanban.model.Subtask;
import org.junit.jupiter.api.Test;
import main.javakanban.model.Status;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {

    @Test
    public void equals_returnTrue_idAreSame() {
        Epic epic1 = new Epic(10, "Эпик 1", "Сделать Эпик 1", Status.NEW);
        Epic epic2 = new Epic(10, "Эпик 2", "Сделать Эпик 2",
                Status.IN_PROGRESS);
        assertTrue(epic1.equals(epic2));
    }

    @Test
    public void addSubtask_notAddSubtask_subtaskIdSameWithEpicId() {
        Epic epic = new Epic(1, "Эпик", "Описание эпика", Status.NEW);
        int subtaskId = epic.getId();
        epic.addSubtask(subtaskId);
        assertTrue(epic.getSubtaskId().isEmpty(), "Сабтаск с тем же ID, что и у эпика, не должен добавляться");
    }
}