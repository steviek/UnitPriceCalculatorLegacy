package com.unitpricecalculator.util

import android.os.Bundle
import android.os.Parcelable
import kotlin.reflect.KClass

data class ParcelableBundleKey<P : Parcelable>(val key: String, val clazz: KClass<P>)

operator fun <P : Parcelable> Bundle.get(key: ParcelableBundleKey<P>): P? {
    return getParcelable(key.key) as P?
}

operator fun <P : Parcelable> Bundle.set(key: ParcelableBundleKey<P>, value: P?) {
    putParcelable(key.key, value)
}

data class StringBundleKey(val key: String)

operator fun Bundle.get(key: StringBundleKey): String? = getString(key.key)

operator fun Bundle.set(key: StringBundleKey, value: String?) = putString(key.key, value)
