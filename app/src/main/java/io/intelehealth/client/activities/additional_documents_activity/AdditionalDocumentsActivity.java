package io.intelehealth.client.activities.additional_documents_activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
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

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.intelehealth.client.R;
import io.intelehealth.client.activities.camera_activity.CameraActivity;
import io.intelehealth.client.activities.identification_activity.IdentificationActivity;
import io.intelehealth.client.node.Node;

public class AdditionalDocumentsActivity extends AppCompatActivity{


    private String patientID;
    private String visitID;
    private List<DocumentObject> rowListItem;
    private AdditionalDocumentAdapter recyclerViewAdapter;

    private final String imgPrefix = "AD";

    final private String imageDir = "Additional Documents";
    final private String baseDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath();
    final private String filePath = baseDir + File.separator + "Patient Images" + File.separator + patientID + File.separator +
            visitID + File.separator + imageDir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_additional_documents);

        Toolbar topToolBar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(topToolBar);

        Intent intent = this.getIntent(); // The intent was passed to the activity
        if (intent != null) {
            patientID = intent.getStringExtra("patientID");
            visitID = intent.getStringExtra("visitID");
        }

        File dir = new File(filePath);
        if(!dir.exists()) dir.mkdirs();
        List <File> fileList = Arrays.asList(dir.listFiles());

        rowListItem = new ArrayList<>();

        for (File file : fileList){
            rowListItem.add(new DocumentObject(file.getName(),file.getAbsolutePath()));
        }

        RecyclerView.LayoutManager linearLayoutManager = new LinearLayoutManager(this);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.document_RecyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        recyclerViewAdapter = new AdditionalDocumentAdapter(this, rowListItem);
        recyclerView.setAdapter(recyclerViewAdapter);
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

        if (requestCode == Node.TAKE_IMAGE_FOR_NODE) {
            if (resultCode == RESULT_OK) {
                String mCurrentPhotoPath = data.getStringExtra("RESULT");
                File photo = new File(mCurrentPhotoPath);
                if(photo.exists()){
                    recyclerViewAdapter.addDocumentToList(new DocumentObject(photo.getName(),photo.getAbsolutePath()));
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_docs:
                Intent cameraIntent = new Intent(this, CameraActivity.class);
                cameraIntent.putExtra(CameraActivity.SET_IMAGE_NAME, patientID+"_"+visitID+"_"+imgPrefix);
                cameraIntent.putExtra(CameraActivity.SET_IMAGE_PATH, filePath);
                startActivityForResult(cameraIntent, CameraActivity.TAKE_IMAGE);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


}


