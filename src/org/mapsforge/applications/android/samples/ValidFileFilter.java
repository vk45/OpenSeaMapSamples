package org.mapsforge.applications.android.samples;

import org.mapsforge.map.reader.header.FileOpenResult;

import java.io.FileFilter;

/**
 * Created by vkandroidstudioadm on 13.02.14.
 */
public interface  ValidFileFilter extends FileFilter {
    /**
     * @return the result of the last {@link #accept} call (might be null).
     */
    FileOpenResult getFileOpenResult();

}
