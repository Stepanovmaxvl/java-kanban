public class Main {

    public static void main(String[] args) {

        TaskManager taskManager = new TaskManager();

        //создаем задачу
        Task buyFood = new Task("Купить продукты", "Нужно сходить в магазин и купить по списку продукты...");
        Task washFloorCreated = taskManager.addTask(buyFood);
        System.out.println(buyFood);

        //обноваляем задачу
        Task updateBuyFood = new Task(buyFood.getId(), "Купить продукты для завтрака", "Список: молоко...",
                Status.IN_PROGRESS);
        Task washFloorUpdated = taskManager.updateTask(updateBuyFood);
        System.out.println(washFloorUpdated);

        //создаем задачи
        Epic trackerTasks = new Epic("Реализовать трекер задач", "Нужно реализовать сервис, которые создает " +
                "и взаимодействую с типами задач: Эпик, задача, подзадача");
        taskManager.addEpic(trackerTasks);
        System.out.println(trackerTasks);
        Subtask trackerTasksSubtask1 = new Subtask("Реализовать тип задачи \"Задача\"", "У пользователя должна быть возможность создать тип Задача",
                trackerTasks.getId());
        Subtask trackerTasksSubtask2 = new Subtask("Реализовать тип задачи \"Эпик\"", "У пользователя должна быть возможность создать тип Подзадача",
                trackerTasks.getId());
        taskManager.addSubtask(trackerTasksSubtask1);
        taskManager.addSubtask(trackerTasksSubtask2);

        System.out.println(trackerTasksSubtask1);
        System.out.println(trackerTasksSubtask2);

        //обновляем статус сабтаска2
        trackerTasksSubtask2.setStatus(Status.DONE);
        taskManager.updateSubtask(trackerTasksSubtask2);
        System.out.println(trackerTasksSubtask2);

    }
}
