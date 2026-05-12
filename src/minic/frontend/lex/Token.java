package minic.frontend.lex;

public final class Token {
    private final TokenType tipo;
    private final String lexema;
    private final Object atributo;
    private final int linea;
    private final int columna;

    public Token(TokenType tipo, String lexema, Object atributo, int linea, int columna) {
        this.tipo = tipo;
        this.lexema = lexema;
        this.atributo = atributo;
        this.linea = linea;
        this.columna = columna;
    }

    public TokenType getTipo() {
        return tipo;
    }

    public String getLexema() {
        return lexema;
    }

    public Object getAtributo() {
        return atributo;
    }

    public int getLinea() {
        return linea;
    }

    public int getColumna() {
        return columna;
    }

    @Override
    public String toString() {
        return String.format(
                "TOKEN(tipo=%s, lexema='%s', atributo=%s, linea=%d, columna=%d)",
                tipo,
                escapar(lexema),
                atributo == null ? "null" : atributo,
                linea,
                columna
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
