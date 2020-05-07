package com.unitpricecalculator.comparisons

import android.content.Context
import android.icu.text.DecimalFormat
import android.icu.text.NumberFormat
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.InputType
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.util.TypedValue
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
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.common.base.Optional
import com.google.common.base.Preconditions
import com.google.common.base.Strings
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
import com.unitpricecalculator.events.CompareUnitChangedEvent
import com.unitpricecalculator.events.CurrencyChangedEvent
import com.unitpricecalculator.events.NoteChangedEvent
import com.unitpricecalculator.events.SystemChangedEvent
import com.unitpricecalculator.events.UnitTypeChangedEvent
import com.unitpricecalculator.saved.SavedComparisonManager
import com.unitpricecalculator.unit.DefaultUnit
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
import java.lang.ref.WeakReference
import java.util.ArrayList
import java.util.Locale
import java.util.concurrent.TimeUnit.SECONDS
import javax.inject.Inject
import javax.inject.Provider
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class ComparisonFragment :
  BaseFragment(), OnUnitEntryChangedListener, SavesState<ComparisonFragmentState?> {

  @Inject internal lateinit var prefs: Prefs

  @Inject internal lateinit var units: Units

  @Inject internal lateinit var systems: Systems

  @Inject internal lateinit var unitTypeArrayAdapterProvider: Provider<UnitTypeArrayAdapter>

  @Inject internal lateinit var currencies: Currencies

  private val unitArrayAdapterFactory = MyUnitArrayAdapterFactory()

  @Inject internal lateinit var bus: Bus

  @Inject internal lateinit var activity: AppCompatActivity

  @Inject internal lateinit var savedComparisonManager: SavedComparisonManager

  private lateinit var rowContainer: LinearLayout
  private lateinit var addRowButton: View
  private lateinit var removeRowButton: TextView
  private lateinit var finalEditText: EditText
  private lateinit var finalSpinner: Spinner
  private lateinit var summaryText: TextView
  private lateinit var unitTypeSpinner: Spinner
  private var priceHeader: TextView? = null
  private lateinit var savedChangesStatus: TextView
  private lateinit var savedChangesDivider: View

  private val fileNameEditText = MutableSometimes.create<EditText>()
  private val saveMenuItem = MutableSometimes.create<MenuItem>()
  private val resumed = MutableSometimes.create<Any>()

  private var unitTypeArrayAdapter: UnitTypeArrayAdapter? = null
  private var alertDialog: AlertDialog? = null
  private var actionMode: ActionMode? = null
  private val handler = Handler()
  private var savedChangesCountdown: Int? = null
  private val savedChangesCountdownTick: Runnable = object : Runnable {
    override fun run() {
      val tick = savedChangesCountdown ?: return
      if (!isViewCreated || context == null) {
        return
      }
      savedChangesStatus.text = getString(string.all_changes_saved, tick)
      if (tick >= 1) {
        savedChangesCountdown = tick - 1
        handler.postDelayed(this, SECONDS.toMillis(1))
      } else {
        savedChangesCountdown = null
        savedChangesStatus.visibility = View.GONE
        savedChangesDivider.visibility = View.GONE
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
    val view = inflater.inflate(layout.fragment_main, container, false)
    savedChangesStatus = view.findViewById(R.id.saved_changes_status)
    savedChangesDivider = view.findViewById(R.id.saved_changes_divider)

    priceHeader = view.findViewById<TextView>(R.id.price_header).also { header ->
      header.text = units.currency.symbol
      header.setOnClickListener { currencies.showChangeCurrencyDialog() }
      header.setOnLongClickListener { _ ->
        applyOrderOrShowDialog(
          if (unitEntries.isSortedBy { it.unitEntry.cost }) {
            Order.PRICE_DESCENDING
          } else {
            Order.PRICE_ASCENDING
          }
        )
        true
      }
    }

    view.findViewById<View>(R.id.number_header).let { header ->
      header.setOnLongClickListener {
        applyOrderOrShowDialog(
          if (unitEntries.isSortedBy { it.unitEntry.quantity }) {
            Order.NUMBER_DESCENDING
          } else {
            Order.NUMBER_ASCENDING
          }
        )
        true
      }
    }

    view.findViewById<View>(R.id.size_header).let { header ->
      header.setOnLongClickListener {
        applyOrderOrShowDialog(
          if (unitEntries.isSortedBy { it.unitEntry.size }) {
            Order.SIZE_DESCENDING
          } else {
            Order.SIZE_ASCENDING
          }
        )
        true
      }
    }

    unitTypeSpinner = view.findViewById(R.id.unit_type_spinner)
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
    rowContainer = view.findViewById(R.id.row_container)
    entryViews.forEach { it.setOnUnitEntryChangedListener(this) }
    updateRowNumbers()

    addRowButton = view.findViewById(R.id.add_row_btn)
    addRowButton.setOnClickListener { addRowView() }

    removeRowButton = view.findViewById(R.id.remove_row_btn)
    removeRowButton.setOnClickListener { removeRow(entryViews.size - 1) }

    finalEditText = view.findViewById(R.id.final_size)
    finalEditText.addTextChangedListener(object : AbstractTextWatcher() {
      override fun afterTextChanged(s: Editable) {
        bus.post(compareUnit)
        refreshViews()
      }
    })
    finalEditText.addLocalizedKeyListener()

    finalSpinner = view.findViewById(R.id.final_spinner)
    if (finalSpinner.adapter == null) {
      finalSpinner.adapter = unitArrayAdapterFactory.create(units.getDefaultQuantity().unit)
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
    summaryText = view.findViewById(R.id.final_text_summary)
    addRowView()
    addRowView()
    isViewCreated = true
    return view
  }

  override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
    super.onCreateOptionsMenu(menu, inflater)
    val layoutInflater =
      LayoutInflater.from(ContextThemeWrapper(getActivity(), style.FileNameEditText))
    val editText =
      layoutInflater.inflate(layout.action_bar_edit_text,  /* root= */ null) as EditText
    editText.setOnKeyListener { v: View, keyCode: Int, event: KeyEvent ->
      if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
        val imm =
          getActivity()!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
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

  private fun setUnitType(parent: Spinner?, unitType: UnitType) {
    units.currentUnitType = unitType
    unitTypeArrayAdapter = unitTypeArrayAdapterProvider.get()
    parent!!.adapter = unitTypeArrayAdapter
    finalSpinner.adapter =
      unitArrayAdapterFactory.create(units.getDefaultQuantity(unitType).unit)
    val event = compareUnit
    for (entryView in entryViews) {
      entryView.onCompareUnitChanged(event)
    }
    refreshViews()
  }

  private fun addRowView(): UnitEntryView {
    val entryView = UnitEntryView(context)
    rowContainer.addView(
      entryView,
      LayoutParams(
        LayoutParams.WRAP_CONTENT,
        LayoutParams.WRAP_CONTENT
      )
    )
    val dp16 = TypedValue
      .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, resources.displayMetrics)
    entryView.setPadding(dp16.toInt(), 0, dp16.toInt(), 0)
    entryView.rowNumber = entryViews.size - 1
    entryView.setOnUnitEntryChangedListener(this)
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
    return ComparisonFragmentState(getCurrentState(context), lastKnownSavedState)
  }

  private fun getCurrentState(context: Context?): SavedComparison? {
    if (!isViewCreated || context == null) {
      return null
    }
    if (pendingSavedStateToRestore != null) {
      return pendingSavedStateToRestore!!.currentComparison
    }
    val list = entryViews.map { it.saveState(context) }
    val unitType = UnitType.fromName(
      unitTypeArrayAdapter!!.getItem(unitTypeSpinner.selectedItemPosition),
      context.resources
    )
    val finalSize = finalEditText.text.toString()
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
    if (!isViewCreated || context == null) {
      return
    }
    val fileNameEditText = fileNameEditText.orNull()
    if (fileNameEditText == null) {
      this.fileNameEditText.whenPresent { restoreState(state) }
      return
    }
    lastKnownSavedState = state.lastKnownSavedComparison
    rowContainer.removeAllViewsInLayout()
    val comparison = state.currentComparison ?: return
    setUnitType(unitTypeSpinner, comparison.unitType)
    for (entryRow in comparison.savedUnitEntryRows) {
      val entryView = addRowView()
      entryView.restoreState(entryRow)
    }
    finalEditText.setText(comparison.finalQuantity)
    val unit = comparison.finalUnit
    val adapter = unitArrayAdapterFactory.create(unit)
    finalSpinner.adapter = adapter
    finalSpinner.setSelection(0)
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
    rowContainer.removeAllViewsInLayout()
    addRowView()
    addRowView()
    finalEditText.setText("")
    finalSpinner.adapter = unitArrayAdapterFactory.create(units.getDefaultQuantity().unit)
    summaryText.text = ""
    fileNameEditText.whenPresent { it.setText("") }
    draftKey = System.currentTimeMillis().toString()
    refreshViews()
  }

  fun save() {
    if (!fileNameEditText.orNull()?.text?.toString().isNullOrBlank()) {
      getCurrentState(context)?.let { save(it.withTimestamp(System.currentTimeMillis())) }
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
      AlertDialog.Builder(context!!)
        .setMessage(string.give_name)
        .setView(view)
        .setPositiveButton(android.R.string.ok) { _, _ ->
          val newName = name.text.toString()
          Preconditions.checkState(
            !Strings.isNullOrEmpty(
              newName
            )
          )
          fileNameEditText.whenPresent { editText: EditText ->
            editText.setText(newName)
            save(getCurrentState(context)!!.withTimestamp(System.currentTimeMillis()))
          }
        }

        .setNegativeButton(android.R.string.cancel, null)
        .setOnDismissListener { alertDialog = null }
        .create()
        .also { alertDialog = it }
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
    finalSpinner.adapter =
      unitArrayAdapterFactory.create(units.getDefaultQuantity(event.unitType).unit)
    finishActionMode()
  }

  @Subscribe
  fun onSystemChanged(event: SystemChangedEvent?) {
    finalSpinner.adapter = unitArrayAdapterFactory.create(units.getDefaultQuantity().unit)
    finishActionMode()
  }

  @Subscribe
  fun onNoteChanged(noteChangedEvent: NoteChangedEvent?) {
    refreshViews()
  }

  @Subscribe
  fun onCurrencyChanged(event: CurrencyChangedEvent) {
    priceHeader?.text = event.currency.symbol
    refreshViews()
  }

  override fun onUnitEntryChanged(unitEntry: Optional<UnitEntry>) {
    refreshViews()
    finishActionMode()
  }

  private val compareUnit: CompareUnitChangedEvent
    get() {
      val unit: Unit = (finalSpinner.adapter as UnitArrayAdapter)
        .getUnit(finalSpinner.selectedItemPosition)
      val size = NumberUtils.firstParsableDouble(
        finalEditText.text.toString(),
        units.getDefaultQuantity().amount.toLocalizedString()
      )
      return CompareUnitChangedEvent(size, unit)
    }

  private fun refreshViews() {
    val currentState = getCurrentState(context)
    if (pendingSavedStateToRestore != null || !isViewCreated || currentState == null) {
      return
    }
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
    if (finalEditText.text.toString().isBlank()) {
      finalEditText.hint = units.getDefaultQuantity().amount.toLocalizedString()
    }
    val sortedUnitEntries = unitEntries.sortedBy { it.unitEntry.pricePerBaseUnit }
    if (sortedUnitEntries.size < 2) {
      summaryText.text = ""
      for (entryView in entryViews) {
        entryView.setEvaluation(NEUTRAL)
      }
    } else {
      val finalSummary = SpannableStringBuilder()
      fun setColor(evaluation: Evaluation, start: Int, end: Int) {
        val context = context ?: return
        finalSummary.setSpan(
          ForegroundColorSpan(ContextCompat.getColor(context, evaluation.primaryColor)),
          start,
          end,
          Spannable.SPAN_INCLUSIVE_EXCLUSIVE
        )
      }

      val best = sortedUnitEntries[0]
      finalSummary.append(getString(string.main_final_summary, best.index + 1)).append("\n\n")
      val startBestSpan = finalSummary.length
      appendSingleRowSummary(finalSummary, best, compareUnit, best.unitEntry)
      val endBestSpan = finalSummary.length
      setColor(GOOD, startBestSpan, endBestSpan)

      for (entryWithIndex in sortedUnitEntries) {
        if (entryWithIndex.index == best.index) continue
        val spanStart = finalSummary.length
        appendSingleRowSummary(finalSummary, entryWithIndex, compareUnit, best.unitEntry)
        val spanEnd = finalSummary.length
        val evaluation =
          if (entryWithIndex.unitEntry.pricePerBaseUnit == best.unitEntry.pricePerBaseUnit) {
            GOOD
          } else {
            BAD
          }
        setColor(evaluation, spanStart, spanEnd)
      }
      summaryText.text = finalSummary
      for (entryView in entryViews) {
        val entry = entryView.entry.orNull()
        entryView.setEvaluation(
          when {
            entry == null -> NEUTRAL
            entry.pricePer(size, unit) <= best.unitEntry.pricePer(size, unit) -> GOOD
            else -> BAD
          }
        )
      }
    }
    removeRowButton.isEnabled = entryViews.size > 1
    addRowButton.isEnabled = entryViews.size < 10
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
        getString(string.m_per_s_u, formattedEntryCostString, unitEntry.sizeString, unitEntrySymbol)
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

  private fun swapRows(index1: Int, index2: Int) {
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

  private fun removeRow(index: Int) {
    Preconditions.checkState(entryViews.size > 1)
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

  private fun applyOrderOrShowDialog(newOrder: Order) {
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

  private fun applyOrder(newOrder: Order) {
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

  private fun updateRowNumbers() {
    entryViews.forEachIndexed { index, view -> view.rowNumber = index }
  }

  private val entryViews: List<UnitEntryView>
    get() = rowContainer.children.filterIsInstance<UnitEntryView>()

  private val unitEntries: List<UnitEntryWithIndexAndNote>
    get() {
      return entryViews.mapIndexedNotNull { index, entryView ->
        entryView.entry.orNull()?.let { UnitEntryWithIndexAndNote(index, it, entryView.note) }
      }
    }

  private inner class MyUnitArrayAdapterFactory {
    fun create(selected: DefaultUnit): UnitArrayAdapter {
      return UnitArrayAdapter(
        context,
        UnitArrayAdapter.getSymbolsAndUnits(
          context,
          systems,
          units,
          selected.unitType,
          selected
        )
      )
    }
  }

  private data class UnitEntryWithIndexAndNote(
    val index: Int,
    val unitEntry: UnitEntry,
    val note: String?
  )
}