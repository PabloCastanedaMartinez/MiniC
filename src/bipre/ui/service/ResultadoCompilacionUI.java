package bipre.ui.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ResultadoCompilacionUI {
    private final List<ResultadoFase> fases;
    private final String salidaFinal;
    private final String detallesTecnicos;
    private final boolean exitoGeneral;

    public ResultadoCompilacionUI(List<ResultadoFase> fases, String salidaFinal,
            String detallesTecnicos, boolean exitoGeneral) {
        this.fases = new ArrayList<ResultadoFase>();
        if (fases != null) {
            this.fases.addAll(fases);
        }
        this.salidaFinal = salidaFinal == null ? "" : salidaFinal;
        this.detallesTecnicos = detallesTecnicos == null ? "" : detallesTecnicos;
        this.exitoGeneral = exitoGeneral;
    }

    public List<ResultadoFase> getFases() {
        return Collections.unmodifiableList(fases);
    }

    public String getSalidaFinal() {
        return salidaFinal;
    }

    public String getDetallesTecnicos() {
        return detallesTecnicos;
    }

    public boolean isExitoGeneral() {
        return exitoGeneral;
    }
}
