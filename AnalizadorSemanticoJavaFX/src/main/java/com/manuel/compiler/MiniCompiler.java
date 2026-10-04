package com.manuel.compiler;

import com.manuel.compiler.Ast.Program;
import java.util.List;

public class MiniCompiler {
    public CompilerResult compile(String source) {
        List<Token> tokens = new Lexer(source).scanTokens();
        Program program = new Parser(tokens).parse();
        SemanticResult semanticResult = new SemanticAnalyzer().analyze(program);

        String generatedCode = "";
        if (semanticResult.isValid()) {
            generatedCode = new CodeGenerator().generate(program);
        }
        return new CompilerResult(tokens, program, semanticResult, generatedCode);
    }
}
