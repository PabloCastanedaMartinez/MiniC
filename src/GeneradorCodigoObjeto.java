public final class GeneradorCodigoObjeto {
    public CodigoObjeto generar(CodigoIntermedio codigo) {
        CodigoObjeto objeto = new CodigoObjeto();
        if (codigo == null) {
            return objeto;
        }
        for (InstruccionTAC instruccion : codigo.getInstrucciones()) {
            traducir(instruccion, objeto);
        }
        return objeto;
    }

    private void traducir(InstruccionTAC instruccion, CodigoObjeto objeto) {
        OperadorTAC operador = instruccion.getOperador();
        if (operador == OperadorTAC.ASIGNACION) {
            objeto.agregar("LOAD", instruccion.getArgumento1());
            objeto.agregar("STORE", instruccion.getResultado());
            return;
        }
        if (operador.esBinario()) {
            objeto.agregar("LOAD", instruccion.getArgumento1());
            objeto.agregar(operacionObjeto(operador), instruccion.getArgumento2());
            objeto.agregar("STORE", instruccion.getResultado());
            return;
        }
        if (operador == OperadorTAC.IF_FALSE) {
            objeto.agregar("LOAD", instruccion.getArgumento1());
            objeto.agregar("JZ", instruccion.getEtiqueta());
            return;
        }
        if (operador == OperadorTAC.IF_TRUE) {
            objeto.agregar("LOAD", instruccion.getArgumento1());
            objeto.agregar("JNZ", instruccion.getEtiqueta());
            return;
        }
        if (operador == OperadorTAC.GOTO) {
            objeto.agregar("JMP", instruccion.getEtiqueta());
            return;
        }
        if (operador == OperadorTAC.LABEL) {
            objeto.agregar("LABEL", instruccion.getEtiqueta());
            return;
        }
        if (operador == OperadorTAC.PRINT) {
            objeto.agregar("PRINT", instruccion.getArgumento1());
            return;
        }
        if (operador == OperadorTAC.READ) {
            objeto.agregar("READ", instruccion.getResultado());
        }
    }

    private String operacionObjeto(OperadorTAC operador) {
        if (operador == OperadorTAC.SUMA) {
            return "ADD";
        }
        if (operador == OperadorTAC.RESTA) {
            return "SUB";
        }
        if (operador == OperadorTAC.MULTIPLICACION) {
            return "MUL";
        }
        if (operador == OperadorTAC.DIVISION) {
            return "DIV";
        }
        if (operador == OperadorTAC.MODULO) {
            return "MOD";
        }
        if (operador == OperadorTAC.MENOR) {
            return "CMP_LT";
        }
        if (operador == OperadorTAC.MAYOR) {
            return "CMP_GT";
        }
        if (operador == OperadorTAC.MENOR_IGUAL) {
            return "CMP_LE";
        }
        if (operador == OperadorTAC.MAYOR_IGUAL) {
            return "CMP_GE";
        }
        if (operador == OperadorTAC.IGUAL) {
            return "CMP_EQ";
        }
        if (operador == OperadorTAC.DIFERENTE || operador == OperadorTAC.DIFERENTE_ALT) {
            return "CMP_NE";
        }
        if (operador == OperadorTAC.AND) {
            return "AND";
        }
        if (operador == OperadorTAC.OR) {
            return "OR";
        }
        return "OP_" + operador.getLexema();
    }
}
