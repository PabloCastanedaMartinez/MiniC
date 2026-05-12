package minic.analysis;

import minic.ir.CodigoIntermedio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ResultadoOptimizacion {
    private final CodigoIntermedio codigoOptimizado;
    private final List<String> optimizaciones;

    public ResultadoOptimizacion(CodigoIntermedio codigoOptimizado, List<String> optimizaciones) {
        this.codigoOptimizado = codigoOptimizado == null ? new CodigoIntermedio() : codigoOptimizado;
        this.optimizaciones = new ArrayList<String>();
        if (optimizaciones != null) {
            this.optimizaciones.addAll(optimizaciones);
        }
    }

    public CodigoIntermedio getCodigoOptimizado() {
        return codigoOptimizado;
    }

    public List<String> getOptimizaciones() {
        return Collections.unmodifiableList(optimizaciones);
    }

    public String optimizacionesComoTexto() {
        StringBuilder salida = new StringBuilder();
        if (optimizaciones.isEmpty()) {
            salida.append("No se aplicaron optimizaciones.").append(System.lineSeparator());
            return salida.toString();
        }
        for (String optimizacion : optimizaciones) {
            salida.append("- ").append(optimizacion).append(System.lineSeparator());
        }
        return salida.toString();
    }
}
