package bipre.runtime;

import bipre.frontend.semantics.TipoDato;

/**
 * Valida y convierte entradas de texto a los tipos de datos del lenguaje MiniC.
 */
public final class ValidadorEntrada {

    /**
     * Convierte una cadena de texto al tipo de dato especificado.
     *
     * @param texto El texto ingresado por el usuario.
     * @param tipo  El tipo de dato esperado.
     * @return El objeto convertido (Integer, Double, Character o String).
     * @throws ErrorEntradaUsuario Si el formato no coincide con el tipo.
     */
    public Object convertir(String texto, TipoDato tipo) throws ErrorEntradaUsuario {
        if (texto == null) {
            return null;
        }

        String trim = texto.trim();

        try {
            switch (tipo) {
                case NUM:
                    return Integer.parseInt(trim);

                case FLOTAR:
                    return Double.parseDouble(trim);

                case LETRA:
                    return validarLetra(trim);

                case CADENA:
                    return trim;

                default:
                    throw new ErrorEntradaUsuario("Tipo de dato no soportado para entrada: " + tipo);
            }
        } catch (NumberFormatException e) {
            String esperado = (tipo == TipoDato.NUM) ? "un numero entero" : "un numero decimal";
            throw new ErrorEntradaUsuario("Se esperaba " + esperado + ", pero se recibio: \"" + texto + "\"");
        }
    }

    private Character validarLetra(String texto) throws ErrorEntradaUsuario {
        if (texto.isEmpty()) {
            throw new ErrorEntradaUsuario("Se esperaba un caracter, pero la entrada esta vacia.");
        }

        // Manejar formato 'A'
        if (texto.length() == 3 && texto.startsWith("'") && texto.endsWith("'")) {
            return texto.charAt(1);
        }

        if (texto.length() != 1) {
            throw new ErrorEntradaUsuario("Se esperaba un unico caracter, pero se recibio: \"" + texto + "\"");
        }

        return texto.charAt(0);
    }
}
