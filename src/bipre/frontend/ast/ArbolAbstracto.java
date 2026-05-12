package bipre.frontend.ast;

public final class ArbolAbstracto {
    private final NodoAST raiz;

    public ArbolAbstracto(NodoAST raiz) {
        this.raiz = raiz;
    }

    public NodoAST getRaiz() {
        return raiz;
    }

    public String comoTexto() {
        return raiz == null ? "" : raiz.comoTexto();
    }
}
