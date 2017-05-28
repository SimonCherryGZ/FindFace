package com.simoncherry.findface.presenter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.FaceDetector;
import android.util.Log;

import com.simoncherry.findface.model.ImageBean;
import com.simoncherry.findface.contract.MainContract;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import io.realm.RealmList;

/**
 * Created by Simon on 2017/5/28.
 */

public class MainPresenter implements MainContract.Presenter{
    private final static String TAG = MainPresenter.class.getSimpleName();

    private MainContract.View mView;

    public MainPresenter(MainContract.View mView) {
        this.mView = mView;
    }

    @Override
    public void startFaceScanTask(List<String> data) {
        Flowable.fromIterable(data)
                .filter(new Predicate<String>() {
                    @Override
                    public boolean test(@NonNull String s) throws Exception {
                        return s != null;
                    }
                })
                .filter(new Predicate<String>() {
                    @Override
                    public boolean test(@NonNull String s) throws Exception {
                        //Bitmap bitmap = BitmapFactory.decodeFile(s);
                        Bitmap bitmap = getEvenWidthBitmap(s);
                        if (bitmap != null) {
                            //bitmap = bitmap.copy(Bitmap.Config.RGB_565, true);
                            FaceDetector.Face[] faces = new FaceDetector.Face[1];
                            FaceDetector faceDetector = new FaceDetector(bitmap.getWidth(), bitmap.getHeight(), 1);
                            int count = faceDetector.findFaces(bitmap, faces);
                            bitmap.recycle();
                            bitmap = null;
                            if (count > 0) {
                                Log.e(TAG, s + " - has face");
                                return true;
                            }
                        }
                        Log.e(TAG, s + " - no face");
                        return false;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        Log.e(TAG, "onSubscribe");
                        mView.onSubscribe(s);
                    }

                    @Override
                    public void onNext(String s) {
                        Log.e(TAG, s);
                        mView.onImageHasFace(s);
                    }

                    @Override
                    public void onError(Throwable t) {
                        Log.e(TAG, t.toString());
                        mView.onError(t.toString());
                    }

                    @Override
                    public void onComplete() {
                        Log.e(TAG, "onComplete");
                        mView.onComplete();
                    }
                });
    }

    @Override
    public void startFaceScanTask(RealmList<ImageBean> data) {

    }

    private Bitmap getEvenWidthBitmap(String imgUrl) {
        Bitmap srcImg = BitmapFactory.decodeFile(imgUrl);
        Bitmap srcFace = srcImg.copy(Bitmap.Config.RGB_565, true);
        srcImg = null;
        int w = srcFace.getWidth();
        int h = srcFace.getHeight();
        if (w % 2 == 1) {
            w++;
            srcFace = Bitmap.createScaledBitmap(srcFace,
                    srcFace.getWidth()+1, srcFace.getHeight(), false);
        }
        if (h % 2 == 1) {
            h++;
            srcFace = Bitmap.createScaledBitmap(srcFace,
                    srcFace.getWidth(), srcFace.getHeight()+1, false);
        }
        return srcFace;
    }
}
