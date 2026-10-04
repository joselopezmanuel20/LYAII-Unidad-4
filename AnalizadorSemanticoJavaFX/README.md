# Analizador Semántico en JavaFX

Proyecto de **mini compilador educativo** para IntelliJ IDEA usando Java, JavaFX y Maven.

## Qué hace

1. Análisis léxico: genera tokens.
2. Análisis sintáctico: verifica la gramática y construye un AST.
3. Análisis semántico:
   - Variables duplicadas.
   - Variables no declaradas.
   - Compatibilidad de tipos.
   - Operaciones inválidas.
   - División literal entre cero.
   - Tabla de símbolos.
4. Generación de código objeto educativo en pseudo-ensamblador.

## Código de ejemplo válido

    int edad = 20;
    double promedio = 9.5;
    string nombre = "Eddi";
    edad = edad + 1;
    print(edad);
    print(nombre);

## Cómo abrir en IntelliJ

1. Abre IntelliJ IDEA.
2. Selecciona **Open**.
3. Abre la carpeta `AnalizadorSemanticoJavaFX`.
4. Espera a que Maven cargue las dependencias.
5. Usa JDK 21.
6. Ejecuta `MainApp.java` o desde Terminal:

    mvn javafx:run

## Gramática simplificada

    programa    -> sentencia* EOF
    sentencia   -> declaracion | asignacion | imprimir
    declaracion -> ("int" | "double" | "string") IDENTIFIER "=" expresion ";"
    asignacion  -> IDENTIFIER "=" expresion ";"
    imprimir    -> "print" "(" expresion ")" ";"
    expresion   -> termino (("+" | "-") termino)*
    termino     -> unario (("*" | "/") unario)*
    unario      -> ("+" | "-") unario | primario
    primario    -> NUMBER | STRING_LITERAL | IDENTIFIER | "(" expresion ")"

## Diagrama

Se incluye `diagrama-clases.puml` para abrirlo con PlantUML.
