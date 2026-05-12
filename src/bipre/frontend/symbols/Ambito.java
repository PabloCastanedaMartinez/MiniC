package bipre.frontend.symbols;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public final class Ambito {
    private final int id;
    private final String nombre;
    private final Ambito padre;
    private final int profundidad;
    private final Map<String, Simbolo> simbolos = new LinkedHashMap<String, Simbolo>();

    public Ambito(int id, String nombre, Ambito padre) {
        this.id = id;
        this.nombre = nombre == null ? "" : nombre;
        this.padre = padre;
        this.profundidad = padre == null ? 0 : padre.getProfundidad() + 1;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEtiqueta() {
        return id == 0 ? "global" : nombre + "_" + id;
    }

    public Ambito getPadre() {
        return padre;
    }

    public int getProfundidad() {
        return profundidad;
    }

    public boolean contieneLocal(String nombreSimbolo) {
        return simbolos.containsKey(nombreSimbolo);
    }

    public Simbolo buscarLocal(String nombreSimbolo) {
        return simbolos.get(nombreSimbolo);
    }

    public void insertar(Simbolo simbolo) {
        simbolos.put(simbolo.getNombre(), simbolo);
    }

    public Collection<Simbolo> getSimbolos() {
        return simbolos.values();
    }
}
