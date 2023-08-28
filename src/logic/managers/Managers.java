package logic.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import history.HistoryManager;
import history.InMemoryHistoryManager;
import logic.LocalDateTimeAdapter;

import java.time.LocalDateTime;

public class Managers {

    public static Gson getGson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter());
        return gsonBuilder.create();
    }

    static public HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }

}
