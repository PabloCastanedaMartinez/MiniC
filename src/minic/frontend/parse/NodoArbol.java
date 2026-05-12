package minic.frontend.parse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class NodoArbol {
    private static int siguienteId = 1;

    private final int id;
    private final String etiqueta;
    private final List<NodoArbol> hijos = new ArrayList<NodoArbol>();

    public NodoArbol(String etiqueta) {
        this.id = siguienteId++;
        this.etiqueta = etiqueta == null ? "" : etiqueta;
    }

    public int getId() {
        return id;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public List<NodoArbol> getHijos() {
        return Collections.unmodifiableList(hijos);
    }

    public NodoArbol agregarHijo(NodoArbol hijo) {
        if (hijo != null) {
            hijos.add(hijo);
        }
        return hijo;
    }

    public String comoTexto() {
        StringBuilder salida = new StringBuilder();
        construirTexto(salida, "", true, true);
        return salida.toString();
    }

    private void construirTexto(StringBuilder salida, String prefijo, boolean ultimo, boolean raiz) {
        if (raiz) {
            salida.append(etiqueta).append(System.lineSeparator());
        } else {
            salida.append(prefijo)
                    .append(ultimo ? "`-- " : "|-- ")
                    .append(etiqueta)
                    .append(System.lineSeparator());
        }

        String nuevoPrefijo;
        if (raiz) {
            nuevoPrefijo = "";
        } else {
            nuevoPrefijo = prefijo + (ultimo ? "    " : "|   ");
        }

        for (int i = 0; i < hijos.size(); i++) {
            hijos.get(i).construirTexto(salida, nuevoPrefijo, i == hijos.size() - 1, false);
        }
    }
}
