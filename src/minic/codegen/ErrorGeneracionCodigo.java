package minic.codegen;

public final class ErrorGeneracionCodigo {
    private final String tipo;
    private final String nodoAST;
    private final String descripcion;

    public ErrorGeneracionCodigo(String tipo, String nodoAST, String descripcion) {
        this.tipo = tipo == null ? "ERROR_GENERACION" : tipo;
        this.nodoAST = nodoAST == null ? "" : nodoAST;
        this.descripcion = descripcion == null ? "" : descripcion;
    }

    public String getTipo() {
        return tipo;
    }

    public String getNodoAST() {
        return nodoAST;
    }

    public String getDescripcion() {
        return descripcion;
    }

    @Override
    public String toString() {
        return "[" + tipo + "] Nodo AST '" + nodoAST + "': " + descripcion;
    }
}
