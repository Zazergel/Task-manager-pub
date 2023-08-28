package logic.http;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import exceptions.NotFoundException;
import logic.managers.Managers;
import logic.managers.TaskManager;
import tasks.Epic;
import tasks.SubTask;
import tasks.Task;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;

public class HttpTaskServer {
    public static final int PORT = 8080;
    private final HttpServer server;
    private final Gson gson;
    private final TaskManager taskManager;

    public HttpTaskServer(TaskManager taskManager) throws IOException {
        this.taskManager = taskManager;
        gson = Managers.getGson();
        server = HttpServer.create(new InetSocketAddress("localhost", PORT), 0);
        server.createContext("/tasks", this::handler);
    }

    private void handler(HttpExchange h) throws IOException {
        try {
            System.out.println("\n /tasks" + h.getRequestURI());
            String path = h.getRequestURI().getPath().replaceFirst("/tasks", "");
            switch (path) {
                case "/task": {
                    System.out.println("запрос /task");
                    handlerTask(h);
                    break;
                }
                case "/subtask": {
                    System.out.println("запрос /subtask");
                    handlerSubTask(h);
                    break;
                }
                case "/epic": {
                    System.out.println("запрос /epic");
                    handlerEpic(h);
                    break;
                }
                case "/subtask/epic": {
                    System.out.println("запрос /subtask/epic");
                    handlerSubTaskToEpic(h);
                    break;
                }
                case "/history": {
                    System.out.println("запрос /history");
                    handlerHistory(h);
                    break;
                }
                case "": {
                    System.out.println("пустой запрос");
                    handlerPriority(h);
                    break;
                }
                default: {
                    System.out.println("/ ждем /task , /subtask , /epic или /history , а получили - " + path);
                    h.sendResponseHeaders(405, 0);
                }
            }
        } catch (JsonSyntaxException e) {
            System.out.println("Передано не корректное тело метода");
            System.out.println("Передайте задачу в формате JSON");
            h.sendResponseHeaders(404, 0);
        } catch (NotFoundException e){
            System.out.println("Запрашиваемый объект не найден!");
            h.sendResponseHeaders(404, 0);
        } catch (Exception e) {
            System.out.println("Ошибка при обработке запроса");
        } finally {
            h.close();
        }
    }

    //Рука, отвечающая за действия с задачами
    private void handlerTask(HttpExchange httpExchange) throws IOException, InterruptedException {
        String requestMethod = httpExchange.getRequestMethod();
        String query = httpExchange.getRequestURI().getQuery();
        switch (requestMethod) {
            case "GET": {
                System.out.println("вызван метод GET");
                if (Objects.nonNull(query)) {
                    int id = Integer.parseInt(query);
                    if (taskManager.getTaskById(id) != null) {
                        final String response = gson.toJson(taskManager.getTaskById(id));
                        sendText(httpExchange, response);
                        return;
                    }
                } else {
                    final String response = gson.toJson(taskManager.showAllTasks());
                    sendText(httpExchange, response);
                    return;
                }
                break;
            }
            case "DELETE": {
                System.out.println("вызван метод DELETE");
                if (Objects.nonNull(query)) {
                    int id = Integer.parseInt(query);
                    if (taskManager.getTaskById(id) != null) {
                        taskManager.delTaskById(id);
                        System.out.println("Задача по ID: " + query + " удалена");
                        httpExchange.sendResponseHeaders(200, 0);
                        return;
                    }
                } else {
                    System.out.println("Все задачи удалены");
                    taskManager.delAllTasks();
                    final String response = gson.toJson(taskManager.getAllTasks());
                    sendText(httpExchange, response);
                    return;
                }
                break;
            }
            case "POST": {
                System.out.println("вызван метод POST");
                String taskInString = readText(httpExchange);
                System.out.println("тело метода: " + taskInString);
                Task task = gson.fromJson(taskInString, Task.class);
                if (!taskManager.getAllTasks().containsKey(task.getId())) {
                    taskManager.createTask(task);
                    System.out.println("Задача типа " + task.getType() + " успешно добавлена");
                    final String response = gson.toJson(task);
                    sendText(httpExchange, response);
                } else {
                    taskManager.updateTask(task);
                    System.out.println("Так как задача с ID: " + task.getId() + " уже есть, она была обновлена");
                    final String response = gson.toJson(taskManager.getTaskById(task.getId()));
                    sendText(httpExchange, response);
                }
                break;
            }
            default: {
                System.out.println("Ожидали метод GET, DELETE или POST, а получили: " + requestMethod);
                httpExchange.sendResponseHeaders(403, 0);

            }
        }
    }

    //Рука, отвечающая за действия с Подзадачами
    private void handlerSubTask(HttpExchange httpExchange) throws IOException, InterruptedException {
        String requestMethod = httpExchange.getRequestMethod();
        String query = httpExchange.getRequestURI().getQuery();
        switch (requestMethod) {
            case "GET": {
                System.out.println("вызван метод GET");
                if (Objects.nonNull(query)) {
                    int id = Integer.parseInt(query);
                    if (taskManager.getSubTaskById(id) != null) {
                        final String response = gson.toJson(taskManager.getSubTaskById(id));
                        sendText(httpExchange, response);
                        return;
                    } else {
                        System.out.println("Подзадачи с ID:" + id + " не существует");
                        httpExchange.sendResponseHeaders(404, 0);
                    }
                } else {
                    final String response = gson.toJson(taskManager.showAllTasks());
                    sendText(httpExchange, response);
                    return;
                }
                break;
            }
            case "DELETE": {
                System.out.println("вызван метод DELETE");
                if (Objects.nonNull(query)) {
                    int id = Integer.parseInt(query);
                    if (taskManager.getSubTaskById(id) != null) {
                        System.out.println("Подзадача по ID: " + query + " удалена");
                        final String response = gson.toJson(taskManager.delSubTaskById(id));
                        sendText(httpExchange, response);
                        return;
                    } else {
                        System.out.println("Подзадачи с ID:" + id + " не существует");
                        httpExchange.sendResponseHeaders(404, 0);
                    }
                } else {
                    System.out.println("Все задачи удалены");
                    taskManager.delAllTasks();
                    final String response = gson.toJson(taskManager.getAllTasks());
                    sendText(httpExchange, response);
                    return;
                }
                break;
            }
            case "POST": {
                System.out.println("вызван метод POST");
                String taskInString = readText(httpExchange);
                System.out.println("тело метода: " + taskInString);

                SubTask subTask = gson.fromJson(taskInString, SubTask.class);
                if (!taskManager.getAllSubTaskFromAllEpics().containsKey(subTask.getId())) {
                    taskManager.createSubTask(subTask);
                    System.out.println("Задача типа " + subTask.getType() + " успешно добавлена");
                    final String response = gson.toJson(subTask);
                    sendText(httpExchange, response);
                } else {
                    taskManager.updateSubTask(subTask);
                    System.out.println("Так как подзадача с ID: " + subTask.getId() + " уже есть, она была обновлена");
                    final String response = gson.toJson(taskManager.getSubTaskById(subTask.getId()));
                    sendText(httpExchange, response);
                }
                break;
            }
            default: {
                System.out.println("Ожидали метод GET, DELETE или POST, а получили: " + requestMethod);
                httpExchange.sendResponseHeaders(403, 0);
            }
        }
    }

    //Рука, отвечающая за действия с Эпиками
    private void handlerEpic(HttpExchange httpExchange) throws IOException, InterruptedException {
        String requestMethod = httpExchange.getRequestMethod();
        String query = httpExchange.getRequestURI().getQuery();
        switch (requestMethod) {
            case "GET": {
                System.out.println("вызван метод GET");
                if (Objects.nonNull(query)) {
                    int id = Integer.parseInt(query);
                    if (taskManager.getEpicById(id) != null) {
                        final String response = gson.toJson(taskManager.getEpicById(id));
                        sendText(httpExchange, response);
                        return;
                    }
                } else {
                    final String response = gson.toJson(taskManager.showAllTasks());
                    sendText(httpExchange, response);
                    return;
                }
                break;
            }
            case "DELETE": {
                System.out.println("вызван метод DELETE");
                if (Objects.nonNull(query)) {
                    int id = Integer.parseInt(query);
                    if (taskManager.getEpics().containsValue(taskManager.getEpicById(id))) {
                        taskManager.delEpicById(id);
                        System.out.println("Эпик задача по ID: " + query + " удалена");
                        httpExchange.sendResponseHeaders(200, 0);
                        return;
                    }
                } else {
                    System.out.println("Все задачи удалены");
                    taskManager.delAllTasks();
                    final String response = gson.toJson(taskManager.getAllTasks());
                    sendText(httpExchange, response);
                    return;
                }
                break;
            }
            case "POST": {
                System.out.println("вызван метод POST");
                String taskInString = readText(httpExchange);
                System.out.println("тело метода: " + taskInString);

                Epic epic = gson.fromJson(taskInString, Epic.class);
                if (!taskManager.getEpics().containsKey(epic.getId())) {
                    taskManager.createEpic(epic);
                    System.out.println("Задача типа " + epic.getType() + " успешно добавлена");
                    final String response = gson.toJson(taskManager.createEpic(epic));
                    sendText(httpExchange, response);
                } else {
                    taskManager.updateEpic(epic);
                    System.out.println("Так как Эпик задача с ID: " + epic.getId() + " уже есть, она была обновлена");
                    final String response = gson.toJson(taskManager.getEpicById(epic.getId()));
                    sendText(httpExchange, response);
                }
                break;
            }
            default: {
                System.out.println("Ожидали метод GET, DELETE или POST, а получили: " + requestMethod);
                httpExchange.sendResponseHeaders(403, 0);
            }
        }
    }

    //Рука, отвечающая за получение подзадач Эпиков
    private void handlerSubTaskToEpic(HttpExchange httpExchange) throws IOException, InterruptedException {
        String requestMethod = httpExchange.getRequestMethod();
        String query = httpExchange.getRequestURI().getQuery();
        if ("GET".equals(requestMethod)) {
            System.out.println("вызван метод GET");
            if (Objects.nonNull(query)) {
                int id = Integer.parseInt(query);
                if (taskManager.getEpicById(id) != null) {
                    final String response = gson.toJson(taskManager.getAllSubTasksByEpic(taskManager.getEpicById(id)));
                    System.out.println("Список подзадачи Эпика с ID:" + id + " успешно возвращен");
                    sendText(httpExchange, response);
                } else {
                    System.out.println("Эпик задачи с ID:" + id + " не существует");
                    httpExchange.sendResponseHeaders(404, 0);
                }
            }
        } else {
            System.out.println("Ожидался метод GET, а получили: " + requestMethod);
            httpExchange.sendResponseHeaders(403, 0);
        }
    }

    //Рука, отвечающая за возвращение списка задач по приоритету
    private void handlerPriority(HttpExchange httpExchange) throws IOException {
        String requestMethod = httpExchange.getRequestMethod();
        if ("GET".equals(requestMethod)) {
            final String response = gson.toJson(taskManager.getPrioritizedTasks());
            System.out.println("Отсортированный по дате список задач возвращен");
            sendText(httpExchange, response);
        } else {
            System.out.println("Ожидался метод GET, а получили: " + requestMethod);
            httpExchange.sendResponseHeaders(403, 0);
        }
    }
    //Рука, отвечающая за возвращение истории
    private void handlerHistory(HttpExchange httpExchange) throws IOException {
        String requestMethod = httpExchange.getRequestMethod();
        if ("GET".equals(requestMethod)) {
            System.out.println("вызван метод GET");
            final String response = gson.toJson(taskManager.getHistory());
            System.out.println("История успешно возвращена");
            sendText(httpExchange, response);
        } else {
            System.out.println("Ожидался метод GET, а получили: " + requestMethod);
            httpExchange.sendResponseHeaders(403, 0);
        }
    }

    //Стартуем
    public void start() {
        System.out.println("Запускаем сервер на порту " + PORT);
        System.out.println("http://localhost:" + PORT + "/tasks");
        server.start();
    }

    //Стоп машина!
    public void stop() {
        server.stop(0);
        System.out.println("Остановили сервер на порту " + PORT);
    }

    public void clear() {
        taskManager.delAllTasks();
        System.out.println("Список всех задач на сервере очищен");
    }

    //Получаем...
    protected String readText(HttpExchange h) throws IOException {
        return new String(h.getRequestBody().readAllBytes(), UTF_8);
    }

    //Посылаем...
    protected void sendText(HttpExchange h, String text) throws IOException {
        byte[] resp = text.getBytes(UTF_8);
        h.getResponseHeaders().add("Content-Type", "application/json;charset=uft-8");
        h.sendResponseHeaders(200, resp.length);
        h.getResponseBody().write(resp);
    }

}