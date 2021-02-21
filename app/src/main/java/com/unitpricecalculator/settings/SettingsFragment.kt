package com.unitpricecalculator.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.TextView
import com.squareup.otto.Bus
import com.squareup.otto.Subscribe
import com.unitpricecalculator.BaseFragment
import com.unitpricecalculator.R
import com.unitpricecalculator.currency.Currencies
import com.unitpricecalculator.events.CurrencyChangedEvent
import com.unitpricecalculator.events.SystemChangedEvent
import com.unitpricecalculator.initialscreen.InitialScreenChangedEvent
import com.unitpricecalculator.initialscreen.InitialScreenManager
import com.unitpricecalculator.locale.AppLocaleManager
import com.unitpricecalculator.mode.DarkModeDialogFragment
import com.unitpricecalculator.mode.DarkModeManager
import com.unitpricecalculator.mode.DarkModeStateChangedEvent
import com.unitpricecalculator.unit.DefaultQuantityDialogFragment
import com.unitpricecalculator.unit.Systems
import com.unitpricecalculator.unit.UnitType
import com.unitpricecalculator.unit.Units
import com.unitpricecalculator.unit.Units.DefaultQuantityChangedEvent
import com.unitpricecalculator.util.prefs.Keys
import com.unitpricecalculator.util.prefs.Prefs
import com.unitpricecalculator.util.sometimes.MutableSometimes
import com.unitpricecalculator.view.DragLinearLayout
import java.util.HashSet
import javax.inject.Inject

class SettingsFragment : BaseFragment() {

  @Inject internal lateinit var units: Units
  @Inject internal lateinit var systems: Systems
  @Inject internal lateinit var currencies: Currencies
  @Inject internal lateinit var bus: Bus
  @Inject internal lateinit var darkModeManager: DarkModeManager
  @Inject internal lateinit var initialScreenManager: InitialScreenManager
  @Inject internal lateinit var prefs: Prefs
  @Inject internal lateinit var localeManager: AppLocaleManager

  private val changeCurrency = MutableSometimes.create<SettingsItemView>()
  private val darkMode = MutableSometimes.create<SettingsItemView>()
  private val initialScreen = MutableSometimes.create<SettingsItemView>()
  private val defaultQuantityViews =
    MutableSometimes.create<Map<UnitType, DefaultQuantityRowView>>()
  private val percentageToggle = MutableSometimes.create<CompoundButton>()
  private val language = MutableSometimes.create<SettingsItemView>()

  override fun onStart() {
    super.onStart()
    bus.register(this)
  }

  override fun onStop() {
    super.onStop()
    bus.unregister(this)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    val view = inflater.inflate(R.layout.fragment_settings, container, false)

    view.findViewById<SettingsItemView>(R.id.change_currency)?.let {
      it.setOnClickListener { currencies.showChangeCurrencyDialog() }
      it.subtitle = units.currency.symbol
      changeCurrency.set(it)
    }

    view.findViewById<SettingsItemView>(R.id.initial_screen)?.let {
      it.setOnClickListener { initialScreenManager.showDialog(childFragmentManager) }
      it.setSubtitle(initialScreenManager.initialScreen.labelResId)
      initialScreen.set(it)
    }

    view.findViewById<SettingsItemView>(R.id.language)?.let {
      it.setOnClickListener { localeManager.showSelectionDialog(childFragmentManager) }
      it.subtitle = localeManager.current.getDisplayName(requireContext())
      language.set(it)
    }

    val dragLinearLayout = view.findViewById<DragLinearLayout>(R.id.drag_linear_layout)

    val includedSystems = HashSet(systems.includedSystems)
    for (system in systems.preferredOrder) {
      val rowView = inflater.inflate(R.layout.list_draggable, dragLinearLayout, false)
      val text = rowView.findViewById<TextView>(R.id.text)
      text.text = resources.getString(system.getName())
      rowView.tag = system
      dragLinearLayout.addDragView(rowView, rowView.findViewById(R.id.handler))
      val checkBox = rowView.findViewById<CheckBox>(R.id.checkbox)
      checkBox.isChecked = includedSystems.contains(system)
      text.setOnClickListener { checkBox.toggle() }
      checkBox.setOnCheckedChangeListener { compoundButton, isChecked ->
        val modificationRequired = isChecked != includedSystems.contains(system)
        if (!modificationRequired) {
          return@setOnCheckedChangeListener
        }

        if (includedSystems.size == 1 && !isChecked) {
          // If this is the last unit, don't allow it to be unchecked.
          compoundButton.toggle()
          return@setOnCheckedChangeListener
        }

        if (isChecked) {
          includedSystems.add(system)
        } else {
          includedSystems.remove(system)
        }
        systems.includedSystems = includedSystems
      }
    }

    dragLinearLayout.setOnViewSwapListener { _, firstPosition, _, secondPosition ->
      val order = systems.preferredOrder
      val temp = order[firstPosition]
      order[firstPosition] = order[secondPosition]
      order[secondPosition] = temp
      systems.preferredOrder = order
    }

    view.findViewById<SettingsItemView>(R.id.dark_mode).let {
      it.setOnClickListener { DarkModeDialogFragment.show(childFragmentManager) }
      it.setSubtitle(darkModeManager.currentDarkModeState.labelResId)
      darkMode.set(it)
    }

    val defaultUnitQuantityContainer =
      view.findViewById<ViewGroup>(R.id.default_unit_quantity_parent)
    val defaultQuantityRowViews = mutableMapOf<UnitType, DefaultQuantityRowView>()
    UnitType.values().forEach { unitType ->
      val rowView =
        layoutInflater.inflate(
          R.layout.view_default_quantity_row,
          defaultUnitQuantityContainer,
          false
        ) as DefaultQuantityRowView
      rowView.setData(units.getDefaultQuantity(unitType))
      rowView.setOnClickListener {
        DefaultQuantityDialogFragment.show(childFragmentManager, units.getDefaultQuantity(unitType))
      }
      defaultUnitQuantityContainer.addView(rowView)
      defaultQuantityRowViews[unitType] = rowView
    }
    defaultQuantityViews.set(defaultQuantityRowViews)

    view.findViewById<CompoundButton>(R.id.percentages).let {
      it.isChecked = prefs.getBoolean(Keys.SHOW_PERCENTAGE)
      it.setOnCheckedChangeListener { _, isChecked ->
        prefs.putBoolean(Keys.SHOW_PERCENTAGE, isChecked)
      }
      percentageToggle.set(it)
    }

    return view
  }

  override fun onDestroyView() {
    super.onDestroyView()
    changeCurrency.set(null)
    darkMode.set(null)
    initialScreen.set(null)
    defaultQuantityViews.set(null)
  }

  @Subscribe
  fun onCurrencyChangedEvent(event: CurrencyChangedEvent) {
    changeCurrency.whenPresent { it.subtitle = event.currency.symbol }
  }

  @Subscribe
  fun onDarkModeStateChanged(event: DarkModeStateChangedEvent) {
    darkMode.whenPresent { it.setSubtitle(event.newState.labelResId) }
  }

  @Subscribe
  fun onInitialScreenChanged(event: InitialScreenChangedEvent) {
    initialScreen.whenPresent { it.setSubtitle(event.newInitialScreen.labelResId) }
  }

  @Subscribe
  fun onSystemsChanged(event: SystemChangedEvent) {
    defaultQuantityViews.whenPresent {
      it.entries.forEach { (unit, row) -> row.setData(units.getDefaultQuantity(unit)) }
    }
  }

  @Subscribe
  fun onDefaultQuantityChanged(event: DefaultQuantityChangedEvent) {
    defaultQuantityViews.whenPresent {
      it.entries.forEach { (unit, row) -> row.setData(units.getDefaultQuantity(unit)) }
    }
  }
}
