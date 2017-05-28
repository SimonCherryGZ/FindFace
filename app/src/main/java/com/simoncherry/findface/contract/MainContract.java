package com.simoncherry.findface.contract;

import android.graphics.Bitmap;

import com.simoncherry.findface.model.ImageBean;

import org.reactivestreams.Subscription;

import java.util.List;

import io.realm.RealmList;

/**
 * Created by Simon on 2017/5/28.
 */

public interface MainContract {
    interface View {
        void onSubscribe(Subscription subscription);
        void onImageHasFace(String path);
        void onImageHasFace(ImageBean imageBean);
        void onError(String error);
        void onComplete();
        void onDrawFaceArea(Bitmap bitmap);
    }

    interface Presenter {
        void startFaceScanTask(final List<String> data);
        void startFaceScanTask(final RealmList<ImageBean> data);
        void drawFaceArea(String path);
    }
}
