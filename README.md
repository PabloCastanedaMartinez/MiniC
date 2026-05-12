# Analizador lexico y sintactico en Java

Este proyecto implementa un analizador lexico manual y un analizador sintactico descendente recursivo para un lenguaje pequeno de estilo imperativo. El lexer reconoce palabras reservadas, identificadores, literales, operadores, delimitadores y errores lexicos segun la tabla de tokens definida para esta fase. El parser consume la lista de tokens producida por el lexer, valida la estructura sintactica y construye un arbol concreto y un AST.

No incluye analisis semantico, generacion de codigo ni ejecucion del programa fuente.

## Estructura del proyecto

```txt
src/
  AnalizadorLexico.java
  ArbolAbstracto.java
  ArbolConcreto.java
  ErrorLexico.java
  ErrorSintactico.java
  Main.java
  NodoAST.java
  NodoArbol.java
  Parser.java
  Token.java
  TokenType.java
  VisualizadorArbol.java

ejemplos/
  valido.txt
  errores.txt
  sintactico_valido.txt
  sintactico_error_punto_coma.txt
  sintactico_error_condicion.txt

docs/
  SintaxisBNF.md
```

Archivos principales:

- `TokenType.java`: enum con todos los tipos de token reconocidos.
- `Token.java`: representa un token reconocido, con tipo, lexema, atributo, linea y columna.
- `ErrorLexico.java`: representa un error lexico, con lexema, linea, columna y descripcion.
- `AnalizadorLexico.java`: contiene la logica del lexer.
- `Parser.java`: consume tokens y valida la gramatica del lenguaje.
- `NodoArbol.java` y `NodoAST.java`: nodos para el arbol concreto y el AST.
- `VisualizadorArbol.java`: exporta los arboles a `.dot` y `.png`.
- `docs/SintaxisBNF.md`: gramatica BNF y decisiones de ambiguedad.
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
7. Resumen final del analisis.

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
```

Si hay errores, se muestra la cantidad encontrada:

```txt
Analisis lexico finalizado con errores: 5 errores lexicos encontrados.
Analisis sintactico finalizado con errores: 2 errores sintacticos encontrados.
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
