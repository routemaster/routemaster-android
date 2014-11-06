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

    /**
     * Get the corresponding enum object for the given string value. This is
     * used to convert strings from JSON data.
     */
    public static Visibility get(String value) {
        for(Visibility v : values()) {
            if(v.value == value) {
                return v;
            }
        }
        // Fallback to private in the case of an invalid value
        return Visibility.PRIVATE;
    }
}
