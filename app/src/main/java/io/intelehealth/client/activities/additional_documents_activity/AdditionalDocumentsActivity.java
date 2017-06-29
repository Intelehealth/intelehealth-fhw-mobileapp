package io.intelehealth.client.activities.additional_documents_activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.intelehealth.client.R;

public class AdditionalDocumentsActivity extends AppCompatActivity {

    private GridLayoutManager gridLayoutManager;

    private String patientID;
    private String visitID;
    private List<DocumentObject> rowListItem;

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

        String baseDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath();
        String filePath = baseDir + File.separator + "patient_images" + File.separator + patientID + File.separator +
                visitID + File.separator + "Additional Documents";

        File dir = new File(filePath);
        List <File> fileList =Arrays.asList(dir.listFiles());

        rowListItem = new ArrayList<>();

        for (File file : fileList){
            rowListItem.add(new DocumentObject(file.getName(),file.getAbsolutePath()));
        }

        gridLayoutManager = new GridLayoutManager(this, 2);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.document_RecyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(gridLayoutManager);

        AdditionalDocumentAdapter recyclerViewAdapter = new AdditionalDocumentAdapter(this, rowListItem);
        recyclerView.setAdapter(recyclerViewAdapter);
    }

}
