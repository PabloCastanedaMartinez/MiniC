# Analisis semantico: reglas, atributos y decisiones

Esta fase consume el AST producido por `minic.frontend.parse.Parser.java`. No vuelve a ejecutar el
lexer ni el parser, no ejecuta el programa fuente y no genera codigo.

## Tipos del lenguaje

- `num`: entero.
- `flotar`: flotante.
- `letra`: caracter.
- `cadena`: tipo de literales de cadena, usado principalmente en `estamp`.
- `booleano_interno`: tipo sintetizado por expresiones relacionales y logicas.
- `error`: tipo interno para recuperacion semantica.

`booleano_interno` no es declarable por el usuario porque no existe una palabra
reservada booleana en la tabla de tokens.

## Ambitos y tabla de simbolos

El analizador crea un ambito global y un nuevo ambito al entrar a cada bloque.
El ciclo `ciclar` abre un ambito propio para que la variable de inicializacion
sea visible en la condicion, la actualizacion y el bloque del ciclo.

Reglas aplicadas:

- Una variable se puede buscar en el ambito actual y en sus ambitos padres.
- Se prohibe redeclarar una variable dentro del mismo ambito.
- Se permite sombreado en ambitos internos; la tabla distingue las variables por
  ambito.
- `recolt(ID)` marca la variable como inicializada.
- Asignaciones validas marcan la variable destino como inicializada.
- Usar un identificador en expresiones, condiciones o `estamp` lo marca como
  usado.

Cada simbolo guarda:

```txt
nombre, tipo, categoria, lineaDeclaracion, columnaDeclaracion,
ambito, inicializado, usado
```

## Gramatica de atributos aplicada

Declaracion sin inicializacion:

```txt
DeclaracionVariable.tipoDeclarado = tipo.lexema
DeclaracionVariable.nombre = ID.lexema
insertar(ID.nombre, tipoDeclarado)
ID.inicializado = false
```

Declaracion con inicializacion:

```txt
DeclaracionVariable.tipoDeclarado = tipo.lexema
valorInicial.tipo = evaluarTipo(expresion)
compatible = compatibleAsignacion(tipoDeclarado, valorInicial.tipo)
si compatible:
    insertar(ID.nombre, tipoDeclarado)
    ID.inicializado = true
si no compatible:
    reportar ERROR_TIPO_INCOMPATIBLE
```

Asignacion:

```txt
ID.tipo = buscar(ID.nombre)
expresion.tipo = evaluarTipo(expresion)
compatible = compatibleAsignacion(ID.tipo, expresion.tipo)
si compatible:
    ID.inicializado = true
si no compatible:
    reportar ERROR_TIPO_INCOMPATIBLE
```

Identificador en expresion:

```txt
ID.declarado = existeEnTabla(ID.nombre)
ID.tipo = buscar(ID.nombre).tipo
ID.inicializado = buscar(ID.nombre).inicializado
si no declarado:
    reportar ERROR_VARIABLE_NO_DECLARADA
si declarado y no inicializado:
    reportar ERROR_VARIABLE_NO_INICIALIZADA
```

Condicion:

```txt
condicion.tipo = evaluarTipo(expresion)
si condicion.tipo != booleano_interno:
    reportar ERROR_CONDICION_NO_BOOLEANA
```

## Reglas de tipos

Asignacion:

- `num = num` permitido.
- `flotar = flotar` permitido.
- `flotar = num` permitido por promocion segura.
- `num = flotar` no permitido.
- `letra = letra` permitido.
- `letra = num`, `num = letra` y mezclas con `cadena` no permitidas.

Operadores aritmeticos:

- `num` con `num` produce `num`.
- Si participa `flotar`, el resultado es `flotar`.
- `/` entre dos `num` produce `num` en esta fase.
- `%` solo acepta `num % num` y produce `num`.
- `letra`, `cadena` y `booleano_interno` no son operandos aritmeticos validos.

Operadores relacionales:

- Comparaciones numericas entre `num` y `flotar` son validas y producen
  `booleano_interno`.
- `==`, `!=` y `<>` se tratan como igualdad/desigualdad.
- Igualdad/desigualdad acepta tipos exactamente iguales o numericos compatibles.
- `<`, `>`, `<=` y `>=` solo aceptan tipos numericos.

Operadores logicos:

- `&&` y `||` solo aceptan operandos `booleano_interno`.
- El resultado de ambos operadores es `booleano_interno`.

Incremento y decremento:

- `++` y `--` solo se permiten sobre variables `num` o `flotar`.
- La variable debe estar declarada e inicializada.

Entrada y salida:

- `recolt(ID)` exige un identificador declarado y asignable.
- `estamp(...)` acepta literales, identificadores declarados y expresiones
  semanticamente validas.
- Si `estamp` usa una variable no inicializada, se reporta error.

## Errores y advertencias

Errores semanticos implementados:

- `ERROR_VARIABLE_NO_DECLARADA`
- `ERROR_REDECLARACION_VARIABLE`
- `ERROR_VARIABLE_NO_INICIALIZADA`
- `ERROR_TIPO_INCOMPATIBLE`
- `ERROR_OPERACION_ARITMETICA_INVALIDA`
- `ERROR_OPERACION_LOGICA_INVALIDA`
- `ERROR_COMPARACION_INCOMPATIBLE`
- `ERROR_INCREMENTO_INVALIDO`
- `ERROR_CONDICION_NO_BOOLEANA`
- `ERROR_ARGUMENTO_RECOLT_INVALIDO`
- `ERROR_ARGUMENTO_ESTAMP_INVALIDO`

Advertencia implementada:

- `ADVERTENCIA_VARIABLE_NO_USADA`

## Ejemplos

Caso valido:

```powershell
java -cp out minic.app.Main ejemplos\semantico_valido.txt
```

Caso invalido:

```powershell
java -cp out minic.app.Main ejemplos\semantico_errores.txt
```
