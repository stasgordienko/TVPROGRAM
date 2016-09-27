package stang.tv;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Set;

public class ChannelsActivity extends AppCompatActivity {
    public static final String TAG = "MY_APP";

    ListView listView;
    ArrayList<ChannelItem> channels;
    String category;
    Boolean favorites;
    DBHelper dbHelper;
    Set<String> fav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channels);

        dbHelper = new DBHelper(getApplicationContext());

        fav = dbHelper.getFavorites();

        Intent i = getIntent();
        favorites = i.getBooleanExtra("favorites", false);
        category = i.getStringExtra("category");
        if(category != null && !category.equals("")){
            channels = dbHelper.getChannels("category", category);
        }
        else if(favorites == true){
            channels = dbHelper.getChannels("favorites", "");
        } else {
            channels = dbHelper.getChannels("", "");
        }

        listView = (ListView) findViewById(R.id.channellistView);
        ChannelItemAdapter adapter = new ChannelItemAdapter(getApplicationContext(), channels, fav);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(ChannelsActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("channel", channels.get(position).id);
                startActivity(intent);
                //setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        dbHelper.storeFavorites(fav);
        super.onDestroy();
    }
}
