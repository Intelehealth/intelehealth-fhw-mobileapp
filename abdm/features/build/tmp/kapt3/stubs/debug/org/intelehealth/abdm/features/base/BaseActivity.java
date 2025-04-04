package org.intelehealth.abdm.features.base;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewbinding.ViewBinding;
import dagger.hilt.android.AndroidEntryPoint;
import dagger.hilt.android.HiltAndroidApp;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0010\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\b&\u0018\u0000*\b\b\u0000\u0010\u0001*\u00020\u0002*\b\b\u0001\u0010\u0003*\u00020\u00042\u00020\u0005B\u0005\u00a2\u0006\u0002\u0010\u0006J\r\u0010\u0013\u001a\u00028\u0000H&\u00a2\u0006\u0002\u0010\tJ\r\u0010\u0014\u001a\u00028\u0001H&\u00a2\u0006\u0002\u0010\u000fJ\u0012\u0010\u0015\u001a\u00020\u00162\b\u0010\u0017\u001a\u0004\u0018\u00010\u0018H\u0014J\b\u0010\u0019\u001a\u00020\u0016H$R\u001c\u0010\u0007\u001a\u00028\u0000X\u0084.\u00a2\u0006\u0010\n\u0002\u0010\f\u001a\u0004\b\b\u0010\t\"\u0004\b\n\u0010\u000bR\u001c\u0010\r\u001a\u00028\u0001X\u0084.\u00a2\u0006\u0010\n\u0002\u0010\u0012\u001a\u0004\b\u000e\u0010\u000f\"\u0004\b\u0010\u0010\u0011\u00a8\u0006\u001a"}, d2 = {"Lorg/intelehealth/abdm/features/base/BaseActivity;", "B", "Landroidx/viewbinding/ViewBinding;", "V", "Lorg/intelehealth/abdm/features/base/BaseViewModel;", "Landroidx/appcompat/app/AppCompatActivity;", "()V", "binding", "getBinding", "()Landroidx/viewbinding/ViewBinding;", "setBinding", "(Landroidx/viewbinding/ViewBinding;)V", "Landroidx/viewbinding/ViewBinding;", "viewModel", "getViewModel", "()Lorg/intelehealth/abdm/features/base/BaseViewModel;", "setViewModel", "(Lorg/intelehealth/abdm/features/base/BaseViewModel;)V", "Lorg/intelehealth/abdm/features/base/BaseViewModel;", "initBinding", "initViewModel", "onCreate", "", "savedInstanceState", "Landroid/os/Bundle;", "setClickListener", "features_debug"})
public abstract class BaseActivity<B extends androidx.viewbinding.ViewBinding, V extends org.intelehealth.abdm.features.base.BaseViewModel> extends androidx.appcompat.app.AppCompatActivity {
    protected V viewModel;
    protected B binding;
    
    public BaseActivity() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    protected final V getViewModel() {
        return null;
    }
    
    protected final void setViewModel(@org.jetbrains.annotations.NotNull
    V p0) {
    }
    
    @org.jetbrains.annotations.NotNull
    protected final B getBinding() {
        return null;
    }
    
    protected final void setBinding(@org.jetbrains.annotations.NotNull
    B p0) {
    }
    
    @java.lang.Override
    protected void onCreate(@org.jetbrains.annotations.Nullable
    android.os.Bundle savedInstanceState) {
    }
    
    protected abstract void setClickListener();
    
    @org.jetbrains.annotations.NotNull
    public abstract V initViewModel();
    
    @org.jetbrains.annotations.NotNull
    public abstract B initBinding();
}