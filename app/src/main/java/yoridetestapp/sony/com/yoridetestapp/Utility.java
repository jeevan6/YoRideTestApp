package yoridetestapp.sony.com.yoridetestapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Cr
 * eated by sony on 6/6/2016.
 */
public class Utility {
    public double angleFromCoordinate(double lat1, double long1, double lat2,
                                       double long2) {

        double dLon = (long2 - long1);

        double y = Math.sin(dLon) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1)
                * Math.cos(lat2) * Math.cos(dLon);

        double brng = Math.atan2(y, x);

        brng = Math.toDegrees(brng);
        brng = (brng + 360) % 360;
        brng = 360 - brng;

        return brng;
    }

    public Bitmap getMarkerIcon(Context context)
    {
        View inflatedView = LayoutInflater.from(context).inflate(R.layout.marker_view, null, false);
        ImageView imageView = (ImageView) inflatedView.findViewById(R.id.image);
        int iconID = R.drawable.rsz_car;
        imageView.setImageResource(iconID);
        return getViewBitmap(inflatedView);
    }
    public Bitmap getViewBitmap(View targetView)
    {
        int measureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        targetView.measure(measureSpec, measureSpec);

        int measuredWidth = targetView.getMeasuredWidth();
        int measuredHeight = targetView.getMeasuredHeight();

        targetView.layout(0, 0, measuredWidth, measuredHeight);
        Log.d("XXXX",measuredWidth+" "+measuredHeight);

        Bitmap b = Bitmap.createBitmap(measuredWidth, measuredHeight,
                Bitmap.Config.ARGB_8888);
        //Bitmap b8 = Bitmap.createScaledBitmap(b, 40, 60, false);
        Canvas c = new Canvas(b);
        targetView.draw(c);
        return b;
    }

    public String getDirectionsUrl(LatLng origin,LatLng dest){

        // Origin of route
        String str_origin = "origin="+origin.latitude+","+origin.longitude;

        // Destination of route
        String str_dest = "destination="+dest.latitude+","+dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin+"&"+str_dest+"&"+sensor;

        // Output format
        String output = "json";

        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;

        return url;
    }

    public String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);

            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();


            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb  = new StringBuffer();

            String line = "";
            while( ( line = br.readLine())  != null){
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        }catch(Exception e){
            Log.d("Exception", e.toString());
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

}
