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
import android.widget.ListView;

import java.util.ArrayList;

public class CategoriesActivity extends AppCompatActivity {
    ListView listView;
    ArrayList<String> categories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);
        DBHelper dbHelper = new DBHelper(getApplicationContext());
        categories = dbHelper.getCategories();

        listView = (ListView) findViewById(R.id.lvCategory);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.date_item, categories);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Intent i = new Intent(CategoriesActivity.this, ChannelsActivity.class);
                i.putExtra("category", categories.get(position));
                startActivity(i);
                finish();
            }
        });

    }
}
