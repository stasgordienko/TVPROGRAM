package stang.tv;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.IBinder;
import android.app.Service;
import android.support.annotation.Nullable;
import android.util.Log;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.JsonMappingException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Administrator on 24.09.2016.
 */
public class UpdateService extends Service{
    public static final String TAG = "MY_APP";
    private static DBHelper dbHelper;
    private static UpdateTask updateTask;
    private static boolean isUpdateRunning = false;
    private static String status = "";
    private static Integer percent = 0;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        String action = intent.getStringExtra("action");

        if(action.equals("update")){
            if(isUpdateRunning == false) {
                isUpdateRunning = true;
                Log.d(TAG, "starting update...");
                sendStatus("Starting...", 0);

                //backgroundTask();
                updateTask = new UpdateTask();
                updateTask.execute();

            } else {
                Log.d(TAG, "update is already running!");
            }
        }

        if(action.equals("stop")) {
            if(isUpdateRunning == true) {
                updateTask.cancel(false);
                sendStatus("Cancel. Stopping update...", -1);
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }


    protected void stopService(){
        sendStatus("Service Stopped", -1);
        stopSelf();
    }

    private void sendStatus(String s, Integer p){
        Intent i = new Intent(UpdateActivity.BROADCAST_ACTION);
        if( s != null ) {status = s;}
        if( p > -1 ) {percent = p;}
        i.putExtra("status", status);
        i.putExtra("percent", Integer.valueOf(percent));
        sendBroadcast(i);
    }

    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    private void onFinish(){
        isUpdateRunning = false;
        sendStatus("Task done.", 100);
    }



private class UpdateTask extends AsyncTask<Void, String, Integer> {

    @Override
    protected Integer doInBackground(Void... params) {
        update();
        return 0;
    }

    @Override
    protected void onPostExecute(Integer result) {
        isUpdateRunning = false;
        sendStatus("Update FINISHED!", 100);
        super.onPostExecute(result);
    }

    @Override
    protected void onProgressUpdate(String... values) {
        sendStatus(String.valueOf(values[0]), Integer.valueOf(values[1]));
    }

    @Override
    protected void onCancelled() {
        isUpdateRunning = false;
        sendStatus("Update CANCELLED!", -1);
        super.onCancelled();
    }

    //------------------------
    protected void update() {
        Log.d(TAG, "UpdateTask");
        if (dbHelper == null) {
            dbHelper = new DBHelper(getApplicationContext());
        }

        //Get connect params
        String linkCategory = "https://t2dev.firebaseio.com/CATEGORY.json";
        String linkChannel = "https://t2dev.firebaseio.com/CHANNEL.json";
        String linkProgram = "https://t2dev.firebaseio.com/PROGRAM.json";

        //------------------------------------
        // Getting from web channels JSON file
        //String channelJSON = getHTTP(linkChannel);
        StringBuilder buffer = new StringBuilder(2048000);
        //PROGRESS
        publishProgress(new String[]{"Getting " + linkChannel, "0"});
        getHTTP(linkChannel, buffer);

        if (buffer.length() > 0 && this.isCancelled() == false) {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.delete("channel", null, null);
            try {
                JSONObject channel = new JSONObject(new String(buffer));
                JSONArray channelNames = channel.names();

                String id;
                String name;
                String url;
                String cat;

                // Parse channels JSON
                //PROGRESS
                publishProgress(new String[]{"Parsing " + linkChannel, "10"});

                for (int i = 0; i < channel.length(); i++) {
                    JSONObject c = channel.getJSONObject(channelNames.getString(i));
                    id = c.getString("id");
                    name = c.getString("name");
                    url = c.getString("tvURL");
                    cat = "";

                    for (int j = 0; j < c.names().length(); j++) {
                        String key = c.names().getString(j);
                        if (c.getString(key).equals("true")) {
                            cat = key;
                        }
                    }

                    //STORE TO DB
                    ContentValues cv = new ContentValues();

                    cv.put("id", id);
                    cv.put("name", name);
                    cv.put("tvURL", url);
                    cv.put("category", cat);


                    long rowID = db.insert("channel", null, cv);
                    //publishProgress("STORED: " + String.valueOf(rowID));
                }

//                for (int i = 0; i < categories.size(); i++) {
//                    db.insert("category", null, cv);
//                }


                db.close();

            } catch (Exception e) {
                Log.d(TAG, e.toString());
            }
        }


        //Getting and parsing big JSON programs file
        //------------------------------------------
        getProgramJSON(linkProgram);

        //String categoryJSON = getHTTP(linkCategory);


        return;
    }


    public int getHTTP(String link, StringBuilder buffer) {
        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        try {
            // Construct the URL for the query
            URL url = new URL(link);

            // Create the request and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            //urlConnection.setReadTimeout(180000);
            urlConnection.connect();

            // Read the input stream
            InputStream inputStream = urlConnection.getInputStream();

            if (inputStream == null) {
                // Nothing to do.
                return -1;
            }

            reader = new BufferedReader(new InputStreamReader(inputStream));


            char[] buf = new char[65535];
            int count = 0;
            do {
                count = reader.read(buf, 0, 65535);
                if (count > 0) buffer.append(buf, 0, count);
            } while (count > -1 && this.isCancelled() == false);


            if (buffer.length() == 0 || this.isCancelled() == true) {
                // Stream was empty.  No point in parsing.
                return 0;
            }

            //response = buffer.toString();
        } catch (IOException e) {
            Log.e(TAG, "Error ", e);
            // If the code didn't successfully get the data, there's no point in attemping
            // to parse it.
            return -1;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(TAG, "Error closing stream", e);
                }
            }
        }

        return buffer.length();
    }


    public int getProgramJSON(String link) {

        HttpURLConnection urlConnection = null;

        //PROGRESS
        publishProgress(new String[]{"Getting " + link, "15"});

        try {
            URL url = new URL(link);

            // Create the request, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            //urlConnection.setReadTimeout(360000);
            urlConnection.connect();

            // Read the input stream
            InputStream inputStream = urlConnection.getInputStream();

            if (inputStream == null || this.isCancelled() == true) {
                // Nothing to do.
                return -1;
            }

            long rowID = 0;
            String tvShowName = "";
            String showID = "";
            String time = "";
            String date = "";

            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.delete("program", null, null);

            try {

                JsonFactory jfactory = new JsonFactory();

                /*** read from inputStream ***/
                JsonParser jParser = jfactory.createJsonParser(inputStream);

                // loop until token equal to "}"
                jParser.nextToken();
                while ((jParser.nextToken() != JsonToken.END_OBJECT) && (this.isCancelled() == false)) {
                    //Dates
                    date = jParser.getCurrentName();
                    Log.d(TAG, "DATE:" + date);

                    ContentValues dateValues = new ContentValues();
                    dateValues.put("date", date);
                    rowID = db.insert("date", null, dateValues);

                    while (jParser.nextToken() != JsonToken.END_OBJECT) {
                        //Channel-Dates
                        String cDateString = jParser.getCurrentName();
                        while (jParser.nextToken() != JsonToken.END_OBJECT) {
                            //Fields inside
                            String fieldname = jParser.getCurrentName();
                            //Log.d(TAG, "FIELDS:");
                            if (fieldname.equals("date")) {
                                // current token is "date",
                                // move to next, which is "date"'s value
                                jParser.nextToken();
                                time = jParser.getText();
                            }

                            if ("tvShowName".equals(fieldname)) {
                                // current token is "tvShowName",
                                // move to next, which is "tvShowName"'s value
                                jParser.nextToken();
                                tvShowName = jParser.getText();
                            }

                            if ("showID".equals(fieldname)) {
                                // current token is "showID", move next
                                jParser.nextToken();
                                showID = jParser.getText();
                            }
                        }
                        //STORE TO DB
                        ContentValues cv = new ContentValues();

                        cv.put("showID", showID);
                        cv.put("date", date);
                        cv.put("time", time);
                        cv.put("tvShowName", tvShowName);

                        rowID = db.insert("program", null, cv);

                        publishProgress(new String[]{date + "\n" + time + "\n" + showID + "\n" + tvShowName + "\nROWS STORED: " + String.valueOf(rowID),String.valueOf(15 + (int)rowID/1000)});
                     }

                }
                jParser.close();
                db.close();

            } catch (JsonGenerationException e) {

                e.printStackTrace();

            } catch (JsonMappingException e) {

                e.printStackTrace();

            } catch (IOException e) {

                e.printStackTrace();

            }


        } catch (IOException e) {
            Log.e(TAG, "Error ", e);
            // If the code didn't successfully get the data, there's no point in attemping
            // to parse it.
            return -1;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        return 0;
    }
}
}

