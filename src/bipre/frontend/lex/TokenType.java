package bipre.frontend.lex;

public enum TokenType {
    NUMERO,
    CARACTER,
    FLOTANTE,
    VALIDAR,
    CICLO,
    ESTAMPAR,
    RECOLECTAR,

    ID,

    OP_ARIT,
    OP_ASIG,
    OP_COMP,
    OP_Y,
    OP_O,
    OP_INC,
    OP_DEC,

    ENTERO_LITERAL,
    FLOTANTE_LITERAL,
    CARACTER_LITERAL,
    CADENA_LITERAL,

    PAREN_IZQ,
    PAREN_DER,
    LLAVE_IZQ,
    LLAVE_DER,
    PUNTO_COMA,
    COMA,

    EOF
}
