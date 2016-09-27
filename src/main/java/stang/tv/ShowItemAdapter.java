package stang.tv;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.content.Context;
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

public class ShowItemAdapter extends BaseAdapter {
    Context ctx;
    LayoutInflater lInflater;
    ArrayList<ShowItem> objects;

    ShowItemAdapter(Context context, ArrayList<ShowItem> items) {
        ctx = context;
        objects = items;
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
        // используем созданные, но не используемые view
        View view = convertView;
        if (view == null) {
            view = lInflater.inflate(R.layout.show_item, parent, false);
        }

        ShowItem item = getShowItem(position);

        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm  dd.MM.yyyy");

        String time = "";
        try{
            time = dateFormat.format( new Date(Long.valueOf(item.time)));
        } catch (Exception e) {
            time = "00:00";
        }


        // заполняем View в пункте списка данными из program
        ((TextView) view.findViewById(R.id.tvDate)).setText(time);
        ((TextView) view.findViewById(R.id.tvShowName)).setText(item.tvShowName + "");
        //((ImageView) view.findViewById(R.id.ivImage)).setImageResource(p.image);

        return view;
    }

    // showItem по позиции
    ShowItem getShowItem(int position) {
        return ((ShowItem) getItem(position));
    }

}