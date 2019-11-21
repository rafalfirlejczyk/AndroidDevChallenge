/*
 * Copyright (c) 2016 Qualcomm Technologies, Inc.
 * All Rights Reserved.
 * Confidential and Proprietary - Qualcomm Technologies, Inc.
 */
package com.zebra.qti.snpe.IQclassifiers;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.File;

public class Model implements Parcelable {

    public static final Uri MODELS_URI = Uri.parse("content://snpe/models");

    public static final String INVALID_ID = "null";

    public File file;
    public String[] labels;
    public File[] rawImages;
    public File[] jpgImages;
    public String name;
    public File meanImage;

    protected Model(Parcel in) {
        name = in.readString();
        file = new File(in.readString());

        final String[] rawPaths = new String[in.readInt()];
        in.readStringArray(rawPaths);
        rawImages = fromPaths(rawPaths);

        final String[] jpgPaths = new String[in.readInt()];
        in.readStringArray(jpgPaths);
        jpgImages = fromPaths(jpgPaths);

        meanImage = new File(in.readString());

        labels = new String[in.readInt()];
        in.readStringArray(labels);
    }

    public Model() {}

    @Override
    //updated for length null check SC 040519
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(file.getAbsolutePath());
        if(rawImages!=null) {
            dest.writeInt(rawImages.length);
        }

//        dest.writeInt(rawImages.length);

        dest.writeStringArray(toPaths(rawImages));
        if(jpgImages!=null) {
            dest.writeInt(jpgImages.length);
        }

//        dest.writeInt(jpgImages.length);
        dest.writeStringArray(toPaths(jpgImages));
        dest.writeString(meanImage.getAbsolutePath());
        if(labels!=null) {
            dest.writeInt(labels.length);
        }

//        dest.writeInt(labels.length);
        dest.writeStringArray(labels);
    }

    //updated SC 040519 to handle null length
    private File[] fromPaths(String[] paths) {
        if(paths!=null){
        final File[] files = new File[paths.length];
        for (int i = 0; i < paths.length; i++) {
            files[i] = new File(paths[i]);
        }
        return files;}
        else {
            return null;
        }
    }

    //updated SC 040519: to handle null length
    private String[] toPaths(File[] files) {
        if(files!=null){
        final String[] paths = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            paths[i] = files[i].getAbsolutePath();
        }
        return paths;}
        else{
            return null;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Model> CREATOR = new Creator<Model>() {
        @Override
        public Model createFromParcel(Parcel in) {
            return new Model(in);
        }

        @Override
        public Model[] newArray(int size) {
            return new Model[size];
        }
    };

    @Override
    public String toString() {
        return name.toUpperCase();
    }
}
