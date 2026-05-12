package minic.app;

import minic.codegen.ErrorGeneracionCodigo;
import minic.frontend.lex.AnalizadorLexico;
import minic.frontend.lex.ErrorLexico;
import minic.frontend.lex.Token;
import minic.frontend.parse.ErrorSintactico;
import minic.frontend.parse.Parser;
import minic.frontend.semantics.AdvertenciaSemantica;
import minic.frontend.semantics.AnalizadorSemantico;
import minic.frontend.semantics.ErrorSemantico;
import minic.gda.GDA;
import minic.pipeline.PipelineCompilacion;
import minic.pipeline.ResultadoCompilacion;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) throws IOException {
        String fuente;

        if (args.length > 0) {
            Path archivo = Paths.get(args[0]);
            fuente = new String(Files.readAllBytes(archivo), StandardCharsets.UTF_8);
            System.out.println("Archivo analizado: " + archivo.toAbsolutePath());
        } else {
            fuente = ejemploDeEntrada();
            System.out.println("Sin archivo de entrada. Se analiza un ejemplo interno.");
        }

        AnalizadorLexico analizador = new AnalizadorLexico(fuente);
        List<Token> tokens = analizador.analizar();
        List<ErrorLexico> errores = analizador.getErrores();

        imprimirTokens(tokens);
        imprimirErrores(errores);

        Parser parser = new Parser(tokens);
        parser.analizar();

        AnalizadorSemantico semantico = null;
        if (errores.isEmpty() && parser.getErrores().isEmpty()) {
            semantico = new AnalizadorSemantico(parser.getArbolAbstracto().getRaiz());
            semantico.analizar();
        }

        ResultadoCompilacion codigo = null;
        if (semantico != null && semantico.getErrores().isEmpty()) {
            PipelineCompilacion pipeline = new PipelineCompilacion(
                    parser.getArbolAbstracto().getRaiz(),
                    semantico.getTablaSimbolos(),
                    semantico.getErrores());
            codigo = pipeline.ejecutar();
        }

        imprimirSintactico(parser);
        imprimirArboles(parser);
        imprimirArchivosGenerados(parser);
        imprimirSemantico(semantico, errores.isEmpty() && parser.getErrores().isEmpty());
        imprimirCodigo(codigo, semantico);
        imprimirResumen(tokens, errores, parser.getErrores(), semantico, codigo);
    }

    private static void imprimirTokens(List<Token> tokens) {
        System.out.println();
        System.out.println("TOKENS:");
        for (Token token : tokens) {
            System.out.println(token);
        }
    }

    private static void imprimirErrores(List<ErrorLexico> errores) {
        System.out.println();
        System.out.println("ERRORES LEXICOS:");
        if (errores.isEmpty()) {
            System.out.println("No se encontraron errores lexicos.");
            return;
        }

        for (ErrorLexico error : errores) {
            System.out.println(error);
        }
    }

    private static void imprimirSintactico(Parser parser) {
        System.out.println();
        System.out.println("ANALISIS SINTACTICO:");
        if (parser.getErrores().isEmpty()) {
            System.out.println("Correcto. No se encontraron errores sintacticos.");
            return;
        }

        System.out.println("Se encontraron errores sintacticos:");
        for (ErrorSintactico error : parser.getErrores()) {
            System.out.println(error);
        }
    }

    private static void imprimirArboles(Parser parser) {
        System.out.println();
        System.out.println("ARBOL CONCRETO:");
        System.out.print(parser.getArbolConcreto().comoTexto());

        System.out.println();
        System.out.println("AST:");
        System.out.print(parser.getArbolAbstracto().comoTexto());
    }

    private static void imprimirArchivosGenerados(Parser parser) {
        System.out.println();
        System.out.println("ARCHIVOS GENERADOS:");
        for (String archivo : parser.getArchivosGenerados()) {
            System.out.println("- " + archivo);
        }
    }

    private static void imprimirSemantico(AnalizadorSemantico semantico, boolean puedeEjecutarse) {
        System.out.println();
        System.out.println("ANALISIS SEMANTICO:");
        if (!puedeEjecutarse || semantico == null) {
            System.out.println("No ejecutado porque existen errores lexicos o sintacticos previos.");
            return;
        }

        if (semantico.getErrores().isEmpty()) {
            System.out.println("Correcto. No se encontraron errores semanticos.");
        } else {
            System.out.println("Se encontraron errores semanticos.");
        }

        System.out.println();
        System.out.println("TABLA DE SIMBOLOS:");
        System.out.print(semantico.getTablaSimbolos().comoTexto());

        System.out.println();
        System.out.println("ERRORES SEMANTICOS:");
        if (semantico.getErrores().isEmpty()) {
            System.out.println("No se encontraron errores semanticos.");
        } else {
            for (ErrorSemantico error : semantico.getErrores()) {
                System.out.println(error);
            }
        }

        System.out.println();
        System.out.println("ADVERTENCIAS SEMANTICAS:");
        if (semantico.getAdvertencias().isEmpty()) {
            System.out.println("No se encontraron advertencias semanticas.");
        } else {
            for (AdvertenciaSemantica advertencia : semantico.getAdvertencias()) {
                System.out.println(advertencia);
            }
        }
    }

    private static void imprimirCodigo(ResultadoCompilacion codigo, AnalizadorSemantico semantico) {
        System.out.println();
        System.out.println("GENERACION Y OPTIMIZACION DE CODIGO:");
        if (semantico == null) {
            System.out.println("No ejecutada porque existen errores lexicos o sintacticos previos.");
            return;
        }
        if (!semantico.getErrores().isEmpty()) {
            System.out.println("Detenida porque existen errores semanticos previos.");
            return;
        }
        if (codigo == null) {
            System.out.println("No ejecutada.");
            return;
        }
        if (!codigo.getErrores().isEmpty()) {
            System.out.println("Se encontraron errores de generacion de codigo:");
            for (ErrorGeneracionCodigo error : codigo.getErrores()) {
                System.out.println(error);
            }
            return;
        }

        System.out.println();
        System.out.println("CODIGO DE TRES DIRECCIONES INICIAL:");
        System.out.print(codigo.getCodigoInicial().comoTexto());

        System.out.println();
        System.out.println("BLOQUES BASICOS Y CFG:");
        System.out.print(codigo.getCfgInicial().comoTexto());

        System.out.println();
        System.out.println("GDA POR BLOQUE:");
        if (codigo.getGdas().isEmpty()) {
            System.out.println("(sin GDA)");
        } else {
            for (GDA gda : codigo.getGdas()) {
                System.out.print(gda.comoTexto());
            }
        }

        System.out.println();
        System.out.println("ANALISIS DE VARIABLES VIVAS:");
        System.out.print(codigo.getAnalisisVariablesVivas());

        System.out.println();
        System.out.println("OPTIMIZACIONES APLICADAS:");
        if (codigo.getOptimizaciones().isEmpty()) {
            System.out.println("No se aplicaron optimizaciones.");
        } else {
            for (String optimizacion : codigo.getOptimizaciones()) {
                System.out.println("- " + optimizacion);
            }
        }

        System.out.println();
        System.out.println("CODIGO DE TRES DIRECCIONES OPTIMIZADO:");
        System.out.print(codigo.getCodigoOptimizado().comoTexto());

        System.out.println();
        System.out.println("CODIGO OBJETO FINAL:");
        System.out.print(codigo.getCodigoObjeto().comoTexto());

        System.out.println();
        System.out.println("ARCHIVOS DE GRAFOS GENERADOS:");
        if (codigo.getArchivosGenerados().isEmpty()) {
            System.out.println("No se generaron archivos de grafos.");
        } else {
            for (String archivo : codigo.getArchivosGenerados()) {
                System.out.println("- " + archivo);
            }
        }
    }

    private static void imprimirResumen(List<Token> tokens, List<ErrorLexico> erroresLexicos,
            List<ErrorSintactico> erroresSintacticos, AnalizadorSemantico semantico,
            ResultadoCompilacion codigo) {
        System.out.println();
        if (erroresLexicos.isEmpty()) {
            System.out.println("Analisis lexico finalizado correctamente: no se encontraron errores lexicos.");
        } else {
            System.out.println("Analisis lexico finalizado con errores: "
                    + erroresLexicos.size()
                    + " errores lexicos encontrados.");
        }
        if (erroresSintacticos.isEmpty()) {
            System.out.println("Analisis sintactico finalizado correctamente: no se encontraron errores sintacticos.");
        } else {
            System.out.println("Analisis sintactico finalizado con errores: "
                    + erroresSintacticos.size()
                    + " errores sintacticos encontrados.");
        }
        if (semantico == null) {
            System.out.println("Analisis semantico no ejecutado por errores previos.");
        } else if (semantico.getErrores().isEmpty()) {
            System.out.println("Analisis semantico finalizado correctamente: no se encontraron errores semanticos.");
        } else {
            System.out.println("Analisis semantico finalizado con errores: "
                    + semantico.getErrores().size()
                    + " errores semanticos encontrados.");
        }
        if (codigo == null) {
            System.out.println("Generacion de codigo no ejecutada por errores previos.");
        } else if (codigo.getErrores().isEmpty()) {
            System.out.println("Generacion y optimizacion de codigo finalizada correctamente.");
        } else {
            System.out.println("Generacion de codigo finalizada con errores internos: "
                    + codigo.getErrores().size()
                    + " errores encontrados.");
        }
        System.out.println("Tokens reconocidos: " + tokens.size());
    }

    private static String ejemploDeEntrada() {
        String salto = System.lineSeparator();
        return String.join(salto,
                "num edad = 18;",
                "flotar promedio = 4.5;",
                "letra inicial = 'P';",
                "",
                "valdt (edad >= 18 && promedio >= 3.0) {",
                "    estamp(\"Estudiante valido\", inicial);",
                "}",
                "",
                "ciclar (num i = 0; i < 5; i++) {",
                "    estamp(i);",
                "}",
                "",
                "recolt(edad);",
                "edad = edad + 1;"
        );
    }
}
