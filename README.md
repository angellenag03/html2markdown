# HTML2Markdown - Traductor de páginas HTML a Markdown

**Asignatura:** Desarrollo de Sistemas III  
**Unidad:** Programación cliente-servidor y procesamiento de documentos  
**Modalidad:** Individual  
**Lenguaje:** Java 21 (API estándar de Java exclusivamente)  
**Entorno de desarrollo:** IntelliJ IDEA / CLI  
**Control de versiones:** Git y GitHub  

---

## 1. Propósito del Proyecto
Desarrollo de una herramienta de línea de comandos en Java que recibe como parámetro la URL de una página web (HTTP/HTTPS), recupera su contenido HTML mediante conexión HTTP estándar (`HttpURLConnection`), interpreta sus estructuras sintácticas semánticas utilizando analizadores de la API oficial (`ParserDelegator` y `ParserCallback`) y genera una representación fiel en formato **Markdown (`.md`)**, almacenándola en el sistema de archivos local en codificación **UTF-8**.

---

## 2. Arquitectura de la Solución
El sistema se organiza en cinco módulos de responsabilidad única (SRP):

```text
HTML2Markdown/
├── README.md                          # Documentación académica y preguntas de reflexión
├── .gitignore                         # Exclusión de binarios, IDEs y carpeta output/
├── src/
│   └── main/
│       └── java/
│           ├── HTML2Markdown.java     # Coordinación, validación CLI y reporte en consola
│           ├── WebClient.java         # Comunicación HTTP con HttpURLConnection, validación MIME y charset
│           ├── HtmlParser.java        # Análisis léxico/sintáctico mediante ParserDelegator
│           ├── MarkdownConverter.java # Reglas de transformación semántica a Markdown y resolución de URLs
│           └── MarkdownWriter.java    # Persistencia en disco usando java.nio.file y UTF-8
├── tests/
│   ├── encabezados.html               # Casos de prueba para h1-h6 y hr
│   ├── listas.html                    # Casos de prueba para listas ordenadas, desordenadas y anidadas
│   ├── enlaces.html                   # Casos de prueba para enlaces e imágenes relativos y absolutos
│   └── documento_completo.html        # Integración completa con código, citas, tablas y scripts
└── output/
    └── .gitkeep
```

---

## 3. Instrucciones de Compilación y Ejecución

### Requisitos
* **Java Development Kit (JDK):** Versión 21 o superior.
* Sin dependencias externas (compilable de forma nativa con la API de Java).

### Compilación
Desde la raíz del repositorio, ejecute:
```bash
javac --release 21 -d bin src/main/java/*.java
```

### Ejecución
Modo con nombre de archivo de salida automático:
```bash
java -cp bin HTML2Markdown https://ejemplo.org/articulo.html
# Genera: articulo.md en el directorio actual
```

Modo con ruta de salida personalizada:
```bash
java -cp bin HTML2Markdown https://ejemplo.org/articulo.html salida.md
```

---

## 4. Pruebas de Funcionamiento (P01 a P12)

| ID | Condición Evaluada | Escenario / Entrada | Resultado Obtenido | Estado |
| :--- | :--- | :--- | :--- | :---: |
| **P01** | Sin argumentos | `java -cp bin HTML2Markdown` | Muestra sintaxis de uso y ejemplos claros en consola. | **Superada** |
| **P02** | URL inválida | `java -cp bin HTML2Markdown ftp://sitio.org` | Mensaje de error controlado indicando que debe iniciar con HTTP o HTTPS. | **Superada** |
| **P03** | Página inexistente | `java -cp bin HTML2Markdown https://httpstat.us/404` | Notifica error HTTP 404 de manera controlada. | **Superada** |
| **P04** | Encabezados h1 – h6 | `tests/encabezados.html` | Traduce correctamente `#` hasta `######` con espaciado vertical. | **Superada** |
| **P05** | Negritas y cursivas | `<b>`, `<strong>`, `<i>`, `<em>` | Genera `**texto**` y `*texto*` preservando el contenido interno. | **Superada** |
| **P06** | Enlaces absolutos y relativos | `<a href="/ruta">` | Resuelve la URL contra la URL base (`https://.../ruta`). | **Superada** |
| **P07** | Imágenes con texto alternativo | `<img src="img.png" alt="Foto">` | Sintaxis `![Foto](https://.../img.png)` con URL resuelta. | **Superada** |
| **P08** | Listas ordenadas y desordenadas | `<ul>`, `<ol>`, anidadas | Aplica viñetas `-`, numeración `1.` e indentación por nivel. | **Superada** |
| **P09** | Elementos anidados | `<b><i>...</i></b>`, listas en listas | Conserva la jerarquía y el anidamiento simétrico. | **Superada** |
| **P10** | Etiquetas script y style | `<script>`, `<style>` | Omite totalmente su contenido y delimitadores. | **Superada** |
| **P11** | Acentos y entidades HTML | `&aacute;`, `&ntilde;`, `&copy;` | ParserDelegator decodifica a caracteres UTF-8 (`á`, `ñ`, `©`). | **Superada** |
| **P12** | Página HTML vacía | `<html><body></body></html>` | Genera archivo Markdown limpio sin excepciones no controladas. | **Superada** |

---

## 5. Preguntas de Reflexión (Sección 10)

### 1. ¿Qué papel desempeña la aplicación dentro de una arquitectura cliente-servidor?
La aplicación actúa estrictamente como un **cliente HTTP**. En el modelo cliente-servidor, el cliente toma la iniciativa de la comunicación enviando una solicitud (`GET`) a través de un socket de red hacia un servidor web remoto, especificando la dirección del recurso deseado mediante una URL. Una vez que el servidor responde con el flujo de datos y los metadatos correspondientes, el cliente valida el protocolo, procesa localmente el documento (análisis léxico y sintáctico) y produce la transformación a Markdown, sin interferir con la lógica ni con el estado interno del servidor.

### 2. ¿Qué responsabilidades corresponden al servidor y cuáles al cliente?
* **Responsabilidades del Servidor:**
  1. Alojar, versionar y garantizar la disponibilidad de los recursos solicitados.
  2. Escuchar conexiones entrantes en puertos de red (80, 443) y gestionar la concurrencia.
  3. Resolver la ruta del recurso, aplicar políticas de autorización y enviar las cabeceras HTTP de respuesta (`Status Code`, `Content-Type`, `Content-Length`, `Charset`).
  4. Transmitir el flujo binario de datos del documento.
* **Responsabilidades del Cliente:**
  1. Formular solicitudes HTTP válidas y conformes al estándar RFC.
  2. Configurar tiempos máximos de espera (*timeouts* de conexión y lectura) para proteger la aplicación de caídas del servidor.
  3. Interpretar códigos de estado (200, 301, 404, 500) y reaccionar adecuadamente.
  4. Decodificar los bytes utilizando la codificación indicada por el servidor (UTF-8, ISO-8859-1).
  5. Interpretar la estructura jerárquica del HTML y efectuar la conversión semántica en destino.

### 3. ¿Qué diferencia existe entre recuperar HTML e interpretar su estructura?
* **Recuperar HTML** es una operación de transporte y entrada/salida (I/O) a nivel de red. Consiste en recibir un flujo lineal de bytes a través de un socket TCP y ensamblarlos en memoria como una cadena de caracteres plana.
* **Interpretar su estructura (Parseo)** implica un análisis léxico (tokenización) y sintáctico donde la cadena plana de texto se transforma en un modelo jerárquico o en un flujo de eventos estructurados (etiquetas de apertura, atributos, etiquetas de cierre y nodos de texto). Interpretar permite distinguir el rol semántico de cada fragmento (por ejemplo, saber si una palabra es un título `<h1>`, un enlace navegable `<a>` o código ejecutable `<script>`).

### 4. ¿Por qué no basta reemplazar etiquetas HTML con expresiones regulares?
HTML es un lenguaje perteneciente a las gramáticas libres de contexto (Chomsky Tipo 2), mientras que las expresiones regulares estándar describen lenguajes regulares (Chomsky Tipo 3). Las expresiones regulares carecen de memoria de pila (memoria ilimitada para balanceo de paréntesis o anidamientos arbitrarios). Por ende:
1. No pueden emparejar de manera determinista etiquetas arbitrariamente anidadas del mismo tipo (por ejemplo `<div><div>...</div></div>` o `<b><i>...</i></b>`).
2. Sufren de problemas de voracidad (*greediness*) al consumir texto entre etiquetas lejanas.
3. Son vulnerables a fallar ante comentarios que contienen código HTML, atributos con comillas complejas o código HTML5 malformado.

### 5. ¿Qué dificultades presentan los elementos anidados?
La mayor dificultad radica en **mantener el estado contextual en una estructura de pila (LIFO)**. Cuando un elemento está dentro de otro (como un elemento `<li>` dentro de un `<ol>` que a su vez está dentro de un `<ul>`):
* Se requiere conocer la profundidad exacta para calcular la sangría de espacios adecuada en Markdown.
* Es necesario preservar el orden simétrico de apertura y cierre de delimitadores (por ejemplo, un enlace en negrita `[**texto**](url)` frente a una negrita que contiene un enlace `**[texto](url)**`).
* Se debe saber si el texto actual se encuentra en un contexto que altera el procesamiento, como un bloque `<pre>` (donde no se deben alterar espacios ni saltos de línea).

### 6. ¿Qué información puede perderse al convertir HTML a Markdown?
Markdown es un formato minimalista diseñado para legibilidad en texto plano, por lo que carece de la riqueza expresiva de HTML/CSS. Se pierde:
1. **Atributos de estilo y presentación:** Colores de fuentes, tamaños tipográficos personalizados, alineaciones, márgenes, fondos y diseños responsivos en CSS.
2. **Atributos identificadores y de datos:** `id`, `class`, atributos `data-*` y accesibilidad ARIA.
3. **Estructuras complejas no soportadas:** Formularios interactivos (`<form>`, `<input>`, `<button>`), elementos multimedia dinámicos (`<canvas>`, `<svg>`, `<audio>`, `<video>`, `<iframe>`) y elementos de interactividad como `<dialog>` y `<details>/<summary>`.
4. **Propiedades avanzadas de tablas:** Combinación de celdas (`colspan`, `rowspan`) y alineaciones mixtas avanzadas.

### 7. ¿Por qué deben resolverse las URL relativas?
En un servidor web, una URL relativa (por ejemplo `/assets/img/diagrama.png` o `../capitulo1.html`) se resuelve contextualmente a partir del dominio y ruta de la página actual. Al guardar la página en un archivo Markdown en la computadora local (`articulo.md`), si los enlaces permanecen relativos, un visor de Markdown los buscará en el sistema de archivos del usuario local (`file://...`), provocando enlaces rotos e imágenes que no cargan. Resolverlas con la URL base garantiza que sigan apuntando al recurso original en Internet de forma absoluta y permanente.

### 8. ¿Qué ventajas ofrece separar descarga, análisis y conversión en clases?
Aplica el **Principio de Responsabilidad Única (SRP)** de la ingeniería de software:
* **Alta cohesión y bajo acoplamiento:** Cada clase se enfoca en un único problema (descarga en `WebClient`, parsing en `HtmlParser`, traducción en `MarkdownConverter` y persistencia en `MarkdownWriter`).
* **Testabilidad aislada:** El conversor a Markdown puede someterse a pruebas unitarias rigurosas con cadenas HTML estáticas en memoria sin depender de acceso a Internet ni de servidores externos.
* **Mantenibilidad:** Si cambian las políticas de red (por ejemplo, agregar soporte de proxy o autenticación HTTP), solo se modifica `WebClient` sin alterar el conversor.

### 9. ¿Qué limitaciones presenta `ParserDelegator` con HTML moderno?
`ParserDelegator` (y la arquitectura del paquete `javax.swing.text.html`) fue diseñado en los orígenes de la plataforma Java basándose fundamentalmente en la especificación **HTML 3.2**. Sus limitaciones principales son:
1. **Desconocimiento de etiquetas semánticas de HTML5:** No reconoce formalmente etiquetas modernas como `<header>`, `<footer>`, `<main>`, `<article>`, `<section>`, `<nav>`, `<aside>`, `<figure>`, tratándolas como etiquetas genéricas o etiquetas vacías (`simpleTag`).
2. **Tolerancia a fallos limitada:** A diferencia de los navegadores modernos con el motor de parseo WHATWG HTML5 (que corrigen automáticamente etiquetas no cerradas u omisiones del árbol DOM), `ParserDelegator` puede reaccionar de forma estricta o descartar fragmentos mal estructurados.

### 10. ¿Qué modificaciones serían necesarias para convertir tablas HTML a Markdown?
Para una conversión robusta de tablas HTML (`<table>`, `<thead>`, `<tbody>`, `<tr>`, `<th>`, `<td>`), se requiere:
1. **Almacenamiento matricial en memoria:** Capturar las filas y celdas en una estructura de datos bidimensional (`List<List<String>>`) antes de emitir la salida Markdown.
2. **Generación de la línea separadora de cabecera:** Detectar la fila de títulos (`<th>` o primera fila de `<td>`) y generar la línea obligatoria de guiones Markdown (`| :--- | :--- |`).
3. **Normalización de columnas:** Identificar el número máximo de columnas y rellenar celdas vacías para garantizar que todas las filas tengan el mismo número de separadores tubería (`|`).
4. **Escape de caracteres especiales:** Escapar cualquier carácter `|` que aparezca dentro del contenido de las celdas para no desestructurar la tabla.
5. **Manejo de celdas combinadas:** Diseñar una estrategia de aproximación para `colspan` o `rowspan` (por ejemplo, duplicar el contenido en las celdas contiguas o aplanarlas con una advertencia).

---

## 6. Desafíos Opcionales Implementados
* **Soporte de Tablas HTML a Markdown:** Reconocimiento de `<table>`, `<tr>`, `<th>` y `<td>` con formateo en rejilla Markdown con línea divisoria de cabeceras.
* **Extracción limpia de Bloques de Código:** Soporte completo de `<pre>` y `<code>` delimitado con triples acentos graves (```).
* **Filtro estricto de seguridad de contenido:** Exclusión de bloques `<script>` y `<style>` para evitar inyección de código o ruido visual en el documento final.
