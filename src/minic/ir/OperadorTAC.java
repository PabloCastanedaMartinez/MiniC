package minic.ir;

public enum OperadorTAC {
    ASIGNACION("="),
    SUMA("+"),
    RESTA("-"),
    MULTIPLICACION("*"),
    DIVISION("/"),
    MODULO("%"),
    MENOR("<"),
    MAYOR(">"),
    MENOR_IGUAL("<="),
    MAYOR_IGUAL(">="),
    IGUAL("=="),
    DIFERENTE("!="),
    DIFERENTE_ALT("<>"),
    AND("&&"),
    OR("||"),
    GOTO("goto"),
    IF_FALSE("ifFalse"),
    IF_TRUE("ifTrue"),
    LABEL("label"),
    READ("read"),
    PRINT("print"),
    NOP("nop");

    private final String lexema;

    OperadorTAC(String lexema) {
        this.lexema = lexema;
    }

    public String getLexema() {
        return lexema;
    }

    public boolean esBinario() {
        return this == SUMA
                || this == RESTA
                || this == MULTIPLICACION
                || this == DIVISION
                || this == MODULO
                || this == MENOR
                || this == MAYOR
                || this == MENOR_IGUAL
                || this == MAYOR_IGUAL
                || this == IGUAL
                || this == DIFERENTE
                || this == DIFERENTE_ALT
                || this == AND
                || this == OR;
    }

    public boolean esSalto() {
        return this == GOTO || this == IF_FALSE || this == IF_TRUE;
    }

    public boolean tieneEfectoSecundario() {
        return this == READ || this == PRINT || this == GOTO || this == IF_FALSE || this == IF_TRUE || this == LABEL;
    }

    public static OperadorTAC desdeLexema(String lexema) {
        for (OperadorTAC operador : values()) {
            if (operador.lexema.equals(lexema)) {
                return operador;
            }
        }
        return NOP;
    }
}
