/*
 * Copyright (c) WhatsApp Inc. and its affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.darwin.sample.stickerscode.model;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.util.List;

public class Sticker implements Parcelable {
    public final String imageFileName;
    public final List<String> emojis;
    long size;
    public Uri uri;
    public String path;

    public Sticker(String imageFileName, List<String> emojis) {
        Log.d("MyStickerClass", "Sticker: "+imageFileName);
        this.imageFileName = imageFileName;
        this.emojis = emojis;
    }

    private Sticker(Parcel in) {
        imageFileName = in.readString();
        emojis = in.createStringArrayList();
        size = in.readLong();
        Log.d("MyStickerClass", "Sticker: "+imageFileName+", "+size);
    }

    public static final Creator<Sticker> CREATOR = new Creator<Sticker>() {
        @Override
        public Sticker createFromParcel(Parcel in) {
            return new Sticker(in);
        }

        @Override
        public Sticker[] newArray(int size) {
            return new Sticker[size];
        }
    };

    public void setSize(long size) {
        this.size = size;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(imageFileName);
        dest.writeStringList(emojis);
        dest.writeLong(size);
    }
}
