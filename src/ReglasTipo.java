public final class ReglasTipo {
    private ReglasTipo() {
    }

    public static boolean compatibleAsignacion(TipoDato destino, TipoDato origen) {
        if (destino == TipoDato.ERROR || origen == TipoDato.ERROR) {
            return false;
        }
        if (destino == origen) {
            return true;
        }
        return destino == TipoDato.FLOTAR && origen == TipoDato.NUM;
    }

    public static TipoDato resultadoAritmetico(String operador, TipoDato izquierdo, TipoDato derecho) {
        if (izquierdo == TipoDato.ERROR || derecho == TipoDato.ERROR) {
            return TipoDato.ERROR;
        }
        if ("%".equals(operador)) {
            return izquierdo == TipoDato.NUM && derecho == TipoDato.NUM ? TipoDato.NUM : TipoDato.ERROR;
        }
        if (!izquierdo.esNumerico() || !derecho.esNumerico()) {
            return TipoDato.ERROR;
        }
        if (izquierdo == TipoDato.FLOTAR || derecho == TipoDato.FLOTAR) {
            return TipoDato.FLOTAR;
        }
        return TipoDato.NUM;
    }

    public static TipoDato resultadoUnario(String operador, TipoDato operando) {
        if (!"+".equals(operador) && !"-".equals(operador)) {
            return TipoDato.ERROR;
        }
        return operando.esNumerico() ? operando : TipoDato.ERROR;
    }

    public static TipoDato resultadoRelacional(String operador, TipoDato izquierdo, TipoDato derecho) {
        if (izquierdo == TipoDato.ERROR || derecho == TipoDato.ERROR) {
            return TipoDato.ERROR;
        }
        if (esIgualdad(operador)) {
            if (izquierdo == derecho) {
                return TipoDato.BOOLEANO_INTERNO;
            }
            if (izquierdo.esNumerico() && derecho.esNumerico()) {
                return TipoDato.BOOLEANO_INTERNO;
            }
            return TipoDato.ERROR;
        }
        if (izquierdo.esNumerico() && derecho.esNumerico()) {
            return TipoDato.BOOLEANO_INTERNO;
        }
        return TipoDato.ERROR;
    }

    public static TipoDato resultadoLogico(String operador, TipoDato izquierdo, TipoDato derecho) {
        if (!"&&".equals(operador) && !"||".equals(operador)) {
            return TipoDato.ERROR;
        }
        return izquierdo == TipoDato.BOOLEANO_INTERNO && derecho == TipoDato.BOOLEANO_INTERNO
                ? TipoDato.BOOLEANO_INTERNO
                : TipoDato.ERROR;
    }

    public static boolean esIncrementable(TipoDato tipo) {
        return tipo == TipoDato.NUM || tipo == TipoDato.FLOTAR;
    }

    public static boolean esOperadorAritmetico(String operador) {
        return "+".equals(operador)
                || "-".equals(operador)
                || "*".equals(operador)
                || "/".equals(operador)
                || "%".equals(operador);
    }

    public static boolean esOperadorLogico(String operador) {
        return "&&".equals(operador) || "||".equals(operador);
    }

    public static boolean esOperadorRelacional(String operador) {
        return "==".equals(operador)
                || "<>".equals(operador)
                || ">".equals(operador)
                || "<".equals(operador)
                || ">=".equals(operador)
                || "<=".equals(operador)
                || "!=".equals(operador);
    }

    public static boolean esIgualdad(String operador) {
        return "==".equals(operador) || "!=".equals(operador) || "<>".equals(operador);
    }
}
