package model;

import main.javakanban.manager.task.InMemoryTaskManager;
import main.javakanban.manager.task.TaskManager;
import main.javakanban.model.Epic;
import main.javakanban.model.Status;
import main.javakanban.model.Subtask;
import org.junit.jupiter.api.Test;

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

    @Test
    public void status_allSubtasksNew_epicIsNew() {
        TaskManager manager = new InMemoryTaskManager();
        Epic epic = manager.addEpic(new Epic(0, "Эпик", "Описание", Status.NEW));
        manager.addSubtask(new Subtask("S1", "", Status.NEW, epic.getId()));
        manager.addSubtask(new Subtask("S2", "", Status.NEW, epic.getId()));
        assertEquals(Status.NEW, manager.getEpicByID(epic.getId()).getStatus());
    }

    @Test
    public void status_allSubtasksDone_epicIsDone() {
        TaskManager manager = new InMemoryTaskManager();
        Epic epic = manager.addEpic(new Epic(0, "Эпик", "Описание", Status.NEW));
        manager.addSubtask(new Subtask("S1", "", Status.DONE, epic.getId()));
        manager.addSubtask(new Subtask("S2", "", Status.DONE, epic.getId()));
        assertEquals(Status.DONE, manager.getEpicByID(epic.getId()).getStatus());
    }

    @Test
    public void status_mixedNewAndDone_epicIsInProgress() {
        TaskManager manager = new InMemoryTaskManager();
        Epic epic = manager.addEpic(new Epic(0, "Эпик", "Описание", Status.NEW));
        manager.addSubtask(new Subtask("S1", "", Status.NEW, epic.getId()));
        manager.addSubtask(new Subtask("S2", "", Status.DONE, epic.getId()));
        assertEquals(Status.IN_PROGRESS, manager.getEpicByID(epic.getId()).getStatus());
    }

    @Test
    public void status_allInProgress_epicIsInProgress() {
        TaskManager manager = new InMemoryTaskManager();
        Epic epic = manager.addEpic(new Epic(0, "Эпик", "Описание", Status.NEW));
        manager.addSubtask(new Subtask("S1", "", Status.IN_PROGRESS, epic.getId()));
        manager.addSubtask(new Subtask("S2", "", Status.IN_PROGRESS, epic.getId()));
        assertEquals(Status.IN_PROGRESS, manager.getEpicByID(epic.getId()).getStatus());
    }
}