package com.unitpricecalculator.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.play.core.review.ReviewManagerFactory
import com.squareup.otto.Bus
import com.squareup.otto.Subscribe
import com.unitpricecalculator.BaseFragment
import com.unitpricecalculator.R
import com.unitpricecalculator.currency.Currencies
import com.unitpricecalculator.databinding.FragmentSettingsBinding
import com.unitpricecalculator.dialog.DelegatingDialogFragment
import com.unitpricecalculator.dialog.DialogId
import com.unitpricecalculator.events.CurrencyChangedEvent
import com.unitpricecalculator.events.SystemChangedEvent
import com.unitpricecalculator.initialscreen.InitialScreenChangedEvent
import com.unitpricecalculator.initialscreen.InitialScreenManager
import com.unitpricecalculator.locale.AppLocaleManager
import com.unitpricecalculator.mode.DarkModeDialogFragment
import com.unitpricecalculator.mode.DarkModeManager
import com.unitpricecalculator.mode.DarkModeStateChangedEvent
import com.unitpricecalculator.unit.Systems
import com.unitpricecalculator.unit.UnitFormatter
import com.unitpricecalculator.unit.UnitType
import com.unitpricecalculator.unit.Units
import com.unitpricecalculator.unit.Units.DefaultQuantityChangedEvent
import com.unitpricecalculator.util.prefs.Prefs
import com.unitpricecalculator.util.prefs.PrefsKeys.ShowPercentage
import com.unitpricecalculator.util.sometimes.MutableSometimes
import com.unitpricecalculator.util.toLocalizedString
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : BaseFragment() {

    @Inject
    internal lateinit var units: Units
    @Inject
    internal lateinit var systems: Systems
    @Inject
    internal lateinit var currencies: Currencies
    @Inject
    internal lateinit var bus: Bus
    @Inject
    internal lateinit var darkModeManager: DarkModeManager
    @Inject
    internal lateinit var initialScreenManager: InitialScreenManager
    @Inject
    internal lateinit var prefs: Prefs
    @Inject
    internal lateinit var localeManager: AppLocaleManager
    @Inject
    internal lateinit var unitFormatter: UnitFormatter

    private val binding = MutableSometimes.create<FragmentSettingsBinding>()

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
        val binding = FragmentSettingsBinding.inflate(layoutInflater, container, false)
        this.binding.set(binding)

        with(binding) {
            currencyRow.setOnClickListener { currencies.showChangeCurrencyDialog() }
            initialScreen.setOnClickListener {
                initialScreenManager.showDialog(childFragmentManager)
            }
            language.setOnClickListener {
                localeManager.showSelectionDialog(childFragmentManager)
            }
            darkMode.setOnClickListener { DarkModeDialogFragment.show(childFragmentManager) }
            percentages.isChecked = prefs[ShowPercentage] == true
            percentages.setOnCheckedChangeListener { _, isChecked ->
                prefs[ShowPercentage] = isChecked
            }

            unitSystems.setOnClickListener {
                DelegatingDialogFragment.show(childFragmentManager, DialogId.UNIT_SYSTEMS)
            }

            defaultUnitQuantitiesRow.setOnClickListener {
                DelegatingDialogFragment.show(childFragmentManager, DialogId.ALL_DEFAULT_QUANTITIES)
            }

            rateButton.setOnClickListener { view ->
                val manager = ReviewManagerFactory.create(view.context)
                val request = manager.requestReviewFlow()
                request.addOnCompleteListener { task ->
                    val activity = activity ?: return@addOnCompleteListener

                    if (!task.isSuccessful) {
                        launchPlayStore()
                        return@addOnCompleteListener
                    }

                    val result = task.result
                    if (result == null) {
                        launchPlayStore()
                        return@addOnCompleteListener
                    }

                    manager.launchReviewFlow(activity, result)
                }
            }

            feedbackButton.setOnClickListener {
                val activity = activity ?: return@setOnClickListener
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "message/rfc822"
                intent.putExtra(Intent.EXTRA_EMAIL, "sixbynineapps@gmail.com")
                intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.feedback_subject))
                try {
                    activity.startActivity(
                        Intent.createChooser(intent, getString(R.string.send_email))
                    )
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(activity, R.string.no_email_client, Toast.LENGTH_SHORT).show()
                }
            }

            buyCoffeeButton.setOnClickListener {
                val activity = activity ?: return@setOnClickListener
                val coffeeIntent = Intent(Intent.ACTION_VIEW)
                    .setData(Uri.parse("https://www.buymeacoffee.com/kideckel"))
                activity.startActivity(coffeeIntent)
            }
        }

        syncViews()
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.set(null)
    }

    @Subscribe
    fun onCurrencyChangedEvent(event: CurrencyChangedEvent) {
        syncViews()
    }

    @Subscribe
    fun onDarkModeStateChanged(event: DarkModeStateChangedEvent) {
        syncViews()
    }

    @Subscribe
    fun onInitialScreenChanged(event: InitialScreenChangedEvent) {
        syncViews()
    }

    @Subscribe
    fun onSystemsChanged(event: SystemChangedEvent) {
        syncViews()
    }

    @Subscribe
    fun onDefaultQuantityChanged(event: DefaultQuantityChangedEvent) {
        /*defaultQuantityViews.whenPresent {
          it.entries.forEach { (unit, row) -> row.setData(units.getDefaultQuantity(unit)) }
        }*/
        syncViews()
    }

    private fun syncViews() = binding.orNull()?.apply {
        darkMode.setSubtitle(darkModeManager.currentDarkModeState.labelResId)
        currencyRow.subtitle = units.currency.symbol
        initialScreen.setSubtitle(initialScreenManager.initialScreen.labelResId)
        language.subtitle = localeManager.current.getDisplayName(root.context)

        val includedSystems = systems.includedSystems
        unitSystems.subtitle =
            systems
                .preferredOrder
                .filter { it in includedSystems }
                .joinToString { getString(it.getName()) }

        defaultUnitQuantitiesRow.subtitle =
            UnitType.values().joinToString(separator = ",\t") { unitType ->
                val quantity = units.getDefaultQuantity(unitType)
                val unitTypeName = getString(quantity.unit.unitType.labelResId)
                val quantityAmount = quantity.amount.toLocalizedString()
                getString(
                    R.string.default_unit_quantity_summary_line,
                    unitTypeName,
                    unitFormatter.format(quantity.unit, quantity.amount, quantityAmount)
                )
            }
    }

    private fun launchPlayStore() {
        val activity = activity ?: return
        val i = Intent(
            Intent.ACTION_VIEW,
            Uri.parse(
                "https://play.google.com/store/apps/details?id=" +
                        "com.unitpricecalculator"
            )
        )
        activity.startActivity(i)
    }
}
