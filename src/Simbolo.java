public final class Simbolo {
    private final String nombre;
    private final TipoDato tipo;
    private final String categoria;
    private final int lineaDeclaracion;
    private final int columnaDeclaracion;
    private final String ambito;
    private final int ambitoId;
    private final int profundidad;
    private boolean inicializado;
    private boolean usado;

    public Simbolo(String nombre, TipoDato tipo, String categoria, int lineaDeclaracion,
            int columnaDeclaracion, String ambito, int ambitoId, int profundidad,
            boolean inicializado) {
        this.nombre = nombre == null ? "" : nombre;
        this.tipo = tipo == null ? TipoDato.ERROR : tipo;
        this.categoria = categoria == null ? "variable" : categoria;
        this.lineaDeclaracion = lineaDeclaracion;
        this.columnaDeclaracion = columnaDeclaracion;
        this.ambito = ambito == null ? "" : ambito;
        this.ambitoId = ambitoId;
        this.profundidad = profundidad;
        this.inicializado = inicializado;
    }

    public String getNombre() {
        return nombre;
    }

    public TipoDato getTipo() {
        return tipo;
    }

    public String getCategoria() {
        return categoria;
    }

    public int getLineaDeclaracion() {
        return lineaDeclaracion;
    }

    public int getColumnaDeclaracion() {
        return columnaDeclaracion;
    }

    public String getAmbito() {
        return ambito;
    }

    public int getAmbitoId() {
        return ambitoId;
    }

    public int getProfundidad() {
        return profundidad;
    }

    public boolean estaInicializado() {
        return inicializado;
    }

    public void marcarInicializado() {
        inicializado = true;
    }

    public boolean estaUsado() {
        return usado;
    }

    public void marcarUsado() {
        usado = true;
    }
}
