package com.simoncherry.findface.contract;

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
        void onError(String error);
        void onComplete();
    }

    interface Presenter {
        void startFaceScanTask(final List<String> data);
        void startFaceScanTask(final RealmList<ImageBean> data);
    }
}
