package com.manuel.compiler;

import com.manuel.compiler.Ast.*;
import java.util.ArrayList;
import java.util.List;

public class Parser {
    private final List<Token> tokens;
    private int current = 0;

    public Parser(List<Token> tokens) { this.tokens = tokens; }

    public Program parse() {
        List<Statement> statements = new ArrayList<>();
        while (!isAtEnd()) statements.add(statement());
        return new Program(statements);
    }

    private Statement statement() {
        if (match(TokenType.INT)) return declaration(DataType.INT);
        if (match(TokenType.DOUBLE)) return declaration(DataType.DOUBLE);
        if (match(TokenType.STRING_TYPE)) return declaration(DataType.STRING);
        if (match(TokenType.PRINT)) return printStatement();
        if (check(TokenType.IDENTIFIER) && checkNext(TokenType.ASSIGN)) return assignment();
        throw error(peek(), "Se esperaba una declaración, asignación o print.");
    }

    private Statement declaration(DataType type) {
        Token name = consume(TokenType.IDENTIFIER, "Se esperaba el nombre de la variable.");
        consume(TokenType.ASSIGN, "Se esperaba '=' después del nombre de la variable.");
        Expression initializer = expression();
        consume(TokenType.SEMICOLON, "Se esperaba ';' al final de la declaración.");
        return new VarDeclaration(type, name, initializer);
    }

    private Statement assignment() {
        Token name = consume(TokenType.IDENTIFIER, "Se esperaba identificador.");
        consume(TokenType.ASSIGN, "Se esperaba '=' en la asignación.");
        Expression value = expression();
        consume(TokenType.SEMICOLON, "Se esperaba ';' al final de la asignación.");
        return new Assignment(name, value);
    }

    private Statement printStatement() {
        consume(TokenType.LEFT_PAREN, "Se esperaba '(' después de print.");
        Expression expr = expression();
        consume(TokenType.RIGHT_PAREN, "Se esperaba ')' después de la expresión.");
        consume(TokenType.SEMICOLON, "Se esperaba ';' al final de print.");
        return new PrintStatement(expr);
    }

    private Expression expression() { return term(); }

    private Expression term() {
        Expression expr = factor();
        while (match(TokenType.PLUS, TokenType.MINUS)) {
            Token op = previous();
            expr = new Binary(expr, op, factor());
        }
        return expr;
    }

    private Expression factor() {
        Expression expr = unary();
        while (match(TokenType.STAR, TokenType.SLASH)) {
            Token op = previous();
            expr = new Binary(expr, op, unary());
        }
        return expr;
    }

    private Expression unary() {
        if (match(TokenType.MINUS, TokenType.PLUS)) return new Unary(previous(), unary());
        return primary();
    }

    private Expression primary() {
        if (match(TokenType.NUMBER)) {
            Token t = previous();
            if (t.lexeme().contains(".")) return new Literal(Double.parseDouble(t.lexeme()), DataType.DOUBLE, t);
            return new Literal(Integer.parseInt(t.lexeme()), DataType.INT, t);
        }
        if (match(TokenType.STRING_LITERAL)) {
            Token t = previous();
            return new Literal(t.lexeme().substring(1, t.lexeme().length() - 1), DataType.STRING, t);
        }
        if (match(TokenType.IDENTIFIER)) return new Variable(previous());
        if (match(TokenType.LEFT_PAREN)) {
            Expression expr = expression();
            consume(TokenType.RIGHT_PAREN, "Se esperaba ')' después de la expresión.");
            return new Grouping(expr);
        }
        throw error(peek(), "Expresión no válida.");
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) if (check(type)) { advance(); return true; }
        return false;
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();
        throw error(peek(), message);
    }

    private ParserException error(Token token, String message) {
        return new ParserException("Error sintáctico en línea " + token.line() +
                ", columna " + token.column() + ": " + message);
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return type == TokenType.EOF;
        return peek().type() == type;
    }

    private boolean checkNext(TokenType type) {
        return current + 1 < tokens.size() && tokens.get(current + 1).type() == type;
    }

    private Token advance() { if (!isAtEnd()) current++; return previous(); }
    private boolean isAtEnd() { return peek().type() == TokenType.EOF; }
    private Token peek() { return tokens.get(current); }
    private Token previous() { return tokens.get(current - 1); }
}
