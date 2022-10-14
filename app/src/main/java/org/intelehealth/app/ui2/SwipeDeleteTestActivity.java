package org.intelehealth.app.ui2;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.app.R;

import java.util.ArrayList;
import java.util.List;

public class SwipeDeleteTestActivity extends AppCompatActivity {
    private static final String TAG = "SwipeDeleteTestActivity";
    private RecyclerTouchListener touchListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_swipe_delete_test);

        List<String> dataList = new ArrayList<>();
        dataList.add("First");
        dataList.add("Second");
        dataList.add("Third");
        dataList.add("Fourth");

        RecyclerView rvTest = findViewById(R.id.rv_test);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvTest.setLayoutManager(layoutManager);
        SwipeTestAdapter swipeTestAdapter = new SwipeTestAdapter(this, dataList);
        rvTest.setAdapter(swipeTestAdapter);

        touchListener = new RecyclerTouchListener(this, rvTest);
        touchListener
                .setClickable(new RecyclerTouchListener.OnRowClickListener() {
                    @Override
                    public void onRowClicked(int position) {
                    }

                    @Override
                    public void onIndependentViewClicked(int independentViewID, int position) {

                    }
                })
                .setSwipeOptionViews(R.id.delete_task)
                .setSwipeable(R.id.rowFG, R.id.rowBG, new RecyclerTouchListener.OnSwipeOptionsClickListener() {
                    @Override
                    public void onSwipeOptionClicked(int viewID, int position) {
                        switch (viewID) {
                            case R.id.delete_task:
                                Log.d(TAG, "onSwipeOptionClicked: delete_task");
                                swipeTestAdapter.removeItem(position);
                                break;

                        }
                    }
                });
        rvTest.addOnItemTouchListener(touchListener);
    }
}