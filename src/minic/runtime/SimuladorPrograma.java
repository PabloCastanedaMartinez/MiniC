package minic.runtime;

import minic.frontend.ast.NodoAST;
import minic.frontend.semantics.TipoDato;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class SimuladorPrograma {
    private static final int LIMITE_ITERACIONES = 10000;

    private final ArrayDeque<Map<String, Variable>> ambitos = new ArrayDeque<Map<String, Variable>>();
    private final List<String> errores = new ArrayList<String>();
    private final StringBuilder salida = new StringBuilder();
    private boolean detenido;

    public ResultadoEjecucion ejecutar(NodoAST raiz) {
        ambitos.clear();
        errores.clear();
        salida.setLength(0);
        detenido = false;

        abrirAmbito();
        ejecutarSentencia(raiz);
        cerrarAmbito();

        return new ResultadoEjecucion(salida.toString(), errores);
    }

    private void ejecutarSentencia(NodoAST nodo) {
        if (nodo == null || detenido) {
            return;
        }

        String nombre = nodo.getNombre();
        if ("Programa".equals(nombre)) {
            for (NodoAST hijo : nodo.getHijos()) {
                ejecutarSentencia(hijo);
                if (detenido) {
                    return;
                }
            }
            return;
        }
        if ("Bloque".equals(nombre)) {
            ejecutarBloque(nodo);
            return;
        }
        if ("DeclaracionVariable".equals(nombre)) {
            ejecutarDeclaracion(nodo);
            return;
        }
        if ("Asignacion".equals(nombre)) {
            ejecutarAsignacion(nodo);
            return;
        }
        if ("Condicional".equals(nombre)) {
            ejecutarCondicional(nodo);
            return;
        }
        if ("Ciclo".equals(nombre)) {
            ejecutarCiclo(nodo);
            return;
        }
        if ("Salida".equals(nombre)) {
            ejecutarSalida(nodo);
            return;
        }
        if ("Incremento".equals(nombre) || "Decremento".equals(nombre)) {
            ejecutarIncrementoDecremento(nodo);
            return;
        }
        if ("Entrada".equals(nombre)) {
            registrarError(nodo, "la simulacion de la UI no soporta entrada interactiva con 'recolt'");
            return;
        }

        registrarError(nodo, "sentencia no soportada por la simulacion: " + nombre);
    }

    private void ejecutarBloque(NodoAST nodo) {
        abrirAmbito();
        for (NodoAST hijo : nodo.getHijos()) {
            ejecutarSentencia(hijo);
            if (detenido) {
                break;
            }
        }
        cerrarAmbito();
    }

    private void ejecutarDeclaracion(NodoAST nodo) {
        String nombre = valor(hijo(nodo, "nombre"));
        TipoDato tipo = TipoDato.desdeLexema(valor(hijo(nodo, "tipo")));
        if (nombre.length() == 0 || "<?>".equals(nombre)) {
            registrarError(nodo, "declaracion sin nombre de variable valido");
            return;
        }

        Variable variable = new Variable(tipo, null);
        NodoAST valorInicial = primerHijo(hijo(nodo, "valorInicial"));
        if (valorInicial != null) {
            Valor evaluado = evaluar(valorInicial);
            if (detenido) {
                return;
            }
            variable.valor = convertir(tipo, evaluado);
        }
        ambitos.peek().put(nombre, variable);
    }

    private void ejecutarAsignacion(NodoAST nodo) {
        String nombre = valor(hijo(nodo, "nombre"));
        Variable variable = buscarVariable(nombre);
        if (variable == null) {
            registrarError(nodo, "variable no declarada durante la simulacion: " + nombre);
            return;
        }

        Valor evaluado = evaluar(primerHijo(hijo(nodo, "valor")));
        if (!detenido) {
            variable.valor = convertir(variable.tipo, evaluado);
        }
    }

    private void ejecutarCondicional(NodoAST nodo) {
        Valor condicion = evaluar(primerHijo(hijo(nodo, "condicion")));
        if (detenido) {
            return;
        }
        if (comoBooleano(condicion, nodo)) {
            ejecutarSentencia(hijo(nodo, "Bloque"));
        }
    }

    private void ejecutarCiclo(NodoAST nodo) {
        abrirAmbito();
        ejecutarSentencia(primerHijo(hijo(nodo, "inicializacion")));

        int iteraciones = 0;
        while (!detenido) {
            Valor condicion = evaluar(primerHijo(hijo(nodo, "condicion")));
            if (detenido || !comoBooleano(condicion, nodo)) {
                break;
            }
            if (iteraciones++ >= LIMITE_ITERACIONES) {
                registrarError(nodo, "se alcanzo el limite de iteraciones de la simulacion");
                break;
            }
            ejecutarSentencia(hijo(nodo, "Bloque"));
            ejecutarSentencia(primerHijo(hijo(nodo, "actualizacion")));
        }

        cerrarAmbito();
    }

    private void ejecutarSalida(NodoAST nodo) {
        NodoAST argumentos = hijo(nodo, "argumentos");
        List<String> partes = new ArrayList<String>();
        if (argumentos != null) {
            for (NodoAST argumento : argumentos.getHijos()) {
                Valor valor = evaluar(argumento);
                if (detenido) {
                    return;
                }
                partes.add(formatear(valor));
            }
        }
        salida.append(unir(partes, " ")).append(System.lineSeparator());
    }

    private void ejecutarIncrementoDecremento(NodoAST nodo) {
        String nombre = valor(hijo(nodo, "nombre"));
        Variable variable = buscarVariable(nombre);
        if (variable == null || variable.valor == null) {
            registrarError(nodo, "variable no inicializada durante la simulacion: " + nombre);
            return;
        }

        int signo = "Incremento".equals(nodo.getNombre()) ? 1 : -1;
        if (variable.tipo == TipoDato.FLOTAR) {
            double actual = ((Number) variable.valor.valor).doubleValue();
            variable.valor = new Valor(TipoDato.FLOTAR, Double.valueOf(actual + signo));
        } else {
            int actual = ((Number) variable.valor.valor).intValue();
            variable.valor = new Valor(TipoDato.NUM, Integer.valueOf(actual + signo));
        }
    }

    private Valor evaluar(NodoAST nodo) {
        if (nodo == null) {
            registrarError(null, "expresion vacia durante la simulacion");
            return Valor.error();
        }

        String nombre = nodo.getNombre();
        if ("Identificador".equals(nombre)) {
            Variable variable = buscarVariable(nodo.getValor());
            if (variable == null || variable.valor == null) {
                registrarError(nodo, "variable no inicializada durante la simulacion: " + nodo.getValor());
                return Valor.error();
            }
            return variable.valor;
        }
        if ("Entero".equals(nombre)) {
            return new Valor(TipoDato.NUM, Integer.valueOf(Integer.parseInt(nodo.getValor())));
        }
        if ("Flotante".equals(nombre)) {
            return new Valor(TipoDato.FLOTAR, Double.valueOf(Double.parseDouble(nodo.getValor())));
        }
        if ("Caracter".equals(nombre)) {
            return new Valor(TipoDato.LETRA, limpiarLiteral(nodo.getValor()));
        }
        if ("Cadena".equals(nombre)) {
            return new Valor(TipoDato.CADENA, limpiarLiteral(nodo.getValor()));
        }
        if ("OperacionBinaria".equals(nombre)) {
            return evaluarOperacionBinaria(nodo);
        }
        if ("OperacionUnaria".equals(nombre)) {
            return evaluarOperacionUnaria(nodo);
        }
        if (nodo.getHijos().size() == 1) {
            return evaluar(nodo.getHijos().get(0));
        }

        registrarError(nodo, "expresion no soportada por la simulacion: " + nodo.getNombre());
        return Valor.error();
    }

    private Valor evaluarOperacionBinaria(NodoAST nodo) {
        String operador = valor(hijo(nodo, "operador"));
        NodoAST izquierdoNodo = primerHijo(hijo(nodo, "izquierdo"));
        NodoAST derechoNodo = primerHijo(hijo(nodo, "derecho"));

        if ("&&".equals(operador)) {
            Valor izquierdo = evaluar(izquierdoNodo);
            return comoBooleano(izquierdo, nodo) ? evaluarBooleano(derechoNodo, nodo) : new Valor(TipoDato.BOOLEANO_INTERNO, Boolean.FALSE);
        }
        if ("||".equals(operador)) {
            Valor izquierdo = evaluar(izquierdoNodo);
            return comoBooleano(izquierdo, nodo) ? new Valor(TipoDato.BOOLEANO_INTERNO, Boolean.TRUE) : evaluarBooleano(derechoNodo, nodo);
        }

        Valor izquierdo = evaluar(izquierdoNodo);
        Valor derecho = evaluar(derechoNodo);
        if (detenido) {
            return Valor.error();
        }

        if (esAritmetico(operador)) {
            return aplicarAritmetico(operador, izquierdo, derecho, nodo);
        }
        if (esRelacional(operador)) {
            return aplicarRelacional(operador, izquierdo, derecho);
        }

        registrarError(nodo, "operador binario no soportado por la simulacion: " + operador);
        return Valor.error();
    }

    private Valor evaluarBooleano(NodoAST nodo, NodoAST origen) {
        Valor valor = evaluar(nodo);
        return new Valor(TipoDato.BOOLEANO_INTERNO, Boolean.valueOf(comoBooleano(valor, origen)));
    }

    private Valor aplicarAritmetico(String operador, Valor izquierdo, Valor derecho, NodoAST nodo) {
        if (izquierdo.tipo == TipoDato.FLOTAR || derecho.tipo == TipoDato.FLOTAR) {
            double a = ((Number) izquierdo.valor).doubleValue();
            double b = ((Number) derecho.valor).doubleValue();
            if (("/".equals(operador) || "%".equals(operador)) && b == 0.0d) {
                registrarError(nodo, "division entre cero durante la simulacion");
                return Valor.error();
            }
            if ("+".equals(operador)) {
                return new Valor(TipoDato.FLOTAR, Double.valueOf(a + b));
            }
            if ("-".equals(operador)) {
                return new Valor(TipoDato.FLOTAR, Double.valueOf(a - b));
            }
            if ("*".equals(operador)) {
                return new Valor(TipoDato.FLOTAR, Double.valueOf(a * b));
            }
            if ("/".equals(operador)) {
                return new Valor(TipoDato.FLOTAR, Double.valueOf(a / b));
            }
        }

        int a = ((Number) izquierdo.valor).intValue();
        int b = ((Number) derecho.valor).intValue();
        if (("/".equals(operador) || "%".equals(operador)) && b == 0) {
            registrarError(nodo, "division entre cero durante la simulacion");
            return Valor.error();
        }
        if ("+".equals(operador)) {
            return new Valor(TipoDato.NUM, Integer.valueOf(a + b));
        }
        if ("-".equals(operador)) {
            return new Valor(TipoDato.NUM, Integer.valueOf(a - b));
        }
        if ("*".equals(operador)) {
            return new Valor(TipoDato.NUM, Integer.valueOf(a * b));
        }
        if ("/".equals(operador)) {
            return new Valor(TipoDato.NUM, Integer.valueOf(a / b));
        }
        if ("%".equals(operador)) {
            return new Valor(TipoDato.NUM, Integer.valueOf(a % b));
        }

        registrarError(nodo, "operador aritmetico no soportado: " + operador);
        return Valor.error();
    }

    private Valor aplicarRelacional(String operador, Valor izquierdo, Valor derecho) {
        boolean resultado;
        if (izquierdo.tipo.esNumerico() && derecho.tipo.esNumerico()) {
            double a = ((Number) izquierdo.valor).doubleValue();
            double b = ((Number) derecho.valor).doubleValue();
            resultado = compararNumeros(operador, a, b);
        } else {
            boolean iguales = izquierdo.valor.equals(derecho.valor);
            resultado = "!=".equals(operador) || "<>".equals(operador) ? !iguales : iguales;
        }
        return new Valor(TipoDato.BOOLEANO_INTERNO, Boolean.valueOf(resultado));
    }

    private boolean compararNumeros(String operador, double a, double b) {
        if ("==".equals(operador)) {
            return a == b;
        }
        if ("!=".equals(operador) || "<>".equals(operador)) {
            return a != b;
        }
        if (">".equals(operador)) {
            return a > b;
        }
        if ("<".equals(operador)) {
            return a < b;
        }
        if (">=".equals(operador)) {
            return a >= b;
        }
        if ("<=".equals(operador)) {
            return a <= b;
        }
        return false;
    }

    private Valor evaluarOperacionUnaria(NodoAST nodo) {
        String operador = valor(hijo(nodo, "operador"));
        NodoAST operandoNodo = nodo.getHijos().size() > 1 ? nodo.getHijos().get(1) : null;
        Valor operando = evaluar(operandoNodo);
        if (detenido || "+".equals(operador)) {
            return operando;
        }
        if ("-".equals(operador)) {
            if (operando.tipo == TipoDato.FLOTAR) {
                return new Valor(TipoDato.FLOTAR, Double.valueOf(-((Number) operando.valor).doubleValue()));
            }
            return new Valor(TipoDato.NUM, Integer.valueOf(-((Number) operando.valor).intValue()));
        }

        registrarError(nodo, "operador unario no soportado: " + operador);
        return Valor.error();
    }

    private Valor convertir(TipoDato tipoDestino, Valor valor) {
        if (tipoDestino == TipoDato.FLOTAR && valor.tipo == TipoDato.NUM) {
            return new Valor(TipoDato.FLOTAR, Double.valueOf(((Number) valor.valor).doubleValue()));
        }
        if (tipoDestino == TipoDato.NUM && valor.tipo == TipoDato.NUM) {
            return new Valor(TipoDato.NUM, Integer.valueOf(((Number) valor.valor).intValue()));
        }
        return valor;
    }

    private boolean comoBooleano(Valor valor, NodoAST nodo) {
        if (valor.tipo == TipoDato.BOOLEANO_INTERNO && valor.valor instanceof Boolean) {
            return ((Boolean) valor.valor).booleanValue();
        }
        registrarError(nodo, "se esperaba una condicion booleana durante la simulacion");
        return false;
    }

    private String formatear(Valor valor) {
        if (valor == null || valor.valor == null) {
            return "";
        }
        if (valor.tipo == TipoDato.FLOTAR) {
            return Double.toString(((Number) valor.valor).doubleValue());
        }
        return String.valueOf(valor.valor);
    }

    private void abrirAmbito() {
        ambitos.push(new LinkedHashMap<String, Variable>());
    }

    private void cerrarAmbito() {
        ambitos.pop();
    }

    private Variable buscarVariable(String nombre) {
        for (Map<String, Variable> ambito : ambitos) {
            if (ambito.containsKey(nombre)) {
                return ambito.get(nombre);
            }
        }
        return null;
    }

    private boolean esAritmetico(String operador) {
        return "+".equals(operador) || "-".equals(operador) || "*".equals(operador)
                || "/".equals(operador) || "%".equals(operador);
    }

    private boolean esRelacional(String operador) {
        return "==".equals(operador) || "!=".equals(operador) || "<>".equals(operador)
                || ">".equals(operador) || "<".equals(operador)
                || ">=".equals(operador) || "<=".equals(operador);
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
        return nodo == null || nodo.getValor() == null ? "" : nodo.getValor();
    }

    private String limpiarLiteral(String literal) {
        if (literal == null || literal.length() < 2) {
            return literal == null ? "" : literal;
        }
        String contenido = literal.substring(1, literal.length() - 1);
        StringBuilder salida = new StringBuilder();
        for (int i = 0; i < contenido.length(); i++) {
            char actual = contenido.charAt(i);
            if (actual == '\\' && i + 1 < contenido.length()) {
                char siguiente = contenido.charAt(++i);
                if (siguiente == 'n') {
                    salida.append('\n');
                } else if (siguiente == 't') {
                    salida.append('\t');
                } else if (siguiente == 'r') {
                    salida.append('\r');
                } else {
                    salida.append(siguiente);
                }
            } else {
                salida.append(actual);
            }
        }
        return salida.toString();
    }

    private String unir(List<String> valores, String separador) {
        StringBuilder texto = new StringBuilder();
        for (int i = 0; i < valores.size(); i++) {
            if (i > 0) {
                texto.append(separador);
            }
            texto.append(valores.get(i));
        }
        return texto.toString();
    }

    private void registrarError(NodoAST nodo, String descripcion) {
        String ubicacion = nodo == null || nodo.getLinea() < 0
                ? ""
                : "Linea " + nodo.getLinea() + ", columna " + nodo.getColumna() + ": ";
        errores.add("[EJECUCION] " + ubicacion + descripcion);
        detenido = true;
    }

    private static final class Variable {
        private final TipoDato tipo;
        private Valor valor;

        private Variable(TipoDato tipo, Valor valor) {
            this.tipo = tipo == null ? TipoDato.ERROR : tipo;
            this.valor = valor;
        }
    }

    private static final class Valor {
        private final TipoDato tipo;
        private final Object valor;

        private Valor(TipoDato tipo, Object valor) {
            this.tipo = tipo == null ? TipoDato.ERROR : tipo;
            this.valor = valor;
        }

        private static Valor error() {
            return new Valor(TipoDato.ERROR, null);
        }
    }
}
