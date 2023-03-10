package com.example.camarasrama;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class ComentarioControlador {
    public ComentarioControlador(){
    }
    public Camara run(Stage stage, Camara camaraComentada){
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(stage);
        dialog.setTitle("Comentarios de "+ camaraComentada.getUbicacion());
        Parent root;
        try {
            root = FXMLLoader.load(new File("src/main/java/com/example/camarasrama/comentarioLayout.fxml").toURI().toURL());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        dialog.getDialogPane().setContent(root);
        TextArea textoComentario = (TextArea) dialog.getDialogPane().lookup("#textoComentario");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        textoComentario.setText(camaraComentada.getZcomentario());

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent()){
            camaraComentada.setZcomentario(textoComentario.getText());
        }
        return camaraComentada;
    }
}
