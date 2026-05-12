package bipre.ui;

import bipre.ui.service.EstadoFase;
import bipre.ui.service.ResultadoFase;
import bipre.ui.service.ServicioCompilacion;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class PanelEstadoFases extends JPanel {
    private final Map<String, FilaFase> filas = new LinkedHashMap<String, FilaFase>();

    public PanelEstadoFases() {
        super(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        inicializar();
    }

    public void reiniciar() {
        for (String fase : ServicioCompilacion.nombresFases()) {
            actualizar(new ResultadoFase(fase, EstadoFase.NO_EJECUTADO, "Pendiente.", null));
        }
    }

    public void actualizar(ResultadoFase fase) {
        FilaFase fila = filas.get(fase.getNombreFase());
        if (fila != null) {
            fila.actualizar(fase);
        }
    }

    public void actualizar(List<ResultadoFase> fases) {
        if (fases == null) {
            return;
        }
        for (ResultadoFase fase : fases) {
            actualizar(fase);
        }
    }

    private void inicializar() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 8, 0);

        int fila = 0;
        for (String fase : ServicioCompilacion.nombresFases()) {
            FilaFase componente = new FilaFase(fase);
            filas.put(fase, componente);
            gbc.gridy = fila++;
            add(componente, gbc);
        }

        gbc.gridy = fila;
        gbc.weighty = 1.0;
        add(new JPanel(), gbc);
    }

    private static final class FilaFase extends JPanel {
        private final JLabel nombre = new JLabel();
        private final JLabel estado = new JLabel();
        private final JLabel mensaje = new JLabel();

        private FilaFase(String fase) {
            super(new BorderLayout(8, 2));
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(226, 232, 240)),
                    BorderFactory.createEmptyBorder(8, 10, 8, 10)
            ));
            setPreferredSize(new Dimension(260, 68));
            setBackground(Color.WHITE);

            nombre.setText(fase);
            nombre.setFont(nombre.getFont().deriveFont(Font.BOLD));
            mensaje.setForeground(new Color(71, 85, 105));

            add(nombre, BorderLayout.NORTH);
            add(estado, BorderLayout.CENTER);
            add(mensaje, BorderLayout.SOUTH);
            actualizar(new ResultadoFase(fase, EstadoFase.NO_EJECUTADO, "Pendiente.", null));
        }

        private void actualizar(ResultadoFase fase) {
            estado.setText(textoEstado(fase.getEstado()));
            estado.setForeground(colorEstado(fase.getEstado()));
            mensaje.setText(fase.getMensaje());
        }

        private String textoEstado(EstadoFase fase) {
            if (fase == EstadoFase.EN_PROCESO) {
                return "\u23F3 En proceso";
            }
            if (fase == EstadoFase.CORRECTO) {
                return "\u2714 Correcto";
            }
            if (fase == EstadoFase.ERROR) {
                return "\u2716 Error";
            }
            return "\u23F8 No ejecutado";
        }

        private Color colorEstado(EstadoFase fase) {
            if (fase == EstadoFase.EN_PROCESO) {
                return new Color(202, 138, 4);
            }
            if (fase == EstadoFase.CORRECTO) {
                return new Color(22, 101, 52);
            }
            if (fase == EstadoFase.ERROR) {
                return new Color(185, 28, 28);
            }
            return new Color(71, 85, 105);
        }
    }
}
