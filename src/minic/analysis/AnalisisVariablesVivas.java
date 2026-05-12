package minic.analysis;

import minic.cfg.BloqueBasico;
import minic.cfg.GrafoFlujoControl;
import minic.ir.InstruccionTAC;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class AnalisisVariablesVivas {
    public void analizar(GrafoFlujoControl cfg) {
        if (cfg == null) {
            return;
        }
        List<BloqueBasico> bloques = cfg.getBloques();
        for (BloqueBasico bloque : bloques) {
            calcularUseDef(bloque);
            bloque.getIn().clear();
            bloque.getOut().clear();
        }

        boolean cambio;
        do {
            cambio = false;
            for (int i = bloques.size() - 1; i >= 0; i--) {
                BloqueBasico bloque = bloques.get(i);
                Set<String> outNuevo = new LinkedHashSet<String>();
                for (BloqueBasico sucesor : bloque.getSucesores()) {
                    outNuevo.addAll(sucesor.getIn());
                }

                Set<String> inNuevo = new LinkedHashSet<String>(bloque.getUse());
                Set<String> outMenosDef = new LinkedHashSet<String>(outNuevo);
                outMenosDef.removeAll(bloque.getDef());
                inNuevo.addAll(outMenosDef);

                if (!outNuevo.equals(bloque.getOut()) || !inNuevo.equals(bloque.getIn())) {
                    bloque.getOut().clear();
                    bloque.getOut().addAll(outNuevo);
                    bloque.getIn().clear();
                    bloque.getIn().addAll(inNuevo);
                    cambio = true;
                }
            }
        } while (cambio);
    }

    public String comoTexto(GrafoFlujoControl cfg) {
        StringBuilder salida = new StringBuilder();
        if (cfg == null || cfg.getBloques().isEmpty()) {
            salida.append("(sin informacion de flujo)").append(System.lineSeparator());
            return salida.toString();
        }
        for (BloqueBasico bloque : cfg.getBloques()) {
            salida.append("Bloque ").append(bloque.getId()).append(System.lineSeparator());
            salida.append("use: ").append(formatear(bloque.getUse())).append(System.lineSeparator());
            salida.append("def: ").append(formatear(bloque.getDef())).append(System.lineSeparator());
            salida.append("in:  ").append(formatear(bloque.getIn())).append(System.lineSeparator());
            salida.append("out: ").append(formatear(bloque.getOut())).append(System.lineSeparator());
        }
        return salida.toString();
    }

    private void calcularUseDef(BloqueBasico bloque) {
        bloque.getUse().clear();
        bloque.getDef().clear();
        for (InstruccionTAC instruccion : bloque.getInstrucciones()) {
            for (String uso : instruccion.getUsos()) {
                if (!bloque.getDef().contains(uso)) {
                    bloque.getUse().add(uso);
                }
            }
            bloque.getDef().addAll(instruccion.getDefiniciones());
        }
    }

    private static String formatear(Set<String> valores) {
        if (valores == null || valores.isEmpty()) {
            return "{}";
        }
        List<String> ordenados = new ArrayList<String>(valores);
        return "{" + String.join(", ", ordenados) + "}";
    }
}
