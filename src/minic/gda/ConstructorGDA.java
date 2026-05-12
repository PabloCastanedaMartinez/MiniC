package minic.gda;

import minic.cfg.BloqueBasico;
import minic.ir.InstruccionTAC;
import minic.ir.OperadorTAC;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ConstructorGDA {
    private int siguienteId;
    private GDA gda;
    private Map<String, NodoGDA> hojas;
    private Map<String, NodoGDA> expresiones;
    private Map<String, NodoGDA> etiquetas;

    public GDA construir(BloqueBasico bloque) {
        siguienteId = 1;
        gda = new GDA(bloque == null ? "" : bloque.getId());
        hojas = new LinkedHashMap<String, NodoGDA>();
        expresiones = new LinkedHashMap<String, NodoGDA>();
        etiquetas = new LinkedHashMap<String, NodoGDA>();

        if (bloque == null) {
            return gda;
        }

        for (InstruccionTAC instruccion : bloque.getInstrucciones()) {
            procesar(instruccion);
        }
        return gda;
    }

    private void procesar(InstruccionTAC instruccion) {
        if (instruccion == null) {
            return;
        }
        if (instruccion.getOperador() == OperadorTAC.ASIGNACION) {
            NodoGDA origen = hoja(instruccion.getArgumento1());
            origen.agregarEtiqueta(instruccion.getResultado());
            etiquetas.put(instruccion.getResultado(), origen);
            return;
        }
        if (instruccion.getOperador().esBinario()) {
            NodoGDA izquierdo = hoja(instruccion.getArgumento1());
            NodoGDA derecho = hoja(instruccion.getArgumento2());
            String clave = claveExpresion(instruccion.getOperador(), izquierdo, derecho);
            NodoGDA expresion = expresiones.get(clave);
            if (expresion == null) {
                expresion = new NodoGDA(siguienteId++, instruccion.getOperador().getLexema(), null);
                expresion.agregarHijo(izquierdo);
                expresion.agregarHijo(derecho);
                expresiones.put(clave, expresion);
                gda.agregarNodo(expresion);
            }
            expresion.agregarEtiqueta(instruccion.getResultado());
            etiquetas.put(instruccion.getResultado(), expresion);
        }
    }

    private NodoGDA hoja(String valor) {
        NodoGDA porEtiqueta = etiquetas.get(valor);
        if (porEtiqueta != null) {
            return porEtiqueta;
        }

        String clave = (InstruccionTAC.esConstante(valor) ? "literal:" : "variable:") + valor;
        NodoGDA nodo = hojas.get(clave);
        if (nodo == null) {
            nodo = new NodoGDA(siguienteId++, InstruccionTAC.esConstante(valor) ? "literal" : "variable", valor);
            if (!InstruccionTAC.esConstante(valor)) {
                nodo.agregarEtiqueta(valor);
                etiquetas.put(valor, nodo);
            }
            hojas.put(clave, nodo);
            gda.agregarNodo(nodo);
        }
        return nodo;
    }

    private String claveExpresion(OperadorTAC operador, NodoGDA izquierdo, NodoGDA derecho) {
        int idIzquierdo = izquierdo.getId();
        int idDerecho = derecho.getId();
        if (esConmutativo(operador) && idDerecho < idIzquierdo) {
            int tmp = idIzquierdo;
            idIzquierdo = idDerecho;
            idDerecho = tmp;
        }
        return operador.getLexema() + ":" + idIzquierdo + ":" + idDerecho;
    }

    private boolean esConmutativo(OperadorTAC operador) {
        return operador == OperadorTAC.SUMA
                || operador == OperadorTAC.MULTIPLICACION
                || operador == OperadorTAC.IGUAL
                || operador == OperadorTAC.DIFERENTE
                || operador == OperadorTAC.DIFERENTE_ALT
                || operador == OperadorTAC.AND
                || operador == OperadorTAC.OR;
    }
}
