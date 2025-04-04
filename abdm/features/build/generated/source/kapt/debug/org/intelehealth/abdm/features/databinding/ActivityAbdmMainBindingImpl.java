package org.intelehealth.abdm.features.databinding;
import org.intelehealth.abdm.features.R;
import org.intelehealth.abdm.features.BR;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;
@SuppressWarnings("unchecked")
public class ActivityAbdmMainBindingImpl extends ActivityAbdmMainBinding  {

    @Nullable
    private static final androidx.databinding.ViewDataBinding.IncludedLayouts sIncludes;
    @Nullable
    private static final android.util.SparseIntArray sViewsWithIds;
    static {
        sIncludes = null;
        sViewsWithIds = new android.util.SparseIntArray();
        sViewsWithIds.put(R.id.toolbar, 2);
        sViewsWithIds.put(R.id.btnDecline, 3);
        sViewsWithIds.put(R.id.btnAccept, 4);
        sViewsWithIds.put(R.id.tvDescription, 5);
    }
    // views
    @NonNull
    private final androidx.constraintlayout.widget.ConstraintLayout mboundView0;
    // variables
    // values
    // listeners
    // Inverse Binding Event Handlers

    public ActivityAbdmMainBindingImpl(@Nullable androidx.databinding.DataBindingComponent bindingComponent, @NonNull View root) {
        this(bindingComponent, root, mapBindings(bindingComponent, root, 6, sIncludes, sViewsWithIds));
    }
    private ActivityAbdmMainBindingImpl(androidx.databinding.DataBindingComponent bindingComponent, View root, Object[] bindings) {
        super(bindingComponent, root, 0
            , (bindings[4] != null) ? org.intelehealth.abdm.features.databinding.BtnSolidActiveBinding.bind((android.view.View) bindings[4]) : null
            , (bindings[3] != null) ? org.intelehealth.abdm.features.databinding.WhiteButtonBinding.bind((android.view.View) bindings[3]) : null
            , (android.widget.LinearLayout) bindings[1]
            , (bindings[2] != null) ? org.intelehealth.abdm.features.databinding.WhiteToolbarBinding.bind((android.view.View) bindings[2]) : null
            , (android.widget.TextView) bindings[5]
            );
        this.llButton.setTag(null);
        this.mboundView0 = (androidx.constraintlayout.widget.ConstraintLayout) bindings[0];
        this.mboundView0.setTag(null);
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