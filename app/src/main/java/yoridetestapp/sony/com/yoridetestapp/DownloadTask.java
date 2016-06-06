package yoridetestapp.sony.com.yoridetestapp;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;

/**
 * Created by sony on 6/5/2016.
 */
public class DownloadTask extends AsyncTask<String, Void, String> {

    // Downloading data in non-ui thread
    Utility util;
    GoogleMap mMap;
    ParserTask parserTask;
    @Override
    protected String doInBackground(String... url) {

        util = new Utility();
        // For storing data from web service
        String data = "";

        try{
            // Fetching the data from web service
            data = util.downloadUrl(url[0]);
        }catch(Exception e){
            Log.d("Background Task", e.toString());
        }
        return data;
    }

    // Executes in UI thread, after the execution of
    // doInBackground()
    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        ParserTask parserTask = new ParserTask();
        parserTask.setMap(mMap);

        // Invokes the thread for parsing the JSON data
        parserTask.execute(result);
    }
    public void setMap(GoogleMap map){
        mMap = map;
    }
    public void resetPolyline(){
        parserTask.resetPolyline();
    }
}
