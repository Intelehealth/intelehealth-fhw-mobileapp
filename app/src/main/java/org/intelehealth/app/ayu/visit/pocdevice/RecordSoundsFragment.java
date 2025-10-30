package org.intelehealth.app.ayu.visit.pocdevice;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import org.intelehealth.app.R;
import org.intelehealth.app.databinding.FragmentConnectPocdeviceBinding;
import org.intelehealth.app.databinding.FragmentRecordSoundsBinding;

public class RecordSoundsFragment extends Fragment{

    private FragmentRecordSoundsBinding mBinding;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_record_sounds, container, false);
        return mBinding.getRoot();
    }
}
