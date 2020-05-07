package com.unitpricecalculator.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.unitpricecalculator.comparisons.ComparisonFragmentState;
import com.unitpricecalculator.comparisons.SavedUnitEntryRow;
import com.unitpricecalculator.json.JsonMapperModule;
import com.unitpricecalculator.json.ObjectMapper;
import dagger.Component;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class SerializationTests {

  private ObjectMapper objectMapper;

  @Before
  public void setUp() {
    objectMapper = DaggerSerializationTests_TestComponent.create().getObjectMapper();
  }

  @Test
  public void testDefaultUnit() {
    Unit unit = DefaultUnit.UNIT;
    String serialized = objectMapper.toJson(unit);
    Unit deSerialized = objectMapper.fromJson(DefaultUnit.class, serialized);
    assertEquals(unit, deSerialized);
  }

  @Test
  public void testSavedComparison() {
    String currencyCode = "USD";
    DefaultUnit unit = DefaultUnit.CENTIMETRE;
    String note = "hello world";
    SavedUnitEntryRow savedUnitEntryRow = new SavedUnitEntryRow("1", "1", "1", unit, note);
    SavedComparison savedComparison = new SavedComparison("key", "comparison", unit.getUnitType(),
        ImmutableList.of(savedUnitEntryRow), "1.4", unit, currencyCode);
    String serialized = objectMapper.toJson(savedComparison);
    SavedComparison deSerialized = objectMapper.fromJson(SavedComparison.class, serialized);
    assertEquals(savedComparison, deSerialized);
  }

  @Test
  public void testComparisonFragmentState() {
    String currencyCode = "USD";
    DefaultUnit unit = DefaultUnit.CENTIMETRE;
    String note = "hello world";
    SavedUnitEntryRow savedUnitEntryRow = new SavedUnitEntryRow("1", "1", "1", unit, note);
    SavedComparison savedComparison = new SavedComparison("key", "comparison", unit.getUnitType(),
        ImmutableList.of(savedUnitEntryRow), "1.4", unit, currencyCode);

    ComparisonFragmentState state = new ComparisonFragmentState(savedComparison, null);

    String serialized = objectMapper.toJson(state);
    ComparisonFragmentState restored =
        objectMapper.fromJson(ComparisonFragmentState.class, serialized);
    assertEquals(state, restored);
  }

  @Test
  public void testSavedComparison_nullCurrencyCode_shouldKeepItNull() {
    String currencyCode = null;
    DefaultUnit unit = DefaultUnit.CENTIMETRE;
    String note = "hello wo rld";
    SavedUnitEntryRow savedUnitEntryRow = new SavedUnitEntryRow("1", "1", "1", unit, note);
    SavedComparison savedComparison = new SavedComparison("key", "comparison", unit.getUnitType(),
        ImmutableList.of(savedUnitEntryRow), "1.4", unit, currencyCode);
    String serialized = objectMapper.toJson(savedComparison);
    SavedComparison deSerialized = objectMapper.fromJson(SavedComparison.class, serialized);
    assertEquals(savedComparison, deSerialized);
  }

  @Test
  public void testSavedComparison_nullKey_shouldGenerateOne() {
    String currencyCode = null;
    DefaultUnit unit = DefaultUnit.CENTIMETRE;
    String note = "hello world";
    SavedUnitEntryRow savedUnitEntryRow = new SavedUnitEntryRow("1", "1", "1", unit, note);
    SavedComparison savedComparison = new SavedComparison(null, "comparison", unit.getUnitType(),
        ImmutableList.of(savedUnitEntryRow), "1.4", unit, currencyCode);
    String serialized = objectMapper.toJson(savedComparison);
    SavedComparison deSerialized = objectMapper.fromJson(SavedComparison.class, serialized);
    assertFalse(Strings.isNullOrEmpty(deSerialized.getKey()));
  }

  @Component(modules = JsonMapperModule.class)
  interface TestComponent {

    ObjectMapper getObjectMapper();
  }

}
