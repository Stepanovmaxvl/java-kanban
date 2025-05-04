package javakanban.test;

import javakanban.model.Epic;
import org.junit.jupiter.api.Test;
import javakanban.model.Status;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {

    @Test
    public void EpicsWithEqualIdShouldBeEqual() {
        Epic epic1 = new Epic(10, "Эпик 1", "Сделать Эпик 1", Status.NEW);
        Epic epic2 = new Epic(10, "Эпик 2", "Сделать Эпик 2",
                Status.IN_PROGRESS);
        assertEquals(epic1, epic2,
                "Наследники класса Task должны быть равны друг другу, если равен их id;");
    }
}