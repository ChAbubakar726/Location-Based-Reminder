package com.nexton.locationbasedreminder.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.nexton.locationbasedreminder.R;
import com.nexton.locationbasedreminder.model.IntroModel;

import java.util.List;



public class IntroAdapter extends PagerAdapter {

    Context mContext;
    List<IntroModel> mIntroList;

    public IntroAdapter(Context mContext, List<IntroModel> mIntroList) {
        this.mContext = mContext;
        this.mIntroList = mIntroList;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layoutScreen = inflater.inflate(R.layout.boarding_layout, null);

        ImageView imgSlide = layoutScreen.findViewById(R.id.introImg_iv);
        TextView title = layoutScreen.findViewById(R.id.introTitle_tv);
        TextView description = layoutScreen.findViewById(R.id.introDesc_tv);

        title.setText(mIntroList.get(position).getIntroTitle());
        description.setText(mIntroList.get(position).getIntroDesc());
        imgSlide.setImageResource(mIntroList.get(position).getIntroImg());

        container.addView(layoutScreen);
        return layoutScreen;

    }

    @Override
    public int getCount() {
        return mIntroList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}
