package org.lumeh.routemaster.models;

public enum Visibility {
    PUBLIC("public"),
    FRIENDS("friends"),
    PRIVATE("private");

    private final String value;

    Visibility(String value) {
        this.value = value;
    }

    public String toString() {
        return this.value;
    }
}
