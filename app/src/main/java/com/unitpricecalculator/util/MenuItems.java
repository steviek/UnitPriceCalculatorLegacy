package com.unitpricecalculator.util;

import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.MenuItem;

public final class MenuItems {

  private static final int COLOR_ENABLED = 0xFFFFFFFF;
  private static final int COLOR_DISABLED = 0x80FFFFFF;

  private MenuItems() {
  }

  public static void setEnabled(@Nullable MenuItem menuItem, boolean enabled) {
    if (menuItem == null) {
      return;
    }
    menuItem.setEnabled(enabled);
    Drawable drawable = DrawableCompat.wrap(menuItem.getIcon());
    DrawableCompat.setTint(drawable, enabled ? COLOR_ENABLED : COLOR_DISABLED);
    menuItem.setIcon(drawable);
  }

}
