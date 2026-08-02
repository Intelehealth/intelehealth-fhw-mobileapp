package org.intelehealth.abdm.presentation.abha_suggestions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.text.InputFilter
import android.view.View
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.intelehealth.abdm.R
import org.intelehealth.abdm.databinding.ActivityAbhaSuggestionsBinding
import org.intelehealth.abdm.presentation.common.UiState

@AndroidEntryPoint
class AbhaSuggestionsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAbhaSuggestionsBinding
    private val viewModel: AbhaSuggestionsViewModel by viewModels()

    private lateinit var txnId: String
    private var defaultAddress: String = ""
    private var isProgrammaticChange = false
    private var shownErrorRes: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAbhaSuggestionsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applyInsets()

        txnId = intent.getStringExtra(EXTRA_TXN_ID).orEmpty()
        defaultAddress = intent.getStringExtra(EXTRA_DEFAULT_ADDRESS).orEmpty()

        setupInput()
        setupActions()
        blockBackPress()
        observeState()
        observeEvents()

        viewModel.loadSuggestions(txnId)
    }

    private fun applyInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            v.setPadding(bars.left, bars.top, bars.right, maxOf(bars.bottom, ime.bottom))
            if (ime.bottom > 0) v.findFocus()?.let { focused ->
                focused.post { focused.requestRectangleOnScreen(Rect(0, 0, focused.width, focused.height), false) }
            }
            insets
        }
    }

    private fun setupInput() {
        binding.tvSuffix.text = viewModel.abhaAddressSuffix
        binding.etAbhaAddress.filters = arrayOf(
            InputFilter { source, _, _, _, _, _ ->
                if (source != null && source.contains("@")) "" else null
            },
        )
        binding.etAbhaAddress.doAfterTextChanged {
            if (isProgrammaticChange) {
                isProgrammaticChange = false
                return@doAfterTextChanged
            }
            if (binding.chipGroup.checkedChipId != View.NO_ID) {
                binding.chipGroup.clearCheck()
            }
            viewModel.onAddressChanged()
        }
        binding.chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            val id = checkedIds.firstOrNull() ?: return@setOnCheckedStateChangeListener
            val chip = group.findViewById<Chip>(id) ?: return@setOnCheckedStateChangeListener
            isProgrammaticChange = true
            binding.etAbhaAddress.setText(chip.text)
        }
    }

    private fun setupActions() {
        binding.btnSubmit.setOnClickListener {
            val checkedId = binding.chipGroup.checkedChipId
            val address = if (checkedId != View.NO_ID) {
                binding.chipGroup.findViewById<Chip>(checkedId).text.toString()
            } else {
                binding.etAbhaAddress.text?.toString().orEmpty().trim()
            }
            viewModel.onSubmitClicked(txnId, address, defaultAddress)
        }

        binding.ivAbhaSuggestion.setOnClickListener {
            AbhaAddressSuggestionDialogFragment()
                .show(supportFragmentManager, AbhaAddressSuggestionDialogFragment.TAG)
        }
    }

    private fun blockBackPress() {
        onBackPressedDispatcher.addCallback(this) {
            Snackbar.make(
                binding.main,
                getString(R.string.please_click_on_submit_button_to_proceed),
                Snackbar.LENGTH_SHORT,
            ).show()
        }
    }

    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { render(it) }
            }
        }
    }

    private fun observeEvents() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    when (event) {
                        is AbhaSuggestionsEvent.AddressRegistered -> finishWithAddress(event.preferredAbhaAddress)
                    }
                }
            }
        }
    }

    private fun render(state: AbhaSuggestionsUiState) {
        populateChips(state.addresses)

        binding.tvAddressError.apply {
            isVisible = state.addressError != null
            state.addressError?.let { res ->
                text = if (res == R.string.abha_address_suffix_not_allowed_validation) {
                    getString(res, viewModel.abhaAddressSuffix)
                } else {
                    getString(res)
                }
            }
        }

        val loading = state.operation is UiState.Loading
        binding.loadingOverlay.isVisible = loading
        if (loading) {
            binding.loadingMessage.text =
                getString(state.loadingMessageRes ?: R.string.abdm_loading_default)
        }
        binding.btnSubmit.isEnabled = !loading

        val operation = state.operation
        if (operation is UiState.Error) {
            if (operation.messageRes != shownErrorRes) {
                shownErrorRes = operation.messageRes
                Snackbar.make(binding.main, getString(operation.messageRes), Snackbar.LENGTH_LONG).show()
            }
        } else {
            shownErrorRes = null
        }
    }

    private fun populateChips(addresses: List<String>) {
        if (binding.chipGroup.childCount > 0 || addresses.isEmpty()) return
        addresses.forEach { address ->
            val chip = Chip(this).apply {
                id = ViewCompat.generateViewId()
                text = address
                isCheckable = true
                setChipBackgroundColorResource(R.color.white)
                setChipStrokeColorResource(R.color.brand_primary_dark)
                chipStrokeWidth = resources.getDimension(R.dimen.chip_stroke_width)
                setTextColor(getColor(R.color.brand_primary))
            }
            binding.chipGroup.addView(chip)
        }
    }

    private fun finishWithAddress(preferredAbhaAddress: String) {
        setResult(
            Activity.RESULT_OK,
            Intent().putExtra(EXTRA_CHOSEN_ADDRESS, preferredAbhaAddress),
        )
        finish()
    }

    companion object {
        private const val EXTRA_TXN_ID = "extra_txn_id"
        private const val EXTRA_DEFAULT_ADDRESS = "extra_default_address"
        const val EXTRA_CHOSEN_ADDRESS = "extra_chosen_address"

        fun newIntent(context: Context, txnId: String, defaultAddress: String): Intent =
            Intent(context, AbhaSuggestionsActivity::class.java)
                .putExtra(EXTRA_TXN_ID, txnId)
                .putExtra(EXTRA_DEFAULT_ADDRESS, defaultAddress)
    }
}
