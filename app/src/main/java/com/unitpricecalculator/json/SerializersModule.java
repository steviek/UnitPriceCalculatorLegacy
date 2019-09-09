package com.unitpricecalculator.json;

import androidx.annotation.Nullable;
import com.unitpricecalculator.comparisons.ComparisonFragmentState;
import com.unitpricecalculator.comparisons.SavedComparison;
import com.unitpricecalculator.comparisons.SavedUnitEntryRow;
import com.unitpricecalculator.unit.DefaultUnit;
import com.unitpricecalculator.unit.UnitType;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ClassKey;
import dagger.multibindings.IntoMap;
import java.util.ArrayList;
import java.util.List;

@Module
public interface SerializersModule {

  @Provides
  @ClassKey(DefaultUnit.class)
  @IntoMap
  static JsonSerializer provideDefaultUnitJsonSerializer() {
    return new EnumSerializer<>(DefaultUnit::valueOf);
  }

  @Provides
  @ClassKey(UnitType.class)
  @IntoMap
  static JsonSerializer provideUnitTypeJsonSerializer() {
    return new EnumSerializer<>(UnitType::valueOf);
  }

  @Provides
  @ClassKey(SavedComparison.class)
  @IntoMap
  static JsonSerializer provideSavedComparisonJsonSerializer() {
    return new JsonSerializer<SavedComparison>() {

      private static final String KEY = "key";
      private static final String NAME = "name";
      private static final String UNIT_TYPE = "savedComparison";
      private static final String SAVED_UNIT_ENTRY_ROWS = "savedUnitEntryRows";
      private static final String FINAL_QUANTITY = "finalQuantity";
      private static final String FINAL_UNIT = "finalUnit";
      private static final String CURRENCY_CODE = "currencyCode";

      @Override
      public JsonObject toJson(ObjectMapper objectMapper, SavedComparison comparison) {
        return new JsonObject(objectMapper)
            .put(KEY, comparison.getKey())
            .put(NAME, comparison.getName())
            .put(UNIT_TYPE, comparison.getUnitType())
            .put(SAVED_UNIT_ENTRY_ROWS,
                objectMapper.toJsonArray(comparison.getSavedUnitEntryRows()))
            .put(FINAL_QUANTITY, comparison.getFinalQuantity())
            .put(FINAL_UNIT, comparison.getFinalUnit())
            .putNullable(CURRENCY_CODE, comparison.getCurrencyCode());
      }

      @Override
      public SavedComparison fromJson(ObjectMapper objectMapper, JsonObject json) {
        @Nullable String key = json.getStringOrNull(KEY);
        String name = json.getStringOrThrow(NAME);
        UnitType unitType = json.getOrThrow(UnitType.class, UNIT_TYPE);
        List<SavedUnitEntryRow> savedUnitEntryRowList =
            json.getJsonArrayOrThrow(SAVED_UNIT_ENTRY_ROWS).toList(SavedUnitEntryRow.class);
        String finalQuantity = json.getStringOrThrow(FINAL_QUANTITY);
        DefaultUnit finalUnit = json.getOrThrow(DefaultUnit.class, FINAL_UNIT);
        @Nullable String currencyCode = json.getStringOrNull(CURRENCY_CODE);
        return new SavedComparison(
            key, name, unitType, savedUnitEntryRowList, finalQuantity, finalUnit, currencyCode);
      }
    };
  }

  @Provides
  @ClassKey(SavedUnitEntryRow.class)
  @IntoMap
  static JsonSerializer provideSavedUnitEntryRowJsonSerializer() {
    return new JsonSerializer<SavedUnitEntryRow>() {

      private static final String COST = "cost";
      private static final String QUANTITY = "quantity";
      private static final String SIZE = "size";
      private static final String UNIT = "unit";
      private static final String NOTE = "note";

      @Override
      public JsonObject toJson(ObjectMapper objectMapper, SavedUnitEntryRow instance) {
        return new JsonObject(objectMapper)
            .put(COST, instance.getCost())
            .put(QUANTITY, instance.getQuantity())
            .put(SIZE, instance.getSize())
            .put(UNIT, instance.getUnit())
            .put(NOTE, instance.getNote());
      }

      @Override
      public SavedUnitEntryRow fromJson(ObjectMapper objectMapper, JsonObject json) {
        String cost = json.getStringOrThrow(COST);
        String quantity = json.getStringOrThrow(QUANTITY);
        String size = json.getStringOrThrow(SIZE);
        DefaultUnit unit = json.getOrThrow(DefaultUnit.class, UNIT);
        @Nullable String note = json.getStringOrNull(NOTE);
        return new SavedUnitEntryRow(cost, quantity, size, unit, note);
      }
    };
  }

  @Provides
  @ClassKey(ComparisonFragmentState.class)
  @IntoMap
  static JsonSerializer provideComparisonFragmentStateJsonSerializer() {
    return new JsonSerializer<ComparisonFragmentState>() {

      private static final String CURRENT_COMPARISON = "currentComparison";
      private static final String LAST_KNOWN_COMPARISON = "lastKnownComparison";

      @Override
      public JsonObject toJson(ObjectMapper objectMapper, ComparisonFragmentState instance) {
        return new JsonObject(objectMapper)
            .put(CURRENT_COMPARISON, instance.getCurrentComparison())
            .putNullable(LAST_KNOWN_COMPARISON, instance.getLastKnownSavedComparison());
      }

      @Override
      public ComparisonFragmentState fromJson(ObjectMapper objectMapper, JsonObject json) {
        return new ComparisonFragmentState(
            json.getOrThrow(SavedComparison.class, CURRENT_COMPARISON),
            json.getOrNull(SavedComparison.class, LAST_KNOWN_COMPARISON));
      }
    };
  }

  @Provides
  @ClassKey(SavedComparison.class)
  @IntoMap
  static LegacyDeserializer provideSavedComparisonFallback() {
    return (objectMapper, json) -> {
      String key = json.getStringOrThrow("key");
      String name = json.getStringOrThrow("name");

      String rawUnitType = json.getStringOrThrow("unitType");
      UnitType unitType = UnitType.valueOf(rawUnitType);

      String finalQuantity = json.getStringOrThrow("finalQuantity");

      JsonArray rawFinalUnit = json.getJsonArrayOrThrow("finalUnit");
      DefaultUnit finalUnit = DefaultUnit.valueOf(rawFinalUnit.getString(1));

      @Nullable String currencyCode = json.getStringOrNull("currencyCode");

      JsonArray rawSavedUnitEntryRows =
          json.getJsonArrayOrThrow("savedUnitEntryRows").getJsonArray(1);
      List<SavedUnitEntryRow> savedUnitEntryRows = new ArrayList<>();

      int length = rawSavedUnitEntryRows.length();
      for (int i = 0; i < length; i++) {
        JsonObject jsonObject = rawSavedUnitEntryRows.getJsonObject(i);
        String cost = jsonObject.getStringOrThrow("cost");
        String quantity = jsonObject.getStringOrThrow("quantity");
        String size = jsonObject.getStringOrThrow("size");
        DefaultUnit unit = DefaultUnit.valueOf(jsonObject.getJsonArrayOrThrow("unit").getString(1));
        @Nullable String note = jsonObject.getStringOrNull("note");
        savedUnitEntryRows.add(new SavedUnitEntryRow(cost, quantity, size, unit, note));
      }

      return new SavedComparison(key, name, unitType, savedUnitEntryRows, finalQuantity, finalUnit,
          currencyCode);
    };
  }

}
