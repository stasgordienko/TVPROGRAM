package stang.tv;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Administrator on 24.09.2016.
 */
public class DBHelper extends SQLiteOpenHelper {
    public static final String TAG = "MY_APP";
    private Context context;

    private static final int DATABASE_VERSION = 1;

    private static final String DBNAME = "base";
    private static final String CATEG = "category";
    private static final String CHAN = "chanel";
    private static final String PROG = "program";
    private static final String DATE = "date";


    public DBHelper(Context context) {
        super(context, DBNAME, null, 1);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "--- onCreate database ---");
        // создаем таблицы
        db.execSQL("CREATE TABLE channel ("
                + "_id INTEGER primary key autoincrement,"
                + "id TEXT,"
                + "name TEXT,"
                + "tvURL TEXT,"
                + "category TEXT" + ");");

        db.execSQL("CREATE TABLE category ("
                + "_id INTEGER primary key autoincrement,"
                + "name TEXT"
                + ");");

        db.execSQL("CREATE TABLE date ("
                + "_id INTEGER primary key autoincrement,"
                + "date TEXT"
                + ");");

        db.execSQL("CREATE TABLE program ("
                + "_id INTEGER primary key autoincrement,"
                + "date TEXT,"
                + "time LONG,"
                + "showID TEXT,"
                + "tvShowName TEXT"
                + ");");

        db.execSQL("CREATE TABLE favorite ("
                + "_id INTEGER primary key autoincrement,"
                + "id TEXT" + ");");

        Log.d(TAG, "--- onCreate FINISH ---");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "--- onUpgrade database ---");
        db.execSQL("DROP TABLE IF EXISTS channel");
        db.execSQL("DROP TABLE IF EXISTS category");
        db.execSQL("DROP TABLE IF EXISTS program");
        db.execSQL("DROP TABLE IF EXISTS date");
        db.execSQL("DROP TABLE IF EXISTS favorite");
        onCreate(db);
    }

    public void storeFavorites(Set<String> fav) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete("favorite", null, null);
        for (String f:fav) {
            ContentValues cv = new ContentValues();
            cv.put("id", f);
            db.insert("favorite", null, cv);
        }
    }

    public Set<String> getFavorites(){
        Set<String> fav = new HashSet<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(true, "favorite", null, null, null, null, null, null, null);
        String id;
        if (c.moveToFirst()) {
            int idColIndex = c.getColumnIndex("id");
            do {
                id = c.getString(idColIndex);
                fav.add(id);
            } while (c.moveToNext());
        }
        Log.d(TAG, "read favorites: " + fav.size() + ";");
        c.close();
        db.close();
        return fav;
    }

    public ArrayList<ChannelItem> getChannels(String filter, String arg) {
        Set<String> fav = getFavorites();
        SQLiteDatabase db = getReadableDatabase();
        Boolean getOnlyFavorites = false;

        String selection = null;
        if(filter.equals("category")){
            selection = filter + "=?";
        }
        else if(filter.equals("favorites")) {
            getOnlyFavorites=true;
        }

        // Specify arguments in placeholder order.
        String[] selectionArgs = null;
        if(!arg.equals("")) {
            selectionArgs = new String[]{String.valueOf(arg)};
        }
        // Issue SQL statement.
        String groupBy = "id";

        String strOrder = "id";// + " DESC";

        Cursor c = db.query(true, "channel",
                null,
                selection,
                selectionArgs,
                groupBy,
                null,
                strOrder,
                null
        );

        ArrayList<ChannelItem> channels = new ArrayList<ChannelItem>();

        String id;
        String name;
        String tvURL;
        String category;
        // ставим позицию курсора на первую строку выборки
        // если в выборке нет строк, вернется false
        if (c.moveToFirst()) {

            // определяем номера столбцов по имени в выборке
            int idColIndex = c.getColumnIndex("id");
            int nameColIndex = c.getColumnIndex("name");
            int tvURLColIndex = c.getColumnIndex("tvURL");
            int categoryColIndex = c.getColumnIndex("category");

            do {
                // получаем значения по номерам столбцов
                id = c.getString(idColIndex);
                name = c.getString(nameColIndex);
                tvURL = c.getString(tvURLColIndex);
                category = c.getString(categoryColIndex);

                ChannelItem ch = new ChannelItem(id, name, tvURL, category);

                if(getOnlyFavorites) {
                    if(fav.contains(id)){
                        channels.add(ch);
                    }
                } else {
                    channels.add(ch);
                }

                // переход на следующую строку
                // а если следующей нет (текущая - последняя), то false - выходим из цикла
            } while (c.moveToNext());
        }
        Log.d(TAG, "read channels: " + channels.size() + ";");
        c.close();

        // закрываем подключение к БД
        db.close();
        return channels;
    }

    public ArrayList<String> getProgramChannels(String date) {
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<String> channels = new ArrayList<String>();

        // Define 'where' part of query.
        String selection = "date=?";
        // Specify arguments in placeholder order.
        String[] selectionArgs = { String.valueOf(date)};
        // Issue SQL statement.
        String groupBy = "showID";

        String strOrder = "showID";// + " DESC";

        Cursor c = db.query(true, "program",
                null,
                selection,
                selectionArgs,
                groupBy,
                null,
                strOrder,
                null
        );

        String showID;

        // ставим позицию курсора на первую строку выборки
        // если в выборке нет строк, вернется false
        if (c.moveToFirst()) {
            int showIDColIndex = c.getColumnIndex("showID");
            do {
                showID = c.getString(showIDColIndex);
                if(showID != null && !showID.equals("")){
                    channels.add(showID);
                }
            } while (c.moveToNext());
        }
        Log.d(TAG, "read channels in programs for date: " + channels.size() + ";");
        c.close();
        db.close();
        return channels;
    }

    public ArrayList<ShowItem> getPrograms(String date, String channelId){
        SQLiteDatabase db = getReadableDatabase();

        // Define 'where' part of query.
        String selection = "date=? and showID=?";
        // Specify arguments in placeholder order.
        String[] selectionArgs = { String.valueOf(date), String.valueOf(channelId)};
        // Issue SQL statement.
        String strOrder = "time"; // + " DESC";

        Cursor c = db.query(true, "program",
                null,
                selection,
                selectionArgs,
                null,
                null,
                strOrder,
                null
        );

        ArrayList<ShowItem> programs = new ArrayList<ShowItem>();

        Long time;
        //String showID;
        String tvShowName;
        // ставим позицию курсора на первую строку выборки
        // если в выборке нет строк, вернется false
        if (c.moveToFirst()) {

            // определяем номера столбцов по имени в выборке
            int timeColIndex = c.getColumnIndex("time");
            //int showIDColIndex = c.getColumnIndex("showID");
            int tvShowNameColIndex = c.getColumnIndex("tvShowName");

            do {
                // получаем значения по номерам столбцов
                time = c.getLong(timeColIndex);
                //showID = c.getString(showIDColIndex);
                tvShowName = c.getString(tvShowNameColIndex);

                ShowItem ch = new ShowItem(time, tvShowName);
                programs.add(ch);

                // переход на следующую строку
                // а если следующей нет (текущая - последняя), то false - выходим из цикла
            } while (c.moveToNext());
        }
        Log.d(TAG, "read programs: " + programs.size() + "; chanelID="+channelId+"; date=" + date);
        c.close();

        // закрываем подключение к БД
        db.close();
        return programs;

    }

    public ArrayList<String> getDates() {
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<String> dates = new ArrayList<String>();

        // Define 'where' part of query.
        String selection = "";
        // Specify arguments in placeholder order.
        String[] selectionArgs = {};
        // Issue SQL statement.
        String groupBy = "date";

        String strOrder = "date";// + " DESC";

        Cursor c = db.query(true, "date",
                null,
                selection,
                selectionArgs,
                groupBy,
                null,
                strOrder,
                null
        );

        String date;

        // ставим позицию курсора на первую строку выборки
        // если в выборке нет строк, вернется false
        if (c.moveToFirst()) {
            int dateColIndex = c.getColumnIndex("date");
            do {
                date = c.getString(dateColIndex);
                if(date != null && !date.equals("")){
                    dates.add(date);
                    Log.d(TAG, date);
                }
            } while (c.moveToNext());
        }
        Log.d(TAG, "read available dates: " + dates.size() + ";");
        c.close();
        db.close();
        return dates;
    }

    public ArrayList<String> getCategories() {
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<String> cat = new ArrayList<String>();

        // Define 'where' part of query.
        String selection = "";
        // Specify arguments in placeholder order.
        String[] selectionArgs = {};
        // Issue SQL statement.
        String groupBy = "category";

        String strOrder = "category";// + " DESC";

        Cursor c = db.query(true, "channel",
                null,
                selection,
                selectionArgs,
                groupBy,
                null,
                strOrder,
                null
        );

        String name;

        if (c.moveToFirst()) {
            int nameColIndex = c.getColumnIndex("category");
            do {
                name = c.getString(nameColIndex);
                if(name != null && !name.equals("")){
                    cat.add(name);
                    Log.d(TAG, name);
                }
            } while (c.moveToNext());
        }
        Log.d(TAG, "read categories: " + cat.size() + ";");
        c.close();
        db.close();
        return cat;
    }
}
