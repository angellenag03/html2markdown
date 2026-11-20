# HTML2Markdown - Traductor de páginas HTML a Markdown

**Asignatura:** Desarrollo de Sistemas III  
**Unidad:** Programación cliente-servidor y procesamiento de documentos  
**Modalidad:** Individual  
**Lenguaje:** Java 21 (API estándar exclusivamente)  
**Entorno:** IntelliJ IDEA / CLI  

---

## 1. Descripción del Proyecto
Aplicación Java de consola que recibe como argumento la URL de una página web (HTTP/HTTPS), recupera su contenido HTML mediante conexión HTTP, interpreta los elementos semánticos principales y genera un archivo equivalente en formato Markdown (`.md`).

---

## 2. Estructura del Proyecto
```text
HTML2Markdown/
├── README.md
├── .gitignore
├── src/
│   └── main/
│       └── java/
│           ├── HTML2Markdown.java
│           ├── WebClient.java
│           ├── HtmlParser.java
│           ├── MarkdownConverter.java
│           └── MarkdownWriter.java
├── tests/
│   ├── encabezados.html
│   ├── listas.html
│   ├── enlaces.html
│   └── documento_completo.html
└── output/
    └── articulo.md
```

---

## 3. Requisitos y Ejecución
* **Java:** JDK 21 o superior
* **Compilación:**
  ```bash
  javac --release 21 -d bin src/main/java/*.java
  ```
* **Ejecución:**
  ```bash
  java -cp bin HTML2Markdown <URL> [archivo_salida.md]
  ```

---

## 4. Preguntas de Reflexión
*(Pendientes de responder conforme al desarrollo de la práctica)*

1. **¿Qué papel desempeña la aplicación dentro de una arquitectura cliente-servidor?**
2. **¿Qué responsabilidades corresponden al servidor y cuáles al cliente?**
3. **¿Qué diferencia existe entre recuperar HTML e interpretar su estructura?**
4. **¿Por qué no basta reemplazar etiquetas HTML con expresiones regulares?**
5. **¿Qué dificultades presentan los elementos anidados?**
6. **¿Qué información puede perderse al convertir HTML a Markdown?**
7. **¿Por qué deben resolverse las URL relativas?**
8. **¿Qué ventajas ofrece separar descarga, análisis y conversión en clases?**
9. **¿Qué limitaciones presenta `ParserDelegator` con HTML moderno?**
10. **¿Qué modificaciones serían necesarias para convertir tablas HTML a Markdown?**

---

## 5. Evidencias y Pruebas de Funcionamiento
*(Casos de prueba P01 a P12 por documentar)*
