package com.manuel.compiler;

import java.util.ArrayList;
import java.util.List;

public class Lexer {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0, current = 0, line = 1, column = 1, tokenColumn = 1;

    public Lexer(String source) {
        this.source = source == null ? "" : source;
    }

    public List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            tokenColumn = column;
            scanToken();
        }
        tokens.add(new Token(TokenType.EOF, "", line, column));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '+' -> add(TokenType.PLUS);
            case '-' -> add(TokenType.MINUS);
            case '*' -> add(TokenType.STAR);
            case '/' -> {
                if (match('/')) {
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else add(TokenType.SLASH);
            }
            case '=' -> add(TokenType.ASSIGN);
            case '(' -> add(TokenType.LEFT_PAREN);
            case ')' -> add(TokenType.RIGHT_PAREN);
            case ';' -> add(TokenType.SEMICOLON);
            case ' ', '\r', '\t' -> { }
            case '\n' -> { line++; column = 1; }
            case '"' -> string();
            default -> {
                if (isDigit(c)) number();
                else if (isAlpha(c)) identifier();
                else throw new LexicalException(
                    "Error léxico en línea " + line + ", columna " + tokenColumn +
                    ": símbolo no reconocido '" + c + "'.");
            }
        }
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) advance();
        String text = source.substring(start, current);
        TokenType type = switch (text) {
            case "int" -> TokenType.INT;
            case "double" -> TokenType.DOUBLE;
            case "string" -> TokenType.STRING_TYPE;
            case "print" -> TokenType.PRINT;
            default -> TokenType.IDENTIFIER;
        };
        add(type);
    }

    private void number() {
        while (isDigit(peek())) advance();
        if (peek() == '.' && isDigit(peekNext())) {
            advance();
            while (isDigit(peek())) advance();
        }
        add(TokenType.NUMBER);
    }

    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') { line++; column = 1; }
            advance();
        }
        if (isAtEnd()) throw new LexicalException("Cadena sin cerrar en línea " + line + ".");
        advance();
        add(TokenType.STRING_LITERAL);
    }

    private char advance() {
        char c = source.charAt(current++);
        column++;
        return c;
    }

    private boolean match(char expected) {
        if (isAtEnd() || source.charAt(current) != expected) return false;
        current++; column++;
        return true;
    }

    private char peek() { return isAtEnd() ? '\0' : source.charAt(current); }
    private char peekNext() { return current + 1 >= source.length() ? '\0' : source.charAt(current + 1); }
    private boolean isAtEnd() { return current >= source.length(); }

    private void add(TokenType type) {
        tokens.add(new Token(type, source.substring(start, current), line, tokenColumn));
    }

    private boolean isDigit(char c) { return c >= '0' && c <= '9'; }
    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }
    private boolean isAlphaNumeric(char c) { return isAlpha(c) || isDigit(c); }
}
