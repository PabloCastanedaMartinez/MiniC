package minic.ir;

public final class GeneradorTemporales {
    private int contador = 1;

    public String nuevoTemporal() {
        return "t" + contador++;
    }
}
