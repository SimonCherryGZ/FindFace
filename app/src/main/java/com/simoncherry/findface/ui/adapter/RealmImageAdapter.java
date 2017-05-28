package com.simoncherry.findface.ui.adapter;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.simoncherry.findface.R;
import com.simoncherry.findface.model.ImageBean;

import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

/**
 * Created by Simon on 2017/5/28.
 */

public class RealmImageAdapter extends RealmRecyclerViewAdapter<ImageBean, RealmImageAdapter.MyViewHolder> {

    private Context mContext;

    public RealmImageAdapter(@Nullable OrderedRealmCollection<ImageBean> data, boolean autoUpdate, Context mContext) {
        super(data, autoUpdate);
        setHasStableIds(true);
        this.mContext = mContext;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_img, parent, false));
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        final ImageBean obj = getItem(position);
//        holder.data = obj;
//        if (obj != null) {
//            GlideApp.with(mContext).load(obj.getPath())
//                    .placeholder(R.mipmap.ic_launcher)
//                    .error(R.mipmap.ic_launcher)
//                    .into(holder.ivImg);
//
//            holder.ivImg.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    if (onItemClickListener != null) {
//                        onItemClickListener.onItemClick(obj.getPath());
//                    }
//                }
//            });
//        } else {
//            holder.ivImg.setImageResource(0);
//        }
        Glide.with(mContext).load(obj.getPath())
                .placeholder(R.mipmap.ic_launcher)
                .error(R.mipmap.ic_launcher)
                .into(holder.ivImg);

        holder.ivImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(obj.getPath());
                }
            }
        });
    }

    @Override
    public long getItemId(int index) {
        return getItem(index).getId();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivImg;
        public ImageBean data;

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
