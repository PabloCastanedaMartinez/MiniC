import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CodigoObjeto {
    private final List<InstruccionObjeto> instrucciones = new ArrayList<InstruccionObjeto>();

    public void agregar(String operacion, String operando) {
        instrucciones.add(new InstruccionObjeto(operacion, operando));
    }

    public List<InstruccionObjeto> getInstrucciones() {
        return Collections.unmodifiableList(instrucciones);
    }

    public String comoTexto() {
        StringBuilder salida = new StringBuilder();
        if (instrucciones.isEmpty()) {
            salida.append("(sin instrucciones)").append(System.lineSeparator());
            return salida.toString();
        }
        for (InstruccionObjeto instruccion : instrucciones) {
            salida.append(instruccion.comoTexto()).append(System.lineSeparator());
        }
        return salida.toString();
    }
}
