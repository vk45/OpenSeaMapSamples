package org.mapsforge.applications.android.samples;

import java.io.File;

import org.mapsforge.map.android.AndroidPreferences;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.model.common.PreferencesFacade;
import org.mapsforge.map.rendertheme.XmlRenderTheme;

import android.content.SharedPreferences;
import android.os.Environment;

public class OSEADualMapViewerWithSeamarks extends OSEAMapViewerWithSeamarks {
	protected PreferencesFacade preferencesFacade2;
	protected TileCache tileCache2;

	@Override
	protected void createLayers() {
		super.createLayers();
		createLayers2();
	}

	/**
	 * creates the layers for the second map view.
	 */
	protected void createLayers2() {
		this.layerManagers
				.get(1)
				.getLayers()
				.add(Utils.createTileRendererLayer(this.tileCache2,
						this.mapViewPositions.get(1), getMapFile2(),
						getRenderTheme2(), false));
	}

	@Override
	protected void createMapViews() {
		super.createMapViews();
		// second mapView is defined in layout
		MapView mapView = (MapView) this.findViewById(R.id.mapView2);
		mapView.getModel().init(this.preferencesFacade2);
		mapView.setClickable(true);
		mapViews.add(mapView); 
	}

	@Override
	protected void createSharedPreferences() {
		super.createSharedPreferences();
		SharedPreferences sp = this.getSharedPreferences(getPersistableId2(),
				MODE_PRIVATE);
		this.preferencesFacade2 = new AndroidPreferences(sp);
	}

	/**
	 * @return tilecache for second map view
	 */
	protected TileCache createTileCache2() {
		// no extra tile cache needed in this instance as map source is the same
		return this.tileCache;
	}

	@Override
	protected void createTileCaches() {
		super.createTileCaches();
		this.tileCache2 = createTileCache2();
	}

	@Override
	protected int getLayoutId() {
		// provides a layout with two mapViews
		return R.layout.dualmapviewer;
	}

	/**
	 * @return the map file for the second view
	 */
	protected File getMapFile2() {
		//return new File(Environment.getExternalStorageDirectory(),
		String aMapFileName = this.getMapFileName2();
		return	new File (aMapFileName);
	}

	/**
	 * @return the map file name for the second view
	 */
	protected String getMapFileName2() {
		return getMapFileName();
	}

	protected String getPersistableId2() {
		return this.getPersistableId() + "-2";
	}

	/**
	 * @return the rendertheme for the second view
	 */
	protected XmlRenderTheme getRenderTheme2() {
		return getRenderTheme();
	}

	/**
	 * @return the screen ratio that the mapview takes up (for cache
	 *         calculation)
	 */
	protected float getScreenRatio() {
		return 0.5f;
	}

	/**
	 * @return the screen ratio that the mapview takes up (for cache
	 *         calculation)
	 */
	protected float getScreenRatio2() {
		return 0.5f;
	}

	@Override
	protected void onPause() {
		super.onPause();
		this.preferencesFacade2.save();
	}
}
