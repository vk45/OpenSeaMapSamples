package org.mapsforge.applications.android.samples;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;

import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.MapPosition;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.Layers;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.model.MapViewPosition;
import org.mapsforge.map.rendertheme.InternalRenderTheme;
import org.mapsforge.map.rendertheme.XmlRenderTheme;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

/**
 * Created by vkandroidstudioadm on 27.02.14.
 */
public class OSEAMapViewerWithSeamarks extends BasicMapViewerXml {
    private static final FileFilter FILE_FILTER_EXTENSION_MAP = new FilterByFileExtension(".map");
    private static final FileFilter FILE_FILTER_EXTENSION_XML = new FilterByFileExtension(".xml");
    private static final int SELECT_MAP_FILE = 0;
    private static final int SELECT_RENDER_THEME_FILE = 1;
    private TileRendererLayer mTileRendererLayer = null;
    private String DEFAULT_MAPFILE_NAME =  "Test_Wismarbucht.map";
    private String mMapfilename = DEFAULT_MAPFILE_NAME;
    private String mRenderThemeFileName ="";
    //private XmlRenderTheme mRenderTheme = null;
    private static final String currentMapNamePathKey = "current_mapfilename";
    private static final String currentRenderThemeNamePathKey = "current_rendertheme_filename";
    private static final String lastLATKey  = "lastlat";
    private static final String lastLONKey  = "lastlon";
    private static final String lastZoomKey = "lastzoom";
    private static final String seamarkSymbolsDefsFile = "symbols.xml";
    private SeamarkOSM mSeamarkOSM = null;
    
    protected SeamarkLayer mSeamarkLayer = null; // we use them in the CompasDemo to delegate
    protected MapView mMapView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SeamarkSymbol.preloadFromDefsFile(seamarkSymbolsDefsFile);
        mSeamarkOSM = new SeamarkOSM(this);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.menu_preferences:
                intent = new Intent(this, Settings.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                startActivity(intent);
                return true;
            case R.id.menu_position_enter_coordinates:
                showDialog(DIALOG_ENTER_COORDINATES);
                return true;
            case R.id.menu_select_mapfile:
                startMapFilePicker();
                break;
            case R.id.menu_select_renderfile:
                startRenderThemeFilePicker();
                break;
        }
        return false;
    }
    /**
     * Sets all file filters and starts the FilePicker to select a map file.
     */
    private void startMapFilePicker() {
        FilePicker.setFileDisplayFilter(FILE_FILTER_EXTENSION_MAP);
        FilePicker.setFileSelectFilter(new ValidMapFile());
        startActivityForResult(new Intent(this, FilePicker.class), SELECT_MAP_FILE);
    }

    /**
     * Sets all file filters and starts the FilePicker to select a map file.
     */
    private void startRenderThemeFilePicker() {
        FilePicker.setFileDisplayFilter(FILE_FILTER_EXTENSION_XML);
        FilePicker.setFileSelectFilter(new ValidRenderTheme());
        startActivityForResult(new Intent(this, FilePicker.class), SELECT_RENDER_THEME_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == SELECT_MAP_FILE) {
            /*TileRendererLayer aTileRendererLayer = null;
            Layers theLayers = this.layerManagers.get(0).getLayers();
            int aSize = theLayers.size();
            Layer aLayer = theLayers.get(0);
            if (aLayer.getClass().getName().equals("TileRendererLayer")) {
                aTileRendererLayer = (TileRendererLayer) aLayer;
            }*/
            if (resultCode == RESULT_OK) {

                if (intent != null && intent.getStringExtra(FilePicker.SELECTED_FILE) != null) {
                    File selectedFile = new File(intent.getStringExtra(FilePicker.SELECTED_FILE));
                    mMapfilename= selectedFile.getAbsolutePath();
                    this.preferencesFacade.putString(currentMapNamePathKey,mMapfilename);
                    this.preferencesFacade.save();
                }
            } else {
                if (resultCode == RESULT_CANCELED && mTileRendererLayer == null) {
                    finish();
                }
            }
        }
        if (requestCode == SELECT_RENDER_THEME_FILE){
            if (resultCode == RESULT_OK) {
                if (intent != null && intent.getStringExtra(FilePicker.SELECTED_FILE) != null) {
                    File selectedFile = new File(intent.getStringExtra(FilePicker.SELECTED_FILE));
                    mRenderThemeFileName = selectedFile.getAbsolutePath();
                    Log.d(SamplesApplication.TAG, "RenderTheme: " + mRenderThemeFileName);
                    this.preferencesFacade.putString(currentRenderThemeNamePathKey,mRenderThemeFileName);
                    this.preferencesFacade.save();
                }
            }
        }
    }


    @Override
    protected XmlRenderTheme getRenderTheme() { 
        XmlRenderTheme aRenderTheme = null;
        String renderThemePath = this.preferencesFacade.getString(currentRenderThemeNamePathKey,"");
        if (renderThemePath.equals("")){
            aRenderTheme = InternalRenderTheme.OSMARENDER;
        } else {
            File renderThemeFile = new File (renderThemePath);
            try {
                aRenderTheme = new ExternalRenderThemeUsingJarResources(renderThemeFile);
                Log.d(SamplesApplication.TAG,"valid render theme found: " +  renderThemePath);
            } catch ( IOException e ){
                Log.d(SamplesApplication.TAG," no valid render theme ");
                aRenderTheme =InternalRenderTheme.OSMARENDER;
            }
        }
        String aRelativePathPrefix = aRenderTheme.getRelativePathPrefix();
        return aRenderTheme;
    }


    /**
   	 * @return a map file
   	 */
       @Override
   	protected File getMapFile() {
           File file = new File(this.getMapFileNamePath());
   		Log.i(SamplesApplication.TAG, "Map file is " + file.getAbsolutePath());
   		return file;
   	}

               /**
                * @return the map file name to be used
                */
       
       protected String getMapFileNamePath() {
       	
       	if (mMapfilename.startsWith("/storage/emulated")) {
       		return mMapfilename;
       	}

           if (mMapfilename.startsWith("/mnt")){ 
               return mMapfilename;
           } else
           {
               if (mMapfilename.startsWith("/sdcard")){
                  //mMapfilename = mMapfilename.substring(7) ;
            	   return mMapfilename;
               } 
               String mapFileNamePath = DirectoryUtils.getMapDirectoryPath()+  "/"+ mMapfilename;;
               return mapFileNamePath;
           }

       }
       
     
    @Override
    protected void createMapViews() {
        MapView mapView = getMapView();
        mapView.getModel().init(this.preferencesFacade);
        mapView.setClickable(true);
        mapView.getMapScaleBar().setVisible(true);
        mapView.setBuiltInZoomControls(hasZoomControls());
        mapView.getMapZoomControls().setZoomLevelMin((byte) 5);
        mapView.getMapZoomControls().setZoomLevelMax((byte) 20);
        mMapView = mapView;
        this.mapViews.add(mapView);
    }

    /**
     * initializes the map view position.
     *
     * @param mvp
     *            the map view position to be set
     * @return the mapviewposition set
     */
    @Override
    protected MapViewPosition initializePosition(MapViewPosition mvp) {
        LatLong center = mvp.getCenter();
        //mvp.setMapPosition(this.createMapPosition()) ;
        if (center.equals(new LatLong(0, 0))) {
            mvp.setMapPosition(this.getInitialPosition());
        }
        mvp.setZoomLevelMax((byte) 24);
        mvp.setZoomLevelMin((byte) 7);
        return mvp;
    }

    MapPosition createMapPosition () {
        MapPosition aMapPosition = null;
        // Berlin
        // aMapPosition = new MapPosition(new LatLong(52.517037, 13.38886), (byte) 12);
        // Lemmer
        if (mMapfilename.contains("NL_14_01_05.map")) {
            aMapPosition = new MapPosition(new LatLong(52.846926, 5.7059429), (byte)7);  // 100 km
        } else if (mMapfilename.contains("DE1403V1_Rostock-Bornholm")) {
        	aMapPosition = new MapPosition(new LatLong(54.1,12.1), (byte) 7);
        } else {
            aMapPosition = new MapPosition(new LatLong(52.1,12.1), (byte) 7);  // 100 km
        }

        return aMapPosition;
    }

    @Override
    protected void onPause() {
        Log.d(SamplesApplication.TAG, "last current map " + mMapfilename);
        this.preferencesFacade.putString(currentMapNamePathKey, mMapfilename);
        this.preferencesFacade.putString(currentRenderThemeNamePathKey,mRenderThemeFileName);
        this.preferencesFacade.save();
        super.onPause();
    }

    @Override
    protected void createLayers() {
        //super.createLayers();
        String aMapFileName = this.preferencesFacade.getString(currentMapNamePathKey,DEFAULT_MAPFILE_NAME);
        mMapfilename = aMapFileName;
        // sometimes mMapFileName starts with /mnt  or /sdcard, we test this in getMapFile();
        File mapfile = getMapFile();
        String fileNamePath = mapfile.getAbsolutePath();
        Log.d(SamplesApplication.TAG, "current map " + fileNamePath);
        TileRendererLayer tileRendererLayer = Utils.createTileRendererLayer(this.tileCache,
                this.mapViewPositions.get(0), mapfile, getRenderTheme(), false);
        this.layerManagers.get(0).getLayers().add(tileRendererLayer);
        mTileRendererLayer = tileRendererLayer;



        Layers theLayers = this.layerManagers.get(0).getLayers();
        int aSize = theLayers.size();
        boolean fillDirectionalSector = false;
        //float displayFactor = 1.0f;
        
        float displayFactor = mMapView.getModel().displayModel.getScaleFactor();
        SeamarkLayer aSeamarkLayer = new SeamarkLayer(AndroidGraphicFactory.INSTANCE,mMapView,getMapFile(),mSeamarkOSM,fillDirectionalSector,displayFactor);
        mSeamarkLayer = aSeamarkLayer;
        theLayers.add(mSeamarkLayer); 
        mSeamarkLayer.updateSeamarksFile();
        /*this.layerManagers
                .get(0)
                .getLayers()
                .add(new TileGridLayer(AndroidGraphicFactory.INSTANCE,
                        this.mapViews.get(0).getModel().displayModel));
        this.layerManagers
                .get(0)
                .getLayers()
                .add(new TileCoordinatesLayer(AndroidGraphicFactory.INSTANCE,
                        this.mapViews.get(0).getModel().displayModel));*/
    }

}