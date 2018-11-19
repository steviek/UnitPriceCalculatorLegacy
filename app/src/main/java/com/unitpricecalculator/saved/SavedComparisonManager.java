package com.unitpricecalculator.saved;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unitpricecalculator.comparisons.SavedComparison;
import com.unitpricecalculator.util.prefs.Keys;
import com.unitpricecalculator.util.prefs.Prefs;
import dagger.Reusable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;

@Reusable
public final class SavedComparisonManager {

  private final SharedPreferences prefs;
  private final ObjectMapper objectMapper;

  @Inject
  SavedComparisonManager(Activity activity, ObjectMapper objectMapper, Prefs prefs) {
    this.prefs =
        activity.getSharedPreferences("saved-prefs", Context.MODE_PRIVATE);
    this.objectMapper = objectMapper;
    cleanUpLegacyValues(prefs);
  }

  private void cleanUpLegacyValues(Prefs prefs) {
    List<SavedComparison> legacyValues =
        prefs.getList(SavedComparison.class, Keys.LEGACY_SAVED_STATES, null);
    if (legacyValues == null) {
      return;
    }

    for (SavedComparison savedComparison : legacyValues) {
      putSavedComparison(savedComparison);
    }

    prefs.remove(Keys.LEGACY_SAVED_STATES);
  }

  public void putSavedComparison(SavedComparison savedComparison) {
    try {
      prefs.edit()
          .putString(savedComparison.getKey(), objectMapper.writeValueAsString(savedComparison))
          .apply();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void removeSavedComparisons(SavedComparison savedComparison) {
    prefs.edit().remove(savedComparison.getKey()).apply();
  }

  public List<SavedComparison> getSavedComparisons() {
    Map<String, ?> map = prefs.getAll();

    List<SavedComparison> list = new ArrayList<>(map.size());
    for (Object value : map.values()) {
      try {
        list.add(objectMapper.readValue((String) value, SavedComparison.class));
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    // Sort in reverse to show newer saved values at the top.
    Collections.sort(list, (o1, o2) -> o2.getKey().compareTo(o1.getKey()));

    return list;
  }

}
