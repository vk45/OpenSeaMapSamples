package org.mapsforge.applications.android.samples;

import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.layer.download.TileDownloadLayer;

public class OSEAOffLineOnlineMapViewer extends OSEADualMapViewerWithSeamarks {
	    private TileDownloadLayer downloadLayer;
	    private TileDownloadLayer seamarkDownloadLayer;

	    @Override
	    public void onPause() {
	        super.onPause();
	        this.downloadLayer.onPause();
	        this.seamarkDownloadLayer.onPause();
	    }

	    @Override
	    public void onResume() {
	        super.onResume();
	        this.downloadLayer.onResume();
	        this.seamarkDownloadLayer.onResume();
	    }

	    @Override
	    protected void createLayers2() {
	       /* this.downloadLayer = new TileDownloadLayer(this.tileCache,
	                this.mapViewPositions.get(0), OpenStreetMapMapnik.INSTANCE,
	                AndroidGraphicFactory.INSTANCE);
	       */
	        this.downloadLayer = new TileDownloadLayer(this.tileCache,
	                this.mapViewPositions.get(1), OpenSeaMapBaseTileSource.INSTANCE,
	                AndroidGraphicFactory.INSTANCE);
	        this.layerManagers.get(1).getLayers().add(this.downloadLayer);

	        this.seamarkDownloadLayer = new TileDownloadLayer(this.tileCache,
	                this.mapViewPositions.get(1), OpenSeaMapSeamarkTileSource.INSTANCE,
	                AndroidGraphicFactory.INSTANCE);
	        this.layerManagers.get(1).getLayers().add(this.seamarkDownloadLayer);

	        // addOverlayLayers(layerManagers.get(0).getLayers());

	    }
}
