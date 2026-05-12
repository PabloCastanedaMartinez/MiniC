import java.util.List;

public final class PipelineCompilacion {
    private final NodoAST ast;
    private final TablaSimbolos tablaSimbolos;
    private final List<ErrorSemantico> erroresSemanticos;

    public PipelineCompilacion(NodoAST ast, TablaSimbolos tablaSimbolos,
            List<ErrorSemantico> erroresSemanticos) {
        this.ast = ast;
        this.tablaSimbolos = tablaSimbolos;
        this.erroresSemanticos = erroresSemanticos;
    }

    public ResultadoCompilacion ejecutar() {
        ResultadoCompilacion resultado = new ResultadoCompilacion();
        if (erroresSemanticos != null && !erroresSemanticos.isEmpty()) {
            resultado.setDetenidoPorErroresPrevios(true);
            return resultado;
        }

        GeneradorCodigoIntermedio generador = new GeneradorCodigoIntermedio();
        CodigoIntermedio codigoInicial = generador.generar(ast, tablaSimbolos);
        resultado.setCodigoInicial(codigoInicial);
        resultado.agregarErrores(generador.getErrores());
        if (!generador.getErrores().isEmpty()) {
            return resultado;
        }

        ConstructorBloquesBasicos constructorBloques = new ConstructorBloquesBasicos();
        ConstructorCFG constructorCFG = new ConstructorCFG();
        GrafoFlujoControl cfgInicial = constructorCFG.construir(constructorBloques.construir(codigoInicial));
        resultado.setCfgInicial(cfgInicial);

        AnalisisVariablesVivas analisis = new AnalisisVariablesVivas();
        analisis.analizar(cfgInicial);
        resultado.setAnalisisVariablesVivas(analisis.comoTexto(cfgInicial));

        ConstructorGDA constructorGDA = new ConstructorGDA();
        for (BloqueBasico bloque : cfgInicial.getBloques()) {
            resultado.agregarGDA(constructorGDA.construir(bloque));
        }

        OptimizadorCodigoIntermedio optimizador = new OptimizadorCodigoIntermedio();
        ResultadoOptimizacion optimizacion = optimizador.optimizar(codigoInicial);
        resultado.setCodigoOptimizado(optimizacion.getCodigoOptimizado());
        resultado.agregarOptimizaciones(optimizacion.getOptimizaciones());

        GrafoFlujoControl cfgOptimizado = constructorCFG.construir(
                constructorBloques.construir(resultado.getCodigoOptimizado()));
        resultado.setCfgOptimizado(cfgOptimizado);

        GeneradorCodigoObjeto generadorObjeto = new GeneradorCodigoObjeto();
        resultado.setCodigoObjeto(generadorObjeto.generar(resultado.getCodigoOptimizado()));

        ExportadorDot exportador = new ExportadorDot();
        resultado.agregarArchivos(exportador.exportar(cfgInicial, resultado.getGdas()));
        return resultado;
    }
}
