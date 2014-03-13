package org.mapsforge.applications.android.samples;

/**
 * Created by vkandroidstudioadm on 13.02.14.
 */

import org.mapsforge.map.reader.MapDatabase;
import org.mapsforge.map.reader.header.FileOpenResult;

import java.io.File;

/**
 * Accepts all valid map files.
 */
public final class ValidMapFile implements ValidFileFilter {
    private FileOpenResult fileOpenResult;

    @Override
    public boolean accept(File mapFile) {
        MapDatabase mapDatabase = new MapDatabase();
        this.fileOpenResult = mapDatabase.openFile(mapFile);
        mapDatabase.closeFile();
        return this.fileOpenResult.isSuccess();
    }

    @Override
    public FileOpenResult getFileOpenResult() {
        return this.fileOpenResult;
    }

}
