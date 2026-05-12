package bipre.ui;

import bipre.runtime.ProveedorEntrada;
import bipre.ui.service.ObservadorProgreso;
import bipre.ui.service.ResultadoCompilacionUI;
import bipre.ui.service.ResultadoFase;
import bipre.ui.service.ServicioCompilacion;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public final class VentanaCompilador extends JFrame {
    private final PanelEditorCodigo panelEditor = new PanelEditorCodigo();
    private final PanelEstadoFases panelEstado = new PanelEstadoFases();
    private final PanelSalida panelSalida = new PanelSalida();
    private final PanelSalida panelDetalles = new PanelSalida();
    private final JButton botonCompilar = new JButton("Compilar");

    public VentanaCompilador() {
        super("Compilador BIPRE");
        configurarVentana();
        construirUI();
        cargarEjemplo();
    }

    private void configurarVentana() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(980, 680));
        setLocationByPlatform(true);
    }

    private void construirUI() {
        JPanel contenedor = new JPanel(new BorderLayout(10, 10));
        contenedor.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        contenedor.setBackground(new Color(248, 250, 252));

        JPanel encabezado = new JPanel(new BorderLayout(10, 0));
        encabezado.setOpaque(false);
        JLabel titulo = new JLabel("Compilador BIPRE");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 20f));
        encabezado.add(titulo, BorderLayout.WEST);
        encabezado.add(botonCompilar, BorderLayout.EAST);

        JPanel editorConTitulo = panelConTitulo("Codigo fuente", panelEditor);
        JPanel fasesConTitulo = panelConTitulo("Estado de compilacion", panelEstado);
        JSplitPane divisionCentral = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, editorConTitulo, fasesConTitulo);
        divisionCentral.setResizeWeight(0.72);
        divisionCentral.setContinuousLayout(true);
        divisionCentral.setBorder(null);

        JTabbedPane pestanas = new JTabbedPane();
        pestanas.addTab("Salida", panelSalida);
        pestanas.addTab("Detalles", panelDetalles);
        pestanas.setPreferredSize(new Dimension(100, 190));

        contenedor.add(encabezado, BorderLayout.NORTH);
        contenedor.add(divisionCentral, BorderLayout.CENTER);
        contenedor.add(pestanas, BorderLayout.SOUTH);
        setContentPane(contenedor);

        botonCompilar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                compilar();
            }
        });
    }

    private JPanel panelConTitulo(String titulo, JPanel contenido) {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setOpaque(false);
        JLabel etiqueta = new JLabel(titulo);
        etiqueta.setFont(etiqueta.getFont().deriveFont(Font.BOLD));
        panel.add(etiqueta, BorderLayout.NORTH);
        panel.add(contenido, BorderLayout.CENTER);
        return panel;
    }

    private void cargarEjemplo() {
        panelEditor.setCodigo(String.join(System.lineSeparator(),
                "num contador = 10;",
                "flotar precio = 10;",
                "num total = 5 + 3 * 2;",
                "",
                "valdt (contador >= 10) {",
                "    estamp(contador);",
                "}",
                "",
                "ciclar (num i = 0; i < 10; i++) {",
                "    estamp(i);",
                "}",
                "",
                "estamp(precio, total);"
        ));
    }

    private void compilar() {
        botonCompilar.setEnabled(false);
        panelEstado.reiniciar();
        panelSalida.limpiar();
        panelDetalles.limpiar();

        final String fuente = panelEditor.getCodigo();
        SwingWorker<ResultadoCompilacionUI, Void> worker = new SwingWorker<ResultadoCompilacionUI, Void>() {
            protected ResultadoCompilacionUI doInBackground() {
                ObservadorProgreso observador = new ObservadorProgreso() {
                    public void faseActualizada(final ResultadoFase fase) {
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                panelEstado.actualizar(fase);
                            }
                        });
                    }
                };

                ProveedorEntrada proveedor = new bipre.ui.service.ProveedorEntradaUI(VentanaCompilador.this, observador);
                ServicioCompilacion servicio = new ServicioCompilacion(observador, proveedor);
                return servicio.compilar(fuente);
            }

            protected void done() {
                try {
                    ResultadoCompilacionUI resultado = get();
                    panelEstado.actualizar(resultado.getFases());
                    if (resultado.isExitoGeneral()) {
                        panelSalida.setTexto(resultado.getSalidaFinal().length() == 0
                                ? "(programa valido sin salida)"
                                : resultado.getSalidaFinal());
                    } else {
                        panelSalida.setTexto(resultado.getSalidaFinal());
                    }
                    panelDetalles.setTexto(construirDetalles(resultado));
                } catch (Exception ex) {
                    panelSalida.setTexto("Error interno de UI: " + ex.getMessage());
                } finally {
                    botonCompilar.setEnabled(true);
                }
            }
        };
        worker.execute();
    }

    private String construirDetalles(ResultadoCompilacionUI resultado) {
        StringBuilder detalles = new StringBuilder();
        for (ResultadoFase fase : resultado.getFases()) {
            detalles.append(fase.getNombreFase())
                    .append(" - ")
                    .append(fase.getEstado())
                    .append(": ")
                    .append(fase.getMensaje())
                    .append(System.lineSeparator());
            for (String detalle : fase.getDetalles()) {
                detalles.append("  ").append(detalle).append(System.lineSeparator());
            }
        }
        if (resultado.getDetallesTecnicos().length() > 0) {
            detalles.append(System.lineSeparator()).append(resultado.getDetallesTecnicos());
        }
        return detalles.toString();
    }
}
