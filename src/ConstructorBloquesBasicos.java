import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class ConstructorBloquesBasicos {
    public List<BloqueBasico> construir(CodigoIntermedio codigo) {
        List<InstruccionTAC> instrucciones = codigo == null
                ? new ArrayList<InstruccionTAC>()
                : codigo.getInstrucciones();
        List<BloqueBasico> bloques = new ArrayList<BloqueBasico>();
        if (instrucciones.isEmpty()) {
            return bloques;
        }

        Set<Integer> lideres = new LinkedHashSet<Integer>();
        lideres.add(Integer.valueOf(0));
        for (int i = 0; i < instrucciones.size(); i++) {
            InstruccionTAC instruccion = instrucciones.get(i);
            if (instruccion.getOperador() == OperadorTAC.LABEL) {
                lideres.add(Integer.valueOf(i));
            }
            if (instruccion.getOperador().esSalto() && i + 1 < instrucciones.size()) {
                lideres.add(Integer.valueOf(i + 1));
            }
        }

        List<Integer> indices = new ArrayList<Integer>(lideres);
        indices.sort(Integer::compareTo);
        for (int i = 0; i < indices.size(); i++) {
            int inicio = indices.get(i).intValue();
            int fin = i + 1 < indices.size() ? indices.get(i + 1).intValue() : instrucciones.size();
            BloqueBasico bloque = new BloqueBasico("B" + (i + 1));
            for (int j = inicio; j < fin; j++) {
                bloque.agregarInstruccion(instrucciones.get(j));
            }
            bloques.add(bloque);
        }
        return bloques;
    }
}
