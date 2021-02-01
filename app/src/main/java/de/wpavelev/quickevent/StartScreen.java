package de.wpavelev.quickevent;

import android.Manifest;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.wpavelev.fastevent.R;

public class StartScreen extends AppCompatActivity {


    public final static String TAG = "StartScreen";

    ArrayList<String> adapterlist;
    ListAdapter adapter;
    ListView list;

    TextView tv_startTime, tv_endTime, tv_Title, tv_Desc, tv_calId;

    HashMap<String, Calendar> map;

    int calId = -1; // -1 = calendar ist not set
    String calName = "";
    String eventTitle = "";
    String eventDescription = "";


    History history = new History();
    private int startMinute, endMinute, startHour, endHour;
    private int delayDay = 0;


    @Override
    protected void onRestart() {
        super.onRestart();

        //loadingSharedPref();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_screen);


        checkPermission();

        list = findViewById(R.id.listView);

        adapterlist = new ArrayList<>();
        loadingSharedPref();

        map = new HashMap<>();


        if (calId == -1) {
            showCalendarDialog();
        }

        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");

        CalendarView cal = findViewById(R.id.calendarView2);

        Calendar test = Calendar.getInstance();
        test.set(2018, 12, 04);

        cal.setDate(test.getTimeInMillis());

        cal.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView calendarView, int year, int month, int day) {

                //<editor-fold desc="CalendarOnChange">
                Calendar calendar;
                String dateString;


                calendar = Calendar.getInstance();
                calendar.set(year, month, day, startHour, startMinute);
                dateString = simpleDateFormat.format(calendar.getTime());

                addOrRemove(dateString, calendar);

                updateAdapter();
                //</editor-fold>

            }
        });

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, adapterlist);

        list.setAdapter(adapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                List<String> temp = new ArrayList<>(map.keySet());
                String date = temp.get(position);
                addOrRemove(date, null);
                updateAdapter();

            }
        });



    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;

    }

    private void showInfo(Context context) {
        // custom dialog
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_info);
        dialog.setTitle("Title...");

        // set the custom dialog components - text, image and button
        tv_startTime = dialog.findViewById(R.id.tv_StartTime);
        tv_endTime = dialog.findViewById(R.id.tv_EndTime);
        tv_calId = dialog.findViewById(R.id.tv_calid);
        tv_Title = dialog.findViewById(R.id.tv_title);
        tv_Desc = dialog.findViewById(R.id.tv_desc);

        tv_startTime.setText("Start Time: " + startHour + ":" + startMinute);
        tv_endTime.setText("End Time: " + endHour + ":" + endMinute);
        tv_Title.setText("Title: " + eventTitle);
        tv_Desc.setText("Description: " + eventDescription);
        tv_calId.setText("Calendar ID:" + calId);
        tv_startTime.setText("Start Time: " + startHour + ":" + startMinute);
        tv_endTime.setText("Start Time: " + endHour + ":" + endMinute);

        Button dialogButton = dialog.findViewById(R.id.dialogButtonOK);
        // if button is clicked, close the custom dialog
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.info:
                showInfo(this);
                return true;

            case R.id.insert:

                insertAllEvents();
                return true;

            case R.id.selCal:
                showCalendarDialog();
                return true;

            case R.id.undo:
                restoreLastAction();
                updateAdapter();
                return true;

            case R.id.setting:
                Intent intent = new Intent(this, Settings.class);
                intent.putExtra(getString(R.string.event_title), eventTitle);
                intent.putExtra(getString(R.string.event_desc), eventDescription);
                intent.putExtra(getString(R.string.event_start_hour), startHour);
                intent.putExtra(getString(R.string.event_end_hour), endHour);
                intent.putExtra(getString(R.string.event_start_minute), startMinute);
                intent.putExtra(getString(R.string.event_end_minute), endMinute);

                startActivityForResult(intent, 0);
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == 0) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {

                eventTitle = data.getStringExtra(getString(R.string.event_title));
                eventDescription = data.getStringExtra(getString(R.string.event_desc));
                startMinute = data.getIntExtra(getString(R.string.event_start_minute),0);
                endMinute = data.getIntExtra(getString(R.string.event_end_minute),0);
                startHour = data.getIntExtra(getString(R.string.event_start_hour),0);
                endHour = data.getIntExtra(getString(R.string.event_end_hour),0);



            }
        }

        savingSharedPref();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        savingSharedPref();

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        outState.putStringArrayList(getString(R.string.adapter_list), adapterlist);

        outState.putInt(getString(R.string.calendar_id),calId);
        outState.putString(getString(R.string.event_title), eventTitle);
        outState.putString(getString(R.string.event_desc), eventDescription);
        outState.putInt(getString(R.string.event_start_hour), startHour);
        outState.putInt(getString(R.string.event_start_minute), startMinute);
        outState.putInt(getString(R.string.event_end_hour), endHour);
        outState.putInt(getString(R.string.event_end_minute), endMinute);


        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {

        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState.getStringArrayList(getString(R.string.adapter_list)) != null) {
            adapterlist = new ArrayList<>(savedInstanceState.getStringArrayList(getString(R.string.adapter_list)));
        } else {
            adapterlist = new ArrayList<>();
        }

        calId = savedInstanceState.getInt(getString(R.string.calendar_id), 0);
        eventTitle = savedInstanceState.getString(getString(R.string.event_title), "");
        eventDescription = savedInstanceState.getString(getString(R.string.event_desc), "");

        startHour = savedInstanceState.getInt(getString(R.string.event_start_hour), 0);
        startMinute = savedInstanceState.getInt(getString(R.string.event_start_minute), 0);
        endHour = savedInstanceState.getInt(getString(R.string.event_end_hour), 0);
        endMinute = savedInstanceState.getInt(getString(R.string.event_end_minute), 0);

    }

    /**
     * Fügt das Datum ein, wenn es noch nicht existiert und verarbeitet Verlauf
     * Wenn Datum bereits vorhanden, aus der Liste entfernen
     * @param dateString name des Datums
     * @param calendar Datum
     */
    private void addOrRemove(String dateString, Calendar calendar) {
        if (!map.containsKey(dateString) && calendar != null) {
            //wenn das Datum nicht in der Liste ist
            //das Datum hinzufügen
            history.addAction(ActionType.ADD, calendar, dateString);
            map.put(dateString, calendar);


        } else {
            //Ansonsten wieder entfern
            map.remove(dateString);
            history.addAction(ActionType.REMOVE, map.get(dateString), dateString);

        }

    }

    public void setCalId(int calId) {
        this.calId = calId;
    }

    public void setCalName(String name) {
        this.calName = name;
    }

    private void savingSharedPref() {
        SharedPreferences sharedPref = this.getPreferences(this.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putInt(getString(R.string.calendar_id),calId);
        editor.putString(getString(R.string.event_title), eventTitle);
        editor.putString(getString(R.string.event_desc), eventDescription);
        editor.putInt(getString(R.string.event_start_hour), startHour);
        editor.putInt(getString(R.string.event_start_minute), startMinute);
        editor.putInt(getString(R.string.event_end_hour), endHour);
        editor.putInt(getString(R.string.event_end_minute), endMinute);



        editor.commit();
    }

    private void loadingSharedPref() {
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        if (sharedPref != null) {
            calId = sharedPref.getInt(getString(R.string.calendar_id), 0);
            eventTitle = sharedPref.getString(getString(R.string.event_title), "");
            eventDescription = sharedPref.getString(getString(R.string.event_desc), "");

            startHour = sharedPref.getInt(getString(R.string.event_start_hour), 0);
            startMinute = sharedPref.getInt(getString(R.string.event_start_minute), 0);
            endHour = sharedPref.getInt(getString(R.string.event_end_hour), 0);
            endMinute = sharedPref.getInt(getString(R.string.event_end_minute), 0);

        }



    }

    private void showCalendarDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cal-ID: " + calId);

        final Map<String, Integer> calendarList = new HashMap<>(getCalendars());

        final ArrayAdapter<String> dialogAdapter = new ArrayAdapter<String>(StartScreen.this, android.R.layout.select_dialog_singlechoice);
        for (String s : calendarList.keySet()) {
            dialogAdapter.add(s);
        }


        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.setAdapter(dialogAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                setCalId(calendarList.get(dialogAdapter.getItem(i)));
                setCalName(dialogAdapter.getItem(i));
                tv_calId.setText("calId: " + calId);

            }
        });


        Log.i("TAG", "calId: " + calId);
        builder.show();
    }



    void updateAdapter() {
        adapterlist = new ArrayList<>(map.keySet());
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, adapterlist);
        list.setAdapter(adapter);

    }

    private void restoreLastAction() {
        Action action = new Action(history.restoreLastAction());
        switch (action.getActionType()) {
            case ADD:
                if (map.containsKey(action.getDatum())) {
                    map.remove(action.getDatum());
                }
                updateAdapter();

                break;

            case REMOVE:
                if (!map.containsKey(action.getDatum())) {
                    map.put(action.getDatum(), action.getCalendar());
                }

                break;
        }

    }

    private void checkPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_CALENDAR}, 0);
        }
    }

    /**
     *
     * @return Map of Calendars with <Name, Id>
     */
    private Map<String, Integer> getCalendars() {

        Map<String, Integer> map = new HashMap<>();

        Cursor cursor;

        if (android.os.Build.VERSION.SDK_INT <= 7) {
            cursor = getContentResolver().query(Uri.parse("content://calendar/calendars"),
                    new String[]{"_id", "displayName"}, null,
                    null, null);

        } else if (android.os.Build.VERSION.SDK_INT <= 14) {
            cursor = getContentResolver().query(Uri.parse("content://com.android.calendar/calendars"),
                    new String[]{"_id", "displayName"}, null, null, null);

        } else {
            cursor = getContentResolver().query(Uri.parse("content://com.android.calendar/calendars"),
                    new String[]{"_id", "calendar_displayName"}, null, null, null);

        }

        // Get calendars name
        Log.i("INFO", "Cursor count " + cursor.getCount());
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            for (int i = 0; i < cursor.getCount(); i++) {
                int calendarId = cursor.getInt(0);
                String calendarName = cursor.getString(1);
                map.put(calendarName, calendarId);
                cursor.moveToNext();
            }
        } else {
            Log.e("ERROR!", "No calendar found in the device");
        }

        cursor.close();
        return map;

    }

    private void insertEvent(Calendar event) {


        Calendar beginTime = event;
        long beginTimeMillis = beginTime.getTimeInMillis();

        Calendar endTime = event;
        endTime.set(Calendar.HOUR_OF_DAY, endHour);
        endTime.set(Calendar.MINUTE, endMinute);

        endTime.add(Calendar.DAY_OF_MONTH, delayDay);
        long endTimeMillis = endTime.getTimeInMillis();


        ContentResolver cr = getContentResolver();
        ContentValues values = new ContentValues();
        values.put(CalendarContract.Events.DTSTART, beginTimeMillis);
        values.put(CalendarContract.Events.DTEND, endTimeMillis);
        values.put(CalendarContract.Events.TITLE, this.eventTitle);
        values.put(CalendarContract.Events.DESCRIPTION, this.eventDescription);
        values.put(CalendarContract.Events.CALENDAR_ID, this.calId);
        values.put(CalendarContract.Events.EVENT_TIMEZONE, "Europe/Berlin");

        Uri uri = null;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED) {
            uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);
        }



        long eventID = Long.parseLong(uri.getLastPathSegment());


    }

    private void insertAllEvents() {
        Set<String> mapsKeys = map.keySet();
        for (String mapsKey : mapsKeys) {
            insertEvent(map.get(mapsKey));
        }

    }


    class History {

        LinkedList<Action> actionList = new LinkedList<>();

        public void addAction(ActionType type, Calendar calendar, String dateString) {
            actionList.add(new Action(type, calendar, dateString));
        }

        public Action restoreLastAction() {
            Action action = new Action(actionList.getLast());
            actionList.removeLast();

            return action;
        }


    }

    class Action {

        ActionType type;

        private ActionType actionType;
        private Calendar calendar;
        private String datum = "";

        public Action(ActionType actionType, Calendar calendar, String datum) {
            this.actionType = actionType;
            this.calendar = calendar;
            this.datum = datum;
        }

        public Action(Action action) {
            this.actionType = action.getActionType();
            this.calendar = action.getCalendar();
            this.datum = action.getDatum();
        }

        public ActionType getActionType() {
            return actionType;
        }

        public Calendar getCalendar() {
            return calendar;
        }

        public String getDatum() {
            return datum;
        }
    }

    public enum ActionType
    {
        ADD, REMOVE;
    }


}
