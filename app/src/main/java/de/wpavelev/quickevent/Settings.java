package de.wpavelev.quickevent;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import de.wpavelev.fastevent.R;

public class Settings extends FragmentActivity {

    Button start, end, okbutton;
    EditText et_Title, et_Desc;
    String[] timeName = new String[]{"Start Time", "End Time"};

    int startHour, endHour;
    int startMinute, endMinute;
    int selectedTime = 0; //0 = start, 1 = ende

    String desc, title;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        start = findViewById(R.id.startTimePicker);
        end = findViewById(R.id.endtimepicker);
        okbutton = findViewById(R.id.button2);
        et_Title = findViewById(R.id.editText1);
        et_Desc = findViewById(R.id.editText2);

        Intent intent = getIntent();
        title = intent.getStringExtra(getString(R.string.event_title));
        desc = intent.getStringExtra(getString(R.string.event_desc));

        startHour = intent.getIntExtra(getString(R.string.event_start_hour), 0);
        startMinute = intent.getIntExtra(getString(R.string.event_start_minute), 0);
        endHour = intent.getIntExtra(getString(R.string.event_end_hour), 0);
        endMinute = intent.getIntExtra(getString(R.string.event_end_minute), 0);


        if (startMinute == 0) {
            start.setText("Event Start: " + startHour + ":" + startMinute + "0");
        } else {
            start.setText("Event Start: " + startHour + ":" + startMinute);
        }


        if (endMinute == 0) {
            end.setText("Event Start: " + endHour + ":" + endMinute + "0");
        } else {
            end.setText("Event Start: " + endHour + ":" + endMinute);
        }



        et_Desc.setText(desc);
        et_Title.setText(title);

        okbutton.setOnClickListener(new Click());
        start.setOnClickListener(new Click());
        end.setOnClickListener(new Click());


    }


    private void sendData() {
        Intent returnIntent = new Intent();
        title = et_Title.getText().toString();
        desc = et_Desc.getText().toString();
        returnIntent.putExtra(getString(R.string.event_title), title);
        returnIntent.putExtra(getString(R.string.event_desc), desc);

        returnIntent.putExtra(getString(R.string.event_start_hour), startHour);
        returnIntent.putExtra(getString(R.string.event_end_hour), endHour);
        returnIntent.putExtra(getString(R.string.event_start_minute), startMinute);
        returnIntent.putExtra(getString(R.string.event_end_minute), endMinute);

        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }


    public void setTime(int hour, int minute) {

        if (selectedTime == 0) {
            this.startHour = hour;
            this.startMinute = minute;
        } else {
            this.endHour = hour;
            this.endMinute = minute;
        }

    }


    public void showTimePickerDialog(View v) {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getFragmentManager(), "TimePicker");

    }

    class Click implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.button2:
                    sendData();

                    break;
                case R.id.startTimePicker:
                    selectedTime = 0;
                    showTimePickerDialog(getCurrentFocus());
                    break;
                case R.id.endtimepicker:
                    selectedTime = 1;
                    showTimePickerDialog(getCurrentFocus());
                    break;
            }
        }
    }


}

