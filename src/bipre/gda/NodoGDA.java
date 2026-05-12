package bipre.gda;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class NodoGDA {
    private final int id;
    private final String operador;
    private final String valor;
    private final List<NodoGDA> hijos = new ArrayList<NodoGDA>();
    private final Set<String> etiquetas = new LinkedHashSet<String>();

    public NodoGDA(int id, String operador, String valor) {
        this.id = id;
        this.operador = operador == null ? "" : operador;
        this.valor = valor;
    }

    public int getId() {
        return id;
    }

    public String getOperador() {
        return operador;
    }

    public String getValor() {
        return valor;
    }

    public void agregarHijo(NodoGDA hijo) {
        if (hijo != null) {
            hijos.add(hijo);
        }
    }

    public List<NodoGDA> getHijos() {
        return Collections.unmodifiableList(hijos);
    }

    public void agregarEtiqueta(String etiqueta) {
        if (etiqueta != null && etiqueta.length() > 0) {
            etiquetas.add(etiqueta);
        }
    }

    public Set<String> getEtiquetas() {
        return Collections.unmodifiableSet(etiquetas);
    }

    public String etiquetaVisible() {
        StringBuilder salida = new StringBuilder();
        salida.append(operador);
        if (valor != null && valor.length() > 0) {
            salida.append(": ").append(valor);
        }
        if (!etiquetas.isEmpty()) {
            salida.append("\\n").append(etiquetas);
        }
        return salida.toString();
    }

    public String comoTexto() {
        StringBuilder salida = new StringBuilder();
        salida.append("Nodo").append(id).append(": ").append(operador);
        if (valor != null && valor.length() > 0) {
            salida.append(" ").append(valor);
        }
        if (!hijos.isEmpty()) {
            salida.append(" (");
            for (int i = 0; i < hijos.size(); i++) {
                if (i > 0) {
                    salida.append(", ");
                }
                salida.append("Nodo").append(hijos.get(i).getId());
            }
            salida.append(")");
        }
        if (!etiquetas.isEmpty()) {
            salida.append(" etiquetas ").append(etiquetas);
        }
        return salida.toString();
    }
}
