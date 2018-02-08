package io.intelehealth.client.activities.physical_exam_activity;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import io.intelehealth.client.R;
import io.intelehealth.client.activities.custom_expandable_list_adapter.CustomExpandableListAdapter;
import io.intelehealth.client.activities.visit_summary_activity.VisitSummaryActivity;
import io.intelehealth.client.activities.vitals_activity.VitalsActivity;
import io.intelehealth.client.database.LocalRecordsDatabaseHelper;
import io.intelehealth.client.node.Node;
import io.intelehealth.client.node.PhysicalExam;
import io.intelehealth.client.utilities.ConceptId;
import io.intelehealth.client.utilities.HelperMethods;

/**
 * Contains a subclass of {@link Node} (that is {@link PhysicalExam}).
 * It gives a series of tabbed questions which are to be responded by checking the patient physically.
 */
public class PhysicalExamActivity extends AppCompatActivity {

    final static String TAG = PhysicalExamActivity.class.getSimpleName();

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


    static Integer patientID;
    static String visitID;
    String state;
    String patientName;
    String intentTag;

    ArrayList<String> selectedExamsList;

    LocalRecordsDatabaseHelper mDbHelper;
    SQLiteDatabase localdb;

    private static String image_Prefix = "PE";
    private static String imageDir = "Physical Exam";

    static String imageName = patientID + "_" + visitID + "_" + image_Prefix;
    static String baseDir;
    static File filePath;



    String mFileName = "physExam.json";


    PhysicalExam physicalExamMap;

    String physicalString;
    Boolean complaintConfirmed = false;

    @Override
    public void onBackPressed() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        baseDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath();

        mDbHelper = new LocalRecordsDatabaseHelper(this);
        localdb = mDbHelper.getWritableDatabase();

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(R.string.wash_hands);
        alertDialogBuilder.setNeutralButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

        //For Testing
//        patientID = Long.valueOf("1");
        selectedExamsList = new ArrayList<>();
        Intent intent = this.getIntent(); // The intent was passed to the activity
        if (intent != null) {
            patientID = intent.getIntExtra("patientID",-1);
            visitID = intent.getStringExtra("visitID");
            state = intent.getStringExtra("state");
            patientName = intent.getStringExtra("name");
            intentTag = intent.getStringExtra("tag");
            //  selectedExamsList = intent.getStringArrayListExtra("exams");
//            Log.v(TAG, "Patient ID: " + patientID);
//            Log.v(TAG, "Visit ID: " + visitID);
//            Log.v(TAG, "Patient Name: " + patientName);
//            Log.v(TAG, "Intent Tag: " + intentTag);
            SharedPreferences mSharedPreference = this.getSharedPreferences(
                    "visit_summary", Context.MODE_PRIVATE);
            Set<String> selectedExams = mSharedPreference.getStringSet("exam_" + patientID, null);
            selectedExamsList.clear();
            if(selectedExams!=null) selectedExamsList.addAll(selectedExams);
            filePath = new File(baseDir + File.separator + "Patient Images" + File.separator +
                    patientID + File.separator + visitID + File.separator + imageDir);
        }

        imageName = patientID + "_" + visitID + "_" + image_Prefix;

        if ((selectedExamsList == null) || selectedExamsList.isEmpty()) {
            Log.d(TAG, "No additional exams were triggered");
        } else {
            Set<String> selectedExamsWithoutDuplicates = new LinkedHashSet<>(selectedExamsList);
            Log.d(TAG, selectedExamsList.toString());
            selectedExamsList.clear();
            selectedExamsList.addAll(selectedExamsWithoutDuplicates);
            Log.d(TAG, selectedExamsList.toString());
            for (String string : selectedExamsList) Log.d(TAG, string);

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            boolean hasLicense = false;
            if (sharedPreferences.contains("licensekey"))
                hasLicense = true;

            if (hasLicense) {
                try {
                    JSONObject currentFile = null;
                    currentFile = new JSONObject(HelperMethods.readFileRoot(mFileName, this));
                    physicalExamMap = new PhysicalExam(currentFile, selectedExamsList);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                physicalExamMap = new PhysicalExam(HelperMethods.encodeJSON(this, mFileName), selectedExamsList);
            }
            //physicalExamMap = new PhysicalExam(HelperMethods.encodeJSON(this, mFileName), selectedExamsList);
        }


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_physical_exam);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
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
        tabLayout.setSelectedTabIndicatorHeight(15);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            tabLayout.setSelectedTabIndicatorColor(getColor(R.color.amber));
            tabLayout.setTabTextColors(getColor(R.color.white),getColor(R.color.amber));
        }else{
            tabLayout.setSelectedTabIndicatorColor(getResources().getColor(R.color.amber));
            tabLayout.setTabTextColors(getResources().getColor(R.color.white),getResources().getColor(R.color.amber));
        }
        if (tabLayout != null) {
            tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
            tabLayout.setupWithViewPager(mViewPager);
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                complaintConfirmed = physicalExamMap.areRequiredAnswered();

                if (complaintConfirmed) {

                    physicalString = physicalExamMap.generateFindings();

                    List<String> imagePathList = physicalExamMap.getImagePathList();

                    if (imagePathList != null) {
                        for (String imagePath : imagePathList) {
                            updateImageDatabase(imagePath);
                        }
                    }

                    if (intentTag != null && intentTag.equals("edit")) {
                        updateDatabase(physicalString);
                        Intent intent = new Intent(PhysicalExamActivity.this, VisitSummaryActivity.class);
                        intent.putExtra("patientID", patientID);
                        intent.putExtra("visitID", visitID);
                        intent.putExtra("state", state);
                        intent.putExtra("name", patientName);
                        intent.putExtra("tag", intentTag);
                        for (String exams : selectedExamsList) {
                            Log.i(TAG, "onClick:++ " + exams);
                        }
                        // intent.putStringArrayListExtra("exams", selectedExamsList);
                        startActivity(intent);
                    } else {
                        long obsId = insertDb(physicalString);
                        Intent intent1 = new Intent(PhysicalExamActivity.this, VitalsActivity.class); // earlier visitsummary
                        intent1.putExtra("patientID", patientID);
                        intent1.putExtra("visitID", visitID);
                        intent1.putExtra("state", state);
                        intent1.putExtra("name", patientName);
                        intent1.putExtra("tag", intentTag);
                        // intent1.putStringArrayListExtra("exams", selectedExamsList);
                        startActivity(intent1);
                    }

                } else {
                    questionsMissing();
                }
//                    Node genExams = physicalExamMap.getOption(0);
//                    for (int i = 0; i < genExams.getOptionsList().size(); i++) {
////                        Log.d(TAG, "current i value " + i);
//                        if(!genExams.getOption(i).anySubSelected()){
////                            Log.d(TAG, genExams.getOption(i).getText());
//                            mViewPager.setCurrentItem(i);
//                            return;
//                        }
//                    }

            }
        });

    }


    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */

        public static PhysicalExam exam_list;

        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        CustomExpandableListAdapter adapter;

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber, PhysicalExam exams, Integer patientID, String visitID) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            args.putInt("PATIENT_ID", patientID);
            args.putString("VISIT_ID", visitID);
            exam_list = exams;
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_physical_exam, container, false);

            final ImageView imageView = (ImageView) rootView.findViewById(R.id.physical_exam_image_view);
            TextView textView = (TextView) rootView.findViewById(R.id.physical_exam_text_view);
            ExpandableListView expandableListView = (ExpandableListView) rootView.findViewById(R.id.physical_exam_expandable_list_view);
            //ListView listView = (ListView) rootView.findViewById(R.id.physical_exam_list_view);
            //VideoView videoView = (VideoView) rootView.findViewById(R.id.physical_exam_video_view);

            int viewNumber = getArguments().getInt(ARG_SECTION_NUMBER);
            final Integer patientID = getArguments().getInt("PATIENT_ID");
            final String visitID = getArguments().getString("VISIT_ID");
            final Node viewNode = exam_list.getExamNode(viewNumber - 1);
            final String parent_name = exam_list.getExamParentNodeName(viewNumber - 1);
            String nodeText = parent_name + " : " + viewNode.findDisplay();

            textView.setText(nodeText);

            Node displayNode = viewNode.getOption(0);

            if (displayNode.isAidAvailable()) {
                String type = displayNode.getJobAidType();
                //Log.d(displayNode.getText(), type);
                if (type.equals("video")) {
                    imageView.setVisibility(View.GONE);
                } else if (type.equals("image")) {
                    String drawableName = "physicalExamAssets/" + displayNode.getJobAidFile() + ".jpg";
                    try {
                        // get input stream
                        InputStream ims = getContext().getAssets().open(drawableName);
                        // load image as Drawable
                        Drawable d = Drawable.createFromStream(ims, null);
                        // set image to ImageView
                        imageView.setImageDrawable(d);
                        imageView.setMinimumHeight(500);
                        imageView.setMinimumWidth(500);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        imageView.setVisibility(View.GONE);
                    }
                } else {
                    imageView.setVisibility(View.GONE);
                }
            } else {
                imageView.setVisibility(View.GONE);
            }


            adapter = new CustomExpandableListAdapter(getContext(), viewNode, this.getClass().getSimpleName());
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



                    if (question.getInputType() != null && question.isSelected()) {

                        if (question.getInputType().equals("camera")) {
                            if (!filePath.exists()) {
                                boolean res = filePath.mkdirs();
                                Log.i("RES>", "" + filePath + " -> " + res);
                            }
                            Node.handleQuestion(question, getActivity(), adapter, filePath.toString(), imageName);
                        } else {
                            Node.handleQuestion(question, (Activity) getContext(), adapter, null, null);
                        }


                    }

                    if (!question.isTerminal() && question.isSelected()) {
                        Node.subLevelQuestion(question, (Activity) getContext(), adapter, filePath.toString(), imageName);
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
            return PlaceholderFragment.newInstance(position + 1, exams, patientID, visitID);
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
        Log.i(TAG, "insertDb: ");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        final String CREATOR_ID = prefs.getString("creatorid", null);

        final int CONCEPT_ID = ConceptId.PHYSICAL_EXAMINATION; // RHK ON EXAM

        ContentValues complaintEntries = new ContentValues();

        complaintEntries.put("patient_id", patientID);
        complaintEntries.put("visit_id", visitID);
        complaintEntries.put("creator", CREATOR_ID);
        complaintEntries.put("value", value);
        complaintEntries.put("concept_id", CONCEPT_ID);

        return localdb.insert("obs", null, complaintEntries);
    }

    public void questionsMissing() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(R.string.question_answer_all_phy_exam);
        alertDialogBuilder.setNeutralButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void updateDatabase(String string) {

        int conceptID = ConceptId.PHYSICAL_EXAMINATION;
        ContentValues contentValues = new ContentValues();
        contentValues.put("value", string);

        String selection = "patient_id = ? AND visit_id = ? AND concept_id = ?";
        String[] args = {String.valueOf(patientID), visitID, String.valueOf(conceptID)};

        int i = localdb.update(
                "obs",
                contentValues,
                selection,
                args
        );
        Log.i(TAG, "updateDatabase: " + i);
        if(i==0){
            insertDb(string);
        }

    }

    private void updateImageDatabase(String imagePath) {
       localdb.execSQL("INSERT INTO image_records (patient_id,visit_id,image_path,image_type,delete_status) values("
                + "'" + patientID + "'" + ","
                + visitID + ","
                + "'" + imagePath + "','" + image_Prefix + "'," +
                0 +
                ")");
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == Node.TAKE_IMAGE_FOR_NODE) {
                if (resultCode == RESULT_OK) {
                    String mCurrentPhotoPath = data.getStringExtra("RESULT");
                    physicalExamMap.setImagePath(mCurrentPhotoPath);
                    Log.i(TAG, mCurrentPhotoPath);
                    physicalExamMap.displayImage(this,filePath.getAbsolutePath(),imageName);

                }

            }
    }



    @Override
    protected void onStop() {
        super.onStop();
    }
}
