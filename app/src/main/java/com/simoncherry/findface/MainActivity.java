package com.simoncherry.findface;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.FaceDetector;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private final static String TAG = MainActivity.class.getSimpleName();

    private Context mContext;

    private Button mBtnTest;
    private Button mBtnSheet;
    private RecyclerView mRecyclerView;
    private GalleryAdapter mGalleryAdapter;
    private BottomSheetDialog mBottomSheetDialog;

    private List<String> mData = new ArrayList<>();
    private MediaLoaderCallback mediaLoaderCallback;

    private Subscription mSubscription = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = MainActivity.this;

        mBtnTest = (Button) findViewById(R.id.btn_test);
        mBtnSheet = (Button) findViewById(R.id.btn_sheet);

        initView();
        //loadLocalImage();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSubscription != null) {
            mSubscription.cancel();
        }
    }

    private void initView() {
        mBtnTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadLocalImage();
            }
        });

        mBtnSheet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBottomSheet();
            }
        });

        mGalleryAdapter = new GalleryAdapter(mContext, mData);
        mGalleryAdapter.setOnItemClickListener(new GalleryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String url) {
                Toast.makeText(mContext, url, Toast.LENGTH_SHORT).show();
            }
        });

        mRecyclerView = (RecyclerView) LayoutInflater.from(mContext)
                .inflate(R.layout.layout_bottom_sheet, null);
        mRecyclerView.setAdapter(mGalleryAdapter);
        mRecyclerView.setLayoutManager(new GridLayoutManager(mContext, 3));

        mBottomSheetDialog = new BottomSheetDialog(mContext);
        mBottomSheetDialog.setContentView(mRecyclerView);
    }

    private void showBottomSheet() {
        mBottomSheetDialog.show();
    }

    private void loadLocalImage() {
        mediaLoaderCallback = new MediaLoaderCallback(mContext);
        mediaLoaderCallback.setOnLoadFinishedListener(new MediaLoaderCallback.OnLoadFinishedListener() {
            @Override
            public void onLoadFinished(List<String> data) {
                Toast.makeText(mContext, "total: " + data.size(), Toast.LENGTH_SHORT).show();
//                mData.addAll(data);
//                mGalleryAdapter.notifyDataSetChanged();
                startFaceScanTask(data);
            }
        });
        getSupportLoaderManager().initLoader(0, null, mediaLoaderCallback);
    }

    private void startFaceScanTask(final List<String> data) {
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
                        Bitmap bitmap = BitmapFactory.decodeFile(s);
                        if (bitmap != null) {
                            bitmap = bitmap.copy(Bitmap.Config.RGB_565, true);
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
//                .map(new Function<String, String>() {
//                    @Override
//                    public String apply(@NonNull String s) throws Exception {
//                        //Bitmap bitmap = BitmapFactory.decodeFile(s);
//                        Bitmap bitmap = getEvenWidthBitmap(s);
//                        if (bitmap != null) {
//                            FaceDetector.Face[] faces = new FaceDetector.Face[1];
//                            FaceDetector faceDetector = new FaceDetector(bitmap.getWidth(), bitmap.getHeight(), 1);
//                            int count = faceDetector.findFaces(bitmap, faces);
//                            bitmap.recycle();
//                            bitmap = null;
//                            if (count > 0) {
//                                return s + " - has face";
//                            }
//                        }
//                        return s + " - no face";
//                    }
//                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        Log.e(TAG, "onSubscribe");
                        mSubscription = s;
                        mSubscription.request(Long.MAX_VALUE);
                    }

                    @Override
                    public void onNext(String s) {
                        Log.e(TAG, s);
                        mData.add(s);
                        mGalleryAdapter.notifyItemChanged(mData.size()-1);
                    }

                    @Override
                    public void onError(Throwable t) {
                        Log.e(TAG, t.toString());
                        Toast.makeText(mContext, t.toString(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onComplete() {
                        Log.e(TAG, "onComplete");
                    }
                });
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
