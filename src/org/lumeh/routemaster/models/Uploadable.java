package org.lumeh.routemaster.models;

import org.json.JSONObject;

public interface Uploadable {
    public String getUploadPath();
    public JSONObject toJson();
}
