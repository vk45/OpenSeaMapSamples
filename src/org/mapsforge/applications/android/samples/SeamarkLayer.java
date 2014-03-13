package org.mapsforge.applications.android.samples;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.mapsforge.core.graphics.Canvas;
import org.mapsforge.core.graphics.FontFamily;
import org.mapsforge.core.graphics.FontStyle;
import org.mapsforge.core.graphics.GraphicFactory;
import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.graphics.Style;
import org.mapsforge.core.model.BoundingBox;
import org.mapsforge.core.model.Dimension;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.MapPosition;
import org.mapsforge.core.model.Point;
import org.mapsforge.core.util.MercatorProjection;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.Layer;
import org.mapsforge.map.model.DisplayModel;
import org.mapsforge.map.util.MapPositionUtil;

import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Handler;
import android.util.Log;

/**
 * Created by vkandroidstudioadm on 27.02.14.
 */
public class SeamarkLayer  extends Layer {

    private static Paint createPaint(GraphicFactory graphicFactory, DisplayModel displayModel) {
        Paint paint = graphicFactory.createPaint();
        paint.setColor(org.mapsforge.core.graphics.Color.BLACK);
        paint.setTypeface(FontFamily.DEFAULT, FontStyle.BOLD);
        paint.setTextSize(20 * displayModel.getScaleFactor());
        return paint;
    }

    private static Paint createPaintStroke(GraphicFactory graphicFactory, DisplayModel displayModel) {
        Paint paint = graphicFactory.createPaint();
        paint.setColor(org.mapsforge.core.graphics.Color.WHITE);
        paint.setTypeface(FontFamily.DEFAULT, FontStyle.BOLD);
        paint.setTextSize(20 * displayModel.getScaleFactor());
        paint.setStrokeWidth(2 * displayModel.getScaleFactor());
        paint.setStyle(Style.STROKE);
        return paint;
    }

    private static final boolean test = false;
    private static final String TAG = "SeamarksLayer";

    //private Paint mPaint;
    //private int mTileSize;
    private GraphicFactory mGraphicFactory;
    private RectF mOval;
    private SeamarkOSM mSeamarkOSM = null;
    private MapView mMapView;
    private File mMapFile;
    private boolean mFillDirectionalSector;
    private byte mShowFilledDirectionalSectorZoom;
    private int mShowNameStatus = 0;  // 0 no show, 1 short name, 2 long Name
    private boolean showOtherSeamarks = true; // 13_10_13 set to true
    private boolean showSectorFires = true;
    private static byte mMinZoomForSeamarks= 8;


    private ArrayList<SeamarkNode> mInfoSeamarksList = null;
    private ArrayList<SeamarkNode> mNodeListNumberedFires;
    private ArrayList<SeamarkNode> mNodeListSingleLights;
    private ArrayList<SeamarkNode> mSeamarksNodeList = null;
    private ArrayList<SeamarkNode> mDisplayedSeamarksNodeList = null;
    private ArrayList<SeamarkWay> mSeamarkWayList = null;
    private ArrayList<SeamarkWay> mDisplayedSeamarkWays = null;

    private boolean mMustShowCenter = false;
    private byte mLastZoom ;
    /*
     * the displayfactor depending on the densitiy of the screen
     * on a nexus 7 we have a density of 320, so all bouys are too small
     */
    private float mDisplayFactor = 1.0f;

    private float mHeading = 0.0f;
    
    private static Handler mReadPoisAndWaysHandler = new Handler();
    private String mSeamarkFilePath = "";

    /**
     * Minimum distance in pixels before the way name is repeated.
     */
    private static final int DISTANCE_BETWEEN_WAY_NAMES = 300;

    /**
     * Distance in pixels to skip from both ends of a segment.
     */
    private static final int SEGMENT_SAFETY_DISTANCE = 30;

    public SeamarkLayer (GraphicFactory pGraphicFactory,MapView pMapView, File pMapFile, SeamarkOSM pSeamarkOSM, boolean pFillDirectionalSector, float pDisplayFactor ){
        super();

        this.mGraphicFactory = pGraphicFactory;
        this.mMapView = pMapView;
        this.mMapFile = pMapFile;
        this.mSeamarkOSM = pSeamarkOSM;
        this.mDisplayFactor = pDisplayFactor;
        this.mShowNameStatus = 0;
        this.mFillDirectionalSector = pFillDirectionalSector;
        this.mShowFilledDirectionalSectorZoom = 13;
        this.mSeamarksNodeList = null; // is set in readpois
        this.mNodeListNumberedFires = new ArrayList<SeamarkNode>();
        this.mNodeListSingleLights = new ArrayList<SeamarkNode>();

        this.mDisplayedSeamarksNodeList = new ArrayList<SeamarkNode>();
        this.mDisplayedSeamarkWays = new ArrayList<SeamarkWay>();
        this.mSeamarkWayList = new ArrayList<SeamarkWay>();
        this.mInfoSeamarksList = new ArrayList<SeamarkNode>();
        this.mHeading = 0.0f; // we start with map/layer orientation to north
    }


    public void setHeading(float pHeading){
    	mHeading = pHeading;
    	//Log.d(TAG," set Heading of Seamarklayer to: " + Float.toString(mHeading));
    }


    public void updateSeamarksFile(){
        if (mMapFile!= null){
            File currentMapFile = mMapFile;
            if (currentMapFile != null ) {
                String currentMapFilePath = currentMapFile.getAbsolutePath();
                setSeamarkFilePathAndRead(currentMapFilePath);
                mReadPoisAndWaysHandler.postDelayed(readPoisAndWays, 1);  // it takes some time to read the seamarks file
            }
        }
    }

    private Runnable readPoisAndWays = new Runnable() {
        public void run() {
            if (mSeamarkOSM.getSeamarkFileReadComplete()){
                //mSeamarksDictionary = mSeamarkOsm.getSeamarksAsDictionary();
                mSeamarksNodeList = mSeamarkOSM.getSeamarksAsArrayList();
                mSeamarkWayList = mSeamarkOSM.getSeamarkWaysAsArrayList();
                updateSeamarkNodesOnOverlay();
                requestRedraw();
            } else {
                // try again
                mReadPoisAndWaysHandler.postDelayed(this,1000);
            }
        }
    };

    public void setSeamarkFilePathAndRead ( String aPath ) {
        mSeamarksNodeList = null;  // the node list will be build in SeamarkOsm,we check if we have a valid list in updateSeamarks
        mSeamarkOSM.clear();
        mSeamarkFilePath = aPath;
        if (aPath.endsWith(".map")) {
            aPath = aPath.substring(0, aPath.length()-4);
            String aSeamarksPath = aPath +"_seamarks"+ ".xml"; 
            File xmlFile = new File (aSeamarksPath);
            if (xmlFile.exists()) {
                mSeamarkFilePath = aSeamarksPath;
                mSeamarkOSM.readSeamarkFile(aSeamarksPath);
            } else {
                String datFilepath = aPath +"_seamarks"+ ".dat";
                File datFile = new File (datFilepath);
                if(datFile.exists()) {
                    mSeamarkFilePath = datFilepath;
                    mSeamarkOSM.readSeamarkFile(datFilepath); 
                } else {
                    String info = "no seamarks file: " + aPath;
                    //String info = mContext.getResources().getString(R.string.osmviewer_seamarks_file_not_found);
                    //mContext.showToastOnUiThread(info);

                }
            }
        }
        if (aPath.endsWith(".xml")){
            File xmlFile = new File (aPath);
            if (xmlFile.exists()) {
                mSeamarkFilePath = aPath;
                mSeamarkOSM.readSeamarkFile(aPath);
            } else {
                String info = "no seamarks file: " + aPath;
                //String info = mContext.getResources().getString(R.string.osmviewer_seamarks_file_not_found);
               // mContext.showToastOnUiThread(info);
            }
        }
    }

    public void setShowNameStatus(int aStatus){
        if (aStatus != mShowNameStatus ){
            mShowNameStatus = aStatus;
            this.requestRedraw();
        }

    }
    public int getShowNameStatus(){
        return mShowNameStatus;
    }

    public void setMustShow(boolean pMustShow) {
        mMustShowCenter = pMustShow;
        this.requestRedraw();
    }


    public void updateSeamarkNodesOnOverlay() {
        // maybe we are called before the seamarksfileRead is completed
        if (!mSeamarkOSM.getSeamarkFileReadComplete()) return;
        if (mSeamarksNodeList== null) return;  // the list may not be set by readpois()
        if (test) Log.d(TAG, "update SeamarkNodesOnOverlay");
        byte zoom = this.mMapView.getModel().mapViewPosition.getZoomLevel();
        // byte zoom = this.mMapView.getMapPosition().getZoomLevel();
        if (mLastZoom != zoom ){  //&& (8 <= zoom  && zoom <=13 ))
            int count = mSeamarksNodeList.size();
            //Log.d(TAG,"nodes: " + count);
            for (int index=0;index < count; index ++){
                if (mSeamarksNodeList!= null){
                    //Log.d(TAG,count + "remove:  "+ index);
                    SeamarkNode aSeamarkNode = mSeamarksNodeList.get(index);
                    removeSeamarkNodeFromLayer(aSeamarkNode);
                }
            }
            mLastZoom = zoom;
        }
		/*try {
		Thread.sleep(sleeptime);
		} catch (Exception e) {

		}*/
        MapPosition aMapPosition = this.mMapView.getModel().mapViewPosition.getMapPosition();
        Dimension aMapDimension =  this.mMapView.getDimension();
        int aTileSize =this.displayModel.getTileSize();
        BoundingBox  aBoundingBox = MapPositionUtil.getBoundingBox(aMapPosition, aMapDimension, aTileSize);

        if (mSeamarksNodeList != null) {
            int count = mSeamarksNodeList.size();
            //Log.d(TAG,"SeamarkNodeList count" + count);
            for (int index=0;index < count; index ++){
                if (mSeamarksNodeList != null){
                    SeamarkNode aSeamarkNode = mSeamarksNodeList.get(index);
                    if (aSeamarkNode != null) {
                        double lat = aSeamarkNode.getLatitudeE6()/1E6;
                        double lon = aSeamarkNode.getLongitudeE6()/1E6;
                        LatLong aNodePoint = new LatLong(lat,lon);
                        if (aBoundingBox != null && aBoundingBox.contains(aNodePoint))  {
                            if (zoom >= mMinZoomForSeamarks) { // zoom 8
                                if (zoom <= mMinZoomForSeamarks + 2) { // 8<= zoom <= 10
                                    if (checkDisplay(aSeamarkNode)){
                                        putSeamarkNodeOnLayer(aSeamarkNode);
                                    }
                                } else {
                                    putSeamarkNodeOnLayer(aSeamarkNode);
                                }
                            }
                        } else {
                            removeSeamarkNodeFromLayer(aSeamarkNode);
                        }
                    }
                }
            }
        }
        if (mSeamarkWayList != null) {
            updateSeamarkWaysOnOverlay(aBoundingBox);
        }
    }

    private boolean checkDisplay(SeamarkNode aSeamarkNode){
        boolean result = false;

        String type = aSeamarkNode.getValueToKey("seamark:type");
        if (type != null && type.equals("buoy_safe_water")) result = true;
        if (type != null && type.equals("light_minor")) result = true;
        if (type != null && type.equals("light_major")) result = true;
        if (type!= null && type.equals("landmark")) {
            String seamarkStr = aSeamarkNode.getValueToKey("seamark");
            if ((seamarkStr != null) && (seamarkStr.equals("lighthouse"))) {
                result = true;
            }
            if ((seamarkStr != null) && (seamarkStr.equals("landmark"))) {
                result = true;
            }
            String lightRangeStr = aSeamarkNode.getValueToKey("seamark:light:range");
            float range = 10.0f;
            try {
                if (lightRangeStr != null)  range = Float.parseFloat(lightRangeStr);
            } catch (Exception e) {}
            if (range > 10.1f) {  // Lighthouse in NL may only have  the range set
                result = true;
            }
            String light1RangeStr = aSeamarkNode.getValueToKey("seamark:light:1:range");
            float range1 = 10.0f;
            try {
                if (light1RangeStr != null)  range1 = Float.parseFloat(light1RangeStr);
            } catch (Exception e) {}
            if (range1 > 10.1f) { // Lighthouse in NL may only have  the range set
                result = true;
            }
        }


        return result;

    }

    private void putSeamarkNodeOnLayer (SeamarkNode aSeamarkNode) {
        if (!aSeamarkNode.getVisibility()){
            if (isInfoSeamark(aSeamarkNode)){
                if( !mInfoSeamarksList.contains(aSeamarkNode)){
                    mInfoSeamarksList.add(aSeamarkNode);
                }

                if (showOtherSeamarks){
                    mDisplayedSeamarksNodeList.add(aSeamarkNode);
                    aSeamarkNode.setVisibility(true);
                    //mCountDisplayedSeamarks++;

                }
            }
            if (!isInfoSeamark(aSeamarkNode)){
                mDisplayedSeamarksNodeList.add(aSeamarkNode);
                aSeamarkNode.setVisibility(true);
                //mCountDisplayedSeamarks++;
                preparePaintSingleLightsIfNecessary(aSeamarkNode);
                preparePaintNumberedLightsIfNecessary( aSeamarkNode);

            }
        }




    }


    private synchronized void removeSeamarkNodeFromLayer (SeamarkNode aSeamarkNode){
        if (mInfoSeamarksList.contains(aSeamarkNode)){
            mInfoSeamarksList.remove(aSeamarkNode);
            //aSeamarkNode.setVisibility(false);
        }
        if (aSeamarkNode.getVisibility()) {
            mDisplayedSeamarksNodeList.remove(aSeamarkNode);
            mNodeListNumberedFires.remove(aSeamarkNode);
            mNodeListSingleLights.remove(aSeamarkNode);
            //mCountDisplayedSeamarks--;
            aSeamarkNode.setVisibility(false);
        } else {

        }


    }



    private void preparePaintSingleLightsIfNecessary(SeamarkNode aSeamarkNode) {
        String seamarkType = aSeamarkNode.getValueToKey("seamark:type");
        if (seamarkType != null &&seamarkType.equals("landmark")) {
			   /*String seamarkStr = aSeamarkNode.getValueToKey("seamark") ;
			   if (seamarkStr != null && seamarkStr.equals("lighthouse")) {
				   String longname = aSeamarkNode.getValueToKey("seamark:longname");
			    	Log.d(TAG,"lighthouse found "+ longname);
					mNodeListMainLights.add(aSeamarkNode);
		       }*/
            String lightColor = aSeamarkNode.getValueToKey("seamark:light:colour");
            if (lightColor != null ) {
                String longname = aSeamarkNode.getValueToKey("seamark:longname");
                Log.d(TAG,"landmark found "+ longname);
                mNodeListSingleLights.add(aSeamarkNode);
            }
        }

    }

    private void preparePaintNumberedLightsIfNecessary(SeamarkNode aSeamarkNode) {
        String seamarkType = aSeamarkNode.getValueToKey("seamark:type");
        if (seamarkType != null) {
			    /*String light1SectorStart = aSeamarkNode.getValueToKey("seamark:light:1:sector_start");
			    if (light1SectorStart != null ){
			    	// we have a sector fire
			    	String longname = aSeamarkNode.getValueToKey("seamark:longname");
			    	Log.d(TAG,"Sector fire found "+ longname);
					mNodeListSectorFire.add(aSeamarkNode);
			    }*/
            if (seamarkType.equals("light_major")
                    || seamarkType.equals("light_minor")
                    ) {
                mNodeListNumberedFires.add(aSeamarkNode);
            } else {
                String light1Color = aSeamarkNode.getValueToKey("seamark:light:1:colour");
                if (light1Color != null ){
                    //we have a other sector fire
                    String longname = aSeamarkNode.getValueToKey("seamark:name");
                    //Log.d(TAG,"Sector fire found "+ longname);
                    mNodeListNumberedFires.add(aSeamarkNode);
                }
            } // else

        } // if type != null

    }

    private boolean isInfoSeamark(SeamarkNode aSeamarkNode){
        boolean result = false;
        String seamarkType = aSeamarkNode.getValueToKey("seamark:type");
        if ( seamarkType != null) {
            if ((seamarkType.contains("small_craft_facility")
                    ||(seamarkType.contains("mooring"))
                    ||(seamarkType.contains("harbour"))
                    || seamarkType.contains("bridge"))
                    || seamarkType.contains("cable")) {
                result = true;
            }
        }
        return result;
    }

    @Override
    public  void draw(BoundingBox boundingBox, byte zoomLevel, Canvas canvas, Point topLeftPoint){
        Paint paint = createPaint(mGraphicFactory, displayModel);
        paint.setStyle(Style.STROKE);
        paint.setStrokeWidth(4);
        paint.setColor(Color.RED);
        if (test) Log.d(TAG,"drawOverlayBitmap begin");

        if (mMustShowCenter) {
            int aWidth = canvas.getWidth();
            int aHeight = canvas.getHeight();
            Point aP = new Point(aWidth / 2, aHeight / 2);
            // projection.toPoint(drawPosition, aPixelPoint, drawZoomLevel);
            //this.mOval = new RectF(aP.x - 5, aP.y - 5, aP.x + 5, aP.y + 5);
            //canvas.drawOval(this.mOval, this.mPaint);
            canvas.drawCircle(aWidth ,aHeight,5,paint);

        }
        if (mSeamarkOSM.getSeamarkFileReadComplete()){
            if (test) Log.d(TAG,"call updateSeamarks");
            this.updateSeamarkNodesOnOverlay();
            if (test) Log.d(TAG, "after call updateSeamarks");
            android.graphics.Canvas androidCanvas = AndroidGraphicFactory.getCanvas(canvas);
            if (this.showSectorFires )  {
                drawSingleLights(boundingBox, zoomLevel, androidCanvas, topLeftPoint);
                drawNumberedLights(boundingBox, zoomLevel, androidCanvas, topLeftPoint);
            }
            drawSeamarkWays(boundingBox,zoomLevel,androidCanvas,topLeftPoint);
            drawSeamarks(boundingBox,zoomLevel,androidCanvas,topLeftPoint);
            //drawRoute(canvas,drawPosition, projection, drawZoomLevel);
        }

        if (test) Log.d(TAG,"drawOverlayBitmap end");
    }


    private void updateSeamarkWaysOnOverlay(BoundingBox boundingBox){
        if (mSeamarkWayList != null ){
            int countNavLines = mSeamarkWayList.size();
            for (int lineIndex = 0; lineIndex < countNavLines; lineIndex ++){
                SeamarkWay seamarkWay = mSeamarkWayList.get(lineIndex);
                if (seamarkWay.belongsToBoundingBox(boundingBox)){
                    addSeamarkWay(seamarkWay);
                } else {
                    removeSeamarkWay(seamarkWay);
                }
            }
        }
    }


    public void addSeamarkWay(SeamarkWay seamarkWay){
        if (!mDisplayedSeamarkWays.contains(seamarkWay)){
            mDisplayedSeamarkWays.add(seamarkWay);
        }
    }

    public void removeSeamarkWay(SeamarkWay seamarkWay){
        if (mDisplayedSeamarkWays.contains(seamarkWay)){
            mDisplayedSeamarkWays.remove(seamarkWay);
        }
    }

    private void drawSeamarkWays (BoundingBox boundingBox, byte zoomLevel,  android.graphics.Canvas androidCanvas, Point topLeftPoint){
        if (mDisplayedSeamarkWays != null) {
            int countLines =mDisplayedSeamarkWays.size();
            for (int index= 0;index < countLines;index++){
                SeamarkWay aSeamarkWay = mDisplayedSeamarkWays.get(index);
                drawSeamarkWay(aSeamarkWay, boundingBox,zoomLevel,androidCanvas,topLeftPoint);
            }
        }
    }

    private float floatValueOfString (String aValueStr) {
        float result = 0.0f;
        try {
            float valueF = Float.valueOf(aValueStr) ;
            result = valueF;
        } catch (NumberFormatException e) {
            Log.d(TAG,e.toString());
        }
        return result;
    }


    private void calculateRenderTextRepeater(String textKey, android.graphics.Paint paint, android.graphics.Paint outline, float[][] coordinates,
                                             List<WayTextContainerOSM> wayNames) {
        // calculate the way name length plus some margin of safety
        float wayNameWidth = paint.measureText(textKey) + 10;

        int skipPixels = 0;

        // get the first way point coordinates
        float previousX = coordinates[0][0];
        float previousY = coordinates[1][0];
        int l = coordinates[0].length;
        // find way segments long enough to draw the way name on them
        for (int i = 0; i < coordinates[0].length; i ++) {
            // get the current way point coordinates
            float currentX = coordinates[0][i];
            float currentY = coordinates[1][i];

            // calculate the length of the current segment (Euclidian distance)
            float diffX = currentX - previousX;
            float diffY = currentY - previousY;
            double segmentLengthInPixel = Math.sqrt(diffX * diffX + diffY * diffY);

            if (skipPixels > 0) {
                skipPixels -= segmentLengthInPixel;
            } else if (segmentLengthInPixel > wayNameWidth) {
                float[] wayNamePath = new float[4];
                // check to prevent inverted way names
                if (previousX <= currentX) {
                    wayNamePath[0] = previousX;
                    wayNamePath[1] = previousY;
                    wayNamePath[2] = currentX;
                    wayNamePath[3] = currentY;
                } else {
                    wayNamePath[0] = currentX;
                    wayNamePath[1] = currentY;
                    wayNamePath[2] = previousX;
                    wayNamePath[3] = previousY;
                }
                wayNames.add(new WayTextContainerOSM(wayNamePath, textKey, paint));
                if (outline != null) {
                    wayNames.add(new WayTextContainerOSM(wayNamePath, textKey, outline));
                }

                skipPixels = DISTANCE_BETWEEN_WAY_NAMES;
            }

            // store the previous way point coordinates
            previousX = currentX;
            previousY = currentY;
        }
    }


    private void drawSeamarkWay (SeamarkWay seamarkWay, BoundingBox boundingBox, byte zoomLevel, android.graphics.Canvas androidCanvas, Point topLeftPoint){
        android.graphics.Paint paint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
        if (seamarkWay != null){
            float[][] coordinates;  // see DatabaseRenderer
            ArrayList<WayTextContainerOSM> wayNames = new ArrayList<WayTextContainerOSM>();
            int paintColor = Color.BLACK;
            float strokeWidth = 2.0f;
            boolean mustDraw = false;
            String navLineOrientationStr = null;
            float navLineOrientation = 0.0f;
            boolean isContour = false;
            String depthValueStr = null;
            float depthValue = 0.0f;
            String dredgedAreaWidthStr = null;
            float dredgedAreaWidth = 0.0f;
            boolean isDredgedArea = false;
            String dredgedArea_minimum_depthStr = null;
            float dredgedArea_minimum_depth = 0.0f;
            String seamarkType = seamarkWay.getValueToKey("seamark:type");
            float[] intervalls = {15.0f,15.0f};
            DashPathEffect aDashPathEffect = new DashPathEffect (intervalls,0);
            if (seamarkType != null) {
                if (seamarkType.equals("fairway")) {
                    paintColor = Color.CYAN;
                    mustDraw = true;
                }
                if (seamarkType.equals("dredged_area")) {
                    dredgedAreaWidthStr=seamarkWay.getValueToKey("seamark:dredged_area:width");
                    dredgedArea_minimum_depthStr = seamarkWay.getValueToKey("seamark:dredged_area:minimum_depth");
                    if (dredgedAreaWidthStr != null){
                        isDredgedArea = true;
                        dredgedAreaWidth = floatValueOfString(dredgedAreaWidthStr);
                    }
                    if (dredgedArea_minimum_depthStr != null){
                        dredgedArea_minimum_depth = floatValueOfString(dredgedArea_minimum_depthStr);
                    }
                    paintColor = Color.CYAN;
                    strokeWidth = 10;
                    mustDraw = true;
                }
                if (seamarkType.equals("navigation_line")) {
                    paintColor = Color.BLACK;
                    navLineOrientationStr = seamarkWay.getValueToKey("seamark:navigation_line:orientation");
                    if (navLineOrientationStr != null){
                        // there may be a Problem see example v="180Â°18&#39;" from Wismarbucht4 way id="139699229"
                        // or v="006Â°24&#39;"  from way id="139691959"
                    }
                    mustDraw = true;
                }
                if (seamarkType.equals("depth_contour")) {
                    float[]  depthContourintervalls = {5.0f,5.0f};
                    aDashPathEffect = new DashPathEffect (depthContourintervalls,0);
                    depthValueStr = seamarkWay.getValueToKey("seamark:depth_contour:depth");
                    paintColor = Color.GRAY;
                    if (depthValueStr != null ) {
                        depthValue = floatValueOfString(depthValueStr);
                        if (depthValue < 3.5f ) {
                            paintColor = Color.BLUE;
                        }
                    }

                    mustDraw = true;
                    isContour = true;
                }
            }
            ArrayList<SeamarkNode> nodeList = seamarkWay.getNodeList();
            if (nodeList != null && mustDraw) {
                paint.setPathEffect(aDashPathEffect);
                paint.setStrokeWidth(strokeWidth);
                paint.setStyle(android.graphics.Paint.Style.STROKE);
                paint.setColor(paintColor);
                android.graphics.Path aPath;
                aPath = new android.graphics.Path();
                int countNodes = nodeList.size();
                coordinates = new float[2][countNodes];
                if (countNodes > 1) {
                    int latE6= nodeList.get(0).getLatitudeE6();
                    int lonE6= nodeList.get(0).getLongitudeE6();
                    LatLong prevPoint = new LatLong(latE6/1e6,lonE6/1e6);
                    int tileSize =this.displayModel.getTileSize();
                    double x  = MercatorProjection.longitudeToPixelX(prevPoint.longitude,zoomLevel, tileSize);
                    double y = MercatorProjection.latitudeToPixelY(prevPoint.latitude,zoomLevel, tileSize);
                    x = x - topLeftPoint.x;
                    y = y - topLeftPoint.y;

                    aPath.moveTo((float)x, (float)y);
                    for (int index = 1;index < countNodes; index ++){
                        latE6= nodeList.get(index).getLatitudeE6();
                        lonE6= nodeList.get(index).getLongitudeE6();
                        LatLong nextPoint = new LatLong(latE6/1e6,lonE6/1e6);
                        x  = MercatorProjection.longitudeToPixelX(nextPoint.longitude,zoomLevel, tileSize);
                        y = MercatorProjection.latitudeToPixelY(nextPoint.latitude,zoomLevel, tileSize);
                        x = x - topLeftPoint.x;
                        y = y - topLeftPoint.y;
                        aPath.lineTo((float)x,(float) y);
                        coordinates[0][index] =(float) x;
                        coordinates[1][index] =(float) y;
                        prevPoint = nextPoint;

						  //aPrevPixelPoint.x = aPrevPixelPoint.x - drawPosition.x;
						  //aPrevPixelPoint.y = aPrevPixelPoint.y - drawPosition.y;
						  // nextPixelPoint.x = nextPixelPoint.x - drawPosition.x;
						  //nextPixelPoint.y = nextPixelPoint.y - drawPosition.y;
						  //canvas.drawLine(aPrevPixelPoint.x,aPrevPixelPoint.y, nextPixelPoint.x,nextPixelPoint.y, paint);

                    }

                    //aPath.close();
                    //aPath.setFillType(Path.FillType.EVEN_ODD);
                    aPath.setFillType(android.graphics.Path.FillType.EVEN_ODD);
                    androidCanvas.drawPath(aPath, paint);

                    // does not paint why??? canvas.drawLine???, x and y must be calculate with aPrevPixelPoint.x -drawPosition.x; 13_04_24
                    // now we deal with the text on the path
                    if (isContour) {
                        depthValueStr = seamarkWay.getValueToKey("seamark:depth_contour:depth");
                        if (depthValueStr != null ) {
                            paint.setTextSize(12);
                            paint.setColor(paintColor);
                            paint.setPathEffect(null);  // this is crucial, otherwise the text is drawn with the effect 13_05_15
                            strokeWidth = 1.0f;
                            paint.setStrokeWidth(strokeWidth);
                            calculateRenderTextRepeater(depthValueStr, paint, null,coordinates, wayNames);
                            int countWayNames = wayNames.size();
                            for (int wayNamesIndex =0; wayNamesIndex < countWayNames; wayNamesIndex++){
                                WayTextContainerOSM wayTextContainer = wayNames.get(wayNamesIndex);
                                aPath.rewind();

                                float[] textCoordinates = wayTextContainer.coordinates;
                                aPath.moveTo(textCoordinates[0], textCoordinates[1]);
                                for (int i = 2; i < textCoordinates.length; i += 2) {
                                    aPath.lineTo(textCoordinates[i], textCoordinates[i + 1]);
                                }
                                androidCanvas.drawTextOnPath(wayTextContainer.text, aPath, 0, -3, wayTextContainer.paint);
                            }

                            //canvas.drawText(depthValue, x, y, paint);
                            // if we want to draw more text on the path, see mapsforge.map.databaserenderer.WayDecorator.renderText
                            // CanvasRasterer.drawWayNames and WayTextContainer , see also DatabaseRenderer.renderway
                            // there is the way length calculated from the coordinates of the way, like x,y
							androidCanvas.drawTextOnPath(depthValueStr, aPath, 0, -3, paint);
							androidCanvas.drawTextOnPath(depthValueStr, aPath, 500.0f, -3, paint); //we try to draw a second time
							androidCanvas.drawTextOnPath(depthValueStr, aPath, 1000.0f, -3, paint);
							androidCanvas.drawTextOnPath(depthValueStr, aPath, 1500.0f, -3, paint);
                            paint.setColor(paintColor);
                        }
                    }

                   if (isDredgedArea && dredgedArea_minimum_depthStr != null ){

                        paint.setTextSize(14);
                        paint.setColor(Color.BLACK);
                        paint.setPathEffect(null);  // this is crucial, otherwise the text is drawn with the effect 13_05_15
                        strokeWidth = 1.0f;
                        paint.setStrokeWidth(strokeWidth);
                        androidCanvas.drawTextOnPath(dredgedArea_minimum_depthStr, aPath, 0, 5, paint);
                        paint.setColor(paintColor);
                    }

                }
            }
        }
    }


    private void drawSeamarks( BoundingBox boundingBox, byte zoomLevel, android.graphics.Canvas androidCanvas, Point topLeftPoint){
        android.graphics.Paint paint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);

        int count = mDisplayedSeamarksNodeList.size();
        //Point aPixelPoint = new Point();
        //Paint paint = mGraphicFactory.createPaint();
        paint.setColor(Color.BLACK);
        //paint.setStyle(Paint.Style.STROKE);
        //paint.setStrokeWidth(1);
        paint.setTextSize(16);
        paint.setTypeface(Typeface.MONOSPACE);
        Log.d(TAG," draw a semark with heading: " + Float.toString(mHeading));
        for (int index = 0; index < count; index++ ) {
            SeamarkNode seamarkNode = mDisplayedSeamarksNodeList.get(index);
            
            
            
            
            
            
            SeamarkDrawable aSeamarkDrawable = new SeamarkDrawable(seamarkNode,zoomLevel, mDisplayFactor);
            android.graphics.Bitmap aSymbolBitmap= aSeamarkDrawable.getBitmap();
            if (aSymbolBitmap != null){
            	
                LatLong geoPoint = new LatLong(seamarkNode.getLatitudeE6()/1e6,seamarkNode.getLongitudeE6()/1e6);
                int tileSize =this.displayModel.getTileSize();
                double x  = MercatorProjection.longitudeToPixelX(geoPoint.longitude,zoomLevel, tileSize);
                double y = MercatorProjection.latitudeToPixelY(geoPoint.latitude,zoomLevel, tileSize);
                x = x - topLeftPoint.x;
                y = y - topLeftPoint.y;
                float left = (float) (x - aSymbolBitmap.getWidth() / 2.0f);
                float right =(float)( x + aSymbolBitmap.getWidth() / 2.0f);
                float bottom = (float)( y + aSymbolBitmap.getHeight() / 2.0f);
                float top = (float) (y - aSymbolBitmap.getHeight() /2.0f);
                RectF rectF = new RectF();
                rectF.set(left, top, right, bottom);
                //drawCenterAndFrame(androidCanvas,rectF);
                if (mHeading > 0.1f) {
                	// we must rotatate the seamark symbol to north
                	
                	Matrix rotationMatrix = new Matrix();
                	rotationMatrix.setRotate(mHeading);
                	android.graphics.Bitmap rotatedBitmap = android.graphics.Bitmap.createBitmap(aSymbolBitmap,
		                    0, 0, aSymbolBitmap.getWidth(), 
		                    aSymbolBitmap.getHeight(),
		                    rotationMatrix, true); 
                	androidCanvas.drawBitmap(rotatedBitmap, left,top,null);
                	
                } else {
                    androidCanvas.drawBitmap(aSymbolBitmap, left,top,null);
                }
                if (zoomLevel > 13) {
                	
                    String nameStr = getSeamarkName(seamarkNode);
                    float drawNamePosY = top + aSymbolBitmap.getHeight() * 0.55f;
                    if (zoomLevel > 15){
                        // make room for the light description
                        drawNamePosY = top + aSymbolBitmap.getHeight() * 0.25f;
                    }
                    paint.setTypeface(Typeface.DEFAULT_BOLD);
                    paint.setTextSize(14);
                    androidCanvas.drawText(nameStr,  (left + aSymbolBitmap.getWidth() * 0.65f), drawNamePosY, paint);
                }

                if (zoomLevel > 15){
                	paint.setTextSize(14);
                	String info = getSeamarkLightDescription (seamarkNode);
                    androidCanvas.drawText(info,  (left + aSymbolBitmap.getWidth() * 0.65f),(top + aSymbolBitmap.getHeight() * 0.55f), paint);
                }
            }
        }
    }
    
    
  private String getSeamarkName (SeamarkNode seamarkNode) {
	  String result = "";
	  String nameStr = seamarkNode.getValueToKey("seamark:name");
      if(nameStr!=null){
          switch (mShowNameStatus) {
              case 1:
                  result = nameStr;
                  break;
              case 2:
                  String shortText = nameStr;
                  if (nameStr.length()> 5) {
                      shortText = nameStr.substring(0, 5) +"..";
                  }
                  result= shortText;
                  break;
              default:
                  break;
          }
      }
	  return result;
  }
  
  
    private String getSeamarkLightDescription (SeamarkNode seamarkNode){
    	String result = "";
    	String lightDescription = seamarkNode.getValueToKey("light:description");
        if (lightDescription != null) {
           result = lightDescription;
        } else {
            String characterStr = seamarkNode.getValueToKey("seamark:light:character");
            String colorStr = seamarkNode.getValueToKey("seamark:light:colour");
            String heightStr = seamarkNode.getValueToKey("seamark:light:height");
            String periodStr = seamarkNode.getValueToKey("seamark:light:period");
            String rangeStr = seamarkNode.getValueToKey("seamark:light:range");
            StringBuffer buf = new StringBuffer();
            if (characterStr != null) {
                buf.append(characterStr);
                buf.append(".");
                if (colorStr != null) {
                    colorStr = colorStr.substring(0,1);
                    colorStr = colorStr.toUpperCase(Locale.US);
                    buf.append(colorStr);
                    buf.append(".");
                }
                if (periodStr!= null) {
                    buf.append(periodStr);
                    buf.append("s");
                }
                if (rangeStr != null) {
                    buf.append(rangeStr);
                    buf.append("m");
                }
                if (heightStr != null){
                    buf.append(heightStr);
                    buf.append("M");
                }
            }
           result = buf.toString();
        }
        return result;

    }

   /* private void drawSymbol(BoundingBox boundingBox, byte zoomLevel, Canvas canvas, Point topLeftPoint){

    }*/

    private void drawCenterAndFrame(android.graphics.Canvas androidCanvas ,RectF rectF){
        //a visible Rectangle with a cross to show the center of the rectangle
        //Paint paint = mGraphicFactory.createPaint();
    	android.graphics.Paint paint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
        paint.setStrokeWidth(2);
        paint.setStyle(android.graphics.Paint.Style.STROKE);
        //paint.setStyle(Style.STROKE);
        paint.setColor(Color.BLUE);
        int left = (int) rectF.left;
        int bottom = (int) rectF.bottom;
        int right = (int) rectF.right;
        int top = (int) rectF.top;
        androidCanvas.drawRect(rectF, paint);
        //canvas.drawLine(left,bottom,right,bottom,paint) ;
        //canvas.drawLine(right,bottom,right,top,paint);
        //canvas.drawLine(right,top,left,top,paint);
        //canvas.drawLine(left,top, left,bottom,paint);
        androidCanvas.drawLine(left,bottom,right,top,paint);
        androidCanvas.drawLine(left,top,right,bottom,paint);
    }

    private void drawCenterAndFrame(Canvas canvas, int left, int top, int right, int bottom){
        Paint paint = mGraphicFactory.createPaint();
        paint.setStrokeWidth(2);
        paint.setStyle(Style.STROKE);
        paint.setColor(Color.BLUE);
        canvas.drawLine(left,bottom,right,bottom,paint) ;
        canvas.drawLine(right, bottom, right, top, paint);
        canvas.drawLine(right, top, left, top, paint);
        canvas.drawLine(left, top, left, bottom, paint);
        canvas.drawLine(left, bottom, right, top, paint);
        canvas.drawLine(left, top, right, bottom, paint);
    }

    private void drawSingleLights (BoundingBox boundingBox, byte zoomLevel,  android.graphics.Canvas androidCanvas, Point topLeftPoint){
        android.graphics.Paint paint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);


        float factor = 1.0f;
        if (zoomLevel > 12) factor = (float)(zoomLevel - 12);
        int count = this.mNodeListSingleLights.size();
        for (int index = 0; index < count; index++ ) {
            SeamarkNode aSeamark = this.mNodeListSingleLights.get(index);

            if (aSeamark != null) {
                String aType = aSeamark.getValueToKey("seamark:type");
                int aLatE6 = aSeamark.getLatitudeE6();
                int aLonE6 = aSeamark.getLongitudeE6();
                double aLat  = aSeamark.getLatitudeE6()/1e6;
                double aLon = aSeamark.getLongitudeE6()/1e6;
                int tileSize =this.displayModel.getTileSize();
                double x  = MercatorProjection.longitudeToPixelX(aLon,zoomLevel, tileSize);
                double y = MercatorProjection.latitudeToPixelY(aLat,zoomLevel, tileSize);
                x = x - topLeftPoint.x;
                y = y - topLeftPoint.y;
                // first we check if we have an unnumbered fire
                String lightColor = aSeamark.getValueToKey("seamark:light:colour");
                if (lightColor!= null) {
                    String lightRange = aSeamark.getValueToKey("seamark:light:range");
                    float range = 10.0f;
                    try {
                        if (lightRange != null)  range = Float.parseFloat(lightRange);
                    } catch (Exception e) {}
                    float sweepAngle = 360.0f;
                    float dx = 80.0f  * factor *(range / 10.0f);
                    float dy = 80.0f * factor * (range / 10.0f);
                    RectF aRectF = new RectF((float)x - dx ,(float)y - dy,(float)x + dx ,(float)y + dy );
                    byte zoom = this.mLastZoom;
                    int baseStroke = 5;
                    if (zoom < 12 ) {
                        baseStroke = 3;
                    }
                    if (zoom < 11 ) {
                        baseStroke = 2;
                    }
                    if (zoom < 10 ) {
                        baseStroke = 1;
                    }
                    paint.setStrokeWidth(baseStroke);
				    /* paint.setColor(Color.BLUE);
					 paint.setStyle(Style.STROKE);
				     canvas.drawRect(aRectF, paint);*/
                    paint.setStyle(android.graphics.Paint.Style.STROKE);
                    paint.setColor(getColor(lightColor));
                    androidCanvas.drawArc(aRectF, 0.0f + 90, - sweepAngle, false,paint);
                }
            } // seamark != null
        } // for
    }
    private void drawNumberedLights (BoundingBox boundingBox, byte zoomLevel,  android.graphics.Canvas androidCanvas, Point topLeftPoint) {
        android.graphics.Paint paint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);


        float factor = 1.0f;
        if (zoomLevel > 12) factor = (float)(zoomLevel - 12);

        if (test) Log.d(TAG, "begin  display Sectorfire updatenr: ");
        int count = this.mNodeListNumberedFires.size();
        for (int index = 0; index < count; index++ ) {
            SeamarkNode aSeamark = this.mNodeListNumberedFires.get(index);

            if (aSeamark != null) {
                String aType = aSeamark.getValueToKey("seamark:type");
                String aName = aSeamark.getValueToKey("seamark:name");
                if (aName != null ){
                    if (test) Log.d(TAG,"Displaying Sectorfire: " + aName);
                } else {
                    if (test) Log.d(TAG,"Displaying unnamed Sectorfire: ");
                }
                double aLat  = aSeamark.getLatitudeE6()/1e6;
                double aLon = aSeamark.getLongitudeE6()/1e6;
                int tileSize =this.displayModel.getTileSize();
                double pixelX  = MercatorProjection.longitudeToPixelX(aLon,zoomLevel, tileSize);
                double pixelY = MercatorProjection.latitudeToPixelY(aLat,zoomLevel, tileSize);
                float x = (float)(pixelX - topLeftPoint.x);
                float y = (float)(pixelY - topLeftPoint.y);

                // we check if we have numbered fires
                int lightnr = 1;
                String nrStr = Integer.toString(lightnr);
                String lightNrColor = aSeamark.getValueToKey("seamark:light:"+nrStr+":colour");
                while ( lightNrColor != null ){
                    if(test)Log.d(TAG,nrStr +" " + lightNrColor);
                    nrStr = Integer.toString(lightnr);
                    String category = aSeamark.getValueToKey("seamark:light:"+nrStr+":category");
                    if (category != null && category.equals("directional")){
                        // directional fire  we simulate a sector with the orientation

                        String orientationStr = aSeamark.getValueToKey("seamark:light:"+nrStr+"orientation");
                        float orientation = 0;
                        try {
                            if (orientationStr != null)  orientation = Float.parseFloat(orientationStr);
                        } catch (Exception e) {}
                        String lightRange = aSeamark.getValueToKey("seamark:light:"+nrStr+":range");
                        float range = 10.0f;
                        try {
                            if (lightRange != null)  range = Float.parseFloat(lightRange);
                        } catch (Exception e) {}
                        orientation = orientation - 45f;
                        float startAngle = orientation -0.5f;
                        float endAngle = orientation + 0.5f;
                        float sweepAngle = 0.0f;
                        if ( endAngle >= startAngle ) {
                            sweepAngle =  Math.abs(endAngle - startAngle);
                        } else {
                            sweepAngle = Math.abs(endAngle + 360 - startAngle);
                        }

                        float dx = 40.0f  * factor *(range / 2.5f);
                        float dy = 40.0f * factor * (range / 2.5f);
                        boolean useCenter = false;
                        paint.setStyle(android.graphics.Paint.Style.STROKE);
                        //paint.setStyle(Style.STROKE);
                        if (mFillDirectionalSector && (zoomLevel > mShowFilledDirectionalSectorZoom )
                                && lightNrColor.equals("white") && (Math.abs(sweepAngle)< 15.0f)) {
                            // we have a directional sector
                            dx= dx*2;
                            dy= dy*2;
                            useCenter = true;
                            paint.setStyle(android.graphics.Paint.Style.FILL);
                        }
                        RectF aRectF = new RectF(x - dx ,y - dy,x + dx ,y + dy );
                        byte zoom =this.mLastZoom;
                        int baseStroke = 5;
                        if (zoom < 12 ) {
                            baseStroke = 3;
                        }
                        if (zoom < 11 ) {
                            baseStroke = 2;
                        }
                        if (zoom < 10 ) {
                            baseStroke = 1;
                        }
                        paint.setStrokeWidth(baseStroke);

                        paint.setColor(getColor(lightNrColor));
                        androidCanvas.drawArc(aRectF, endAngle + 90, -sweepAngle, useCenter,paint);

                        double aRad = Math.toRadians(endAngle +90);
                        float endLineX = dx* (float) Math.cos(aRad);
                        float endLineY = dy *(float) Math.sin(aRad);
                        paint.setColor(Color.BLACK);
                        paint.setStrokeWidth(0.5f);
                        androidCanvas.drawLine(x,y, x + endLineX,y +  endLineY, paint);
                        aRad = Math.toRadians(startAngle +90);
                        endLineX = dx* (float) Math.cos(aRad);
                        endLineY = dy *(float) Math.sin(aRad);
                        paint.setColor(Color.BLACK);
                        paint.setStrokeWidth(0.5f);
                        androidCanvas.drawLine(x,y,x + endLineX,y +  endLineY, paint);

                    } // if category directional
                    else {
                        String lightSectorStart = aSeamark.getValueToKey("seamark:light:"+nrStr+":sector_start");
                        if (lightSectorStart != null){ // we have a sector fire
                            String lightSectorEnd = aSeamark.getValueToKey("seamark:light:"+nrStr+":sector_end");
                            if (lightSectorEnd != null && !lightSectorStart.equals("shore") && !lightSectorEnd.equals("shore")) {
                                // we cannot calculate the sector with the parameter shore
                                String lightRange = aSeamark.getValueToKey("seamark:light:"+nrStr+":range");
                                float startAngle = 0.0f;
                                float endAngle = 0.0f;
                                float range = 10.0f;
                                try {
                                    startAngle = Float.parseFloat(lightSectorStart);
                                    endAngle = Float.parseFloat(lightSectorEnd);
                                } catch ( Exception e) {}
                                try {
                                    if (lightRange != null)  range = Float.parseFloat(lightRange);
                                } catch (Exception e) {}
                                float sweepAngle = 0.0f;
                                if ( endAngle >= startAngle ) {
                                    sweepAngle =  Math.abs(endAngle - startAngle);
                                } else {
                                    sweepAngle = Math.abs(endAngle + 360 - startAngle);
                                }

                                float dx = 40.0f  * factor *(range / 2.5f);
                                float dy = 40.0f * factor * (range / 2.5f);
                                boolean useCenter = false;
                                paint.setStyle(android.graphics.Paint.Style.STROKE);
                                if (mFillDirectionalSector && (zoomLevel > mShowFilledDirectionalSectorZoom)
                                        && lightNrColor.equals("white") && (Math.abs(sweepAngle)< 15.0f)) {
                                    // we have a directional sector
                                    dx= dx*2;
                                    dy= dy*2;
                                    useCenter = true;
                                    paint.setStyle(android.graphics.Paint.Style.FILL);
                                }
                                RectF aRectF = new RectF(x - dx ,y - dy,x + dx ,y + dy );
                                byte zoom = this.mLastZoom;
                                int baseStroke = 5;
                                if (zoom < 12 ) {
                                    baseStroke = 3;
                                }
                                if (zoom < 11 ) {
                                    baseStroke = 2;
                                }
                                if (zoom < 10 ) {
                                    baseStroke = 1;
                                }
                                paint.setStrokeWidth(baseStroke);
						    /* paint.setColor(Color.BLUE);
							 paint.setStyle(Style.STROKE);
						     canvas.drawRect(aRectF, paint);*/

                                paint.setColor(getColor(lightNrColor));
                                androidCanvas.drawArc(aRectF, endAngle + 90, -sweepAngle, useCenter,paint);
                                double aRad = Math.toRadians(endAngle +90);
                                float endLineX = dx* (float) Math.cos(aRad);
                                float endLineY = dy *(float) Math.sin(aRad);
                                paint.setColor(Color.BLACK);
                                paint.setStrokeWidth(0.5f);
                                androidCanvas.drawLine(x,(float)y, x + endLineX,y +  endLineY, paint);
                                aRad = Math.toRadians(startAngle +90);
                                endLineX = dx* (float) Math.cos(aRad);
                                endLineY = dy *(float) Math.sin(aRad);
                                paint.setColor(Color.BLACK);
                                paint.setStrokeWidth(0.5f);
                                androidCanvas.drawLine(x,(float)y, x + endLineX,y +  endLineY, paint);
							 /*paint.setColor(Color.RED);
							 canvas.drawArc(aRectF, 0, 45, false,paint);*/
                            }
                        } // if lightSector start
                        else {
                            String lightRange = aSeamark.getValueToKey("seamark:light:"+nrStr+":range");
                            if (lightRange != null) {
                                float range = 10.0f;
                                try {
                                    if (lightRange != null)  range = Float.parseFloat(lightRange);
                                } catch (Exception e) {}
                                float dx = 80.0f  * factor *(range / 10.0f);
                                float dy = 80.0f * factor * (range / 10.0f);
                                RectF aRectF = new RectF(x - dx ,y - dy,x + dx ,y + dy );
                                byte zoom = this.mLastZoom;
                                int baseStroke = 5;
                                if (zoom < 12 ) {
                                    baseStroke = 3;
                                }
                                if (zoom < 11 ) {
                                    baseStroke = 2;
                                }
                                if (zoom < 10 ) {
                                    baseStroke = 1;
                                }
                                paint.setStrokeWidth(baseStroke);
                                paint.setStyle(android.graphics.Paint.Style.STROKE);
                                paint.setColor(getColor(lightNrColor));
                                androidCanvas.drawArc(aRectF, 0, 360, false,paint);

                            } // lightRange != null

                        } // else
                        // lightnr++ hier war der schwere Fehler, lightnr++ wurde nicht immer ausgeführt
                    }  // else
                    lightnr ++;
                    nrStr = Integer.toString(lightnr);
                    lightNrColor = aSeamark.getValueToKey("seamark:light:"+nrStr+":colour");
                }// while lightNrColor != null
            } // if seamark != null
            if (test) Log.d(TAG, "end display Sectorfire "+ index);
        } // for
        if (test) Log.d(TAG, "end  display Sectorfire ");
    } // drawSectorFires

    private int getColor (String aColor) {
        int result = Color.CYAN;
        if (aColor == null) return result;
        if (aColor.equals("green")) {
            result = Color.GREEN;
        } else
        if (aColor.equals("red")) {
            result = Color.RED;
        }
        else
        if (aColor.equals("yellow")) {
            result = Color.YELLOW;
        } else
        if (aColor.equals("white")) {
            result =Color.YELLOW;
        }
        return result;
    }





}
