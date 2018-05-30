package kr.ac.inha.nsl.mindnavigator;

import android.app.Activity;
import android.support.annotation.ColorInt;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;

public class Tools {
    // region Variables
    private static int cellWidth, cellHeight;
    // endregion

    public static void setCellSize(int width, int height) {
        cellWidth = width;
        cellHeight = height;
    }

    public static void cellClearOut(ViewGroup[][] grid, int row, int col, Activity activity, ViewGroup parent, LinearLayout.OnClickListener cellClickListener) {
        if (grid[row][col] == null) {
            activity.getLayoutInflater().inflate(R.layout.date_cell, parent, true);
            ViewGroup res = (ViewGroup) parent.getChildAt(parent.getChildCount() - 1);
            res.getLayoutParams().width = cellWidth;
            res.getLayoutParams().height = cellHeight;
            res.setOnClickListener(cellClickListener);
            grid[row][col] = res;
        } else {
            TextView date_text = grid[row][col].findViewById(R.id.date_text_view);
            date_text.setTextColor(activity.getColor(R.color.textColor));
            date_text.setBackgroundResource(R.drawable.bg_cell);

            while (grid[row][col].getChildCount() > 1)
                grid[row][col].removeViewAt(1);
        }
    }

    public static void addEvent(Activity activity, ViewGroup parent, Event event) {
        activity.getLayoutInflater().inflate(R.layout.event_element, parent);
        TextView res = (TextView) parent.getChildAt(parent.getChildCount() - 1);
        res.setBackgroundColor(event.getStressColor());
        res.setText(event.getTitle());
    }
}

class Event {
    Event(String title, int stressLevel, Calendar startTime, Calendar endTime) {
        setTitle(title);
        setStressLevel(stressLevel);
        setStartTime(startTime);
        setEndTime(endTime);
        id = System.currentTimeMillis() / 1000;

        events.add(this);
    }

    public static void init(Activity activity) {
        stressColors[0] = activity.getColor(R.color.slvl0_color);
        stressColors[1] = activity.getColor(R.color.slvl1_color);
        stressColors[2] = activity.getColor(R.color.slvl2_color);
        stressColors[3] = activity.getColor(R.color.slvl3_color);
        stressColors[4] = activity.getColor(R.color.slvl4_color);

        events.clear();
    }

    public static ArrayList<Event> getOneDayEvents(Calendar day) {
        ArrayList<Event> res = new ArrayList<>();

        Calendar comDay = (Calendar) day.clone();

        comDay.set(Calendar.HOUR, 0);
        comDay.set(Calendar.MINUTE, 0);
        comDay.set(Calendar.SECOND, 0);
        comDay.set(Calendar.MILLISECOND, 0);
        long fromTime = comDay.getTimeInMillis();

        comDay.add(Calendar.DAY_OF_MONTH, 1);
        comDay.add(Calendar.MINUTE, -1);
        long toTime = comDay.getTimeInMillis();

        for (Event event : events) {
            long start = event.getStartTime().getTimeInMillis();
            long end = event.getEndTime().getTimeInMillis();

            if ((start >= fromTime && start < toTime) || (end >= fromTime && end < toTime))
                res.add(event);
        }

        return res;
    }

    //region Variables
    @ColorInt
    private static int[] stressColors = new int[5];
    private static ArrayList<Event> events = new ArrayList<>();

    private long id;
    private int stressLevel;
    private String title;
    private Calendar startTime;
    private Calendar endTime;
    //endregion

    private void setStartTime(Calendar startTime) {
        startTime.set(Calendar.SECOND, 0);
        startTime.set(Calendar.MILLISECOND, 0);
        this.startTime = (Calendar) startTime.clone();
    }

    private Calendar getStartTime() {
        return startTime;
    }

    private void setEndTime(Calendar endTime) {
        endTime.set(Calendar.SECOND, 0);
        endTime.set(Calendar.MILLISECOND, 0);
        this.endTime = (Calendar) endTime.clone();
    }

    private Calendar getEndTime() {
        return endTime;
    }

    private void setStressLevel(int stressLevel) {
        this.stressLevel = stressLevel;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getEventId() {
        return id;
    }

    public int getStressColor() {
        return stressColors[stressLevel];
    }

    public String getTitle() {
        return title;
    }
}