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
