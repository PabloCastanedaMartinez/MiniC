# Analizador lexico, sintactico y semantico en Java

Este proyecto implementa un analizador lexico manual, un analizador sintactico descendente recursivo y un analizador semantico para un lenguaje pequeno de estilo imperativo. El lexer reconoce palabras reservadas, identificadores, literales, operadores, delimitadores y errores lexicos. El parser consume la lista de tokens producida por el lexer, valida la estructura sintactica y construye un arbol concreto y un AST. El analizador semantico consume ese AST, construye una tabla de simbolos con ambitos y aplica comprobacion fuerte de tipos mediante atributos.

No incluye generacion de codigo ni ejecucion del programa fuente.

## Estructura del proyecto

```txt
src/
  AnalizadorLexico.java
  AnalizadorSemantico.java
  AdvertenciaSemantica.java
  Ambito.java
  ArbolAbstracto.java
  ArbolConcreto.java
  ErrorLexico.java
  ErrorSemantico.java
  ErrorSintactico.java
  Main.java
  NodoAST.java
  NodoArbol.java
  Parser.java
  ReglasTipo.java
  Simbolo.java
  TablaSimbolos.java
  TipoDato.java
  Token.java
  TokenType.java
  VisitadorAST.java
  VisualizadorArbol.java

ejemplos/
  valido.txt
  errores.txt
  semantico_valido.txt
  semantico_errores.txt
  sintactico_valido.txt
  sintactico_error_punto_coma.txt
  sintactico_error_condicion.txt

docs/
  AnalisisSemantico.md
  SintaxisBNF.md
```

Archivos principales:

- `TokenType.java`: enum con todos los tipos de token reconocidos.
- `Token.java`: representa un token reconocido, con tipo, lexema, atributo, linea y columna.
- `ErrorLexico.java`: representa un error lexico, con lexema, linea, columna y descripcion.
- `AnalizadorLexico.java`: contiene la logica del lexer.
- `Parser.java`: consume tokens y valida la gramatica del lenguaje.
- `AnalizadorSemantico.java`: recorre el AST, valida tipos, ambitos, usos e inicializacion.
- `TablaSimbolos.java`, `Ambito.java` y `Simbolo.java`: representan los identificadores declarados.
- `ReglasTipo.java` y `TipoDato.java`: concentran las reglas de compatibilidad fuerte.
- `NodoArbol.java` y `NodoAST.java`: nodos para el arbol concreto y el AST.
- `VisualizadorArbol.java`: exporta los arboles a `.dot` y `.png`.
- `docs/SintaxisBNF.md`: gramatica BNF y decisiones de ambiguedad.
- `docs/AnalisisSemantico.md`: reglas semanticas, gramatica de atributos y decisiones.
- `Main.java`: clase principal para probar el analizador desde consola.

## Requisitos

- Java JDK instalado.
- `javac` y `java` disponibles en la terminal.

Puedes comprobarlo con:

```powershell
java -version
javac -version
```

## Compilar

Desde la raiz del proyecto:

```powershell
mkdir out
javac -d out src\*.java
```

Esto compila las clases Java dentro del directorio `out`.

## Ejecutar con un archivo de entrada

El programa recibe como primer argumento la ruta de un archivo de texto con codigo fuente del lenguaje.

Ejemplo con una entrada valida:

```powershell
java -cp out Main ejemplos\valido.txt
```

Ejemplo con una entrada sintacticamente valida:

```powershell
java -cp out Main ejemplos\sintactico_valido.txt
```

Ejemplo con una entrada que contiene errores lexicos:

```powershell
java -cp out Main ejemplos\errores.txt
```

Ejemplos con errores sintacticos:

```powershell
java -cp out Main ejemplos\sintactico_error_punto_coma.txt
java -cp out Main ejemplos\sintactico_error_condicion.txt
```

Ejemplos para la fase semantica:

```powershell
java -cp out Main ejemplos\semantico_valido.txt
java -cp out Main ejemplos\semantico_errores.txt
```

Tambien puedes pasar cualquier archivo `.txt` propio:

```powershell
java -cp out Main ruta\al\archivo.txt
```

El archivo se lee usando UTF-8.

## Ejecutar sin archivo de entrada

Si ejecutas el programa sin argumentos:

```powershell
java -cp out Main
```

no se utiliza ningun archivo por defecto. En ese caso, `Main` analiza una cadena interna definida en el metodo `ejemploDeEntrada()` de `src/Main.java`.

El programa mostrara:

```txt
Sin archivo de entrada. Se analiza un ejemplo interno.
```

Ese ejemplo interno contiene codigo equivalente a:

```txt
num edad = 18;
flotar promedio = 4.5;
letra inicial = 'P';

valdt (edad >= 18 && promedio >= 3.0) {
    estamp("Estudiante valido", inicial);
}

ciclar (num i = 0; i < 5; i++) {
    estamp(i);
}

recolt(edad);
edad = edad + 1;
```

## Usar el analizador desde codigo Java

Tambien puedes usar el lexer directamente desde otra clase Java, sin pasar por `Main`:

```java
String codigo = "num edad = 18;";

AnalizadorLexico lexer = new AnalizadorLexico(codigo);
List<Token> tokens = lexer.analizar();
List<ErrorLexico> errores = lexer.getErrores();
```

`analizar()` devuelve la lista de tokens reconocidos. Los errores lexicos quedan disponibles con `getErrores()`.

## Salida del programa

Al ejecutar `Main`, la salida se divide en estas partes:

1. Lista de tokens reconocidos.
2. Lista de errores lexicos encontrados.
3. Resultado del analisis sintactico.
4. Arbol concreto en texto.
5. AST en texto.
6. Archivos graficos generados.
7. Resultado del analisis semantico.
8. Tabla de simbolos final.
9. Errores y advertencias semanticas.
10. Resumen final del analisis.

Ejemplo de token:

```txt
TOKEN(tipo=NUMERO, lexema='num', atributo=int, linea=1, columna=1)
```

Ejemplo de error:

```txt
[LEXICO] Linea 1, columna 5: identificador invalido iniciado con numero. Lexema: '123edad'
```

Si no hay errores, se muestra:

```txt
Analisis lexico finalizado correctamente: no se encontraron errores lexicos.
Analisis sintactico finalizado correctamente: no se encontraron errores sintacticos.
Analisis semantico finalizado correctamente: no se encontraron errores semanticos.
```

Si hay errores, se muestra la cantidad encontrada:

```txt
Analisis lexico finalizado con errores: 5 errores lexicos encontrados.
Analisis sintactico finalizado con errores: 2 errores sintacticos encontrados.
Analisis semantico finalizado con errores: 3 errores semanticos encontrados.
```

El parser genera estos archivos en el directorio desde donde se ejecuta el programa:

```txt
arbol_concreto.dot
arbol_concreto.png
ast.dot
ast.png
```

## Tokens reconocidos

Palabras reservadas:

- `num`
- `letra`
- `flotar`
- `valdt`
- `ciclar`
- `estamp`
- `recolt`

Identificadores:

```txt
[a-zA-Z_][a-zA-Z0-9_]*
```

Literales:

- Enteros: `10`, `0`, `250`
- Flotantes: `3.14`, `0.5`, `10.0`
- Caracteres: `'a'`, `'Z'`, `'\n'`
- Cadenas: `"Hola"`

Operadores:

- Aritmeticos: `+`, `-`, `*`, `/`, `%`
- Asignacion: `=`
- Comparativos: `==`, `<>`, `>`, `<`, `>=`, `<=`, `!=`
- Logicos: `&&`, `||`
- Incremento y decremento: `++`, `--`

Delimitadores:

- `(`
- `)`
- `{`
- `}`
- `;`
- `,`

El lexer siempre agrega un token `EOF` al final del analisis.

## Errores lexicos detectados

El analizador registra errores sin detener todo el proceso. Entre los casos manejados estan:

- Caracteres no reconocidos, por ejemplo `@`.
- Identificadores invalidos iniciados con numero, por ejemplo `123edad`.
- Numeros flotantes incompletos, por ejemplo `12.`.
- Cadenas sin cerrar.
- Caracteres sin cerrar.
- Caracteres literales vacios, por ejemplo `''`.
- Caracteres literales invalidos, por ejemplo `'ab'`.
- Operadores logicos incompletos, por ejemplo `&` o `|`.
- Operadores incompletos como `!` sin `=`.

Cada error incluye el lexema problemático, linea, columna y descripcion.

## Analisis semantico

La fase semantica solo se ejecuta si no hay errores lexicos ni sintacticos. Sus
reglas principales son:

- `num`, `flotar` y `letra` son tipos declarables.
- `cadena` se usa para literales de cadena.
- `booleano_interno` se usa solo como tipo sintetizado para condiciones.
- Se permite promocion segura de `num` a `flotar`.
- No se permite asignar `flotar` a `num`, ni mezclar `letra` o `cadena` con
  operadores aritmeticos.
- `&&` y `||` solo aceptan operandos `booleano_interno`.
- `valdt` y la condicion de `ciclar` exigen `booleano_interno`.
- `++` y `--` solo se permiten sobre variables numericas declaradas e
  inicializadas.
- Las variables declaradas y nunca usadas se reportan como advertencias.

Consulta `docs/AnalisisSemantico.md` para las reglas completas y la gramatica de
atributos aplicada sobre el AST.
