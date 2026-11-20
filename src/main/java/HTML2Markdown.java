import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Clase principal y punto de entrada de la aplicación HTML2Markdown.
 * Coordina la validación de argumentos, descarga HTTP, análisis HTML,
 * conversión a Markdown y guardado del archivo.
 */
public class HTML2Markdown {

    public static void main(String[] args) {
        // RF01 / P01: Validación de argumentos
        if (args.length == 0) {
            mostrarUso();
            System.exit(1);
        }

        String urlStr = args[0].trim();
        Path rutaDestino;

        // RF02: Ruta de salida opcional
        if (args.length >= 2 && !args[1].isBlank()) {
            rutaDestino = Path.of(args[1].trim());
        } else {
            rutaDestino = MarkdownWriter.generarRutaPredeterminada(urlStr);
        }

        // RF01 / P02: Validar formato de URL
        URL url;
        try {
            URI uri = URI.create(urlStr);
            if (uri.getScheme() == null || (!uri.getScheme().equalsIgnoreCase("http") && !uri.getScheme().equalsIgnoreCase("https"))) {
                System.err.println("Error: La URL debe comenzar con http:// o https://");
                System.exit(2);
                return;
            }
            url = uri.toURL();
        } catch (IllegalArgumentException | MalformedURLException e) {
            System.err.println("Error: La URL proporcionada no es válida: " + e.getMessage());
            System.exit(2);
            return;
        }

        System.out.println("HTML2Markdown");
        System.out.println("--------------------------------");
        System.out.println("URL: " + url);
        System.out.println("Conectando con el servidor...");

        WebClient client = new WebClient();
        String html;

        try {
            // RF03, RF04: Conexión y descarga
            html = client.descargarHTML(url);
            System.out.println("Respuesta HTTP: " + client.getUltimoCodigoHttp());
        } catch (IOException e) {
            // RF11 / P03: Manejo de errores de conexión/HTTP
            System.err.println("Error en la conexión HTTP: " + e.getMessage());
            System.exit(3);
            return;
        }

        System.out.println("Analizando HTML...");
        System.out.println("Convirtiendo a Markdown...");

        // RF05, RF06, RF07, RF08, RF09: Parseo y Conversión
        HtmlParser parser = new HtmlParser();
        MarkdownConverter converter = new MarkdownConverter(url.toString());

        try {
            parser.parse(html, converter);
        } catch (IOException e) {
            System.err.println("Error durante el análisis del documento HTML: " + e.getMessage());
            System.exit(4);
            return;
        }

        String markdown = converter.getMarkdown();

        // RF10: Guardar en disco
        MarkdownWriter writer = new MarkdownWriter();
        try {
            writer.guardar(rutaDestino, markdown);
            long tamanioGenerado = Files.size(rutaDestino);

            // RF12: Resumen de la operación
            System.out.println("Archivo generado: " + rutaDestino.getFileName());
            System.out.println("Ubicación: " + rutaDestino.toAbsolutePath());
            System.out.println("Tamaño: " + String.format("%,d", tamanioGenerado) + " bytes");
            System.out.println("Conversión finalizada.");
        } catch (IOException e) {
            System.err.println("Error al guardar el archivo Markdown: " + e.getMessage());
            System.exit(5);
        }
    }

    private static void mostrarUso() {
        System.out.println("HTML2Markdown - Traductor de páginas HTML a Markdown");
        System.out.println("Uso:");
        System.out.println("  java HTML2Markdown <URL> [archivo_salida.md]");
        System.out.println();
        System.out.println("Ejemplos:");
        System.out.println("  java HTML2Markdown https://ejemplo.org/articulo.html");
        System.out.println("  java HTML2Markdown https://ejemplo.org/articulo.html salida.md");
    }
}
