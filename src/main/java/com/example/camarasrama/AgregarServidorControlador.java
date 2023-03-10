package com.example.camarasrama;

import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class AgregarServidorControlador {
    @FXML
    private ListView<String> visorServidores;
    private String migrado;

    public AgregarServidorControlador(){

    }
    public void run(Stage stage, ListView<Camara> listaCamaras){
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(stage);
        dialog.setTitle("Servidores");
        Parent root;
        try {
            root = FXMLLoader.load(new File("src/main/java/com/example/camarasrama/agregarServidorLayout.fxml").toURI().toURL());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        dialog.getDialogPane().setContent(root);
        visorServidores = (ListView<String>) dialog.getDialogPane().lookup("#visorServidores");
        visorServidores.setItems(Main.SERVIDORES);
        ContextMenu contextMenu = new ContextMenu();
        contextMenu.setAutoHide(true);
        MenuItem deleteItem = new MenuItem("Borrar servidor\t");
        MenuItem migrarItem = new MenuItem("Migrar servidor\t");
        contextMenu.getItems().addAll(deleteItem, migrarItem);
        migrado = "";
        visorServidores.setOnMouseClicked(click -> {
            ListView<String> list = (ListView<String>) click.getSource();
            String servidor = list.getSelectionModel().getSelectedItem();
            if (click.getButton() == MouseButton.SECONDARY) {
                if (servidor == null){
                    return;
                }
                contextMenu.show(visorServidores, click.getScreenX(), click.getScreenY());
                deleteItem.setOnAction(e -> {
                    Main.SERVIDORES.remove(servidor);
                    guardarServidores();
                });
                if (migrado.equals("")){
                    migrarItem.setOnAction(e -> migrado = servidor);
                    migrarItem.setText("Migrar servidor\t");
                }else {
                    migrarItem.setOnAction(e -> {
                        FilteredList<Camara> items = (FilteredList<Camara>) listaCamaras.getItems();
                        for (Camara camara : items.getSource()) {
                            if (camara.getServidor().equals(migrado)) {
                                camara.setServidor(servidor);
                            }
                        }
                        migrado = "";
                        listaCamaras.refresh();
                        CSVManager csvManager = new CSVManager();
                        csvManager.guardarCSV(new File("src/main/java/com/example/camarasrama/Camaras.csv"), (ObservableList<Camara>) items.getSource());
                    });
                    migrarItem.setText("Migrar Aqui\t");
                }
            }else{
                contextMenu.hide();
            }
        });
        TextField textoNuevoServidor = (TextField) dialog.getDialogPane().lookup("#textoNuevoServidor");
        textoNuevoServidor.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.ENTER){
                if (!textoNuevoServidor.getText().equals("")){
                    Main.SERVIDORES.add(textoNuevoServidor.getText());
                    guardarServidores();
                    textoNuevoServidor.setText("");
                }
            }
        });
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
        dialog.getDialogPane().requestFocus();

    }
    private void guardarServidores(){
        ordenarServidores();
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("src/main/java/com/example/camarasrama/servidores.txt"))) {
            for (String servidor : Main.SERVIDORES) {
                bw.write(servidor);
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void ordenarServidores(){
        Main.SERVIDORES.sort(String::compareTo);
    }
}
