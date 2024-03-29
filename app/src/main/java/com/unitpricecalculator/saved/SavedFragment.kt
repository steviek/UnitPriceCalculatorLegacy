package com.unitpricecalculator.saved

import android.content.DialogInterface
import android.graphics.Color
import android.os.Build.VERSION
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.view.ActionMode
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.EditText
import android.widget.ListView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.core.content.ContextCompat
import com.squareup.otto.Bus
import com.squareup.otto.Subscribe
import com.unitpricecalculator.BaseFragment
import com.unitpricecalculator.R
import com.unitpricecalculator.comparisons.SavedComparison
import com.unitpricecalculator.events.DataImportedEvent
import com.unitpricecalculator.events.SavedComparisonDeletedEvent
import com.unitpricecalculator.export.ExportManager
import com.unitpricecalculator.export.ImportManager
import com.unitpricecalculator.unit.Units
import com.unitpricecalculator.util.abstracts.AbstractTextWatcher
import com.unitpricecalculator.util.materialize
import com.unitpricecalculator.util.stripAccents
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class SavedFragment : BaseFragment() {

  @Inject lateinit var savedComparisonManager: SavedComparisonManager

  private lateinit var callback: Callback

  @Inject lateinit var bus: Bus

  @Inject lateinit var units: Units

  @Inject lateinit var exportManager: ExportManager

  @Inject lateinit var importManager: ImportManager

  private var savedComparisons = ArrayList<SavedComparison>()
  private var filteredComparisons = ArrayList<SavedComparison>()
  private var adapter: SavedComparisonsArrayAdapter? = null
  private var filter: (SavedComparison) -> Boolean = { true }

  private var alertDialog: AlertDialog? = null
  private var actionMode: ActionMode? = null

  private fun createCallback(selectedPosition: Int, view: View) = object : ActionMode.Callback {
    override fun onCreateActionMode(
      mode: ActionMode,
      menu: Menu
    ): Boolean {
      mode.menuInflater.inflate(R.menu.menu_action, menu)
      if (VERSION.SDK_INT >= 21) activity?.window?.statusBarColor = Color.BLACK
      return true
    }

    override fun onPrepareActionMode(mode: ActionMode, menu: Menu) = false

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
      return when (item.itemId) {
        R.id.action_rename -> {
          val context = context ?: return false
          val adapter = adapter ?: return false
          val dialog = alertDialog ?: run {
            val nameEditText = EditText(context)
            val sideMargin = resources.getDimensionPixelOffset(R.dimen.horizontal_margin)
            nameEditText.inputType = InputType.TYPE_CLASS_TEXT
            nameEditText.setHint(R.string.enter_name)
            val name = adapter.getItem(selectedPosition)!!.name
            nameEditText.setText(name)
            nameEditText.setSelectAllOnFocus(true)
            nameEditText.requestFocus()

            AlertDialog.Builder(context)
              .setMessage(R.string.give_name)
              .setPositiveButton(
                android.R.string.ok
              ) { _, _ ->
                val savedName = nameEditText.text.toString()
                if (savedName.isNotBlank()) {
                  val old = adapter.getItem(selectedPosition)!!
                  val renamed = old.rename(savedName)
                  savedComparisonManager.putSavedComparison(renamed)
                  savedComparisons.remove(old)
                  savedComparisons.add(selectedPosition, renamed)

                  refreshFilteredComparisons()
                }
                actionMode?.finish()
              }
              .setNegativeButton(android.R.string.cancel) { _, _ -> actionMode?.finish() }
              .setOnDismissListener { alertDialog = null }
              .create()
              .also { newDialog ->
                alertDialog = newDialog

                newDialog.setView(nameEditText, sideMargin, 0, sideMargin, 0)
                nameEditText.addTextChangedListener(object : AbstractTextWatcher() {
                  override fun afterTextChanged(s: Editable) {
                    newDialog.getButton(DialogInterface.BUTTON_POSITIVE)?.isEnabled = s.isNotBlank()
                  }
                })
              }
          }
          if (dialog.isShowing) {
            dialog.dismiss()
          } else {
            dialog.show()
            dialog.materialize()
          }
          true
        }
        R.id.action_delete -> {
          val adapter = adapter ?: return false
          val comparison = adapter.getItem(selectedPosition)!!
          savedComparisons.remove(comparison)
          filteredComparisons.remove(comparison)
          savedComparisonManager.removeSavedComparison(comparison)
          bus.post(SavedComparisonDeletedEvent(comparison.key))
          adapter.notifyDataSetChanged()
          mode.finish()
          true
        }
        else -> false
      }
    }

    override fun onDestroyActionMode(mode: ActionMode) {
      actionMode = null
      view.isSelected = false
      if (VERSION.SDK_INT >= 21) {
        activity?.let {
          it.window?.statusBarColor = ContextCompat.getColor(it, R.color.colorPrimaryDark)
        }
      }
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    callback = requireActivity() as Callback
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    setHasOptionsMenu(true)
    return inflater.inflate(R.layout.fragment_saved, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    savedComparisons.clear()
    savedComparisons.addAll(savedComparisonManager.savedComparisons)
    val listView = view.findViewById<ListView>(R.id.list_view)
    adapter = SavedComparisonsArrayAdapter(requireContext(), filteredComparisons, units)
    listView.adapter = adapter
    listView.onItemClickListener = OnItemClickListener { _, _, position, _ ->
      adapter?.getItem(position)?.let { callback.onLoadSavedComparison(it) }
    }
    listView.onItemLongClickListener =
      OnItemLongClickListener { _, longPressedView, position, _ ->
        if (actionMode != null) return@OnItemLongClickListener false

        // Start the CAB using the ActionMode.Callback defined above
        view.isSelected = true
        actionMode = requireActivity().startActionMode(createCallback(position, longPressedView))
        true
      }
    refreshFilteredComparisons()
    activity?.invalidateOptionsMenu()
  }

  override fun onStart() {
    super.onStart()
    savedComparisons.clear()
    savedComparisons.addAll(savedComparisonManager.savedComparisons)
    refreshFilteredComparisons()
    activity?.invalidateOptionsMenu()

    bus.register(this)
  }

  override fun onStop() {
    super.onStop()
    bus.unregister(this)
  }

  override fun onPrepareOptionsMenu(menu: Menu) {
    super.onPrepareOptionsMenu(menu)
    val activity = baseActivity ?: return
    val actionBar = activity.supportActionBar ?: return
    activity.menuInflater.inflate(R.menu.menu_saved_comparisons, menu)

    actionBar.customView = null
    actionBar.setDisplayShowCustomEnabled(false)
    activity.setTitle(R.string.saved_comparisons)
    val searchView = menu.findItem(R.id.action_search).actionView as SearchView
    searchView.setOnQueryTextListener(object : OnQueryTextListener {
      override fun onQueryTextSubmit(query: String): Boolean {
        return true
      }

      override fun onQueryTextChange(newText: String): Boolean {
        filter = if (newText.isBlank()) {
          { true }
        } else {
          val queryWithoutAccentsInLowerCase =
            newText.stripAccents().toLowerCase(Locale.getDefault())
          ({
            queryWithoutAccentsInLowerCase in
              it.name.stripAccents().toLowerCase(Locale.getDefault()) ||
              it.savedUnitEntryRows.any { row ->
                row.note
                  ?.stripAccents()
                  ?.toLowerCase(Locale.getDefault())
                  ?.contains(queryWithoutAccentsInLowerCase)
                  ?: false
              }
          })
        }
        refreshFilteredComparisons()
        return true
      }
    })

    menu.findItem(R.id.action_export).isEnabled = savedComparisons.isNotEmpty()
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return when (item.itemId) {
      R.id.action_export -> {
        exportManager.startExport(savedComparisons)
        true
      }
      R.id.action_import -> {
        importManager.startImport()
        true
      }
      else -> super.onOptionsItemSelected(item)
    }
  }

  private fun refreshFilteredComparisons() {
    filteredComparisons.clear()
    filteredComparisons.addAll(savedComparisons.filter(filter).sortedDescending())
    adapter?.notifyDataSetChanged()
  }

  @Subscribe
  fun onDataImported(event: DataImportedEvent) {
    savedComparisons.clear()
    savedComparisons.addAll(savedComparisonManager.savedComparisons)
    refreshFilteredComparisons()
    activity?.invalidateOptionsMenu()
  }

  interface Callback {
    fun onLoadSavedComparison(comparison: SavedComparison)
  }
}