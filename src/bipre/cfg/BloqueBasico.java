package bipre.cfg;

import bipre.ir.InstruccionTAC;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class BloqueBasico {
    private final String id;
    private final List<InstruccionTAC> instrucciones = new ArrayList<InstruccionTAC>();
    private final List<BloqueBasico> sucesores = new ArrayList<BloqueBasico>();
    private final List<BloqueBasico> predecesores = new ArrayList<BloqueBasico>();
    private final Set<String> use = new LinkedHashSet<String>();
    private final Set<String> def = new LinkedHashSet<String>();
    private final Set<String> in = new LinkedHashSet<String>();
    private final Set<String> out = new LinkedHashSet<String>();

    public BloqueBasico(String id) {
        this.id = id == null ? "" : id;
    }

    public String getId() {
        return id;
    }

    public void agregarInstruccion(InstruccionTAC instruccion) {
        if (instruccion != null) {
            instrucciones.add(instruccion);
        }
    }

    public List<InstruccionTAC> getInstrucciones() {
        return Collections.unmodifiableList(instrucciones);
    }

    public InstruccionTAC ultimaInstruccion() {
        if (instrucciones.isEmpty()) {
            return null;
        }
        return instrucciones.get(instrucciones.size() - 1);
    }

    public void agregarSucesor(BloqueBasico sucesor) {
        if (sucesor == null || sucesores.contains(sucesor)) {
            return;
        }
        sucesores.add(sucesor);
        sucesor.agregarPredecesor(this);
    }

    private void agregarPredecesor(BloqueBasico predecesor) {
        if (predecesor != null && !predecesores.contains(predecesor)) {
            predecesores.add(predecesor);
        }
    }

    public List<BloqueBasico> getSucesores() {
        return Collections.unmodifiableList(sucesores);
    }

    public List<BloqueBasico> getPredecesores() {
        return Collections.unmodifiableList(predecesores);
    }

    public Set<String> getUse() {
        return use;
    }

    public Set<String> getDef() {
        return def;
    }

    public Set<String> getIn() {
        return in;
    }

    public Set<String> getOut() {
        return out;
    }

    public String comoTexto() {
        StringBuilder salida = new StringBuilder();
        salida.append(id).append(":").append(System.lineSeparator());
        for (InstruccionTAC instruccion : instrucciones) {
            salida.append("  ").append(instruccion.comoTexto()).append(System.lineSeparator());
        }
        salida.append("  sucesores: ").append(ids(sucesores)).append(System.lineSeparator());
        salida.append("  predecesores: ").append(ids(predecesores)).append(System.lineSeparator());
        return salida.toString();
    }

    private static String ids(List<BloqueBasico> bloques) {
        if (bloques.isEmpty()) {
            return "{}";
        }
        StringBuilder salida = new StringBuilder("{");
        for (int i = 0; i < bloques.size(); i++) {
            if (i > 0) {
                salida.append(", ");
            }
            salida.append(bloques.get(i).getId());
        }
        salida.append("}");
        return salida.toString();
    }
}
