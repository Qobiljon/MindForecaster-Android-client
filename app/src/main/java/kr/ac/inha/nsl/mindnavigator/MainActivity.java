package kr.ac.inha.nsl.mindnavigator;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    //region Notification variables
    AlarmManager alarmManager;
    Intent intentEveryDay;
    PendingIntent broadcastEveryDay;

    Intent intentSundays;
    PendingIntent broadcastSundays;

    //endregion
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.my_toolbar));
        init();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        updateCalendarView();
        super.onActivityResult(requestCode, resultCode, data);
    }

    // region Variables
    final static int EVENT_ACTIVITY = 0;

    private GridLayout event_grid;
    private ViewGroup[][] cells = new ViewGroup[7][5];
    private TextView monthName;
    private TextView year;
    private Calendar currentCal;
    private Calendar clickedCellCal;

    private String[] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};

    // region CellClick Listener
    LinearLayout.OnClickListener cellClick = new LinearLayout.OnClickListener() {
        @Override
        public void onClick(View view) {
            clickedCellCal = Calendar.getInstance();
            clickedCellCal.setTimeInMillis((long) view.getTag());

            FragmentTransaction ft = getFragmentManager().beginTransaction();
            Fragment prev = getFragmentManager().findFragmentByTag("dialog");
            if (prev != null) {
                ft.remove(prev);
            }
            ft.addToBackStack(null);
            Bundle args = new Bundle();
            args.putLong("selectedDayMillis", clickedCellCal.getTimeInMillis());
            DialogFragment dialogFragment = new EventsListDialog();
            dialogFragment.setArguments(args);
            dialogFragment.show(ft, "dialog");

        }
    };
    // endregion
    // endregion

    private void init() {
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);


        //notificationEveryDay(22);
        //notificationEverySunday(20);

        currentCal = Calendar.getInstance();
        event_grid = findViewById(R.id.event_grid);
        monthName = findViewById(R.id.header_month_name);
        year = findViewById(R.id.header_year);

        event_grid.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                event_grid.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                Tools.setCellSize(event_grid.getWidth() / event_grid.getColumnCount(), event_grid.getHeight() / event_grid.getRowCount());
                updateCalendarView();
            }
        });
    }

    @SuppressLint("CutPasteId")
    public void updateCalendarView() {
        Log.e("UPDATE", "CALLED");

        // Update the value of year and month according to the currently selected month
        monthName.setText(months[currentCal.get(Calendar.MONTH)]);
        year.setText(String.valueOf(currentCal.get(Calendar.YEAR)));

        // First clear our the grid
        for (int row = 0; row < event_grid.getRowCount(); row++)
            for (int col = 0; col < event_grid.getColumnCount(); col++)
                Tools.cellClearOut(cells, col, row, this, event_grid, cellClick);

        // Check if displayed month contains today
        Calendar today = Calendar.getInstance();
        TextView todayText;

        if (currentCal.get(Calendar.MONTH) == today.get(Calendar.MONTH) && currentCal.get(Calendar.YEAR) == today.get(Calendar.YEAR)) {
            int col = today.get(Calendar.DAY_OF_WEEK) - 1;
            int row = today.get(Calendar.DAY_OF_MONTH);
            today.set(Calendar.DAY_OF_MONTH, 1);
            row = (row + today.get(Calendar.DAY_OF_WEEK) - 1) / 7;

            todayText = cells[col][row].findViewById(R.id.date_text_view);
            todayText.setTextColor(Color.WHITE);
            todayText.setBackgroundResource(R.drawable.bg_today_view);
        }

        // Calculate which date of week current month starts from

        int firstDayOfMonth = getFirstWeekdayIndex(currentCal.get(Calendar.DAY_OF_MONTH), currentCal.get(Calendar.MONTH), currentCal.get(Calendar.YEAR));
        int numOfDaysCurMonth = currentCal.getActualMaximum(Calendar.DAY_OF_MONTH);

        Calendar clone = (Calendar) currentCal.clone();
        clone.add(Calendar.MONTH, -1);
        int prevCnt = clone.getActualMaximum(Calendar.DAY_OF_MONTH) - firstDayOfMonth + 2;

        // Set dates to display
        int col = 0, row, count = 1;
        for (row = 0; row < event_grid.getRowCount(); row++) {
            if (row == 0) {
                for (col = 0; col < firstDayOfMonth - 1; col++) {
                    TextView date_text = cells[col][row].findViewById(R.id.date_text_view);
                    date_text.setText(String.valueOf(prevCnt));

                    clone.set(Calendar.DAY_OF_MONTH, prevCnt);
                    cells[col][row].setTag(clone.getTimeInMillis());

                    prevCnt++;
                }
                clone.add(Calendar.MONTH, 1);
                for (; col < event_grid.getColumnCount(); col++, count++) {
                    TextView date_text = cells[col][row].findViewById(R.id.date_text_view);
                    date_text.setText(String.valueOf(count));

                    clone.set(Calendar.DAY_OF_MONTH, count);
                    cells[col][row].setTag(clone.getTimeInMillis());
                }
            } else
                for (col = 0; count <= numOfDaysCurMonth && col < event_grid.getColumnCount(); col++, count++) {
                    TextView date_text = cells[col][row].findViewById(R.id.date_text_view);
                    date_text.setText(String.valueOf(count));

                    clone.set(Calendar.DAY_OF_MONTH, count);
                    cells[col][row].setTag(clone.getTimeInMillis());
                }
        }
        clone.add(Calendar.MONTH, 1);

        for (count = 1, row = event_grid.getRowCount() - 1; col < event_grid.getColumnCount(); col++, count++) {
            TextView date_text = cells[col][row].findViewById(R.id.date_text_view);
            date_text.setText(String.valueOf(count));

            clone.set(Calendar.DAY_OF_MONTH, count);
            cells[col][row].setTag(clone.getTimeInMillis());
        }

        // Download events from internet and display them
        if (Tools.isNetworkAvailable(this))
            Tools.execute(new MyRunnable(
                    getString(R.string.url_events_fetch),
                    SignInActivity.loginPrefs.getString(SignInActivity.username, null),
                    SignInActivity.loginPrefs.getString(SignInActivity.password, null),
                    cells[0][0].getTag(),
                    cells[cells.length - 1][cells[0].length - 1].getTag(),
                    currentCal.get(Calendar.MONTH),
                    currentCal.get(Calendar.YEAR)
            ) {
                @Override
                public void run() {
                    String url = (String) args[0];
                    String username = (String) args[1];
                    String password = (String) args[2];
                    long period_from = (long) args[3];
                    long period_till = (long) args[4];
                    int month = (int) args[5];
                    int year = (int) args[6];

                    JSONObject body = new JSONObject();
                    try {
                        body.put("username", username);
                        body.put("password", password);
                        body.put("period_from", period_from);
                        body.put("period_till", period_till);

                        JSONObject res = new JSONObject(Tools.post(url, body));
                        switch (res.getInt("result")) {
                            case Tools.RES_OK:
                                JSONArray array = res.getJSONArray("array");
                                Event[] events = new Event[array.length()];

                                for (int n = 0; n < array.length(); n++) {
                                    JSONObject event = array.getJSONObject(n);
                                    Calendar startTimeCal = Calendar.getInstance(), endTimeCal = Calendar.getInstance();
                                    startTimeCal.setTimeInMillis(event.getLong("startTime"));
                                    endTimeCal.setTimeInMillis(event.getLong("endTime"));

                                    events[n] = new Event(event.getLong("eventId"));
                                    events[n].setTitle(event.getString("title"));
                                    events[n].setStressLevel(event.getInt("stressLevel"));
                                    events[n].setStartTime(startTimeCal);
                                    events[n].setEndTime(endTimeCal);
                                    events[n].setIntervention(event.getString("intervention"));
                                    events[n].setInterventionReminder((short) event.getInt("interventionReminder"));
                                    events[n].setStressType(event.getString("stressType"));
                                    events[n].setStressCause(event.getString("stressCause"));
                                    events[n].setRepeatMode(event.getInt("repeatMode"));
                                }
                                Event.setCurrentEventBank(events);
                                Tools.cacheMonthlyEvents(MainActivity.this, events, month, year);

                                runOnUiThread(new MyRunnable() {
                                    @Override
                                    public void run() {
                                        for (int row = 0; row < event_grid.getRowCount(); row++)
                                            for (int col = 0; col < event_grid.getColumnCount(); col++) {
                                                Calendar day = Calendar.getInstance();
                                                day.setTimeInMillis((long) cells[col][row].getTag());
                                                ArrayList<Event> dayEvents = Event.getOneDayEvents(day);
                                                if (dayEvents.size() == 2) {
                                                    Log.e("ERRORCOUNT", "CASE SIZE 2");
                                                }
                                                for (Event event : dayEvents) {
                                                    getLayoutInflater().inflate(R.layout.event_small_element, cells[col][row]);
                                                    TextView tv = (TextView) cells[col][row].getChildAt(cells[col][row].getChildCount() - 1);
                                                    tv.setBackgroundColor(Tools.stressLevelToColor(event.getStressLevel()));
                                                    tv.setText(event.getTitle());
                                                }
                                            }
                                    }
                                });
                                break;
                            case Tools.RES_FAIL:
                                runOnUiThread(new MyRunnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(MainActivity.this, "Failed to load the events for this month.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                break;
                            case Tools.RES_SRV_ERR:
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(MainActivity.this, "Failure occurred while processing the request. (SERVER SIDE)", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                break;
                            default:
                                break;
                        }
                    } catch (JSONException | IOException e) {
                        e.printStackTrace();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "Failed to proceed due to an error in connection with server.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            });
        else {
            Event.setCurrentEventBank(Tools.readOfflineMonthlyEvents(this, currentCal.get(Calendar.MONTH), currentCal.get(Calendar.YEAR)));
            for (row = 0; row < event_grid.getRowCount(); row++)
                for (col = 0; col < event_grid.getColumnCount(); col++) {
                    Calendar day = Calendar.getInstance();
                    day.setTimeInMillis((long) cells[col][row].getTag());
                    ArrayList<Event> dayEvents = Event.getOneDayEvents(day);
                    for (Event event : dayEvents) {
                        getLayoutInflater().inflate(R.layout.event_small_element, cells[col][row]);
                        TextView tv = (TextView) cells[col][row].getChildAt(cells[col][row].getChildCount() - 1);
                        tv.setBackgroundColor(Tools.stressLevelToColor(event.getStressLevel()));
                        tv.setText(event.getTitle());
                    }
                }
        }
    }

    public int getFirstWeekdayIndex(int day, int month, int year) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DATE, day);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        return cal.get(Calendar.DAY_OF_WEEK);
    }

    public void navNextWeekClick(View view) {
        currentCal.add(Calendar.MONTH, 1);
        updateCalendarView();
    }

    public void navPrevWeekClick(View view) {
        currentCal.add(Calendar.MONTH, -1);
        updateCalendarView();
    }

    public void todayClick(MenuItem item) {
        NotificationManager nMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (nMgr != null) {
            nMgr.cancelAll();
        }
        currentCal = Calendar.getInstance();
        updateCalendarView();
    }

    public void logoutClick(MenuItem item) {
        SharedPreferences.Editor editor = SignInActivity.loginPrefs.edit();
        editor.clear();
        editor.apply();
        Intent intent = new Intent(MainActivity.this, SignInActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.activity_in_reverse, R.anim.activity_out_reverse);
    }

    public void selectMonth(View view) {
    }

    public void selectYear(View view) {
    }

    public void onNewEventClick(View view) {
        Intent intent = new Intent(this, EventActivity.class);
        if (view == findViewById(R.id.btn_add_from_dialog))
            intent.putExtra("selectedDayMillis", clickedCellCal.getTimeInMillis());
        else
            intent.putExtra("selectedDayMillis", Calendar.getInstance().getTimeInMillis());

        startActivityForResult(intent, 0);
        overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
    }

    public void notificationEveryDay(int hourOfDay) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 15);
        intentEveryDay = new Intent(this, AlaramReceiverEveryDay.class);
        intentEveryDay.putExtra("NotificationContext", "This is every day notification");
        broadcastEveryDay = PendingIntent.getBroadcast(this, Tools.NOTIF_PENDING_INTENT_EVERY_DAY, intentEveryDay, PendingIntent.FLAG_UPDATE_CURRENT);

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), AlarmManager.INTERVAL_DAY, broadcastEveryDay);
    }

    public void notificationEverySunday(int hourOfDay) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 15);
        cal.set(Calendar.DAY_OF_WEEK, 1);
        intentSundays = new Intent(this, AlarmReceiverEverySunday.class);
        intentSundays.putExtra("NotificationContext", "This is every Sunday notification");
        broadcastSundays = PendingIntent.getBroadcast(this, Tools.NOTIF_PENDING_INTENT_EVERY_SUNDAY, intentSundays, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), AlarmManager.INTERVAL_DAY * 7, broadcastSundays);

    }
}
