package bipre.ui;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Font;

public final class PanelSalida extends JPanel {
    private final JTextArea texto = new JTextArea();

    public PanelSalida() {
        super(new BorderLayout());
        texto.setEditable(false);
        texto.setLineWrap(false);
        texto.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        add(new JScrollPane(texto), BorderLayout.CENTER);
    }

    public void setTexto(String valor) {
        texto.setText(valor == null ? "" : valor);
        texto.setCaretPosition(0);
    }

    public void limpiar() {
        setTexto("");
    }
}
