package org.intelehealth.abdm.features;

import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.View;
import androidx.databinding.DataBinderMapper;
import androidx.databinding.DataBindingComponent;
import androidx.databinding.ViewDataBinding;
import java.lang.IllegalArgumentException;
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.RuntimeException;
import java.lang.String;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.intelehealth.abdm.features.databinding.ActivityAbdmMainBindingImpl;
import org.intelehealth.abdm.features.databinding.DialogAbdmRegistrationConfirmationBindingImpl;

public class DataBinderMapperImpl extends DataBinderMapper {
  private static final int LAYOUT_ACTIVITYABDMMAIN = 1;

  private static final int LAYOUT_DIALOGABDMREGISTRATIONCONFIRMATION = 2;

  private static final SparseIntArray INTERNAL_LAYOUT_ID_LOOKUP = new SparseIntArray(2);

  static {
    INTERNAL_LAYOUT_ID_LOOKUP.put(org.intelehealth.abdm.features.R.layout.activity_abdm_main, LAYOUT_ACTIVITYABDMMAIN);
    INTERNAL_LAYOUT_ID_LOOKUP.put(org.intelehealth.abdm.features.R.layout.dialog_abdm_registration_confirmation, LAYOUT_DIALOGABDMREGISTRATIONCONFIRMATION);
  }

  @Override
  public ViewDataBinding getDataBinder(DataBindingComponent component, View view, int layoutId) {
    int localizedLayoutId = INTERNAL_LAYOUT_ID_LOOKUP.get(layoutId);
    if(localizedLayoutId > 0) {
      final Object tag = view.getTag();
      if(tag == null) {
        throw new RuntimeException("view must have a tag");
      }
      switch(localizedLayoutId) {
        case  LAYOUT_ACTIVITYABDMMAIN: {
          if ("layout/activity_abdm_main_0".equals(tag)) {
            return new ActivityAbdmMainBindingImpl(component, view);
          }
          throw new IllegalArgumentException("The tag for activity_abdm_main is invalid. Received: " + tag);
        }
        case  LAYOUT_DIALOGABDMREGISTRATIONCONFIRMATION: {
          if ("layout/dialog_abdm_registration_confirmation_0".equals(tag)) {
            return new DialogAbdmRegistrationConfirmationBindingImpl(component, view);
          }
          throw new IllegalArgumentException("The tag for dialog_abdm_registration_confirmation is invalid. Received: " + tag);
        }
      }
    }
    return null;
  }

  @Override
  public ViewDataBinding getDataBinder(DataBindingComponent component, View[] views, int layoutId) {
    if(views == null || views.length == 0) {
      return null;
    }
    int localizedLayoutId = INTERNAL_LAYOUT_ID_LOOKUP.get(layoutId);
    if(localizedLayoutId > 0) {
      final Object tag = views[0].getTag();
      if(tag == null) {
        throw new RuntimeException("view must have a tag");
      }
      switch(localizedLayoutId) {
      }
    }
    return null;
  }

  @Override
  public int getLayoutId(String tag) {
    if (tag == null) {
      return 0;
    }
    Integer tmpVal = InnerLayoutIdLookup.sKeys.get(tag);
    return tmpVal == null ? 0 : tmpVal;
  }

  @Override
  public String convertBrIdToString(int localId) {
    String tmpVal = InnerBrLookup.sKeys.get(localId);
    return tmpVal;
  }

  @Override
  public List<DataBinderMapper> collectDependencies() {
    ArrayList<DataBinderMapper> result = new ArrayList<DataBinderMapper>(1);
    result.add(new androidx.databinding.library.baseAdapters.DataBinderMapperImpl());
    return result;
  }

  private static class InnerBrLookup {
    static final SparseArray<String> sKeys = new SparseArray<String>(1);

    static {
      sKeys.put(0, "_all");
    }
  }

  private static class InnerLayoutIdLookup {
    static final HashMap<String, Integer> sKeys = new HashMap<String, Integer>(2);

    static {
      sKeys.put("layout/activity_abdm_main_0", org.intelehealth.abdm.features.R.layout.activity_abdm_main);
      sKeys.put("layout/dialog_abdm_registration_confirmation_0", org.intelehealth.abdm.features.R.layout.dialog_abdm_registration_confirmation);
    }
  }
}
