package bipre.ir;

public final class GeneradorEtiquetas {
    private int contador = 1;

    public String nuevaEtiqueta() {
        return "L" + contador++;
    }
}
