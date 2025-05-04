package javakanban.test;

import javakanban.model.Status;
import javakanban.model.Task;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {


    @Test
    public void tasksWithEqualIdShouldBeEqual() {
        Task task1 = new Task(10, "Задача 1", "Сделать Задачу 1", Status.NEW);
        Task task2 = new Task(10, "Задача 2", "Сделать Задачу 2", Status.DONE);
        assertEquals(task1, task2,
                "Экземпляры класса Task должны быть равны друг другу, если равен их id;");
    }

}