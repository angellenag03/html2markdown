import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Encargado de persistir el contenido Markdown generado en el sistema de archivos
 * local utilizando java.nio.file y codificación UTF-8.
 */
public class MarkdownWriter {

    /**
     * Guarda el contenido Markdown en la ruta de destino especificada.
     * Crea automáticamente los directorios padre si no existen.
     *
     * @param destino Ruta del archivo .md de destino.
     * @param contenido Texto en formato Markdown.
     * @throws IOException Si ocurre un error de escritura.
     */
    public void guardar(Path destino, String contenido) throws IOException {
        if (destino == null) {
            throw new IllegalArgumentException("La ruta de destino no puede ser nula.");
        }
        if (contenido == null) {
            contenido = "";
        }

        Path parent = destino.getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }

        Files.writeString(
                destino,
                contenido,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE
        );
    }

    /**
     * Genera una ruta de archivo predeterminada a partir de una URL dada.
     *
     * @param urlString URL de origen.
     * @return Path con el nombre del archivo derivado y extensión .md.
     */
    public static Path generarRutaPredeterminada(String urlString) {
        String nombre = "articulo.md";
        if (urlString != null && !urlString.isBlank()) {
            try {
                URI uri = URI.create(urlString);
                String path = uri.getPath();
                if (path != null && !path.isBlank() && !path.equals("/")) {
                    if (path.endsWith("/")) {
                        path = path.substring(0, path.length() - 1);
                    }
                    int lastSlash = path.lastIndexOf('/');
                    String segmento = (lastSlash >= 0) ? path.substring(lastSlash + 1) : path;
                    if (!segmento.isBlank()) {
                        int dotIndex = segmento.lastIndexOf('.');
                        if (dotIndex > 0) {
                            nombre = segmento.substring(0, dotIndex) + ".md";
                        } else {
                            nombre = segmento + ".md";
                        }
                    }
                }
            } catch (Exception e) {
                nombre = "articulo.md";
            }
        }
        return Path.of(nombre);
    }
}
