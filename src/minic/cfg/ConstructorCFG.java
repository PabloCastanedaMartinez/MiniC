package minic.cfg;

import minic.ir.InstruccionTAC;
import minic.ir.OperadorTAC;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ConstructorCFG {
    public GrafoFlujoControl construir(List<BloqueBasico> bloques) {
        Map<String, BloqueBasico> etiquetas = new HashMap<String, BloqueBasico>();
        if (bloques == null) {
            return new GrafoFlujoControl(null);
        }

        for (BloqueBasico bloque : bloques) {
            for (InstruccionTAC instruccion : bloque.getInstrucciones()) {
                if (instruccion.getOperador() == OperadorTAC.LABEL) {
                    etiquetas.put(instruccion.getEtiqueta(), bloque);
                    break;
                }
            }
        }

        for (int i = 0; i < bloques.size(); i++) {
            BloqueBasico bloque = bloques.get(i);
            BloqueBasico siguiente = i + 1 < bloques.size() ? bloques.get(i + 1) : null;
            InstruccionTAC ultima = bloque.ultimaInstruccion();
            if (ultima == null) {
                if (siguiente != null) {
                    bloque.agregarSucesor(siguiente);
                }
                continue;
            }

            if (ultima.getOperador() == OperadorTAC.GOTO) {
                bloque.agregarSucesor(etiquetas.get(ultima.getEtiqueta()));
            } else if (ultima.getOperador() == OperadorTAC.IF_FALSE || ultima.getOperador() == OperadorTAC.IF_TRUE) {
                bloque.agregarSucesor(etiquetas.get(ultima.getEtiqueta()));
                if (siguiente != null) {
                    bloque.agregarSucesor(siguiente);
                }
            } else if (siguiente != null) {
                bloque.agregarSucesor(siguiente);
            }
        }

        return new GrafoFlujoControl(bloques);
    }
}
