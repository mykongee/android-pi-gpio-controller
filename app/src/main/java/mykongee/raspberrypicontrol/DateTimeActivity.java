package mykongee.raspberrypicontrol;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.datetimepicker.date.DatePickerDialog;
import com.android.datetimepicker.time.RadialPickerLayout;
import com.android.datetimepicker.time.TimePickerDialog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DateTimeActivity extends AppCompatActivity implements
        DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener{

    private static final String TIME_PATTERN = "HH:mm";
    private final String LOG_TAG = DateTimeActivity.class.getSimpleName();

    private TextView dateView;
    private TextView timeView;
    private Calendar calendar;
    private DateFormat dateFormat;
    private SimpleDateFormat simpleDateFormat;
    private String queryFormatDate;
    private String queryFormatTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_date_time);

        calendar = Calendar.getInstance();
        dateFormat = DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault());
        simpleDateFormat = new SimpleDateFormat(TIME_PATTERN, Locale.getDefault());

        dateView = (TextView) findViewById(R.id.lblDate);
        timeView = (TextView) findViewById(R.id.lblTime);
        Button alarmButton = (Button) findViewById(R.id.btnAlarm);

        alarmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlarmLightTask alarmLightTask = new AlarmLightTask();
                Log.v(LOG_TAG, queryFormatDate + " " + queryFormatTime);
                alarmLightTask.execute(queryFormatDate + " " + queryFormatTime);
                Intent intent = new Intent();
                //intent.setAction(AlarmClock.ACTION_SET_ALARM);
                startActivity(intent);
            }
        });

        update();

    }

    private void update() {
        dateView.setText(dateFormat.format(calendar.getTime()));
        timeView.setText(simpleDateFormat.format(calendar.getTime()));
    }

    public void onClick(View v){
        switch (v.getId()) {
            case R.id.btnDatePicker:
                DatePickerDialog.newInstance(this,
                        calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)).
                        show(getFragmentManager(), "datePicker");
                break;
            case R.id.btnTimePicker:
                TimePickerDialog.newInstance(this,
                        calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE),
                        true).show(getFragmentManager(), "timePicker");
                break;
        }
    }

    @Override
    public void onDateSet(DatePickerDialog dialog, int year, int monthOfYear, int dayOfMonth) {
        calendar.set(year, monthOfYear, dayOfMonth);
        // Store String-formatted date for API query
        queryFormatDate = Integer.toString(year) + "-" + Integer.toString(monthOfYear + 1) +
        "-" + Integer.toString(dayOfMonth);
        update();
    }

    @Override
    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        // Store String-formatted time for API query
        queryFormatTime = Integer.toString(hourOfDay) + ":" + Integer.toString(minute);
        update();
    }

    class AlarmLightTask extends AsyncTask<String, Void, Void> {
        HttpURLConnection httpURLConnection;
        BufferedReader reader;

        @Override
        protected Void doInBackground(String... params){
            try {
                final String BASE_URL = Constants.ALARM_BASE_URL;
                Uri builtUri = Uri.parse(BASE_URL).buildUpon().
                        appendQueryParameter("time", params[0]).
                        build();

                URL url = new URL(builtUri.toString());
                Log.v(LOG_TAG, "connected to " + url);
                httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.connect();

                InputStream in = httpURLConnection.getInputStream();
                StringBuffer stringBuffer = new StringBuffer();
                if (in == null) {
                    return null;
                }

                reader = new BufferedReader(new InputStreamReader(in));

                String line;

                while ((line = reader.readLine()) != null){
                    stringBuffer.append(line + "/n");
                }

                Log.v(LOG_TAG, " " + stringBuffer.toString());

                if (stringBuffer.length() == 0){
                    return null;
                }

            } catch (Exception e){
                e.printStackTrace();
            } finally {
                Log.v(LOG_TAG, "disconnected");
                httpURLConnection.disconnect();
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e){
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result){
        }
    }
}
