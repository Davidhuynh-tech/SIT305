package com.example.mediacontentapp.sports.data;

import com.example.mediacontentapp.R;
import com.example.mediacontentapp.sports.model.NewsItem;

import java.util.ArrayList;
import java.util.List;

public final class SportsNewsRepository {

    private SportsNewsRepository() {
    }

    public static List<NewsItem> getAllNews() {
        List<NewsItem> items = new ArrayList<>();
        items.add(new NewsItem(1, "Melbourne Tigers vs Sydney Hawks",
                "Match Date: 2026-04-10\nScore: 2-1\nVenue: AAMI Park\nStatus: Completed",
                "Football", true, R.drawable.sport_img_football));
        items.add(new NewsItem(2, "City Dunkers vs West Hoops",
                "Match Date: 2026-04-11\nScore: 98-91\nVenue: Rod Laver Arena\nStatus: Completed",
                "Basketball", true, R.drawable.sport_img_basketball));
        items.add(new NewsItem(3, "Victoria Strikers vs Brisbane Smashers",
                "Match Date: 2026-04-12\nScore: 245/8\nVenue: MCG\nStatus: Completed",
                "Cricket", true, R.drawable.sport_img_cricket));
        items.add(new NewsItem(4, "Northern United vs Eastern FC",
                "Match Date: 2026-04-13\nScore: 0-0\nVenue: Marvel Stadium\nStatus: Completed",
                "Football", false, R.drawable.sport_img_football));
        items.add(new NewsItem(5, "South Flames vs Metro Giants",
                "Match Date: 2026-04-14\nScore: 105-112\nVenue: John Cain Arena\nStatus: Completed",
                "Basketball", false, R.drawable.sport_img_basketball));
        items.add(new NewsItem(6, "Coastal Warriors vs Perth Blazers",
                "Match Date: 2026-04-15\nScore: 189/6\nVenue: Perth Stadium\nStatus: In Progress",
                "Cricket", false, R.drawable.sport_img_cricket));
        items.add(new NewsItem(7, "Green Eagles vs Red Lions",
                "Match Date: 2026-04-16\nScore: 3-2\nVenue: Lakeside Stadium\nStatus: Scheduled",
                "Football", false, R.drawable.sport_img_football));
        items.add(new NewsItem(8, "Elite Shooters vs Rapid Bulls",
                "Match Date: 2026-04-17\nScore: 87-95\nVenue: State Basketball Centre\nStatus: Scheduled",
                "Basketball", false, R.drawable.sport_img_basketball));
        return items;
    }
}
