package com.manuel.compiler;

import com.manuel.compiler.Ast.*;

public class CodeGenerator {
    private final StringBuilder out = new StringBuilder();

    public String generate(Program program) {
        out.setLength(0);
        out.append("; Código objeto educativo (pseudo-ensamblador)\n");
        for (Statement s : program.statements()) emitStatement(s);
        out.append("HALT\n");
        return out.toString();
    }

    private void emitStatement(Statement s) {
        if (s instanceof VarDeclaration d) {
            out.append("; declarar ").append(d.declaredType()).append(" ").append(d.name().lexeme()).append("\n");
            emitExpression(d.initializer());
            out.append("STORE ").append(d.name().lexeme()).append("\n");
        } else if (s instanceof Assignment a) {
            emitExpression(a.value());
            out.append("STORE ").append(a.name().lexeme()).append("\n");
        } else if (s instanceof PrintStatement p) {
            emitExpression(p.expression());
            out.append("PRINT\n");
        }
    }

    private void emitExpression(Expression e) {
        if (e instanceof Literal l) {
            if (l.type() == DataType.STRING) out.append("PUSH \"").append(l.value()).append("\"\n");
            else out.append("PUSH ").append(l.value()).append("\n");
        } else if (e instanceof Variable v) {
            out.append("LOAD ").append(v.name().lexeme()).append("\n");
        } else if (e instanceof Grouping g) {
            emitExpression(g.expression());
        } else if (e instanceof Unary u) {
            emitExpression(u.right());
            if (u.operator().type() == TokenType.MINUS) out.append("NEG\n");
        } else if (e instanceof Binary b) {
            emitExpression(b.left());
            emitExpression(b.right());
            switch (b.operator().type()) {
                case PLUS -> out.append("ADD\n");
                case MINUS -> out.append("SUB\n");
                case STAR -> out.append("MUL\n");
                case SLASH -> out.append("DIV\n");
                default -> { }
            }
        }
    }
}
