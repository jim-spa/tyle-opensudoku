package org.moire.opensudoku.gui.importing;

import android.content.Context;

import org.moire.opensudoku.db.SudokuInvalidFormatException;

/**
 * Handles import of puzzles via intent's extras.
 *
 * @author romario
 */
public class ExtrasImportTask implements ImportProcessor {

	private String mFolderName;
	private String mGames;
	private boolean mAppendToFolder;
	private ImportTask importTask;

	public ExtrasImportTask(Context context, String folderName, String games, boolean appendToFolder) {
		importTask = new ImportTask(context, this);
		mFolderName = folderName;
		mGames = games;
		mAppendToFolder = appendToFolder;
	}

	@Override
	public void processImport() throws SudokuInvalidFormatException {
		if (mAppendToFolder) {
			importTask.appendToFolder(mFolderName);
		} else {
			importTask.importFolder(mFolderName);
		}

		for (String game : mGames.split("\n")) {
			importTask.importGame(game);
		}
	}

	@Override
	public void execute() {
		importTask.execute();
	}

}
