package com.simoncherry.findface.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.simoncherry.GlideApp;
import com.simoncherry.findface.R;
import com.simoncherry.findface.model.ImageBean;

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
public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.MyViewHolder>{

    private Context mContext;
    private List<ImageBean> mData;

    public ImageAdapter(Context mContext, List<ImageBean> mData) {
        this.mContext = mContext;
        this.mData = mData;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(mContext)
                .inflate(R.layout.item_img, parent, false));
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        final ImageBean bean = mData.get(position);
        if (bean != null && bean.isNotNull()) {
            GlideApp.with(mContext).load(bean.getPath())
                    .placeholder(R.mipmap.ic_launcher)
                    .error(R.mipmap.ic_launcher)
                    .into(holder.ivImg);

            holder.ivImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClick(bean.getPath());
                    }
                }
            });
        } else {
            holder.ivImg.setImageResource(0);
        }
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivImg;

        MyViewHolder(View itemView) {
            super(itemView);
            ivImg = (ImageView) itemView.findViewById(R.id.iv_img);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(String url);
    }

    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
}
