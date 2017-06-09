package com.simoncherry.findface.ui.activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.simoncherry.findface.MediaLoaderCallback;
import com.simoncherry.findface.R;
import com.simoncherry.findface.contract.MainContract;
import com.simoncherry.findface.model.ImageBean;
import com.simoncherry.findface.presenter.MainPresenter;
import com.simoncherry.findface.ui.adapter.GalleryAdapter;
import com.simoncherry.findface.ui.adapter.ImageAdapter;
import com.simoncherry.findface.ui.custom.CustomBottomSheet;
import com.simoncherry.findface.util.BitmapUtils;
import com.simoncherry.findface.util.CrazyClick;
import com.simoncherry.findface.util.FileUtils;
import com.simoncherry.findface.util.JNIUtils;

import org.reactivestreams.Subscription;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmList;
import io.realm.RealmResults;

public class MainActivity extends AppCompatActivity implements MainContract.View {

    private final static String TAG = MainActivity.class.getSimpleName();

    private Context mContext;
    private MainPresenter mPresenter;
    private Unbinder mUnbinder;

    @BindView(R.id.btn_test) Button mBtnTest;
    @BindView(R.id.btn_sheet) Button mBtnSheet;
    @BindView(R.id.btn_jni) Button mBtnJNI;
    @BindView(R.id.iv_result) ImageView mIvResult;
    RecyclerView mRecyclerView;
    TextView mTvHint;

    private GalleryAdapter mGalleryAdapter;
    private ImageAdapter mImageAdapter;
    private CustomBottomSheet mBottomSheetDialog;

    private List<String> mData = new ArrayList<>();
    private List<ImageBean> mImages = new ArrayList<>();
    private MediaLoaderCallback mediaLoaderCallback = null;

    private Subscription mSubscription = null;

    private Realm realm;
    private RealmResults<ImageBean> realmResults;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mUnbinder = ButterKnife.bind(this);
        mContext = MainActivity.this;
        realm = Realm.getDefaultInstance();
        mPresenter = new MainPresenter(this, this);

        initView();
        initRealm();
        initRawFile();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSubscription != null) {
            mSubscription.cancel();
        }
        mRecyclerView.setAdapter(null);
        realmResults.removeAllChangeListeners();
        realm.close();
        mUnbinder.unbind();
    }

    private void initView() {
        mBtnTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (CrazyClick.isCrazy()) {
                    return;
                }
                if (mediaLoaderCallback == null) {
                    loadLocalImage();
                }
            }
        });

        mBtnSheet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBottomSheet();
            }
        });

        mBtnJNI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doGrayScale();
            }
        });

        mGalleryAdapter = new GalleryAdapter(mContext, mData);
        mGalleryAdapter.setOnItemClickListener(new GalleryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String url) {
                Toast.makeText(mContext, url, Toast.LENGTH_SHORT).show();
            }
        });

        mImageAdapter = new ImageAdapter(mContext, mImages);
        mImageAdapter.setOnItemClickListener(new ImageAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String path) {
                Toast.makeText(mContext, path, Toast.LENGTH_SHORT).show();
                mBottomSheetDialog.dismiss();
                mIvResult.setImageResource(R.mipmap.ic_launcher);
                mPresenter.drawFaceArea3(path);
            }
        });

        View sheetView = LayoutInflater.from(mContext)
                .inflate(R.layout.layout_bottom_sheet, null);
        mTvHint = (TextView) sheetView.findViewById(R.id.tv_hint);
        mRecyclerView = (RecyclerView) sheetView.findViewById(R.id.rv_gallery);

        //mRecyclerView.setAdapter(mGalleryAdapter);
        mRecyclerView.setAdapter(mImageAdapter);

        mRecyclerView.setLayoutManager(new GridLayoutManager(mContext, 3));

        mBottomSheetDialog = new CustomBottomSheet(mContext);
        mBottomSheetDialog.setContentView(sheetView);
    }

    private void initRealm() {
        realmResults = realm.where(ImageBean.class).findAllAsync();
        realmResults.addChangeListener(new RealmChangeListener<RealmResults<ImageBean>>() {
            @Override
            public void onChange(RealmResults<ImageBean> results) {
                if (results.size() > 0) {
                    Log.e(TAG, "results size: " + results.size());
                    mImages.clear();
                    mImages.addAll(results.subList(0, results.size()));
                    if (mImageAdapter != null) {
                        mImageAdapter.notifyDataSetChanged();
                        Log.e(TAG, "getItemCount: " + mImageAdapter.getItemCount());
                    }
                }
            }
        });
    }

    private void initRawFile() {
        File dstDir = getDir("file", Context.MODE_PRIVATE);
        File myFile = new File(dstDir, "lbpcascade_frontalface.xml");
        String path = myFile.getAbsolutePath();  // "/data/data/com.simoncherry.findface/app_file/lbpcascade_frontalface.xml"
        Log.e(TAG, "raw file path: " + path);
        if (!myFile.exists()) {
            Log.e(TAG, "raw file not exist, copy it");
            FileUtils.copyFileFromRawToOthers(mContext, R.raw.lbpcascade_frontalface, myFile.getAbsolutePath());
        } else {
            Log.e(TAG, "raw file exist");
        }
    }

    private void showBottomSheet() {
        mBottomSheetDialog.show();
    }

    private void loadLocalImage() {
        mediaLoaderCallback = new MediaLoaderCallback(mContext);
        mediaLoaderCallback.setOnLoadFinishedListener(new MediaLoaderCallback.OnLoadFinishedListener() {
            @Override
            public void onLoadFinished(List<String> data) {
                Toast.makeText(mContext, "Total Size: " + data.size(), Toast.LENGTH_SHORT).show();
                mPresenter.startFaceScanTask(data);
            }

            @Override
            public void onLoadFinished(RealmList<ImageBean> data) {
                Toast.makeText(mContext, "Total Size: " + data.size(), Toast.LENGTH_SHORT).show();
                mPresenter.startFaceScanTask3(data);
            }
        });
        getSupportLoaderManager().initLoader(0, null, mediaLoaderCallback);
    }

    private void doGrayScale() {
        Bitmap bitmap = BitmapUtils.getViewBitmap(mIvResult);
        int w = bitmap.getWidth(), h = bitmap.getHeight();
        int[] pix = new int[w * h];
        bitmap.getPixels(pix, 0, w, 0, 0, w, h);

        int [] resultPixes = JNIUtils.doGrayScale(pix, w, h);
        showProcessResult(resultPixes, w, h);
    }

    private void showProcessResult(int[] resultPixes, int w, int h) {
        Bitmap result = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
        result.setPixels(resultPixes, 0, w, 0, 0, w, h);
        mIvResult.setImageBitmap(result);
    }

    @Override
    public void onSubscribe(Subscription subscription) {
        mSubscription = subscription;
        mSubscription.request(Long.MAX_VALUE);
    }

    @Override
    public void onImageHasFace(String path) {
        mData.add(path);
        mGalleryAdapter.notifyItemChanged(mData.size()-1);
    }

    @Override
    public void onImageHasFace(final ImageBean imageBean) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealmOrUpdate(imageBean);
            }
        });
    }

    @Override
    public void onError(String error) {
        Toast.makeText(mContext, "Rx Exception: " + error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onComplete() {
        Log.e(TAG, "onComplete");
    }

    @Override
    public void onDrawFaceArea(Bitmap bitmap) {
        if (bitmap == null) {
            mIvResult.setImageResource(R.mipmap.ic_launcher);
            Toast.makeText(mContext, "无法读取图片，或没有检测到人脸", Toast.LENGTH_SHORT).show();
        } else {
            mIvResult.setImageBitmap(bitmap);
        }
    }
}
