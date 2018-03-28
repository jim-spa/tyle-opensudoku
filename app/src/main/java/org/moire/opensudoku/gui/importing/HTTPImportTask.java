package org.moire.opensudoku.gui.importing;

import android.content.Context;

import org.moire.opensudoku.db.SudokuInvalidFormatException;

import java.io.IOException;
import java.io.StringReader;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HTTPImportTask implements ImportProcessor {

    // TODO just for testing right now, will need a better way to do this with Oauth2, etc
    private static final String ENDPOINT = "http://www.ottoarms.com/easy.opensudoku";
    private static final OkHttpClient client = new OkHttpClient();

    private ImportTask importTask;

    public HTTPImportTask(Context context) {
        importTask = new ImportTask(context, this);
    }

    private String run(final String url) throws IOException {
        Request request = new Request.Builder().url(url).build();
        Response response = HTTPImportTask.client.newCall(request).execute();
        return response.body().string();
    }

    @Override
    public void processImport() throws SudokuInvalidFormatException {
        try {
            String response = run(ENDPOINT);
            StringReader reader = new StringReader(response);
            XmlParser.importXml(reader, importTask);
        } catch (IOException e) {
            // TODO Do something here
        }
    }

    @Override
    public void execute() {
        importTask.execute();
    }

}
