# Analisis sintactico: BNF y decisiones

Este documento define la sintaxis aceptada por `Parser.java`. La fuente de verdad
para los lexemas sigue siendo `TablaTokens.md` y el lexer existente.

## Nota sobre `flotar`

La tabla actual documenta que `flotar` se clasifica como `NUMERO` con atributo
`float`. El parser no cambia esa decision: reconoce el tipo por lexema y acepta
`num`, `letra` y `flotar`.

## BNF

```bnf
<programa> ::= <lista_sentencias> EOF

<lista_sentencias> ::= <sentencia> <lista_sentencias>
                     | epsilon

<sentencia> ::= <declaracion>
              | <asignacion>
              | <condicional>
              | <ciclo>
              | <entrada>
              | <salida>
              | <bloque>

<declaracion> ::= <tipo_dato> ID ";"
                | <tipo_dato> ID "=" <expresion> ";"

<declaracion_sin_punto_coma> ::= <tipo_dato> ID
                               | <tipo_dato> ID "=" <expresion>

<tipo_dato> ::= "num"
              | "letra"
              | "flotar"

<asignacion> ::= ID "=" <expresion> ";"

<asignacion_sin_punto_coma> ::= ID "=" <expresion>

<condicional> ::= "valdt" "(" <condicion> ")" <bloque>

<ciclo> ::= "ciclar" "(" <inicializacion> ";" <condicion> ";" <actualizacion> ")" <bloque>

<inicializacion> ::= <declaracion_sin_punto_coma>
                   | <asignacion_sin_punto_coma>

<actualizacion> ::= ID "++"
                  | ID "--"
                  | ID "=" <expresion>

<bloque> ::= "{" <lista_sentencias> "}"

<entrada> ::= "recolt" "(" ID ")" ";"

<salida> ::= "estamp" "(" <lista_argumentos_salida> ")" ";"

<lista_argumentos_salida> ::= <expresion> <resto_argumentos_salida>

<resto_argumentos_salida> ::= "," <expresion> <resto_argumentos_salida>
                            | epsilon

<condicion> ::= <expresion>

<expresion> ::= <expresion_or>

<expresion_or> ::= <expresion_and> <expresion_or_tail>

<expresion_or_tail> ::= "||" <expresion_and> <expresion_or_tail>
                      | epsilon

<expresion_and> ::= <expresion_relacional> <expresion_and_tail>

<expresion_and_tail> ::= "&&" <expresion_relacional> <expresion_and_tail>
                       | epsilon

<expresion_relacional> ::= <expresion_aditiva> <expresion_relacional_tail>

<expresion_relacional_tail> ::= OP_COMP <expresion_aditiva> <expresion_relacional_tail>
                              | epsilon

<expresion_aditiva> ::= <expresion_multiplicativa> <expresion_aditiva_tail>

<expresion_aditiva_tail> ::= "+" <expresion_multiplicativa> <expresion_aditiva_tail>
                           | "-" <expresion_multiplicativa> <expresion_aditiva_tail>
                           | epsilon

<expresion_multiplicativa> ::= <expresion_unaria> <expresion_multiplicativa_tail>

<expresion_multiplicativa_tail> ::= "*" <expresion_unaria> <expresion_multiplicativa_tail>
                                  | "/" <expresion_unaria> <expresion_multiplicativa_tail>
                                  | "%" <expresion_unaria> <expresion_multiplicativa_tail>
                                  | epsilon

<expresion_unaria> ::= "+" <expresion_unaria>
                     | "-" <expresion_unaria>
                     | <primario>

<primario> ::= ID
             | ENTERO_LITERAL
             | FLOTANTE_LITERAL
             | CARACTER_LITERAL
             | CADENA_LITERAL
             | "(" <expresion> ")"
```

`OP_COMP` corresponde a los lexemas comparativos reconocidos por el lexer:
`==`, `<>`, `>`, `<`, `>=`, `<=` y `!=`.

## Resolucion de ambiguedades

La precedencia se resuelve separando la gramatica por niveles:

1. Parentesis y primarios.
2. Operadores unarios `+` y `-`.
3. Multiplicacion, division y modulo.
4. Suma y resta.
5. Operadores relacionales.
6. `&&`.
7. `||`.

Los operadores aritmeticos, relacionales y logicos se construyen como binarios
asociativos por la izquierda. El parser lo implementa acumulando el nodo izquierdo
en bucles descendentes recursivos.

La asignacion no es una expresion en esta fase: solo aparece como sentencia o
como inicializacion/actualizacion de `ciclar`. Por eso la asociatividad de
asignacion no aplica todavia. Si en una fase posterior se aceptan asignaciones
anidadas, deben tratarse como asociativas por la derecha.

## Dangling else

La tabla de tokens actual no define una palabra reservada equivalente a `else`.
Por lo tanto, la gramatica de `valdt` solo acepta:

```bnf
<condicional> ::= "valdt" "(" <condicion> ")" <bloque>
```

No se inventa un nuevo lexema. Para una fase posterior, la extension minima seria
agregar una palabra reservada documentada para la rama alternativa y definirla
como asociada al `valdt` mas cercano que aun no tenga alternativa.

## Visualizacion

El parser actualiza los `.dot` durante la construccion y renderiza los `.png` al
terminar el analisis:

- `arbol_concreto.dot`
- `arbol_concreto.png`
- `ast.dot`
- `ast.png`

Los `.dot` son compatibles con Graphviz. Los `.png` se generan con Java2D para
evitar depender de una instalacion local de Graphviz.
