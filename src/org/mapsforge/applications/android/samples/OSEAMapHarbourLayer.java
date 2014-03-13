package org.mapsforge.applications.android.samples;

import android.os.AsyncTask;
import android.util.Log;

import org.mapsforge.core.graphics.Canvas;
import org.mapsforge.core.model.BoundingBox;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.Point;
import org.mapsforge.map.layer.Layer;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Created by vkandroidstudioadm on 13.02.14.
 */
public class OSEAMapHarbourLayer extends Layer {
    private LatLong latLong;
    private OSEAOnlineMapViewer01 mContext;
    private static final String TAG = "OSEAMapHarbourLayer";

    public OSEAMapHarbourLayer(OSEAOnlineMapViewer01 pContext) {
        super();
        mContext = pContext;
        latLong = new LatLong(54.1,10.2);
    }

    public  void draw(BoundingBox boundingBox, byte zoomLevel, Canvas canvas, Point topLeftPoint){
        // we do nothing, we only want to check the position of the tap
    }

    public boolean onTap(LatLong geoPoint, Point viewPosition,
                         Point tapPoint) {

            Log.w(SamplesApplication.TAG, "Tap coordinates  " + geoPoint.toString());
            Log.w(SamplesApplication.TAG, "xy coordinates  " + tapPoint.toString());
            getHarbourInfoWithAsyncTask(geoPoint);
            return true;

    }

    public String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
        Reader reader = null;
        reader = new InputStreamReader(stream, "UTF-8");
        char[] buffer = new char[len];
        reader.read(buffer);
        return new String(buffer);
    }

    /**
     * we try to read from the url provided, we read max 200 bytes , that is enough for expected result
     * the code is executed from DownloadHarborInfoTask.doInBackground(...)
     * @param testurl
     * @return
     * @throws IOException
     */
    private String downloadUrlAsString(URL testurl) throws IOException{
        InputStream testinputStream = null;
        try {

            HttpURLConnection testcon = (HttpURLConnection) testurl.openConnection();
            testcon.setConnectTimeout(5000);
            testcon.setReadTimeout(5000);
            testcon.setRequestMethod("GET");
            testcon.connect();
            int response = testcon.getResponseCode();
            Log.d(TAG,"the request code: " + response);
            testinputStream = testcon.getInputStream();
            String contentAsString = readIt(testinputStream, 200);
            //Log.d(TAG,contentAsString);
            return contentAsString;
        }

        finally {
            if (testinputStream != null) {
                testinputStream.close();
            }
        }

    }


    private void getHarbourInfoWithAsyncTask(LatLong geoPoint){
        //  example Timmendorf_-_Poel LAT 53.992036 LON 11.375013
        //  http://harbor.openseamap.org/getHarbours.php?b=53.9&t=54.0&l=11.36&r=11.38&ucid=113&maxSize=5
        // new url
        //  http://dev.openseamap.org/website/map/api/getHarbours.php?b=43.16098&t=43.46375&l=16.23863&r=17.39219&ucid=0&maxSize=5&zoom=11
        // also http://dev.openseamap.org/website/map/api/getHarbours.php?b=53.9&t=54.0&l=11.36&r=11.38&ucid=113&maxSize=5&maxSize=5&zoom=11
        // result is:  putHarbourMarker(1275, 11.375, 53.991666666667, 'Timmendorf_-_Poel', 'http://www.skipperguide.de/wiki/Timmendorf_-_Poel', '5');
        //        putHarbourMarker(1276, 11.3752, 53.992133333333, 'Timmendorf_auf_Poel', 'http://www.skipperguide.de/wiki/Timmendorf_auf_Poel', '5');
        // calculate a bounding box
        // String testUrl = "http://dev.openseamap.org/website/map/api/getHarbours.php?b=53.98204&t=54.00204&l=11.36501&r=11.38501&ucid=113&maxSize=5"; 2014_01_29

        double lon = geoPoint.longitude;
        double lat  = geoPoint.latitude;
        double rad = 0.01;
        double left = lon - rad;
        double right = lon +rad;
        double bottom= lat - rad;
        double top = lat + rad;
        //byte zoom = mContext.mLastZoom;
        byte zoom = 14;  // only for test
        String protocoll = "http";
        //String host = "harbor.openseamap.org";
        String host = "dev.openseamap.org";
        StringBuffer buf = new StringBuffer();
        buf.append("/website/map/api/getHarbours.php?");
        buf.append("b=");
        buf.append(customFormat("00.00000",bottom));
        buf.append("&t=");
        buf.append(customFormat("00.00000",top));
        buf.append("&l=");
        buf.append(customFormat("00.00000",left));
        buf.append("&r=");
        buf.append(customFormat("00.00000",right));
        //buf.append("&ucid=113&maxSize=5");
        buf.append("&ucid=113&maxSize=5");
        //buf.append("&zoom="+zoom);
        String aUrlStr = buf.toString();
        URL aUrl = null;
        try {
            aUrl = new URL(protocoll,host, aUrlStr);
            String myUrlString = aUrl.toString();
            //Log.d(TAG,myUrlString);
        } catch (MalformedURLException e) {
            Log.d(TAG,"Unknown Exception");
        }
        if ( aUrl != null) {
            // do all in a Async task
            new DownloadHarborInfoTask().execute(aUrl);
	    }
    }



    /**
     *
     * @param pattern  use a pattern like "000.00"
     * @param value    the value to convert  45.34523
     * @return  aString with the value formatted  045.34
     */
    private  String customFormat(String pattern, double value ) {
        DecimalFormatSymbols decimalFormatSymbolsUS = new DecimalFormatSymbols(Locale.US);
        DecimalFormat myFormatter = new DecimalFormat(pattern,decimalFormatSymbolsUS);
        String output = myFormatter.format(value);
        return output;
    }

    /**
     *  if we have a valid url to the skipperguide we start the browser with this url
     * @param aUrl
     */
    private void startBrowser(String aUrl){
        if (aUrl != null ){
           mContext.startBrowser(aUrl);
        }
    }



    /**
     *
     * @author vkADM
     * without this we get  a crash on Devices with OS > 3.0 networkOnMainThreadException
     * how to Avoid ANRs  http://developer.android.com/training/articles/perf-anr.html
     *  http://developer.android.com/reference/android/os/AsyncTask.html
     * it works on the DEll Streak7 with 2.2 and on the SAMSUNG Tablet with 4.03
     */
    private class DownloadHarborInfoTask extends AsyncTask<URL,Void,String> {

        protected String doInBackground(URL...urls ) {
            String downloadResult = null;
            String result = null;
            int count = urls.length;
            if (count== 1) { // we have one url to download from
                try {
                    //Log.d(TAG,"Infotask download "+ urls[0]);
                    downloadResult = downloadUrlAsString(urls[0]);
                } catch (IOException e) {
                    Log.d(TAG,"IOException "+ e.toString());

                } catch (Exception e ) {
                    Log.d(TAG,"NetWorkException " + e.toString());  // without this we get  a crash on Devices with OS > 3.0 networkOnMainThreadException
                    // how to Avoid ANRs  http://developer.android.com/training/articles/perf-anr.html
                    // http://developer.android.com/reference/android/os/AsyncTask.html
                }                                                   // it works on the DEll Streak7 with 2.2
            }
            if (downloadResult != null) {
                //Log.d(TAG,"download task result " + downloadResult);
                // we expect something like
                //       0                  1       2                    3                      4
                // putHarbourMarker(1275, 11.375, 53.991666666667, 'Timmendorf_-_Poel', 'http://www.skipperguide.de/wiki/Timmendorf_-_Poel', '5');
                String[] fields = downloadResult.split(",");
                if (fields.length >= 4) {
                    String harborUrl = fields[4];
                    if (harborUrl.contains("www.skipperguide.de/wiki")){
                        if (harborUrl.startsWith(" ")) {
                            harborUrl = harborUrl.substring(1);
                        }
                        if (harborUrl.startsWith("'")) {
                            harborUrl = harborUrl.substring(1);
                        }
                        if (harborUrl.endsWith("'")) {
                            harborUrl = harborUrl.substring(0,harborUrl.length()-1);
                        }
                        result = harborUrl;
                    }
                }
            }
            if (result != null) {
                Log.w(TAG,"found harbor info: " + result);
            }else {
                Log.w(TAG," no harbour info: ");
            }
            return result;
        }

        protected void onPostExecute(String aString) {
            Log.d(TAG,"Result of harbour lookup: " + aString);
            startBrowser(aString);
            //showTapDialog(aString);
        }
    }


}
