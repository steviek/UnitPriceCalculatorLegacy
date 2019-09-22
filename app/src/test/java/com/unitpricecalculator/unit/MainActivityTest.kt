package com.unitpricecalculator.unit

import android.os.Build.VERSION_CODES.KITKAT
import android.os.Build.VERSION_CODES.P
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import com.google.common.truth.Truth.assertThat
import com.unitpricecalculator.R
import com.unitpricecalculator.main.MainActivity
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [KITKAT, P])
class MainActivityTest {

  private lateinit var activity: MainActivity
  private lateinit var rowContainer: ViewGroup
  private lateinit var addRowButton: Button
  private lateinit var removeRowButton: Button

  @Before
  fun setUp() {
    activity = Robolectric.setupActivity(MainActivity::class.java)
    rowContainer = activity.findViewById(R.id.row_container)
    addRowButton = activity.findViewById(R.id.add_row_btn)
    removeRowButton = activity.findViewById(R.id.remove_row_btn)
  }

  @Test
  fun init_toolbar_shouldDisplayUntitledHint() {
   val title = activity.supportActionBar!!.customView as EditText
    assertThat(title.text.toString()).isEmpty()
    assertThat(title.hint.toString()).isEqualTo("Untitled")
  }

  @Test
  fun init_shouldContainTwoRowsAndEnabledAddRemoveButton() {
    assertThat(rowContainer.childCount).isEqualTo(2)
    assertThat(addRowButton.isEnabled).isTrue()
    assertThat(removeRowButton.isEnabled).isTrue()
  }

  @Test
  fun clickAddRowButton_shouldShouldContainThreeRows() {
    addRowButton.performClick()

    assertThat(rowContainer.childCount).isEqualTo(3)
  }

  @Test
  fun clickRemoveRowButton_shouldShouldContainThreeRows() {
    removeRowButton.performClick()

    assertThat(rowContainer.childCount).isEqualTo(1)
    assertThat(removeRowButton.isEnabled).isFalse()
  }
}