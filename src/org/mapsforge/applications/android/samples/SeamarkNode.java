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
import org.mapsforge.core.model.Tag;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Created by vkandroidstudioadm on 27.02.14.
 */
public class SeamarkNode {
    private String mNodeNumber;
    private int  mLatitudeE6 ;
    private int mLongitudeE6;
    private ArrayList<Tag> mTagList;
    private LinkedHashMap<String,String> mTagDictionary;


    private boolean mIsVisible = false;

    public SeamarkNode (String nodeNumber){
        mNodeNumber = nodeNumber;
        mTagDictionary = new LinkedHashMap<String,String>();
        mTagList = new ArrayList<Tag>();
    }

    public String getId() {
        return mNodeNumber;
    }


    public void setVisibility(boolean visible){
        mIsVisible= visible;
    }
    public boolean getVisibility() {
        return mIsVisible;
    }


    public void setLatitudeE6( int lat) {
        mLatitudeE6 = lat;
    }

    public int getLatitudeE6 (){
        return mLatitudeE6;
    }

    public void setLongitudeE6 (int lon) {
        mLongitudeE6 = lon;
    }

    public int getLongitudeE6 (){
        return mLongitudeE6;
    }

    public void addTag(Tag tag) {
        String key = tag.key;
        String value = tag.value;
        mTagDictionary.put(key, value);
        mTagList.add(tag);

    }

    public Tag getTag(int index) {
        Tag result = null;
        if (index > -1 && index < mTagList.size()) {
            result = mTagList.get(index);
        }
        return result;
    }

    public int getTagListSize(){
        return mTagList.size();
    }

    public String getValueToKey(String key){
        String result = null;
        if (mTagDictionary.containsKey(key)){
            result = mTagDictionary.get(key);
        }
        return result;
    }

    public LinkedHashMap<String,String> getTagDictionary() {
        return mTagDictionary;
    }

    public void clear() {
        mTagDictionary.clear();
        mTagList.clear();
    }
}
