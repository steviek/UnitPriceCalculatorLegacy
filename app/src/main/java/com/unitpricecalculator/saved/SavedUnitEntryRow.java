package com.unitpricecalculator.saved;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;

import com.unitpricecalculator.unit.Unit;
import com.unitpricecalculator.unit.Units;
import com.unitpricecalculator.util.Jsonable;

import org.json.JSONException;
import org.json.JSONObject;

public class SavedUnitEntryRow implements Jsonable {

    private final String cost;
    private final String quantity;
    private final String size;
    private final Unit unit;

    public SavedUnitEntryRow(String cost, String quantity, String size, Unit unit) {
        this.cost = Preconditions.checkNotNull(cost);
        this.quantity = Preconditions.checkNotNull(quantity);
        this.size = Preconditions.checkNotNull(size);
        this.unit = Preconditions.checkNotNull(unit);
    }

    public String getCost() {
        return cost;
    }

    public String getQuantity() {
        return quantity;
    }

    public String getSize() {
        return size;
    }

    public Unit getUnit() {
        return unit;
    }

    @Override
    public JSONObject toJson() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("cost", cost);
        obj.put("quantity", quantity);
        obj.put("size", size);
        obj.put("unit", Units.toJson(unit));
        return obj;
    }

    public static final Creator<SavedUnitEntryRow> JSON_CREATOR = new Creator<SavedUnitEntryRow>() {
        @Override
        public SavedUnitEntryRow fromJson(JSONObject object) {
            try {
                String cost = object.getString("cost");
                String quantity = object.getString("quantity");
                String size = object.getString("size");
                Unit unit = Units.fromJson(object.getJSONObject("unit"));
                return new SavedUnitEntryRow(cost, quantity, size, unit);
            } catch (JSONException e) {
                throw Throwables.propagate(e);
            }
        }
    };
}
