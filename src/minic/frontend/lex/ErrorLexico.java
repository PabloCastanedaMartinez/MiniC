package minic.frontend.lex;

public final class ErrorLexico {
    private final String lexema;
    private final int linea;
    private final int columna;
    private final String descripcion;

    public ErrorLexico(String lexema, int linea, int columna, String descripcion) {
        this.lexema = lexema;
        this.linea = linea;
        this.columna = columna;
        this.descripcion = descripcion;
    }

    public String getLexema() {
        return lexema;
    }

    public int getLinea() {
        return linea;
    }

    public int getColumna() {
        return columna;
    }

    public String getDescripcion() {
        return descripcion;
    }

    @Override
    public String toString() {
        return String.format(
                "[LEXICO] Linea %d, columna %d: %s. Lexema: '%s'",
                linea,
                columna,
                descripcion,
                escapar(lexema)
        );
    }

    private static String escapar(String texto) {
        return texto
                .replace("\\", "\\\\")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
                .replace("'", "\\'");
    }
}
