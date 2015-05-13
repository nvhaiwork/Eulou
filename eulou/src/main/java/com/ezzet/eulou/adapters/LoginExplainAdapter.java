package com.ezzet.eulou.adapters;

import android.content.Context;

import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;

import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;


import com.ezzet.eulou.R;


public class LoginExplainAdapter extends PagerAdapter {

    private Context mContext;
    private LayoutInflater mInflater;
    private int[] mResources = {
            R.drawable.image_viewpager_login_explain_1,
            R.drawable.image_viewpager_login_explain_2,
            R.drawable.image_viewpager_login_explain_3,
            R.drawable.image_viewpager_login_explain_4
    };

    public LoginExplainAdapter(Context context) {
        // TODO Auto-generated constructor stub

        this.mContext = context;
        this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return mResources.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((LinearLayout) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View itemView = mInflater.inflate(R.layout.viewpager_item, container, false);

        ImageView imageView = (ImageView) itemView.findViewById(R.id.imageView_viewpager_item);
        imageView.setImageResource(mResources[position]);

        container.addView(itemView);

        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((LinearLayout) object);
    }

}
