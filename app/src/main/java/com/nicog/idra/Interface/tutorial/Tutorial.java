package com.nicog.idra.Interface.tutorial;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;
import android.view.View;

import com.nicog.idra.R;


public class Tutorial extends AppCompatActivity {
    private static final int NUM_PAGES = 4;
    private ViewPager2 mPager;
    private FragmentStateAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tutorialstart);
    }

    public void start(View v){
        setContentView(R.layout.tutorial);
        mPager = findViewById(R.id.tutorialViewPager);
        pagerAdapter = new ScreenSlidePagerAdapter(this);
        mPager.setAdapter(pagerAdapter);
    }


    private class ScreenSlidePagerAdapter extends FragmentStateAdapter {

        public ScreenSlidePagerAdapter(FragmentActivity fa) {
            super(fa);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return new ScreenSlidePageFragment(position, mPager);
        }

        @Override
        public int getItemCount() {
            return NUM_PAGES;
        }
    }

}
