package org.intelehealth.unicef.utilities;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.intelehealth.unicef.R;

public class CustomEditText extends RelativeLayout {
    TextView hint_title;
    EditText edittext_title;

    public CustomEditText(Context context) {
        super(context);
        init();
    }

    public CustomEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void init() {
        inflate(getContext(), R.layout.custom_edittext_addpatient, this);
        hint_title = findViewById(R.id.hint_title);
        edittext_title = findViewById(R.id.edittext_title);
    }

    public void setHint_title(TextView hint_title, String text) {
        hint_title.setText(text);
    }

    public void setEdittext_title(TextView hint_title, String text) {
        hint_title.setText(text);
    }



}
