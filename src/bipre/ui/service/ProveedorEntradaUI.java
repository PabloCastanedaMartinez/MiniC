package bipre.ui.service;

import bipre.frontend.semantics.TipoDato;
import bipre.runtime.ErrorEntradaUsuario;
import bipre.runtime.ProveedorEntrada;
import bipre.runtime.ValidadorEntrada;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.lang.reflect.InvocationTargetException;

/**
 * Implementacion de ProveedorEntrada que utiliza cuadros de dialogo de Swing.
 */
public final class ProveedorEntradaUI implements ProveedorEntrada {
    private final Component padre;
    private final ObservadorProgreso observador;
    private final ValidadorEntrada validador = new ValidadorEntrada();

    public ProveedorEntradaUI(Component padre, ObservadorProgreso observador) {
        this.padre = padre;
        this.observador = observador;
    }

    @Override
    public Object solicitarEntrada(final String nombreVariable, final TipoDato tipo) throws ErrorEntradaUsuario {
        notificarEsperando(nombreVariable);

        while (true) {
            final String[] resultado = new String[1];
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        resultado[0] = JOptionPane.showInputDialog(
                                padre,
                                "Ingrese valor para " + nombreVariable + " (tipo " + tipo + "):",
                                "Entrada requerida",
                                JOptionPane.QUESTION_MESSAGE
                        );
                    }
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new ErrorEntradaUsuario("Ejecucion interrumpida durante la entrada.");
            } catch (InvocationTargetException e) {
                throw new ErrorEntradaUsuario("Error interno al mostrar el dialogo de entrada.");
            }

            String valor = resultado[0];
            if (valor == null) {
                return null; // Cancelado por el usuario
            }

            try {
                return validador.convertir(valor, tipo);
            } catch (final ErrorEntradaUsuario e) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        JOptionPane.showMessageDialog(padre, e.getMessage(), "Error de entrada", JOptionPane.ERROR_MESSAGE);
                    }
                });
                // Continua el bucle para reintentar
            }
        }
    }

    private void notificarEsperando(final String nombreVariable) {
        if (observador != null) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    observador.faseActualizada(new ResultadoFase(
                            ServicioCompilacion.FASE_EJECUCION,
                            EstadoFase.EN_PROCESO,
                            "Esperando entrada para " + nombreVariable,
                            null
                    ));
                }
            });
        }
    }
}
