package main.javakanban;

import main.javakanban.manager.task.InMemoryTaskManager;
import main.javakanban.manager.task.Managers;
import main.javakanban.model.*;

public class Main {

    private static final InMemoryTaskManager inMemoryTaskManager = Managers.getDefault();

    public static void main(String[] args) {

        addTasks();
        printAllTasks();
        printViewHistory();
    }

    private static void addTasks() {
        Task buyFood = new Task("Купить продукты", "Нужно сходить в магазин и купить по списку продукты...");
        inMemoryTaskManager.addTask(buyFood);

        Task updateBuyFood = new Task(buyFood.getId(), "Купить продукты для завтрака",
                "Список: молоко...", Status.IN_PROGRESS);
        inMemoryTaskManager.updateTask(updateBuyFood);
        inMemoryTaskManager.addTask(new Task("Купить продукты на обед", "Список: хлеб..."));


        Epic trackerTasks = new Epic("Реализовать трекер задач", "Нужно реализовать сервис, который создает и взаимодействую с типами задач: Эпик, задача, подзадача ");
        inMemoryTaskManager.addEpic(trackerTasks);
        Subtask flatRenovationSubtask1 = new Subtask("Реализовать тип задачи \"Задача\"", "У пользователя должна быть возможность создать тип Задача",
                trackerTasks.getId());
        Subtask flatRenovationSubtask2 = new Subtask("Реализовать тип задачи \"Эпик\"", "У пользователя должна быть возможность создать тип Подзадача",
                trackerTasks.getId());
        Subtask flatRenovationSubtask3 = new Subtask("Реализовать тестирование сервисов", "У dev и QA должна быть возможность протестировать исходный код",
                trackerTasks.getId());
        inMemoryTaskManager.addSubtask(flatRenovationSubtask1);
        inMemoryTaskManager.addSubtask(flatRenovationSubtask2);
        inMemoryTaskManager.addSubtask(flatRenovationSubtask3);
        flatRenovationSubtask2.setStatus(Status.DONE);
        inMemoryTaskManager.updateSubtask(flatRenovationSubtask2);
    }

    private static void printAllTasks() {
        System.out.println("Задачи:");
        for (Task task : Main.inMemoryTaskManager.getTasks()) {
            System.out.println(task);
        }

        System.out.println("Эпики:");
        for (Epic epic : Main.inMemoryTaskManager.getEpics()) {
            System.out.println(epic);
        }

        System.out.println("Подзадачи:");
        for (Task subtask : Main.inMemoryTaskManager.getSubtasks()) {
            System.out.println(subtask);
        }
    }

    private static void printViewHistory() {
        //просматриваем 11 задач, в истории должны отобразиться последние 10
        Main.inMemoryTaskManager.getTaskByID(1);
        Main.inMemoryTaskManager.getTaskByID(2);
        Main.inMemoryTaskManager.getEpicByID(3);
        Main.inMemoryTaskManager.getTaskByID(1);
        Main.inMemoryTaskManager.getSubtaskByID(4);
        Main.inMemoryTaskManager.getSubtaskByID(5);
        Main.inMemoryTaskManager.getSubtaskByID(6);
        Main.inMemoryTaskManager.getEpicByID(3);
        Main.inMemoryTaskManager.getSubtaskByID(4);
        Main.inMemoryTaskManager.getTaskByID(2);
        Main.inMemoryTaskManager.getSubtaskByID(6);
        System.out.println();
        System.out.println("История просмотров:");
        for (Task task : Main.inMemoryTaskManager.getHistory()) {
            System.out.println(task);
        }
    }
}

