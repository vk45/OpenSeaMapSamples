package org.mapsforge.applications.android.samples;

import org.mapsforge.core.model.Tile;
import org.mapsforge.map.layer.download.tilesource.AbstractTileSource;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by vkandroidstudioadm on 12.02.14.
 */
public class OpenSeaMapBaseTileSource extends AbstractTileSource {
    public static final OpenSeaMapBaseTileSource INSTANCE = new OpenSeaMapBaseTileSource (new String[] {
            "osm2.wtnet.de"}, 80);
    //t1.openseamap.org/seamark
    private static final int PARALLEL_REQUESTS_LIMIT = 8;
    private static final String PROTOCOL = "http";
    private static final int ZOOM_LEVEL_MAX = 18;
    private static final int ZOOM_LEVEL_MIN = 0;

    public OpenSeaMapBaseTileSource(String[] hostName, int port) {
        super(hostName, port);
    }

    @Override
    public int getParallelRequestsLimit() {
        return PARALLEL_REQUESTS_LIMIT;
    }

    @Override
    public URL getTileUrl(Tile tile) throws MalformedURLException {
        StringBuilder stringBuilder = new StringBuilder(32);

        stringBuilder.append("/tiles/base/");
        stringBuilder.append(tile.zoomLevel);
        stringBuilder.append('/');
        stringBuilder.append(tile.tileX);
        stringBuilder.append('/');
        stringBuilder.append(tile.tileY);
        stringBuilder.append(".png");

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
        return false;  // the base layer is not transparent
    }
}
