package com.unitpricecalculator.util;

import org.json.JSONException;
import org.json.JSONObject;

public interface Jsonable {
    JSONObject toJson() throws JSONException;

    interface Creator<T extends Jsonable> {
        T fromJson(JSONObject object);
    }
}
