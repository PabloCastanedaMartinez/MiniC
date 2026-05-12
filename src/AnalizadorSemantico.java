import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class AnalizadorSemantico implements VisitadorAST<AtributosNodo> {
    private final NodoAST raiz;
    private final List<ErrorSemantico> errores = new ArrayList<ErrorSemantico>();
    private final List<AdvertenciaSemantica> advertencias = new ArrayList<AdvertenciaSemantica>();
    private TablaSimbolos tablaSimbolos = new TablaSimbolos();

    public AnalizadorSemantico(NodoAST raiz) {
        this.raiz = raiz;
    }

    public boolean analizar() {
        errores.clear();
        advertencias.clear();
        tablaSimbolos = new TablaSimbolos();

        if (raiz == null) {
            registrarError("ERROR_AST_VACIO", "", -1, -1,
                    "no existe AST para realizar el analisis semantico");
            return false;
        }

        visitar(raiz);
        generarAdvertencias();
        return errores.isEmpty();
    }

    public List<ErrorSemantico> getErrores() {
        return Collections.unmodifiableList(errores);
    }

    public List<AdvertenciaSemantica> getAdvertencias() {
        return Collections.unmodifiableList(advertencias);
    }

    public TablaSimbolos getTablaSimbolos() {
        return tablaSimbolos;
    }

    @Override
    public AtributosNodo visitar(NodoAST nodo) {
        if (nodo == null) {
            return new AtributosNodo(TipoDato.ERROR, false, -1, -1);
        }

        String nombre = nodo.getNombre();
        if ("Programa".equals(nombre)) {
            for (NodoAST hijo : nodo.getHijos()) {
                visitar(hijo);
            }
            return atributos(TipoDato.VACIO, true, nodo);
        }
        if ("Bloque".equals(nombre)) {
            return visitarBloque(nodo);
        }
        if ("DeclaracionVariable".equals(nombre)) {
            return visitarDeclaracion(nodo);
        }
        if ("Asignacion".equals(nombre)) {
            return visitarAsignacion(nodo);
        }
        if ("Condicional".equals(nombre)) {
            return visitarCondicional(nodo);
        }
        if ("Ciclo".equals(nombre)) {
            return visitarCiclo(nodo);
        }
        if ("Entrada".equals(nombre)) {
            return visitarEntrada(nodo);
        }
        if ("Salida".equals(nombre)) {
            return visitarSalida(nodo);
        }
        if ("Incremento".equals(nombre) || "Decremento".equals(nombre)) {
            return visitarIncrementoDecremento(nodo);
        }

        return evaluarExpresion(nodo);
    }

    private AtributosNodo visitarBloque(NodoAST nodo) {
        tablaSimbolos.abrirAmbito("bloque");
        for (NodoAST hijo : nodo.getHijos()) {
            visitar(hijo);
        }
        tablaSimbolos.cerrarAmbito();
        return atributos(TipoDato.VACIO, true, nodo);
    }

    private AtributosNodo visitarDeclaracion(NodoAST nodo) {
        NodoAST tipoNodo = hijo(nodo, "tipo");
        NodoAST nombreNodo = hijo(nodo, "nombre");
        String nombre = valor(nombreNodo);
        TipoDato tipo = TipoDato.desdeLexema(valor(tipoNodo));

        boolean declaracionValida = true;
        Simbolo simbolo = null;

        if (nombre == null || nombre.length() == 0 || "<?>".equals(nombre)) {
            registrarError("ERROR_DECLARACION_INVALIDA", nombre, linea(nodo), columna(nodo),
                    "la declaracion no contiene un identificador valido");
            declaracionValida = false;
        } else if (!tipo.esDeclarablePorUsuario()) {
            registrarError("ERROR_TIPO_NO_DECLARABLE", valor(tipoNodo), linea(tipoNodo), columna(tipoNodo),
                    "el tipo '" + valor(tipoNodo) + "' no es declarable por el usuario");
            declaracionValida = false;
        } else if (tablaSimbolos.existeEnAmbitoActual(nombre)) {
            registrarError("ERROR_REDECLARACION_VARIABLE", nombre, linea(nombreNodo), columna(nombreNodo),
                    "la variable '" + nombre + "' ya fue declarada en el ambito actual");
            declaracionValida = false;
        } else {
            simbolo = new Simbolo(
                    nombre,
                    tipo,
                    "variable",
                    linea(nombreNodo),
                    columna(nombreNodo),
                    tablaSimbolos.getAmbitoActual().getEtiqueta(),
                    tablaSimbolos.getAmbitoActual().getId(),
                    tablaSimbolos.getAmbitoActual().getProfundidad(),
                    false
            );
            tablaSimbolos.insertar(simbolo);
        }

        NodoAST valorInicial = primerHijo(hijo(nodo, "valorInicial"));
        if (valorInicial != null) {
            AtributosNodo atributosValor = evaluarExpresion(valorInicial);
            if (tipo != TipoDato.ERROR && atributosValor.getTipo() != TipoDato.ERROR
                    && !ReglasTipo.compatibleAsignacion(tipo, atributosValor.getTipo())) {
                registrarError("ERROR_TIPO_INCOMPATIBLE", nombre, linea(valorInicial), columna(valorInicial),
                        "no se puede inicializar una variable de tipo "
                                + tipo
                                + " con una expresion de tipo "
                                + atributosValor.getTipo());
                declaracionValida = false;
            } else if (simbolo != null && atributosValor.esValida()) {
                simbolo.marcarInicializado();
            }
            declaracionValida = declaracionValida && atributosValor.esValida();
        }

        return atributos(TipoDato.VACIO, declaracionValida, nodo);
    }

    private AtributosNodo visitarAsignacion(NodoAST nodo) {
        NodoAST nombreNodo = hijo(nodo, "nombre");
        String nombre = valor(nombreNodo);
        Simbolo simbolo = buscarVariableDestino(nombreNodo);

        NodoAST expresion = primerHijo(hijo(nodo, "valor"));
        AtributosNodo atributosValor = evaluarExpresion(expresion);
        boolean esValida = atributosValor.esValida();

        if (simbolo != null && atributosValor.getTipo() != TipoDato.ERROR
                && !ReglasTipo.compatibleAsignacion(simbolo.getTipo(), atributosValor.getTipo())) {
            registrarError("ERROR_TIPO_INCOMPATIBLE", nombre, linea(expresion), columna(expresion),
                    "no se puede asignar una expresion de tipo "
                            + atributosValor.getTipo()
                            + " a una variable de tipo "
                            + simbolo.getTipo());
            esValida = false;
        } else if (simbolo != null && atributosValor.esValida()) {
            simbolo.marcarInicializado();
        }

        return atributos(TipoDato.VACIO, esValida && simbolo != null, nodo);
    }

    private AtributosNodo visitarCondicional(NodoAST nodo) {
        NodoAST expresionCondicion = primerHijo(hijo(nodo, "condicion"));
        AtributosNodo condicion = evaluarExpresion(expresionCondicion);
        boolean esValida = condicion.esValida();

        if (condicion.getTipo() != TipoDato.ERROR && condicion.getTipo() != TipoDato.BOOLEANO_INTERNO) {
            registrarError("ERROR_CONDICION_NO_BOOLEANA", valorVisible(expresionCondicion),
                    linea(expresionCondicion), columna(expresionCondicion),
                    "la condicion de 'valdt' debe ser booleano_interno y se obtuvo "
                            + condicion.getTipo());
            esValida = false;
        }

        NodoAST bloque = hijo(nodo, "Bloque");
        if (bloque != null) {
            visitar(bloque);
        }
        return atributos(TipoDato.VACIO, esValida, nodo);
    }

    private AtributosNodo visitarCiclo(NodoAST nodo) {
        tablaSimbolos.abrirAmbito("ciclo");
        boolean esValida = true;

        NodoAST inicializacion = primerHijo(hijo(nodo, "inicializacion"));
        if (inicializacion != null) {
            esValida = visitar(inicializacion).esValida() && esValida;
        }

        NodoAST expresionCondicion = primerHijo(hijo(nodo, "condicion"));
        AtributosNodo condicion = evaluarExpresion(expresionCondicion);
        if (condicion.getTipo() != TipoDato.ERROR && condicion.getTipo() != TipoDato.BOOLEANO_INTERNO) {
            registrarError("ERROR_CONDICION_NO_BOOLEANA", valorVisible(expresionCondicion),
                    linea(expresionCondicion), columna(expresionCondicion),
                    "la condicion de 'ciclar' debe ser booleano_interno y se obtuvo "
                            + condicion.getTipo());
            esValida = false;
        }
        esValida = condicion.esValida() && esValida;

        NodoAST actualizacion = primerHijo(hijo(nodo, "actualizacion"));
        if (actualizacion != null) {
            esValida = visitar(actualizacion).esValida() && esValida;
        }

        NodoAST bloque = hijo(nodo, "Bloque");
        if (bloque != null) {
            visitar(bloque);
        }

        tablaSimbolos.cerrarAmbito();
        return atributos(TipoDato.VACIO, esValida, nodo);
    }

    private AtributosNodo visitarEntrada(NodoAST nodo) {
        NodoAST nombreNodo = hijo(nodo, "nombre");
        Simbolo simbolo = buscarVariableDestino(nombreNodo);
        if (simbolo == null) {
            return atributos(TipoDato.VACIO, false, nodo);
        }
        if (!simbolo.getTipo().esDeclarablePorUsuario()) {
            registrarError("ERROR_ARGUMENTO_RECOLT_INVALIDO", valor(nombreNodo), linea(nombreNodo), columna(nombreNodo),
                    "'recolt' solo puede recibir variables declarables del lenguaje");
            return atributos(TipoDato.VACIO, false, nodo);
        }
        simbolo.marcarInicializado();
        return atributos(TipoDato.VACIO, true, nodo);
    }

    private AtributosNodo visitarSalida(NodoAST nodo) {
        NodoAST argumentos = hijo(nodo, "argumentos");
        boolean esValida = true;
        if (argumentos == null || argumentos.getHijos().isEmpty()) {
            registrarError("ERROR_ARGUMENTO_ESTAMP_INVALIDO", "estamp", linea(nodo), columna(nodo),
                    "'estamp' requiere al menos un argumento valido");
            return atributos(TipoDato.VACIO, false, nodo);
        }
        for (NodoAST argumento : argumentos.getHijos()) {
            AtributosNodo atributosArgumento = evaluarExpresion(argumento);
            esValida = atributosArgumento.esValida() && esValida;
        }
        return atributos(TipoDato.VACIO, esValida, nodo);
    }

    private AtributosNodo visitarIncrementoDecremento(NodoAST nodo) {
        NodoAST nombreNodo = hijo(nodo, "nombre");
        String nombre = valor(nombreNodo);
        Simbolo simbolo = buscarVariableDestino(nombreNodo);
        if (simbolo == null) {
            return atributos(TipoDato.VACIO, false, nodo);
        }

        boolean esValida = true;
        simbolo.marcarUsado();
        if (!simbolo.estaInicializado()) {
            registrarError("ERROR_VARIABLE_NO_INICIALIZADA", nombre, linea(nombreNodo), columna(nombreNodo),
                    "la variable '" + nombre + "' debe estar inicializada antes de usar '"
                            + nodo.getNombre() + "'");
            esValida = false;
        }
        if (!ReglasTipo.esIncrementable(simbolo.getTipo())) {
            registrarError("ERROR_INCREMENTO_INVALIDO", nombre, linea(nombreNodo), columna(nombreNodo),
                    "'" + nodo.getNombre() + "' solo se permite sobre variables numericas y se obtuvo "
                            + simbolo.getTipo());
            esValida = false;
        }
        return atributos(TipoDato.VACIO, esValida, nodo);
    }

    private AtributosNodo evaluarExpresion(NodoAST nodo) {
        if (nodo == null) {
            return new AtributosNodo(TipoDato.ERROR, false, -1, -1);
        }

        String nombre = nodo.getNombre();
        if ("Identificador".equals(nombre)) {
            return evaluarIdentificador(nodo);
        }
        if ("Entero".equals(nombre)) {
            return atributos(TipoDato.NUM, true, nodo);
        }
        if ("Flotante".equals(nombre)) {
            return atributos(TipoDato.FLOTAR, true, nodo);
        }
        if ("Caracter".equals(nombre)) {
            return atributos(TipoDato.LETRA, true, nodo);
        }
        if ("Cadena".equals(nombre)) {
            return atributos(TipoDato.CADENA, true, nodo);
        }
        if ("OperacionBinaria".equals(nombre)) {
            return evaluarOperacionBinaria(nodo);
        }
        if ("OperacionUnaria".equals(nombre)) {
            return evaluarOperacionUnaria(nodo);
        }
        if ("ExpresionInvalida".equals(nombre)
                || "InicializacionInvalida".equals(nombre)
                || "ActualizacionInvalida".equals(nombre)) {
            return atributos(TipoDato.ERROR, false, nodo);
        }

        if (nodo.getHijos().size() == 1) {
            return evaluarExpresion(nodo.getHijos().get(0));
        }

        AtributosNodo ultimo = atributos(TipoDato.VACIO, true, nodo);
        for (NodoAST hijo : nodo.getHijos()) {
            ultimo = evaluarExpresion(hijo);
        }
        return ultimo;
    }

    private AtributosNodo evaluarIdentificador(NodoAST nodo) {
        String nombre = valor(nodo);
        Simbolo simbolo = tablaSimbolos.buscar(nombre);
        if (simbolo == null) {
            registrarError("ERROR_VARIABLE_NO_DECLARADA", nombre, linea(nodo), columna(nodo),
                    "la variable '" + nombre + "' no ha sido declarada");
            return atributos(TipoDato.ERROR, false, nodo);
        }

        simbolo.marcarUsado();
        if (!simbolo.estaInicializado()) {
            registrarError("ERROR_VARIABLE_NO_INICIALIZADA", nombre, linea(nodo), columna(nodo),
                    "la variable '" + nombre + "' se usa antes de recibir un valor");
            return atributos(simbolo.getTipo(), false, nodo);
        }
        return atributos(simbolo.getTipo(), true, nodo);
    }

    private AtributosNodo evaluarOperacionBinaria(NodoAST nodo) {
        NodoAST operadorNodo = hijo(nodo, "operador");
        String operador = valor(operadorNodo);
        AtributosNodo izquierdo = evaluarExpresion(primerHijo(hijo(nodo, "izquierdo")));
        AtributosNodo derecho = evaluarExpresion(primerHijo(hijo(nodo, "derecho")));

        if (izquierdo.getTipo() == TipoDato.ERROR || derecho.getTipo() == TipoDato.ERROR) {
            return atributos(TipoDato.ERROR, false, nodo);
        }

        TipoDato resultado;
        if (ReglasTipo.esOperadorAritmetico(operador)) {
            resultado = ReglasTipo.resultadoAritmetico(operador, izquierdo.getTipo(), derecho.getTipo());
            if (resultado == TipoDato.ERROR) {
                registrarError("ERROR_OPERACION_ARITMETICA_INVALIDA", operador,
                        linea(operadorNodo), columna(operadorNodo),
                        "el operador '" + operador + "' no acepta operandos "
                                + izquierdo.getTipo() + " y " + derecho.getTipo());
                return atributos(TipoDato.ERROR, false, nodo);
            }
            return atributos(resultado, izquierdo.esValida() && derecho.esValida(), nodo);
        }

        if (ReglasTipo.esOperadorRelacional(operador)) {
            resultado = ReglasTipo.resultadoRelacional(operador, izquierdo.getTipo(), derecho.getTipo());
            if (resultado == TipoDato.ERROR) {
                registrarError("ERROR_COMPARACION_INCOMPATIBLE", operador,
                        linea(operadorNodo), columna(operadorNodo),
                        "no se pueden comparar valores de tipo "
                                + izquierdo.getTipo()
                                + " y "
                                + derecho.getTipo()
                                + " con el operador '"
                                + operador
                                + "'");
                return atributos(TipoDato.ERROR, false, nodo);
            }
            return atributos(TipoDato.BOOLEANO_INTERNO, izquierdo.esValida() && derecho.esValida(), nodo);
        }

        if (ReglasTipo.esOperadorLogico(operador)) {
            resultado = ReglasTipo.resultadoLogico(operador, izquierdo.getTipo(), derecho.getTipo());
            if (resultado == TipoDato.ERROR) {
                registrarError("ERROR_OPERACION_LOGICA_INVALIDA", operador,
                        linea(operadorNodo), columna(operadorNodo),
                        "el operador '" + operador + "' requiere operandos booleano_interno y se obtuvo "
                                + izquierdo.getTipo() + " y " + derecho.getTipo());
                return atributos(TipoDato.ERROR, false, nodo);
            }
            return atributos(TipoDato.BOOLEANO_INTERNO, izquierdo.esValida() && derecho.esValida(), nodo);
        }

        registrarError("ERROR_OPERADOR_DESCONOCIDO", operador, linea(operadorNodo), columna(operadorNodo),
                "operador binario no reconocido por las reglas semanticas");
        return atributos(TipoDato.ERROR, false, nodo);
    }

    private AtributosNodo evaluarOperacionUnaria(NodoAST nodo) {
        NodoAST operadorNodo = hijo(nodo, "operador");
        String operador = valor(operadorNodo);
        NodoAST operandoNodo = nodo.getHijos().size() > 1 ? nodo.getHijos().get(1) : null;
        AtributosNodo operando = evaluarExpresion(operandoNodo);
        if (operando.getTipo() == TipoDato.ERROR) {
            return atributos(TipoDato.ERROR, false, nodo);
        }

        TipoDato resultado = ReglasTipo.resultadoUnario(operador, operando.getTipo());
        if (resultado == TipoDato.ERROR) {
            registrarError("ERROR_OPERACION_UNARIA_INVALIDA", operador,
                    linea(operadorNodo), columna(operadorNodo),
                    "el operador unario '" + operador + "' solo acepta operandos numericos y se obtuvo "
                            + operando.getTipo());
            return atributos(TipoDato.ERROR, false, nodo);
        }
        return atributos(resultado, operando.esValida(), nodo);
    }

    private Simbolo buscarVariableDestino(NodoAST nombreNodo) {
        String nombre = valor(nombreNodo);
        Simbolo simbolo = tablaSimbolos.buscar(nombre);
        if (simbolo == null) {
            registrarError("ERROR_VARIABLE_NO_DECLARADA", nombre, linea(nombreNodo), columna(nombreNodo),
                    "la variable '" + nombre + "' no ha sido declarada");
        }
        return simbolo;
    }

    private void generarAdvertencias() {
        for (Simbolo simbolo : tablaSimbolos.getTodosLosSimbolos()) {
            if (!simbolo.estaUsado()) {
                advertencias.add(new AdvertenciaSemantica(
                        "ADVERTENCIA_VARIABLE_NO_USADA",
                        simbolo.getNombre(),
                        simbolo.getLineaDeclaracion(),
                        simbolo.getColumnaDeclaracion(),
                        "la variable '" + simbolo.getNombre() + "' fue declarada pero nunca utilizada"
                ));
            }
        }
    }

    private AtributosNodo atributos(TipoDato tipo, boolean esValida, NodoAST nodo) {
        return new AtributosNodo(tipo, esValida, linea(nodo), columna(nodo));
    }

    private NodoAST hijo(NodoAST nodo, String nombre) {
        if (nodo == null) {
            return null;
        }
        for (NodoAST hijo : nodo.getHijos()) {
            if (nombre.equals(hijo.getNombre())) {
                return hijo;
            }
        }
        return null;
    }

    private NodoAST primerHijo(NodoAST nodo) {
        if (nodo == null || nodo.getHijos().isEmpty()) {
            return null;
        }
        return nodo.getHijos().get(0);
    }

    private String valor(NodoAST nodo) {
        if (nodo == null || nodo.getValor() == null) {
            return "";
        }
        return nodo.getValor();
    }

    private String valorVisible(NodoAST nodo) {
        if (nodo == null) {
            return "";
        }
        if (nodo.getValor() != null) {
            return nodo.getValor();
        }
        return nodo.getNombre();
    }

    private int linea(NodoAST nodo) {
        if (nodo == null) {
            return -1;
        }
        if (nodo.getLinea() > 0) {
            return nodo.getLinea();
        }
        for (NodoAST hijo : nodo.getHijos()) {
            int linea = linea(hijo);
            if (linea > 0) {
                return linea;
            }
        }
        return -1;
    }

    private int columna(NodoAST nodo) {
        if (nodo == null) {
            return -1;
        }
        if (nodo.getColumna() > 0) {
            return nodo.getColumna();
        }
        for (NodoAST hijo : nodo.getHijos()) {
            int columna = columna(hijo);
            if (columna > 0) {
                return columna;
            }
        }
        return -1;
    }

    private void registrarError(String tipo, String lexema, int linea, int columna, String descripcion) {
        errores.add(new ErrorSemantico(tipo, lexema, linea, columna, descripcion));
    }
}
