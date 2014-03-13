/*
 * Copyright 2013-2014 Ludwig M Brinckmann
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.mapsforge.applications.android.samples;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;

/**
 * A simple start screen for the sample activities.
 */
public class Samples extends Activity {
	
	
	public static final String OPENSEAMAP_TESTMAP =  "Test_Wismarbucht";
	
	public static final String DEFAULT_APP_DATA_DIRECTORY =    "Test_OpenSeaMapSamples";
	public static final String DEFAULT_MAP_DATA_DIRECTORY =     DEFAULT_APP_DATA_DIRECTORY + "/MapData";
	public static final String DEFAULT_SYMBOLS_DATA_DIRECTORY = DEFAULT_APP_DATA_DIRECTORY + "/SymbolDefs";
	public static final String DEFAULT_RENDER_DATA_DIRECTORY =  DEFAULT_APP_DATA_DIRECTORY + "/Rendertheme";
	
	public static final String DEFAULT_SEAMARKS_SYMBOL_FILENAME = "symbols.xml";
	public static final String DEFAULT_STANDRAD_RENDERER_FILENAME = "openseamaprenderer001.xml";
	
	public static final String DEFAULT_MAPFILE_NAME =  OPENSEAMAP_TESTMAP+".map";
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.options_menu, menu);
		return true;
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
		}
		return false;
	}

	@Override
	public void onResume() {
		super.onResume();
		final SharedPreferences preferences = getSharedPreferences(
				"installation", Activity.MODE_PRIVATE);
		final String accepted = "accepted";
		File aDirectory = android.os.Environment.getExternalStorageDirectory();
		final String aDirectoryPath = aDirectory.getAbsolutePath();
		File aTestMapFile = new File(aDirectory,DEFAULT_MAPFILE_NAME);
		final boolean aTestMapFound = aTestMapFile.exists();
		if (!preferences.getBoolean(accepted, false)) {
			final AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Warning");
			builder.setCancelable(true);
			builder.setPositiveButton(R.string.startup_dontshowagain,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							preferences.edit().putBoolean(accepted, true)
									.commit();
						}
					});
			
			String aStartUpMsg = getResources().getString(R.string.startup_message)	  ;
			if (!aTestMapFound) {
				aStartUpMsg = aStartUpMsg + "\n\n" + "Assert that " + DEFAULT_MAPFILE_NAME + " is in the base directory: " + aDirectoryPath;
			}
			builder.setMessage( aStartUpMsg);
			builder.create().show();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_samples);
		LinearLayout linearLayout = (LinearLayout) findViewById(R.id.samples);
        linearLayout.addView(createButton(OSEAMapViewer01.class,
                "offline  OpenSeaMap Base map "));
        linearLayout.addView(createButton(OSEAMapViewerWithSeamarks.class,
                "offline  OpenSeaMap with seamarks "));
        linearLayout.addView(createButton(OSEAOnlineMapViewer01.class,
                "online  OpenSeaMap with harbour layer"));
        linearLayout.addView(createButton(OpenSeaMapOnlineMapViewCompassDemo.class,
        		"online  MapViewCompassDemo")); 
        linearLayout.addView(createButton(OpenSeaMapViewerCompassDemo.class,
                "offline OpenSeaMap with CompassDemo")); 
        
        
        
		//linearLayout.addView(createButton(BasicMapViewer.class));
		
		//linearLayout.addView(createButton(BasicMapViewerXml.class));
		 /*
		linearLayout.addView(createButton(DiagnosticsMapViewer.class));
		linearLayout.addView(createButton(RenderThemeMapViewer.class));

		linearLayout.addView(createButton(AssetsRenderThemeMapViewer.class,
				"Rendertheme using Android Assets"));
		linearLayout.addView(createButton(SVGAssetsRenderThemeMapViewer.class,
				"Rendertheme using SVG files"));
		linearLayout.addView(createButton(RenderThemeChanger.class,
				"Automatically changing render themes"));
		linearLayout.addView(createButton(ChangingBitmaps.class,
				"Automatically changing bitmaps"));
		linearLayout.addView(createButton(DownloadLayerViewer.class,
				"Downloading Mapnik"));
		linearLayout.addView(createButton(OverlayMapViewer.class));
		linearLayout.addView(createButton(LongPressAction.class,
				"Long Press Action"));
		linearLayout
				.addView(createButton(MoveAnimation.class, "Move Animation"));
		linearLayout
				.addView(createButton(ZoomToBounds.class, "Zoom to Bounds"));
		linearLayout.addView(createButton(OverlayWithoutBaseMapViewer.class,
				"Just Overlays, No Map"));
		linearLayout.addView(createButton(LocationOverlayMapViewer.class));
		*/
		linearLayout.addView(createButton(DualMapViewer.class, "Dual MapDB"));
		
		linearLayout.addView(createButton(OSEADualMapViewerWithSeamarks.class,
                "OpenSeaMap Dualviewer")); 
		linearLayout.addView(createButton(OSEAOffLineOnlineMapViewer.class,
                "OpenSeaMap offline online Dual viewer")); 
		//linearLayout.addView(createButton(MapViewCompassDemo.class," MapViewCompassDemo")); 
		
		
		
		/*
		linearLayout.addView(createButton(
				DualMapViewerWithDifferentDisplayModels.class,
				"Dual Viewer with different DisplayModels"));
		linearLayout.addView(createButton(DualMapnikMapViewer.class,
				"Tied MapViews MapDB/Mapnik"));
		linearLayout.addView(createButton(DualOverviewMapViewer.class,
				"Overview Mapview"));
		linearLayout
				.addView(createButton(BubbleOverlay.class, "Bubble Overlay"));
		linearLayout.addView(createButton(ItemListActivity.class,
				"Fragment List/View"));
		linearLayout.addView(createButton(StackedLayersMapViewer.class,
				"Stacked rendered tiles"));
	  */
		copyFilesAtStart();
	}

	private Button createButton(final Class<?> clazz) {
		return this.createButton(clazz, null);
	}

	private Button createButton(final Class<?> clazz, String text) {
		Button button = new Button(this);
		if (text == null) {
			button.setText(clazz.getSimpleName());
		} else {
			button.setText(text);
		}
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				startActivity(new Intent(Samples.this, clazz));
			}
		});
		return button;
	}
	
	
	private void copyFilesAtStart() {
		copyStandardSymbolDefsIfNecessary();
		copyStandardRendererIfNecessary();
		copyTestMapIfNecessary();
	}
	
	private boolean copyFileFromAssetToStandardDirectory (String fileName) {
		boolean result = false;
		InputStream is = null;
		FileOutputStream fos = null;
		try {
			
			String dest = DirectoryUtils.getBaseDirectoryPath();
			is = getAssets().open(fileName);
			fos = new FileOutputStream(dest);
			DirectoryUtils.copy(is,fos);
			//String aStr = getResources().getString(R.string.main_activity_copy);
			//mMainTextView.append("\n" + "copy : " + fileName); 
			//mMainTextView.append("\n" + aStr + fileName);  
			result = true;
		} catch (IOException e){
			//if (test) Log.d(TAG,"cant create file "+ e.toString());
			//String aStr = getResources().getString(R.string.main_activity_create_std_renderer);
			//mMainTextView.append("\n" + "can't create OpenSeaRenderer "+ e.toString());
			//mMainTextView.append("\n" + aStr + e.toString());
		} finally {
			if (is != null)
				try { is.close(); }catch (IOException e){}
			if (fos != null)
				try { fos.close(); } catch (IOException e) {}
		}
		return result;
	}
	
	private boolean copyFileFromAssetToDest (String fileName,String dest) {
		boolean result = false; 
		InputStream is = null;
		FileOutputStream fos = null;
		try {
			is = getAssets().open(fileName);
			fos = new FileOutputStream(dest);
			DirectoryUtils.copy(is,fos);
			
			result = true;
		} catch (IOException e){
			//if (test) Log.d(TAG,"cant create file "+ e.toString());
			
		} finally {
			if (is != null)
				try { is.close(); }catch (IOException e){}
			if (fos != null)
				try { fos.close(); } catch (IOException e) {}
		}
		return result;
	} 
	
	
	
	private void copyStandardSymbolDefsIfNecessary() {
		   String symboldefsName = DEFAULT_SEAMARKS_SYMBOL_FILENAME;
		   String destDir = DirectoryUtils.getSeamarkSymbolsDirectoryPath();
		   DirectoryUtils.createExternalDirectoryIfNecessary(destDir);
		   String filePath = destDir + "/" + symboldefsName;
		   File aTestFile = new File(filePath);
		   if (!aTestFile.exists()){
			   boolean ok = copyFileFromAssetToDest (symboldefsName,filePath);
			   if (!ok) {
				   //showToastOnUiThread("Could not create seamarks symbol file") ; 
			   }
		   }
		  
	   }
	 
	 private void copyStandardRendererIfNecessary() {
		   String renderThemeName = DEFAULT_STANDRAD_RENDERER_FILENAME;
		   String destDir = DirectoryUtils.getRenderThemeDirectoryPath();
		   DirectoryUtils.createExternalDirectoryIfNecessary(destDir);
	       String filePath = destDir + "/" + renderThemeName;
		   File aTestFile = new File(filePath);
		   if (!aTestFile.exists()){
			   boolean ok = copyFileFromAssetToDest (renderThemeName,filePath);
			   if (!ok) {
				   //showToastOnUiThread("Could not create standard renderer file") ; 
			   }
		   }
	   }
	 
	 private void copyTestMapIfNecessary() {
		   String testMapName = OPENSEAMAP_TESTMAP; 
		   String destDir =  DirectoryUtils.getMapDirectoryPath();
		   DirectoryUtils.createExternalDirectoryIfNecessary(destDir);
		   
		   String filePath = destDir + "/" + testMapName+".map";
		   File aTestFile = new File(filePath);
		   if (!aTestFile.exists()){
			   boolean ok = copyFileFromAssetToDest (testMapName+".map", filePath);
		   }
		    
		    String seamarkFilePath = destDir + "/" + testMapName+"_seamarks.xml";
		    aTestFile = new File(seamarkFilePath);
		    if (!aTestFile.exists()){
			   boolean ok = copyFileFromAssetToDest (testMapName+"_seamarks.xml",seamarkFilePath);
		   }
		   
		  
		    
	   }
 
}
