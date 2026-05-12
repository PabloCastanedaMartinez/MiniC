import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class GeneradorCodigoIntermedio {
    private final List<ErrorGeneracionCodigo> errores = new ArrayList<ErrorGeneracionCodigo>();
    private final GeneradorTemporales temporales = new GeneradorTemporales();
    private final GeneradorEtiquetas etiquetas = new GeneradorEtiquetas();
    private CodigoIntermedio codigo = new CodigoIntermedio();

    public CodigoIntermedio generar(NodoAST raiz, TablaSimbolos tablaSimbolos) {
        errores.clear();
        codigo = new CodigoIntermedio();
        if (raiz == null) {
            registrarError("ERROR_AST_VACIO", "", "no existe AST para generar codigo intermedio");
            return codigo;
        }
        visitarSentencia(raiz);
        return codigo;
    }

    public List<ErrorGeneracionCodigo> getErrores() {
        return Collections.unmodifiableList(errores);
    }

    private void visitarSentencia(NodoAST nodo) {
        if (nodo == null) {
            return;
        }

        String nombre = nodo.getNombre();
        if ("Programa".equals(nombre) || "Bloque".equals(nombre)) {
            for (NodoAST hijo : nodo.getHijos()) {
                visitarSentencia(hijo);
            }
            return;
        }
        if ("DeclaracionVariable".equals(nombre)) {
            generarDeclaracion(nodo);
            return;
        }
        if ("Asignacion".equals(nombre)) {
            generarAsignacion(nodo);
            return;
        }
        if ("Condicional".equals(nombre)) {
            generarCondicional(nodo);
            return;
        }
        if ("Ciclo".equals(nombre)) {
            generarCiclo(nodo);
            return;
        }
        if ("Entrada".equals(nombre)) {
            generarEntrada(nodo);
            return;
        }
        if ("Salida".equals(nombre)) {
            generarSalida(nodo);
            return;
        }
        if ("Incremento".equals(nombre) || "Decremento".equals(nombre)) {
            generarIncrementoDecremento(nodo);
            return;
        }

        registrarError("ERROR_NODO_NO_SOPORTADO", nodo.getEtiqueta(),
                "el nodo no corresponde a una sentencia soportada por TAC");
    }

    private void generarDeclaracion(NodoAST nodo) {
        NodoAST nombreNodo = hijo(nodo, "nombre");
        String nombre = valor(nombreNodo);
        NodoAST expresion = primerHijo(hijo(nodo, "valorInicial"));
        if (expresion != null) {
            String valor = generarExpresion(expresion);
            if (valor != null) {
                codigo.agregar(InstruccionTAC.asignacion(nombre, valor));
            }
        }
    }

    private void generarAsignacion(NodoAST nodo) {
        NodoAST nombreNodo = hijo(nodo, "nombre");
        NodoAST expresion = primerHijo(hijo(nodo, "valor"));
        String destino = valor(nombreNodo);
        String origen = generarExpresion(expresion);
        if (destino.length() > 0 && origen != null) {
            codigo.agregar(InstruccionTAC.asignacion(destino, origen));
        }
    }

    private void generarCondicional(NodoAST nodo) {
        NodoAST condicion = primerHijo(hijo(nodo, "condicion"));
        NodoAST bloque = hijo(nodo, "Bloque");
        String etiquetaFin = etiquetas.nuevaEtiqueta();
        String valorCondicion = generarExpresion(condicion);
        if (valorCondicion == null) {
            return;
        }
        codigo.agregar(InstruccionTAC.siFalso(valorCondicion, etiquetaFin));
        visitarSentencia(bloque);
        codigo.agregar(InstruccionTAC.etiqueta(etiquetaFin));
    }

    private void generarCiclo(NodoAST nodo) {
        NodoAST inicializacion = primerHijo(hijo(nodo, "inicializacion"));
        NodoAST condicion = primerHijo(hijo(nodo, "condicion"));
        NodoAST actualizacion = primerHijo(hijo(nodo, "actualizacion"));
        NodoAST bloque = hijo(nodo, "Bloque");

        visitarSentencia(inicializacion);
        String etiquetaInicio = etiquetas.nuevaEtiqueta();
        String etiquetaFin = etiquetas.nuevaEtiqueta();

        codigo.agregar(InstruccionTAC.etiqueta(etiquetaInicio));
        String valorCondicion = generarExpresion(condicion);
        if (valorCondicion != null) {
            codigo.agregar(InstruccionTAC.siFalso(valorCondicion, etiquetaFin));
        }
        visitarSentencia(bloque);
        visitarSentencia(actualizacion);
        codigo.agregar(InstruccionTAC.irA(etiquetaInicio));
        codigo.agregar(InstruccionTAC.etiqueta(etiquetaFin));
    }

    private void generarEntrada(NodoAST nodo) {
        String nombre = valor(hijo(nodo, "nombre"));
        if (nombre.length() > 0) {
            codigo.agregar(InstruccionTAC.leer(nombre));
        }
    }

    private void generarSalida(NodoAST nodo) {
        NodoAST argumentos = hijo(nodo, "argumentos");
        if (argumentos == null) {
            return;
        }
        for (NodoAST argumento : argumentos.getHijos()) {
            String valor = generarExpresion(argumento);
            if (valor != null) {
                codigo.agregar(InstruccionTAC.imprimir(valor));
            }
        }
    }

    private void generarIncrementoDecremento(NodoAST nodo) {
        String nombre = valor(hijo(nodo, "nombre"));
        if (nombre.length() == 0) {
            return;
        }
        OperadorTAC operador = "Incremento".equals(nodo.getNombre()) ? OperadorTAC.SUMA : OperadorTAC.RESTA;
        codigo.agregar(InstruccionTAC.binaria(operador, nombre, nombre, "1"));
    }

    private String generarExpresion(NodoAST nodo) {
        if (nodo == null) {
            registrarError("ERROR_EXPRESION_INCOMPLETA", "", "no se puede generar codigo para una expresion vacia");
            return null;
        }

        String nombre = nodo.getNombre();
        if ("Identificador".equals(nombre)
                || "Entero".equals(nombre)
                || "Flotante".equals(nombre)
                || "Caracter".equals(nombre)
                || "Cadena".equals(nombre)) {
            return nodo.getValor();
        }
        if ("OperacionBinaria".equals(nombre)) {
            return generarOperacionBinaria(nodo);
        }
        if ("OperacionUnaria".equals(nombre)) {
            return generarOperacionUnaria(nodo);
        }
        if (nodo.getHijos().size() == 1) {
            return generarExpresion(nodo.getHijos().get(0));
        }

        registrarError("ERROR_EXPRESION_NO_SOPORTADA", nodo.getEtiqueta(),
                "el nodo de expresion no tiene traduccion TAC");
        return null;
    }

    private String generarOperacionBinaria(NodoAST nodo) {
        String operadorLexema = valor(hijo(nodo, "operador"));
        NodoAST izquierdoNodo = primerHijo(hijo(nodo, "izquierdo"));
        NodoAST derechoNodo = primerHijo(hijo(nodo, "derecho"));

        if ("&&".equals(operadorLexema)) {
            return generarAndCortocircuito(izquierdoNodo, derechoNodo);
        }
        if ("||".equals(operadorLexema)) {
            return generarOrCortocircuito(izquierdoNodo, derechoNodo);
        }

        String izquierdo = generarExpresion(izquierdoNodo);
        String derecho = generarExpresion(derechoNodo);
        OperadorTAC operador = OperadorTAC.desdeLexema(operadorLexema);
        if (operador == OperadorTAC.NOP || !operador.esBinario()) {
            registrarError("ERROR_OPERADOR_TAC", operadorLexema,
                    "operador binario no reconocido durante la generacion TAC");
            return null;
        }
        if (izquierdo == null || derecho == null) {
            return null;
        }

        String temporal = temporales.nuevoTemporal();
        codigo.agregar(InstruccionTAC.binaria(operador, temporal, izquierdo, derecho));
        return temporal;
    }

    private String generarAndCortocircuito(NodoAST izquierdoNodo, NodoAST derechoNodo) {
        String resultado = temporales.nuevoTemporal();
        String etiquetaFin = etiquetas.nuevaEtiqueta();
        String izquierdo = generarExpresion(izquierdoNodo);
        if (izquierdo == null) {
            return null;
        }

        codigo.agregar(InstruccionTAC.asignacion(resultado, "0"));
        codigo.agregar(InstruccionTAC.siFalso(izquierdo, etiquetaFin));
        String derecho = generarExpresion(derechoNodo);
        if (derecho == null) {
            return null;
        }
        codigo.agregar(InstruccionTAC.siFalso(derecho, etiquetaFin));
        codigo.agregar(InstruccionTAC.asignacion(resultado, "1"));
        codigo.agregar(InstruccionTAC.etiqueta(etiquetaFin));
        return resultado;
    }

    private String generarOrCortocircuito(NodoAST izquierdoNodo, NodoAST derechoNodo) {
        String resultado = temporales.nuevoTemporal();
        String etiquetaFin = etiquetas.nuevaEtiqueta();
        String izquierdo = generarExpresion(izquierdoNodo);
        if (izquierdo == null) {
            return null;
        }

        codigo.agregar(InstruccionTAC.asignacion(resultado, "1"));
        codigo.agregar(InstruccionTAC.siVerdadero(izquierdo, etiquetaFin));
        String derecho = generarExpresion(derechoNodo);
        if (derecho == null) {
            return null;
        }
        codigo.agregar(InstruccionTAC.siVerdadero(derecho, etiquetaFin));
        codigo.agregar(InstruccionTAC.asignacion(resultado, "0"));
        codigo.agregar(InstruccionTAC.etiqueta(etiquetaFin));
        return resultado;
    }

    private String generarOperacionUnaria(NodoAST nodo) {
        String operador = valor(hijo(nodo, "operador"));
        NodoAST operandoNodo = nodo.getHijos().size() > 1 ? nodo.getHijos().get(1) : null;
        String operando = generarExpresion(operandoNodo);
        if (operando == null) {
            return null;
        }
        if ("+".equals(operador)) {
            return operando;
        }
        if ("-".equals(operador)) {
            String temporal = temporales.nuevoTemporal();
            codigo.agregar(InstruccionTAC.binaria(OperadorTAC.RESTA, temporal, "0", operando));
            return temporal;
        }

        registrarError("ERROR_OPERADOR_UNARIO_TAC", operador,
                "operador unario no reconocido durante la generacion TAC");
        return null;
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

    private void registrarError(String tipo, String nodoAST, String descripcion) {
        errores.add(new ErrorGeneracionCodigo(tipo, nodoAST, descripcion));
    }
}
