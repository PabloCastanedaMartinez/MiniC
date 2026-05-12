package bipre.runtime;

import bipre.frontend.semantics.TipoDato;

/**
 * Interfaz para solicitar entrada de datos al usuario durante la simulacion del programa.
 */
public interface ProveedorEntrada {
    /**
     * Solicita un valor al usuario para una variable especifica.
     *
     * @param nombreVariable Nombre de la variable que requiere el valor.
     * @param tipo           Tipo de dato esperado.
     * @return El objeto convertido al tipo correspondiente, o null si se cancela.
     * @throws ErrorEntradaUsuario Si ocurre un error irrecuperable en la entrada.
     */
    Object solicitarEntrada(String nombreVariable, TipoDato tipo) throws ErrorEntradaUsuario;
}
