package kr.ac.inha.nsl.mindnavigator;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.LongSparseArray;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Tools {
    // region Variables
    static final short
            NOTIF_PENDING_INTENT_EVERY_DAY = 0,
            NOTIF_PENDING_INTENT_EVERY_SUNDAY = 1;

    static final short
            RES_OK = 0,
            RES_SRV_ERR = -1,
            RES_FAIL = 1;

    private static int cellWidth, cellHeight;

    private static ExecutorService executor = Executors.newCachedThreadPool();
    // endregion

    static void setCellSize(int width, int height) {
        cellWidth = width;
        cellHeight = height;
    }

    static void cellClearOut(ViewGroup[][] grid, int row, int col, Activity activity, ViewGroup parent, LinearLayout.OnClickListener cellClickListener) {
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
            date_text.setBackground(null);

            while (grid[row][col].getChildCount() > 1)
                grid[row][col].removeViewAt(1);
        }
    }

    static String post(String _url, JSONObject json_body) throws IOException {
        URL url = new URL(_url);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setDoOutput(json_body != null);
        con.setDoInput(true);
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.connect();

        if (json_body != null) {
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(json_body.toString());
            wr.flush();
            wr.close();
        }

        int status = con.getResponseCode();
        if (status != HttpURLConnection.HTTP_OK) {
            con.disconnect();
            return null;
        } else {
            byte[] buf = new byte[1024];
            int rd;
            StringBuilder sb = new StringBuilder();
            BufferedInputStream is = new BufferedInputStream(con.getInputStream());
            while ((rd = is.read(buf)) > 0)
                sb.append(new String(buf, 0, rd, "utf-8"));
            is.close();
            con.disconnect();
            return sb.toString();
        }
    }

    static void execute(MyRunnable runnable) {
        executor.execute(runnable);
    }

    static void toggle_keyboard(@NonNull Activity activity, EditText editText, boolean show) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null)
            if (show)
                imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
            else
                imm.hideSoftInputFromWindow(editText.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
    }

    static void copy_date(long fromMillis, Calendar toCal) {
        Calendar fromCal = Calendar.getInstance();
        fromCal.setTimeInMillis(fromMillis);

        toCal.set(Calendar.YEAR, fromCal.get(Calendar.YEAR));
        toCal.set(Calendar.MONTH, fromCal.get(Calendar.MONTH));
        toCal.set(Calendar.DAY_OF_MONTH, fromCal.get(Calendar.DAY_OF_MONTH));
    }

    static void copy_time(long fromMillis, Calendar toCal) {
        Calendar fromCal = Calendar.getInstance();
        fromCal.setTimeInMillis(fromMillis);

        toCal.set(Calendar.HOUR_OF_DAY, fromCal.get(Calendar.HOUR_OF_DAY));
        toCal.set(Calendar.MINUTE, fromCal.get(Calendar.MINUTE));
        toCal.set(Calendar.SECOND, 0);
        toCal.set(Calendar.MILLISECOND, 0);
    }

    @ColorInt
    static int stressLevelToColor(int level) {
        float c = 5.11f;

        if (level > 98)
            return Color.RED;
        else if (level < 50)
            return Color.argb(0xff, (int) (level * c), 0xff, 0);
        else
            return Color.argb(0xff, 0xff, (int) (c * (100 - level)), 0);
    }

    static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo;
        if (connectivityManager == null)
            return false;
        activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private static void writeToFile(Context context, String fileName, String data) {
        try {
            FileOutputStream outputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
            outputStreamWriter.write(data);
            outputStreamWriter.close();
            outputStream.close();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    private static String readFromFile(Context context, String fileName) {
        String ret = "";

        try {
            InputStream inputStream = context.openFileInput(fileName);

            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString;
                StringBuilder stringBuilder = new StringBuilder();

                while ((receiveString = bufferedReader.readLine()) != null) {
                    stringBuilder.append(receiveString);
                }

                bufferedReader.close();
                inputStream.close();
                ret = stringBuilder.toString();
            }
        } catch (FileNotFoundException e) {
            Log.e("Exception", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("Exception", "Can not read file: " + e.toString());
        }

        return ret;
    }

    static void cacheMonthlyEvents(Context context, Event[] events, int month, int year) {
        if (events.length == 0)
            return;

        JSONArray array = new JSONArray();
        for (Event event : events)
            array.put(event.toJson());

        Tools.writeToFile(context, String.format(Locale.US, "events_%02d_%d.json", month, year), array.toString());
    }

    static Event[] readOfflineMonthlyEvents(Context context, int month, int year) {
        JSONArray array;
        try {
            array = new JSONArray(readFromFile(context, String.format(Locale.US, "events_%02d_%d.json", month, year)));
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        try {
            Event[] res = new Event[array.length()];
            for (int n = 0; n < array.length(); n++) {
                res[n] = new Event(1);
                res[n].fromJson(array.getJSONObject(n));
            }
            return res;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void cacheInterventions(Context context, String[] interventions, String type) {
        if (interventions.length == 0)
            return;

        JSONArray array = new JSONArray();
        for (String intervention : interventions)
            array.put(intervention);

        Tools.writeToFile(context, String.format(Locale.US, "%s_interventions.json", type), array.toString());
    }

    static void cacheSystemInterventions(Context context, String[] sysInterventions) {
        cacheInterventions(context, sysInterventions, "system");
    }

    static void cachePeerInterventions(Context context, String[] peerInterventions) {
        cacheInterventions(context, peerInterventions, "peer");
    }

    private static String[] readOfflineInterventions(Context context, String type) {
        JSONArray array;
        try {
            array = new JSONArray(readFromFile(context, String.format(Locale.US, "%s_interventions.json", type)));
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        try {
            String[] res = new String[array.length()];
            for (int n = 0; n < array.length(); n++)
                res[n] = array.getString(n);
            return res;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    static String[] readOfflineSystemInterventions(Context context) {
        return readOfflineInterventions(context, "system");
    }

    public static String[] readOfflinePeerInterventions(Context context) {
        return readOfflineInterventions(context, "peer");
    }

    static int addDailyNotif(Context context, Calendar when, String text) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intentEveryDay = new Intent(context, AlaramReceiverEveryDay.class);
        intentEveryDay.putExtra("Content", text);
        intentEveryDay.putExtra("notification_id", when);
        PendingIntent broadcastEveryDay = PendingIntent.getBroadcast(context, (int) when.getTimeInMillis(), intentEveryDay, PendingIntent.FLAG_UPDATE_CURRENT);
        if (alarmManager != null)
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, when.getTimeInMillis(), AlarmManager.INTERVAL_DAY, broadcastEveryDay);
        return (int) when.getTimeInMillis();
    }

    static int addSundayNotif(Context context, Calendar when, String text) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intentSundays = new Intent(context, AlarmReceiverEverySunday.class);
        intentSundays.putExtra("Content", text);
        intentSundays.putExtra("notification_id", when);
        PendingIntent broadcastSundays = PendingIntent.getBroadcast(context, (int) when.getTimeInMillis(), intentSundays, PendingIntent.FLAG_UPDATE_CURRENT);
        if (alarmManager != null)
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, when.getTimeInMillis(), AlarmManager.INTERVAL_DAY * 7, broadcastSundays);
        return (int) when.getTimeInMillis();
    }

    static int addEventNotif(Context context, long event_id, String text) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(event_id);
        Intent intentEvent = new Intent(context, AlarmReceiverEvent.class);
        intentEvent.putExtra("Content", text);
        intentEvent.putExtra("EventId", event_id);
        PendingIntent broadcastEvent = PendingIntent.getBroadcast(context, (int) cal.getTimeInMillis(), intentEvent, PendingIntent.FLAG_UPDATE_CURRENT);
        if (alarmManager != null)
            alarmManager.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), broadcastEvent);
        return (int) cal.getTimeInMillis();
    }

    static void cancelNotif(Context context, PendingIntent pendingIntent, int notif_id) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null)
            alarmManager.cancel(pendingIntent);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null)
            notificationManager.cancel(notif_id);
    }
}

abstract class MyRunnable implements Runnable {
    MyRunnable(Object... args) {
        this.args = Arrays.copyOf(args, args.length);
    }

    Object[] args;
}

class Event {
    Event(long id) {
        newEvent = id == 0;
        if (newEvent)
            this.id = System.currentTimeMillis() / 1000;
        else
            this.id = id;
    }

    static ArrayList<Event> getOneDayEvents(@NonNull Calendar day) {
        ArrayList<Event> res = new ArrayList<>();

        if (currentEventBank == null || currentEventBank.length == 0)
            return res;

        Calendar comDay = (Calendar) day.clone();
        comDay.set(Calendar.HOUR, 0);
        comDay.set(Calendar.MINUTE, 0);
        comDay.set(Calendar.SECOND, 0);
        comDay.set(Calendar.MILLISECOND, 0);
        long periodFrom = comDay.getTimeInMillis();

        comDay.add(Calendar.DAY_OF_MONTH, 1);
        comDay.add(Calendar.MINUTE, -1);
        long periodTill = comDay.getTimeInMillis();

        for (Event event : currentEventBank) {
            long evStartTime = event.getStartTime().getTimeInMillis();
            long evEndTime = event.getEndTime().getTimeInMillis();

            if (periodFrom <= evStartTime && evStartTime < periodTill)
                res.add(event);
            else if (periodFrom < evEndTime && evEndTime <= periodTill)
                res.add(event);
            else if (evStartTime <= periodFrom && periodTill <= evEndTime)
                res.add(event);
        }

        return res;
    }

    //region Variables
    private static Event[] currentEventBank;
    private static LongSparseArray<Event> idEventMap = new LongSparseArray<>();
    static final int NO_REPEAT = 0, REPEAT_EVERYDAY = 1, REPEAT_WEEKLY = 2;

    private boolean newEvent;

    private long id;
    private String title;
    private int stressLevel;
    private Calendar startTime;
    private Calendar endTime;
    private String intervention;
    private short interventionReminder;
    private String stressType;
    private String stressCause;
    private int repeatMode;
    //endregion

    static void setCurrentEventBank(Event[] bank) {
        currentEventBank = bank;

        idEventMap.clear();
        for (Event event : currentEventBank)
            idEventMap.put(event.id, event);
    }

    static Event getEventById(long key) {
        return idEventMap.get(key);
    }

    boolean isNewEvent() {
        return newEvent;
    }

    long getEventId() {
        return id;
    }

    void setStartTime(Calendar startTime) {
        startTime.set(Calendar.SECOND, 0);
        startTime.set(Calendar.MILLISECOND, 0);
        this.startTime = (Calendar) startTime.clone();
    }

    Calendar getStartTime() {
        return startTime;
    }

    void setEndTime(Calendar endTime) {
        endTime.set(Calendar.SECOND, 0);
        endTime.set(Calendar.MILLISECOND, 0);
        this.endTime = (Calendar) endTime.clone();
    }

    Calendar getEndTime() {
        return endTime;
    }

    void setStressLevel(int stressLevel) {
        this.stressLevel = stressLevel;
    }

    int getStressLevel() {
        return stressLevel;
    }

    void setTitle(String title) {
        this.title = title;
    }

    String getTitle() {
        return title;
    }

    void setIntervention(String intervention) {
        this.intervention = intervention;
    }

    String getIntervention() {
        return intervention;
    }

    void setStressType(String stressType) {
        this.stressType = stressType;
    }

    String getStressType() {
        return stressType;
    }

    void setStressCause(String stressCause) {
        this.stressCause = stressCause;
    }

    String getStressCause() {
        return stressCause;
    }

    void setRepeatMode(int repeatMode) {
        this.repeatMode = repeatMode;
    }

    int getRepeatMode() {
        return repeatMode;
    }

    void setInterventionReminder(short interventionReminder) {
        this.interventionReminder = interventionReminder;
    }

    short getInterventionReminder() {
        return interventionReminder;
    }

    JSONObject toJson() {
        JSONObject eventJson = new JSONObject();

        try {
            eventJson.put("id", getEventId());
            eventJson.put("title", getTitle());
            eventJson.put("stressLevel", getStressLevel());
            eventJson.put("startTime", getStartTime().getTimeInMillis());
            eventJson.put("endTime", getEndTime().getTimeInMillis());
            eventJson.put("intervention", getIntervention());
            eventJson.put("interventionReminder", getInterventionReminder());
            eventJson.put("stressType", getStressType());
            eventJson.put("stressCause", getStressCause());
            eventJson.put("repeatMode", getRepeatMode());
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return eventJson;
    }

    void fromJson(JSONObject eventJson) {
        try {
            Calendar startTime = Calendar.getInstance(), endTime = Calendar.getInstance();
            startTime.setTimeInMillis(eventJson.getLong("startTime"));
            endTime.setTimeInMillis(eventJson.getLong("endTime"));

            id = eventJson.getLong("id");
            setTitle(eventJson.getString("title"));
            setStressLevel(eventJson.getInt("stressLevel"));
            setStartTime(startTime);
            setEndTime(endTime);
            setIntervention(eventJson.getString("intervention"));
            setInterventionReminder((short) eventJson.getInt("interventionReminder"));
            setStressType(eventJson.getString("stressType"));
            setStressCause(eventJson.getString("stressCause"));
            setRepeatMode(eventJson.getInt("repeatMode"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
