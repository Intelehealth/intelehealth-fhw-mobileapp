package org.intelehealth.abdm.features.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding

abstract class BaseActivity<B : ViewBinding, V : BaseViewModel> : AppCompatActivity() {
    protected lateinit var viewModel: V
    protected lateinit var binding: B
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = initBinding()
        setContentView(binding.root)
        viewModel = initViewModel()
    }

    protected abstract fun setClickListener()

    abstract fun initViewModel(): V

    abstract fun initBinding(): B
}
