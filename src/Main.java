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

        imprimirSintactico(parser);
        imprimirArboles(parser);
        imprimirArchivosGenerados(parser);
        imprimirResumen(tokens, errores, parser.getErrores());
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

    private static void imprimirResumen(List<Token> tokens, List<ErrorLexico> erroresLexicos,
            List<ErrorSintactico> erroresSintacticos) {
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
