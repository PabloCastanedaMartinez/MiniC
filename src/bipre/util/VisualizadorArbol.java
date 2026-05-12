package bipre.util;

import bipre.frontend.ast.NodoAST;
import bipre.frontend.parse.NodoArbol;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

public final class VisualizadorArbol {
    private static final int ANCHO_NODO = 190;
    private static final int ALTO_NODO = 38;
    private static final int ESPACIO_HORIZONTAL = 28;
    private static final int ESPACIO_VERTICAL = 72;
    private static final int MARGEN = 32;

    private final Path rutaDot;
    private final Path rutaPng;
    private final String nombreGrafo;
    private final Color colorNodo;

    public VisualizadorArbol(Path rutaDot, Path rutaPng, String nombreGrafo, Color colorNodo) {
        this.rutaDot = rutaDot;
        this.rutaPng = rutaPng;
        this.nombreGrafo = nombreGrafo == null ? "Arbol" : nombreGrafo;
        this.colorNodo = colorNodo == null ? new Color(238, 242, 255) : colorNodo;
    }

    public Path getRutaDot() {
        return rutaDot;
    }

    public Path getRutaPng() {
        return rutaPng;
    }

    public void actualizar(NodoArbol raiz) {
        if (raiz == null) {
            return;
        }
        NodoGrafico nodo = convertir(raiz);
        actualizarDot(nodo);
    }

    public void actualizar(NodoAST raiz) {
        if (raiz == null) {
            return;
        }
        NodoGrafico nodo = convertir(raiz);
        actualizarDot(nodo);
    }

    public void exportarFinal(NodoArbol raiz) {
        if (raiz == null) {
            return;
        }
        exportar(convertir(raiz));
    }

    public void exportarFinal(NodoAST raiz) {
        if (raiz == null) {
            return;
        }
        exportar(convertir(raiz));
    }

    private void actualizarDot(NodoGrafico raiz) {
        try {
            escribirDot(raiz);
        } catch (IOException ex) {
            System.err.println("No se pudo actualizar el archivo DOT del arbol: " + ex.getMessage());
        }
    }

    private void exportar(NodoGrafico raiz) {
        try {
            escribirDot(raiz);
            escribirPng(raiz);
        } catch (IOException ex) {
            System.err.println("No se pudo actualizar la visualizacion del arbol: " + ex.getMessage());
        }
    }

    private NodoGrafico convertir(NodoArbol nodo) {
        NodoGrafico grafico = new NodoGrafico(nodo.getId(), nodo.getEtiqueta());
        for (NodoArbol hijo : nodo.getHijos()) {
            grafico.hijos.add(convertir(hijo));
        }
        return grafico;
    }

    private NodoGrafico convertir(NodoAST nodo) {
        NodoGrafico grafico = new NodoGrafico(nodo.getId(), nodo.getEtiqueta());
        for (NodoAST hijo : nodo.getHijos()) {
            grafico.hijos.add(convertir(hijo));
        }
        return grafico;
    }

    private void escribirDot(NodoGrafico raiz) throws IOException {
        crearDirectorioPadre(rutaDot);
        BufferedWriter writer = Files.newBufferedWriter(rutaDot, StandardCharsets.UTF_8);
        try {
            writer.write("digraph ");
            writer.write(nombreGrafo);
            writer.write(" {");
            writer.newLine();
            writer.write("  rankdir=TB;");
            writer.newLine();
            writer.write("  node [shape=box, style=\"rounded,filled\", fontname=\"Arial\", fontsize=10, fillcolor=\"");
            writer.write(colorHex(colorNodo));
            writer.write("\"];");
            writer.newLine();
            writer.write("  edge [color=\"#475569\"];");
            writer.newLine();
            escribirDotNodo(writer, raiz);
            writer.write("}");
            writer.newLine();
        } finally {
            writer.close();
        }
    }

    private void escribirDotNodo(BufferedWriter writer, NodoGrafico nodo) throws IOException {
        writer.write("  n");
        writer.write(Integer.toString(nodo.id));
        writer.write(" [label=\"");
        writer.write(escaparDot(nodo.etiqueta));
        writer.write("\"];");
        writer.newLine();

        for (NodoGrafico hijo : nodo.hijos) {
            writer.write("  n");
            writer.write(Integer.toString(nodo.id));
            writer.write(" -> n");
            writer.write(Integer.toString(hijo.id));
            writer.write(";");
            writer.newLine();
            escribirDotNodo(writer, hijo);
        }
    }

    private void escribirPng(NodoGrafico raiz) throws IOException {
        crearDirectorioPadre(rutaPng);
        Dimension dimension = medir(raiz);
        int ancho = Math.max(320, dimension.width + MARGEN * 2);
        int alto = Math.max(220, dimension.height + MARGEN * 2);
        BufferedImage imagen = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = imagen.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(255, 255, 255));
            g.fillRect(0, 0, ancho, alto);
            g.setFont(new Font("Arial", Font.PLAIN, 12));
            dibujar(g, raiz, MARGEN, MARGEN, dimension.width);
        } finally {
            g.dispose();
        }
        ImageIO.write(imagen, "png", rutaPng.toFile());
    }

    private Dimension medir(NodoGrafico nodo) {
        if (nodo.hijos.isEmpty()) {
            return new Dimension(ANCHO_NODO, ALTO_NODO);
        }

        int anchoHijos = 0;
        int altoHijos = 0;
        for (int i = 0; i < nodo.hijos.size(); i++) {
            Dimension dimensionHijo = medir(nodo.hijos.get(i));
            anchoHijos += dimensionHijo.width;
            if (i > 0) {
                anchoHijos += ESPACIO_HORIZONTAL;
            }
            altoHijos = Math.max(altoHijos, dimensionHijo.height);
        }

        int ancho = Math.max(ANCHO_NODO, anchoHijos);
        int alto = ALTO_NODO + ESPACIO_VERTICAL + altoHijos;
        return new Dimension(ancho, alto);
    }

    private void dibujar(Graphics2D g, NodoGrafico nodo, int x, int y, int anchoDisponible) {
        int nodoX = x + anchoDisponible / 2 - ANCHO_NODO / 2;
        int nodoY = y;
        int centroPadreX = nodoX + ANCHO_NODO / 2;
        int centroPadreY = nodoY + ALTO_NODO;

        int anchoHijos = 0;
        List<Dimension> dimensiones = new ArrayList<Dimension>();
        for (int i = 0; i < nodo.hijos.size(); i++) {
            Dimension dimensionHijo = medir(nodo.hijos.get(i));
            dimensiones.add(dimensionHijo);
            anchoHijos += dimensionHijo.width;
            if (i > 0) {
                anchoHijos += ESPACIO_HORIZONTAL;
            }
        }

        int hijoX = x + (anchoDisponible - anchoHijos) / 2;
        int hijoY = y + ALTO_NODO + ESPACIO_VERTICAL;
        for (int i = 0; i < nodo.hijos.size(); i++) {
            Dimension dimensionHijo = dimensiones.get(i);
            int centroHijoX = hijoX + dimensionHijo.width / 2;
            int centroHijoY = hijoY;
            g.setColor(new Color(71, 85, 105));
            g.setStroke(new BasicStroke(1.2f));
            g.drawLine(centroPadreX, centroPadreY, centroHijoX, centroHijoY);
            dibujar(g, nodo.hijos.get(i), hijoX, hijoY, dimensionHijo.width);
            hijoX += dimensionHijo.width + ESPACIO_HORIZONTAL;
        }

        g.setColor(colorNodo);
        g.fillRoundRect(nodoX, nodoY, ANCHO_NODO, ALTO_NODO, 10, 10);
        g.setColor(new Color(51, 65, 85));
        g.setStroke(new BasicStroke(1.0f));
        g.drawRoundRect(nodoX, nodoY, ANCHO_NODO, ALTO_NODO, 10, 10);

        g.setColor(new Color(15, 23, 42));
        FontMetrics metrics = g.getFontMetrics();
        String etiqueta = ajustarTexto(nodo.etiqueta, metrics, ANCHO_NODO - 16);
        int textoX = nodoX + (ANCHO_NODO - metrics.stringWidth(etiqueta)) / 2;
        int textoY = nodoY + (ALTO_NODO - metrics.getHeight()) / 2 + metrics.getAscent();
        g.drawString(etiqueta, textoX, textoY);
    }

    private static String ajustarTexto(String texto, FontMetrics metrics, int anchoMaximo) {
        if (texto == null) {
            return "";
        }
        if (metrics.stringWidth(texto) <= anchoMaximo) {
            return texto;
        }

        String sufijo = "...";
        String ajustado = texto;
        while (ajustado.length() > 0 && metrics.stringWidth(ajustado + sufijo) > anchoMaximo) {
            ajustado = ajustado.substring(0, ajustado.length() - 1);
        }
        return ajustado + sufijo;
    }

    private static void crearDirectorioPadre(Path ruta) throws IOException {
        if (ruta == null) {
            return;
        }
        Path padre = ruta.toAbsolutePath().getParent();
        if (padre != null) {
            Files.createDirectories(padre);
        }
    }

    private static String escaparDot(String texto) {
        if (texto == null) {
            return "";
        }
        return texto
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private static String colorHex(Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

    private static final class NodoGrafico {
        private final int id;
        private final String etiqueta;
        private final List<NodoGrafico> hijos = new ArrayList<NodoGrafico>();

        private NodoGrafico(int id, String etiqueta) {
            this.id = id;
            this.etiqueta = etiqueta == null ? "" : etiqueta;
        }
    }
}
