import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public final class ExportadorDot {
    private final List<String> archivosGenerados = new ArrayList<String>();

    public List<String> exportar(GrafoFlujoControl cfg, List<GDA> gdas) {
        archivosGenerados.clear();
        exportarCFG(cfg, Paths.get("cfg.dot"));
        if (gdas != null) {
            for (GDA gda : gdas) {
                exportarGDA(gda, Paths.get("gda_" + gda.getBloqueId() + ".dot"));
            }
        }
        return new ArrayList<String>(archivosGenerados);
    }

    private void exportarCFG(GrafoFlujoControl cfg, Path ruta) {
        try {
            crearDirectorioPadre(ruta);
            BufferedWriter writer = Files.newBufferedWriter(ruta, StandardCharsets.UTF_8);
            try {
                writer.write("digraph CFG {");
                writer.newLine();
                writer.write("  rankdir=TB;");
                writer.newLine();
                writer.write("  node [shape=box, style=\"rounded,filled\", fontname=\"Arial\", fillcolor=\"#e0f2fe\"];");
                writer.newLine();
                if (cfg != null) {
                    for (BloqueBasico bloque : cfg.getBloques()) {
                        writer.write("  ");
                        writer.write(bloque.getId());
                        writer.write(" [label=\"");
                        writer.write(escapar(etiquetaBloque(bloque)));
                        writer.write("\"];");
                        writer.newLine();
                    }
                    for (BloqueBasico bloque : cfg.getBloques()) {
                        for (BloqueBasico sucesor : bloque.getSucesores()) {
                            writer.write("  ");
                            writer.write(bloque.getId());
                            writer.write(" -> ");
                            writer.write(sucesor.getId());
                            writer.write(";");
                            writer.newLine();
                        }
                    }
                }
                writer.write("}");
                writer.newLine();
            } finally {
                writer.close();
            }
            registrar(ruta);
            intentarPng(ruta);
        } catch (IOException ex) {
            System.err.println("No se pudo exportar cfg.dot: " + ex.getMessage());
        }
    }

    private void exportarGDA(GDA gda, Path ruta) {
        try {
            crearDirectorioPadre(ruta);
            BufferedWriter writer = Files.newBufferedWriter(ruta, StandardCharsets.UTF_8);
            try {
                writer.write("digraph GDA_" + sanitizar(gda.getBloqueId()) + " {");
                writer.newLine();
                writer.write("  rankdir=TB;");
                writer.newLine();
                writer.write("  node [shape=ellipse, style=filled, fontname=\"Arial\", fillcolor=\"#ecfccb\"];");
                writer.newLine();
                for (NodoGDA nodo : gda.getNodos()) {
                    writer.write("  n");
                    writer.write(Integer.toString(nodo.getId()));
                    writer.write(" [label=\"");
                    writer.write(escapar(nodo.etiquetaVisible()));
                    writer.write("\"];");
                    writer.newLine();
                    for (NodoGDA hijo : nodo.getHijos()) {
                        writer.write("  n");
                        writer.write(Integer.toString(nodo.getId()));
                        writer.write(" -> n");
                        writer.write(Integer.toString(hijo.getId()));
                        writer.write(";");
                        writer.newLine();
                    }
                }
                writer.write("}");
                writer.newLine();
            } finally {
                writer.close();
            }
            registrar(ruta);
            intentarPng(ruta);
        } catch (IOException ex) {
            System.err.println("No se pudo exportar " + ruta + ": " + ex.getMessage());
        }
    }

    private String etiquetaBloque(BloqueBasico bloque) {
        StringBuilder salida = new StringBuilder();
        salida.append(bloque.getId());
        int indice = 1;
        for (InstruccionTAC instruccion : bloque.getInstrucciones()) {
            salida.append("\\n").append(indice++).append(": ").append(instruccion.comoTexto());
        }
        return salida.toString();
    }

    private void registrar(Path ruta) {
        archivosGenerados.add(ruta.toString());
    }

    private void intentarPng(Path rutaDot) {
        Path rutaPng = reemplazarExtension(rutaDot, ".png");
        try {
            Process proceso = new ProcessBuilder("dot", "-Tpng", rutaDot.toString(), "-o", rutaPng.toString())
                    .redirectErrorStream(true)
                    .start();
            int codigo = proceso.waitFor();
            if (codigo == 0 && Files.exists(rutaPng)) {
                archivosGenerados.add(rutaPng.toString());
            }
        } catch (IOException ex) {
            // Graphviz no esta instalado o no esta en PATH; el DOT queda disponible.
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    private Path reemplazarExtension(Path ruta, String extension) {
        String texto = ruta.toString();
        int punto = texto.lastIndexOf('.');
        if (punto >= 0) {
            texto = texto.substring(0, punto);
        }
        return Paths.get(texto + extension);
    }

    private static void crearDirectorioPadre(Path ruta) throws IOException {
        Path padre = ruta.toAbsolutePath().getParent();
        if (padre != null) {
            Files.createDirectories(padre);
        }
    }

    private static String escapar(String texto) {
        if (texto == null) {
            return "";
        }
        return texto
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }

    private static String sanitizar(String texto) {
        if (texto == null || texto.length() == 0) {
            return "bloque";
        }
        return texto.replaceAll("[^A-Za-z0-9_]", "_");
    }
}
