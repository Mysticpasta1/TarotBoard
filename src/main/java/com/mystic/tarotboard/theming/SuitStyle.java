package com.mystic.tarotboard.theming;

import java.io.Serializable;
import java.util.List;

public class SuitStyle implements Serializable {
    private String groupName;
    private String colorHex;

    // Default constructor for serialization
    public SuitStyle() {}

    public SuitStyle(String groupName, String colorHex) {
        this.groupName = groupName;
        this.colorHex = colorHex;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getColorHex() {
        return colorHex;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public void setColorHex(String colorHex) {
        this.colorHex = colorHex;
    }
}
