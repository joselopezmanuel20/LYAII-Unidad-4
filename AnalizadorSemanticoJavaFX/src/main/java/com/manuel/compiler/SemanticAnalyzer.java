package com.manuel.compiler;

import com.manuel.compiler.Ast.*;
import java.util.ArrayList;
import java.util.List;

public class SemanticAnalyzer {
    private final SymbolTable table = new SymbolTable();
    private final List<SemanticError> errors = new ArrayList<>();

    public SemanticResult analyze(Program program) {
        for (Statement s : program.statements()) analyzeStatement(s);
        return new SemanticResult(table, List.copyOf(errors));
    }

    private void analyzeStatement(Statement s) {
        if (s instanceof VarDeclaration d) analyzeDeclaration(d);
        else if (s instanceof Assignment a) analyzeAssignment(a);
        else if (s instanceof PrintStatement p) typeOf(p.expression());
    }

    private void analyzeDeclaration(VarDeclaration d) {
        String name = d.name().lexeme();
        if (table.contains(name)) {
            error(d.name(), "La variable '" + name + "' ya fue declarada.");
            return;
        }
        DataType initType = typeOf(d.initializer());
        if (!isAssignable(d.declaredType(), initType)) {
            error(d.name(), "No se puede asignar un valor de tipo " + initType +
                    " a una variable de tipo " + d.declaredType() + ".");
        }
        table.define(new Symbol(name, d.declaredType(), d.name().line()));
    }

    private void analyzeAssignment(Assignment a) {
        Symbol symbol = table.get(a.name().lexeme());
        if (symbol == null) {
            error(a.name(), "La variable '" + a.name().lexeme() + "' no ha sido declarada.");
            typeOf(a.value());
            return;
        }
        DataType valueType = typeOf(a.value());
        if (!isAssignable(symbol.type(), valueType)) {
            error(a.name(), "Asignación incompatible: '" + a.name().lexeme() +
                    "' es de tipo " + symbol.type() + " y recibe " + valueType + ".");
        }
    }

    private DataType typeOf(Expression e) {
        if (e instanceof Literal l) return l.type();

        if (e instanceof Variable v) {
            Symbol s = table.get(v.name().lexeme());
            if (s == null) {
                error(v.name(), "La variable '" + v.name().lexeme() + "' no ha sido declarada.");
                return DataType.UNKNOWN;
            }
            return s.type();
        }

        if (e instanceof Grouping g) return typeOf(g.expression());

        if (e instanceof Unary u) {
            DataType r = typeOf(u.right());
            if (!isNumeric(r) && r != DataType.UNKNOWN) {
                error(u.operator(), "El operador unario '" + u.operator().lexeme() + "' requiere un valor numérico.");
                return DataType.UNKNOWN;
            }
            return r;
        }

        if (e instanceof Binary b) {
            DataType left = typeOf(b.left());
            DataType right = typeOf(b.right());

            if (left == DataType.UNKNOWN || right == DataType.UNKNOWN) return DataType.UNKNOWN;

            if (b.operator().type() == TokenType.PLUS &&
                    left == DataType.STRING && right == DataType.STRING) return DataType.STRING;

            if (!isNumeric(left) || !isNumeric(right)) {
                error(b.operator(), "El operador '" + b.operator().lexeme() +
                        "' requiere operandos numéricos, excepto string + string.");
                return DataType.UNKNOWN;
            }

            if (b.operator().type() == TokenType.SLASH && isLiteralZero(b.right())) {
                error(b.operator(), "División entre cero detectada.");
            }

            return (left == DataType.DOUBLE || right == DataType.DOUBLE)
                    ? DataType.DOUBLE : DataType.INT;
        }

        return DataType.UNKNOWN;
    }

    private boolean isLiteralZero(Expression e) {
        if (e instanceof Literal l) {
            if (l.value() instanceof Integer i) return i == 0;
            if (l.value() instanceof Double d) return d == 0.0;
        }
        return false;
    }

    private boolean isNumeric(DataType t) { return t == DataType.INT || t == DataType.DOUBLE; }

    private boolean isAssignable(DataType target, DataType value) {
        if (value == DataType.UNKNOWN) return true;
        if (target == value) return true;
        return target == DataType.DOUBLE && value == DataType.INT;
    }

    private void error(Token t, String message) {
        errors.add(new SemanticError(t.line(), t.column(), message));
    }
}
