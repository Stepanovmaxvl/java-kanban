package test.javakanban.test.model;

import main.javakanban.model.Status;
import main.javakanban.model.Task;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {


    @Test
    public void equals_returnTrue_idAreSame() {
        Task task1 = new Task(10, "Задача 1", "Сделать Задачу 1", Status.NEW);
        Task task2 = new Task(10, "Задача 2", "Сделать Задачу 2", Status.DONE);
        assertTrue(task1.equals(task1));
    }
}