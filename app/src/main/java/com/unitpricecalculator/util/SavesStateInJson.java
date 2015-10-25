package com.unitpricecalculator.util;

import org.json.JSONException;
import org.json.JSONObject;

public interface SavesStateInJson {

    JSONObject saveState() throws JSONException;

    void restoreState(JSONObject object) throws JSONException;

}
