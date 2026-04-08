package Task_4P.com.ui;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import Task_4P.com.data.Event;
import Task_4P.com.data.EventRepository;

public class EventViewModel extends AndroidViewModel {
    private final EventRepository repository;
    private final LiveData<List<Event>> allEvents;

    public EventViewModel(@NonNull Application application) {
        super(application);
        repository = new EventRepository(application);
        allEvents = repository.getAllEvents();
    }

    public LiveData<List<Event>> getAllEvents() {
        return allEvents;
    }

    public void insert(Event event) {
        repository.insert(event);
    }

    public void update(Event event) {
        repository.update(event);
    }

    public void delete(Event event) {
        repository.delete(event);
    }
}
