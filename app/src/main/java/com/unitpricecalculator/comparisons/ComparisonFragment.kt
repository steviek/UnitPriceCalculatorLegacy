package com.unitpricecalculator.comparisons

import android.content.Context
import android.icu.text.NumberFormat
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.InputType
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.ActionMode
import android.view.ActionMode.Callback
import android.view.ContextThemeWrapper
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.common.base.Optional
import com.google.common.base.Preconditions
import com.squareup.otto.Bus
import com.squareup.otto.Subscribe
import com.unitpricecalculator.BaseFragment
import com.unitpricecalculator.R
import com.unitpricecalculator.R.layout
import com.unitpricecalculator.R.string
import com.unitpricecalculator.R.style
import com.unitpricecalculator.comparisons.Order.NONE
import com.unitpricecalculator.comparisons.Order.NUMBER_ASCENDING
import com.unitpricecalculator.comparisons.Order.NUMBER_DESCENDING
import com.unitpricecalculator.comparisons.Order.PRICE_ASCENDING
import com.unitpricecalculator.comparisons.Order.PRICE_DESCENDING
import com.unitpricecalculator.comparisons.Order.SIZE_ASCENDING
import com.unitpricecalculator.comparisons.Order.SIZE_DESCENDING
import com.unitpricecalculator.comparisons.UnitEntryView.Evaluation
import com.unitpricecalculator.comparisons.UnitEntryView.Evaluation.BAD
import com.unitpricecalculator.comparisons.UnitEntryView.Evaluation.GOOD
import com.unitpricecalculator.comparisons.UnitEntryView.Evaluation.NEUTRAL
import com.unitpricecalculator.comparisons.UnitEntryView.OnUnitEntryChangedListener
import com.unitpricecalculator.currency.Currencies
import com.unitpricecalculator.databinding.ActionBarEditTextBinding
import com.unitpricecalculator.databinding.ComparisonFragmentBinding
import com.unitpricecalculator.events.CompareUnitChangedEvent
import com.unitpricecalculator.events.CurrencyChangedEvent
import com.unitpricecalculator.events.NoteChangedEvent
import com.unitpricecalculator.events.SystemChangedEvent
import com.unitpricecalculator.events.UnitTypeChangedEvent
import com.unitpricecalculator.saved.SavedComparisonManager
import com.unitpricecalculator.unit.Systems
import com.unitpricecalculator.unit.Unit
import com.unitpricecalculator.unit.UnitEntry
import com.unitpricecalculator.unit.UnitType
import com.unitpricecalculator.unit.Units
import com.unitpricecalculator.util.MenuItems
import com.unitpricecalculator.util.NumberUtils
import com.unitpricecalculator.util.SavesState
import com.unitpricecalculator.util.abstracts.AbstractOnItemSelectedListener
import com.unitpricecalculator.util.abstracts.AbstractTextWatcher
import com.unitpricecalculator.util.addLocalizedKeyListener
import com.unitpricecalculator.util.children
import com.unitpricecalculator.util.isIntegral
import com.unitpricecalculator.util.isSortedBy
import com.unitpricecalculator.util.logger.Logger
import com.unitpricecalculator.util.materialize
import com.unitpricecalculator.util.parseDoubleOrThrow
import com.unitpricecalculator.util.prefs.Keys
import com.unitpricecalculator.util.prefs.Prefs
import com.unitpricecalculator.util.sometimes.MutableSometimes
import com.unitpricecalculator.util.toLocalizedString
import dagger.hilt.android.AndroidEntryPoint
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit.SECONDS
import javax.inject.Inject
import javax.inject.Provider
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@AndroidEntryPoint
class ComparisonFragment :
    BaseFragment(), OnUnitEntryChangedListener, SavesState<ComparisonFragmentState?> {

    @Inject
    internal lateinit var prefs: Prefs

    @Inject
    internal lateinit var units: Units

    @Inject
    internal lateinit var systems: Systems

    @Inject
    internal lateinit var unitTypeArrayAdapterProvider: Provider<UnitTypeArrayAdapter>

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

    private val binding = MutableSometimes.create<ComparisonFragmentBinding>()

    private val fileNameEditText = MutableSometimes.create<EditText>()
    private val saveMenuItem = MutableSometimes.create<MenuItem>()
    private val resumed = MutableSometimes.create<Any>()

    private lateinit var unitTypeArrayAdapter: UnitTypeArrayAdapter
    private var alertDialog: AlertDialog? = null
    private var actionMode: ActionMode? = null
    private val handler = Handler()
    private var savedChangesCountdown: Int? = null
    private val savedChangesCountdownTick: Runnable = object : Runnable {
        override fun run() {
            val tick = savedChangesCountdown ?: return
            val binding = binding.orNull() ?: return
            val runnable = this
            with(binding) {
                savedChangesStatus.text = getString(string.all_changes_saved, tick)
                if (tick >= 1) {
                    savedChangesCountdown = tick - 1
                    handler.postDelayed(runnable, SECONDS.toMillis(1))
                } else {
                    savedChangesCountdown = null
                    savedChangesStatus.visibility = View.GONE
                    savedChangesDivider.visibility = View.GONE
                }
            }
        }
    }
    private var pendingSavedStateToRestore: ComparisonFragmentState? = null
    private var draftKey = System.currentTimeMillis().toString()
    private var lastKnownSavedState: SavedComparison? = null
    private var isViewCreated = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        val binding = ComparisonFragmentBinding.inflate(inflater, container, false)
        this.binding.set(binding)

        with(binding) {
            priceHeader.text = units.currency.symbol
            priceHeader.setOnClickListener { currencies.showChangeCurrencyDialog() }
            priceHeader.setOnLongClickListener { _ ->
                applyOrderOrShowDialog(
                    if (unitEntries.isSortedBy { it.unitEntry.cost }) {
                        Order.PRICE_DESCENDING
                    } else {
                        Order.PRICE_ASCENDING
                    }
                )
                true
            }

            numberHeader.setOnLongClickListener {
                applyOrderOrShowDialog(
                    if (unitEntries.isSortedBy { it.unitEntry.quantity }) {
                        Order.NUMBER_DESCENDING
                    } else {
                        Order.NUMBER_ASCENDING
                    }
                )
                true
            }

            sizeHeader.setOnLongClickListener {
                applyOrderOrShowDialog(
                    if (unitEntries.isSortedBy { it.unitEntry.size }) {
                        Order.SIZE_DESCENDING
                    } else {
                        Order.SIZE_ASCENDING
                    }
                )
                true
            }

            unitTypeArrayAdapter = unitTypeArrayAdapterProvider.get()
            unitTypeSpinner.adapter = unitTypeArrayAdapter
            unitTypeSpinner.onItemSelectedListener = object : AbstractOnItemSelectedListener() {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    Logger.d("onItemSelected: %s", parent.getItemAtPosition(position))
                    val unitType = UnitType.fromName(
                        parent.getItemAtPosition(position) as String,
                        resources
                    )
                    if (units.currentUnitType != unitType) {
                        setUnitType(parent as Spinner, unitType)
                    }
                }
            }

            entryViews.forEach { it.setOnUnitEntryChangedListener(this@ComparisonFragment) }
            updateRowNumbers()

            addRowBtn.setOnClickListener { addRowView() }

            removeRowBtn.setOnClickListener { removeRow(entryViews.size - 1) }

            finalSize.addTextChangedListener(object : AbstractTextWatcher() {
                override fun afterTextChanged(s: Editable) {
                    bus.post(compareUnit)
                    refreshViews()
                }
            })
            finalSize.addLocalizedKeyListener()

            if (finalSpinner.adapter == null) {
                finalSpinner.adapter =
                    unitArrayAdapterFactory.create(units.getDefaultQuantity().unit)
                finalSpinner.onItemSelectedListener = object : AbstractOnItemSelectedListener() {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        bus.post(compareUnit)
                        refreshViews()
                    }
                }
            }
            addRowView()
            addRowView()
            isViewCreated = true
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.set(null)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        val layoutInflater =
            LayoutInflater.from(ContextThemeWrapper(getActivity(), style.FileNameEditText))
        val editText = ActionBarEditTextBinding.inflate(layoutInflater).root
        editText.setOnKeyListener { v: View, keyCode: Int, event: KeyEvent ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                val imm =
                    requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.windowToken, 0)
                return@setOnKeyListener true
            }
            false
        }
        editText.addTextChangedListener(object : AbstractTextWatcher() {
            override fun afterTextChanged(s: Editable) {
                refreshViews()
            }
        })
        fileNameEditText.set(editText)
        lastKnownSavedState?.let { editText.setText(it.name) }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        fileNameEditText.whenPresent { activity.supportActionBar?.customView = it }
        activity.supportActionBar?.setDisplayShowCustomEnabled(true)
        activity.title = ""
        activity.menuInflater.inflate(R.menu.menu_main, menu)
        saveMenuItem.set(menu.findItem(R.id.action_save))
    }

    override fun onDestroyOptionsMenu() {
        super.onDestroyOptionsMenu()
        fileNameEditText.set(null)
        saveMenuItem.set(null)
    }

    private fun setUnitType(parent: Spinner, unitType: UnitType) {
        val binding = binding.orNull() ?: return
        units.currentUnitType = unitType
        unitTypeArrayAdapter = unitTypeArrayAdapterProvider.get()
        parent.adapter = unitTypeArrayAdapter
        binding.finalSpinner.adapter =
            unitArrayAdapterFactory.create(units.getDefaultQuantity(unitType).unit)
        val event = binding.compareUnit
        for (entryView in binding.entryViews) {
            entryView.onCompareUnitChanged(event)
        }
        refreshViews()
    }

    private fun ComparisonFragmentBinding.addRowView(): UnitEntryView {
        val entryView = UnitEntryView(root.context)
        rowContainer.addView(
            entryView,
            LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            )
        )
        val horizontalMargin = resources.getDimensionPixelSize(R.dimen.horizontal_margin)
        entryView.setPadding(horizontalMargin, 0, horizontalMargin, 0)
        entryView.rowNumber = entryViews.size - 1
        entryView.setOnUnitEntryChangedListener(this@ComparisonFragment)
        entryView.onCompareUnitChanged(compareUnit)
        entryView.isLongClickable = true
        val entryViewReference = WeakReference(entryView)
        entryView.setOnLongClickListener { v: View ->
            actionMode = v.startActionMode(object : Callback {
                override fun onCreateActionMode(
                    mode: ActionMode,
                    menu: Menu
                ): Boolean {
                    if (entryViews.size < 2) {
                        return false
                    }
                    mode.menuInflater.inflate(R.menu.menu_row_action_mode, menu)
                    return entryViewReference.get()?.let {
                        it.onEnterActionMode()
                        true
                    } ?: false
                }

                override fun onPrepareActionMode(
                    mode: ActionMode,
                    menu: Menu
                ): Boolean {
                    val rowNumber = entryViewReference.get()?.rowNumber ?: return false
                    val upVisible = rowNumber > 0
                    menu.findItem(R.id.action_up).isVisible = upVisible
                    val downVisible = rowNumber < entryViews.size - 1
                    menu.findItem(R.id.action_down).isVisible = downVisible
                    val deleteVisible = entryViews.size >= 2
                    menu.findItem(R.id.action_delete).isVisible = deleteVisible
                    return true
                }

                override fun onActionItemClicked(
                    mode: ActionMode,
                    item: MenuItem
                ): Boolean {
                    val rowNumber = entryViewReference.get()?.rowNumber ?: return false
                    return when (item.itemId) {
                        R.id.action_delete -> {
                            removeRow(rowNumber)
                            true
                        }
                        R.id.action_up -> {
                            swapRows(rowNumber, rowNumber - 1)
                            mode.invalidate()
                            true
                        }
                        R.id.action_down -> {
                            swapRows(rowNumber, rowNumber + 1)
                            mode.invalidate()
                            true
                        }
                        else -> false
                    }
                }

                override fun onDestroyActionMode(mode: ActionMode) {
                    actionMode = null
                    entryViewReference.get()?.onExitActionMode()
                }
            })
            true
        }
        finishActionMode()
        refreshViews()
        return entryView
    }

    override fun saveState(context: Context): ComparisonFragmentState {
        return ComparisonFragmentState(getCurrentState(), lastKnownSavedState)
    }

    private fun getCurrentState(): SavedComparison? {
        val context = context ?: return null
        val binding = binding.orNull() ?: return null
        pendingSavedStateToRestore?.let { return it.currentComparison }
        val list = binding.entryViews.map { it.saveState(context) }
        val unitType = UnitType.fromName(
            unitTypeArrayAdapter.getItem(binding.unitTypeSpinner.selectedItemPosition),
            context.resources
        )
        val finalSize = binding.finalSize.text.toString()
        val finalSpinner = binding.finalSpinner
        val finalUnit =
            (finalSpinner.adapter as UnitArrayAdapter).getUnit(finalSpinner.selectedItemPosition)
        val savedName = fileNameEditText.orNull()?.text?.toString() ?: ""
        val key = lastKnownSavedState?.key ?: draftKey
        return SavedComparison(
            key,
            savedName,
            unitType,
            list,
            finalSize,
            finalUnit,
            units.currency.currencyCode,
            /* timestampMillis= */ null
        )
    }

    override fun restoreState(state: ComparisonFragmentState) {
        pendingSavedStateToRestore = state
        val binding = binding.orNull() ?: run {
            binding.whenPresent {
                pendingSavedStateToRestore?.let { restoreState(it) }
            }
            return
        }
        val fileNameEditText = fileNameEditText.orNull()
        if (fileNameEditText == null) {
            this.fileNameEditText.whenPresent { restoreState(state) }
            return
        }
        lastKnownSavedState = state.lastKnownSavedComparison
        binding.rowContainer.removeAllViewsInLayout()
        val comparison = state.currentComparison ?: return
        setUnitType(binding.unitTypeSpinner, comparison.unitType)
        for (entryRow in comparison.savedUnitEntryRows) {
            val entryView = binding.addRowView()
            entryView.restoreState(entryRow)
        }
        binding.finalSize.setText(comparison.finalQuantity)
        val unit = comparison.finalUnit
        val adapter = unitArrayAdapterFactory.create(unit)
        binding.finalSpinner.adapter = adapter
        binding.finalSpinner.setSelection(0)
        draftKey = comparison.key
        fileNameEditText.setText(comparison.name)
        pendingSavedStateToRestore = null
        adapter.notifyDataSetChanged()
        refreshViews()
    }

    fun clear() {
        lastKnownSavedState = null
        if (!resumed.isPresent) {
            resumed.whenPresent { clear() }
            return
        }
        val binding = binding.orNull() ?: run {
            binding.whenPresent { clear() }
            return
        }
        with(binding) {
            rowContainer.removeAllViewsInLayout()
            addRowView()
            addRowView()
            finalSize.setText("")
            finalSpinner.adapter = unitArrayAdapterFactory.create(units.getDefaultQuantity().unit)
            finalTextSummary.text = ""
            fileNameEditText.whenPresent { it.setText("") }
            draftKey = System.currentTimeMillis().toString()
            refreshViews()
        }
    }

    fun save() {
        val context = context ?: return
        val currentState = getCurrentState() ?: return
        if (!fileNameEditText.orNull()?.text?.toString().isNullOrBlank()) {
            save(currentState.withTimestamp(System.currentTimeMillis()))
            return
        }
        val dialog = alertDialog ?: run {
            val view = LayoutInflater.from(context).inflate(layout.view_enter_name, null)
            val name = view.findViewById<EditText>(R.id.comparison_label)
            name.inputType = InputType.TYPE_CLASS_TEXT
            name.setHint(string.enter_name)
            name.requestFocus()
            name.addTextChangedListener(object : AbstractTextWatcher() {
                override fun afterTextChanged(s: Editable) {
                    alertDialog?.getButton(AlertDialog.BUTTON_POSITIVE)?.isEnabled = s.isNotBlank()
                }
            })
            AlertDialog.Builder(context)
                .setMessage(string.give_name)
                .setView(view)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    val newName = name.text.toString()
                    if (newName.isNotBlank()) {
                        fileNameEditText.whenPresent { editText: EditText ->
                            editText.setText(newName)
                            save(
                                currentState.copy(
                                    timestampMillis = System.currentTimeMillis(),
                                    name = newName
                                )
                            )
                        }
                    }
                }

                .setNegativeButton(android.R.string.cancel, null)
                .setOnDismissListener { alertDialog = null }
                .create()
                .also { dialog ->
                    dialog.setOnShowListener {
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.isEnabled =
                            name.text.isNotBlank()
                    }
                    alertDialog = dialog
                }
        }
        if (dialog.isShowing) {
            dialog.dismiss()
        } else {
            dialog.show()
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.isEnabled = false
            dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
            dialog.materialize()
        }
    }

    private fun save(comparison: SavedComparison) {
        if (!prefs.getBoolean(Keys.HAS_CLICKED_SAVE)) {
            prefs.putBoolean(Keys.HAS_CLICKED_SAVE, true)
            savedChangesCountdown = 10
            savedChangesCountdownTick.run()
        }
        Preconditions.checkNotNull(comparison.key)
        savedComparisonManager.putSavedComparison(comparison)
        lastKnownSavedState = comparison
        refreshViews()
    }

    override fun onResume() {
        super.onResume()
        bus.register(this)
        pendingSavedStateToRestore?.let {
            restoreState(it)
            refreshViews()
        }
        resumed.set(Any())
    }

    override fun onPause() {
        super.onPause()
        bus.unregister(this)
        resumed.set(null)
    }

    @Subscribe
    fun onUnitTypeChanged(event: UnitTypeChangedEvent) {
        binding.orNull()?.apply {
            finalSpinner.adapter =
                unitArrayAdapterFactory.create(units.getDefaultQuantity(event.unitType).unit)
        }

        finishActionMode()
    }

    @Subscribe
    fun onSystemChanged(event: SystemChangedEvent?) {
        binding.orNull()?.apply {
            finalSpinner.adapter = unitArrayAdapterFactory.create(units.getDefaultQuantity().unit)
        }
        finishActionMode()
    }

    @Subscribe
    fun onNoteChanged(noteChangedEvent: NoteChangedEvent?) {
        refreshViews()
    }

    @Subscribe
    fun onCurrencyChanged(event: CurrencyChangedEvent) {
        binding.orNull()?.apply {
            priceHeader.text = event.currency.symbol
        }
        refreshViews()
    }

    override fun onUnitEntryChanged(unitEntry: Optional<UnitEntry>) {
        refreshViews()
        finishActionMode()
    }

    private val ComparisonFragmentBinding.compareUnit: CompareUnitChangedEvent
        get() {
            val unit: Unit = (finalSpinner.adapter as UnitArrayAdapter)
                .getUnit(finalSpinner.selectedItemPosition)
            val size = NumberUtils.firstParsableDouble(
                finalSize.text.toString(),
                units.getDefaultQuantity().amount.toLocalizedString()
            )
            return CompareUnitChangedEvent(size, unit)
        }

    private fun refreshViews() {
        val currentState = getCurrentState()
        val binding = binding.orNull() ?: return
        if (pendingSavedStateToRestore != null || !isViewCreated || currentState == null) {
            return
        }
        with(binding) {
            val hasClickedSave = prefs.getBoolean(Keys.HAS_CLICKED_SAVE)
            val canSave = (currentState.isEmpty() && lastKnownSavedState != null) ||
                    (!currentState.isEmpty() && currentState.copy(timestampMillis = null) !=
                            lastKnownSavedState?.copy(timestampMillis = null))
            saveMenuItem.whenPresent { MenuItems.setEnabled(it, canSave) }
            if (hasClickedSave && savedChangesCountdown == null) {
                savedChangesStatus.visibility = View.GONE
                savedChangesDivider.visibility = View.GONE
            } else if (!hasClickedSave && canSave) {
                savedChangesStatus.visibility = View.VISIBLE
                savedChangesStatus.setText(string.unsaved_changes)
                savedChangesDivider.visibility = View.VISIBLE
            } else if (!hasClickedSave) {
                savedChangesStatus.visibility = View.GONE
                savedChangesDivider.visibility = View.GONE
            }
            val compareUnit = compareUnit
            val size = compareUnit.size.parseDoubleOrThrow()
            val unit = compareUnit.unit
            if (finalSize.text.toString().isBlank()) {
                finalSize.hint = units.getDefaultQuantity().amount.toLocalizedString()
            }
            val sortedUnitEntries = unitEntries.sortedBy { it.unitEntry.pricePerBaseUnit }
            if (sortedUnitEntries.size < 2) {
                finalTextSummary.text = ""
                for (entryView in entryViews) {
                    entryView.setEvaluation(NEUTRAL)
                }
            } else {
                val finalSummary = SpannableStringBuilder()
                fun setColor(evaluation: Evaluation, start: Int, end: Int) {
                    val context = context ?: return
                    finalSummary.setSpan(
                        ForegroundColorSpan(
                            ContextCompat.getColor(
                                context,
                                evaluation.primaryColor
                            )
                        ),
                        start,
                        end,
                        Spannable.SPAN_INCLUSIVE_EXCLUSIVE
                    )
                }

                val best = sortedUnitEntries[0]
                finalSummary.append(getString(string.main_final_summary, best.index + 1))
                    .append("\n\n")
                val startBestSpan = finalSummary.length
                appendSingleRowSummary(finalSummary, best, compareUnit, best.unitEntry)
                val endBestSpan = finalSummary.length
                setColor(GOOD, startBestSpan, endBestSpan)

                for (entryWithIndex in sortedUnitEntries) {
                    if (entryWithIndex.index == best.index) continue
                    val spanStart = finalSummary.length
                    appendSingleRowSummary(
                        finalSummary,
                        entryWithIndex,
                        compareUnit,
                        best.unitEntry
                    )
                    val spanEnd = finalSummary.length
                    val evaluation =
                        if (entryWithIndex.unitEntry.pricePerBaseUnit == best.unitEntry.pricePerBaseUnit) {
                            GOOD
                        } else {
                            BAD
                        }
                    setColor(evaluation, spanStart, spanEnd)
                }
                finalTextSummary.text = finalSummary
                for (entryView in entryViews) {
                    val entry = entryView.entry.orNull()
                    entryView.setEvaluation(
                        when {
                            entry == null -> NEUTRAL
                            entry.pricePer(size, unit) <= best.unitEntry.pricePer(
                                size,
                                unit
                            ) -> GOOD
                            else -> BAD
                        }
                    )
                }
            }
            removeRowBtn.isEnabled = entryViews.size > 1
            addRowBtn.isEnabled = entryViews.size < 10
        }
    }

    private fun appendSingleRowSummary(
        message: SpannableStringBuilder,
        unitEntryWithIndexAndNote: UnitEntryWithIndexAndNote,
        compareUnitChangedEvent: CompareUnitChangedEvent,
        best: UnitEntry
    ) {
        val context = context ?: return
        val (index, unitEntry, note) = unitEntryWithIndexAndNote

        if (note.isNullOrBlank()) {
            message.append(context.getString(R.string.row_x, index + 1))
        } else {
            message.append(context.getString(R.string.note_row_x, note, index + 1))
        }
        message.append("\n")

        val compareUnit = compareUnitChangedEvent.unit
        val compareSize = compareUnitChangedEvent.size
        val formatter = units.formatter
        val formattedEntryCostString = formatter.apply(unitEntry.cost)
        val unitEntrySymbol = unitEntry.unit.getSymbol(resources)
        if (unitEntry.quantity == 1 && unitEntry.sizeString == "1") {
            message.append(getString(string.m_per_u, formattedEntryCostString, unitEntrySymbol))
        } else if (unitEntry.quantity == 1) {
            message.append(
                getString(
                    string.m_per_s_u,
                    formattedEntryCostString,
                    unitEntry.sizeString,
                    unitEntrySymbol
                )
            )
        } else {
            message.append(
                getString(
                    string.m_per_qxs_u, formattedEntryCostString, unitEntry.quantityString,
                    unitEntry.sizeString, unitEntrySymbol
                )
            )
        }
        message.append(" = ")
        val formattedCompareUnitCost = formatter
            .apply(unitEntry.pricePer(compareSize.parseDoubleOrThrow(), compareUnit))
        val compareUnitSymbol = compareUnit.getSymbol(resources)
        if (compareSize == "1") {
            message
                .append(getString(string.m_per_u, formattedCompareUnitCost, compareUnitSymbol))
        } else {
            message.append(
                getString(
                    string.m_per_s_u,
                    formattedCompareUnitCost,
                    compareSize,
                    compareUnitSymbol
                )
            )
        }
        message.append("\n")

        if (prefs.getBoolean(Keys.SHOW_PERCENTAGE) && best !== unitEntry) {
            val formattedPercentage = if (Build.VERSION.SDK_INT >= 24) {
                val percent = (unitEntry.pricePerBaseUnit / best.pricePerBaseUnit) - 1
                NumberFormat.getPercentInstance().format(percent)
            } else {
                val percent = 100 * ((unitEntry.pricePerBaseUnit / best.pricePerBaseUnit) - 1)
                val roundedPercent = when {
                    percent.isIntegral() -> percent
                    percent < 1 -> percent
                    percent >= 100 -> percent.roundToInt().toDouble()
                    else -> (percent * 100).toInt() / 100.00
                }
                roundedPercent.toLocalizedString() + "%"
            }
            message.append("(+").append(formattedPercentage).append(")").append("\n")
        }

        message.append("\n")
    }

    private fun finishActionMode() {
        actionMode?.finish()
    }

    private fun ComparisonFragmentBinding.swapRows(index1: Int, index2: Int) {
        Preconditions.checkArgument(index1 >= 0)
        Preconditions.checkArgument(index2 >= 0)
        Preconditions.checkArgument(index1 < entryViews.size)
        Preconditions.checkArgument(index2 < entryViews.size)
        if (index1 == index2) {
            return
        }
        val max = max(index1, index2)
        val min = min(index1, index2)
        val maxEntryView = entryViews[max]
        val minEntryView = entryViews[min]
        val maxFocusedViewId = maxEntryView.focusedViewId
        val minFocusedViewId = minEntryView.focusedViewId
        rowContainer.removeView(maxEntryView)
        rowContainer.removeView(minEntryView)
        rowContainer.addView(maxEntryView, min)
        rowContainer.addView(minEntryView, max)
        minEntryView.rowNumber = max
        maxEntryView.rowNumber = min
        if (maxFocusedViewId.isPresent) {
            maxEntryView.setFocusedViewId(maxFocusedViewId.get())
        } else if (minFocusedViewId.isPresent) {
            minEntryView.setFocusedViewId(minFocusedViewId.get())
        }
        refreshViews()
    }

    private fun ComparisonFragmentBinding.removeRow(index: Int) {
        if (entryViews.size <= index) return
        val entryView = entryViews[index]
        if (entryView.focusedViewId.isPresent) {
            entryView.clearFocus()
        }
        entryView.setOnUnitEntryChangedListener(null)
        rowContainer.removeView(entryView)
        updateRowNumbers()
        finishActionMode()
        refreshViews()
    }

    private fun ComparisonFragmentBinding.applyOrderOrShowDialog(newOrder: Order) {
        val context = context ?: return
        if (prefs.getBoolean(Keys.ORDER_ONBOARDING)) {
            applyOrder(newOrder)
            return
        }
        AlertDialog.Builder(context)
            .setTitle(string.change_order_title)
            .setMessage(string.change_order_message)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                prefs.putBoolean(Keys.ORDER_ONBOARDING, true)
                applyOrder(newOrder)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .create()
            .show()
    }

    private fun ComparisonFragmentBinding.applyOrder(newOrder: Order) {
        entryViews
            .mapNotNull { view -> view.entry.orNull()?.let { it -> view to it } }
            .onEach { (view, _) -> rowContainer.removeView(view) }
            .sortedBy { (_, entry) ->
                when (newOrder) {
                    PRICE_ASCENDING -> entry.cost
                    PRICE_DESCENDING -> -entry.cost
                    NUMBER_ASCENDING -> entry.quantity.toDouble()
                    NUMBER_DESCENDING -> -entry.quantity.toDouble()
                    SIZE_ASCENDING -> entry.size
                    SIZE_DESCENDING -> -entry.size
                    NONE -> 0.0
                }
            }
            .asReversed()
            .forEach { (view, _) -> rowContainer.addView(view, 0) }

        updateRowNumbers()
        refreshViews()
    }

    private fun ComparisonFragmentBinding.updateRowNumbers() {
        entryViews.forEachIndexed { index, view -> view.rowNumber = index }
    }

    private val ComparisonFragmentBinding.entryViews: List<UnitEntryView>
        get() = rowContainer.children.filterIsInstance<UnitEntryView>()

    private val ComparisonFragmentBinding.unitEntries: List<UnitEntryWithIndexAndNote>
        get() {
            return entryViews.mapIndexedNotNull { index, entryView ->
                entryView.entry.orNull()
                    ?.let { UnitEntryWithIndexAndNote(index, it, entryView.note) }
            }
        }

    private data class UnitEntryWithIndexAndNote(
        val index: Int,
        val unitEntry: UnitEntry,
        val note: String?
    )
}