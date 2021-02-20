package com.unitpricecalculator.export

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import com.google.common.io.Files
import com.squareup.otto.Bus
import com.unitpricecalculator.R
import com.unitpricecalculator.events.DataImportedEvent
import com.unitpricecalculator.json.ObjectMapper
import com.unitpricecalculator.saved.SavedComparisonManager
import com.unitpricecalculator.util.RequestCodes
import com.unitpricecalculator.util.logger.Logger
import java.io.*
import javax.inject.Inject

class ImportManager @Inject constructor(
  private val activity: Activity,
  private val objectMapper: ObjectMapper,
  private val savedComparisonManager: SavedComparisonManager,
  private val bus: Bus
) {

  fun startImport() {
    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
      .addCategory(Intent.CATEGORY_OPENABLE)
      .setType("text/plain")
    activity.startActivityForResult(intent, RequestCodes.RC_OPEN_FILE)
  }

  fun handleActivityResult(data: Intent?) {
    val uri = data?.data ?: return

    val pfd =
      try {
        activity.contentResolver.openFileDescriptor(uri, "r")
      } catch (e: Throwable) {
        Logger.e(e)
        null
      }
    if (pfd == null) {
      Toast.makeText(activity, R.string.error_opening_file, Toast.LENGTH_SHORT).show()
      return
    }

    val stringBuilder = StringBuilder()
    val inputStream = FileInputStream(pfd.fileDescriptor)
    BufferedReader(InputStreamReader(inputStream)).useLines { lines ->
      lines.forEach { stringBuilder.append(it) }
    }
    inputStream.close()

    val content = stringBuilder.toString()
    val savedData = objectMapper.fromJson(SavedData::class.java, content)
    savedData.savedComparisons.forEach {
      savedComparisonManager.putSavedComparison(it)
    }
    bus.post(DataImportedEvent)
  }
}