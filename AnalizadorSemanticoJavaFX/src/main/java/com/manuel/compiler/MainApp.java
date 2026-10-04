package com.manuel.compiler;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.stream.Collectors;

public class MainApp extends Application {
    private final TextArea codeArea = new TextArea();
    private final TextArea messagesArea = new TextArea();
    private final TextArea generatedArea = new TextArea();
    private final TableView<Token> tokenTable = new TableView<>();
    private final TableView<Symbol> symbolTable = new TableView<>();

    @Override
    public void start(Stage stage) {
        stage.setTitle("Compilador Java - Analizador Semántico");

        Label title = new Label("Proyecto: Analizador Semántico en Java");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
        Label subtitle = new Label(
            "Incluye análisis léxico, sintáctico, semántico y generación de código objeto educativo."
        );

        VBox header = new VBox(4, title, subtitle);
        header.setPadding(new Insets(12));

        codeArea.setPromptText("Escribe aquí el código fuente...");
        codeArea.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 14px;");
        codeArea.setText("""
                int edad = 20;
                double promedio = 9.5;
                string nombre = "Eddi";
                edad = edad + 1;
                print(edad);
                print(nombre);
                """);

        Button compileButton = new Button("Compilar");
        Button clearButton = new Button("Limpiar");
        Button errorExampleButton = new Button("Ejemplo con errores");

        compileButton.setOnAction(e -> compile());
        clearButton.setOnAction(e -> {
            codeArea.clear();
            messagesArea.clear();
            generatedArea.clear();
            tokenTable.getItems().clear();
            symbolTable.getItems().clear();
        });

        errorExampleButton.setOnAction(e -> codeArea.setText("""
                int x = 10;
                int x = 20;
                y = x + 1;
                string nombre = "Eddi";
                x = nombre;
                int z = 10 / 0;
                print(z);
                """));

        HBox buttons = new HBox(10, compileButton, clearButton, errorExampleButton);

        configureTokenTable();
        configureSymbolTable();

        messagesArea.setEditable(false);
        generatedArea.setEditable(false);
        generatedArea.setStyle("-fx-font-family: 'Consolas';");

        TabPane tabs = new TabPane();
        tabs.getTabs().add(new Tab("Tokens", tokenTable));
        tabs.getTabs().add(new Tab("Tabla de símbolos", symbolTable));
        tabs.getTabs().add(new Tab("Mensajes / errores", messagesArea));
        tabs.getTabs().add(new Tab("Código objeto", generatedArea));
        tabs.getTabs().forEach(t -> t.setClosable(false));

        VBox editorPane = new VBox(8, new Label("Código fuente"), codeArea, buttons);
        VBox.setVgrow(codeArea, Priority.ALWAYS);

        SplitPane split = new SplitPane(editorPane, tabs);
        split.setOrientation(Orientation.VERTICAL);
        split.setDividerPositions(0.52);

        BorderPane root = new BorderPane();
        root.setTop(header);
        root.setCenter(split);
        root.setPadding(new Insets(10));

        stage.setScene(new Scene(root, 1050, 720));
        stage.show();
    }

    private void configureTokenTable() {
        TableColumn<Token, String> typeCol = new TableColumn<>("Tipo");
        typeCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().type().name()));

        TableColumn<Token, String> lexemeCol = new TableColumn<>("Lexema");
        lexemeCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().lexeme()));

        TableColumn<Token, String> lineCol = new TableColumn<>("Línea");
        lineCol.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().line())));

        TableColumn<Token, String> colCol = new TableColumn<>("Columna");
        colCol.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().column())));

        tokenTable.getColumns().addAll(typeCol, lexemeCol, lineCol, colCol);
    }

    private void configureSymbolTable() {
        TableColumn<Symbol, String> nameCol = new TableColumn<>("Variable");
        nameCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().name()));

        TableColumn<Symbol, String> typeCol = new TableColumn<>("Tipo");
        typeCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().type().name()));

        TableColumn<Symbol, String> lineCol = new TableColumn<>("Línea declaración");
        lineCol.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().declarationLine())));

        symbolTable.getColumns().addAll(nameCol, typeCol, lineCol);
    }

    private void compile() {
        tokenTable.getItems().clear();
        symbolTable.getItems().clear();
        generatedArea.clear();
        messagesArea.clear();

        try {
            CompilerResult result = new MiniCompiler().compile(codeArea.getText());

            tokenTable.getItems().addAll(
                result.tokens().stream().filter(t -> t.type() != TokenType.EOF).toList()
            );
            symbolTable.getItems().addAll(result.semanticResult().symbolTable().values());

            if (result.semanticResult().isValid()) {
                messagesArea.setText("""
                        COMPILACIÓN EXITOSA

                        No se encontraron errores léxicos, sintácticos ni semánticos.
                        Se generó código objeto educativo.
                        """);
                generatedArea.setText(result.generatedCode());
            } else {
                String errors = result.semanticResult().errors().stream()
                    .map(SemanticError::toString)
                    .collect(Collectors.joining("\n"));

                messagesArea.setText(
                    "COMPILACIÓN DETENIDA POR ERRORES SEMÁNTICOS\n\n" + errors
                );
            }
        } catch (LexicalException | ParserException ex) {
            messagesArea.setText("COMPILACIÓN DETENIDA\n\n" + ex.getMessage());
        } catch (Exception ex) {
            messagesArea.setText("Error inesperado: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
