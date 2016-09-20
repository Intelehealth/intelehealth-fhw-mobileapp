package edu.jhu.bme.cbid.healthassistantsclient;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import edu.jhu.bme.cbid.healthassistantsclient.objects.Node;
import edu.jhu.bme.cbid.healthassistantsclient.objects.PhysicalExam;

public class PhysicalExamActivity extends AppCompatActivity {

    final static String LOG_TAG = "Physical Exam Activity";

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;


    String patientID = "1";
    String visitID;
    String state;
    String patientName;
    String intentTag;

    ArrayList<String> selectedExamsList = new ArrayList<>();

    String mFileName = "physExam.json";
//    String mFileName = "physExamDemo.json";

    String storageName = "physical";

    PhysicalExam physicalExamMap;

    String physicalString;

    @Override
    public void onBackPressed(){
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //For Testing
//        patientID = Long.valueOf("1");

        Intent intent = this.getIntent(); // The intent was passed to the activity
        if (intent != null) {
            patientID = intent.getStringExtra("patientID");
            visitID = intent.getStringExtra("visitID");
            state = intent.getStringExtra("state");
            patientName = intent.getStringExtra("name");
            intentTag = intent.getStringExtra("tag");
            selectedExamsList = intent.getStringArrayListExtra("exams");
//            Log.v(LOG_TAG, "Patient ID: " + patientID);
//            Log.v(LOG_TAG, "Visit ID: " + visitID);
//            Log.v(LOG_TAG, "Patient Name: " + patientName);
//            Log.v(LOG_TAG, "Intent Tag: " + intentTag);
        }

        if (selectedExamsList == null) {
            Log.d(LOG_TAG, "No additional exams were triggered");
        } else {
            Log.d(LOG_TAG, selectedExamsList.toString());
        }
        physicalExamMap = new PhysicalExam(HelperMethods.encodeJSON(this, mFileName), selectedExamsList);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_physical_exam);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }

        setTitle(patientName + ": " + getTitle());
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), physicalExamMap);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        if (mViewPager != null) {
            mViewPager.setAdapter(mSectionsPagerAdapter);
        }

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        if (tabLayout != null) {
            tabLayout.setupWithViewPager(mViewPager);
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(physicalExamMap.areRequiredAnswered()){

                    physicalString = physicalExamMap.generateFindings();

                    long obsId = insertDb(physicalString);
                    if (intentTag.equals("edit")){
                        Intent intent = new Intent(PhysicalExamActivity.this, VisitSummaryActivity.class);
                        intent.putExtra("patientID", patientID);
                        intent.putExtra("visitID", visitID);
                        intent.putExtra("state", state);
                        intent.putExtra("name", patientName);
                        intent.putExtra("tag", intentTag);
                        startActivity(intent);
                    } else {
                        Intent intent1 = new Intent(PhysicalExamActivity.this, VisitSummaryActivity.class);
                        intent1.putExtra("patientID", patientID);
                        intent1.putExtra("visitID", visitID);
                        intent1.putExtra("state", state);
                        intent1.putExtra("name", patientName);
                        intent1.putExtra("tag", intentTag);
                        startActivity(intent1);
                    }
                } else {
                    Node genExams = physicalExamMap.getOption(0);
                    for (int i = 0; i < genExams.getOptionsList().size(); i++) {
//                        Log.d(LOG_TAG, "current i value " + i);
                        if(!genExams.getOption(i).anySubSelected()){
//                            Log.d(LOG_TAG, genExams.getOption(i).getText());
                            mViewPager.setCurrentItem(i);
                            return;
                        }
                    }
                }
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_physical_exam, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber, PhysicalExam exams) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            args.putSerializable("maps", exams);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_physical_exam, container, false);

            ImageView imageView = (ImageView) rootView.findViewById(R.id.physical_exam_image_view);
            TextView textView = (TextView) rootView.findViewById(R.id.physical_exam_text_view);
            ExpandableListView expandableListView = (ExpandableListView) rootView.findViewById(R.id.physical_exam_expandable_list_view);
            //ListView listView = (ListView) rootView.findViewById(R.id.physical_exam_list_view);
            //VideoView videoView = (VideoView) rootView.findViewById(R.id.physical_exam_video_view);


            PhysicalExam exams = (PhysicalExam) getArguments().getSerializable("maps");
            int viewNumber = getArguments().getInt(ARG_SECTION_NUMBER);
            final Node viewNode = exams.getExamNode(viewNumber - 1);
            String nodeText = viewNode.getText();
            textView.setText(nodeText);

            Node displayNode = viewNode.getOption(0);

            if (displayNode.isAidAvailable()) {
                String type = displayNode.getJobAidType();
                //Log.d(displayNode.getText(), type);
                if(type.equals("video")){
                    imageView.setVisibility(View.GONE);
                } else if (type.equals("image")){
                    String drawableName = "physicalExamAssets/" + displayNode.getJobAidFile() + ".jpg";
                    try
                    {
                        // get input stream
                        InputStream ims = getContext().getAssets().open(drawableName);
                        // load image as Drawable
                        Drawable d = Drawable.createFromStream(ims, null);
                        // set image to ImageView
                        imageView.setImageDrawable(d);
                        imageView.setMinimumHeight(500);
                        imageView.setMinimumWidth(500);
                    }
                    catch(IOException ex)
                    {
                        ex.printStackTrace();
                    }
                } else {
                    imageView.setVisibility(View.GONE);
                }
            }


            final CustomExpandableListAdapter adapter = new CustomExpandableListAdapter(getContext(), viewNode, this.getClass().getSimpleName());
            expandableListView.setAdapter(adapter);
            expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
                @Override
                public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                    Node question = viewNode.getOption(groupPosition).getOption(childPosition);
                    //Log.d("Clicked", question.language());
                    question.toggleSelected();
                    if (viewNode.getOption(groupPosition).anySubSelected()) {
                        viewNode.getOption(groupPosition).setSelected();
                    } else {
                        viewNode.getOption(groupPosition).setUnselected();
                    }
                    adapter.notifyDataSetChanged();

                    if (question.getInputType() != null) {
                        Node.handleQuestion(question, (Activity) getContext(), adapter);
                    }

                    if (!question.isTerminal()) {
                        Node.subLevelQuestion(question, (Activity) getContext(), adapter);
                    }

                    return false;
                }
            });

            expandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
                @Override
                public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                    return true;
                }
            });

            expandableListView.expandGroup(0);


            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private PhysicalExam exams;

        public SectionsPagerAdapter(FragmentManager fm, PhysicalExam inputNode) {
            super(fm);
            this.exams = inputNode;
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1, exams);
        }

        @Override
        public int getCount() {
            return exams.getTotalNumberOfExams();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            //return exams.getTitle(position);
            return String.valueOf(position + 1);
        }
    }

    private long insertDb(String value) {
        LocalRecordsDatabaseHelper mDbHelper = new LocalRecordsDatabaseHelper(this);

        final int CREATOR_ID = 42; // TODO: Connect the proper CREATOR_ID

        final int CONCEPT_ID = 163189; // RHK ON EXAM

        ContentValues complaintEntries = new ContentValues();

        complaintEntries.put("patient_id", patientID);
        complaintEntries.put("visit_id", visitID);
        complaintEntries.put("creator", CREATOR_ID);
        complaintEntries.put("value", value);
        complaintEntries.put("concept_id", CONCEPT_ID);

        SQLiteDatabase localdb = mDbHelper.getWritableDatabase();
        return localdb.insert("obs", null, complaintEntries);
    }

}
