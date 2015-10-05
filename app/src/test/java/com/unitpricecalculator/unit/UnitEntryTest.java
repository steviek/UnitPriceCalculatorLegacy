package com.unitpricecalculator.unit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static com.google.common.truth.Truth.assertThat;

@RunWith(JUnit4.class)
public final class UnitEntryTest {

    @Test
    public void testConversions() {
        UnitEntry oneGram = UnitEntry.builder().setCost(1).setSize(1).setUnit(Unit.GRAM).build();
        assertThat(oneGram.pricePer(1, Unit.KILOGRAM)).isWithin(0.00001).of(1000.0);
        assertThat(oneGram.pricePer(1, Unit.MILLIGRAM)).isWithin(0.00001).of(0.001);

        UnitEntry tenCm = UnitEntry.builder().setCost(5).setSize(10).setUnit(Unit.CENTIMETRE).build();
        assertThat(tenCm.pricePer(10, Unit.MILLIMETRE)).isWithin(0.00001).of(0.5);
    }

    @Test
    public void succeeds_MetricToImperial() {
        UnitEntry twoLitres = UnitEntry.builder().setCost(24).setQuantity(3).setSize(4)
                .setUnit(Unit.MILLILITRE).build();
        assertThat(twoLitres.pricePer(10, Unit.US_CUP)).isWithin(0.001).of(4731.76);
    }

    @Test(expected = IllegalArgumentException.class)
    public void fails_convertInvalidTypes() {
        UnitEntry oneLitre = UnitEntry.builder().setCost(1).setSize(1).setUnit(Unit.LITRE).build();
        oneLitre.pricePer(1, Unit.KILOGRAM);
    }

    @Test(expected = IllegalArgumentException.class)
    public void fails_noSize() {
        UnitEntry.builder().setCost(1).setUnit(Unit.KILOGRAM).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void fails_noUnit() {
        UnitEntry.builder().setCost(1).setCost(0).build();
    }
}
