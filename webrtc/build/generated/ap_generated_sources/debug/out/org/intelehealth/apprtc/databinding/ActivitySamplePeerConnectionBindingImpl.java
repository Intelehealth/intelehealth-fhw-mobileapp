package org.intelehealth.apprtc.databinding;
import org.intelehealth.apprtc.R;
import org.intelehealth.apprtc.BR;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;
@SuppressWarnings("unchecked")
public class ActivitySamplePeerConnectionBindingImpl extends ActivitySamplePeerConnectionBinding  {

    @Nullable
    private static final androidx.databinding.ViewDataBinding.IncludedLayouts sIncludes;
    @Nullable
    private static final android.util.SparseIntArray sViewsWithIds;
    static {
        sIncludes = null;
        sViewsWithIds = new android.util.SparseIntArray();
        sViewsWithIds.put(R.id.incoming_surface_view_frame, 1);
        sViewsWithIds.put(R.id.incoming_surface_view, 2);
        sViewsWithIds.put(R.id.self_surface_view_frame, 3);
        sViewsWithIds.put(R.id.self_surface_view, 4);
        sViewsWithIds.put(R.id.call_end_imv, 5);
        sViewsWithIds.put(R.id.audio_imv, 6);
        sViewsWithIds.put(R.id.video_imv, 7);
        sViewsWithIds.put(R.id.flip_imv, 8);
        sViewsWithIds.put(R.id.status_tv, 9);
        sViewsWithIds.put(R.id.calling_layout, 10);
        sViewsWithIds.put(R.id.caller_name_tv, 11);
        sViewsWithIds.put(R.id.calling_hints_tv, 12);
        sViewsWithIds.put(R.id.ripple_background_content, 13);
        sViewsWithIds.put(R.id.centerImage, 14);
        sViewsWithIds.put(R.id.in_call_reject_imv, 15);
        sViewsWithIds.put(R.id.in_call_accept_imv, 16);
    }
    // views
    @NonNull
    private final android.widget.RelativeLayout mboundView0;
    // variables
    // values
    // listeners
    // Inverse Binding Event Handlers

    public ActivitySamplePeerConnectionBindingImpl(@Nullable androidx.databinding.DataBindingComponent bindingComponent, @NonNull View root) {
        this(bindingComponent, root, mapBindings(bindingComponent, root, 17, sIncludes, sViewsWithIds));
    }
    private ActivitySamplePeerConnectionBindingImpl(androidx.databinding.DataBindingComponent bindingComponent, View root, Object[] bindings) {
        super(bindingComponent, root, 0
            , (android.widget.ImageView) bindings[6]
            , (android.widget.ImageView) bindings[5]
            , (android.widget.TextView) bindings[11]
            , (android.widget.TextView) bindings[12]
            , (android.widget.RelativeLayout) bindings[10]
            , (android.widget.ImageView) bindings[14]
            , (android.widget.ImageView) bindings[8]
            , (android.widget.ImageView) bindings[16]
            , (android.widget.ImageView) bindings[15]
            , (org.webrtc.SurfaceViewRenderer) bindings[2]
            , (android.widget.FrameLayout) bindings[1]
            , (com.skyfishjy.library.RippleBackground) bindings[13]
            , (org.webrtc.SurfaceViewRenderer) bindings[4]
            , (android.widget.FrameLayout) bindings[3]
            , (android.widget.TextView) bindings[9]
            , (android.widget.ImageView) bindings[7]
            );
        this.mboundView0 = (android.widget.RelativeLayout) bindings[0];
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