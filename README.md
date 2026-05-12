# Compilador MiniC en Java

Este proyecto implementa un compilador educativo por fases para un lenguaje pequeno de estilo imperativo. Incluye analizador lexico manual, parser descendente recursivo, analizador semantico con tabla de simbolos y tipos fuertes, generacion de codigo de tres direcciones, bloques basicos, CFG, GDA, analisis de variables vivas, optimizacion basica y codigo objeto simbolico.

No ejecuta el programa fuente ni genera codigo maquina real de una arquitectura fisica.

## Estructura del proyecto

```txt
src/
  minic.frontend.lex.AnalizadorLexico.java
  minic.frontend.semantics.AnalizadorSemantico.java
  minic.analysis.AnalisisVariablesVivas.java
  minic.frontend.semantics.AdvertenciaSemantica.java
  minic.frontend.symbols.Ambito.java
  minic.frontend.ast.ArbolAbstracto.java
  minic.frontend.parse.ArbolConcreto.java
  minic.cfg.BloqueBasico.java
  minic.ir.CodigoIntermedio.java
  minic.codegen.CodigoObjeto.java
  minic.cfg.ConstructorBloquesBasicos.java
  minic.cfg.ConstructorCFG.java
  minic.gda.ConstructorGDA.java
  minic.frontend.lex.ErrorLexico.java
  minic.codegen.ErrorGeneracionCodigo.java
  minic.frontend.semantics.ErrorSemantico.java
  minic.frontend.parse.ErrorSintactico.java
  minic.util.ExportadorDot.java
  minic.gda.GDA.java
  minic.ir.GeneradorCodigoIntermedio.java
  minic.codegen.GeneradorCodigoObjeto.java
  minic.ir.GeneradorEtiquetas.java
  minic.ir.GeneradorTemporales.java
  minic.cfg.GrafoFlujoControl.java
  minic.codegen.InstruccionObjeto.java
  minic.ir.InstruccionTAC.java
  minic.app.Main.java
  minic.frontend.ast.NodoAST.java
  minic.frontend.parse.NodoArbol.java
  minic.gda.NodoGDA.java
  minic.ir.OperadorTAC.java
  minic.analysis.OptimizadorCodigoIntermedio.java
  minic.frontend.parse.Parser.java
  minic.pipeline.PipelineCompilacion.java
  minic.frontend.semantics.ReglasTipo.java
  minic.pipeline.ResultadoCompilacion.java
  minic.analysis.ResultadoOptimizacion.java
  minic.frontend.symbols.Simbolo.java
  minic.frontend.symbols.TablaSimbolos.java
  minic.frontend.semantics.TipoDato.java
  minic.frontend.lex.Token.java
  minic.frontend.lex.TokenType.java
  minic.frontend.ast.VisitadorAST.java
  minic.util.VisualizadorArbol.java

ejemplos/
  valido.txt
  errores.txt
  semantico_valido.txt
  semantico_errores.txt
  codigo_asignacion_simple.txt
  codigo_expresion_aritmetica.txt
  codigo_subexpresion_comun.txt
  codigo_plegamiento_constantes.txt
  codigo_propagacion_copias.txt
  codigo_condicional.txt
  codigo_ciclo.txt
  codigo_muerto.txt
  sintactico_valido.txt
  sintactico_error_punto_coma.txt
  sintactico_error_condicion.txt

docs/
  AnalisisSemantico.md
  GeneracionCodigo.md
  SintaxisBNF.md
```

Archivos principales:

- `minic.frontend.lex.TokenType.java`: enum con todos los tipos de token reconocidos.
- `minic.frontend.lex.Token.java`: representa un token reconocido, con tipo, lexema, atributo, linea y columna.
- `minic.frontend.lex.ErrorLexico.java`: representa un error lexico, con lexema, linea, columna y descripcion.
- `minic.frontend.lex.AnalizadorLexico.java`: contiene la logica del lexer.
- `minic.frontend.parse.Parser.java`: consume tokens y valida la gramatica del lenguaje.
- `minic.frontend.semantics.AnalizadorSemantico.java`: recorre el AST, valida tipos, ambitos, usos e inicializacion.
- `minic.frontend.symbols.TablaSimbolos.java`, `minic.frontend.symbols.Ambito.java` y `minic.frontend.symbols.Simbolo.java`: representan los identificadores declarados.
- `minic.frontend.semantics.ReglasTipo.java` y `minic.frontend.semantics.TipoDato.java`: concentran las reglas de compatibilidad fuerte.
- `minic.frontend.parse.NodoArbol.java` y `minic.frontend.ast.NodoAST.java`: nodos para el arbol concreto y el AST.
- `minic.util.VisualizadorArbol.java`: exporta los arboles a `.dot` y `.png`.
- `minic.ir.GeneradorCodigoIntermedio.java`: traduce el AST validado a TAC.
- `minic.cfg.ConstructorBloquesBasicos.java` y `minic.cfg.ConstructorCFG.java`: dividen TAC en bloques y construyen el CFG.
- `minic.gda.ConstructorGDA.java`: construye GDA por bloque basico.
- `minic.analysis.AnalisisVariablesVivas.java`: calcula `use`, `def`, `in` y `out`.
- `minic.analysis.OptimizadorCodigoIntermedio.java`: aplica optimizaciones locales y eliminacion de codigo muerto.
- `minic.codegen.GeneradorCodigoObjeto.java`: genera ensamblador simbolico educativo.
- `minic.pipeline.PipelineCompilacion.java`: coordina generacion, analisis, optimizacion y codigo objeto.
- `docs/SintaxisBNF.md`: gramatica BNF y decisiones de ambiguedad.
- `docs/AnalisisSemantico.md`: reglas semanticas, gramatica de atributos y decisiones.
- `docs/GeneracionCodigo.md`: reglas de TAC, bloques, CFG, GDA, optimizacion y codigo objeto.
- `minic.app.Main.java`: clase principal para probar el analizador desde consola.

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
New-Item -ItemType Directory -Force out | Out-Null
javac -d out (Get-ChildItem -Path src -Recurse -Filter *.java).FullName
```

Esto compila las clases Java dentro del directorio `out`.

Alternativa en `cmd.exe`:

```bat
if not exist out mkdir out
dir /s /b src\*.java > sources.txt
javac -d out @sources.txt
```

## Ejecutar con un archivo de entrada

El programa recibe como primer argumento la ruta de un archivo de texto con codigo fuente del lenguaje.

Ejemplo con una entrada valida:

```powershell
java -cp out minic.app.Main ejemplos\valido.txt
```

Ejemplo con una entrada sintacticamente valida:

```powershell
java -cp out minic.app.Main ejemplos\sintactico_valido.txt
```

Ejemplo con una entrada que contiene errores lexicos:

```powershell
java -cp out minic.app.Main ejemplos\errores.txt
```

Ejemplos con errores sintacticos:

```powershell
java -cp out minic.app.Main ejemplos\sintactico_error_punto_coma.txt
java -cp out minic.app.Main ejemplos\sintactico_error_condicion.txt
```

Ejemplos para la fase semantica:

```powershell
java -cp out minic.app.Main ejemplos\semantico_valido.txt
java -cp out minic.app.Main ejemplos\semantico_errores.txt
```

Ejemplos para generacion y optimizacion de codigo:

```powershell
java -cp out minic.app.Main ejemplos\codigo_asignacion_simple.txt
java -cp out minic.app.Main ejemplos\codigo_expresion_aritmetica.txt
java -cp out minic.app.Main ejemplos\codigo_subexpresion_comun.txt
java -cp out minic.app.Main ejemplos\codigo_plegamiento_constantes.txt
java -cp out minic.app.Main ejemplos\codigo_propagacion_copias.txt
java -cp out minic.app.Main ejemplos\codigo_condicional.txt
java -cp out minic.app.Main ejemplos\codigo_ciclo.txt
java -cp out minic.app.Main ejemplos\codigo_muerto.txt
```

Tambien puedes pasar cualquier archivo `.txt` propio:

```powershell
java -cp out minic.app.Main ruta\al\archivo.txt
```

El archivo se lee usando UTF-8.

## Ejecutar sin archivo de entrada

Si ejecutas el programa sin argumentos:

```powershell
java -cp out minic.app.Main
```

no se utiliza ningun archivo por defecto. En ese caso, `minic.app.Main` analiza una cadena interna definida en el metodo `ejemploDeEntrada()` de `src/minic/app/Main.java`.

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

Tambien puedes usar el lexer directamente desde otra clase Java, sin pasar por `minic.app.Main`:

```java
import minic.frontend.lex.AnalizadorLexico;
import minic.frontend.lex.ErrorLexico;
import minic.frontend.lex.Token;

String codigo = "num edad = 18;";

AnalizadorLexico lexer = new AnalizadorLexico(codigo);
List<Token> tokens = lexer.analizar();
List<ErrorLexico> errores = lexer.getErrores();
```

`analizar()` devuelve la lista de tokens reconocidos. Los errores lexicos quedan disponibles con `getErrores()`.

## Salida del programa

Al ejecutar `minic.app.Main`, la salida se divide en estas partes:

1. Lista de tokens reconocidos.
2. Lista de errores lexicos encontrados.
3. Resultado del analisis sintactico.
4. Arbol concreto en texto.
5. AST en texto.
6. Archivos graficos generados.
7. Resultado del analisis semantico.
8. Tabla de simbolos final.
9. Errores y advertencias semanticas.
10. Codigo de tres direcciones inicial.
11. Bloques basicos y CFG.
12. GDA por bloque.
13. Analisis de variables vivas.
14. Optimizaciones aplicadas.
15. Codigo de tres direcciones optimizado.
16. Codigo objeto final.
17. Resumen final del analisis.

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
Generacion y optimizacion de codigo finalizada correctamente.
```

Si hay errores, se muestra la cantidad encontrada:

```txt
Analisis lexico finalizado con errores: 5 errores lexicos encontrados.
Analisis sintactico finalizado con errores: 2 errores sintacticos encontrados.
Analisis semantico finalizado con errores: 3 errores semanticos encontrados.
Generacion de codigo no ejecutada por errores previos.
```

El parser genera estos archivos en el directorio desde donde se ejecuta el programa:

```txt
arbol_concreto.dot
arbol_concreto.png
ast.dot
ast.png
cfg.dot
gda_B1.dot
gda_B2.dot
```

Si Graphviz esta disponible en `PATH`, tambien se generan `cfg.png` y los
`gda_B*.png`.

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

## Generacion y optimizacion de codigo

La fase de codigo solo se ejecuta si las fases lexico, sintactica y semantica no
reportan errores. Sus pasos son:

- Generar TAC desde el AST.
- Dividir TAC en bloques basicos.
- Construir CFG.
- Construir GDA por bloque.
- Calcular variables vivas.
- Aplicar propagacion de copias, propagacion de constantes, plegamiento de
  constantes, subexpresiones comunes locales, simplificacion algebraica,
  eliminacion de codigo muerto y simplificacion de saltos.
- Generar codigo objeto simbolico con instrucciones como `LOAD`, `STORE`,
  `ADD`, `CMP_GE`, `JZ`, `JMP`, `PRINT` y `READ`.

Consulta `docs/GeneracionCodigo.md` para las reglas completas y ejemplos.
