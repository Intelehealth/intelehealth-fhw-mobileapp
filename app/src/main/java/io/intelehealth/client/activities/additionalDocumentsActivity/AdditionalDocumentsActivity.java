package io.intelehealth.client.activities.additionalDocumentsActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.crashlytics.android.Crashlytics;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.intelehealth.client.R;
import io.intelehealth.client.activities.cameraActivity.CameraActivity;
import io.intelehealth.client.database.dao.ImagesDAO;
import io.intelehealth.client.models.DocumentObject;
import io.intelehealth.client.utilities.exception.DAOException;

public class AdditionalDocumentsActivity extends AppCompatActivity {


    private String patientUuid;
    private String visitUuid;
    private String encounterVitals;
    private String encounterAdultIntials;
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
        Toolbar topToolBar = findViewById(R.id.toolbar);
        setSupportActionBar(topToolBar);

        FloatingActionButton fab = findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        baseDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath();
        Intent intent = this.getIntent(); // The intent was passed to the activity
        if (intent != null) {
            patientUuid = intent.getStringExtra("patientUuid");
            visitUuid = intent.getStringExtra("visitUuid");
            encounterVitals = intent.getStringExtra("encounterUuidVitals");
            encounterAdultIntials = intent.getStringExtra("encounterUuidAdultIntial");

            filePath = baseDir + File.separator + "Patient Images" + File.separator + patientUuid + File.separator +
                    visitUuid + File.separator + imageDir;

            File dir = new File(filePath);
            if (!dir.exists())
                dir.mkdirs();
            File[] fileList = dir.listFiles();
            rowListItem = new ArrayList<>();

            for (File file : fileList)
                rowListItem.add(new DocumentObject(file.getName(), file.getAbsolutePath()));

            RecyclerView.LayoutManager linearLayoutManager = new LinearLayoutManager(this);

            RecyclerView recyclerView = findViewById(R.id.document_RecyclerView);
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
                    updateImageDatabase(photo.getAbsolutePath());
                }
            }
        }
    }
    // File base_dir = new File(filePath);
    //File files[] = base_dir.listFiles();
    //for (File file : files)

    // }


    private void updateImageDatabase(String imagePath) {
        ImagesDAO imagesDAO = new ImagesDAO();
        try {
            imagesDAO.insertObsImageDatabase(patientUuid, visitUuid, encounterAdultIntials, imagePath, imgPrefix);
        } catch (DAOException e) {
            Crashlytics.getInstance().core.logException(e);
        }
//
//        SQLiteDatabase localdb = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
//        ContentValues contentValues=new ContentValues();
//        try {
//            contentValues.put("uuid", UUID.randomUUID().toString());
//            contentValues.put("patinetuuid", patientUuid);
//            contentValues.put("visituuid", visitUuid);
//            contentValues.put("image_path", imagePath);
//            contentValues.put("image_type", "AD");
//            localdb.insertWithOnConflict("tbl_image_records", null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
////        localdb.execSQL("INSERT INTO image_records (uuid,patientUuid,visituuid,image_path,image_type) values("
////                + "'" + UUID.randomUUID().toString() + "'" + ","
////                + "'" + patientUuid + "'" + ","
////                + visitUuid + ","
////                + "'" + imagePath + "','"
////                + "AD" +
////                ")");
//        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_docs:
                Intent cameraIntent = new Intent(this, CameraActivity.class);
                String imageName = UUID.randomUUID().toString() + "_" + imgPrefix;
                cameraIntent.putExtra(CameraActivity.SET_IMAGE_NAME, imageName);
                cameraIntent.putExtra(CameraActivity.SET_IMAGE_PATH, filePath);
                startActivityForResult(cameraIntent, CameraActivity.TAKE_IMAGE);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


}
