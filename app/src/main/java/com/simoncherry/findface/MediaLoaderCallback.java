package com.simoncherry.findface;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 *     author : Donald
 *     e-mail : xxx@xx
 *     time   : 2017/05/27
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class MediaLoaderCallback implements LoaderManager.LoaderCallbacks<Cursor> {

    private Context mContext;

    public MediaLoaderCallback(Context mContext) {
        this.mContext = mContext;
    }

    private final String[] IMAGE_PROJECTION = {
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.MIME_TYPE,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media._ID };

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        CursorLoader cursorLoader = new CursorLoader(mContext,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_PROJECTION,
                IMAGE_PROJECTION[4]+">0 AND "+IMAGE_PROJECTION[3]+"=? OR "+IMAGE_PROJECTION[3]+"=? ",
                new String[]{"image/jpeg", "image/png"}, IMAGE_PROJECTION[2] + " DESC");
        return cursorLoader;
    }

    private boolean fileExist(String path){
        if(!TextUtils.isEmpty(path)){
            return new File(path).exists();
        }
        return false;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null) {
            if (data.getCount() > 0) {
                List<String> images = new ArrayList<>();
                data.moveToFirst();
                do {
                    String path = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[0]));
                    String name = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[1]));
                    long dateTime = data.getLong(data.getColumnIndexOrThrow(IMAGE_PROJECTION[2]));
                    if (fileExist(path)) {
                        images.add(path);
                    }

                } while (data.moveToNext());

                if (onLoadFinishedListener != null) {
                    onLoadFinishedListener.onLoadFinished(images);
                }
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    public interface OnLoadFinishedListener {
        void onLoadFinished(List<String> data);
    }

    private OnLoadFinishedListener onLoadFinishedListener;

    public void setOnLoadFinishedListener(OnLoadFinishedListener onLoadFinishedListener) {
        this.onLoadFinishedListener = onLoadFinishedListener;
    }
}
