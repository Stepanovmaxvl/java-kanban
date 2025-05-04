package javakanban.test;

import javakanban.model.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SubtaskTest {

    @Test
    public void SubtasksWithEqualIdShouldBeEqual() {
        Subtask subtask1 = new Subtask(10, "Подзадача 1", "Сделать Подзадачу 1", Status.NEW, 5);
        Subtask subtask2 = new Subtask(10, "Подзадача 2", "Сделать Подзадачу 2", Status.DONE, 5);
        assertEquals(subtask1, subtask2,
                "Наследники класса Task должны быть равны друг другу, если равен их id;");
    }

}