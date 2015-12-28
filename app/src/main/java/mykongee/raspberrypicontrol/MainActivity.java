package mykongee.raspberrypicontrol;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends Activity {

    private final String LOG_TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button blinkButton = (Button) findViewById(R.id.blink_button);
        blinkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(LOG_TAG, "clicked");
                BlinkTask blinkTask = new BlinkTask();
                blinkTask.execute();
            }
        });

        Button next = (Button) findViewById(R.id.to_alarm_light);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), DateTimeActivity.class);
                startActivity(intent);

            }
        });
    }

    class BlinkTask extends AsyncTask<Void, Void, Void> {
        HttpURLConnection httpURLConnection;
        BufferedReader reader;

        @Override
        protected Void doInBackground(Void... params){
            try {
                final String BASE_URL = "http://192.168.1.21:3000/light";
                Uri builtUri = Uri.parse(BASE_URL).buildUpon().build();

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
