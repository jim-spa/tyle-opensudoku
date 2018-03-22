package org.moire.opensudoku.gui.importing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import android.content.Context;
import android.net.Uri;
import org.moire.opensudoku.db.SudokuInvalidFormatException;

/**
 * Handles import of .sdm files (see http://sudocue.net/download.php).
 *
 * @author romario
 */
public class SdmImportTask implements ImportProcessor {

	private Uri mUri;
	private ImportTask importTask;

	public SdmImportTask(Context context, Uri uri) {
		importTask = new ImportTask(context, this);
		mUri = uri;
	}

	@Override
	public void processImport() throws SudokuInvalidFormatException {
		importTask.importFolder(mUri.getLastPathSegment());

		try {
			URL url = new URL(mUri.toString());
			InputStreamReader isr = new InputStreamReader(url.openStream());
			BufferedReader br = null;
			try {
				br = new BufferedReader(isr);
				String s;
				while ((s = br.readLine()) != null) {
					if (!s.equals("")) {
						importTask.importGame(s);
					}
				}
			} finally {
				if (br != null) br.close();
			}
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void execute() {
		importTask.execute();
	}

}
