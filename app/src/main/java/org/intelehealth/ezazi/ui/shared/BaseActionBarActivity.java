package org.intelehealth.ezazi.ui.shared;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.intelehealth.ezazi.R;

import java.util.Objects;

/**
 * Created by Vaghela Mithun R. on 03-06-2023 - 19:29.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
public abstract class BaseActionBarActivity extends BaseActivity {

    protected void setupActionBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        if (getScreenTitle() != 0)
            getSupportActionBar().setTitle(getString(getScreenTitle()));
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackNavigate());
    }

    protected abstract @StringRes int getScreenTitle();

    protected void onBackNavigate(){
        onBackPressed();
    }
}
