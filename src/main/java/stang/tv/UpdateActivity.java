package stang.tv;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class UpdateActivity extends AppCompatActivity implements View.OnClickListener{
    public static final String TAG = "MY_APP";
    public final static String BROADCAST_ACTION = "stang.tv.updateservicebroadcast";
    BroadcastReceiver br;
    TextView textViewUpdate;
    ProgressBar progressBar;
    Button buttonStart;
    Button buttonStop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

        textViewUpdate = (TextView) findViewById(R.id.textViewUpdate);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setMax(100);

        buttonStart = (Button) findViewById(R.id.buttonStart);
        buttonStop = (Button) findViewById(R.id.buttonStop);

        buttonStart.setOnClickListener(this);
        buttonStop.setOnClickListener(this);

        // создаем BroadcastReceiver
        br = new BroadcastReceiver() {
            // действия при получении сообщений
            public void onReceive(Context context, Intent intent) {
                String status = intent.getStringExtra("status");
                Integer perc = intent.getIntExtra("percent", 100);
                if(status != null && !status.equals("")){
                    //Log.d(TAG, "onReceive: status = " + status);
                    textViewUpdate.setText(status);
                }
                progressBar.setProgress(perc);

            }
        };
        // создаем фильтр для BroadcastReceiver
        IntentFilter intFilt = new IntentFilter(BROADCAST_ACTION);
        // регистрируем BroadcastReceiver
        registerReceiver(br, intFilt);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // дерегистрируем (выключаем) BroadcastReceiver
        unregisterReceiver(br);

        //Intent i = new Intent(this, MainActivity.class);
        //startActivity(i);
        //finish();
    }

    @Override
    public void onClick(View v) {
        Intent serviceIntent;
        switch(v.getId()){

            case R.id.buttonStart: /** Start update */
                serviceIntent = new Intent(this, UpdateService.class);
                serviceIntent.putExtra("action", "update");
                startService(serviceIntent);
                break;

            case R.id.buttonStop: /** Stop update */
                //MyAlertDialog();
                //stopService(new Intent(this, UpdateService.class));
                serviceIntent = new Intent(this, UpdateService.class);
                serviceIntent.putExtra("action", "stop");
                startService(serviceIntent);
                break;
        }

    }
}
