package org.intelehealth.abdm.features.databinding;
import org.intelehealth.abdm.features.R;
import org.intelehealth.abdm.features.BR;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;
@SuppressWarnings("unchecked")
public class DialogAbdmRegistrationConfirmationBindingImpl extends DialogAbdmRegistrationConfirmationBinding  {

    @Nullable
    private static final androidx.databinding.ViewDataBinding.IncludedLayouts sIncludes;
    @Nullable
    private static final android.util.SparseIntArray sViewsWithIds;
    static {
        sIncludes = null;
        sViewsWithIds = new android.util.SparseIntArray();
        sViewsWithIds.put(R.id.btnYes, 2);
        sViewsWithIds.put(R.id.btnNo, 3);
        sViewsWithIds.put(R.id.dialog_icon, 4);
        sViewsWithIds.put(R.id.tvTitle, 5);
        sViewsWithIds.put(R.id.dialog_subtitle, 6);
    }
    // views
    @NonNull
    private final androidx.cardview.widget.CardView mboundView0;
    @NonNull
    private final android.widget.LinearLayout mboundView1;
    // variables
    // values
    // listeners
    // Inverse Binding Event Handlers

    public DialogAbdmRegistrationConfirmationBindingImpl(@Nullable androidx.databinding.DataBindingComponent bindingComponent, @NonNull View root) {
        this(bindingComponent, root, mapBindings(bindingComponent, root, 7, sIncludes, sViewsWithIds));
    }
    private DialogAbdmRegistrationConfirmationBindingImpl(androidx.databinding.DataBindingComponent bindingComponent, View root, Object[] bindings) {
        super(bindingComponent, root, 0
            , (bindings[3] != null) ? org.intelehealth.abdm.features.databinding.BtnSolidActiveBinding.bind((android.view.View) bindings[3]) : null
            , (bindings[2] != null) ? org.intelehealth.abdm.features.databinding.WhiteButtonBinding.bind((android.view.View) bindings[2]) : null
            , (android.widget.ImageView) bindings[4]
            , (android.widget.TextView) bindings[6]
            , (android.widget.TextView) bindings[5]
            );
        this.mboundView0 = (androidx.cardview.widget.CardView) bindings[0];
        this.mboundView0.setTag(null);
        this.mboundView1 = (android.widget.LinearLayout) bindings[1];
        this.mboundView1.setTag(null);
        setRootTag(root);
        // listeners
        invalidateAll();
    }

    @Override
    public void invalidateAll() {
        synchronized(this) {
                mDirtyFlags = 0x1L;
        }
        requestRebind();
    }

    @Override
    public boolean hasPendingBindings() {
        synchronized(this) {
            if (mDirtyFlags != 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean setVariable(int variableId, @Nullable Object variable)  {
        boolean variableSet = true;
            return variableSet;
    }

    @Override
    protected boolean onFieldChange(int localFieldId, Object object, int fieldId) {
        switch (localFieldId) {
        }
        return false;
    }

    @Override
    protected void executeBindings() {
        long dirtyFlags = 0;
        synchronized(this) {
            dirtyFlags = mDirtyFlags;
            mDirtyFlags = 0;
        }
        // batch finished
    }
    // Listener Stub Implementations
    // callback impls
    // dirty flag
    private  long mDirtyFlags = 0xffffffffffffffffL;
    /* flag mapping
        flag 0 (0x1L): null
    flag mapping end*/
    //end
}