package Task_4P.com.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "events")
public class Event {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String title;
    public String category;
    public String location;
    public long dateTimeMillis;
}
