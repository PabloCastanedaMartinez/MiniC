public final class InstruccionObjeto {
    private final String operacion;
    private final String operando;

    public InstruccionObjeto(String operacion, String operando) {
        this.operacion = operacion == null ? "" : operacion;
        this.operando = operando;
    }

    public String getOperacion() {
        return operacion;
    }

    public String getOperando() {
        return operando;
    }

    public String comoTexto() {
        if (operando == null || operando.length() == 0) {
            return operacion;
        }
        return operacion + " " + operando;
    }

    @Override
    public String toString() {
        return comoTexto();
    }
}
