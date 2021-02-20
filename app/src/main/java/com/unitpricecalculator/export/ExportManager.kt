package com.unitpricecalculator.export

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.util.Base64
import android.util.Log
import android.widget.Toast
import com.google.common.io.Files
import com.unitpricecalculator.R
import com.unitpricecalculator.comparisons.SavedComparison
import com.unitpricecalculator.inject.ActivityScoped
import com.unitpricecalculator.json.ObjectMapper
import com.unitpricecalculator.util.RequestCodes
import com.unitpricecalculator.util.logger.Logger
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@ActivityScoped
class ExportManager @Inject constructor(
  private val activity: Activity,
  private val objectMapper: ObjectMapper
  ) {

  private var pendingSavedComparisons: List<SavedComparison>? = null

  fun startExport(savedComparisons: List<SavedComparison>) {
    pendingSavedComparisons = savedComparisons
    val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
      addCategory(Intent.CATEGORY_OPENABLE)
      type = "text/plain"
      putExtra(Intent.EXTRA_TITLE, "saved_comparisons.upc")
    }
    activity.startActivityForResult(intent, RequestCodes.RC_CREATE_FILE)
  }

  fun handleActivityResult(data: Intent?) {
    val uri = data?.data ?: return
    val savedComparisons = pendingSavedComparisons ?: return

    val pfd =
      try {
        activity.contentResolver.openFileDescriptor(uri, "w")
      } catch (e: Throwable) {
        Logger.e(e)
        null
      }
    if (pfd == null) {
      Toast.makeText(activity, R.string.error_opening_file, Toast.LENGTH_SHORT).show()
      return
    }

    val outputStream = FileOutputStream(pfd.fileDescriptor)
    val content = objectMapper.toJson(SavedData(savedComparisons))
    try {
      outputStream.write(content.toByteArray())
      Toast.makeText(activity, R.string.saved_successfully, Toast.LENGTH_SHORT).show()
      pendingSavedComparisons = null
    } finally {
      outputStream.close()
      pfd.close()
    }
  }
}