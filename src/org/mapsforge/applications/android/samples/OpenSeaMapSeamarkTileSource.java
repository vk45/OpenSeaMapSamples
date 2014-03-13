package org.mapsforge.applications.android.samples;

import android.util.Log;

import org.mapsforge.core.graphics.CorruptedInputStreamException;
import org.mapsforge.core.graphics.TileBitmap;
import org.mapsforge.core.model.Tile;
import org.mapsforge.core.util.IOUtils;
import org.mapsforge.map.layer.download.tilesource.AbstractTileSource;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by vkandroidstudioadm on 12.02.14.
 */
public class OpenSeaMapSeamarkTileSource extends AbstractTileSource {
    public static final OpenSeaMapSeamarkTileSource INSTANCE = new OpenSeaMapSeamarkTileSource (new String[] {
            "t1.openseamap.org"}, 80);
    //t1.openseamap.org/seamark
    private static final int PARALLEL_REQUESTS_LIMIT = 8;
    private static final String PROTOCOL = "http";
    private static final int ZOOM_LEVEL_MAX = 18;
    private static final int ZOOM_LEVEL_MIN = 0;

    public OpenSeaMapSeamarkTileSource(String[] hostName, int port) {
        super(hostName, port);
    }

    @Override
    public int getParallelRequestsLimit() {
        return PARALLEL_REQUESTS_LIMIT;
    }

    @Override
    public URL getTileUrl(Tile tile) throws MalformedURLException {
        StringBuilder stringBuilder = new StringBuilder(32);

        stringBuilder.append("/seamark/");
        stringBuilder.append(tile.zoomLevel);
        stringBuilder.append('/');
        stringBuilder.append(tile.tileX);
        stringBuilder.append('/');
        stringBuilder.append(tile.tileY);
        stringBuilder.append(".png");
        String resultUrlStr = stringBuilder.toString();
        Log.i("Seamarks", resultUrlStr);
        return new URL(PROTOCOL, getHostName(), this.port, stringBuilder.toString());

    }

    @Override
    public byte getZoomLevelMax() {
        return ZOOM_LEVEL_MAX;
    }

    @Override
    public byte getZoomLevelMin() {
        return ZOOM_LEVEL_MIN;
    }

    @Override
    public boolean hasAlpha() {
        return true;  // this is a transparent layer
    }


}
