# Tabla de tokens

Esta tabla resume los tokens reconocidos por el analizador lexico del proyecto.
Cada token conserva como atributos comunes la `linea` y la `columna` donde inicia
el lexema. La columna `Atributo` muestra el valor semantico que guarda el objeto
`bipre.frontend.lex.Token`.

> Nota: la tabla sigue la implementacion actual de `bipre.frontend.lex.AnalizadorLexico.java`. En
> esa implementacion, el lexema `flotar` se clasifica como `NUMERO` con atributo
> `float`.

| Lexema | Tipo | Atributo |
| --- | --- | --- |
| `num` | `NUMERO` | `int` |
| `letra` | `CARACTER` | `char` |
| `flotar` | `NUMERO` | `float` |
| `valdt` | `VALIDAR` | `if` |
| `ciclar` | `CICLO` | `for` |
| `estamp` | `ESTAMPAR` | `printf` |
| `recolt` | `RECOLECTAR` | `scanf` |
| `[a-zA-Z_][a-zA-Z0-9_]*` | `ID` | `identificador` |
| `+` | `OP_ARIT` | `aritmetico` |
| `-` | `OP_ARIT` | `aritmetico` |
| `*` | `OP_ARIT` | `aritmetico` |
| `/` | `OP_ARIT` | `aritmetico` |
| `%` | `OP_ARIT` | `aritmetico` |
| `=` | `OP_ASIG` | `asignacion` |
| `==` | `OP_COMP` | `comparativo` |
| `<>` | `OP_COMP` | `comparativo` |
| `>` | `OP_COMP` | `comparativo` |
| `<` | `OP_COMP` | `comparativo` |
| `>=` | `OP_COMP` | `comparativo` |
| `<=` | `OP_COMP` | `comparativo` |
| `!=` | `OP_COMP` | `comparativo` |
| `&&` | `OP_Y` | `AND logico` |
| `\|\|` | `OP_O` | `OR logico` |
| `++` | `OP_INC` | `incremento` |
| `--` | `OP_DEC` | `decremento` |
| `[0-9]+` | `ENTERO_LITERAL` | Valor numerico `BigInteger` del lexema |
| `[0-9]+\.[0-9]+` | `FLOTANTE_LITERAL` | Valor numerico `BigDecimal` del lexema |
| `'a'`, `'\n'`, `'\t'`, `'\r'`, `'\''`, `'\"'`, `'\\'` | `CARACTER_LITERAL` | Caracter decodificado |
| `"texto"` | `CADENA_LITERAL` | Cadena decodificada sin comillas |
| `(` | `PAREN_IZQ` | `parentesis izquierdo` |
| `)` | `PAREN_DER` | `parentesis derecho` |
| `{` | `LLAVE_IZQ` | `llave izquierda` |
| `}` | `LLAVE_DER` | `llave derecha` |
| `;` | `PUNTO_COMA` | `punto y coma` |
| `,` | `COMA` | `coma` |
| Fin de entrada | `EOF` | `null` |

## Atributos del objeto bipre.frontend.lex.Token

Cada token se representa con los siguientes campos:

| Campo | Descripcion |
| --- | --- |
| `tipo` | Tipo definido en `bipre.frontend.lex.TokenType`. |
| `lexema` | Texto exacto reconocido en la fuente. |
| `atributo` | Valor asociado al token, segun la tabla anterior. |
| `linea` | Linea donde inicia el token. |
| `columna` | Columna donde inicia el token. |
