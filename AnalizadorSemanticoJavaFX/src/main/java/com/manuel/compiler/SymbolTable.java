package com.manuel.compiler;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class SymbolTable {
    private final Map<String, Symbol> symbols = new LinkedHashMap<>();

    public boolean contains(String name) { return symbols.containsKey(name); }
    public void define(Symbol symbol) { symbols.put(symbol.name(), symbol); }
    public Symbol get(String name) { return symbols.get(name); }
    public Collection<Symbol> values() { return symbols.values(); }
}
