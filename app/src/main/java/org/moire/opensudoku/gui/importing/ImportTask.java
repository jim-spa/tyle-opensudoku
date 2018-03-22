package org.moire.opensudoku.gui.importing;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import org.moire.opensudoku.R;
import org.moire.opensudoku.db.SudokuDatabase;
import org.moire.opensudoku.db.SudokuImportParams;
import org.moire.opensudoku.db.SudokuInvalidFormatException;
import org.moire.opensudoku.game.FolderInfo;
import org.moire.opensudoku.gui.ImportSudokuActivity;
import org.moire.opensudoku.utils.Const;

/**
 * To add support for new import source, do following:
 * <p/>
 * 1) Subclass this class. Any input parameters specific for your import should be put
 * in constructor of your class.
 * 3) Add code to {@link ImportSudokuActivity} which creates instance of your new class and
 * passes it input parameters.
 * <p/>
 * TODO: add cancel support
 *
 * @author romario
 */
class ImportTask extends AsyncTask<Void, Integer, Boolean> {
	static final int NUM_OF_PROGRESS_UPDATES = 20;

	private Context context;

	private OnImportProgressListener onImportProgressListener;

	private SudokuDatabase mDatabase;
	private FolderInfo mFolder; // currently processed folder
	private int mFolderCount; // count of processed folders
	private int mGameCount; //count of processed puzzles
	private String mImportError;
	private boolean mImportSuccessful;

	private final ImportProcessor processor;

	ImportTask(Context context, ImportProcessor processor) {
		super();
		this.context = context;
		onImportProgressListener = (OnImportProgressListener) context;
		this.processor = processor;
	}

	public Context getContext() {
		return context;
	}

	@Override
	protected Boolean doInBackground(Void... params) {

		try {
			return processImportInternal();
		} catch (Exception e) {
			Log.e(Const.TAG, "Exception occurred during import.", e);
			setError(context.getString(R.string.unknown_import_error));
		}

		return false;
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		onImportProgressListener.onImportProgress(values);
	}

	@Override
	protected void onPostExecute(Boolean result) {
		if (result) {

			if (mFolderCount == 1) {
				Toast.makeText(context, context.getString(R.string.puzzles_saved, mFolder.name),
						Toast.LENGTH_LONG).show();
			} else if (mFolderCount > 1) {
				Toast.makeText(context, context.getString(R.string.folders_created) + ": " + mFolderCount,
						Toast.LENGTH_LONG).show();
			}

		} else {
			Toast.makeText(context, mImportError, Toast.LENGTH_LONG).show();
		}

		if (onImportProgressListener != null) {
			long folderId = -1;
			if (mFolderCount == 1) {
				folderId = mFolder.id;
			}
			onImportProgressListener.onImportFinished(result, folderId);
		}
	}

	Boolean processImportInternal() {
		mImportSuccessful = true;

		long start = System.currentTimeMillis();

		mDatabase = new SudokuDatabase(context);
		try {
			mDatabase.beginTransaction();

			// let subclass handle the import
			processor.processImport();

			mDatabase.setTransactionSuccessful();
		} catch (SudokuInvalidFormatException e) {
			setError(context.getString(R.string.invalid_format));
		} finally {
			mDatabase.endTransaction();
			mDatabase.close();
			mDatabase = null;
		}


		if (mFolderCount == 0 && mGameCount == 0) {
			setError(context.getString(R.string.no_puzzles_found));
			return false;
		}

		long end = System.currentTimeMillis();

		Log.i(Const.TAG, String.format("Imported in %f seconds.",
				(end - start) / 1000f));

		return mImportSuccessful;
	}

	/**
	 * Creates new folder and starts appending puzzles to this folder.
	 *
	 * @param name
	 */
	void importFolder(String name) {
		importFolder(name, System.currentTimeMillis());
	}


	/**
	 * Creates new folder and starts appending puzzles to this folder.
	 *
	 * @param name
	 * @param created
	 */
	void importFolder(String name, long created) {
		if (mDatabase == null) {
			throw new IllegalStateException("Database is not opened.");
		}

		mFolderCount++;

		mFolder = mDatabase.insertFolder(name, created);
	}

	/**
	 * Starts appending puzzles to the folder with given <code>name</code>. If such folder does
	 * not exist, this method creates new one.
	 *
	 * @param name
	 */
	void appendToFolder(String name) {
		if (mDatabase == null) {
			throw new IllegalStateException("Database is not opened.");
		}

		mFolderCount++;

		mFolder = null;
		mFolder = mDatabase.findFolder(name);
		if (mFolder == null) {
			mFolder = mDatabase.insertFolder(name, System.currentTimeMillis());
		}
	}

	private SudokuImportParams mImportParams = new SudokuImportParams();

	void importGame(String data) throws SudokuInvalidFormatException {
		mImportParams.clear();
		mImportParams.data = data;
		importGame(mImportParams);
	}
	void importGame(SudokuImportParams pars) throws SudokuInvalidFormatException {
		if (mDatabase == null) {
			throw new IllegalStateException("Database is not opened.");
		}

		mDatabase.importSudoku(mFolder.id, pars);
	}

	void setError(String error) {
		mImportError = error;
		mImportSuccessful = false;
	}

}
