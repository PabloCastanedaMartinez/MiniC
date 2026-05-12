package minic.ui.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ResultadoFase {
    private final String nombreFase;
    private final EstadoFase estado;
    private final String mensaje;
    private final List<String> detalles;

    public ResultadoFase(String nombreFase, EstadoFase estado, String mensaje, List<String> detalles) {
        this.nombreFase = nombreFase == null ? "" : nombreFase;
        this.estado = estado == null ? EstadoFase.NO_EJECUTADO : estado;
        this.mensaje = mensaje == null ? "" : mensaje;
        this.detalles = new ArrayList<String>();
        if (detalles != null) {
            this.detalles.addAll(detalles);
        }
    }

    public String getNombreFase() {
        return nombreFase;
    }

    public EstadoFase getEstado() {
        return estado;
    }

    public String getMensaje() {
        return mensaje;
    }

    public List<String> getDetalles() {
        return Collections.unmodifiableList(detalles);
    }
}
