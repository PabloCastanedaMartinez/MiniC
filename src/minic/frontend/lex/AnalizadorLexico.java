package minic.frontend.lex;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class AnalizadorLexico {
    private static final Map<String, TokenInfo> PALABRAS_RESERVADAS = crearPalabrasReservadas();

    private final String fuente;
    private final List<Token> tokens = new ArrayList<Token>();
    private final List<ErrorLexico> errores = new ArrayList<ErrorLexico>();

    private int inicio;
    private int actual;
    private int linea;
    private int columna;

    public AnalizadorLexico(String fuente) {
        this.fuente = fuente == null ? "" : fuente;
    }

    public List<Token> analizar() {
        tokens.clear();
        errores.clear();
        inicio = 0;
        actual = 0;
        linea = 1;
        columna = 1;

        while (!estaAlFinal()) {
            inicio = actual;
            int lineaInicio = linea;
            int columnaInicio = columna;
            escanearToken(lineaInicio, columnaInicio);
        }

        tokens.add(new Token(TokenType.EOF, "", null, linea, columna));
        return getTokens();
    }

    public List<Token> getTokens() {
        return Collections.unmodifiableList(tokens);
    }

    public List<ErrorLexico> getErrores() {
        return Collections.unmodifiableList(errores);
    }

    private void escanearToken(int lineaInicio, int columnaInicio) {
        char c = avanzar();

        switch (c) {
            case ' ':
            case '\r':
            case '\t':
                return;
            case '\n':
                return;
            case '(':
                agregarToken(TokenType.PAREN_IZQ, "parentesis izquierdo", lineaInicio, columnaInicio);
                return;
            case ')':
                agregarToken(TokenType.PAREN_DER, "parentesis derecho", lineaInicio, columnaInicio);
                return;
            case '{':
                agregarToken(TokenType.LLAVE_IZQ, "llave izquierda", lineaInicio, columnaInicio);
                return;
            case '}':
                agregarToken(TokenType.LLAVE_DER, "llave derecha", lineaInicio, columnaInicio);
                return;
            case ';':
                agregarToken(TokenType.PUNTO_COMA, "punto y coma", lineaInicio, columnaInicio);
                return;
            case ',':
                agregarToken(TokenType.COMA, "coma", lineaInicio, columnaInicio);
                return;
            case '+':
                if (coincidir('+')) {
                    agregarToken(TokenType.OP_INC, "incremento", lineaInicio, columnaInicio);
                } else {
                    agregarToken(TokenType.OP_ARIT, "aritmetico", lineaInicio, columnaInicio);
                }
                return;
            case '-':
                if (coincidir('-')) {
                    agregarToken(TokenType.OP_DEC, "decremento", lineaInicio, columnaInicio);
                } else {
                    agregarToken(TokenType.OP_ARIT, "aritmetico", lineaInicio, columnaInicio);
                }
                return;
            case '*':
            case '/':
            case '%':
                agregarToken(TokenType.OP_ARIT, "aritmetico", lineaInicio, columnaInicio);
                return;
            case '=':
                if (coincidir('=')) {
                    agregarToken(TokenType.OP_COMP, "comparativo", lineaInicio, columnaInicio);
                } else {
                    agregarToken(TokenType.OP_ASIG, "asignacion", lineaInicio, columnaInicio);
                }
                return;
            case '>':
                if (coincidir('=')) {
                    agregarToken(TokenType.OP_COMP, "comparativo", lineaInicio, columnaInicio);
                } else {
                    agregarToken(TokenType.OP_COMP, "comparativo", lineaInicio, columnaInicio);
                }
                return;
            case '<':
                if (coincidir('=') || coincidir('>')) {
                    agregarToken(TokenType.OP_COMP, "comparativo", lineaInicio, columnaInicio);
                } else {
                    agregarToken(TokenType.OP_COMP, "comparativo", lineaInicio, columnaInicio);
                }
                return;
            case '!':
                if (coincidir('=')) {
                    agregarToken(TokenType.OP_COMP, "comparativo", lineaInicio, columnaInicio);
                } else {
                    agregarError(lexemaActual(), lineaInicio, columnaInicio, "operador incompleto; se esperaba '!='");
                }
                return;
            case '&':
                if (coincidir('&')) {
                    agregarToken(TokenType.OP_Y, "AND logico", lineaInicio, columnaInicio);
                } else {
                    agregarError(lexemaActual(), lineaInicio, columnaInicio, "operador logico incompleto; se esperaba '&&'");
                }
                return;
            case '|':
                if (coincidir('|')) {
                    agregarToken(TokenType.OP_O, "OR logico", lineaInicio, columnaInicio);
                } else {
                    agregarError(lexemaActual(), lineaInicio, columnaInicio, "operador logico incompleto; se esperaba '||'");
                }
                return;
            case '"':
                leerCadena(lineaInicio, columnaInicio);
                return;
            case '\'':
                leerCaracter(lineaInicio, columnaInicio);
                return;
            default:
                if (esDigito(c)) {
                    leerNumero(lineaInicio, columnaInicio);
                } else if (esInicioIdentificador(c)) {
                    leerIdentificadorOPalabraReservada(lineaInicio, columnaInicio);
                } else {
                    agregarError(lexemaActual(), lineaInicio, columnaInicio, "caracter no reconocido");
                }
        }
    }

    private void leerIdentificadorOPalabraReservada(int lineaInicio, int columnaInicio) {
        while (esParteIdentificador(ver())) {
            avanzar();
        }

        String lexema = lexemaActual();
        TokenInfo palabraReservada = PALABRAS_RESERVADAS.get(lexema);
        if (palabraReservada != null) {
            agregarToken(palabraReservada.tipo, palabraReservada.atributo, lineaInicio, columnaInicio);
        } else {
            agregarToken(TokenType.ID, "identificador", lineaInicio, columnaInicio);
        }
    }

    private void leerNumero(int lineaInicio, int columnaInicio) {
        while (esDigito(ver())) {
            avanzar();
        }

        boolean esFlotante = false;

        if (ver() == '.') {
            avanzar();
            if (!esDigito(ver())) {
                agregarError(lexemaActual(), lineaInicio, columnaInicio, "numero flotante incompleto");
                return;
            }

            esFlotante = true;
            while (esDigito(ver())) {
                avanzar();
            }
        }

        // Decision tomada del contexto: 123abc se reporta como un unico
        // identificador invalido iniciado con numero, no como ENTERO + ID.
        if (esInicioIdentificador(ver())) {
            while (esParteIdentificador(ver())) {
                avanzar();
            }
            agregarError(lexemaActual(), lineaInicio, columnaInicio, "identificador invalido iniciado con numero");
            return;
        }

        String lexema = lexemaActual();
        if (esFlotante) {
            agregarToken(TokenType.FLOTANTE_LITERAL, new BigDecimal(lexema), lineaInicio, columnaInicio);
        } else {
            agregarToken(TokenType.ENTERO_LITERAL, new BigInteger(lexema), lineaInicio, columnaInicio);
        }
    }

    private void leerCadena(int lineaInicio, int columnaInicio) {
        StringBuilder valor = new StringBuilder();
        String escapeInvalido = null;

        while (!estaAlFinal()) {
            char c = ver();

            if (c == '"') {
                avanzar();
                if (escapeInvalido != null) {
                    agregarError(lexemaActual(), lineaInicio, columnaInicio,
                            "secuencia de escape no soportada en cadena: \\" + escapeInvalido);
                    return;
                }
                agregarToken(TokenType.CADENA_LITERAL, valor.toString(), lineaInicio, columnaInicio);
                return;
            }

            if (c == '\n') {
                agregarError(lexemaActual(), lineaInicio, columnaInicio, "cadena sin cerrar");
                return;
            }

            if (c == '\\') {
                avanzar();
                if (estaAlFinal() || ver() == '\n') {
                    agregarError(lexemaActual(), lineaInicio, columnaInicio, "cadena sin cerrar");
                    return;
                }
                char escape = avanzar();
                Character decodificado = decodificarEscape(escape);
                if (decodificado == null) {
                    escapeInvalido = Character.toString(escape);
                } else {
                    valor.append(decodificado.charValue());
                }
            } else {
                valor.append(avanzar());
            }
        }

        agregarError(lexemaActual(), lineaInicio, columnaInicio, "cadena sin cerrar");
    }

    private void leerCaracter(int lineaInicio, int columnaInicio) {
        if (estaAlFinal() || ver() == '\n') {
            agregarError(lexemaActual(), lineaInicio, columnaInicio, "caracter sin cerrar");
            return;
        }

        if (ver() == '\'') {
            avanzar();
            agregarError(lexemaActual(), lineaInicio, columnaInicio, "caracter literal vacio");
            return;
        }

        Character valor = null;
        String escapeInvalido = null;

        if (ver() == '\\') {
            avanzar();
            if (estaAlFinal() || ver() == '\n') {
                agregarError(lexemaActual(), lineaInicio, columnaInicio, "caracter sin cerrar");
                return;
            }
            char escape = avanzar();
            valor = decodificarEscape(escape);
            if (valor == null) {
                escapeInvalido = Character.toString(escape);
            }
        } else {
            valor = Character.valueOf(avanzar());
        }

        if (ver() == '\'') {
            avanzar();
            if (escapeInvalido != null) {
                agregarError(lexemaActual(), lineaInicio, columnaInicio,
                        "secuencia de escape no soportada en caracter: \\" + escapeInvalido);
                return;
            }
            agregarToken(TokenType.CARACTER_LITERAL, valor, lineaInicio, columnaInicio);
            return;
        }

        recuperarCaracterInvalido();
        agregarError(lexemaActual(), lineaInicio, columnaInicio,
                "caracter literal invalido; debe contener exactamente un caracter o escape valido");
    }

    private void recuperarCaracterInvalido() {
        // El punto y coma se deja sin consumir para que pueda reconocerse
        // como delimitador despues de reportar el literal de caracter invalido.
        while (!estaAlFinal() && ver() != '\'' && ver() != '\n' && ver() != ';') {
            avanzar();
        }

        if (!estaAlFinal() && ver() == '\'') {
            avanzar();
        }
    }

    private Character decodificarEscape(char escape) {
        // El contexto menciona escapes como \n, \t, \", \\ y ejemplos de
        // caracter. Se acepta tambien \r y \' por consistencia con literales.
        switch (escape) {
            case 'n':
                return Character.valueOf('\n');
            case 't':
                return Character.valueOf('\t');
            case 'r':
                return Character.valueOf('\r');
            case '"':
                return Character.valueOf('"');
            case '\'':
                return Character.valueOf('\'');
            case '\\':
                return Character.valueOf('\\');
            default:
                return null;
        }
    }

    private void agregarToken(TokenType tipo, Object atributo, int lineaInicio, int columnaInicio) {
        tokens.add(new Token(tipo, lexemaActual(), atributo, lineaInicio, columnaInicio));
    }

    private void agregarError(String lexema, int lineaInicio, int columnaInicio, String descripcion) {
        errores.add(new ErrorLexico(lexema, lineaInicio, columnaInicio, descripcion));
    }

    private boolean coincidir(char esperado) {
        if (estaAlFinal()) {
            return false;
        }
        if (fuente.charAt(actual) != esperado) {
            return false;
        }
        avanzar();
        return true;
    }

    private char avanzar() {
        char c = fuente.charAt(actual);
        actual++;
        if (c == '\n') {
            linea++;
            columna = 1;
        } else {
            columna++;
        }
        return c;
    }

    private char ver() {
        if (estaAlFinal()) {
            return '\0';
        }
        return fuente.charAt(actual);
    }

    private boolean estaAlFinal() {
        return actual >= fuente.length();
    }

    private String lexemaActual() {
        return fuente.substring(inicio, actual);
    }

    private static boolean esDigito(char c) {
        return c >= '0' && c <= '9';
    }

    private static boolean esInicioIdentificador(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    private static boolean esParteIdentificador(char c) {
        return esInicioIdentificador(c) || esDigito(c);
    }

    private static Map<String, TokenInfo> crearPalabrasReservadas() {
        Map<String, TokenInfo> palabras = new HashMap<String, TokenInfo>();
        palabras.put("num", new TokenInfo(TokenType.NUMERO, "int"));
        palabras.put("letra", new TokenInfo(TokenType.CARACTER, "char"));
        palabras.put("flotar", new TokenInfo(TokenType.NUMERO, "float"));
        palabras.put("valdt", new TokenInfo(TokenType.VALIDAR, "if"));
        palabras.put("ciclar", new TokenInfo(TokenType.CICLO, "for"));
        palabras.put("estamp", new TokenInfo(TokenType.ESTAMPAR, "printf"));
        palabras.put("recolt", new TokenInfo(TokenType.RECOLECTAR, "scanf"));
        return palabras;
    }

    private static final class TokenInfo {
        private final TokenType tipo;
        private final String atributo;

        private TokenInfo(TokenType tipo, String atributo) {
            this.tipo = tipo;
            this.atributo = atributo;
        }
    }
}
