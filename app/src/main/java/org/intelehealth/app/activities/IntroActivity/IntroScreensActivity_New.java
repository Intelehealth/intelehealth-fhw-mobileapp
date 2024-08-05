package org.intelehealth.app.activities.IntroActivity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.LocaleList;
import android.util.DisplayMetrics;
import org.intelehealth.app.utilities.CustomLog;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.onboarding.SetupPrivacyNoteActivity_New;
import org.intelehealth.app.models.IntroContent;
import org.intelehealth.app.utilities.SessionManager;

import java.util.ArrayList;
import java.util.Locale;

public class IntroScreensActivity_New extends AppCompatActivity {
    private static final String TAG = "IntroScreensActivityNew";
    private ViewPager2 viewPager;
    private ViewPager2.OnPageChangeCallback onPageChangeCallback;
    private MyViewpagerAdapter myViewPagerAdapter;
    private LinearLayout dotsLayout;
    private TextView[] dots;
    private ImageView[] dots1;
    Button btnSkip;
    private int[] layouts;
    private int page = 0;
    private Handler handler;
    private int delay = 7000;
    String appLanguage;
    SessionManager sessionManager = null;

    Runnable runnable = new Runnable() {
        public void run() {
            if (myViewPagerAdapter.getItemCount() == page) {
                page = 0;
            } else {
                page++;
            }
            viewPager.setCurrentItem(page, true);
            handler.postDelayed(this, delay);
        }
    };
    private boolean dotFromCallback = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro_screens_new_ui2);
        viewPager = findViewById(R.id.pager_intro_screens);
        dotsLayout = findViewById(R.id.layoutDots_intro);
        btnSkip = findViewById(R.id.btn_skip_intro);
        sessionManager = new SessionManager(IntroScreensActivity_New.this);
        ImageView ivBack = findViewById(R.id.iv_back_arrow);
        /*ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(IntroScreensActivity_New.this, SplashScreenActivity.class);
                startActivity(intent);
               finish();
            }
        });*/

        appLanguage = sessionManager.getAppLanguage();
        if (!appLanguage.equalsIgnoreCase("")) {
            setLocale(appLanguage);
        }

        btnSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomLog.d(TAG, "onClick: ");
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
        myViewPagerAdapter = new MyViewpagerAdapter(this);
        viewPager.setAdapter(myViewPagerAdapter);
        //viewPager.addOnPageChangeListener(viewPagerPageChangeListener);

        onPageChangeCallback = new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                //dot not showing when its calling first time from here
                //that's why skipping first time call
                if (position == 0 && !dotFromCallback) {
                    dotFromCallback = true;
                } else if (dotFromCallback) {
                    addBottomDots1(position);
                    page = position;
                }

                super.onPageSelected(position);
            }
        };

        viewPager.registerOnPageChangeCallback(onPageChangeCallback);

        handler = new Handler();

        //auto slide layouts


    }


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(setLocale(newBase));
    }

    public Context setLocale(Context context) {
        SessionManager sessionManager1 = new SessionManager(context);
        String appLanguage = sessionManager1.getAppLanguage();
        Resources res = context.getResources();
        Configuration conf = res.getConfiguration();
        Locale locale = new Locale(appLanguage);
        Locale.setDefault(locale);
        conf.setLocale(locale);
        context.createConfigurationContext(conf);
        DisplayMetrics dm = res.getDisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            conf.setLocales(new LocaleList(locale));
        } else {
            conf.locale = locale;
        }
        res.updateConfiguration(conf, dm);
        return context;
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewPager.unregisterOnPageChangeCallback(onPageChangeCallback);
    }

    /*public class MyViewPagerAdapter extends PagerAdapter {
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
    }*/

    public static class MyViewpagerAdapter extends FragmentStateAdapter {

        private final ArrayList<Fragment> introFragments = new ArrayList<>();

        public MyViewpagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
            introFragments.add(SlideFragment.newInstance(IntroContent.getContent(fragmentActivity, ViewType.ONE)));
            introFragments.add(SlideFragment.newInstance(IntroContent.getContent(fragmentActivity, ViewType.TWO)));
            introFragments.add(SlideFragment.newInstance(IntroContent.getContent(fragmentActivity, ViewType.THREE)));
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return introFragments.get(position);
        }

        @Override
        public int getItemCount() {
            return introFragments.size();
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

    public void setLocale(String appLanguage) {
        // here comes en, hi, mr
        Locale locale = new Locale(appLanguage);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
    }
}