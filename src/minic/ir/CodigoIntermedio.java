package minic.ir;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CodigoIntermedio {
    private final List<InstruccionTAC> instrucciones = new ArrayList<InstruccionTAC>();

    public void agregar(InstruccionTAC instruccion) {
        if (instruccion != null) {
            instrucciones.add(instruccion);
        }
    }

    public List<InstruccionTAC> getInstrucciones() {
        return Collections.unmodifiableList(instrucciones);
    }

    public CodigoIntermedio copia() {
        CodigoIntermedio copia = new CodigoIntermedio();
        for (InstruccionTAC instruccion : instrucciones) {
            copia.agregar(instruccion.copia());
        }
        return copia;
    }

    public String comoTexto() {
        StringBuilder salida = new StringBuilder();
        for (int i = 0; i < instrucciones.size(); i++) {
            salida.append(String.format("%3d: %s%n", i + 1, instrucciones.get(i).comoTexto()));
        }
        if (instrucciones.isEmpty()) {
            salida.append("(sin instrucciones)").append(System.lineSeparator());
        }
        return salida.toString();
    }
}
