package minic.analysis;

import minic.cfg.BloqueBasico;
import minic.cfg.ConstructorBloquesBasicos;
import minic.cfg.ConstructorCFG;
import minic.cfg.GrafoFlujoControl;
import minic.ir.CodigoIntermedio;
import minic.ir.InstruccionTAC;
import minic.ir.OperadorTAC;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class OptimizadorCodigoIntermedio {
    private final List<String> optimizaciones = new ArrayList<String>();

    public ResultadoOptimizacion optimizar(CodigoIntermedio codigoInicial) {
        optimizaciones.clear();
        CodigoIntermedio despuesLocales = optimizarLocalmente(codigoInicial);
        CodigoIntermedio despuesMuertas = eliminarCodigoMuerto(despuesLocales);
        CodigoIntermedio despuesSaltos = eliminarSaltosRedundantes(despuesMuertas);
        return new ResultadoOptimizacion(despuesSaltos, optimizaciones);
    }

    private CodigoIntermedio optimizarLocalmente(CodigoIntermedio codigoInicial) {
        ConstructorBloquesBasicos constructorBloques = new ConstructorBloquesBasicos();
        List<BloqueBasico> bloques = constructorBloques.construir(codigoInicial);
        CodigoIntermedio salida = new CodigoIntermedio();
        for (BloqueBasico bloque : bloques) {
            OptimizerBloque optimizadorBloque = new OptimizerBloque();
            List<InstruccionTAC> instrucciones = optimizadorBloque.optimizar(bloque.getInstrucciones());
            for (InstruccionTAC instruccion : instrucciones) {
                salida.agregar(instruccion);
            }
        }
        return salida;
    }

    private CodigoIntermedio eliminarCodigoMuerto(CodigoIntermedio codigo) {
        ConstructorBloquesBasicos constructorBloques = new ConstructorBloquesBasicos();
        ConstructorCFG constructorCFG = new ConstructorCFG();
        GrafoFlujoControl cfg = constructorCFG.construir(constructorBloques.construir(codigo));
        AnalisisVariablesVivas analisis = new AnalisisVariablesVivas();
        analisis.analizar(cfg);

        Map<String, List<InstruccionTAC>> porBloque = new LinkedHashMap<String, List<InstruccionTAC>>();
        for (BloqueBasico bloque : cfg.getBloques()) {
            Set<String> vivos = new LinkedHashSet<String>(bloque.getOut());
            List<InstruccionTAC> retenidasInvertidas = new ArrayList<InstruccionTAC>();
            List<InstruccionTAC> instrucciones = bloque.getInstrucciones();
            for (int i = instrucciones.size() - 1; i >= 0; i--) {
                InstruccionTAC instruccion = instrucciones.get(i);
                List<String> definiciones = instruccion.getDefiniciones();
                boolean eliminar = false;
                if (instruccion.esEliminableSiMuerta() && !definiciones.isEmpty()) {
                    eliminar = true;
                    for (String definicion : definiciones) {
                        if (vivos.contains(definicion)) {
                            eliminar = false;
                            break;
                        }
                    }
                }

                if (eliminar) {
                    optimizaciones.add("Codigo muerto eliminado: " + instruccion.comoTexto());
                    continue;
                }

                for (String definicion : definiciones) {
                    vivos.remove(definicion);
                }
                vivos.addAll(instruccion.getUsos());
                retenidasInvertidas.add(instruccion);
            }

            List<InstruccionTAC> retenidas = new ArrayList<InstruccionTAC>();
            for (int i = retenidasInvertidas.size() - 1; i >= 0; i--) {
                retenidas.add(retenidasInvertidas.get(i));
            }
            porBloque.put(bloque.getId(), retenidas);
        }

        CodigoIntermedio salida = new CodigoIntermedio();
        for (List<InstruccionTAC> instrucciones : porBloque.values()) {
            for (InstruccionTAC instruccion : instrucciones) {
                salida.agregar(instruccion);
            }
        }
        return salida;
    }

    private CodigoIntermedio eliminarSaltosRedundantes(CodigoIntermedio codigo) {
        CodigoIntermedio saltosConstantes = simplificarSaltosConstantes(codigo);
        List<InstruccionTAC> instrucciones = saltosConstantes.getInstrucciones();
        Set<String> etiquetasReferenciadas = new HashSet<String>();
        for (InstruccionTAC instruccion : instrucciones) {
            if (instruccion.getOperador() == OperadorTAC.GOTO
                    || instruccion.getOperador() == OperadorTAC.IF_FALSE
                    || instruccion.getOperador() == OperadorTAC.IF_TRUE) {
                etiquetasReferenciadas.add(instruccion.getEtiqueta());
            }
        }

        CodigoIntermedio salida = new CodigoIntermedio();
        for (int i = 0; i < instrucciones.size(); i++) {
            InstruccionTAC instruccion = instrucciones.get(i);
            if (instruccion.getOperador() == OperadorTAC.GOTO
                    && i + 1 < instrucciones.size()
                    && instrucciones.get(i + 1).getOperador() == OperadorTAC.LABEL
                    && instruccion.getEtiqueta().equals(instrucciones.get(i + 1).getEtiqueta())) {
                optimizaciones.add("Salto redundante eliminado: " + instruccion.comoTexto());
                continue;
            }
            if (instruccion.getOperador() == OperadorTAC.LABEL
                    && !etiquetasReferenciadas.contains(instruccion.getEtiqueta())
                    && i != 0) {
                optimizaciones.add("Etiqueta no referenciada eliminada: " + instruccion.getEtiqueta());
                continue;
            }
            salida.agregar(instruccion);
        }
        return salida;
    }

    private CodigoIntermedio simplificarSaltosConstantes(CodigoIntermedio codigo) {
        CodigoIntermedio salida = new CodigoIntermedio();
        for (InstruccionTAC instruccion : codigo.getInstrucciones()) {
            if ((instruccion.getOperador() == OperadorTAC.IF_FALSE
                    || instruccion.getOperador() == OperadorTAC.IF_TRUE)
                    && esNumero(instruccion.getArgumento1())) {
                boolean verdadero = new BigDecimal(instruccion.getArgumento1()).compareTo(BigDecimal.ZERO) != 0;
                if (instruccion.getOperador() == OperadorTAC.IF_FALSE && !verdadero) {
                    InstruccionTAC salto = InstruccionTAC.irA(instruccion.getEtiqueta());
                    salida.agregar(salto);
                    optimizaciones.add("Salto condicional constante simplificado: "
                            + instruccion.comoTexto() + " => " + salto.comoTexto());
                } else if (instruccion.getOperador() == OperadorTAC.IF_TRUE && verdadero) {
                    InstruccionTAC salto = InstruccionTAC.irA(instruccion.getEtiqueta());
                    salida.agregar(salto);
                    optimizaciones.add("Salto condicional constante simplificado: "
                            + instruccion.comoTexto() + " => " + salto.comoTexto());
                } else {
                    optimizaciones.add("Salto condicional constante eliminado: " + instruccion.comoTexto());
                }
                continue;
            }
            salida.agregar(instruccion);
        }
        return salida;
    }

    private final class OptimizerBloque {
        private final Map<String, String> copias = new HashMap<String, String>();
        private final Map<String, String> constantes = new HashMap<String, String>();
        private final Map<String, String> expresiones = new HashMap<String, String>();

        private List<InstruccionTAC> optimizar(List<InstruccionTAC> instrucciones) {
            List<InstruccionTAC> salida = new ArrayList<InstruccionTAC>();
            for (InstruccionTAC original : instrucciones) {
                InstruccionTAC instruccion = original.copia();
                reemplazarUsos(instruccion);

                if (instruccion.getOperador() == OperadorTAC.LABEL
                        || instruccion.getOperador() == OperadorTAC.GOTO
                        || instruccion.getOperador() == OperadorTAC.IF_FALSE
                        || instruccion.getOperador() == OperadorTAC.IF_TRUE
                        || instruccion.getOperador() == OperadorTAC.PRINT) {
                    salida.add(instruccion);
                    continue;
                }

                if (instruccion.getOperador() == OperadorTAC.READ) {
                    matarDefinicion(instruccion.getResultado());
                    salida.add(instruccion);
                    continue;
                }

                if (instruccion.getOperador() == OperadorTAC.ASIGNACION) {
                    procesarAsignacion(instruccion, salida);
                    continue;
                }

                if (instruccion.getOperador().esBinario()) {
                    procesarBinaria(instruccion, salida);
                    continue;
                }

                salida.add(instruccion);
            }
            return salida;
        }

        private void procesarAsignacion(InstruccionTAC instruccion, List<InstruccionTAC> salida) {
            String destino = instruccion.getResultado();
            String origen = resolverValor(instruccion.getArgumento1());
            instruccion.setArgumento1(origen);
            matarDefinicion(destino);

            if (destino != null && destino.equals(origen)) {
                optimizaciones.add("Asignacion redundante eliminada: " + instruccion.comoTexto());
                return;
            }

            if (InstruccionTAC.esConstante(origen)) {
                constantes.put(destino, origen);
            } else if (InstruccionTAC.esSimbolo(origen)) {
                copias.put(destino, origen);
            }
            salida.add(instruccion);
        }

        private void procesarBinaria(InstruccionTAC instruccion, List<InstruccionTAC> salida) {
            matarDefinicion(instruccion.getResultado());

            String plegado = plegarConstantes(instruccion);
            if (plegado != null) {
                InstruccionTAC reemplazo = InstruccionTAC.asignacion(instruccion.getResultado(), plegado);
                constantes.put(instruccion.getResultado(), plegado);
                salida.add(reemplazo);
                optimizaciones.add("Plegamiento de constantes: "
                        + instruccion.comoTexto() + " => " + reemplazo.comoTexto());
                return;
            }

            String simplificado = simplificarAlgebraicamente(instruccion);
            if (simplificado != null) {
                InstruccionTAC reemplazo = InstruccionTAC.asignacion(instruccion.getResultado(), simplificado);
                if (InstruccionTAC.esConstante(simplificado)) {
                    constantes.put(instruccion.getResultado(), simplificado);
                } else if (InstruccionTAC.esSimbolo(simplificado)) {
                    copias.put(instruccion.getResultado(), simplificado);
                }
                salida.add(reemplazo);
                optimizaciones.add("Simplificacion algebraica: "
                        + instruccion.comoTexto() + " => " + reemplazo.comoTexto());
                return;
            }

            String clave = claveExpresion(instruccion);
            String existente = expresiones.get(clave);
            if (existente != null) {
                InstruccionTAC reemplazo = InstruccionTAC.asignacion(instruccion.getResultado(), existente);
                copias.put(instruccion.getResultado(), existente);
                salida.add(reemplazo);
                optimizaciones.add("Subexpresion comun eliminada: "
                        + instruccion.getArgumento1() + " " + instruccion.getOperador().getLexema()
                        + " " + instruccion.getArgumento2() + "; se reutiliza " + existente);
                return;
            }

            expresiones.put(clave, instruccion.getResultado());
            salida.add(instruccion);
        }

        private void reemplazarUsos(InstruccionTAC instruccion) {
            String arg1 = resolverValor(instruccion.getArgumento1());
            String arg2 = resolverValor(instruccion.getArgumento2());
            if (arg1 != null && !arg1.equals(instruccion.getArgumento1())) {
                optimizaciones.add("Propagacion aplicada: "
                        + instruccion.getArgumento1() + " => " + arg1);
                instruccion.setArgumento1(arg1);
            }
            if (arg2 != null && !arg2.equals(instruccion.getArgumento2())) {
                optimizaciones.add("Propagacion aplicada: "
                        + instruccion.getArgumento2() + " => " + arg2);
                instruccion.setArgumento2(arg2);
            }
        }

        private String resolverValor(String valor) {
            if (valor == null) {
                return null;
            }
            String actual = valor;
            Set<String> vistos = new HashSet<String>();
            while (copias.containsKey(actual) && !vistos.contains(actual)) {
                vistos.add(actual);
                actual = copias.get(actual);
            }
            if (constantes.containsKey(actual)) {
                return constantes.get(actual);
            }
            return actual;
        }

        private void matarDefinicion(String simbolo) {
            if (simbolo == null || simbolo.length() == 0) {
                return;
            }
            copias.remove(simbolo);
            constantes.remove(simbolo);
            List<String> copiasBorrar = new ArrayList<String>();
            for (Map.Entry<String, String> entrada : copias.entrySet()) {
                if (simbolo.equals(entrada.getValue())) {
                    copiasBorrar.add(entrada.getKey());
                }
            }
            for (String copia : copiasBorrar) {
                copias.remove(copia);
            }
            List<String> borrar = new ArrayList<String>();
            for (Map.Entry<String, String> entrada : expresiones.entrySet()) {
                if (entrada.getValue().equals(simbolo) || entrada.getKey().contains(":" + simbolo + ":")
                        || entrada.getKey().endsWith(":" + simbolo)) {
                    borrar.add(entrada.getKey());
                }
            }
            for (String clave : borrar) {
                expresiones.remove(clave);
            }
        }

        private String claveExpresion(InstruccionTAC instruccion) {
            String izquierda = instruccion.getArgumento1();
            String derecha = instruccion.getArgumento2();
            if (esConmutativo(instruccion.getOperador()) && derecha.compareTo(izquierda) < 0) {
                String tmp = izquierda;
                izquierda = derecha;
                derecha = tmp;
            }
            return instruccion.getOperador().getLexema() + ":" + izquierda + ":" + derecha;
        }

        private boolean esConmutativo(OperadorTAC operador) {
            return operador == OperadorTAC.SUMA
                    || operador == OperadorTAC.MULTIPLICACION
                    || operador == OperadorTAC.IGUAL
                    || operador == OperadorTAC.DIFERENTE
                    || operador == OperadorTAC.DIFERENTE_ALT
                    || operador == OperadorTAC.AND
                    || operador == OperadorTAC.OR;
        }
    }

    private String plegarConstantes(InstruccionTAC instruccion) {
        String a = instruccion.getArgumento1();
        String b = instruccion.getArgumento2();
        if (!esNumero(a) || !esNumero(b)) {
            return null;
        }
        try {
            OperadorTAC operador = instruccion.getOperador();
            boolean entero = esEntero(a) && esEntero(b);
            if (operador == OperadorTAC.SUMA) {
                return numero(new BigDecimal(a).add(new BigDecimal(b)), entero);
            }
            if (operador == OperadorTAC.RESTA) {
                return numero(new BigDecimal(a).subtract(new BigDecimal(b)), entero);
            }
            if (operador == OperadorTAC.MULTIPLICACION) {
                return numero(new BigDecimal(a).multiply(new BigDecimal(b)), entero);
            }
            if (operador == OperadorTAC.DIVISION) {
                if (new BigDecimal(b).compareTo(BigDecimal.ZERO) == 0) {
                    return null;
                }
                if (entero) {
                    return Integer.toString(Integer.parseInt(a) / Integer.parseInt(b));
                }
                return numero(new BigDecimal(a).divide(new BigDecimal(b), 8, RoundingMode.HALF_UP), false);
            }
            if (operador == OperadorTAC.MODULO && entero && Integer.parseInt(b) != 0) {
                return Integer.toString(Integer.parseInt(a) % Integer.parseInt(b));
            }
            if (operador == OperadorTAC.MENOR) {
                return bool(new BigDecimal(a).compareTo(new BigDecimal(b)) < 0);
            }
            if (operador == OperadorTAC.MAYOR) {
                return bool(new BigDecimal(a).compareTo(new BigDecimal(b)) > 0);
            }
            if (operador == OperadorTAC.MENOR_IGUAL) {
                return bool(new BigDecimal(a).compareTo(new BigDecimal(b)) <= 0);
            }
            if (operador == OperadorTAC.MAYOR_IGUAL) {
                return bool(new BigDecimal(a).compareTo(new BigDecimal(b)) >= 0);
            }
            if (operador == OperadorTAC.IGUAL) {
                return bool(new BigDecimal(a).compareTo(new BigDecimal(b)) == 0);
            }
            if (operador == OperadorTAC.DIFERENTE || operador == OperadorTAC.DIFERENTE_ALT) {
                return bool(new BigDecimal(a).compareTo(new BigDecimal(b)) != 0);
            }
        } catch (ArithmeticException | NumberFormatException ex) {
            return null;
        }
        return null;
    }

    private String simplificarAlgebraicamente(InstruccionTAC instruccion) {
        String a = instruccion.getArgumento1();
        String b = instruccion.getArgumento2();
        OperadorTAC operador = instruccion.getOperador();
        if (operador == OperadorTAC.SUMA) {
            if (esCero(b)) {
                return a;
            }
            if (esCero(a)) {
                return b;
            }
        }
        if (operador == OperadorTAC.RESTA && esCero(b)) {
            return a;
        }
        if (operador == OperadorTAC.MULTIPLICACION) {
            if (esUno(b)) {
                return a;
            }
            if (esUno(a)) {
                return b;
            }
            if (esCero(a) || esCero(b)) {
                return "0";
            }
        }
        if (operador == OperadorTAC.DIVISION && esUno(b)) {
            return a;
        }
        if (operador == OperadorTAC.MODULO && esUno(b)) {
            return "0";
        }
        return null;
    }

    private boolean esNumero(String valor) {
        return valor != null && valor.matches("-?[0-9]+(\\.[0-9]+)?");
    }

    private boolean esEntero(String valor) {
        return valor != null && valor.matches("-?[0-9]+");
    }

    private boolean esCero(String valor) {
        return esNumero(valor) && new BigDecimal(valor).compareTo(BigDecimal.ZERO) == 0;
    }

    private boolean esUno(String valor) {
        return esNumero(valor) && new BigDecimal(valor).compareTo(BigDecimal.ONE) == 0;
    }

    private String numero(BigDecimal valor, boolean entero) {
        BigDecimal limpio = valor.stripTrailingZeros();
        if (entero) {
            return limpio.toPlainString().split("\\.")[0];
        }
        return limpio.toPlainString();
    }

    private String bool(boolean valor) {
        return valor ? "1" : "0";
    }
}
