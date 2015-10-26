package com.unitpricecalculator.saved;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;

import com.unitpricecalculator.unit.Unit;
import com.unitpricecalculator.unit.Units;
import com.unitpricecalculator.util.Jsonable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public final class SavedComparison implements Jsonable {

    private final String name;
    private final ImmutableList<SavedUnitEntryRow> savedUnitEntryRows;
    private final String finalQuantity;
    private final Unit finalUnit;

    public SavedComparison(String name, ImmutableList<SavedUnitEntryRow> savedUnitEntryRows, String finalQuantity,
                           Unit finalUnit) {
        this.name = name;
        this.savedUnitEntryRows = savedUnitEntryRows;
        this.finalQuantity = finalQuantity;
        this.finalUnit = finalUnit;
    }

    public ImmutableList<SavedUnitEntryRow> getSavedUnitEntryRows() {
        return savedUnitEntryRows;
    }

    public String getFinalQuantity() {
        return finalQuantity;
    }

    public Unit getFinalUnit() {
        return finalUnit;
    }

    public String getName() {
        return name;
    }

    @Override
    public JSONObject toJson() throws JSONException {
        JSONObject obj = new JSONObject();
        JSONArray arr = new JSONArray();
        for (SavedUnitEntryRow entryRow : savedUnitEntryRows) {
            arr.put(entryRow.toJson());
        }
        obj.put("name", name);
        obj.put("savedRows", arr);
        obj.put("finalQuantity", finalQuantity);
        obj.put("finalUnit", Units.toJson(finalUnit));
        return obj;
    }

    public static final Creator<SavedComparison> JSON_CREATOR = new Creator<SavedComparison>() {
        @Override
        public SavedComparison fromJson(JSONObject object) {
            try {
                JSONArray arr = object.getJSONArray("savedRows");
                ImmutableList.Builder<SavedUnitEntryRow> list = ImmutableList.builder();
                for (int i = 0; i < arr.length(); i ++) {
                    JSONObject savedRow = arr.getJSONObject(i);
                    SavedUnitEntryRow entryRow = SavedUnitEntryRow.JSON_CREATOR.fromJson(savedRow);
                    list.add(entryRow);
                }
                String name = object.getString("name");
                String finalQuantity = object.getString("finalQuantity");
                Unit unit = Units.fromJson(object.getJSONObject("finalUnit"));
                return new SavedComparison(name, list.build(), finalQuantity, unit);
            } catch (JSONException e) {
                throw Throwables.propagate(e);
            }
        }
    };
}
