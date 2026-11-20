import java.net.URI;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;

/**
 * Conversor de eventos HTML a sintaxis Markdown.
 * Aplica reglas de transformación semántica para encabezados, énfasis, enlaces,
 * imágenes, listas anidadas, citas, código y tablas (desafío opcional).
 */
public class MarkdownConverter extends HTMLEditorKit.ParserCallback {

    private final String baseUrl;
    private final StringBuilder markdown = new StringBuilder();

    // Estado para etiquetas ignoradas
    private int ignoredTagDepth = 0;

    // Estado para enlaces
    private final Deque<String> linkHrefStack = new ArrayDeque<>();
    private final Deque<StringBuilder> linkTextStack = new ArrayDeque<>();

    // Estado para listas anidadas
    private static class ListState {
        final boolean isOrdered;
        int count;

        ListState(boolean isOrdered) {
            this.isOrdered = isOrdered;
            this.count = 0;
        }
    }
    private final Deque<ListState> listStack = new ArrayDeque<>();

    // Estado para bloques de código
    private boolean inPre = false;
    private boolean inCode = false;

    // Estado para citas (blockquote)
    private int blockquoteDepth = 0;

    // Soporte para tablas (Desafío opcional)
    private boolean inTable = false;
    private final List<List<String>> currentTable = new ArrayList<>();
    private List<String> currentRow = new ArrayList<>();
    private StringBuilder currentCell = new StringBuilder();
    private boolean inCell = false;

    public MarkdownConverter(String baseUrl) {
        this.baseUrl = (baseUrl != null) ? baseUrl.trim() : "";
    }

    @Override
    public void handleStartTag(HTML.Tag tag, MutableAttributeSet a, int pos) {
        if (isIgnoredTag(tag)) {
            ignoredTagDepth++;
            return;
        }
        if (ignoredTagDepth > 0) return;

        if (tag == HTML.Tag.H1) startHeading(1);
        else if (tag == HTML.Tag.H2) startHeading(2);
        else if (tag == HTML.Tag.H3) startHeading(3);
        else if (tag == HTML.Tag.H4) startHeading(4);
        else if (tag == HTML.Tag.H5) startHeading(5);
        else if (tag == HTML.Tag.H6) startHeading(6);
        else if (tag == HTML.Tag.P) startParagraph();
        else if (tag == HTML.Tag.B || tag == HTML.Tag.STRONG) appendInline("**");
        else if (tag == HTML.Tag.I || tag == HTML.Tag.EM) appendInline("*");
        else if (tag == HTML.Tag.A) {
            Object hrefAttr = (a != null) ? a.getAttribute(HTML.Attribute.HREF) : null;
            String href = (hrefAttr != null) ? hrefAttr.toString() : "";
            linkHrefStack.push(resolveUrl(href));
            linkTextStack.push(new StringBuilder());
        }
        else if (tag == HTML.Tag.UL) {
            listStack.push(new ListState(false));
            ensureNewline();
        }
        else if (tag == HTML.Tag.OL) {
            listStack.push(new ListState(true));
            ensureNewline();
        }
        else if (tag == HTML.Tag.LI) {
            startListItem();
        }
        else if (tag == HTML.Tag.BLOCKQUOTE) {
            blockquoteDepth++;
            ensureNewline();
        }
        else if (tag == HTML.Tag.PRE) {
            inPre = true;
            ensureDoubleNewline();
            markdown.append("```\n");
        }
        else if (tag == HTML.Tag.CODE) {
            inCode = true;
            if (!inPre) appendInline("`");
        }
        else if (tag.toString().equalsIgnoreCase("table")) {
            inTable = true;
            currentTable.clear();
        }
        else if (tag.toString().equalsIgnoreCase("tr")) {
            currentRow = new ArrayList<>();
        }
        else if (tag.toString().equalsIgnoreCase("th") || tag.toString().equalsIgnoreCase("td")) {
            inCell = true;
            currentCell = new StringBuilder();
        }
    }

    @Override
    public void handleEndTag(HTML.Tag tag, int pos) {
        if (isIgnoredTag(tag)) {
            if (ignoredTagDepth > 0) ignoredTagDepth--;
            return;
        }
        if (ignoredTagDepth > 0) return;

        if (isHeading(tag)) {
            ensureDoubleNewline();
        }
        else if (tag == HTML.Tag.P) {
            ensureDoubleNewline();
        }
        else if (tag == HTML.Tag.B || tag == HTML.Tag.STRONG) appendInline("**");
        else if (tag == HTML.Tag.I || tag == HTML.Tag.EM) appendInline("*");
        else if (tag == HTML.Tag.A) {
            String href = linkHrefStack.isEmpty() ? "" : linkHrefStack.pop();
            StringBuilder textBuffer = linkTextStack.isEmpty() ? new StringBuilder() : linkTextStack.pop();
            String linkText = textBuffer.toString().trim();
            if (linkText.isEmpty()) linkText = href;
            
            String mdLink = "[" + linkText + "](" + href + ")";
            appendInline(mdLink);
        }
        else if (tag == HTML.Tag.UL || tag == HTML.Tag.OL) {
            if (!listStack.isEmpty()) listStack.pop();
            if (listStack.isEmpty()) ensureDoubleNewline();
        }
        else if (tag == HTML.Tag.LI) {
            ensureNewline();
        }
        else if (tag == HTML.Tag.BLOCKQUOTE) {
            if (blockquoteDepth > 0) blockquoteDepth--;
            ensureDoubleNewline();
        }
        else if (tag == HTML.Tag.PRE) {
            inPre = false;
            ensureNewline();
            markdown.append("```\n\n");
        }
        else if (tag == HTML.Tag.CODE) {
            if (!inPre) appendInline("`");
            inCode = false;
        }
        else if (tag.toString().equalsIgnoreCase("th") || tag.toString().equalsIgnoreCase("td")) {
            if (inCell) {
                currentRow.add(currentCell.toString().trim());
                inCell = false;
            }
        }
        else if (tag.toString().equalsIgnoreCase("tr")) {
            if (!currentRow.isEmpty()) {
                currentTable.add(new ArrayList<>(currentRow));
            }
        }
        else if (tag.toString().equalsIgnoreCase("table")) {
            inTable = false;
            renderMarkdownTable();
        }
    }

    @Override
    public void handleSimpleTag(HTML.Tag tag, MutableAttributeSet a, int pos) {
        if (ignoredTagDepth > 0) return;

        if (tag == HTML.Tag.IMG) {
            Object srcAttr = (a != null) ? a.getAttribute(HTML.Attribute.SRC) : null;
            Object altAttr = (a != null) ? a.getAttribute(HTML.Attribute.ALT) : null;
            String rawSrc = (srcAttr != null) ? srcAttr.toString() : "";
            String alt = (altAttr != null) ? altAttr.toString() : "";
            String resolvedSrc = resolveUrl(rawSrc);

            appendInline("![" + alt + "](" + resolvedSrc + ")");
        }
        else if (tag == HTML.Tag.BR) {
            if (inPre) {
                markdown.append("\n");
            } else {
                appendInline("  \n");
            }
        }
        else if (tag == HTML.Tag.HR) {
            ensureDoubleNewline();
            markdown.append("---\n\n");
        }
    }

    @Override
    public void handleText(char[] data, int pos) {
        if (ignoredTagDepth > 0) return;

        String text = new String(data);
        if (inCell) {
            currentCell.append(text);
            return;
        }

        if (inPre) {
            markdown.append(text);
            return;
        }

        if (!linkTextStack.isEmpty()) {
            linkTextStack.peek().append(text);
            return;
        }

        appendInline(text);
    }

    private void appendInline(String text) {
        if (blockquoteDepth > 0 && (markdown.length() == 0 || markdown.charAt(markdown.length() - 1) == '\n')) {
            markdown.append("> ".repeat(blockquoteDepth));
        }
        markdown.append(text);
    }

    private void startHeading(int level) {
        ensureDoubleNewline();
        markdown.append("#".repeat(Math.max(1, Math.min(level, 6)))).append(" ");
    }

    private void startParagraph() {
        ensureDoubleNewline();
    }

    private void startListItem() {
        ensureNewline();
        int depth = Math.max(0, listStack.size() - 1);
        String indent = "  ".repeat(depth);
        markdown.append(indent);

        if (!listStack.isEmpty()) {
            ListState state = listStack.peek();
            if (state.isOrdered) {
                state.count++;
                markdown.append(state.count).append(". ");
            } else {
                markdown.append("- ");
            }
        } else {
            markdown.append("- ");
        }
    }

    private boolean isHeading(HTML.Tag tag) {
        return tag == HTML.Tag.H1 || tag == HTML.Tag.H2 || tag == HTML.Tag.H3
                || tag == HTML.Tag.H4 || tag == HTML.Tag.H5 || tag == HTML.Tag.H6;
    }

    private boolean isIgnoredTag(HTML.Tag tag) {
        return tag == HTML.Tag.SCRIPT || tag == HTML.Tag.STYLE;
    }

    private void ensureNewline() {
        if (markdown.length() > 0 && markdown.charAt(markdown.length() - 1) != '\n') {
            markdown.append("\n");
        }
    }

    private void ensureDoubleNewline() {
        int len = markdown.length();
        if (len == 0) return;
        if (len >= 2 && markdown.charAt(len - 1) == '\n' && markdown.charAt(len - 2) == '\n') {
            return;
        }
        if (markdown.charAt(len - 1) == '\n') {
            markdown.append("\n");
        } else {
            markdown.append("\n\n");
        }
    }

    private void renderMarkdownTable() {
        if (currentTable.isEmpty()) return;
        ensureDoubleNewline();

        int maxCols = 0;
        for (List<String> row : currentTable) {
            maxCols = Math.max(maxCols, row.size());
        }
        if (maxCols == 0) return;

        List<String> header = currentTable.get(0);
        markdown.append("|");
        for (int c = 0; c < maxCols; c++) {
            String val = (c < header.size()) ? header.get(c) : "";
            markdown.append(" ").append(val).append(" |");
        }
        markdown.append("\n|");
        for (int c = 0; c < maxCols; c++) {
            markdown.append(" --- |");
        }
        markdown.append("\n");

        for (int r = 1; r < currentTable.size(); r++) {
            List<String> row = currentTable.get(r);
            markdown.append("|");
            for (int c = 0; c < maxCols; c++) {
                String val = (c < row.size()) ? row.get(c) : "";
                markdown.append(" ").append(val).append(" |");
            }
            markdown.append("\n");
        }
        markdown.append("\n");
    }

    public String resolveUrl(String rawUrl) {
        if (rawUrl == null || rawUrl.isBlank()) return "";
        rawUrl = rawUrl.trim();
        if (baseUrl.isBlank()) return rawUrl;

        try {
            URI base = URI.create(baseUrl);
            return base.resolve(rawUrl).toString();
        } catch (Exception e) {
            try {
                return new URL(new URL(baseUrl), rawUrl).toString();
            } catch (Exception ex) {
                return rawUrl;
            }
        }
    }

    public String getMarkdown() {
        String result = markdown.toString();
        result = result.replaceAll("\n{3,}", "\n\n");
        return result.trim();
    }
}
