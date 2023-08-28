package tests;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import logic.http.HttpTaskServer;
import logic.managers.InMemoryTaskManager;
import logic.managers.Managers;
import logic.managers.TaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import tasks.Epic;
import tasks.SubTask;
import tasks.Task;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class HttpTaskServerTest {
    private HttpTaskServer server;
    private Task task;
    private Epic epic;
    private final static String STATUS_200 = "Возвращен код состояния не 200";
    private final static String STATUS_403 = "Возвращен код состояния не 403";
    private final static String STATUS_404 = "Возвращен код состояния не 404";

    private final Gson gson = Managers.getGson();

    @BeforeEach
    void init() throws IOException, InterruptedException {
        TaskManager mng = new InMemoryTaskManager();
        task = new Task("TestTask", "TestTask description", LocalDateTime.now(), 15);
        mng.createTask(task);
        epic = new Epic("TestEpic", "EpicDesc");
        mng.createEpic(epic);
        SubTask subtask = new SubTask("Test Subtask", "Test description", LocalDateTime.now().plusMinutes(10), 30, epic.getId());
        mng.createSubTask(subtask);
        server = new HttpTaskServer(mng);
        server.start();
    }

    @AfterEach
    void stop() {
        server.clear();
        server.stop();
    }

    @Test
    void getTasks() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        URI url = URI.create("http://localhost:8080/tasks/task");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), STATUS_200);

        Type userType = new TypeToken<ArrayList<String>>() {
        }.getType();

        List<String> tasks = gson.fromJson(response.body(), userType);

        assertNotNull(tasks, "Список задач пустой");
        assertEquals(3, tasks.size(), "Не верное количество задач");
    }

    @ParameterizedTest
    @ValueSource(strings = {"task?1", "subtask?3", "epic?2"})
    void getTasksByID(String s) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        URI url = URI.create("http://localhost:8080/tasks/" + s);
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), STATUS_200);

        Type userType = new TypeToken<Task>() {
        }.getType();

        Task taskResponse = gson.fromJson(response.body(), userType);

        assertNotNull(taskResponse, "Задача не возвращена");
        assertEquals(taskResponse.getId(), Integer.parseInt(String.valueOf(s.charAt((s.length() - 1))))
                , "Возвращена не верная задача");
    }

    @ParameterizedTest
    @ValueSource(strings = {"task?2222", "subtask?2222", "epic?222", "subtask/epic?9999"})
    void getTasksByNotExistID(String s) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        URI url = URI.create("http://localhost:8080/tasks/" + s);
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode(), STATUS_404);
    }

    @Test
    void deleteAllTasks() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        URI url = URI.create("http://localhost:8080/tasks/task");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), STATUS_200);

        Type userType = new TypeToken<HashMap<Integer, Task>>() {
        }.getType();

        HashMap<Integer, Task> taskResponse = gson.fromJson(response.body(), userType);

        assertNotNull(taskResponse, "Задачи не возвращены");
        assertEquals(0, taskResponse.size(), "Задача не удалена");
    }

    @ParameterizedTest
    @ValueSource(strings = {"task?1", "epic?2"})
    void deleteTasksByID(String s) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        URI url = URI.create("http://localhost:8080/tasks/" + s);
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), STATUS_200);
    }

    @ParameterizedTest
    @ValueSource(strings = {"task?32", "subtask?33", "epic?56"})
    void deleteTasksByNotExistID(String s) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        URI url = URI.create("http://localhost:8080/tasks/" + s);
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode(), STATUS_404);
    }

    @Test
    void postNewTask() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        URI url = URI.create("http://localhost:8080/tasks/task");
        Task newTask = new Task("TestTask 2", "TestTask 2 description", LocalDateTime.now().plusMinutes(25), 15);
        newTask.setId(4);
        String json = gson.toJson(newTask);
        final HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(body).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), STATUS_200);

        Type userType = new TypeToken<Task>() {
        }.getType();

        Task taskResponse = gson.fromJson(response.body(), userType);

        assertNotNull(taskResponse, "Задачи не добавлена");
        assertEquals(4, taskResponse.getId(), "Задача не добавлена");
    }

    @Test
    void postNewSubTasks() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        URI url = URI.create("http://localhost:8080/tasks/subtask");
        SubTask newTask = new SubTask("Подзадача 2", "Описание подзадачи 2",
                LocalDateTime.now(), 30, 2);
        String json = gson.toJson(newTask);
        final HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(body).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), STATUS_200);

        Type userType = new TypeToken<SubTask>() {
        }.getType();

        SubTask taskResponse = gson.fromJson(response.body(), userType);

        assertNotNull(taskResponse, "Подзадачи не добавлена");
        assertEquals(4, taskResponse.getId(), "Подзадача не добавлена");
    }

    @Test
    void postNewEpic() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        URI url = URI.create("http://localhost:8080/tasks/epic");
        Epic epic2 = new Epic("Эпик задача 2", "Описание Эпик задачи 2");
        String json = gson.toJson(epic2);
        final HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(body).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), STATUS_200);

        Type userType = new TypeToken<Epic>() {
        }.getType();

        Epic taskResponse = gson.fromJson(response.body(), userType);

        assertNotNull(taskResponse, "Эпик задачи не добавлена");
        assertEquals(5, taskResponse.getId(), "Эпик задача не добавлена");
    }

    @ParameterizedTest
    @ValueSource(strings = {"task", "subtask", "epic"})
    void postTasksByNotExistBody(String s) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        URI url = URI.create("http://localhost:8080/tasks/" + s);
        String json = "Тут нет задачи";
        final HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(body).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode(), STATUS_404);
    }

    @Test
    void postTasksUpdate() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        URI url = URI.create("http://localhost:8080/tasks/task");
        Task newTask = new Task("Новое имя задачи", "Новое описание задачи", LocalDateTime.now(), 15);
        newTask.setId(1);
        String json = gson.toJson(newTask);
        final HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(body).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), STATUS_200);

        Type userType = new TypeToken<Task>() {
        }.getType();

        Task taskResponse = gson.fromJson(response.body(), userType);

        assertNotNull(taskResponse, "Задачи не добавлена");
        assertEquals(1, taskResponse.getId(), "Задача не обновлена");
        assertEquals("Новое имя задачи", taskResponse.getName(), "Задача не обновлена");
    }

    @Test
    void postSubTasksUpdate() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        URI url = URI.create("http://localhost:8080/tasks/subtask");
        SubTask newTask = new SubTask("Новое имя подзадачи", "Новое описание подзадачи", LocalDateTime.now().plusMinutes(10), 30, epic.getId());
        newTask.setId(3);
        String json = gson.toJson(newTask);
        final HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(body).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), STATUS_200);

        Type userType = new TypeToken<SubTask>() {
        }.getType();

        SubTask taskResponse = gson.fromJson(response.body(), userType);

        assertNotNull(taskResponse, "Задачи не добавлена");
        assertEquals(3, taskResponse.getId(), "Подзадача не обновлена");
        assertEquals("Новое имя подзадачи", taskResponse.getName(), "Подзадача не обновлена");
    }

    @Test
    void postEpicUpdate() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        URI url = URI.create("http://localhost:8080/tasks/epic");
        Epic newEpic = new Epic("Новое имя Эпик задачи", "Новое описание Эпик задачи");
        newEpic.setId(2);
        String json = gson.toJson(newEpic);
        final HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(body).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), STATUS_200);

        Type userType = new TypeToken<Epic>() {
        }.getType();

        Epic taskResponse = gson.fromJson(response.body(), userType);

        assertNotNull(taskResponse, "Задачи не добавлена");
        assertEquals(2, taskResponse.getId(), "Эпик задача не обновлена");
        assertEquals("Новое имя Эпик задачи", taskResponse.getName(), "Эпик задача не обновлена");
    }

    @ParameterizedTest
    @ValueSource(strings = {"task", "subtask", "epic"})
    void putTasks(String s) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        URI url = URI.create("http://localhost:8080/tasks/" + s);
        String json = "Тут могла бы быть  задача";
        final HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);
        HttpRequest request = HttpRequest.newBuilder().uri(url).PUT(body).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(403, response.statusCode(), STATUS_403);
    }

    @Test
    void getSubTaskOfEpic() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        URI url = URI.create("http://localhost:8080/tasks/subtask/epic?2");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), STATUS_200);

        Type userType = new TypeToken<ArrayList<SubTask>>() {
        }.getType();

        List<SubTask> tasks = gson.fromJson(response.body(), userType);

        assertNotNull(tasks, "Список задач пустой");
        assertEquals(1, tasks.size(), "Не верное количество задач");
    }

    @Test
    void deleteSubTaskByNotExistEpic() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        URI url = URI.create("http://localhost:8080/tasks/subtask/epic?2");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(403, response.statusCode(), STATUS_403);
    }

    @Test
    void getHistory() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        URI url = URI.create("http://localhost:8080/tasks/history");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), STATUS_200);

        Type userType = new TypeToken<List<Integer>>() {
        }.getType();

        List<Integer> history = gson.fromJson(response.body(), userType);

        assertNotNull(history, "Список задач пустой");
    }

    @Test
    void deleteHistory() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        URI url = URI.create("http://localhost:8080/tasks/history");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(403, response.statusCode(), STATUS_403);
    }

    @Test
    void getPrioritizedTasks() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), STATUS_200);

        Type userType = new TypeToken<ArrayList<Task>>() {
        }.getType();

        ArrayList<Task> history = gson.fromJson(response.body(), userType);

        assertNotNull(history, "Список задач пустой");
        assertEquals(2, history.size(), "Не верное количество задач");
        assertEquals(history.get(0).getId(), task.getId(), "Не верный порядок задач");
    }

    @Test
    void deletePrioritizedTasks() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(403, response.statusCode(), STATUS_403);
    }
}