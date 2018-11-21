package com.unitpricecalculator.saved;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.unitpricecalculator.comparisons.SavedComparison;
import com.unitpricecalculator.comparisons.SavedUnitEntryRow;
import com.unitpricecalculator.unit.DefaultUnit;
import com.unitpricecalculator.unit.Unit;
import com.unitpricecalculator.unit.UnitType;
import com.unitpricecalculator.util.prefs.Keys;
import com.unitpricecalculator.util.prefs.Prefs;
import dagger.Reusable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
    Set<String> savedStates = prefs.getStringSet(Keys.LEGACY_SAVED_STATES, null);
    if (savedStates == null) {
      return;
    }

    long key = 1;
    for (String rawState : savedStates) {
      try {
        JSONObject jsonObject = new JSONObject(rawState);
        String name = jsonObject.getString("name");

        String unitType = jsonObject.getString("unitType");
        UnitType decodedUnitType = UnitType.valueOf(unitType);

        String finalQuantity = jsonObject.getString("finalQuantity");

        JSONArray finalUnit = jsonObject.getJSONArray("finalUnit");
        Unit decodedUnit = DefaultUnit.valueOf(finalUnit.getString(1));

        JSONArray savedUnitEntryRows =
            jsonObject.getJSONArray("savedUnitEntryRows").getJSONArray(1);
        ImmutableList.Builder<SavedUnitEntryRow> unitEntryRowBuilder = ImmutableList.builder();
        for (int i = 0; i < savedUnitEntryRows.length(); i++) {
          JSONObject row = savedUnitEntryRows.getJSONObject(i);
          String cost = row.getString("cost");
          String quantity = row.getString("quantity");
          String size = row.getString("size");
          Unit unit = DefaultUnit.valueOf(row.getJSONArray("unit").getString(1));
          unitEntryRowBuilder.add(new SavedUnitEntryRow(cost, quantity, size, unit, ""));
        }
        putSavedComparison(new SavedComparison(String.valueOf(key++), name, decodedUnitType,
            unitEntryRowBuilder.build(), finalQuantity, decodedUnit, /* currencyCode= */ null));
      } catch (JSONException e) {
        // We failed, just skip it and destroy the old saved files.
      }
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

  public void removeSavedComparison(SavedComparison savedComparison) {
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
