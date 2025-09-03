package com.unitpricecalculator.unit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static com.google.common.truth.Truth.assertThat;

@RunWith(JUnit4.class)
public final class UnitEntryTest {

    @Test
    public void testConversions() {
        UnitEntry oneGram = new UnitEntry(
                1.0,
                "1",
                1,
                "1",
                1.0,
                "1",
                DefaultUnit.GRAM
        );
        assertThat(oneGram.pricePer(1, DefaultUnit.KILOGRAM)).isWithin(0.00001).of(1000.0);
        assertThat(oneGram.pricePer(1, DefaultUnit.MILLIGRAM)).isWithin(0.00001).of(0.001);

        UnitEntry tenCm = new UnitEntry(
                5.0,
                "5",
                1,
                "",
                10.0,
                "10",
                DefaultUnit.CENTIMETRE
        );
        assertThat(tenCm.pricePer(10, DefaultUnit.MILLIMETRE)).isWithin(0.00001).of(0.5);
    }

    @Test
    public void succeeds_MetricToImperial() {
        UnitEntry twoLitres = new UnitEntry(
                24.0,
                "24",
                3,
                "3",
                4,
                "4",
                DefaultUnit.MILLILITRE
        );
        assertThat(twoLitres.pricePer(10, DefaultUnit.US_CUP)).isWithin(0.001).of(4731.76);
    }

    @Test(expected = IllegalArgumentException.class)
    public void fails_convertInvalidTypes() {
        UnitEntry oneLitre = new UnitEntry(
                1,
                "1",
                1,
                "1",
                1,
                "1",
                DefaultUnit.LITRE
        );
        oneLitre.pricePer(1, DefaultUnit.KILOGRAM);
    }
}
