package com.messbuddy.messbuddy.entity.type;

public enum LoginRole {
    USER("User"),
    MESS_OWNER("Mess Owner");

    private final String value;

    LoginRole(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static LoginRole fromValue(String value) {
        for (LoginRole role : values()) {
            if (role.value.equalsIgnoreCase(value) || role.name().equalsIgnoreCase(value)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Invalid login role: " + value);
    }
}
