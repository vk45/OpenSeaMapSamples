/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mapsforge.applications.android.samples;



import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.model.DisplayModel;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DrawFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Picture;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

//import com.google.android.maps.GeoPoint;
//import com.google.android.maps.MapActivity;
//import com.google.android.maps.MapView;
//import com.google.android.maps.MyLocationOverlay;

/**
 * Example of how to use an {@link com.google.android.maps.MapView}
 * in conjunction with the {@link com.hardware.SensorManager}
 * <h3>MapViewCompassDemo</h3>

<p>This demonstrates creating a Map based Activity.</p>

<h4>Source files</h4>
 * <table class="LinkTable">
 *         <tr>
 *             <td >src/com.example.android.apis/view/MapViewCompassDemo.java</td>
 *             <td >The Alert Dialog Samples implementation</td>
 *         </tr>
 * </table>
 */
public class OpenSeaMapOnlineMapViewCompassDemo extends OSEAOnlineMapViewer01 {

	private static final String TAG = "OpenSeaMapViewCompassDemo";
    private SensorManager mSensorManager;
    private RotateView mRotateView;
    private Button mPlusButton = null;
    private Button mMinusButton = null;
    private Button mFixNorthButton;
    private TextView mZoomInfo = null;
    private TextView mHeadingInfo = null;
    //private MyLocationOverlay mMyLocationOverlay;

    private class RotateView extends ViewGroup implements SensorListener {
        private static final float SQ2 = 1.414213562373095f;
        private final SmoothCanvas mCanvas = new SmoothCanvas();
        private float mHeading = 0;
        private float mSavedHeading = mHeading;
        private float mPreviousX;
        private float mPreviousY;
        private MapView mInnerMapView = null;
        private float mMapMoveDelta;
        private boolean moveThresholdReached;
        private boolean mNorthFixed = false;

        public RotateView(Context context) {
            super(context);
            mMapMoveDelta = ViewConfiguration.get(context).getScaledTouchSlop();
        }

        public void onSensorChanged(int sensor, float[] values) {
            // Log.d(TAG, "x: " + values[0] + "y: " + values[1] + "z: " + values[2]);
            synchronized (this) {
                float newHeading = values[0];
                if (Math.abs(mHeading - newHeading) > 3 ){
                    //Log.d(TAG, "x: " + values[0] + "y: " + values[1] + "z: " + values[2]);
                    //mHeading = newHeading +180.0f;
                	if (!mNorthFixed) {
                         mHeading = newHeading;
                         mHeadingInfo.setText( Float.toString(mHeading)+ "\n\n");
                         invalidate();
                	}
                   
                }

            }
        }

        public float getHeading() {
            return mHeading;
        }
        
        public void setNorthFixed (boolean northFixed) {
        	if (northFixed ) {
        		mSavedHeading = mHeading;
        		mHeading = 0;
        	}else {
        		mHeading = mSavedHeading;
        	}
        	mHeadingInfo.setText( Float.toString(mHeading)+ "\n\n");
        	mNorthFixed = northFixed;
        	
        }

        @Override
        protected void dispatchDraw(Canvas canvas) {
            canvas.save(Canvas.MATRIX_SAVE_FLAG);
            canvas.rotate(-mHeading, getWidth() * 0.5f, getHeight() * 0.5f);
            mCanvas.delegate = canvas;
            super.dispatchDraw(mCanvas);
            canvas.restore();
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            final int width = getWidth();
            final int height = getHeight();
            final int count = getChildCount();
            for (int i = 0; i < count; i++) {
                final View view = getChildAt(i);
                final int childWidth = view.getMeasuredWidth();
                final int childHeight = view.getMeasuredHeight();
                final int childLeft = (width - childWidth) / 2;
                final int childTop = (height - childHeight) / 2;
                view.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight);
            }
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            int w = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
            int h = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
            int sizeSpec;
            if (w > h) {
                sizeSpec = MeasureSpec.makeMeasureSpec((int) (w * SQ2), MeasureSpec.EXACTLY);
            } else {
                sizeSpec = MeasureSpec.makeMeasureSpec((int) (h * SQ2), MeasureSpec.EXACTLY);
            }
            final int count = getChildCount();
            for (int i = 0; i < count; i++) {
                getChildAt(i).measure(sizeSpec, sizeSpec);
            }
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }

        public void setInnerView (MapView pMapView) {
            mInnerMapView = pMapView;
        }

        @Override
        public boolean dispatchTouchEvent(MotionEvent ev) {
            // TODO: rotate events too
            // we handle the move
            float x = ev.getX();
            float y = ev.getY();
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    this.moveThresholdReached = false;
                    break;
                case MotionEvent.ACTION_MOVE:
                    float dx = x  -mPreviousX;
                    float dy = y - mPreviousY;
                    Log.d(TAG, "heading " + mHeading + " dx: " + dx + " dy: " + dy);
                    Matrix aMatrix = new Matrix();
                    aMatrix.setRotate(-mHeading);
                    aMatrix.postTranslate(dx, dy);
                    aMatrix.postRotate(mHeading);
                    float[] srcPoint = {0,0};
                    float[] destPoint = {0,0};
                    aMatrix.mapPoints(destPoint,srcPoint);
                    Log.d(TAG,"parameters calcutated with matrix");
                    Log.d(TAG, "dx:    " + dx +    " dy:    " + dy);
                    Log.d(TAG, "moveX: " + destPoint[0] + " moveY: " + destPoint[1]);
                    Log.d(TAG,"");
                    float moveX = destPoint[0];
                    float moveY = destPoint[1];
                    if (this.moveThresholdReached) {

                        mPreviousX = x;
                        mPreviousY = y;
                        this.mInnerMapView.getModel().mapViewPosition.moveCenter(moveX, moveY);
                    } else if (Math.abs(moveX) > this.mMapMoveDelta || Math.abs(moveY) > this.mMapMoveDelta) {
                        cancelLongPress();
                        this.moveThresholdReached = true;
                        mPreviousX = x;
                        mPreviousY = y;

                    }
                    return true;

            }

            mPreviousX = x;
            mPreviousY = y;

            return super.dispatchTouchEvent(ev);
        }

        public void onAccuracyChanged(int sensor, int accuracy) {
            // TODO Auto-generated method stub

        }
    }

    private void makeGUI() {
        mRotateView = new RotateView(this);
        LinearLayout main = new LinearLayout(this);
        main.setOrientation(LinearLayout.VERTICAL);
        float aTextSize = 20.f * DisplayModel.getDeviceScaleFactor();
        int  aBottomPadding = (int) (5 *  DisplayModel.getDeviceScaleFactor());
        mHeadingInfo = new TextView(this);
        mHeadingInfo.setBackgroundColor(Color.WHITE);
        mHeadingInfo.setTextColor(Color.BLACK);
        mHeadingInfo.setText("heading: " + Float.toString(mRotateView.getHeading()) + "   " + "\n \n  ");
        mHeadingInfo.setPadding(0, 0, 0, aBottomPadding);
        
        mZoomInfo = new TextView(this);
        byte aZoomLevel = mMapView.getModel().mapViewPosition.getZoomLevel();
        mZoomInfo.setText("zoom: " + aZoomLevel + " " + "\n   ");
        mZoomInfo.setTextSize(aTextSize);
        mZoomInfo.setPadding(0, 0, 0, aBottomPadding);
        
       
        
        LinearLayout firstRow = new LinearLayout(this);
        firstRow.setOrientation(LinearLayout.HORIZONTAL);
        //int padBottom = firstRow.getPaddingBottom() + (int)(20.0f * DisplayModel.getDeviceScaleFactor());
        //firstRow.setPadding(firstRow.getPaddingLeft(),firstRow.getPaddingTop(), firstRow.getPaddingRight(),padBottom);

        firstRow.addView(mZoomInfo, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        firstRow.addView(mHeadingInfo, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        main.addView(firstRow, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1));


        mPlusButton = new Button(this);
        mPlusButton.setText("+");
        mPlusButton.setTextSize(aTextSize);
        mPlusButton.setBackgroundColor(Color.WHITE);
        mPlusButton.setTextColor(Color.BLACK);
        mPlusButton.setPadding(0, 0, 0, aBottomPadding);
        mPlusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                byte aZoomLevel = mMapView.getModel().mapViewPosition.getZoomLevel();
                byte newZoomLevel = (byte) (aZoomLevel + 1);
                Log.d(TAG,"zoomlevel + clicked "+ newZoomLevel);
                mMapView.getModel().mapViewPosition.setZoomLevel(newZoomLevel);
                mZoomInfo.setText("Zoom: " + newZoomLevel + " " + "\n   ");
            }
        });

        mPlusButton.setClickable(true);
        mPlusButton.setVisibility(View.VISIBLE);
        //Rect bounds = mPlusButton.getClipBounds();
        Button dummyButton = new Button(this);
        mMinusButton = new Button(this);
        mMinusButton.setText("-");
        mMinusButton.setTextSize(aTextSize);
        mMinusButton.setBackgroundColor(Color.WHITE);
        mMinusButton.setTextColor(Color.BLACK);
        mMinusButton.setPadding(0, 0, 0, aBottomPadding);
        mMinusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                byte aZoomLevel = mMapView.getModel().mapViewPosition.getZoomLevel();
                byte newZoomLevel = (byte) (aZoomLevel -1);
                Log.d(TAG,"zoomlevel - clicked "+ newZoomLevel);
                mMapView.getModel().mapViewPosition.setZoomLevel(newZoomLevel);
                mZoomInfo.setText("Zoom: " + newZoomLevel + " " + "\n   ");
            }
        });

        mMinusButton.setClickable(true);
        mMinusButton.setVisibility(View.VISIBLE);
        
        mFixNorthButton = new Button(this);
        mFixNorthButton.setText("HEAD up");
        mFixNorthButton.setClickable(true);
        mFixNorthButton.setVisibility(View.VISIBLE);
        mFixNorthButton.setPadding(0, 0, 0, aBottomPadding);
        mFixNorthButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               if (mFixNorthButton.getText().equals("HEAD up")) {
            	    mRotateView.setNorthFixed(true);
            	    mFixNorthButton.setText("NORTH");
               } else {
            	   mRotateView.setNorthFixed(false);
           	       mFixNorthButton.setText("HEAD up");
               }
              
               mRotateView.invalidate();
            }
        });

        firstRow.addView(mPlusButton, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        firstRow.addView(dummyButton, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        firstRow.addView(mMinusButton, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        firstRow.addView(mFixNorthButton, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        
        
        LinearLayout thirdRow = new LinearLayout(this);
        thirdRow.setOrientation(LinearLayout.HORIZONTAL);

        mRotateView.addView(mMapView);
        mRotateView.setInnerView(mMapView);
        thirdRow.addView(mRotateView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));
        int padTop = thirdRow.getPaddingTop();
        thirdRow.setPadding(thirdRow.getPaddingLeft(),padTop + 40, thirdRow.getPaddingRight(), thirdRow.getPaddingBottom());
        main.addView(thirdRow,new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));
        setContentView(main);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        makeGUI();
    }

    @Override
    protected MapView getMapView() {
        MapView mv = new MapView(this);
        //setContentView(mv); // we don't set the ContentView now, its done with the RotateView, see makeGUI!!
        return mv;
    }

    @Override
    protected void createMapViews() {
        MapView mapView = getMapView();
        mapView.getModel().init(this.preferencesFacade);
        mapView.setClickable(true);
        mapView.getMapScaleBar().setVisible(true);
        mapView.setBuiltInZoomControls(hasZoomControls());
        mapView.getMapZoomControls().setZoomLevelMin((byte) 10);
        mapView.getMapZoomControls().setZoomLevelMax((byte) 20);
        mMapView = mapView;
        this.mapViews.add(mapView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(mRotateView,
                SensorManager.SENSOR_ORIENTATION,
                SensorManager.SENSOR_DELAY_UI);
        // mMyLocationOverlay.enableMyLocation();
    }

    @Override
    protected void onStop() {
        mSensorManager.unregisterListener(mRotateView);
        //  mMyLocationOverlay.disableMyLocation();
        super.onStop();
    }

    /*@Override
    protected boolean isRouteDisplayed() {
        return false;
    }*/


    static final class SmoothCanvas extends Canvas {
        Canvas delegate;

        private final Paint mSmooth = new Paint(Paint.FILTER_BITMAP_FLAG);

        public void setBitmap(Bitmap bitmap) {
            delegate.setBitmap(bitmap);
        }

        /*public void setViewport(int width, int height) {
            delegate.setViewport(width, height);
        }
*/
        public boolean isOpaque() {
            return delegate.isOpaque();
        }

        public int getWidth() {
            return delegate.getWidth();
        }

        public int getHeight() {
            return delegate.getHeight();
        }

        public int save() {
            return delegate.save();
        }

        public int save(int saveFlags) {
            return delegate.save(saveFlags);
        }

        public int saveLayer(RectF bounds, Paint paint, int saveFlags) {
            return delegate.saveLayer(bounds, paint, saveFlags);
        }

        public int saveLayer(float left, float top, float right, float
                bottom, Paint paint,
                int saveFlags) {
            return delegate.saveLayer(left, top, right, bottom, paint,
                    saveFlags);
        }

        public int saveLayerAlpha(RectF bounds, int alpha, int saveFlags) {
            return delegate.saveLayerAlpha(bounds, alpha, saveFlags);
        }

        public int saveLayerAlpha(float left, float top, float right,
                float bottom, int alpha,
                int saveFlags) {
            return delegate.saveLayerAlpha(left, top, right, bottom,
                    alpha, saveFlags);
        }

        public void restore() {
            delegate.restore();
        }

        public int getSaveCount() {
            return delegate.getSaveCount();
        }

        public void restoreToCount(int saveCount) {
            delegate.restoreToCount(saveCount);
        }

        public void translate(float dx, float dy) {
            delegate.translate(dx, dy);
        }

        public void scale(float sx, float sy) {
            delegate.scale(sx, sy);
        }

        public void rotate(float degrees) {
            delegate.rotate(degrees);
        }

        public void skew(float sx, float sy) {
            delegate.skew(sx, sy);
        }

        public void concat(Matrix matrix) {
            delegate.concat(matrix);
        }

        public void setMatrix(Matrix matrix) {
            delegate.setMatrix(matrix);
        }

        public void getMatrix(Matrix ctm) {
            delegate.getMatrix(ctm);
        }

        public boolean clipRect(RectF rect, Region.Op op) {
            return delegate.clipRect(rect, op);
        }

        public boolean clipRect(Rect rect, Region.Op op) {
            return delegate.clipRect(rect, op);
        }

        public boolean clipRect(RectF rect) {
            return delegate.clipRect(rect);
        }

        public boolean clipRect(Rect rect) {
            return delegate.clipRect(rect);
        }

        public boolean clipRect(float left, float top, float right,
                float bottom, Region.Op op) {
            return delegate.clipRect(left, top, right, bottom, op);
        }

        public boolean clipRect(float left, float top, float right,
                float bottom) {
            return delegate.clipRect(left, top, right, bottom);
        }

        public boolean clipRect(int left, int top, int right, int bottom) {
            return delegate.clipRect(left, top, right, bottom);
        }

        public boolean clipPath(Path path, Region.Op op) {
            return delegate.clipPath(path, op);
        }

        public boolean clipPath(Path path) {
            return delegate.clipPath(path);
        }

        public boolean clipRegion(Region region, Region.Op op) {
            return delegate.clipRegion(region, op);
        }

        public boolean clipRegion(Region region) {
            return delegate.clipRegion(region);
        }

        public DrawFilter getDrawFilter() {
            return delegate.getDrawFilter();
        }

        public void setDrawFilter(DrawFilter filter) {
            delegate.setDrawFilter(filter);
        }

        /*public GL getGL() {
            return delegate.getGL();
        }
*/
        public boolean quickReject(RectF rect, EdgeType type) {
            return delegate.quickReject(rect, type);
        }

        public boolean quickReject(Path path, EdgeType type) {
            return delegate.quickReject(path, type);
        }

        public boolean quickReject(float left, float top, float right,
                float bottom,
                EdgeType type) {
            return delegate.quickReject(left, top, right, bottom, type);
        }

        public boolean getClipBounds(Rect bounds) {
            return delegate.getClipBounds(bounds);
        }

        public void drawRGB(int r, int g, int b) {
            delegate.drawRGB(r, g, b);
        }

        public void drawARGB(int a, int r, int g, int b) {
            delegate.drawARGB(a, r, g, b);
        }

        public void drawColor(int color) {
            delegate.drawColor(color);
        }

        public void drawColor(int color, PorterDuff.Mode mode) {
            delegate.drawColor(color, mode);
        }

        public void drawPaint(Paint paint) {
            delegate.drawPaint(paint);
        }

        public void drawPoints(float[] pts, int offset, int count,
                Paint paint) {
            delegate.drawPoints(pts, offset, count, paint);
        }

        public void drawPoints(float[] pts, Paint paint) {
            delegate.drawPoints(pts, paint);
        }

        public void drawPoint(float x, float y, Paint paint) {
            delegate.drawPoint(x, y, paint);
        }

        public void drawLine(float startX, float startY, float stopX,
                float stopY, Paint paint) {
            delegate.drawLine(startX, startY, stopX, stopY, paint);
        }

        public void drawLines(float[] pts, int offset, int count, Paint paint) {
            delegate.drawLines(pts, offset, count, paint);
        }

        public void drawLines(float[] pts, Paint paint) {
            delegate.drawLines(pts, paint);
        }

        public void drawRect(RectF rect, Paint paint) {
            delegate.drawRect(rect, paint);
        }

        public void drawRect(Rect r, Paint paint) {
            delegate.drawRect(r, paint);
        }

        public void drawRect(float left, float top, float right, float
                bottom, Paint paint) {
            delegate.drawRect(left, top, right, bottom, paint);
        }

        public void drawOval(RectF oval, Paint paint) {
            delegate.drawOval(oval, paint);
        }

        public void drawCircle(float cx, float cy, float radius, Paint paint) {
            delegate.drawCircle(cx, cy, radius, paint);
        }

        public void drawArc(RectF oval, float startAngle, float
                sweepAngle, boolean useCenter,
                Paint paint) {
            delegate.drawArc(oval, startAngle, sweepAngle, useCenter, paint);
        }

        public void drawRoundRect(RectF rect, float rx, float ry, Paint paint) {
            delegate.drawRoundRect(rect, rx, ry, paint);
        }

        public void drawPath(Path path, Paint paint) {
            delegate.drawPath(path, paint);
        }

        public void drawBitmap(Bitmap bitmap, float left, float top,
                Paint paint) {
            if (paint == null) {
                paint = mSmooth;
            } else {
                paint.setFilterBitmap(true);
            }
            delegate.drawBitmap(bitmap, left, top, paint);
        }

        public void drawBitmap(Bitmap bitmap, Rect src, RectF dst,
                Paint paint) {
            if (paint == null) {
                paint = mSmooth;
            } else {
                paint.setFilterBitmap(true);
            }
            delegate.drawBitmap(bitmap, src, dst, paint);
        }

        public void drawBitmap(Bitmap bitmap, Rect src, Rect dst, Paint paint) {
            if (paint == null) {
                paint = mSmooth;
            } else {
                paint.setFilterBitmap(true);
            }
            delegate.drawBitmap(bitmap, src, dst, paint);
        }

        public void drawBitmap(int[] colors, int offset, int stride,
                int x, int y, int width,
                int height, boolean hasAlpha, Paint paint) {
            if (paint == null) {
                paint = mSmooth;
            } else {
                paint.setFilterBitmap(true);
            }
            delegate.drawBitmap(colors, offset, stride, x, y, width,
                    height, hasAlpha, paint);
        }

        public void drawBitmap(Bitmap bitmap, Matrix matrix, Paint paint) {
            if (paint == null) {
                paint = mSmooth;
            } else {
                paint.setFilterBitmap(true);
            }
            delegate.drawBitmap(bitmap, matrix, paint);
        }

        public void drawBitmapMesh(Bitmap bitmap, int meshWidth, int
                meshHeight, float[] verts,
                int vertOffset, int[] colors, int colorOffset, Paint paint) {
            delegate.drawBitmapMesh(bitmap, meshWidth, meshHeight,
                    verts, vertOffset, colors,
                    colorOffset, paint);
        }

        public void drawVertices(VertexMode mode, int vertexCount,
                float[] verts, int vertOffset,
                float[] texs, int texOffset, int[] colors, int
                colorOffset, short[] indices,
                int indexOffset, int indexCount, Paint paint) {
            delegate.drawVertices(mode, vertexCount, verts,
                    vertOffset, texs, texOffset, colors,
                    colorOffset, indices, indexOffset, indexCount, paint);
        }

        public void drawText(char[] text, int index, int count, float
                x, float y, Paint paint) {
            delegate.drawText(text, index, count, x, y, paint);
        }

        public void drawText(String text, float x, float y, Paint paint) {
            delegate.drawText(text, x, y, paint);
        }

        public void drawText(String text, int start, int end, float x,
                float y, Paint paint) {
            delegate.drawText(text, start, end, x, y, paint);
        }

        public void drawText(CharSequence text, int start, int end,
                float x, float y, Paint paint) {
            delegate.drawText(text, start, end, x, y, paint);
        }

        public void drawPosText(char[] text, int index, int count,
                float[] pos, Paint paint) {
            delegate.drawPosText(text, index, count, pos, paint);
        }

        public void drawPosText(String text, float[] pos, Paint paint) {
            delegate.drawPosText(text, pos, paint);
        }

        public void drawTextOnPath(char[] text, int index, int count,
                Path path, float hOffset,
                float vOffset, Paint paint) {
            delegate.drawTextOnPath(text, index, count, path, hOffset,
                    vOffset, paint);
        }

        public void drawTextOnPath(String text, Path path, float
                hOffset, float vOffset,
                Paint paint) {
            delegate.drawTextOnPath(text, path, hOffset, vOffset, paint);
        }

        public void drawPicture(Picture picture) {
            delegate.drawPicture(picture);
        }

        public void drawPicture(Picture picture, RectF dst) {
            delegate.drawPicture(picture, dst);
        }

        public void drawPicture(Picture picture, Rect dst) {
            delegate.drawPicture(picture, dst);
        }
    }
}
