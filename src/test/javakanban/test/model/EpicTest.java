package test.javakanban.test.model;

import main.javakanban.model.Epic;
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
}