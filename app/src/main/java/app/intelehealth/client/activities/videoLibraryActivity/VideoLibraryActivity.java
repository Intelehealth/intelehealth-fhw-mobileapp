package app.intelehealth.client.activities.videoLibraryActivity;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.File;

import app.intelehealth.client.R;
import app.intelehealth.client.utilities.Logger;


public class VideoLibraryActivity extends AppCompatActivity implements VideoLibraryFragment.OnFragmentInteractionListener {
    private static final String TAG = VideoLibraryActivity.class.getSimpleName();
    final String LOG_TAG = VideoLibraryActivity.class.getSimpleName();
    Toolbar mToolbar;


    File rootFile;
    FragmentManager fragmentManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_library);
        setTitle(R.string.title_activity_video_library);

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        fragmentManager = getSupportFragmentManager();
        Boolean isSDPresent = android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);

        if (isSDPresent) {
            if (isExternalStorageWritable()) {

                rootFile = getExtVideoStorageDir(this, "Intelehealth Videos");
                openFragment(rootFile.getAbsolutePath());
            }

        } else {
            Toast.makeText(this, getString(R.string.error_no_sd), Toast.LENGTH_LONG).show();
        }

    }
    private void openFragment(String filepath) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment videoFragment = new VideoLibraryFragment();
        fragmentTransaction.replace(R.id.video_fragment_FrameLayout, videoFragment);
        Bundle bundle = new Bundle();
        bundle.putString("FILEPATH", filepath);   //parameters are (key, value).
        videoFragment.setArguments(bundle);
        fragmentTransaction.commit();
    }

    public void openFragmentAddToBackstack(String filepath) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment videoFragment = new VideoLibraryFragment();
        fragmentTransaction.replace(R.id.video_fragment_FrameLayout, videoFragment);
        fragmentTransaction.addToBackStack(null);
        Bundle bundle = new Bundle();
        bundle.putString("FILEPATH", filepath);   //parameters are (key, value).
        videoFragment.setArguments(bundle);
        fragmentTransaction.commit();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    public File getExtVideoStorageDir(Context context, String folderName) {
        // Get the directory for the app's private pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MOVIES), folderName);
        Logger.logD(TAG, file.getAbsolutePath());
        if (!file.mkdirs()) {
            Log.e(LOG_TAG, "Directory not created");
        }
        return file;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();

    }

}
