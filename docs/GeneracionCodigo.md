# Generacion y optimizacion de codigo

Esta fase recibe como entrada el AST validado por el analizador semantico y la tabla de simbolos. No vuelve a hacer analisis lexico, sintactico ni semantico. Si existen errores semanticos, la generacion se detiene y no se produce codigo objeto.

## Representacion intermedia TAC

El compilador genera codigo de tres direcciones con instrucciones `bipre.ir.InstruccionTAC`:

```text
resultado = argumento1 operador argumento2
resultado = argumento1
ifFalse condicion goto Lx
ifTrue condicion goto Lx
goto Lx
label Lx
read x
print x
```

Los temporales se generan como `t1`, `t2`, `t3`, y las etiquetas como `L1`, `L2`, `L3`.

Traducciones principales:

- Declaracion con valor inicial: `num x = 10;` genera `x = 10`.
- Asignacion: `x = a + b * c;` genera temporales segun el AST ya construido.
- Condicional `valdt`: genera condicion, `ifFalse` y etiqueta final.
- Ciclo `ciclar`: genera inicializacion, etiqueta de inicio, condicion, salida, cuerpo, actualizacion y salto al inicio.
- Entrada `recolt`: genera `read x`.
- Salida `estamp`: genera `print x`.
- `&&` y `||`: se traducen con cortocircuito usando saltos y temporales booleanos internos `0` y `1`.

## Bloques basicos y CFG

Los bloques basicos se construyen con estas reglas:

1. La primera instruccion inicia un bloque.
2. Toda instruccion `label` inicia un bloque.
3. La instruccion posterior a `goto`, `ifFalse` o `ifTrue` inicia un bloque.

El CFG enlaza:

- `goto` con el bloque de la etiqueta destino.
- `ifFalse`/`ifTrue` con la etiqueta destino y con el bloque siguiente.
- Bloques sin salto con el bloque siguiente.

Cada bloque conserva sus sucesores y predecesores.

## GDA por bloque

Para cada bloque basico se construye un grafo dirigido aciclico local:

- Hojas: variables y literales.
- Nodos internos: operadores aritmeticos, relacionales o logicos.
- Etiquetas: temporales o variables que contienen el valor del nodo.

El GDA se usa para visualizar redundancias locales y respaldar la eliminacion de subexpresiones comunes dentro de un bloque.

## Analisis de flujo de datos

Se implementa analisis de variables vivas por bloque:

```text
in[B] = use[B] union (out[B] - def[B])
out[B] = union in[S] para cada sucesor S de B
```

El resultado se imprime como `use`, `def`, `in` y `out` por bloque. La eliminacion de codigo muerto usa esta informacion para remover asignaciones cuyo resultado no vive despues de la instruccion.

## Optimizaciones

Las optimizaciones implementadas son:

- Eliminacion de subexpresiones comunes locales.
- Propagacion de copias.
- Propagacion de constantes.
- Plegamiento de constantes.
- Eliminacion de codigo muerto.
- Simplificacion algebraica.
- Simplificacion de saltos constantes.
- Eliminacion de `goto` hacia la siguiente etiqueta.
- Eliminacion de etiquetas no referenciadas.

No se eliminan instrucciones con efectos secundarios como `read`, `print` o saltos necesarios.

## Codigo objeto simbolico

El codigo TAC optimizado se traduce a un ensamblador educativo:

```text
x = y           => LOAD y / STORE x
t1 = a + b      => LOAD a / ADD b / STORE t1
t1 = a >= b     => LOAD a / CMP_GE b / STORE t1
ifFalse t1 goto L2 => LOAD t1 / JZ L2
goto L1         => JMP L1
label L1        => LABEL L1
print x         => PRINT x
read x          => READ x
```

## Archivos graficos

Se generan archivos DOT:

- `cfg.dot`
- `gda_B1.dot`, `gda_B2.dot`, etc.

Si Graphviz esta disponible en `PATH`, tambien se generan los `.png` correspondientes.

## Ejecucion

Compilar:

```powershell
javac -d out src\*.java
```

Ejecutar un ejemplo:

```powershell
java -cp out bipre.app.Main ejemplos\codigo_subexpresion_comun.txt
```

Casos incluidos:

- `ejemplos\codigo_asignacion_simple.txt`
- `ejemplos\codigo_expresion_aritmetica.txt`
- `ejemplos\codigo_subexpresion_comun.txt`
- `ejemplos\codigo_plegamiento_constantes.txt`
- `ejemplos\codigo_propagacion_copias.txt`
- `ejemplos\codigo_condicional.txt`
- `ejemplos\codigo_ciclo.txt`
- `ejemplos\codigo_muerto.txt`
