package minic.ui;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public final class MainUI {
    private MainUI() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception ignored) {
                    // El look and feel del sistema es opcional.
                }
                VentanaCompilador ventana = new VentanaCompilador();
                ventana.pack();
                ventana.setVisible(true);
            }
        });
    }
}
