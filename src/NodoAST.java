import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class NodoAST {
    private static int siguienteId = 1;

    private final int id;
    private final String nombre;
    private final String valor;
    private final List<NodoAST> hijos = new ArrayList<NodoAST>();

    public NodoAST(String nombre) {
        this(nombre, null);
    }

    public NodoAST(String nombre, String valor) {
        this.id = siguienteId++;
        this.nombre = nombre == null ? "" : nombre;
        this.valor = valor;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getValor() {
        return valor;
    }

    public String getEtiqueta() {
        if (valor == null || valor.length() == 0) {
            return nombre;
        }
        return nombre + ": " + valor;
    }

    public List<NodoAST> getHijos() {
        return Collections.unmodifiableList(hijos);
    }

    public NodoAST agregarHijo(NodoAST hijo) {
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
            salida.append(getEtiqueta()).append(System.lineSeparator());
        } else {
            salida.append(prefijo)
                    .append(ultimo ? "`-- " : "|-- ")
                    .append(getEtiqueta())
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
