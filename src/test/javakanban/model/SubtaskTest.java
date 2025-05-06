package test.javakanban.model;

import main.javakanban.model.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SubtaskTest {

    @Test
    public void equals_returnTrue_idAreSame() {
        Subtask subtask1 = new Subtask(10, "Подзадача 1", "Сделать Подзадачу 1", Status.NEW, 5);
        Subtask subtask2 = new Subtask(10, "Подзадача 2", "Сделать Подзадачу 2", Status.DONE, 5);
        assertTrue(subtask1.equals(subtask2));
        }

}