import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ResultadoCompilacion {
    private boolean detenidoPorErroresPrevios;
    private CodigoIntermedio codigoInicial = new CodigoIntermedio();
    private CodigoIntermedio codigoOptimizado = new CodigoIntermedio();
    private GrafoFlujoControl cfgInicial = new GrafoFlujoControl(null);
    private GrafoFlujoControl cfgOptimizado = new GrafoFlujoControl(null);
    private final List<GDA> gdas = new ArrayList<GDA>();
    private String analisisVariablesVivas = "";
    private final List<String> optimizaciones = new ArrayList<String>();
    private CodigoObjeto codigoObjeto = new CodigoObjeto();
    private final List<ErrorGeneracionCodigo> errores = new ArrayList<ErrorGeneracionCodigo>();
    private final List<String> archivosGenerados = new ArrayList<String>();

    public boolean isDetenidoPorErroresPrevios() {
        return detenidoPorErroresPrevios;
    }

    public void setDetenidoPorErroresPrevios(boolean detenidoPorErroresPrevios) {
        this.detenidoPorErroresPrevios = detenidoPorErroresPrevios;
    }

    public CodigoIntermedio getCodigoInicial() {
        return codigoInicial;
    }

    public void setCodigoInicial(CodigoIntermedio codigoInicial) {
        this.codigoInicial = codigoInicial == null ? new CodigoIntermedio() : codigoInicial;
    }

    public CodigoIntermedio getCodigoOptimizado() {
        return codigoOptimizado;
    }

    public void setCodigoOptimizado(CodigoIntermedio codigoOptimizado) {
        this.codigoOptimizado = codigoOptimizado == null ? new CodigoIntermedio() : codigoOptimizado;
    }

    public GrafoFlujoControl getCfgInicial() {
        return cfgInicial;
    }

    public void setCfgInicial(GrafoFlujoControl cfgInicial) {
        this.cfgInicial = cfgInicial == null ? new GrafoFlujoControl(null) : cfgInicial;
    }

    public GrafoFlujoControl getCfgOptimizado() {
        return cfgOptimizado;
    }

    public void setCfgOptimizado(GrafoFlujoControl cfgOptimizado) {
        this.cfgOptimizado = cfgOptimizado == null ? new GrafoFlujoControl(null) : cfgOptimizado;
    }

    public List<GDA> getGdas() {
        return Collections.unmodifiableList(gdas);
    }

    public void agregarGDA(GDA gda) {
        if (gda != null) {
            gdas.add(gda);
        }
    }

    public String getAnalisisVariablesVivas() {
        return analisisVariablesVivas;
    }

    public void setAnalisisVariablesVivas(String analisisVariablesVivas) {
        this.analisisVariablesVivas = analisisVariablesVivas == null ? "" : analisisVariablesVivas;
    }

    public List<String> getOptimizaciones() {
        return Collections.unmodifiableList(optimizaciones);
    }

    public void agregarOptimizaciones(List<String> optimizaciones) {
        if (optimizaciones != null) {
            this.optimizaciones.addAll(optimizaciones);
        }
    }

    public CodigoObjeto getCodigoObjeto() {
        return codigoObjeto;
    }

    public void setCodigoObjeto(CodigoObjeto codigoObjeto) {
        this.codigoObjeto = codigoObjeto == null ? new CodigoObjeto() : codigoObjeto;
    }

    public List<ErrorGeneracionCodigo> getErrores() {
        return Collections.unmodifiableList(errores);
    }

    public void agregarErrores(List<ErrorGeneracionCodigo> errores) {
        if (errores != null) {
            this.errores.addAll(errores);
        }
    }

    public List<String> getArchivosGenerados() {
        return Collections.unmodifiableList(archivosGenerados);
    }

    public void agregarArchivos(List<String> archivos) {
        if (archivos != null) {
            archivosGenerados.addAll(archivos);
        }
    }
}
