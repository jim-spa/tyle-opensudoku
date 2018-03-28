package org.moire.opensudoku.gui.importing;

import org.moire.opensudoku.R;
import org.moire.opensudoku.db.SudokuImportParams;
import org.moire.opensudoku.db.SudokuInvalidFormatException;
import org.moire.opensudoku.game.SudokuGame;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

/**
 * I want to use this as a utility class for XML parsing to be used by multiple classes for
 * XML parsing.  An interface with default or static methods does not make sense because interfaces
 * are designed with extensibility in mind. The use of static methods and a private constructor
 * with final class is done to prevent extensiblity and highlight the class' intention of being just
 * a utility class.
 */
public final class XmlParser {

    private XmlParser() {}

    static void importXml(final Reader input, final ImportTask importTask) throws SudokuInvalidFormatException {
        BufferedReader inBR = new BufferedReader(input);
        /*
         * while((s=in.readLine())!=null){ Log.i(tag, "line: "+s); }
         */

        // parse xml
        XmlPullParserFactory factory;
        XmlPullParser xpp;
        try {
            factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(false);
            xpp = factory.newPullParser();
            xpp.setInput(inBR);
            int eventType = xpp.getEventType();
            String rootTag = "";
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    rootTag = xpp.getName();
                    if (rootTag.equals("opensudoku")) {
                        String version = xpp.getAttributeValue(null, "version");
                        if (version == null) {
                            // no version provided, assume that it's version 1
                            importV1(xpp, importTask);
                        } else if (version.equals("2")) {
                            importV2(xpp, importTask);
                        } else {
                            importTask.setError("Unknown version of data.");
                        }
                    } else {
                        importTask.setError(importTask.getContext().getString(R.string.invalid_format));
                        return;
                    }
                }
                eventType = xpp.next();
            }
        } catch (XmlPullParserException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void importV2(XmlPullParser parser, ImportTask importTask)
            throws XmlPullParserException, IOException, SudokuInvalidFormatException {
        int eventType = parser.getEventType();
        String lastTag = "";
        SudokuImportParams importParams = new SudokuImportParams();

        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                lastTag = parser.getName();
                if (lastTag.equals("folder")) {
                    String name = parser.getAttributeValue(null, "name");
                    long created = parseLong(parser.getAttributeValue(null, "created"), System.currentTimeMillis());
                    importTask.importFolder(name, created);
                } else if (lastTag.equals("game")) {
                    importParams.clear();
                    importParams.created = parseLong(parser.getAttributeValue(null, "created"), System.currentTimeMillis());
                    importParams.state = parseLong(parser.getAttributeValue(null, "state"), SudokuGame.GAME_STATE_NOT_STARTED);
                    importParams.time = parseLong(parser.getAttributeValue(null, "time"), 0);
                    importParams.lastPlayed = parseLong(parser.getAttributeValue(null, "last_played"), 0);
                    importParams.data = parser.getAttributeValue(null, "data");
                    importParams.note = parser.getAttributeValue(null, "note");

                    importTask.importGame(importParams);
                }
            } else if (eventType == XmlPullParser.END_TAG) {
                lastTag = "";
            } else if (eventType == XmlPullParser.TEXT) {
                if (lastTag.equals("name")) {
                }

            }
            eventType = parser.next();
        }
    }

    private static long parseLong(String string, long defaultValue) {
        return string != null ? Long.parseLong(string) : defaultValue;
    }

    private static void importV1(XmlPullParser parser, ImportTask importTask)
            throws XmlPullParserException, IOException, SudokuInvalidFormatException {
        int eventType = parser.getEventType();
        String lastTag = "";

        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                lastTag = parser.getName();
                if (lastTag.equals("game")) {
                    importTask.importGame(parser.getAttributeValue(null, "data"));
                }
            } else if (eventType == XmlPullParser.END_TAG) {
                lastTag = "";
            } else if (eventType == XmlPullParser.TEXT) {
                if (lastTag.equals("name")) {
                    importTask.importFolder(parser.getText());
                }

            }
            eventType = parser.next();
        }
    }

}
