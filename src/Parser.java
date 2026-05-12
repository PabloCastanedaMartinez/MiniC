import java.awt.Color;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Parser {
    private final List<Token> tokens;
    private final List<ErrorSintactico> errores = new ArrayList<ErrorSintactico>();

    private int actual;
    private ArbolConcreto arbolConcreto;
    private ArbolAbstracto arbolAbstracto;
    private VisualizadorArbol visualizadorConcreto;
    private VisualizadorArbol visualizadorAst;

    public Parser(List<Token> tokens) {
        this.tokens = new ArrayList<Token>();
        if (tokens != null) {
            this.tokens.addAll(tokens);
        }
        if (this.tokens.isEmpty() || this.tokens.get(this.tokens.size() - 1).getTipo() != TokenType.EOF) {
            this.tokens.add(new Token(TokenType.EOF, "", null, 1, 1));
        }
    }

    public boolean analizar() {
        errores.clear();
        actual = 0;

        NodoArbol raizConcreta = new NodoArbol("Programa");
        NodoAST raizAst = new NodoAST("Programa");
        arbolConcreto = new ArbolConcreto(raizConcreta);
        arbolAbstracto = new ArbolAbstracto(raizAst);

        visualizadorConcreto = new VisualizadorArbol(
                Paths.get("arbol_concreto.dot"),
                Paths.get("arbol_concreto.png"),
                "ArbolConcreto",
                new Color(219, 234, 254)
        );
        visualizadorAst = new VisualizadorArbol(
                Paths.get("ast.dot"),
                Paths.get("ast.png"),
                "AST",
                new Color(220, 252, 231)
        );

        actualizarConcreto();
        actualizarAst();

        NodoArbol listaSentencias = agregarConcreto(raizConcreta, "ListaSentencias");
        while (!estaAlFinal()) {
            if (verifica(TokenType.LLAVE_DER)) {
                registrarError(ver(), "inicio de sentencia o EOF",
                        "delimitador de bloque de cierre sin bloque abierto");
                agregarTerminal(listaSentencias, avanzar());
                continue;
            }

            NodoAST sentencia = parseSentencia(listaSentencias);
            if (sentencia != null) {
                agregarAst(raizAst, sentencia);
            }
        }

        consumir(listaSentencias, TokenType.EOF, "EOF", "se esperaba fin de archivo");
        actualizarConcreto();
        actualizarAst();
        exportarVisualizacionesFinales();
        return errores.isEmpty();
    }

    public List<ErrorSintactico> getErrores() {
        return Collections.unmodifiableList(errores);
    }

    public ArbolConcreto getArbolConcreto() {
        return arbolConcreto;
    }

    public ArbolAbstracto getArbolAbstracto() {
        return arbolAbstracto;
    }

    public List<String> getArchivosGenerados() {
        List<String> archivos = new ArrayList<String>();
        if (visualizadorConcreto != null) {
            archivos.add(visualizadorConcreto.getRutaDot().toString());
            archivos.add(visualizadorConcreto.getRutaPng().toString());
        }
        if (visualizadorAst != null) {
            archivos.add(visualizadorAst.getRutaDot().toString());
            archivos.add(visualizadorAst.getRutaPng().toString());
        }
        return archivos;
    }

    private NodoAST parseSentencia(NodoArbol padre) {
        NodoArbol sentencia = agregarConcreto(padre, "Sentencia");

        if (esTipoDato(ver())) {
            return parseDeclaracion(sentencia, true);
        }
        if (verifica(TokenType.ID)) {
            return parseAsignacion(sentencia, true);
        }
        if (verifica(TokenType.VALIDAR)) {
            return parseCondicional(sentencia);
        }
        if (verifica(TokenType.CICLO)) {
            return parseCiclo(sentencia);
        }
        if (verifica(TokenType.RECOLECTAR)) {
            return parseEntrada(sentencia);
        }
        if (verifica(TokenType.ESTAMPAR)) {
            return parseSalida(sentencia);
        }
        if (verifica(TokenType.LLAVE_IZQ)) {
            return parseBloque(sentencia);
        }

        registrarError(ver(),
                "declaracion, asignacion, valdt, ciclar, recolt, estamp o bloque",
                "sentencia invalida");
        agregarConcreto(sentencia, "ERROR: sentencia invalida");
        recuperarSentencia();
        return null;
    }

    private NodoAST parseDeclaracion(NodoArbol padre, boolean requierePuntoComa) {
        NodoArbol declaracion = agregarConcreto(padre, "Declaracion");
        Token tipo = consumirTipoDato(declaracion);
        Token id = consumir(declaracion, TokenType.ID, "ID", "se esperaba un identificador en la declaracion");

        NodoAST ast = new NodoAST("DeclaracionVariable");
        ast.agregarHijo(new NodoAST("tipo", tipo == null ? "<?>" : tipo.getLexema()));
        ast.agregarHijo(new NodoAST("nombre", id == null ? "<?>" : id.getLexema()));

        if (coincideLexema(TokenType.OP_ASIG, "=")) {
            agregarTerminal(declaracion, anterior());
            NodoAST valorInicial = new NodoAST("valorInicial");
            valorInicial.agregarHijo(parseExpresion(declaracion));
            ast.agregarHijo(valorInicial);
        }

        if (requierePuntoComa) {
            consumir(declaracion, TokenType.PUNTO_COMA, ";", "falta ';' al final de la declaracion");
        }
        return ast;
    }

    private NodoAST parseAsignacion(NodoArbol padre, boolean requierePuntoComa) {
        NodoArbol asignacion = agregarConcreto(padre, "Asignacion");
        Token id = consumir(asignacion, TokenType.ID, "ID", "se esperaba un identificador al inicio de la asignacion");
        consumirLexema(asignacion, TokenType.OP_ASIG, "=", "=", "se esperaba '=' en la asignacion");

        NodoAST ast = new NodoAST("Asignacion");
        ast.agregarHijo(new NodoAST("nombre", id == null ? "<?>" : id.getLexema()));
        NodoAST valor = new NodoAST("valor");
        valor.agregarHijo(parseExpresion(asignacion));
        ast.agregarHijo(valor);

        if (requierePuntoComa) {
            consumir(asignacion, TokenType.PUNTO_COMA, ";", "falta ';' al final de la asignacion");
        }
        return ast;
    }

    private NodoAST parseCondicional(NodoArbol padre) {
        NodoArbol condicional = agregarConcreto(padre, "Condicional");
        consumir(condicional, TokenType.VALIDAR, "valdt", "se esperaba 'valdt'");
        consumir(condicional, TokenType.PAREN_IZQ, "(", "se esperaba '(' despues de 'valdt'");

        NodoAST ast = new NodoAST("Condicional");
        NodoAST condicion = new NodoAST("condicion");
        condicion.agregarHijo(parseExpresion(condicional));
        ast.agregarHijo(condicion);

        consumir(condicional, TokenType.PAREN_DER, ")", "se esperaba ')' despues de la condicion");
        ast.agregarHijo(parseBloque(condicional));
        return ast;
    }

    private NodoAST parseCiclo(NodoArbol padre) {
        NodoArbol ciclo = agregarConcreto(padre, "Ciclo");
        consumir(ciclo, TokenType.CICLO, "ciclar", "se esperaba 'ciclar'");
        consumir(ciclo, TokenType.PAREN_IZQ, "(", "se esperaba '(' despues de 'ciclar'");

        NodoAST ast = new NodoAST("Ciclo");

        NodoAST inicializacion = new NodoAST("inicializacion");
        inicializacion.agregarHijo(parseInicializacionCiclo(ciclo));
        ast.agregarHijo(inicializacion);
        consumir(ciclo, TokenType.PUNTO_COMA, ";", "se esperaba ';' despues de la inicializacion del ciclo");

        NodoAST condicion = new NodoAST("condicion");
        condicion.agregarHijo(parseExpresion(ciclo));
        ast.agregarHijo(condicion);
        consumir(ciclo, TokenType.PUNTO_COMA, ";", "se esperaba ';' despues de la condicion del ciclo");

        NodoAST actualizacion = new NodoAST("actualizacion");
        actualizacion.agregarHijo(parseActualizacionCiclo(ciclo));
        ast.agregarHijo(actualizacion);

        consumir(ciclo, TokenType.PAREN_DER, ")", "se esperaba ')' despues de la actualizacion del ciclo");
        ast.agregarHijo(parseBloque(ciclo));
        return ast;
    }

    private NodoAST parseInicializacionCiclo(NodoArbol padre) {
        NodoArbol inicializacion = agregarConcreto(padre, "Inicializacion");
        if (esTipoDato(ver())) {
            return parseDeclaracion(inicializacion, false);
        }
        if (verifica(TokenType.ID)) {
            return parseAsignacion(inicializacion, false);
        }

        registrarError(ver(), "declaracion o asignacion",
                "inicializacion de ciclo incompleta o invalida");
        agregarConcreto(inicializacion, "ERROR: inicializacion invalida");
        return new NodoAST("InicializacionInvalida");
    }

    private NodoAST parseActualizacionCiclo(NodoArbol padre) {
        NodoArbol actualizacion = agregarConcreto(padre, "Actualizacion");
        if (!verifica(TokenType.ID)) {
            registrarError(ver(), "ID", "actualizacion de ciclo incompleta o invalida");
            agregarConcreto(actualizacion, "ERROR: actualizacion invalida");
            return new NodoAST("ActualizacionInvalida");
        }

        Token id = consumir(actualizacion, TokenType.ID, "ID", "se esperaba identificador en la actualizacion");

        if (coincide(TokenType.OP_INC)) {
            agregarTerminal(actualizacion, anterior());
            NodoAST ast = new NodoAST("Incremento");
            ast.agregarHijo(new NodoAST("nombre", id == null ? "<?>" : id.getLexema()));
            return ast;
        }

        if (coincide(TokenType.OP_DEC)) {
            agregarTerminal(actualizacion, anterior());
            NodoAST ast = new NodoAST("Decremento");
            ast.agregarHijo(new NodoAST("nombre", id == null ? "<?>" : id.getLexema()));
            return ast;
        }

        if (coincideLexema(TokenType.OP_ASIG, "=")) {
            agregarTerminal(actualizacion, anterior());
            NodoAST ast = new NodoAST("Asignacion");
            ast.agregarHijo(new NodoAST("nombre", id == null ? "<?>" : id.getLexema()));
            NodoAST valor = new NodoAST("valor");
            valor.agregarHijo(parseExpresion(actualizacion));
            ast.agregarHijo(valor);
            return ast;
        }

        registrarError(ver(), "++, -- o =", "se esperaba incremento, decremento o asignacion en la actualizacion");
        agregarConcreto(actualizacion, "ERROR: actualizacion incompleta");
        return new NodoAST("ActualizacionInvalida");
    }

    private NodoAST parseEntrada(NodoArbol padre) {
        NodoArbol entrada = agregarConcreto(padre, "Entrada");
        consumir(entrada, TokenType.RECOLECTAR, "recolt", "se esperaba 'recolt'");
        consumir(entrada, TokenType.PAREN_IZQ, "(", "se esperaba '(' despues de 'recolt'");
        Token id = consumir(entrada, TokenType.ID, "ID", "se esperaba identificador en 'recolt'");
        consumir(entrada, TokenType.PAREN_DER, ")", "se esperaba ')' despues del identificador");
        consumir(entrada, TokenType.PUNTO_COMA, ";", "falta ';' al final de 'recolt'");

        NodoAST ast = new NodoAST("Entrada");
        ast.agregarHijo(new NodoAST("nombre", id == null ? "<?>" : id.getLexema()));
        return ast;
    }

    private NodoAST parseSalida(NodoArbol padre) {
        NodoArbol salida = agregarConcreto(padre, "Salida");
        consumir(salida, TokenType.ESTAMPAR, "estamp", "se esperaba 'estamp'");
        consumir(salida, TokenType.PAREN_IZQ, "(", "se esperaba '(' despues de 'estamp'");

        NodoAST ast = new NodoAST("Salida");
        NodoAST argumentos = new NodoAST("argumentos");
        if (verifica(TokenType.PAREN_DER)) {
            registrarError(ver(), "argumento de salida", "'estamp' requiere al menos un argumento");
            agregarConcreto(salida, "ERROR: argumento de salida faltante");
        } else {
            argumentos.agregarHijo(parseExpresion(salida));
            while (coincide(TokenType.COMA)) {
                agregarTerminal(salida, anterior());
                argumentos.agregarHijo(parseExpresion(salida));
            }
        }
        ast.agregarHijo(argumentos);

        consumir(salida, TokenType.PAREN_DER, ")", "se esperaba ')' despues de los argumentos de salida");
        consumir(salida, TokenType.PUNTO_COMA, ";", "falta ';' al final de 'estamp'");
        return ast;
    }

    private NodoAST parseBloque(NodoArbol padre) {
        NodoArbol bloque = agregarConcreto(padre, "Bloque");
        consumir(bloque, TokenType.LLAVE_IZQ, "{", "se esperaba '{' para iniciar el bloque");

        NodoAST ast = new NodoAST("Bloque");
        NodoArbol listaSentencias = agregarConcreto(bloque, "ListaSentencias");
        while (!estaAlFinal() && !verifica(TokenType.LLAVE_DER)) {
            NodoAST sentencia = parseSentencia(listaSentencias);
            if (sentencia != null) {
                ast.agregarHijo(sentencia);
            }
        }

        consumir(bloque, TokenType.LLAVE_DER, "}", "se esperaba '}' para cerrar el bloque");
        return ast;
    }

    private NodoAST parseExpresion(NodoArbol padre) {
        NodoArbol expresion = agregarConcreto(padre, "Expresion");
        return parseOr(expresion);
    }

    private NodoAST parseOr(NodoArbol padre) {
        NodoArbol nodo = agregarConcreto(padre, "ExpresionOR");
        NodoAST izquierda = parseAnd(nodo);

        while (coincide(TokenType.OP_O)) {
            Token operador = anterior();
            agregarTerminal(nodo, operador);
            NodoAST derecha = parseAnd(nodo);
            izquierda = operacionBinaria(operador, izquierda, derecha);
        }
        return izquierda;
    }

    private NodoAST parseAnd(NodoArbol padre) {
        NodoArbol nodo = agregarConcreto(padre, "ExpresionAND");
        NodoAST izquierda = parseRelacional(nodo);

        while (coincide(TokenType.OP_Y)) {
            Token operador = anterior();
            agregarTerminal(nodo, operador);
            NodoAST derecha = parseRelacional(nodo);
            izquierda = operacionBinaria(operador, izquierda, derecha);
        }
        return izquierda;
    }

    private NodoAST parseRelacional(NodoArbol padre) {
        NodoArbol nodo = agregarConcreto(padre, "ExpresionRelacional");
        NodoAST izquierda = parseAditiva(nodo);

        while (coincide(TokenType.OP_COMP)) {
            Token operador = anterior();
            agregarTerminal(nodo, operador);
            NodoAST derecha = parseAditiva(nodo);
            izquierda = operacionBinaria(operador, izquierda, derecha);
        }
        return izquierda;
    }

    private NodoAST parseAditiva(NodoArbol padre) {
        NodoArbol nodo = agregarConcreto(padre, "ExpresionAditiva");
        NodoAST izquierda = parseMultiplicativa(nodo);

        while (verificaOperadorAritmetico("+") || verificaOperadorAritmetico("-")) {
            Token operador = avanzar();
            agregarTerminal(nodo, operador);
            NodoAST derecha = parseMultiplicativa(nodo);
            izquierda = operacionBinaria(operador, izquierda, derecha);
        }
        return izquierda;
    }

    private NodoAST parseMultiplicativa(NodoArbol padre) {
        NodoArbol nodo = agregarConcreto(padre, "ExpresionMultiplicativa");
        NodoAST izquierda = parseUnaria(nodo);

        while (verificaOperadorAritmetico("*")
                || verificaOperadorAritmetico("/")
                || verificaOperadorAritmetico("%")) {
            Token operador = avanzar();
            agregarTerminal(nodo, operador);
            NodoAST derecha = parseUnaria(nodo);
            izquierda = operacionBinaria(operador, izquierda, derecha);
        }
        return izquierda;
    }

    private NodoAST parseUnaria(NodoArbol padre) {
        NodoArbol nodo = agregarConcreto(padre, "Unaria");
        if (verificaOperadorAritmetico("+") || verificaOperadorAritmetico("-")) {
            Token operador = avanzar();
            agregarTerminal(nodo, operador);
            NodoAST operando = parseUnaria(nodo);
            NodoAST ast = new NodoAST("OperacionUnaria");
            ast.agregarHijo(new NodoAST("operador", operador.getLexema()));
            ast.agregarHijo(operando);
            return ast;
        }
        return parsePrimario(nodo);
    }

    private NodoAST parsePrimario(NodoArbol padre) {
        NodoArbol primario = agregarConcreto(padre, "Primario");

        if (coincide(TokenType.ID)) {
            Token token = anterior();
            agregarTerminal(primario, token);
            return new NodoAST("Identificador", token.getLexema());
        }
        if (coincide(TokenType.ENTERO_LITERAL)) {
            Token token = anterior();
            agregarTerminal(primario, token);
            return new NodoAST("Entero", token.getLexema());
        }
        if (coincide(TokenType.FLOTANTE_LITERAL)) {
            Token token = anterior();
            agregarTerminal(primario, token);
            return new NodoAST("Flotante", token.getLexema());
        }
        if (coincide(TokenType.CARACTER_LITERAL)) {
            Token token = anterior();
            agregarTerminal(primario, token);
            return new NodoAST("Caracter", token.getLexema());
        }
        if (coincide(TokenType.CADENA_LITERAL)) {
            Token token = anterior();
            agregarTerminal(primario, token);
            return new NodoAST("Cadena", token.getLexema());
        }
        if (coincide(TokenType.PAREN_IZQ)) {
            agregarTerminal(primario, anterior());
            NodoAST expresion = parseExpresion(primario);
            consumir(primario, TokenType.PAREN_DER, ")", "se esperaba ')' para cerrar la expresion agrupada");
            return expresion;
        }

        registrarError(ver(),
                "ID, literal numerico, literal de caracter, literal de cadena o '('",
                "expresion incompleta o invalida");
        agregarConcreto(primario, "ERROR: expresion incompleta");
        if (!esDelimitadorDeExpresion(ver())) {
            agregarTerminal(primario, avanzar());
        }
        return new NodoAST("ExpresionInvalida");
    }

    private NodoAST operacionBinaria(Token operador, NodoAST izquierda, NodoAST derecha) {
        NodoAST ast = new NodoAST("OperacionBinaria");
        ast.agregarHijo(new NodoAST("operador", operador.getLexema()));
        NodoAST nodoIzquierdo = new NodoAST("izquierdo");
        nodoIzquierdo.agregarHijo(izquierda);
        ast.agregarHijo(nodoIzquierdo);
        NodoAST nodoDerecho = new NodoAST("derecho");
        nodoDerecho.agregarHijo(derecha);
        ast.agregarHijo(nodoDerecho);
        return ast;
    }

    private Token consumir(NodoArbol padre, TokenType tipo, String esperado, String descripcion) {
        if (verifica(tipo)) {
            Token token = avanzar();
            agregarTerminal(padre, token);
            return token;
        }

        registrarError(ver(), esperado, descripcion);
        agregarConcreto(padre, "ERROR: se esperaba " + esperado);
        return null;
    }

    private Token consumirLexema(NodoArbol padre, TokenType tipo, String lexema, String esperado, String descripcion) {
        if (verificaLexema(tipo, lexema)) {
            Token token = avanzar();
            agregarTerminal(padre, token);
            return token;
        }

        registrarError(ver(), esperado, descripcion);
        agregarConcreto(padre, "ERROR: se esperaba " + esperado);
        return null;
    }

    private Token consumirTipoDato(NodoArbol padre) {
        if (esTipoDato(ver())) {
            Token token = avanzar();
            agregarTerminal(padre, token);
            return token;
        }

        registrarError(ver(), "num, letra o flotar", "se esperaba un tipo de dato");
        agregarConcreto(padre, "ERROR: se esperaba tipo de dato");
        return null;
    }

    private boolean coincide(TokenType tipo) {
        if (!verifica(tipo)) {
            return false;
        }
        avanzar();
        return true;
    }

    private boolean coincideLexema(TokenType tipo, String lexema) {
        if (!verificaLexema(tipo, lexema)) {
            return false;
        }
        avanzar();
        return true;
    }

    private boolean verifica(TokenType tipo) {
        return ver().getTipo() == tipo;
    }

    private boolean verificaLexema(TokenType tipo, String lexema) {
        return verifica(tipo) && ver().getLexema().equals(lexema);
    }

    private boolean verificaOperadorAritmetico(String lexema) {
        return verificaLexema(TokenType.OP_ARIT, lexema);
    }

    private boolean esTipoDato(Token token) {
        if (token == null) {
            return false;
        }
        if (token.getTipo() == TokenType.CARACTER || token.getTipo() == TokenType.FLOTANTE) {
            return true;
        }
        if (token.getTipo() == TokenType.NUMERO) {
            return "num".equals(token.getLexema()) || "flotar".equals(token.getLexema());
        }
        return false;
    }

    private boolean estaAlFinal() {
        return ver().getTipo() == TokenType.EOF;
    }

    private Token ver() {
        if (actual >= tokens.size()) {
            return tokens.get(tokens.size() - 1);
        }
        return tokens.get(actual);
    }

    private Token anterior() {
        if (actual == 0) {
            return tokens.get(0);
        }
        return tokens.get(actual - 1);
    }

    private Token avanzar() {
        if (actual < tokens.size()) {
            actual++;
        }
        return anterior();
    }

    private void recuperarSentencia() {
        while (!estaAlFinal()) {
            if (anterior().getTipo() == TokenType.PUNTO_COMA) {
                return;
            }
            if (verifica(TokenType.LLAVE_DER) || puedeIniciarSentencia(ver())) {
                return;
            }
            avanzar();
        }
    }

    private boolean puedeIniciarSentencia(Token token) {
        return esTipoDato(token)
                || token.getTipo() == TokenType.ID
                || token.getTipo() == TokenType.VALIDAR
                || token.getTipo() == TokenType.CICLO
                || token.getTipo() == TokenType.RECOLECTAR
                || token.getTipo() == TokenType.ESTAMPAR
                || token.getTipo() == TokenType.LLAVE_IZQ;
    }

    private boolean esDelimitadorDeExpresion(Token token) {
        TokenType tipo = token.getTipo();
        return tipo == TokenType.PUNTO_COMA
                || tipo == TokenType.PAREN_DER
                || tipo == TokenType.LLAVE_DER
                || tipo == TokenType.COMA
                || tipo == TokenType.EOF;
    }

    private void registrarError(Token encontrado, String esperado, String descripcion) {
        errores.add(new ErrorSintactico(
                describirToken(encontrado),
                esperado,
                encontrado.getLinea(),
                encontrado.getColumna(),
                descripcion
        ));
    }

    private String describirToken(Token token) {
        if (token.getTipo() == TokenType.EOF) {
            return "EOF";
        }
        return token.getTipo() + " '" + escapar(token.getLexema()) + "'";
    }

    private NodoArbol agregarConcreto(NodoArbol padre, String etiqueta) {
        NodoArbol hijo = new NodoArbol(etiqueta);
        if (padre != null) {
            padre.agregarHijo(hijo);
        }
        actualizarConcreto();
        return hijo;
    }

    private void agregarTerminal(NodoArbol padre, Token token) {
        agregarConcreto(padre, etiquetaTerminal(token));
    }

    private NodoAST agregarAst(NodoAST padre, NodoAST hijo) {
        if (padre != null && hijo != null) {
            padre.agregarHijo(hijo);
        }
        actualizarAst();
        return hijo;
    }

    private String etiquetaTerminal(Token token) {
        if (token.getTipo() == TokenType.EOF) {
            return "EOF";
        }
        return token.getTipo() + ": " + escapar(token.getLexema());
    }

    private void actualizarConcreto() {
        if (visualizadorConcreto != null && arbolConcreto != null) {
            visualizadorConcreto.actualizar(arbolConcreto.getRaiz());
        }
    }

    private void actualizarAst() {
        if (visualizadorAst != null && arbolAbstracto != null) {
            visualizadorAst.actualizar(arbolAbstracto.getRaiz());
        }
    }

    private void exportarVisualizacionesFinales() {
        if (visualizadorConcreto != null && arbolConcreto != null) {
            visualizadorConcreto.exportarFinal(arbolConcreto.getRaiz());
        }
        if (visualizadorAst != null && arbolAbstracto != null) {
            visualizadorAst.exportarFinal(arbolAbstracto.getRaiz());
        }
    }

    private static String escapar(String texto) {
        if (texto == null) {
            return "";
        }
        return texto
                .replace("\\", "\\\\")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
                .replace("'", "\\'");
    }
}
