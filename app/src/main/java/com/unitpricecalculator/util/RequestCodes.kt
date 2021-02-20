package com.unitpricecalculator.util

/** Store of request codes so I don't accidentally reuse them. */
object RequestCodes {
  /** Request code to write to a file location using storage access framework. */
  const val RC_CREATE_FILE = 1
  /** Request code to read from a file location using storage access framework. */
  const val RC_OPEN_FILE = 2
}