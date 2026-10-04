package com.manuel.compiler;

public record SemanticError(int line, int column, String message) {
    @Override
    public String toString() {
        return "Línea " + line + ", columna " + column + ": " + message;
    }
}
