package org.mapsforge.applications.android.samples;
/**
 * Copyright 2012 V.Klein
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


import android.graphics.Paint;

/**
 * Created by vkandroidstudioadm on 03.03.14.
 */
public class WayTextContainerOSM {
    final float[] coordinates;
    final Paint paint;
    final String text;

    WayTextContainerOSM(float[] coordinates, String text, Paint paint) {
        this.coordinates = coordinates;
        this.text = text;
        this.paint = paint;
    }
}
