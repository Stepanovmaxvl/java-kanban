package main.javakanban.manager.task;

import main.javakanban.model.Epic;
import main.javakanban.model.Subtask;
import main.javakanban.model.Task;

import java.util.ArrayList;
import java.util.List;

public interface TaskManager {

    //int getNextId();

      //создаем тип "Задача"
    Task addTask(Task task);

    //создаем тип "Эпик"
    Epic addEpic(Epic epic);

    //создаем тип "Подзадача"
    Subtask addSubtask(Subtask subtask);

    //обновляем тип "Задача"
    Task updateTask(Task task);

    //обновляем тип "Эпик"
    Epic updateEpic(Epic epic);

    //обновляем тип "Подзадача"
    Subtask updateSubtask(Subtask subtask);

    Task getTaskByID(int id);

    Epic getEpicByID(int id);

    Subtask getSubtaskByID(int id);

    List<Task> getTasks();

    ArrayList<Epic> getEpics();

    List<Subtask> getSubtasks();

    ArrayList<Subtask> getEpicSubtasks(int id);

    void deleteTasks();

    void deleteEpics();

    void deleteSubtasks();

    Object deleteTaskByID(int id);

    void deleteEpicByID(int id);

    Object deleteSubtaskByID(int id);

    List<Task> getHistory();
}
