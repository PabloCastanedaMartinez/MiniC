package minic.ui;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

public final class PanelEditorCodigo extends JPanel {
    private final JTextArea editor = new JTextArea();
    private final JTextArea lineas = new JTextArea("1");

    public PanelEditorCodigo() {
        super(new BorderLayout());
        Font fuente = new Font(Font.MONOSPACED, Font.PLAIN, 14);
        editor.setFont(fuente);
        editor.setTabSize(4);
        editor.setLineWrap(false);

        lineas.setFont(fuente);
        lineas.setEditable(false);
        lineas.setFocusable(false);
        lineas.setForeground(new Color(100, 116, 139));
        lineas.setBackground(new Color(241, 245, 249));
        lineas.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 8, 4, 8));

        JScrollPane scroll = new JScrollPane(editor);
        scroll.setRowHeaderView(lineas);
        add(scroll, BorderLayout.CENTER);

        editor.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                actualizarLineas();
            }

            public void removeUpdate(DocumentEvent e) {
                actualizarLineas();
            }

            public void changedUpdate(DocumentEvent e) {
                actualizarLineas();
            }
        });
    }

    public String getCodigo() {
        return editor.getText();
    }

    public void setCodigo(String codigo) {
        editor.setText(codigo == null ? "" : codigo);
        editor.setCaretPosition(0);
        actualizarLineas();
    }

    private void actualizarLineas() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                int total = Math.max(1, editor.getLineCount());
                StringBuilder texto = new StringBuilder();
                for (int i = 1; i <= total; i++) {
                    texto.append(i);
                    if (i < total) {
                        texto.append(System.lineSeparator());
                    }
                }
                lineas.setText(texto.toString());
            }
        });
    }
}
