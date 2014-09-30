package org.lumeh.routemaster.models;

import javax.json.JsonObject;

public interface Uploadable {
    public String getUploadPath();
    public JsonObject toJson();
}
