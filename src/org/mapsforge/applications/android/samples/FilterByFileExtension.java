package org.mapsforge.applications.android.samples;

/**
 * Created by vkandroidstudioadm on 13.02.14.
 */

import java.io.File;
import java.io.FileFilter;

/**
 * Accepts all readable directories and all readable files with a given extension.
 */
public class FilterByFileExtension implements FileFilter {
    private final String extension;

    /**
     * @param extension
     *            the allowed file name extension.
     */
    public FilterByFileExtension(String extension) {
        this.extension = extension;
    }

    @Override
    public boolean accept(File file) {
        // accept only readable files
        if (file.canRead()) {
            if (file.isDirectory()) {
                // accept all directories
                return true;
            } else if (file.isFile() && file.getName().endsWith(this.extension)) {
                return true;
            }
        }
        return false;
    }
}

