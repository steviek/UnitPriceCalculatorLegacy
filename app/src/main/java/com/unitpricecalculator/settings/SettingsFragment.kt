package com.unitpricecalculator.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import com.squareup.otto.Bus
import com.squareup.otto.Subscribe
import com.unitpricecalculator.BaseFragment
import com.unitpricecalculator.R
import com.unitpricecalculator.currency.Currencies
import com.unitpricecalculator.events.CurrencyChangedEvent
import com.unitpricecalculator.events.SystemChangedEvent
import com.unitpricecalculator.mode.DarkModeDialogFragment
import com.unitpricecalculator.mode.DarkModeManager
import com.unitpricecalculator.mode.DarkModeStateChangedEvent
import com.unitpricecalculator.unit.DefaultQuantityDialogFragment
import com.unitpricecalculator.unit.Systems
import com.unitpricecalculator.unit.UnitType
import com.unitpricecalculator.unit.Units
import com.unitpricecalculator.unit.Units.DefaultQuantityChangedEvent
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

  private val changeCurrencySubtitle = MutableSometimes.create<TextView>()
  private val darkModeSubtitle = MutableSometimes.create<TextView>()
  private lateinit var defaultQuantityViews: Map<UnitType, DefaultQuantityRowView>

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

    val changeCurrencySubtitleTextView = view.findViewById<TextView>(R.id.change_currency_subtitle)
    changeCurrencySubtitle.set(changeCurrencySubtitleTextView)
    changeCurrencySubtitleTextView.text = units.currency.symbol
    view.findViewById<View>(R.id.change_currency_parent)
      .setOnClickListener { currencies.showChangeCurrencyDialog() }

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

    val darkModeSubtitleTextView = view.findViewById<TextView>(R.id.dark_mode_subtitle)
    darkModeSubtitle.set(darkModeSubtitleTextView)
    darkModeSubtitleTextView.setText(darkModeManager.currentDarkModeState.labelResId)
    view.findViewById<View>(R.id.dark_mode_parent)
      .setOnClickListener { DarkModeDialogFragment.show(childFragmentManager) }

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
    this.defaultQuantityViews = defaultQuantityRowViews

    return view
  }

  @Subscribe
  fun onCurrencyChangedEvent(event: CurrencyChangedEvent) {
    changeCurrencySubtitle.whenPresent { it.text = event.currency.symbol }
  }

  @Subscribe
  fun onDarkModeStateChanged(event: DarkModeStateChangedEvent) {
    darkModeSubtitle.whenPresent { it.setText(event.newState.labelResId) }
  }

  @Subscribe
  fun onSystemsChanged(event: SystemChangedEvent) {
    defaultQuantityViews.entries.forEach { it.value.setData(units.getDefaultQuantity(it.key)) }
  }

  @Subscribe
  fun onDefaultQuantityChanged(event: DefaultQuantityChangedEvent) {
    defaultQuantityViews.entries.forEach { it.value.setData(units.getDefaultQuantity(it.key)) }
  }
}