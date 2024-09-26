package org.intelehealth.abdm.features.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

abstract class BaseDialogFragment<B : ViewBinding, V : BaseViewModel>: DialogFragment() {
    protected lateinit var viewModel: V

    protected lateinit var binding: B

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = initViewModel()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = initBinding()
        return binding.root
    }
    protected abstract fun setClickListener()

    abstract fun initViewModel(): V

    abstract fun initBinding(): B
}