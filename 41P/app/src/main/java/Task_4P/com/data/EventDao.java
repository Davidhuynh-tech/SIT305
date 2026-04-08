package Task_4P.com.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface EventDao {
    @Insert
    long insert(Event event);

    @Update
    void update(Event event);

    @Delete
    void delete(Event event);

    @Query("SELECT * FROM events ORDER BY dateTimeMillis ASC")
    LiveData<List<Event>> getAllEventsSorted();
}
