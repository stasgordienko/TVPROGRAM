package stang.tv;


import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Created by Administrator on 25.09.2016.
 */

public class PageFragment extends Fragment {

    static final String ARGUMENT_CHANNEL_NUMBER = "arg_channel_number";
    static final String ARGUMENT_CHANNEL_ID = "arg_channel_id";
    static Context context;
    static DBHelper dbHelper;

    int channelNumber;
    String channelId;

    int backColor;

    ArrayList<ShowItem> items;

    static PageFragment newInstance(Context _context, DBHelper _dbHelper, int page, String date, String channelId) {
        context = _context;
        dbHelper = _dbHelper;
        PageFragment pageFragment = new PageFragment();
        Bundle arguments = new Bundle();
        arguments.putInt(ARGUMENT_CHANNEL_NUMBER, page);
        arguments.putString(ARGUMENT_CHANNEL_ID, channelId);
        pageFragment.setArguments(arguments);

        pageFragment.items = dbHelper.getPrograms(date, channelId);
        return pageFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        channelNumber = getArguments().getInt(ARGUMENT_CHANNEL_NUMBER);
        channelId = getArguments().getString(ARGUMENT_CHANNEL_ID);

        Random rnd = new Random();
        backColor = Color.argb(40, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment, null);
        setHasOptionsMenu(true);

        ShowItemAdapter adapter = new ShowItemAdapter(context, items);
        ListView tvShowList = (ListView) view.findViewById(R.id.tvShowList);
        tvShowList.setAdapter(adapter);


        return view;
    }
}