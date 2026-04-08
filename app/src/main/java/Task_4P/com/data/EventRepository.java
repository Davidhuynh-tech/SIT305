package Task_4P.com.data;

import android.content.Context;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EventRepository {
    private final EventDao eventDao;
    private final LiveData<List<Event>> allEvents;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public EventRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        eventDao = db.eventDao();
        allEvents = eventDao.getAllEventsSorted();
    }

    public LiveData<List<Event>> getAllEvents() {
        return allEvents;
    }

    public void insert(Event event) {
        executor.execute(() -> eventDao.insert(event));
    }

    public void update(Event event) {
        executor.execute(() -> eventDao.update(event));
    }

    public void delete(Event event) {
        executor.execute(() -> eventDao.delete(event));
    }
}
