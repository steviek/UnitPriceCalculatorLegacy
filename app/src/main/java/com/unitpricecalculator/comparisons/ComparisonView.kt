package com.unitpricecalculator.comparisons

import android.annotation.SuppressLint
import android.content.Context
import android.icu.text.NumberFormat
import android.os.Build
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.ActionMode
import android.view.ActionMode.Callback
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.common.base.Optional
import com.squareup.otto.Bus
import com.squareup.otto.Subscribe
import com.unitpricecalculator.R
import com.unitpricecalculator.R.string
import com.unitpricecalculator.comparisons.Order.NONE
import com.unitpricecalculator.comparisons.Order.NUMBER_ASCENDING
import com.unitpricecalculator.comparisons.Order.NUMBER_DESCENDING
import com.unitpricecalculator.comparisons.Order.PRICE_ASCENDING
import com.unitpricecalculator.comparisons.Order.PRICE_DESCENDING
import com.unitpricecalculator.comparisons.Order.SIZE_ASCENDING
import com.unitpricecalculator.comparisons.Order.SIZE_DESCENDING
import com.unitpricecalculator.comparisons.UnitEntryView.Evaluation
import com.unitpricecalculator.comparisons.UnitEntryView.OnUnitEntryChangedListener
import com.unitpricecalculator.currency.Currencies
import com.unitpricecalculator.databinding.ComparisonViewBinding
import com.unitpricecalculator.events.CompareUnitChangedEvent
import com.unitpricecalculator.events.CurrencyChangedEvent
import com.unitpricecalculator.events.NoteChangedEvent
import com.unitpricecalculator.events.SystemChangedEvent
import com.unitpricecalculator.events.UnitTypeChangedEvent
import com.unitpricecalculator.locale.AppLocaleManager
import com.unitpricecalculator.unit.DefaultUnit
import com.unitpricecalculator.unit.Unit
import com.unitpricecalculator.unit.UnitEntry
import com.unitpricecalculator.unit.UnitFormatter
import com.unitpricecalculator.unit.UnitType
import com.unitpricecalculator.unit.Units
import com.unitpricecalculator.util.NumberUtils
import com.unitpricecalculator.util.abstracts.AbstractTextWatcher
import com.unitpricecalculator.util.addLocalizedKeyListener
import com.unitpricecalculator.util.afterTextChanged
import com.unitpricecalculator.util.children
import com.unitpricecalculator.util.getString
import com.unitpricecalculator.util.isIntegral
import com.unitpricecalculator.util.isSortedBy
import com.unitpricecalculator.util.logger.Logger
import com.unitpricecalculator.util.onItemSelected
import com.unitpricecalculator.util.parseDoubleOrThrow
import com.unitpricecalculator.util.prefs.Prefs
import com.unitpricecalculator.util.prefs.PrefsKeys
import com.unitpricecalculator.util.prefs.PrefsKeys.ShowPercentage
import com.unitpricecalculator.util.toLocalizedString
import dagger.hilt.android.AndroidEntryPoint
import java.lang.ref.WeakReference
import java.util.Locale
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@SuppressLint("ViewConstructor")
@AndroidEntryPoint
class ComparisonView(
    context: Context,
    initialComparison: SavedComparison,
    private var lastSavedComparison: SavedComparison?
) : FrameLayout(context), OnUnitEntryChangedListener {

    var comparisonKey = initialComparison.key

    @Inject
    internal lateinit var prefs: Prefs

    @Inject
    internal lateinit var units: Units

    @Inject
    internal lateinit var currencies: Currencies

    @Inject
    internal lateinit var unitTypeArrayAdapterFactory: UnitTypeArrayAdapterFactory

    @Inject
    internal lateinit var unitArrayAdapterFactory: UnitArrayAdapterFactory

    @Inject
    internal lateinit var bus: Bus

    @Inject
    internal lateinit var unitFormatter: UnitFormatter

    private val binding: ComparisonViewBinding = run {
        val layoutInflater = LayoutInflater.from(context)
        ComparisonViewBinding.inflate(layoutInflater).also { addView(it.root) }
    }

    private var actionMode: ActionMode? = null
    private var actionModePosition = -1

    private val nameTextWatcher = object : AbstractTextWatcher() {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            Logger.d("Name: Before text changed in $this: s=$s, start=$start, count=$count, after=$after")
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            Logger.d("Name: onTextChanged in $this: s=$s, start=$start, count=$count, before=$before")
        }

        override fun afterTextChanged(s: Editable?) {
            Logger.d("Name: afterTextChanged in $this: s=$s")
            syncViews()
        }
    }

    init {
        Logger.d("Create Comparison view for $initialComparison")
        with(binding) {
            scrollView.setOnClickListener { actionMode?.finish() }
            saveButton.setOnClickListener { listener?.save() }
            clearButton.setOnClickListener { listener?.clear() }
            nameEditText.setText(initialComparison.name)
            nameEditText.setOnKeyListener { v, keyCode, event ->
                if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                    val imm =
                        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.windowToken, 0)
                    return@setOnKeyListener true
                }
                false
            }
            nameEditText.addTextChangedListener(nameTextWatcher)

            priceHeader.text = (initialComparison.currency ?: units.currency).symbol
            priceHeader.setOnClickListener { currencies.showChangeCurrencyDialog() }
            priceHeader.setOnLongClickListener { _ ->
                applyOrderOrShowDialog(
                    if (unitEntries.isSortedBy { it.unitEntry.cost }) {
                        PRICE_DESCENDING
                    } else {
                        PRICE_ASCENDING
                    }
                )
                true
            }

            numberHeader.setOnLongClickListener {
                applyOrderOrShowDialog(
                    if (unitEntries.isSortedBy { it.unitEntry.quantity }) {
                        NUMBER_DESCENDING
                    } else {
                        NUMBER_ASCENDING
                    }
                )
                true
            }

            sizeHeader.setOnLongClickListener {
                applyOrderOrShowDialog(
                    if (unitEntries.isSortedBy { it.unitEntry.size }) {
                        SIZE_DESCENDING
                    } else {
                        SIZE_ASCENDING
                    }
                )
                true
            }

            val adapter = unitTypeArrayAdapterFactory.create()
            adapter.setNotifyOnChange(true)
            Logger.d("Set adapter with ${adapter.count} items, unit type is ${units.currentUnitType}")
            unitTypeTextField.setAdapter(adapter)
            unitTypeTextField.setText(initialComparison.unitType.loadLabel(context), false)
            unitTypeTextField.afterTextChanged { s ->
                val unitType =
                    UnitType.values()
                        .firstOrNull { it.loadLabel(context) == s }
                        ?: return@afterTextChanged
                Logger.d("Unit type changed to $unitType")
                if (units.currentUnitType != unitType) {
                    setUnitType(unitType)
                }
            }

            entryViews.forEach { it.setOnUnitEntryChangedListener(this@ComparisonView) }

            updateRowNumbers()

            addRowBtn.setOnClickListener { addRowView() }

            removeRowBtn.setOnClickListener { removeRow(entryViews.size - 1) }

            finalSize.hint = String.format(AppLocaleManager.getInstance().currentLocale, "%d", 1)
            finalSize.setText(initialComparison.finalQuantity)
            finalSize.afterTextChanged {
                bus.post(compareUnitChangedEvent())
                syncViews()
            }
            finalSize.addLocalizedKeyListener()

            if (finalSpinner.adapter == null) {
                finalSpinner.adapter = unitArrayAdapterFactory.create(initialComparison.finalUnit)
                finalSpinner.onItemSelected {
                    bus.post(compareUnitChangedEvent())
                    syncViews()
                }
            }

            initialComparison.savedUnitEntryRows.forEach { addRowView(it) }
        }

        syncViews()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        bus.register(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        bus.unregister(this)
    }

    override fun onUnitEntryChanged(view: UnitEntryView?, unitEntry: Optional<UnitEntry>?) {
        syncViews()
        actionMode?.finish()
    }

    @Subscribe
    fun onUnitTypeChanged(event: UnitTypeChangedEvent) {
        binding.finalSpinner.adapter =
                unitArrayAdapterFactory.create(units.getDefaultQuantity(event.unitType).unit)

        actionMode?.finish()
    }

    @Subscribe
    fun onSystemChanged(event: SystemChangedEvent?) {
        binding.finalSpinner.adapter = unitArrayAdapterFactory.create(units.getDefaultQuantity().unit)
        actionMode?.finish()
    }

    @Subscribe
    fun onNoteChanged(noteChangedEvent: NoteChangedEvent?) {
        syncViews()
    }

    @Subscribe
    fun onCurrencyChanged(event: CurrencyChangedEvent) {
        binding.priceHeader.text = event.currency.symbol
        syncViews()
    }

    var listener: ComparisonViewListener? = null

    fun setLastSavedComparison(comparison: SavedComparison?) {
        if (comparison == lastSavedComparison) return
        lastSavedComparison = comparison
        syncViews()
    }

    fun setTitle(title: String?) = with(binding) {
        if (nameEditText.text.toString() == (title ?: "")) return

        nameEditText.removeTextChangedListener(nameTextWatcher)
        nameEditText.setText(title)
        nameEditText.addTextChangedListener(nameTextWatcher)
    }

    private fun ComparisonViewBinding.applyOrderOrShowDialog(newOrder: Order) {
        if (prefs[PrefsKeys.OrderOnboarding] == true) {
            applyOrder(newOrder)
            return
        }
        MaterialAlertDialogBuilder(context)
            .setTitle(string.change_order_title)
            .setMessage(string.change_order_message)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                prefs[PrefsKeys.OrderOnboarding] = true
                applyOrder(newOrder)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .create()
            .show()
    }

    private fun ComparisonViewBinding.applyOrder(newOrder: Order) {
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
        syncViews()
    }

    private val unitEntries: List<UnitEntryWithIndexAndNote>
        get() {
            return entryViews.mapIndexedNotNull { index, entryView ->
                entryView.entry.orNull()
                    ?.let { UnitEntryWithIndexAndNote(index, it, entryView.note) }
            }
        }

    private val entryViews: List<UnitEntryView>
        get() = binding.rowContainer.children.filterIsInstance<UnitEntryView>()

    private fun updateRowNumbers() {
        entryViews.forEachIndexed { index, view -> view.rowNumber = index }
    }

    private fun setUnitType(unitType: UnitType) {
        if (!isAttachedToWindow) return

        units.currentUnitType = unitType
        Logger.d("Set unit type to $unitType")
        binding.unitTypeTextField.setText(unitType.loadLabel(context), /* filter= */ false)
        binding.finalSpinner.adapter =
            unitArrayAdapterFactory.create(units.getDefaultQuantity(unitType).unit)
        val event = binding.compareUnitChangedEvent()
        for (entryView in entryViews) {
            entryView.onCompareUnitChanged(event)
        }
        syncViews()
    }

    private fun ComparisonViewBinding.compareUnitChangedEvent(): CompareUnitChangedEvent {
        val unit: Unit = (finalSpinner.adapter as UnitArrayAdapter)
            .getUnit(finalSpinner.selectedItemPosition)
        val size = NumberUtils.firstParsableDouble(
            finalSize.text.toString(),
            units.getDefaultQuantity().amount.toLocalizedString()
        )
        return CompareUnitChangedEvent(size, unit)
    }

    private fun ComparisonViewBinding.addRowView(data: SavedUnitEntryRow? = null): UnitEntryView {
        val entryView = UnitEntryView(root.context)
        Logger.d("Add $entryView")
        rowContainer.addView(
            entryView,
            android.view.ViewGroup.LayoutParams(
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
        if (data != null) {
            entryView.restoreState(data)
        }

        val horizontalMargin = resources.getDimensionPixelSize(R.dimen.horizontal_margin)
        entryView.setPadding(horizontalMargin, 0, horizontalMargin, 0)
        val position = entryViews.size
        entryView.rowNumber = position - 1
        entryView.setOnUnitEntryChangedListener(this@ComparisonView)
        entryView.onCompareUnitChanged(compareUnitChangedEvent())
        entryView.isLongClickable = true
        val entryViewReference = WeakReference(entryView)
        entryView.setOnClickListener {
            actionMode?.finish()
        }
        entryView.setOnLongClickListener { v: View ->
            if (actionMode != null && actionModePosition == position) {
                actionMode?.finish()
                return@setOnLongClickListener true
            }

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
                            if (rowNumber - 1 > 0) {
                                mode.invalidate()
                            } else {
                                mode.finish()
                            }
                            true
                        }
                        R.id.action_down -> {
                            swapRows(rowNumber, rowNumber + 1)
                            if (rowNumber + 2 >= entryViews.size) {
                                mode.finish()
                            } else {
                                mode.invalidate()
                            }
                            true
                        }
                        else -> false
                    }
                }

                override fun onDestroyActionMode(mode: ActionMode) {
                    actionMode = null
                    actionModePosition = -1
                    entryViewReference.get()?.onExitActionMode()
                }
            }, ActionMode.TYPE_FLOATING)
            actionModePosition = position
            true
        }
        actionMode?.finish()
        syncViews()
        return entryView
    }

    private fun ComparisonViewBinding.removeRow(index: Int) {
        if (entryViews.size <= index) return
        val entryView = entryViews[index]
        if (entryView.focusedViewId != null) {
            entryView.clearFocus()
        }
        entryView.setOnUnitEntryChangedListener(null)
        rowContainer.removeView(entryView)
        updateRowNumbers()
        actionMode?.finish()
        syncViews()
    }

    private fun ComparisonViewBinding.swapRows(index1: Int, index2: Int) {
        require(index1 in entryViews.indices)
        require(index2 in entryViews.indices)
        if (index1 == index2) return

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
        maxEntryView.focusedViewId = maxFocusedViewId
        minEntryView.focusedViewId = minFocusedViewId
        syncViews()
    }

    fun getCurrentState(): SavedComparison {
        val list = entryViews.map { it.saveState(context) }
        val unitType = units.currentUnitType
        val finalSize = binding.finalSize.text.toString()
        val finalSpinner = binding.finalSpinner
        val finalUnit =
            (finalSpinner.adapter as UnitArrayAdapter).getUnit(finalSpinner.selectedItemPosition)
        val savedName = binding.nameEditText.text?.toString() ?: ""
        return SavedComparison(
            comparisonKey,
            savedName,
            unitType,
            list,
            finalSize,
            finalUnit,
            units.currency.currencyCode,
            /* timestampMillis= */ null
        )
    }

    private fun syncViews() {
        val currentState = getCurrentState()
        val lastSavedComparison = lastSavedComparison
        with(binding) {
            val canSave = (currentState.isEmpty && lastSavedComparison != null) ||
                    (currentState.isNotEmpty && currentState.copy(timestampMillis = null) !=
                            lastSavedComparison?.copy(timestampMillis = null))
            saveButton.isEnabled = canSave

            val canClear = lastSavedComparison != null || currentState.isNotEmpty
            clearButton.isEnabled = canClear

            val compareUnit = compareUnitChangedEvent()
            val size = compareUnit.size.parseDoubleOrThrow()
            val unit = compareUnit.unit
            if (finalSize.text.toString().isBlank()) {
                finalSize.hint = units.getDefaultQuantity().amount.toLocalizedString()
            }
            val sortedUnitEntries = unitEntries.sortedBy { it.unitEntry.pricePerBaseUnit }
            if (sortedUnitEntries.size < 2) {
                finalTextSummary.text = ""
                for (entryView in entryViews) {
                    entryView.setEvaluation(Evaluation.NEUTRAL)
                }
            } else {
                val finalSummary = SpannableStringBuilder()
                fun setColor(evaluation: Evaluation, start: Int, end: Int) {
                    val context = context ?: return
                    finalSummary.setSpan(
                        ForegroundColorSpan(
                            ContextCompat.getColor(context, evaluation.primaryColor)
                        ),
                        start,
                        end,
                        android.text.Spannable.SPAN_INCLUSIVE_EXCLUSIVE
                    )
                }

                val best = sortedUnitEntries[0]
                finalSummary.append(getString(string.main_final_summary, best.index + 1))
                    .append("\n\n")
                val startBestSpan = finalSummary.length
                appendSingleRowSummary(finalSummary, best, compareUnit, best.unitEntry)
                val endBestSpan = finalSummary.length
                setColor(Evaluation.GOOD, startBestSpan, endBestSpan)

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
                            Evaluation.GOOD
                        } else {
                            Evaluation.BAD
                        }
                    setColor(evaluation, spanStart, spanEnd)
                }
                finalTextSummary.text = finalSummary
                for (entryView in entryViews) {
                    val entry = entryView.entry.orNull()
                    entryView.setEvaluation(
                        when {
                            entry == null -> Evaluation.NEUTRAL
                            entry.pricePer(size, unit) <= best.unitEntry.pricePer(
                                size,
                                unit
                            ) -> Evaluation.GOOD
                            else -> Evaluation.BAD
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
        val (index, unitEntry, note) = unitEntryWithIndexAndNote

        if (note.isNullOrBlank()) {
            message.append(getString(string.row_x, index + 1))
        } else {
            message.append(getString(string.note_row_x, note, index + 1))
        }
        message.append("\n")

        val compareUnit = compareUnitChangedEvent.unit
        val compareSize = compareUnitChangedEvent.size
        val formatter = units.formatter
        val formattedEntryCostString = formatter.format(unitEntry.cost)
        val formattedUnit =
            unitFormatter.format(
                unitEntry.unit as DefaultUnit,
                unitEntry.size,
                unitEntry.sizeString,
            )
        if (unitEntry.quantityString?.toDoubleOrNull() == 1.0) {
            message.append(getString(string.m_per_u, formattedEntryCostString, formattedUnit))
        } else {
            message.append(
                getString(
                    string.m_per_qxu,
                    formattedEntryCostString,
                    unitEntry.quantityString,
                    formattedUnit
                )
            )
        }
        message.append(" = ")
        val formattedCompareUnitCost = formatter
            .format(unitEntry.pricePer(compareSize.parseDoubleOrThrow(), compareUnit))
        val formattedCompareUnit =
            unitFormatter.format(
                compareUnit as DefaultUnit,
                compareSize.toDoubleOrNull() ?: 1.0,
                compareSize
            )
        message
            .append(getString(string.m_per_u, formattedCompareUnitCost, formattedCompareUnit))
        message.append("\n")

        if (prefs[ShowPercentage] == true && best !== unitEntry) {
            val percent = (unitEntry.pricePerBaseUnit / best.pricePerBaseUnit) - 1
            val formattedPercentage = NumberFormat.getPercentInstance().format(percent)
            message.append("(+").append(formattedPercentage).append(")").append("\n")
        }

        message.append("\n")
    }

    private data class UnitEntryWithIndexAndNote(
        val index: Int,
        val unitEntry: UnitEntry,
        val note: String?
    )

    interface ComparisonViewListener {
        fun save()

        fun clear()
    }
}