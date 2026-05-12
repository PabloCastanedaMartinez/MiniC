import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class GrafoFlujoControl {
    private final List<BloqueBasico> bloques = new ArrayList<BloqueBasico>();

    public GrafoFlujoControl(List<BloqueBasico> bloques) {
        if (bloques != null) {
            this.bloques.addAll(bloques);
        }
    }

    public List<BloqueBasico> getBloques() {
        return Collections.unmodifiableList(bloques);
    }

    public String comoTexto() {
        StringBuilder salida = new StringBuilder();
        if (bloques.isEmpty()) {
            salida.append("(sin bloques)").append(System.lineSeparator());
            return salida.toString();
        }
        for (BloqueBasico bloque : bloques) {
            salida.append(bloque.comoTexto());
        }
        return salida.toString();
    }
}
