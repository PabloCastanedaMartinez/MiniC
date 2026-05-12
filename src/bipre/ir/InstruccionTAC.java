package bipre.ir;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class InstruccionTAC {
    private OperadorTAC operador;
    private String argumento1;
    private String argumento2;
    private String resultado;
    private String etiqueta;
    private String comentario;

    public InstruccionTAC(OperadorTAC operador, String argumento1, String argumento2,
            String resultado, String etiqueta, String comentario) {
        this.operador = operador == null ? OperadorTAC.NOP : operador;
        this.argumento1 = normalizar(argumento1);
        this.argumento2 = normalizar(argumento2);
        this.resultado = normalizar(resultado);
        this.etiqueta = normalizar(etiqueta);
        this.comentario = normalizar(comentario);
    }

    public static InstruccionTAC asignacion(String destino, String origen) {
        return new InstruccionTAC(OperadorTAC.ASIGNACION, origen, null, destino, null, null);
    }

    public static InstruccionTAC binaria(OperadorTAC operador, String destino, String izquierda, String derecha) {
        return new InstruccionTAC(operador, izquierda, derecha, destino, null, null);
    }

    public static InstruccionTAC irA(String etiqueta) {
        return new InstruccionTAC(OperadorTAC.GOTO, null, null, null, etiqueta, null);
    }

    public static InstruccionTAC siFalso(String condicion, String etiqueta) {
        return new InstruccionTAC(OperadorTAC.IF_FALSE, condicion, null, null, etiqueta, null);
    }

    public static InstruccionTAC siVerdadero(String condicion, String etiqueta) {
        return new InstruccionTAC(OperadorTAC.IF_TRUE, condicion, null, null, etiqueta, null);
    }

    public static InstruccionTAC etiqueta(String etiqueta) {
        return new InstruccionTAC(OperadorTAC.LABEL, null, null, null, etiqueta, null);
    }

    public static InstruccionTAC leer(String destino) {
        return new InstruccionTAC(OperadorTAC.READ, null, null, destino, null, null);
    }

    public static InstruccionTAC imprimir(String valor) {
        return new InstruccionTAC(OperadorTAC.PRINT, valor, null, null, null, null);
    }

    public InstruccionTAC copia() {
        return new InstruccionTAC(operador, argumento1, argumento2, resultado, etiqueta, comentario);
    }

    public OperadorTAC getOperador() {
        return operador;
    }

    public void setOperador(OperadorTAC operador) {
        this.operador = operador == null ? OperadorTAC.NOP : operador;
    }

    public String getArgumento1() {
        return argumento1;
    }

    public void setArgumento1(String argumento1) {
        this.argumento1 = normalizar(argumento1);
    }

    public String getArgumento2() {
        return argumento2;
    }

    public void setArgumento2(String argumento2) {
        this.argumento2 = normalizar(argumento2);
    }

    public String getResultado() {
        return resultado;
    }

    public void setResultado(String resultado) {
        this.resultado = normalizar(resultado);
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public String getComentario() {
        return comentario;
    }

    public boolean esAsignacion() {
        return operador == OperadorTAC.ASIGNACION;
    }

    public boolean esOperacionBinaria() {
        return operador.esBinario();
    }

    public boolean defineValor() {
        return operador == OperadorTAC.ASIGNACION || operador.esBinario() || operador == OperadorTAC.READ;
    }

    public boolean esEliminableSiMuerta() {
        return operador == OperadorTAC.ASIGNACION || operador.esBinario();
    }

    public List<String> getUsos() {
        List<String> usos = new ArrayList<String>();
        if (operador == OperadorTAC.ASIGNACION || operador.esBinario()
                || operador == OperadorTAC.IF_FALSE || operador == OperadorTAC.IF_TRUE
                || operador == OperadorTAC.PRINT) {
            agregarUso(usos, argumento1);
        }
        if (operador.esBinario()) {
            agregarUso(usos, argumento2);
        }
        return usos;
    }

    public List<String> getDefiniciones() {
        if (defineValor() && esSimbolo(resultado)) {
            return Collections.singletonList(resultado);
        }
        return Collections.emptyList();
    }

    public void reemplazarUso(String original, String reemplazo) {
        if (original == null || reemplazo == null) {
            return;
        }
        if (original.equals(argumento1)) {
            argumento1 = reemplazo;
        }
        if (original.equals(argumento2)) {
            argumento2 = reemplazo;
        }
    }

    public String comoTexto() {
        String texto;
        if (operador == OperadorTAC.LABEL) {
            texto = "label " + etiqueta;
        } else if (operador == OperadorTAC.GOTO) {
            texto = "goto " + etiqueta;
        } else if (operador == OperadorTAC.IF_FALSE) {
            texto = "ifFalse " + argumento1 + " goto " + etiqueta;
        } else if (operador == OperadorTAC.IF_TRUE) {
            texto = "ifTrue " + argumento1 + " goto " + etiqueta;
        } else if (operador == OperadorTAC.READ) {
            texto = "read " + resultado;
        } else if (operador == OperadorTAC.PRINT) {
            texto = "print " + argumento1;
        } else if (operador == OperadorTAC.ASIGNACION) {
            texto = resultado + " = " + argumento1;
        } else if (operador.esBinario()) {
            texto = resultado + " = " + argumento1 + " " + operador.getLexema() + " " + argumento2;
        } else {
            texto = "nop";
        }
        if (comentario != null && comentario.length() > 0) {
            return texto + "  # " + comentario;
        }
        return texto;
    }

    @Override
    public String toString() {
        return comoTexto();
    }

    public static boolean esSimbolo(String valor) {
        if (valor == null || valor.length() == 0) {
            return false;
        }
        if (esConstante(valor)) {
            return false;
        }
        return valor.matches("[A-Za-z_][A-Za-z0-9_]*");
    }

    public static boolean esTemporal(String valor) {
        return valor != null && valor.matches("t[0-9]+");
    }

    public static boolean esConstante(String valor) {
        if (valor == null || valor.length() == 0) {
            return false;
        }
        if (valor.startsWith("\"") && valor.endsWith("\"")) {
            return true;
        }
        if (valor.startsWith("'") && valor.endsWith("'")) {
            return true;
        }
        return valor.matches("-?[0-9]+(\\.[0-9]+)?");
    }

    private static void agregarUso(List<String> usos, String valor) {
        if (esSimbolo(valor)) {
            usos.add(valor);
        }
    }

    private static String normalizar(String valor) {
        if (valor == null) {
            return null;
        }
        return valor.trim();
    }
}
