package com.spms.system.enums;

import lombok.Getter;

@Getter
public enum SerialNumberUpdate {
    DAY(1, "yyyymmdd"),
    MONTH(2, "yyyymm"),
    YEAR(3, "yyyy"),
    NEVER(4, "");

    private final int key;
    private final String defaultTemplate;

    SerialNumberUpdate(int key, String defaultTemplate) {
        this.key = key;
        this.defaultTemplate = defaultTemplate;
    }

    public static SerialNumberUpdate fromKey(Integer key) {
        if (key == null) {
            return DAY;
        }
        for (SerialNumberUpdate value : values()) {
            if (value.key == key) {
                return value;
            }
        }
        return DAY;
    }
}
