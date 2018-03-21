package org.moire.opensudoku.gui.inputmethod;

/**
 * Created by goku on 3/14/18.
 */

public enum EditMode {

    MODE_EDIT_VALUE(0),
    MODE_EDIT_NOTE(1);

    private int value;

    EditMode(int value) {
        this.value = value;
    }

    // TODO probably dumb
    static EditMode valueOf(int value) {
        switch (value) {
            case 0:
                return MODE_EDIT_VALUE;
            case 1:
                return MODE_EDIT_NOTE;
        }
        throw new IllegalArgumentException("Value is out of range of the legal enumeration values");
    }

    public int getValue() {
        return value;
    }

    void toggleValue() {
        this.value = this.getValue() == MODE_EDIT_VALUE.getValue() ? MODE_EDIT_NOTE.getValue() : MODE_EDIT_VALUE.getValue();
    }

}
