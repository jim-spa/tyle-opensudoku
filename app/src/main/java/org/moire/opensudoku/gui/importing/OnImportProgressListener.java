package org.moire.opensudoku.gui.importing;

/**
 * Created by goku on 3/22/18.
 */

public interface OnImportProgressListener {
    /**
     * Occurs when import is finished.
     *
     * @param importSuccessful Indicates whether import was successful.
     * @param folderId         Contains id of imported folder, or -1 if multiple folders were imported.
     */
    void onImportFinished(boolean importSuccessful, long folderId);

    void onImportProgress(Integer... values);
}
