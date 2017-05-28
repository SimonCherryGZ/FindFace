package com.simoncherry.findface.presenter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.media.FaceDetector;
import android.util.Log;

import com.simoncherry.findface.contract.MainContract;
import com.simoncherry.findface.model.ImageBean;
import com.simoncherry.findface.model.SkipBean;
import com.simoncherry.findface.util.BitmapUtils;
import com.simoncherry.findface.util.ViewUtils;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;
import io.realm.RealmList;

/**
 * Created by Simon on 2017/5/28.
 */

public class MainPresenter implements MainContract.Presenter{
    private final static String TAG = MainPresenter.class.getSimpleName();

    private Context mContext;
    private MainContract.View mView;

    public MainPresenter(Context mContext, MainContract.View mView) {
        this.mContext = mContext;
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
                        Bitmap bitmap = BitmapUtils.getEvenWidthBitmap(s);
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
        Flowable.fromIterable(data)
                .filter(new Predicate<ImageBean>() {
                    @Override
                    public boolean test(@NonNull ImageBean imageBean) throws Exception {
                        return imageBean != null && imageBean.isNotNull();
                    }
                })
                .filter(new Predicate<ImageBean>() {  // 有人脸的表中不存在
                    @Override
                    public boolean test(@NonNull ImageBean imageBean) throws Exception {
                        Realm realm = Realm.getDefaultInstance();
                        boolean isNotExist = realm.where(ImageBean.class).equalTo("id", imageBean.getId()).findFirst() == null;
                        realm.close();
                        if (!isNotExist) {
                            Log.e(TAG, imageBean.getId() + " 此图片已检测过存在人脸，跳过");
                        }
                        return isNotExist;
                    }
                })
                .filter(new Predicate<ImageBean>() {  // 没有人脸的表中不存在
                    @Override
                    public boolean test(@NonNull ImageBean imageBean) throws Exception {
                        Realm realm = Realm.getDefaultInstance();
                        boolean isNotExist = realm.where(SkipBean.class).equalTo("id", imageBean.getId()).findFirst() == null;
                        realm.close();
                        if (!isNotExist) {
                            Log.e(TAG, imageBean.getId() + " 此图片已检测过不存在人脸，跳过");
                        }
                        return isNotExist;
                    }
                })
                .filter(new Predicate<ImageBean>() {
                    @Override
                    public boolean test(@NonNull final ImageBean imageBean) throws Exception {
                        String path = imageBean.getPath();
                        //Bitmap bitmap = BitmapUtils.getEvenWidthBitmap(path);
                        int screenWidth = ViewUtils.getScreenWidth(mContext);
                        Bitmap bitmap = BitmapUtils.getRequireWidthBitmap(BitmapUtils.getEvenWidthBitmap(path), screenWidth);
                        if (bitmap != null) {
                            FaceDetector.Face[] faces = new FaceDetector.Face[1];
                            FaceDetector faceDetector = new FaceDetector(bitmap.getWidth(), bitmap.getHeight(), 1);
                            int count = faceDetector.findFaces(bitmap, faces);
                            bitmap.recycle();
                            bitmap = null;
                            if (count > 0) {
                                Log.e(TAG, path + " - has face");
                                return true;
                            } else {
                                Realm realm = Realm.getDefaultInstance();
                                realm.executeTransaction(new Realm.Transaction() {
                                    @Override
                                    public void execute(Realm realm) {
                                        SkipBean skipBean = new SkipBean(imageBean.getId(), imageBean.getPath(), imageBean.getName(), imageBean.getDate());
                                        realm.copyToRealmOrUpdate(skipBean);
                                    }
                                });
                                realm.close();
                            }
                        }
                        Log.e(TAG, path + " - no face");
                        return false;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ImageBean>() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        Log.e(TAG, "onSubscribe");
                        mView.onSubscribe(s);
                    }

                    @Override
                    public void onNext(ImageBean imageBean) {
                        Log.e(TAG, imageBean.toString());
                        mView.onImageHasFace(imageBean);
                    }

                    @Override
                    public void onError(Throwable t) {
                        Log.e(TAG, t.toString());
                        mView.onError("Rx Exception: " + t.toString());
                    }

                    @Override
                    public void onComplete() {
                        Log.e(TAG, "onComplete");
                        mView.onComplete();
                    }
                });
    }

    @Override
    public void drawFaceArea(String path) {
        Flowable.just(path)
                .filter(new Predicate<String>() {
                    @Override
                    public boolean test(@NonNull String s) throws Exception {
                        return s != null;
                    }
                })
                .map(new Function<String, Bitmap>() {
                    @Override
                    public Bitmap apply(@NonNull String s) throws Exception {
                        //Bitmap bitmap = BitmapUtils.getEvenWidthBitmap(s);
                        int screenWidth = ViewUtils.getScreenWidth(mContext);
                        Bitmap bitmap = BitmapUtils.getRequireWidthBitmap(BitmapUtils.getEvenWidthBitmap(s), screenWidth);
                        if (bitmap != null) {
                            FaceDetector.Face[] faces = new FaceDetector.Face[1];
                            FaceDetector faceDetector = new FaceDetector(bitmap.getWidth(), bitmap.getHeight(), 1);
                            int count = faceDetector.findFaces(bitmap, faces);
                            if (count > 0) {
                                Canvas canvas = new Canvas(bitmap);
                                Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                                mPaint.setColor(Color.YELLOW);
                                mPaint.setStrokeWidth(5);
                                mPaint.setStyle(Paint.Style.STROKE);

                                canvas.drawBitmap(bitmap, 0, 0, mPaint);
                                for (int i = 0; i < count; i++){
                                    //双眼的中心点
                                    PointF midPoint = new PointF();
                                    faces[i].getMidPoint(midPoint);
                                    //双眼的距离
                                    float eyeDistance = faces[i].eyesDistance();
                                    //画矩形
                                    canvas.drawRect(midPoint.x - eyeDistance, midPoint.y - eyeDistance, midPoint.x + eyeDistance, midPoint.y + eyeDistance, mPaint);
                                }
                                return bitmap;
                            }
                        }
                        return null;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Bitmap>() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        s.request(1);
                    }

                    @Override
                    public void onNext(Bitmap bitmap) {
                        mView.onDrawFaceArea(bitmap);
                    }

                    @Override
                    public void onError(Throwable t) {
                        Log.e(TAG, "Rx Exception: " + t.toString());
                        mView.onError("Rx Exception: " + t.toString());
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }
}
