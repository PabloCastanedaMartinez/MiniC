package bipre.runtime;

/**
 * Excepcion lanzada cuando ocurre un error durante la entrada de datos por parte del usuario.
 */
public class ErrorEntradaUsuario extends RuntimeException {
    public ErrorEntradaUsuario(String mensaje) {
        super(mensaje);
    }
}
