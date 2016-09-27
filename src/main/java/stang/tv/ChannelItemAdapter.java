package stang.tv;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Administrator on 25.09.2016.
 */

public class ChannelItemAdapter extends BaseAdapter {
    public static final String TAG = "MY_APP";

    Context ctx;
    LayoutInflater lInflater;
    ArrayList<ChannelItem> objects;
    public Set<String> favorites;

    ChannelItemAdapter(Context context, ArrayList<ChannelItem> items, Set<String> fav) {
        ctx = context;
        objects = items;
        favorites = fav;
        lInflater = (LayoutInflater) ctx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    // кол-во элементов
    @Override
    public int getCount() {
        return objects.size();
    }

    // элемент по позиции
    @Override
    public Object getItem(int position) {
        return objects.get(position);
    }

    // id по позиции
    @Override
    public long getItemId(int position) {
        return position;
    }

    // пункт списка
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ImageView iv;

        // используем созданные, но не используемые view
        View view = convertView;
        if (view == null) {
            view = lInflater.inflate(R.layout.channel_item, parent, false);
        }

        ChannelItem item = getChannelItem(position);

        int icon = R.drawable.star_off;
        if (favorites.contains(item.id)) {
            icon = R.drawable.star_on;
        }

        // заполняем View в пункте списка данными из program
        ((TextView) view.findViewById(R.id.tvChName)).setText(item.name);
        ((TextView) view.findViewById(R.id.tvCatName)).setText(item.category);
        ((TextView) view.findViewById(R.id.tvURL)).setText(item.tvURL);
        iv = (ImageView) view.findViewById(R.id.ivImage);
        iv.setImageResource(icon);
        iv.setContentDescription(item.id);
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (favorites.contains(iv.getContentDescription())){
                    iv.setImageResource(R.drawable.star_off);
                    favorites.remove(String.valueOf(iv.getContentDescription()));
                } else {
                    iv.setImageResource(R.drawable.star_on);
                    favorites.add(String.valueOf(iv.getContentDescription()));
                }
            }
        });

        return view;
    }

    // showItem по позиции
    ChannelItem getChannelItem(int position) {
        return ((ChannelItem) getItem(position));
    }

}