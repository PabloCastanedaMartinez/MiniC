package minic.frontend.semantics;

public enum TipoDato {
    NUM("num"),
    FLOTAR("flotar"),
    LETRA("letra"),
    CADENA("cadena"),
    BOOLEANO_INTERNO("booleano_interno"),
    ERROR("error"),
    VACIO("vacio");

    private final String nombre;

    TipoDato(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    public boolean esNumerico() {
        return this == NUM || this == FLOTAR;
    }

    public boolean esDeclarablePorUsuario() {
        return this == NUM || this == FLOTAR || this == LETRA;
    }

    public static TipoDato desdeLexema(String lexema) {
        if ("num".equals(lexema)) {
            return NUM;
        }
        if ("flotar".equals(lexema)) {
            return FLOTAR;
        }
        if ("letra".equals(lexema)) {
            return LETRA;
        }
        if ("cadena".equals(lexema)) {
            return CADENA;
        }
        if ("booleano_interno".equals(lexema)) {
            return BOOLEANO_INTERNO;
        }
        return ERROR;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
