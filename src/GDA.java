import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class GDA {
    private final String bloqueId;
    private final List<NodoGDA> nodos = new ArrayList<NodoGDA>();

    public GDA(String bloqueId) {
        this.bloqueId = bloqueId == null ? "" : bloqueId;
    }

    public String getBloqueId() {
        return bloqueId;
    }

    public void agregarNodo(NodoGDA nodo) {
        if (nodo != null) {
            nodos.add(nodo);
        }
    }

    public List<NodoGDA> getNodos() {
        return Collections.unmodifiableList(nodos);
    }

    public String comoTexto() {
        StringBuilder salida = new StringBuilder();
        salida.append("GDA ").append(bloqueId).append(":").append(System.lineSeparator());
        if (nodos.isEmpty()) {
            salida.append("  (sin expresiones)").append(System.lineSeparator());
            return salida.toString();
        }
        for (NodoGDA nodo : nodos) {
            salida.append("  ").append(nodo.comoTexto()).append(System.lineSeparator());
        }
        return salida.toString();
    }
}
