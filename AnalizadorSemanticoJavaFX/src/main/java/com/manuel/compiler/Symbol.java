package com.manuel.compiler;

import com.manuel.compiler.Ast.DataType;

public record Symbol(String name, DataType type, int declarationLine) {}
