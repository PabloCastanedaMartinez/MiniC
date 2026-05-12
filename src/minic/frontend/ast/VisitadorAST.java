package minic.frontend.ast;

public interface VisitadorAST<T> {
    T visitar(NodoAST nodo);
}
