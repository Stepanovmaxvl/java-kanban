package test.javakanban.model;

import main.javakanban.model.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SubtaskTest {

    @Test
    public void equals_returnTrue_idAreSame() {
        Subtask subtask1 = new Subtask("Подзадача 1", "Сделать Подзадачу 1", Status.NEW, 5);
        Subtask subtask2 = new Subtask("Подзадача 2", "Сделать Подзадачу 2", Status.DONE, 5);

        assertTrue(subtask1.equals(subtask2));
    }

    @Test
    public void setId_donotSetId_idIsSameWithEpicId() {
        int epicId = 1;
        Subtask subtask = new Subtask("Сабтаск", "Описание сабтаска", Status.NEW, epicId);
        subtask.setId(epicId);
        assertEquals(epicId, subtask.getId(), "ID сабтаска должен остаться прежним");
    }
}