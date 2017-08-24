package io.intelehealth.client.activities.additional_documents_activity;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.intelehealth.client.R;
import io.intelehealth.client.activities.camera_activity.CameraActivity;
import io.intelehealth.client.activities.identification_activity.IdentificationActivity;
import io.intelehealth.client.database.LocalRecordsDatabaseHelper;
import io.intelehealth.client.node.Node;

public class AdditionalDocumentsActivity extends AppCompatActivity {


    private String patientID;
    private String visitID;
    private List<DocumentObject> rowListItem;
    private AdditionalDocumentAdapter recyclerViewAdapter;

    private final String imgPrefix = "AD";

    final private String imageDir = "Additional Documents";
    private String baseDir;
    private String filePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_additional_documents);

        Toolbar topToolBar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(topToolBar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        baseDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath();

        Intent intent = this.getIntent(); // The intent was passed to the activity
        if (intent != null) {
            patientID = intent.getStringExtra("patientID");
            visitID = intent.getStringExtra("visitID");

            filePath = baseDir + File.separator + "Patient Images" + File.separator + patientID + File.separator +
                    visitID + File.separator + imageDir;

            File dir = new File(filePath);
            if (!dir.exists())
                dir.mkdirs();
            List<File> fileList = Arrays.asList(dir.listFiles());
            rowListItem = new ArrayList<>();

            for (File file : fileList)
                rowListItem.add(new DocumentObject(file.getName(), file.getAbsolutePath()));

            RecyclerView.LayoutManager linearLayoutManager = new LinearLayoutManager(this);

            RecyclerView recyclerView = (RecyclerView) findViewById(R.id.document_RecyclerView);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(linearLayoutManager);

            recyclerViewAdapter = new AdditionalDocumentAdapter(this, rowListItem, filePath);
            recyclerView.setAdapter(recyclerViewAdapter);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_additional_docs, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CameraActivity.TAKE_IMAGE) {
            if (resultCode == RESULT_OK) {
                String mCurrentPhotoPath = data.getStringExtra("RESULT");
                File photo = new File(mCurrentPhotoPath);
                if (photo.exists()) {
                    recyclerViewAdapter.add(new DocumentObject(photo.getName(), photo.getAbsolutePath()));
                }
                File base_dir = new File(filePath);
                File files[] = base_dir.listFiles();
                for (File file : files)
                    updateImageDatabase(file.getAbsolutePath());
            }
        }
    }

    private void updateImageDatabase(String imagePath) {
        LocalRecordsDatabaseHelper mDbHelper = new LocalRecordsDatabaseHelper(this);
        SQLiteDatabase localdb = mDbHelper.getWritableDatabase();
        localdb.execSQL("INSERT INTO image_records (patient_id,visit_id,image_path,image_type,delete_status) values("
                + "'" + patientID + "'" + ","
                + visitID + ","
                + "'" + imagePath + "','" + "AD" + "'," +
                0 +
                ")");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_docs:
                Intent cameraIntent = new Intent(this, CameraActivity.class);
                String imageName = patientID + "_" + visitID + "_" + imgPrefix;
                cameraIntent.putExtra(CameraActivity.SET_IMAGE_NAME, imageName);
                cameraIntent.putExtra(CameraActivity.SET_IMAGE_PATH, filePath);
                startActivityForResult(cameraIntent, CameraActivity.TAKE_IMAGE);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


}


