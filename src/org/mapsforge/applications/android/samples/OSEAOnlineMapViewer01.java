package org.mapsforge.applications.android.samples;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;

import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.Layers;
import org.mapsforge.map.layer.download.TileDownloadLayer;
import org.mapsforge.map.layer.download.tilesource.OpenStreetMapMapnik;

import java.util.List;

/**
 * Created by vkandroidstudioadm on 13.02.14.
 */
public class OSEAOnlineMapViewer01 extends BasicMapViewerXml{
    private TileDownloadLayer downloadLayer;
    private TileDownloadLayer seamarkDownloadLayer;
    
    protected MapView mMapView = null;

    @Override
    protected void onPause() {
        super.onPause();
        this.downloadLayer.onPause();
        this.seamarkDownloadLayer.onPause();
    }

    @Override
   protected void onResume() {
        super.onResume();
        this.downloadLayer.onResume();
        this.seamarkDownloadLayer.onResume();
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

    @Override
    protected void createLayers() {
       /* this.downloadLayer = new TileDownloadLayer(this.tileCache,
                this.mapViewPositions.get(0), OpenStreetMapMapnik.INSTANCE,
                AndroidGraphicFactory.INSTANCE);
       */
        this.downloadLayer = new TileDownloadLayer(this.tileCache,
                this.mapViewPositions.get(0), OpenSeaMapBaseTileSource.INSTANCE,
                AndroidGraphicFactory.INSTANCE);
        this.layerManagers.get(0).getLayers().add(this.downloadLayer);

        this.seamarkDownloadLayer = new TileDownloadLayer(this.tileCache,
                this.mapViewPositions.get(0), OpenSeaMapSeamarkTileSource.INSTANCE,
                AndroidGraphicFactory.INSTANCE);
        this.layerManagers.get(0).getLayers().add(this.seamarkDownloadLayer);

        addOverlayLayers(layerManagers.get(0).getLayers());

    }

    protected void addOverlayLayers(Layers layers) {
        OSEAMapHarbourLayer harbourLayer;
        harbourLayer = new OSEAMapHarbourLayer(this);
        layers.add(harbourLayer);
    }


   public void startBrowser(String aUrlStr){
        if (aUrlStr != null ){
            Uri webpage = Uri.parse(aUrlStr);
            Intent webIntent = new Intent(Intent.ACTION_VIEW, webpage);
            PackageManager packageManager = this.getPackageManager();
            List<ResolveInfo> activities = packageManager.queryIntentActivities(webIntent, 0);
            boolean isIntentSafe = activities.size() > 0;

            // Start an activity if it's safe
            if (isIntentSafe) {
                this.startActivity(webIntent);
            }
        }
    }

}
