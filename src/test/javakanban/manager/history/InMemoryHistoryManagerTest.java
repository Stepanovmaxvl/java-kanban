package test.javakanban.manager.history;

import main.javakanban.manager.history.InMemoryHistoryManager;
import main.javakanban.manager.history.HistoryManager;
import main.javakanban.manager.task.InMemoryTaskManager;
import main.javakanban.model.Epic;
import main.javakanban.model.Status;
import main.javakanban.model.Subtask;
import main.javakanban.model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static test.javakanban.manager.task.InMemoryTaskManagerTest.*;

class InMemoryHistoryManagerTest {


    //private InMemoryTaskManager taskManager;
    //убрал переменную, но теперь BeforeEach не работает как окружение для Test, пришлось в кажом методе инициализировать

    @BeforeEach
    public void beforeEach() {
        InMemoryTaskManager taskManager = new InMemoryTaskManager();
    }

    @Test
    // про название спасибо, теперь понял что должно быть название метода, которое тестируется
    public void add_deleteFirstTask_addedMoreThen10Task() {
        InMemoryTaskManager taskManager = new InMemoryTaskManager();
        InMemoryHistoryManager historyManager = new InMemoryHistoryManager();

        for (int i = 0; i < 20; i++) {
            Task task = new Task("Задача " + i, "Описание " + i);
            historyManager.add(task);
        }
        List<Task> tasks = taskManager.getTasks();
        for (Task task : tasks) {
            taskManager.getTaskByID(task.getId());
        }

        List<Task> history = historyManager.getHistory();
        assertEquals(10, history.size(), "Неверное количество элементов в истории");
    }

    @Test
    public void add_addCopyOfTask() throws CloneNotSupportedException {
        InMemoryHistoryManager historyManager = new InMemoryHistoryManager();
        // Создаем задачу и добавляем в историю
        Task task = new Task("Задача", "Описание");
        historyManager.add(task);
        task.setName("Измененная задача");
        Task taskInHistory = historyManager.getHistory().get(0);
        assertNotEquals(task.getName(), taskInHistory.getName(), "Имя задачи в истории изменилось!");
        task.setDescription("Новое описание");
        assertNotEquals(task.getDescription(), taskInHistory.getDescription(), "Описание задачи в истории изменилось!");
    }

    @Test
    public void getHistory_returnOldTask_afterUpdate() {
        InMemoryTaskManager taskManager = new InMemoryTaskManager();
        Task washFloor = new Task("Помыть полы", "С новым средством");
        taskManager.addTask(washFloor);
        taskManager.getTaskByID(washFloor.getId());
        taskManager.updateTask(new Task(washFloor.getId(), "Не забыть помыть полы",
                "Можно и без средства", Status.IN_PROGRESS));
        List<Task> tasks = taskManager.getHistory();
        Task oldTask = tasks.getFirst();
        assertEquals(washFloor.getName(), oldTask.getName(), "В истории не сохранилась старая версия задачи");
        assertEquals(washFloor.getDescription(), oldTask.getDescription(),
                "В истории не сохранилась старая версия задачи");
    }

    @Test
    public void getHistory_returnOldEpic_afterUpdate() {
        InMemoryTaskManager taskManager = new InMemoryTaskManager();
        Epic flatRenovation = new Epic("Сделать ремонт", "Нужно успеть за отпуск");
        taskManager.addEpic(flatRenovation);
        taskManager.getEpicByID(flatRenovation.getId());
        taskManager.updateEpic(new Epic(flatRenovation.getId(), "Новое имя", "новое описание",
                Status.IN_PROGRESS));
        List<Task> epics = taskManager.getHistory();
        Epic oldEpic = (Epic) epics.getFirst();
        assertNotEquals(flatRenovation.getName(), oldEpic.getName(), "Имя эпика должно был изменено");
        assertNotEquals(flatRenovation.getDescription(), oldEpic.getDescription(), "Описание эпика должно было изменено");
    }

    @Test
    public void getHistory_returnOldSubtask_afterUpdate() {
        InMemoryTaskManager taskManager = new InMemoryTaskManager();
        Epic flatRenovation = new Epic("Сделать ремонт", "Нужно успеть за отпуск");
        taskManager.addEpic(flatRenovation);
        Subtask flatRenovationSubtask3 = new Subtask("Заказать книжный шкаф", "Из темного дерева",Status.NEW,
                flatRenovation.getId());
        taskManager.addSubtask(flatRenovationSubtask3);
        taskManager.getSubtaskByID(flatRenovationSubtask3.getId());
        taskManager.updateSubtask(new Subtask( "Новое имя",
                "новое описание", Status.IN_PROGRESS, flatRenovation.getId()));
        List<Task> subtasks = taskManager.getHistory();
        Subtask oldSubtask = (Subtask) subtasks.getFirst();
        assertEquals(flatRenovationSubtask3.getName(), oldSubtask.getName(),
                "В истории не сохранилась старая версия сабтаска");
        assertEquals(flatRenovationSubtask3.getDescription(), oldSubtask.getDescription(),
                "В истории не сохранилась старая версия сабтаска");
    }
}