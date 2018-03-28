package org.moire.opensudoku.gui.importing;

import org.moire.opensudoku.db.SudokuInvalidFormatException;

/**
 * Created by goku on 3/22/18.
 */

public interface ImportProcessor {
    void processImport() throws SudokuInvalidFormatException;
    void execute();
}
