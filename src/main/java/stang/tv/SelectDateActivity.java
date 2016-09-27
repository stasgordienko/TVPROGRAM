package stang.tv;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;

public class SelectDateActivity extends AppCompatActivity {
    public static final String TAG = "MY_APP";
    ListView dateListView;
    Button updateButton;
    DBHelper dbHelper;
    ArrayList<String> dates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_date);

        dateListView = (ListView) findViewById(R.id.dateListView);
        updateButton = (Button) findViewById(R.id.selectButton);
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Открываеи UpdateActivity
                Intent i = new Intent(SelectDateActivity.this, UpdateActivity.class);
                startActivity(i);
            }
        });

        dbHelper = new DBHelper(getApplicationContext());
        dates = dbHelper.getDates();

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.date_item, dates);
        dateListView.setAdapter(adapter);

        dateListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                pref.edit().putString("dateToShow", dates.get(position)).commit();
                Log.d(TAG, "SelectDateActivity dateToShow=" + dates.get(position));
                Intent intent = new Intent(SelectDateActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                //intent.putExtra("date", dates.get(position));
                //setResult(RESULT_OK, intent);
                finish();
            }
        });

    }

    @Override
    protected void onDestroy() {
        Intent intent = new Intent(SelectDateActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        //intent.putExtra("date", dates.get(position));
        //setResult(RESULT_OK, intent);
        //finish();
        super.onDestroy();
    }
}
