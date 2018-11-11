/*
 * Licensed under Apache-2.0
 *
 * Designed and developed by Aidan Follestad (@afollestad)
 */
package com.afollestad.rxkprefs

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.VisibleForTesting
import com.afollestad.rxkprefs.adapters.BooleanAdapter
import com.afollestad.rxkprefs.adapters.FloatAdapter
import com.afollestad.rxkprefs.adapters.IntAdapter
import com.afollestad.rxkprefs.adapters.LongAdapter
import com.afollestad.rxkprefs.adapters.StringAdapter
import com.afollestad.rxkprefs.adapters.StringSet
import com.afollestad.rxkprefs.adapters.StringSetAdapter
import io.reactivex.Observable

internal class RealRxkPrefs(
  context: Context,
  key: String,
  mode: Int = Context.MODE_PRIVATE
) : RxkPrefs {
  private val prefs =
    context.getSharedPreferences(key, mode) ?: dumpsterFire()

  @VisibleForTesting
  internal val onKeyChange = Observable.create<String> { emitter ->
    val changeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
      emitter.onNext(key)
    }
    emitter.setCancellable {
      prefs.unregisterOnSharedPreferenceChangeListener(changeListener)
    }
    prefs.registerOnSharedPreferenceChangeListener(changeListener)
  }
      .share() ?: dumpsterFire()

  override fun boolean(
    key: String,
    defaultValue: Boolean
  ): Pref<Boolean> = RealPref(prefs, key, defaultValue, onKeyChange, BooleanAdapter.INSTANCE)

  override fun float(
    key: String,
    defaultValue: Float
  ): Pref<Float> = RealPref(prefs, key, defaultValue, onKeyChange, FloatAdapter.INSTANCE)

  override fun integer(
    key: String,
    defaultValue: Int
  ): Pref<Int> = RealPref(prefs, key, defaultValue, onKeyChange, IntAdapter.INSTANCE)

  override fun long(
    key: String,
    defaultValue: Long
  ): Pref<Long> = RealPref(prefs, key, defaultValue, onKeyChange, LongAdapter.INSTANCE)

  override fun string(
    key: String,
    defaultValue: String
  ): Pref<String> = RealPref(prefs, key, defaultValue, onKeyChange, StringAdapter.INSTANCE)

  override fun stringSet(
    key: String,
    defaultValue: StringSet
  ): Pref<StringSet> = RealPref(prefs, key, defaultValue, onKeyChange, StringSetAdapter.INSTANCE)

  override fun clear() {
    prefs.edit()
        .clear()
        .apply()
  }

  override fun getSharedPrefs() = prefs
}

/**
 * We this in combination with non-Kotlin framework classes that return "nullable" even though
 * they're never actually null. Helps the compiler stay calm, along with Codacy's static analysis.
 */
@Throws(IllegalStateException::class)
internal fun <T> dumpsterFire(): T {
  throw IllegalStateException("This should never happen!")
}
