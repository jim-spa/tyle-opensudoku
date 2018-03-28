package org.moire.opensudoku.gui.importing;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import org.moire.opensudoku.db.SudokuInvalidFormatException;

/**
 * Handles import of application/x-opensudoku or .opensudoku files.
 *
 * @author romario
 */
public class OpenSudokuImportTask implements ImportProcessor {

	private Uri mUri;
	private ImportTask importTask;

	public OpenSudokuImportTask(Context context, Uri uri) {
		importTask = new ImportTask(context, this);
		mUri = uri;
	}

	@Override
	public void processImport() throws SudokuInvalidFormatException {
		try {
			InputStreamReader streamReader;
			if (mUri.getScheme().equals("content")) {
				ContentResolver contentResolver = importTask.getContext().getContentResolver();
				streamReader = new InputStreamReader(contentResolver.openInputStream(mUri));
			} else {
				java.net.URI juri;
				juri = new java.net.URI(mUri.getScheme(), mUri
						.getSchemeSpecificPart(), mUri.getFragment());
				streamReader = new InputStreamReader(juri.toURL().openStream());
			}

			try {
				XmlParser.importXml(streamReader, importTask);
			} finally {
				streamReader.close();
			}
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void execute() {
		importTask.execute();
	}

}
