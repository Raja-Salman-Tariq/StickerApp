/*
 * Copyright (c) WhatsApp Inc. and its affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.darwin.sample.stickerscode.utils;


import static com.darwin.sample.stickerscode.model.StickerContentProvider.ANDROID_APP_DOWNLOAD_LINK_IN_QUERY;
import static com.darwin.sample.stickerscode.model.StickerContentProvider.ANIMATED_STICKER_PACK;
import static com.darwin.sample.stickerscode.model.StickerContentProvider.AVOID_CACHE;
import static com.darwin.sample.stickerscode.model.StickerContentProvider.CUSTOM;
import static com.darwin.sample.stickerscode.model.StickerContentProvider.IMAGE_DATA_VERSION;
import static com.darwin.sample.stickerscode.model.StickerContentProvider.IOS_APP_DOWNLOAD_LINK_IN_QUERY;
import static com.darwin.sample.stickerscode.model.StickerContentProvider.LICENSE_AGREENMENT_WEBSITE;
import static com.darwin.sample.stickerscode.model.StickerContentProvider.PRIVACY_POLICY_WEBSITE;
import static com.darwin.sample.stickerscode.model.StickerContentProvider.PUBLISHER_EMAIL;
import static com.darwin.sample.stickerscode.model.StickerContentProvider.PUBLISHER_WEBSITE;
import static com.darwin.sample.stickerscode.model.StickerContentProvider.STICKER_FILE_EMOJI_IN_QUERY;
import static com.darwin.sample.stickerscode.model.StickerContentProvider.STICKER_FILE_NAME_IN_QUERY;
import static com.darwin.sample.stickerscode.model.StickerContentProvider.STICKER_PACK_ICON_IN_QUERY;
import static com.darwin.sample.stickerscode.model.StickerContentProvider.STICKER_PACK_IDENTIFIER_IN_QUERY;
import static com.darwin.sample.stickerscode.model.StickerContentProvider.STICKER_PACK_NAME_IN_QUERY;
import static com.darwin.sample.stickerscode.model.StickerContentProvider.STICKER_PACK_PUBLISHER_IN_QUERY;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;


import com.darwin.sample.BuildConfig;
import com.darwin.sample.stickerscode.model.Sticker;
import com.darwin.sample.stickerscode.model.StickerContentProvider;
import com.darwin.sample.stickerscode.model.StickerPack;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class StickerPackLoader {

    /**
     * Get the list of sticker packs for the sticker content provider
     */
    @NonNull
    public static ArrayList<StickerPack> fetchStickerPacks(Context context) throws IllegalStateException {

        // simply get reference to data from content provider
        final Cursor cursor = context.getContentResolver().query(StickerContentProvider.AUTHORITY_URI, null, null, null, null);
        if (cursor == null) {
            throw new IllegalStateException("could not fetch from content provider, " + BuildConfig.CONTENT_PROVIDER_AUTHORITY);
        }
        HashSet<String> identifierSet = new HashSet<>();
        final ArrayList<StickerPack> stickerPackList = fetchFromContentProvider(cursor); // get packs
        for (StickerPack stickerPack : stickerPackList) {
            if (identifierSet.contains(stickerPack.identifier)) {
                throw new IllegalStateException("sticker pack identifiers should be unique, there are more than one pack with identifier:" + stickerPack.identifier);
            } else {
                identifierSet.add(stickerPack.identifier);
            }
        }
        if (stickerPackList.isEmpty()) {
            throw new IllegalStateException("There should be at least one sticker pack in the app");
        }
        for (StickerPack stickerPack : stickerPackList) {
            final List<Sticker> stickers = getStickersForPack(context, stickerPack);
            stickerPack.setStickers(stickers);
            StickerPackValidator.verifyStickerPackValidity(context, stickerPack);
        }
        return stickerPackList;
    }

    @NonNull
    private static List<Sticker> getStickersForPack(Context context, StickerPack stickerPack) { // get stickers
        final List<Sticker> stickers = fetchFromContentProviderForStickers(stickerPack.identifier, context.getContentResolver());
        for (Sticker sticker : stickers) {
            final byte[] bytes;
            try {
//                Log.d("customStkrchq", stickerPack.identifier+" ??? "+stickerPack.custom);
                if (stickerPack.custom) {
                    Log.d("customStkrchq", "entered custom if: ");
                    File file = context.getExternalFilesDir(
                            "custom_stickers"+File.separator
                                    +stickerPack.identifier+File.separator
                                    +"stickers"+File.separator
                                    +sticker.imageFileName);

                    InputStream inputStream = new FileInputStream(file);
                    final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

                    if (inputStream == null) {
                        throw new IOException("cannot read CUSTOM sticker asset:" + stickerPack.identifier + "/" + sticker.imageFileName);
                    }
                    int read;
                    byte[] data = new byte[16384];

                    while ((read = inputStream.read(data, 0, data.length)) != -1) {
                        buffer.write(data, 0, read);
                    }
                    bytes = buffer.toByteArray();

                } else {
//                    Log.d("customStkrchq", "DID NOT entered custom if: ");
                    bytes = fetchStickerAsset(stickerPack.identifier, sticker.imageFileName, context.getContentResolver());
                }
                if (bytes.length <= 0) {
                    throw new IllegalStateException("Asset file is empty, pack: " + stickerPack.name + ", sticker: " + sticker.imageFileName);
                }
                sticker.setSize(bytes.length);
            } catch (IOException | IllegalArgumentException e) {
                throw new IllegalStateException("Asset file doesn't exist. pack: " + stickerPack.name + ", sticker: " + sticker.imageFileName, e);
            }
        }
        return stickers;
    }


    @NonNull
    private static ArrayList<StickerPack> fetchFromContentProvider(Cursor cursor) { // compose raw data into stickerpacks
        ArrayList<StickerPack> stickerPackList = new ArrayList<>();
        cursor.moveToFirst();
        do {
            final String identifier = cursor.getString(cursor.getColumnIndexOrThrow(STICKER_PACK_IDENTIFIER_IN_QUERY));
            final String name = cursor.getString(cursor.getColumnIndexOrThrow(STICKER_PACK_NAME_IN_QUERY));
            final String publisher = cursor.getString(cursor.getColumnIndexOrThrow(STICKER_PACK_PUBLISHER_IN_QUERY));
            final String trayImage = cursor.getString(cursor.getColumnIndexOrThrow(STICKER_PACK_ICON_IN_QUERY));
            final String androidPlayStoreLink = cursor.getString(cursor.getColumnIndexOrThrow(ANDROID_APP_DOWNLOAD_LINK_IN_QUERY));
            final String iosAppLink = cursor.getString(cursor.getColumnIndexOrThrow(IOS_APP_DOWNLOAD_LINK_IN_QUERY));
            final String publisherEmail = cursor.getString(cursor.getColumnIndexOrThrow(PUBLISHER_EMAIL));
            final String publisherWebsite = cursor.getString(cursor.getColumnIndexOrThrow(PUBLISHER_WEBSITE));
            final String privacyPolicyWebsite = cursor.getString(cursor.getColumnIndexOrThrow(PRIVACY_POLICY_WEBSITE));
            final String licenseAgreementWebsite = cursor.getString(cursor.getColumnIndexOrThrow(LICENSE_AGREENMENT_WEBSITE));
            final String imageDataVersion = cursor.getString(cursor.getColumnIndexOrThrow(IMAGE_DATA_VERSION));
            final boolean avoidCache = cursor.getShort(cursor.getColumnIndexOrThrow(AVOID_CACHE)) > 0;
            final boolean animatedStickerPack = cursor.getShort(cursor.getColumnIndexOrThrow(ANIMATED_STICKER_PACK)) > 0;
            final boolean custom = cursor.getShort(cursor.getColumnIndexOrThrow(CUSTOM)) > 0;
            final StickerPack stickerPack = new StickerPack(identifier, name, publisher, trayImage, publisherEmail, publisherWebsite, privacyPolicyWebsite, licenseAgreementWebsite, imageDataVersion, avoidCache, animatedStickerPack);
            stickerPack.custom = custom;
            stickerPack.setAndroidPlayStoreLink(androidPlayStoreLink);
            stickerPack.setIosAppStoreLink(iosAppLink);
            stickerPackList.add(stickerPack);

            if (identifier.equals("stickerPack0")) {
                Log.d("customStkrchq",
                        "ID = "+identifier+"\n"+
                                "name = "+name+"\n"+
                                "pub = "+publisher+"\n"+
                                "tray = "+trayImage+"\n"+
                                "play store link = "+androidPlayStoreLink+"\n"+
                                "ios link = "+iosAppLink+"\n"+
                                "pubemail = "+publisherEmail+"\n"+
                                "pub site ="+publisherWebsite+"\n"+
                                "privacy = "+privacyPolicyWebsite+"\n"+
                                "liscense = "+licenseAgreementWebsite+"\n"+
                                "im data ver = "+imageDataVersion+"\n"+
                                "avoid cache = "+avoidCache+"\n"+
                                "animated = "+animatedStickerPack+"\n"+
                                "custom = "+custom
                );
            }

        } while (cursor.moveToNext());

        return stickerPackList;
    }

    @NonNull
    private static List<Sticker> fetchFromContentProviderForStickers(String identifier, ContentResolver contentResolver) { // compose raw data into stickers
        Uri uri = getStickerListUri(identifier);

        final String[] projection = {STICKER_FILE_NAME_IN_QUERY, STICKER_FILE_EMOJI_IN_QUERY};
        final Cursor cursor = contentResolver.query(uri, projection, null, null, null);
        List<Sticker> stickers = new ArrayList<>();
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                final String name = cursor.getString(cursor.getColumnIndexOrThrow(STICKER_FILE_NAME_IN_QUERY));
                final String emojisConcatenated = cursor.getString(cursor.getColumnIndexOrThrow(STICKER_FILE_EMOJI_IN_QUERY));
                List<String> emojis = new ArrayList<>(StickerPackValidator.EMOJI_MAX_LIMIT);
                if (!TextUtils.isEmpty(emojisConcatenated)) {
                    emojis = Arrays.asList(emojisConcatenated.split(","));
                }
                stickers.add(new Sticker(name, emojis));
            } while (cursor.moveToNext());
        }
        if (cursor != null) {
            cursor.close();
        }
        return stickers;
    }

    static byte[] fetchStickerAsset(@NonNull final String identifier, @NonNull final String name, ContentResolver contentResolver) throws IOException {
        try (final InputStream inputStream = contentResolver.openInputStream(getStickerAssetUri(identifier, name));
             final ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            if (inputStream == null) {
                throw new IOException("cannot read sticker asset:" + identifier + "/" + name);
            }
            int read;
            byte[] data = new byte[16384];

            while ((read = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, read);
            }
            return buffer.toByteArray();
        }
    }

    private static Uri getStickerListUri(String identifier) {
        return new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT).authority(BuildConfig.CONTENT_PROVIDER_AUTHORITY).appendPath(StickerContentProvider.STICKERS).appendPath(identifier).build();
    }

    public static Uri getStickerAssetUri(String identifier, String stickerName) {
        return new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT).authority(BuildConfig.CONTENT_PROVIDER_AUTHORITY).appendPath(StickerContentProvider.STICKERS_ASSET).appendPath(identifier).appendPath(stickerName).build();
    }
}
