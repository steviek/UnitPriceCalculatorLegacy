package com.unitpricecalculator.unit;

import static org.junit.Assert.assertEquals;

import android.icu.util.Currency;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.google.common.collect.ImmutableList;
import com.unitpricecalculator.comparisons.SavedComparison;
import com.unitpricecalculator.comparisons.SavedUnitEntryRow;
import java.util.Locale;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class SerializationTests {

    private ObjectMapper objectMapper;

    @Before
    public void setUp() throws Exception {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new GuavaModule());
        objectMapper.enableDefaultTyping();
    }

    @Test
    public void testDefaultUnit() throws Exception {
        Unit unit = DefaultUnit.UNIT;
        String serialized = objectMapper.writeValueAsString(unit);
        Unit deSerialized = objectMapper.readValue(serialized, DefaultUnit.class);
        assertEquals(unit, deSerialized);
    }

    @Test
    public void testCustomUnit() throws Exception {
        Unit unit = new CustomUnit("some-key", "zz", System.METRIC, UnitType.LENGTH, 4.2);
        String serialized = objectMapper.writeValueAsString(unit);
        Unit deSerialized = objectMapper.readValue(serialized, CustomUnit.class);
        assertEquals(unit, deSerialized);
    }

    @Test
    public void testSavedComparison() throws Exception {
        Currency currency = Currency.getInstance(Locale.getDefault());
        Unit unit = DefaultUnit.CENTIMETRE;
        SavedUnitEntryRow savedUnitEntryRow = new SavedUnitEntryRow("1", "1", "1", unit);
        SavedComparison savedComparison = new SavedComparison("comparison", unit.getUnitType(),
                ImmutableList.of(savedUnitEntryRow), "1.4", unit, currency.getCurrencyCode());
        String serialized = objectMapper.writeValueAsString(savedComparison);
        SavedComparison deSerialized = objectMapper.readValue(serialized, SavedComparison.class);
        assertEquals(savedComparison, deSerialized);
    }


}
