package bipre.frontend.parse;

public final class ArbolConcreto {
    private final NodoArbol raiz;

    public ArbolConcreto(NodoArbol raiz) {
        this.raiz = raiz;
    }

    public NodoArbol getRaiz() {
        return raiz;
    }

    public String comoTexto() {
        return raiz == null ? "" : raiz.comoTexto();
    }
}
