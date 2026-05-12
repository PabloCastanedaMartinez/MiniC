package minic.ui;

import minic.ui.service.ResultadoCompilacionUI;
import minic.ui.service.ResultadoFase;
import minic.ui.service.ServicioCompilacion;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class PruebaServicioCompilacion {
    private PruebaServicioCompilacion() {
    }

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.out.println("Uso: java -cp out minic.ui.PruebaServicioCompilacion <archivo>");
            return;
        }

        Path archivo = Paths.get(args[0]);
        String fuente = new String(Files.readAllBytes(archivo), StandardCharsets.UTF_8);
        ResultadoCompilacionUI resultado = new ServicioCompilacion().compilar(fuente);

        System.out.println("FASES:");
        for (ResultadoFase fase : resultado.getFases()) {
            System.out.println("- " + fase.getNombreFase() + ": " + fase.getEstado()
                    + " - " + fase.getMensaje());
        }

        System.out.println();
        System.out.println("SALIDA:");
        System.out.print(resultado.getSalidaFinal());
    }
}
