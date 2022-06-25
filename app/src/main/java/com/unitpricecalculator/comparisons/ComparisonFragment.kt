package com.unitpricecalculator.comparisons

import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.squareup.otto.Bus
import com.unitpricecalculator.BaseFragment
import com.unitpricecalculator.R
import com.unitpricecalculator.currency.Currencies
import com.unitpricecalculator.databinding.ComparisonFragmentBinding
import com.unitpricecalculator.databinding.ViewEnterNameBinding
import com.unitpricecalculator.saved.SavedComparisonManager
import com.unitpricecalculator.unit.Systems
import com.unitpricecalculator.unit.Units
import com.unitpricecalculator.util.ParcelableBundleKey
import com.unitpricecalculator.util.SavesState
import com.unitpricecalculator.util.afterTextChanged
import com.unitpricecalculator.util.get
import com.unitpricecalculator.util.logger.Logger
import com.unitpricecalculator.util.prefs.Prefs
import com.unitpricecalculator.util.set
import com.unitpricecalculator.util.sometimes.MutableSometimes
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ComparisonFragment : BaseFragment(), SavesState<ComparisonFragmentState?>, ComparisonView.ComparisonViewListener {

    @Inject
    internal lateinit var prefs: Prefs

    @Inject
    internal lateinit var units: Units

    @Inject
    internal lateinit var systems: Systems

    @Inject
    internal lateinit var unitTypeArrayAdapterFactory: UnitTypeArrayAdapterFactory

    @Inject
    internal lateinit var currencies: Currencies

    @Inject
    internal lateinit var unitArrayAdapterFactory: UnitArrayAdapterFactory

    @Inject
    internal lateinit var bus: Bus

    @Inject
    internal lateinit var activity: AppCompatActivity

    @Inject
    internal lateinit var savedComparisonManager: SavedComparisonManager

    @Inject
    lateinit var comparisonFactory: ComparisonFactory

    private var comparisonState: ComparisonFragmentState? = null
    private var viewState = MutableSometimes.create<ViewState>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Logger.d("ComparisonFragment onCreateView, comparison state was $comparisonState")
        var comparisonState = comparisonState
        if (comparisonState == null) {
            if (savedInstanceState != null) {
                comparisonState = savedInstanceState[ComparisonKey]
            }
            val arguments = arguments
            if (comparisonState == null && arguments != null) {
                comparisonState = arguments[ComparisonKey]
            }

            if (comparisonState == null) {
                comparisonState = ComparisonFragmentState(comparisonFactory.createComparison())
            }
            this.comparisonState = comparisonState
        }

        return ComparisonFragmentBinding.inflate(inflater, container, false).root.also {
            val comparisonView =
                createAndReplaceComparisonView(it, comparisonState)
            viewState.set(
                ViewState(
                    viewContainer = it,
                    comparisonView = comparisonView
                )
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        updateComparisonStateFromView()
        Logger.d("ComparisonFragment onDestroyView, comparison state is $comparisonState")
        viewState.set(null)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Logger.d("ComparisonFragment onSaveInstanceState")
        updateComparisonStateFromView()
        outState[ComparisonKey] = comparisonState
    }

    private fun updateComparisonStateFromView() {
        val currentState = viewState.orNull()?.comparisonView?.getCurrentState() ?: return
        val previousComparisonState = comparisonState ?: return
        comparisonState = previousComparisonState.copy(currentComparison = currentState)
        Logger.d("Updated comparison state to $comparisonState")
    }

    override fun saveState(context: Context): ComparisonFragmentState {
        updateComparisonStateFromView()
        return comparisonState ?: ComparisonFragmentState(comparisonFactory.createComparison())
    }

    override fun restoreState(state: ComparisonFragmentState) {
        Logger.d("restore state $state")
        comparisonState = state
        val viewState = viewState.orNull() ?: return
        val comparisonView =
            createAndReplaceComparisonView(viewState.comparisonView, state)
        this.viewState.set(viewState.copy(comparisonView = comparisonView))
    }

    override fun clear() {
        val newComparison = comparisonFactory.createComparison()
        val newComparisonState = ComparisonFragmentState(newComparison)
        comparisonState = newComparisonState

        val viewState = viewState.orNull() ?: return

        Logger.d("Clear comparison page")
        val comparisonView =
            createAndReplaceComparisonView(viewState.viewContainer, newComparisonState)

        this.viewState.set(viewState.copy(comparisonView = comparisonView))
    }

    override fun save() {
        Logger.d("Start save")
        val context = context ?: return
        val currentState = viewState.orNull()?.comparisonView?.getCurrentState() ?: return
        if (currentState.name.isNotBlank()) {
            save(currentState.withTimestamp(System.currentTimeMillis()))
            return
        }

        val layoutInflater = LayoutInflater.from(context)
        val viewBinding = ViewEnterNameBinding.inflate(layoutInflater)
        val name = viewBinding.comparisonLabel
        name.inputType = InputType.TYPE_CLASS_TEXT
        name.setHint(R.string.enter_name)
        name.requestFocus()

        val alertDialog = MaterialAlertDialogBuilder(context)
            .setMessage(R.string.give_name)
            .setView(viewBinding.root)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val newName = name.text.toString()
                if (newName.isBlank()) return@setPositiveButton
                viewState.orNull()?.comparisonView?.setTitle(newName)
                save(
                    currentState.copy(
                        timestampMillis = System.currentTimeMillis(),
                        name = newName
                    )
                )
            }
            .setNegativeButton(android.R.string.cancel, null)
            .create()

        name.afterTextChanged { s ->
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)?.isEnabled = s.isNotBlank()
        }

        alertDialog.setOnShowListener {
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)?.isEnabled = name.text.isNotBlank()
        }
        alertDialog.show()
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)?.isEnabled = false
        alertDialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
    }

    private fun save(comparison: SavedComparison) {
        Logger.d("Saving $comparison")
        savedComparisonManager.putSavedComparison(comparison)
        comparisonState = ComparisonFragmentState(comparison, comparison)
        syncViews()
    }

    private fun createAndReplaceComparisonView(
        container: ViewGroup,
        comparisonState: ComparisonFragmentState,
    ): ComparisonView {
        Logger.d("createAndReplaceComparisonView - $comparisonState")
        val previousComparisonView = viewState.orNull()?.comparisonView
        previousComparisonView?.listener = null
        container.removeAllViewsInLayout()
        val (comparison, lastSavedComparison) = comparisonState
        val comparisonView = ComparisonView(container.context, comparison, lastSavedComparison)
        container.addView(comparisonView, LayoutParams(MATCH_PARENT, MATCH_PARENT))
        comparisonView.listener = this
        return comparisonView
    }

    override fun onResume() {
        super.onResume()
        bus.register(this)
    }

    override fun onPause() {
        super.onPause()
        bus.unregister(this)
    }

    private fun syncViews() {
        val (comparison, lastSavedComparison) = comparisonState ?: return
        val viewState = viewState.orNull() ?: return
        val comparisonView = viewState.comparisonView

        comparisonView.setTitle(comparison.name)
        comparisonView.setLastSavedComparison(lastSavedComparison)
    }

    private data class ViewState(val viewContainer: FrameLayout, val comparisonView: ComparisonView)

    companion object {
        private val ComparisonKey =
            ParcelableBundleKey("comparison", ComparisonFragmentState::class)

        fun create(comparison: ComparisonFragmentState? = null): ComparisonFragment {
            val args = Bundle()
            args[ComparisonKey] = comparison
            return ComparisonFragment().also { it.arguments = args }
        }
    }
}