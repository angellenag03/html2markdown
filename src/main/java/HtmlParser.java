import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;

/**
 * Analizador sintáctico de documentos HTML basado en las clases estándar
 * javax.swing.text.html.parser.ParserDelegator y HTMLEditorKit.ParserCallback.
 */
public class HtmlParser {

    /**
     * Parsea un flujo Reader utilizando el callback suministrado.
     *
     * @param reader Flujo con el código HTML.
     * @param callback Callback que recibe los eventos sintácticos.
     * @throws IOException Si ocurre un error de lectura durante el parseo.
     */
    public void parse(Reader reader, HTMLEditorKit.ParserCallback callback) throws IOException {
        if (reader == null) {
            throw new IllegalArgumentException("El Reader no puede ser nulo.");
        }
        if (callback == null) {
            throw new IllegalArgumentException("El callback no puede ser nulo.");
        }
        new ParserDelegator().parse(reader, callback, true);
    }

    /**
     * Parsea un String que contiene código HTML utilizando el callback suministrado.
     *
     * @param html Contenido HTML en cadena.
     * @param callback Callback que recibe los eventos sintácticos.
     * @throws IOException Si ocurre un error de lectura durante el parseo.
     */
    public void parse(String html, HTMLEditorKit.ParserCallback callback) throws IOException {
        try (Reader reader = new StringReader(html != null ? html : "")) {
            parse(reader, callback);
        }
    }
}
