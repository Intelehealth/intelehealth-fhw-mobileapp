package org.intelehealth.app.activities.IntroActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.chooseLanguageActivity.SplashScreenActivity;
import org.intelehealth.app.activities.onboarding.SetupPrivacyNoteActivity_New;

public class IntroScreensActivity_New extends AppCompatActivity {
    private static final String TAG = "IntroScreensActivityNew";
    private ViewPager viewPager;
    private MyViewPagerAdapter myViewPagerAdapter;
    private LinearLayout dotsLayout;
    private TextView[] dots;
    private ImageView[] dots1;
    private int[] layouts;
    private int page = 0;
    private Handler handler;
    private int delay = 5000;

    Runnable runnable = new Runnable() {
        public void run() {
            if (myViewPagerAdapter.getCount() == page) {
                page = 0;
            } else {
                page++;
            }
            viewPager.setCurrentItem(page, true);
            handler.postDelayed(this, delay);
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro_screens_new_ui2);

        viewPager = findViewById(R.id.pager_intro_screens);
        dotsLayout = findViewById(R.id.layoutDots_intro);
        Button btnSkip = findViewById(R.id.btn_skip_intro);
        ImageView ivBack = findViewById(R.id.iv_back_arrow);
        /*ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(IntroScreensActivity_New.this, SplashScreenActivity.class);
                startActivity(intent);
               finish();
            }
        });*/

        btnSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: ");
                Intent intent = new Intent(IntroScreensActivity_New.this, SetupPrivacyNoteActivity_New.class);
                startActivity(intent);
                finish();
            }
        });
        layouts = new int[]{
                R.layout.layout_first_intro_screen_ui2,
                R.layout.layout_second_intro_screen_ui2,
                R.layout.layout_third_intro_screen_ui2

        };

        addBottomDots1(0);

        myViewPagerAdapter = new MyViewPagerAdapter();
        viewPager.setAdapter(myViewPagerAdapter);
        viewPager.addOnPageChangeListener(viewPagerPageChangeListener);
        handler = new Handler();

        //auto slide layouts


    }
    @Override
    protected void onResume() {
        super.onResume();
        handler.postDelayed(runnable, delay);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable);
    }
    ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {
            addBottomDots1(position);
            page = position;

        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }
    };

    public class MyViewPagerAdapter extends PagerAdapter {
        private LayoutInflater layoutInflater;

        public MyViewPagerAdapter() {
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View view = layoutInflater.inflate(layouts[position], container, false);
            container.addView(view);

            return view;
        }

        @Override
        public int getCount() {
            return layouts.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view == obj;
        }


        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View view = (View) object;
            container.removeView(view);
        }
    }


    private void addBottomDots1(int currentPage) {
        dotsLayout.removeAllViews();

        dots1 = new ImageView[layouts.length];
        for (int i = 0; i < dots1.length; i++) {
            dots1[i] = new ImageView(this);
            dots1[i].setBackgroundResource(R.drawable.ui2_ic_slider_round);
            dots1[i].setMaxHeight(7);
            dots1[i].setMaxWidth(7);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, 8, 0);
            dots1[i].setLayoutParams(lp);
            dotsLayout.addView(dots1[i]);
        }

        if (dots1.length > 0) {
            dots1[currentPage].setBackgroundResource(R.drawable.ui2_ic_slider_bar);

        }
    }
}