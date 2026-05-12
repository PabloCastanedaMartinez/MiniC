public final class ErrorSemantico {
    private final String tipo;
    private final String lexema;
    private final int linea;
    private final int columna;
    private final String descripcion;

    public ErrorSemantico(String tipo, String lexema, int linea, int columna, String descripcion) {
        this.tipo = tipo == null ? "ERROR_SEMANTICO" : tipo;
        this.lexema = lexema == null ? "" : lexema;
        this.linea = linea;
        this.columna = columna;
        this.descripcion = descripcion == null ? "" : descripcion;
    }

    public String getTipo() {
        return tipo;
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
        return "[" + tipo + "] Linea " + linea
                + ", columna " + columna
                + ": " + descripcion
                + (lexema.isEmpty() ? "" : " Lexema: '" + escapar(lexema) + "'");
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
