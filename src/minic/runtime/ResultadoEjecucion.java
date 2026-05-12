package minic.runtime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ResultadoEjecucion {
    private final String salida;
    private final List<String> errores;

    public ResultadoEjecucion(String salida, List<String> errores) {
        this.salida = salida == null ? "" : salida;
        this.errores = new ArrayList<String>();
        if (errores != null) {
            this.errores.addAll(errores);
        }
    }

    public String getSalida() {
        return salida;
    }

    public List<String> getErrores() {
        return Collections.unmodifiableList(errores);
    }

    public boolean isExitoso() {
        return errores.isEmpty();
    }
}
