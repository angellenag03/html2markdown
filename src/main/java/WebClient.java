import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Cliente HTTP encargado de establecer la conexión y recuperar el contenido HTML
 * utilizando exclusivamente clases de la API estándar de Java (URL, HttpURLConnection).
 */
public class WebClient {

    private static final int CONNECT_TIMEOUT_MS = 10000; // 10 segundos
    private static final int READ_TIMEOUT_MS = 15000;    // 15 segundos
    private static final int MAX_SIZE_BYTES = 5 * 1024 * 1024; // 5 MB máximo

    private int ultimoCodigoHttp = -1;
    private String ultimoContentType = "";
    private long ultimoTamanioBytes = 0;
    private Charset ultimoCharset = StandardCharsets.UTF_8;

    public WebClient() {
    }

    /**
     * Descarga el documento HTML remoto a partir de una URL HTTP o HTTPS.
     *
     * @param url Objeto URL de destino.
     * @return Contenido HTML decodificado como String.
     * @throws IOException Si ocurre un error de conexión, lectura, código HTTP no exitoso o tipo no HTML.
     */
    public String descargarHTML(URL url) throws IOException {
        if (url == null) {
            throw new IllegalArgumentException("La URL no puede ser nula.");
        }

        String protocolo = url.getProtocol().toLowerCase();
        if (!protocolo.equals("http") && !protocolo.equals("https")) {
            throw new IOException("Protocolo no soportado: '" + protocolo + "'. Se requiere HTTP o HTTPS.");
        }

        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
            connection.setReadTimeout(READ_TIMEOUT_MS);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("User-Agent", "HTML2Markdown/1.0 (Java 21; University Project)");
            connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            connection.setRequestProperty("Accept-Language", "es,en;q=0.9");

            connection.connect();

            int status = connection.getResponseCode();
            this.ultimoCodigoHttp = status;

            // Manejo de redirecciones entre protocolos distintos si HttpURLConnection no los sigue
            if (status == HttpURLConnection.HTTP_MOVED_PERM || 
                status == HttpURLConnection.HTTP_MOVED_TEMP || 
                status == HttpURLConnection.HTTP_SEE_OTHER ||
                status == 307 || status == 308) {
                String nuevaUrl = connection.getHeaderField("Location");
                if (nuevaUrl != null && !nuevaUrl.isBlank()) {
                    URL redirectUrl = URI.create(url.toString()).resolve(nuevaUrl).toURL();
                    connection.disconnect();
                    return descargarHTML(redirectUrl);
                }
            }

            if (status < 200 || status >= 300) {
                throw new IOException("Error HTTP " + status + ": " + connection.getResponseMessage());
            }

            // Validar Content-Type
            String contentType = connection.getContentType();
            this.ultimoContentType = (contentType != null) ? contentType : "desconocido";

            if (contentType != null) {
                String ctLower = contentType.toLowerCase();
                if (!ctLower.contains("text/html") && !ctLower.contains("application/xhtml+xml")) {
                    throw new IOException("El recurso solicitado no es HTML válido (Content-Type recibido: " + contentType + ")");
                }
            }

            // Identificar codificación de caracteres
            Charset charset = extraerCharset(contentType);
            this.ultimoCharset = charset;

            // Leer flujo de entrada con límite de tamaño
            try (InputStream in = connection.getInputStream();
                 ByteArrayOutputStream out = new ByteArrayOutputStream()) {

                byte[] buffer = new byte[8192];
                int bytesRead;
                long totalBytes = 0;

                while ((bytesRead = in.read(buffer)) != -1) {
                    totalBytes += bytesRead;
                    if (totalBytes > MAX_SIZE_BYTES) {
                        throw new IOException("El documento HTML supera el límite máximo permitido de " 
                                + (MAX_SIZE_BYTES / (1024 * 1024)) + " MB.");
                    }
                    out.write(buffer, 0, bytesRead);
                }

                this.ultimoTamanioBytes = totalBytes;
                return out.toString(charset);
            }

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private Charset extraerCharset(String contentType) {
        if (contentType == null) {
            return StandardCharsets.UTF_8;
        }
        for (String param : contentType.split(";")) {
            String trimmed = param.trim();
            if (trimmed.toLowerCase().startsWith("charset=")) {
                String charsetName = trimmed.substring("charset=".length()).trim();
                charsetName = charsetName.replace("\"", "").replace("'", "");
                try {
                    return Charset.forName(charsetName);
                } catch (Exception e) {
                    return StandardCharsets.UTF_8;
                }
            }
        }
        return StandardCharsets.UTF_8;
    }

    public int getUltimoCodigoHttp() {
        return ultimoCodigoHttp;
    }

    public String getUltimoContentType() {
        return ultimoContentType;
    }

    public long getUltimoTamanioBytes() {
        return ultimoTamanioBytes;
    }

    public Charset getUltimoCharset() {
        return ultimoCharset;
    }
}
