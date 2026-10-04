package com.manuel.compiler;

import java.util.List;

public final class Ast {
    private Ast() {}

    public enum DataType { INT, DOUBLE, STRING, UNKNOWN }

    public sealed interface Node permits Program, Statement, Expression {}
    public record Program(List<Statement> statements) implements Node {}

    public sealed interface Statement extends Node permits VarDeclaration, Assignment, PrintStatement {}
    public record VarDeclaration(DataType declaredType, Token name, Expression initializer) implements Statement {}
    public record Assignment(Token name, Expression value) implements Statement {}
    public record PrintStatement(Expression expression) implements Statement {}

    public sealed interface Expression extends Node permits Binary, Literal, Variable, Grouping, Unary {}
    public record Binary(Expression left, Token operator, Expression right) implements Expression {}
    public record Literal(Object value, DataType type, Token token) implements Expression {}
    public record Variable(Token name) implements Expression {}
    public record Grouping(Expression expression) implements Expression {}
    public record Unary(Token operator, Expression right) implements Expression {}
}
