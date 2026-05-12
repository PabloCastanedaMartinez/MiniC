package minic.frontend.symbols;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class TablaSimbolos {
    private final List<Ambito> ambitos = new ArrayList<Ambito>();
    private int siguienteId = 1;
    private Ambito actual;

    public TablaSimbolos() {
        actual = new Ambito(0, "global", null);
        ambitos.add(actual);
    }

    public Ambito getAmbitoActual() {
        return actual;
    }

    public Ambito abrirAmbito(String nombre) {
        Ambito nuevo = new Ambito(siguienteId++, nombre, actual);
        ambitos.add(nuevo);
        actual = nuevo;
        return actual;
    }

    public void cerrarAmbito() {
        if (actual.getPadre() != null) {
            actual = actual.getPadre();
        }
    }

    public boolean existeEnAmbitoActual(String nombre) {
        return actual.contieneLocal(nombre);
    }

    public void insertar(Simbolo simbolo) {
        actual.insertar(simbolo);
    }

    public Simbolo buscar(String nombre) {
        Ambito ambito = actual;
        while (ambito != null) {
            Simbolo simbolo = ambito.buscarLocal(nombre);
            if (simbolo != null) {
                return simbolo;
            }
            ambito = ambito.getPadre();
        }
        return null;
    }

    public List<Ambito> getAmbitos() {
        return Collections.unmodifiableList(ambitos);
    }

    public List<Simbolo> getTodosLosSimbolos() {
        List<Simbolo> salida = new ArrayList<Simbolo>();
        for (Ambito ambito : ambitos) {
            salida.addAll(ambito.getSimbolos());
        }
        return salida;
    }

    public String comoTexto() {
        StringBuilder salida = new StringBuilder();
        salida.append(String.format("%-15s %-18s %-12s %-15s %-8s %-10s%n",
                "Nombre", "Tipo", "minic.frontend.symbols.Ambito", "Inicializado", "Usado", "Linea"));
        for (Simbolo simbolo : getTodosLosSimbolos()) {
            salida.append(String.format("%-15s %-18s %-12s %-15s %-8s %-10s%n",
                    simbolo.getNombre(),
                    simbolo.getTipo(),
                    simbolo.getAmbito(),
                    simbolo.estaInicializado(),
                    simbolo.estaUsado(),
                    simbolo.getLineaDeclaracion()));
        }
        return salida.toString();
    }
}
