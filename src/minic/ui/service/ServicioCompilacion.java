package minic.ui.service;

import minic.codegen.ErrorGeneracionCodigo;
import minic.frontend.lex.AnalizadorLexico;
import minic.frontend.lex.ErrorLexico;
import minic.frontend.lex.Token;
import minic.frontend.parse.ErrorSintactico;
import minic.frontend.parse.Parser;
import minic.frontend.semantics.AdvertenciaSemantica;
import minic.frontend.semantics.AnalizadorSemantico;
import minic.frontend.semantics.ErrorSemantico;
import minic.pipeline.PipelineCompilacion;
import minic.runtime.ResultadoEjecucion;
import minic.runtime.SimuladorPrograma;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ServicioCompilacion {
    public static final String FASE_LEXICO = "Analisis Lexico";
    public static final String FASE_SINTACTICO = "Analisis Sintactico";
    public static final String FASE_SEMANTICO = "Analisis Semantico";
    public static final String FASE_GENERACION = "Generacion / Optimizacion";
    public static final String FASE_EJECUCION = "Ejecucion / Simulacion";

    private static final List<String> NOMBRES_FASES = Collections.unmodifiableList(Arrays.asList(
            FASE_LEXICO,
            FASE_SINTACTICO,
            FASE_SEMANTICO,
            FASE_GENERACION,
            FASE_EJECUCION
    ));

    private final ObservadorProgreso observador;

    public ServicioCompilacion() {
        this(null);
    }

    public ServicioCompilacion(ObservadorProgreso observador) {
        this.observador = observador;
    }

    public static List<String> nombresFases() {
        return NOMBRES_FASES;
    }

    public ResultadoCompilacionUI compilar(String fuente) {
        EstadoCompilacion estado = new EstadoCompilacion();
        String codigoFuente = fuente == null ? "" : fuente;
        StringBuilder detalles = new StringBuilder();

        AnalizadorLexico lexer;
        List<Token> tokens;
        Parser parser;
        AnalizadorSemantico semantico;

        try {
            estado.actualizar(FASE_LEXICO, EstadoFase.EN_PROCESO, "Analizando tokens.", null);
            lexer = new AnalizadorLexico(codigoFuente);
            tokens = lexer.analizar();
            if (!lexer.getErrores().isEmpty()) {
                estado.actualizar(FASE_LEXICO, EstadoFase.ERROR,
                        "Se encontraron " + lexer.getErrores().size() + " errores lexicos.",
                        erroresLexicos(lexer.getErrores()));
                estado.marcarPendientes("No ejecutado por errores lexicos previos.");
                return estado.resultado(salidaErrores(lexer.getErrores()), detalles.toString(), false);
            }
            estado.actualizar(FASE_LEXICO, EstadoFase.CORRECTO,
                    "Correcto. Tokens reconocidos: " + tokens.size() + ".", null);
            detalles.append("TOKENS RECONOCIDOS: ").append(tokens.size()).append(System.lineSeparator());
        } catch (RuntimeException ex) {
            estado.actualizar(FASE_LEXICO, EstadoFase.ERROR, "Fallo interno en el analisis lexico.",
                    lista(ex.getMessage()));
            estado.marcarPendientes("No ejecutado por fallo lexico.");
            return estado.resultado("Error lexico: " + ex.getMessage(), detalles.toString(), false);
        }

        try {
            estado.actualizar(FASE_SINTACTICO, EstadoFase.EN_PROCESO, "Validando estructura del programa.", null);
            parser = new Parser(tokens);
            parser.analizar();
            if (!parser.getErrores().isEmpty()) {
                estado.actualizar(FASE_SINTACTICO, EstadoFase.ERROR,
                        "Se encontraron " + parser.getErrores().size() + " errores sintacticos.",
                        erroresSintacticos(parser.getErrores()));
                estado.marcarPendientes("No ejecutado por errores sintacticos previos.");
                return estado.resultado(salidaErrores(parser.getErrores()), detalles.toString(), false);
            }
            estado.actualizar(FASE_SINTACTICO, EstadoFase.CORRECTO,
                    "Correcto. Programa sintacticamente valido.", null);
        } catch (RuntimeException ex) {
            estado.actualizar(FASE_SINTACTICO, EstadoFase.ERROR, "Fallo interno en el analisis sintactico.",
                    lista(ex.getMessage()));
            estado.marcarPendientes("No ejecutado por fallo sintactico.");
            return estado.resultado("Error sintactico: " + ex.getMessage(), detalles.toString(), false);
        }

        try {
            estado.actualizar(FASE_SEMANTICO, EstadoFase.EN_PROCESO, "Validando tipos, ambitos y usos.", null);
            semantico = new AnalizadorSemantico(parser.getArbolAbstracto().getRaiz());
            semantico.analizar();
            if (!semantico.getErrores().isEmpty()) {
                estado.actualizar(FASE_SEMANTICO, EstadoFase.ERROR,
                        "Se encontraron " + semantico.getErrores().size() + " errores semanticos.",
                        erroresSemanticos(semantico.getErrores()));
                estado.marcarPendientes("No ejecutado por errores semanticos previos.");
                return estado.resultado(salidaErrores(semantico.getErrores()), detalles.toString(), false);
            }
            List<String> detallesSemanticos = advertencias(semantico.getAdvertencias());
            estado.actualizar(FASE_SEMANTICO, EstadoFase.CORRECTO,
                    mensajeSemantico(semantico.getAdvertencias().size()), detallesSemanticos);
            detalles.append(System.lineSeparator()).append("TABLA DE SIMBOLOS:")
                    .append(System.lineSeparator())
                    .append(semantico.getTablaSimbolos().comoTexto());
        } catch (RuntimeException ex) {
            estado.actualizar(FASE_SEMANTICO, EstadoFase.ERROR, "Fallo interno en el analisis semantico.",
                    lista(ex.getMessage()));
            estado.marcarPendientes("No ejecutado por fallo semantico.");
            return estado.resultado("Error semantico: " + ex.getMessage(), detalles.toString(), false);
        }

        try {
            estado.actualizar(FASE_GENERACION, EstadoFase.EN_PROCESO,
                    "Generando TAC, optimizando y produciendo codigo objeto.", null);
            PipelineCompilacion pipeline = new PipelineCompilacion(
                    parser.getArbolAbstracto().getRaiz(),
                    semantico.getTablaSimbolos(),
                    semantico.getErrores());
            minic.pipeline.ResultadoCompilacion codigo = pipeline.ejecutar();
            if (!codigo.getErrores().isEmpty()) {
                estado.actualizar(FASE_GENERACION, EstadoFase.ERROR,
                        "Se encontraron " + codigo.getErrores().size() + " errores de generacion.",
                        erroresGeneracion(codigo.getErrores()));
                estado.marcarPendientes("No ejecutado por errores de generacion previos.");
                return estado.resultado(salidaErrores(codigo.getErrores()), detalles.toString(), false);
            }
            estado.actualizar(FASE_GENERACION, EstadoFase.CORRECTO,
                    "Correcto. Optimizaciones aplicadas: " + codigo.getOptimizaciones().size() + ".", null);
            agregarDetallesCodigo(detalles, codigo);
        } catch (RuntimeException ex) {
            estado.actualizar(FASE_GENERACION, EstadoFase.ERROR, "Fallo interno en generacion de codigo.",
                    lista(ex.getMessage()));
            estado.marcarPendientes("No ejecutado por fallo de generacion.");
            return estado.resultado("Error de generacion: " + ex.getMessage(), detalles.toString(), false);
        }

        try {
            estado.actualizar(FASE_EJECUCION, EstadoFase.EN_PROCESO,
                    "Simulando la salida observable del programa.", null);
            ResultadoEjecucion ejecucion = new SimuladorPrograma().ejecutar(parser.getArbolAbstracto().getRaiz());
            if (!ejecucion.isExitoso()) {
                estado.actualizar(FASE_EJECUCION, EstadoFase.ERROR,
                        "La simulacion encontro errores.", ejecucion.getErrores());
                return estado.resultado(salidaErrores(ejecucion.getErrores()), detalles.toString(), false);
            }
            estado.actualizar(FASE_EJECUCION, EstadoFase.CORRECTO,
                    "Correcto. Salida producida por 'estamp'.", null);
            return estado.resultado(ejecucion.getSalida(), detalles.toString(), true);
        } catch (RuntimeException ex) {
            estado.actualizar(FASE_EJECUCION, EstadoFase.ERROR, "Fallo interno en la simulacion.",
                    lista(ex.getMessage()));
            return estado.resultado("Error de ejecucion: " + ex.getMessage(), detalles.toString(), false);
        }
    }

    private String mensajeSemantico(int advertencias) {
        if (advertencias == 0) {
            return "Correcto. No se encontraron errores semanticos.";
        }
        return "Correcto con " + advertencias + " advertencias.";
    }

    private void agregarDetallesCodigo(StringBuilder detalles, minic.pipeline.ResultadoCompilacion codigo) {
        detalles.append(System.lineSeparator()).append("CODIGO DE TRES DIRECCIONES OPTIMIZADO:")
                .append(System.lineSeparator())
                .append(codigo.getCodigoOptimizado().comoTexto());
        detalles.append(System.lineSeparator()).append("CODIGO OBJETO:")
                .append(System.lineSeparator())
                .append(codigo.getCodigoObjeto().comoTexto());
    }

    private List<String> erroresLexicos(List<ErrorLexico> errores) {
        List<String> salida = new ArrayList<String>();
        for (ErrorLexico error : errores) {
            salida.add(error.toString());
        }
        return salida;
    }

    private List<String> erroresSintacticos(List<ErrorSintactico> errores) {
        List<String> salida = new ArrayList<String>();
        for (ErrorSintactico error : errores) {
            salida.add(error.toString());
        }
        return salida;
    }

    private List<String> erroresSemanticos(List<ErrorSemantico> errores) {
        List<String> salida = new ArrayList<String>();
        for (ErrorSemantico error : errores) {
            salida.add(error.toString());
        }
        return salida;
    }

    private List<String> erroresGeneracion(List<ErrorGeneracionCodigo> errores) {
        List<String> salida = new ArrayList<String>();
        for (ErrorGeneracionCodigo error : errores) {
            salida.add(error.toString());
        }
        return salida;
    }

    private List<String> advertencias(List<AdvertenciaSemantica> advertencias) {
        List<String> salida = new ArrayList<String>();
        for (AdvertenciaSemantica advertencia : advertencias) {
            salida.add(advertencia.toString());
        }
        return salida;
    }

    private String salidaErrores(List<?> errores) {
        StringBuilder salida = new StringBuilder();
        for (Object error : errores) {
            salida.append(error).append(System.lineSeparator());
        }
        return salida.toString();
    }

    private List<String> lista(String valor) {
        List<String> salida = new ArrayList<String>();
        salida.add(valor == null ? "" : valor);
        return salida;
    }

    private final class EstadoCompilacion {
        private final Map<String, ResultadoFase> fases = new LinkedHashMap<String, ResultadoFase>();

        private EstadoCompilacion() {
            for (String fase : NOMBRES_FASES) {
                fases.put(fase, new ResultadoFase(fase, EstadoFase.NO_EJECUTADO, "Pendiente.", null));
            }
        }

        private void actualizar(String fase, EstadoFase estado, String mensaje, List<String> detalles) {
            ResultadoFase resultado = new ResultadoFase(fase, estado, mensaje, detalles);
            fases.put(fase, resultado);
            if (observador != null) {
                observador.faseActualizada(resultado);
            }
        }

        private void marcarPendientes(String mensaje) {
            for (String fase : NOMBRES_FASES) {
                ResultadoFase actual = fases.get(fase);
                if (actual.getEstado() == EstadoFase.NO_EJECUTADO || actual.getEstado() == EstadoFase.EN_PROCESO) {
                    actualizar(fase, EstadoFase.NO_EJECUTADO, mensaje, null);
                }
            }
        }

        private ResultadoCompilacionUI resultado(String salidaFinal, String detallesTecnicos, boolean exito) {
            return new ResultadoCompilacionUI(new ArrayList<ResultadoFase>(fases.values()),
                    salidaFinal, detallesTecnicos, exito);
        }
    }
}
