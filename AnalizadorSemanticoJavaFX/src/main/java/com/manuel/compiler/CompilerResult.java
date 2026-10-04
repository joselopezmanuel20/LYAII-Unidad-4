package com.manuel.compiler;

import com.manuel.compiler.Ast.Program;
import java.util.List;

public record CompilerResult(
        List<Token> tokens,
        Program ast,
        SemanticResult semanticResult,
        String generatedCode
) {}
