package com.unitpricecalculator.saved

import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.util.Size
import android.view.ActionMode
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.EditText
import android.widget.PopupMenu
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.squareup.otto.Bus
import com.squareup.otto.Subscribe
import com.unitpricecalculator.BaseFragment
import com.unitpricecalculator.R
import com.unitpricecalculator.comparisons.SavedComparison
import com.unitpricecalculator.comparisons.SavedSortOrder
import com.unitpricecalculator.comparisons.SavedSortOrder.LastModifiedAscending
import com.unitpricecalculator.comparisons.SavedSortOrder.LastModifiedDescending
import com.unitpricecalculator.comparisons.SavedSortOrder.TitleAscending
import com.unitpricecalculator.comparisons.SavedSortOrder.TitleDescending
import com.unitpricecalculator.comparisons.savedSortOrder
import com.unitpricecalculator.databinding.SavedFragmentBinding
import com.unitpricecalculator.events.DataImportedEvent
import com.unitpricecalculator.events.SavedComparisonDeletedEvent
import com.unitpricecalculator.export.ExportManager
import com.unitpricecalculator.export.ImportManager
import com.unitpricecalculator.locale.AppLocaleManager
import com.unitpricecalculator.locale.currentLocale
import com.unitpricecalculator.unit.Units
import com.unitpricecalculator.util.CachingCollator
import com.unitpricecalculator.util.StringBundleKey
import com.unitpricecalculator.util.abstracts.AbstractTextWatcher
import com.unitpricecalculator.util.get
import com.unitpricecalculator.util.prefs.Prefs
import com.unitpricecalculator.util.set
import com.unitpricecalculator.util.setDrawableEnd
import com.unitpricecalculator.util.setDrawableStart
import com.unitpricecalculator.util.stripAccents
import com.unitpricecalculator.view.OnSearchQueryChangedListener
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import javax.inject.Inject
import kotlin.math.roundToInt

@AndroidEntryPoint
class SavedFragment : BaseFragment() {

    @Inject
    lateinit var savedComparisonManager: SavedComparisonManager

    private lateinit var callback: Callback

    @Inject
    lateinit var prefs: Prefs

    @Inject
    lateinit var bus: Bus

    @Inject
    lateinit var units: Units

    @Inject
    lateinit var exportManager: ExportManager

    @Inject
    lateinit var importManager: ImportManager

    @Inject
    lateinit var appLocaleManager: AppLocaleManager

    private var savedComparisons = ArrayList<SavedComparison>()
    private var filteredComparisons = ArrayList<SavedComparison>()
    private var adapter: SavedComparisonsArrayAdapter? = null
    private var filter: (SavedComparison) -> Boolean = { true }

    private var alertDialog: AlertDialog? = null
    private var actionMode: ActionMode? = null
    private var viewBinding: SavedFragmentBinding? = null
    private var storedCollators: StoredCollator? = null
    private var optionsPopupMenu: PopupMenu? = null
    private var queryText: String? = null

    private fun createCallback(selectedPosition: Int, view: View) = object : ActionMode.Callback {
        override fun onCreateActionMode(
            mode: ActionMode,
            menu: Menu
        ): Boolean {
            mode.menuInflater.inflate(R.menu.menu_action, menu)
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
                        val sideMargin =
                            resources.getDimensionPixelOffset(R.dimen.horizontal_margin)
                        nameEditText.inputType = InputType.TYPE_CLASS_TEXT
                        nameEditText.setHint(R.string.enter_name)
                        val name = adapter.getItem(selectedPosition)!!.name
                        nameEditText.setText(name)
                        nameEditText.setSelectAllOnFocus(true)
                        nameEditText.requestFocus()

                        MaterialAlertDialogBuilder(context)
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

                                    refreshDisplayedComparisons()
                                }
                                actionMode?.finish()
                            }
                            .setNegativeButton(android.R.string.cancel) {
                                    _, _ -> actionMode?.finish()
                            }
                            .setOnDismissListener { alertDialog = null }
                            .create()
                            .also { newDialog ->
                                alertDialog = newDialog

                                newDialog.setView(nameEditText, sideMargin, 0, sideMargin, 0)
                                nameEditText.addTextChangedListener(object : AbstractTextWatcher() {
                                    override fun afterTextChanged(s: Editable) {
                                        newDialog.getButton(DialogInterface.BUTTON_POSITIVE)?.isEnabled =
                                            s.isNotBlank()
                                    }
                                })
                            }
                    }
                    if (dialog.isShowing) {
                        dialog.dismiss()
                    } else {
                        dialog.show()
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
        return SavedFragmentBinding.inflate(inflater, container, false)
            .also { viewBinding = it }
            .root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = SavedFragmentBinding.bind(view)

        val savedQuery = queryText ?: savedInstanceState?.get(SearchTextKey)

        val locale = appLocaleManager.currentLocale

        val searchView = binding.searchView
        searchView.query = savedQuery ?: ""
        searchView.queryChangedListener = OnSearchQueryChangedListener { query ->
            actionMode?.finish()

            filter = if (query.isBlank()) {
                { true }
            } else {
                val queryWithoutAccentsInLowerCase =
                    query.stripAccents().lowercase(locale)
                ({
                    queryWithoutAccentsInLowerCase in
                            it.name.stripAccents().lowercase(locale) ||
                            it.savedUnitEntryRows.any { row ->
                                row.note
                                    ?.stripAccents()
                                    ?.lowercase(locale)
                                    ?.contains(queryWithoutAccentsInLowerCase)
                                    ?: false
                            }
                })
            }
            refreshDisplayedComparisons()
        }

        savedComparisons.clear()
        savedComparisons.addAll(savedComparisonManager.savedComparisons)
        val listView = binding.listView
        adapter = SavedComparisonsArrayAdapter(requireContext(), filteredComparisons, units)
        listView.adapter = adapter
        listView.onItemClickListener = OnItemClickListener { _, _, position, _ ->
            actionMode?.let {
                it.finish()
                return@OnItemClickListener
            }
            adapter?.getItem(position)?.let { callback.onLoadSavedComparison(it) }
        }
        listView.onItemLongClickListener =
            OnItemLongClickListener { _, longPressedView, position, _ ->
                actionMode?.let {
                    it.finish()
                    return@OnItemLongClickListener true
                }

                // Start the CAB using the ActionMode.Callback defined above
                longPressedView.isSelected = true
                actionMode =
                    longPressedView.startActionMode(
                        createCallback(position, longPressedView),
                        ActionMode.TYPE_FLOATING
                    )
                true
            }
        refreshDisplayedComparisons()
        activity?.invalidateOptionsMenu()

        binding.titleButton.setOnClickListener {
            actionMode?.finish()
            prefs.savedSortOrder = prefs.savedSortOrder.toggledByTitle()
            syncViews()
        }

        binding.lastModifiedButton.setOnClickListener {
            actionMode?.finish()
            prefs.savedSortOrder = prefs.savedSortOrder.toggledByLastModified()
            syncViews()
        }

        binding.optionsButton.setOnClickListener { button ->
            actionMode?.finish()

            optionsPopupMenu?.let {
                it.dismiss()
                optionsPopupMenu = null
                return@setOnClickListener
            }

            val popupMenu = PopupMenu(button.context, button)
            popupMenu.inflate(R.menu.menu_saved_comparisons)
            popupMenu.menu.findItem(R.id.action_export).isEnabled = savedComparisons.isNotEmpty()
            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_export -> {
                        exportManager.startExport(savedComparisons)
                        true
                    }
                    R.id.action_import -> {
                        importManager.startImport()
                        true
                    }
                    else -> false
                }
            }
            popupMenu.setOnDismissListener { optionsPopupMenu = null }
            popupMenu.show()
            optionsPopupMenu = popupMenu
        }

        syncViews()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        viewBinding?.apply {
            queryText = searchView.query.toString()
        }
        outState[SearchTextKey] = queryText
    }

    override fun onDestroyView() {
        super.onDestroyView()
        queryText = viewBinding?.searchView?.query?.toString()
        viewBinding = null
    }

    override fun onStart() {
        super.onStart()
        savedComparisons.clear()
        savedComparisons.addAll(savedComparisonManager.savedComparisons)
        refreshDisplayedComparisons()
        activity?.invalidateOptionsMenu()

        bus.register(this)
    }

    override fun onStop() {
        super.onStop()
        bus.unregister(this)
        actionMode?.finish()
        actionMode = null
    }

    @Subscribe
    fun onDataImported(event: DataImportedEvent) {
        savedComparisons.clear()
        savedComparisons.addAll(savedComparisonManager.savedComparisons)
        refreshDisplayedComparisons()
    }

    private fun syncViews() = viewBinding?.apply {
        val dp24 = (resources.displayMetrics.density * 24).roundToInt()
        val size = Size(dp24, dp24)
        when (prefs.savedSortOrder) {
            LastModifiedDescending -> {
                titleButton.setDrawableEnd(R.drawable.empty_24, size)
                lastModifiedButton.setDrawableStart(R.drawable.ic_arrow_down_24, size)
            }
            LastModifiedAscending -> {
                titleButton.setDrawableEnd(R.drawable.empty_24, size)
                lastModifiedButton.setDrawableStart(R.drawable.ic_arrow_up_24, size)
            }
            TitleDescending -> {
                lastModifiedButton.setDrawableStart(R.drawable.empty_24, size)
                titleButton.setDrawableEnd(R.drawable.ic_arrow_down_24, size)
            }
            TitleAscending -> {
                lastModifiedButton.setDrawableStart(R.drawable.empty_24, size)
                titleButton.setDrawableEnd(R.drawable.ic_arrow_up_24, size)
            }
        }
        refreshDisplayedComparisons()
    }

    private fun refreshDisplayedComparisons() {
        filteredComparisons.clear()
        filteredComparisons.addAll(
            savedComparisons.filter(filter).sortedWith(getOrCreateComparator(prefs.savedSortOrder))
        )
        adapter?.notifyDataSetChanged()
    }

    private fun getOrCreateComparator(order: SavedSortOrder): Comparator<SavedComparison> {
        return when (order) {
            LastModifiedDescending -> compareByTimestamp(ascending = false)
            LastModifiedAscending -> compareByTimestamp(ascending = true)
            TitleDescending -> compareByName(ascending = false)
            TitleAscending -> compareByName(ascending = true)
        }
    }

    private fun compareByTimestamp(ascending: Boolean) = Comparator<SavedComparison> { o1, o2 ->
        val o1Millis = o1.timestampMillis
        val o2Millis = o2.timestampMillis
        when {
            o1Millis == null && o2Millis == null -> 0
            o1Millis == null -> 1
            o2Millis == null -> -1
            ascending -> o1Millis.compareTo(o2Millis)
            else -> o2Millis.compareTo(o1Millis)
        }
    }

    private fun compareByName(ascending: Boolean): Comparator<SavedComparison> {
        val locale = appLocaleManager.current.toLocale()
        var storedCollators = storedCollators?.takeIf { it.locale == locale }
        if (storedCollators == null) {
            storedCollators = StoredCollator(locale)
            this.storedCollators = storedCollators
        }

        return Comparator { o1, o2 ->
            storedCollators.collator.compare(o1, o2) * (if (ascending) 1 else -1)
        }
    }
    private class StoredCollator(val locale: Locale) {
        val collator = CachingCollator<SavedComparison>(locale) { it.name }
    }

    interface Callback {
        fun onLoadSavedComparison(comparison: SavedComparison)
    }

    private companion object {
        val SearchTextKey = StringBundleKey("key-search-text")
    }
}