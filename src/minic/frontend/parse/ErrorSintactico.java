package minic.frontend.parse;

public final class ErrorSintactico {
    private final String tokenEncontrado;
    private final String tokenEsperado;
    private final int linea;
    private final int columna;
    private final String descripcion;

    public ErrorSintactico(String tokenEncontrado, String tokenEsperado, int linea, int columna, String descripcion) {
        this.tokenEncontrado = tokenEncontrado;
        this.tokenEsperado = tokenEsperado;
        this.linea = linea;
        this.columna = columna;
        this.descripcion = descripcion;
    }

    public String getTokenEncontrado() {
        return tokenEncontrado;
    }

    public String getTokenEsperado() {
        return tokenEsperado;
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
                "[SINTACTICO] Linea %d, columna %d: %s. Encontrado: %s. Esperado: %s",
                linea,
                columna,
                descripcion,
                tokenEncontrado,
                tokenEsperado
        );
    }
}
