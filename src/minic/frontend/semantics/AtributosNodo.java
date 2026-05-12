package minic.frontend.semantics;

import minic.frontend.ast.NodoAST;

public final class AtributosNodo {
    private final TipoDato tipo;
    private final boolean esValida;
    private final int linea;
    private final int columna;

    public AtributosNodo(TipoDato tipo, boolean esValida, int linea, int columna) {
        this.tipo = tipo == null ? TipoDato.ERROR : tipo;
        this.esValida = esValida;
        this.linea = linea;
        this.columna = columna;
    }

    public TipoDato getTipo() {
        return tipo;
    }

    public boolean esValida() {
        return esValida;
    }

    public int getLinea() {
        return linea;
    }

    public int getColumna() {
        return columna;
    }

    public static AtributosNodo valido(TipoDato tipo, NodoAST nodo) {
        return new AtributosNodo(tipo, true, linea(nodo), columna(nodo));
    }

    public static AtributosNodo error(NodoAST nodo) {
        return new AtributosNodo(TipoDato.ERROR, false, linea(nodo), columna(nodo));
    }

    private static int linea(NodoAST nodo) {
        return nodo == null ? -1 : nodo.getLinea();
    }

    private static int columna(NodoAST nodo) {
        return nodo == null ? -1 : nodo.getColumna();
    }
}
