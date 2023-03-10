package com.example.camarasrama;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

public class DialogoTutorial {
    private Dialog<ButtonType> dialog;
    private Accordion acordionPane;
    public  DialogoTutorial(){

    }
    public void run(Stage stage){
        dialog = new Dialog<>();
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(stage);
        FXMLLoader fxmlLoader = new FXMLLoader();
        try {
            fxmlLoader.setLocation(new File("C:\\Users\\andre\\OneDrive\\Escritorio\\Java\\CamarasRama\\src\\main\\java\\com\\example\\camarasrama\\tutorialLayout.fxml").toURI().toURL());
        } catch (
                MalformedURLException e) {
            throw new RuntimeException(e);
        }
        try {
            dialog.getDialogPane().setContent(fxmlLoader.load());
        } catch (
                IOException e) {
            // handle exception
        }
        acordionPane = (Accordion)dialog.getDialogPane().lookup("#acordionPane");
        setLista();
        setMapa();
        setFiltros();
        setAgregar();
        setServidores();
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }
    private void setLista(){
        TitledPane listaPane = acordionPane.getPanes().get(0);
        AnchorPane listaAnchor = (AnchorPane) listaPane.getContent();
        GridPane listaGrid = (GridPane) listaAnchor.lookup("#listaGrid");
        Label contextTutorial = (Label)listaGrid.lookup("#contextTutorial");
        contextTutorial.setText("La lista muestra todos los dispositivos agregados y permite modificarlos.\n\n" +
                "Al hacer click derecho sobre un dispositivo se muestran las acciones:\n" +
                "Editar->Permite editar los parametros del dipositivo\n" +
                "Ver comentario->Permite agregar/modificar un comentario en el dispositivo\n" +
                "Ping->Realiza un ping al dispositivo\n" +
                "Borrar->Borra el dispositivo de la lista y del mapa");
        Label resizeTutorial = (Label) listaGrid.lookup("#resizeTutorial");
        resizeTutorial.setText("Hay dos formas de cambiar el tamaño de la lista\n\n" +
                "1-Haciendo click y arrastrando sobre el borde izquierdo,\n" +
                "de esta forma se podra elegir un tamaño personalizado.\n" +
                "2-Haciendo click sobre el boton de ocultar,\n" +
                "de esta forma se oculta completamente la lista");
    }
    private void setMapa(){
        TitledPane mapaPane = acordionPane.getPanes().get(1);
        AnchorPane mapaAnchor = (AnchorPane) mapaPane.getContent();
        GridPane mapaGrid = (GridPane) mapaAnchor.lookup("#mapaGrid");
        Label tutorialIconos = (Label) mapaGrid.lookup("#tutorialIconos");
        tutorialIconos.setText("Cada icono representa un tipo de dispositivo y\n" +
                "su color representa su actividad\n\n" +
                "De izquierda a derecha estos son:\n" +
                "Domo, Fija, Switch\n" +
                "LPR, Radio Enlace, Parada segura");
        Label tutorialIconoClick = (Label) mapaGrid.lookup("#tutorialIconoClick");
        tutorialIconoClick.setText("Hacer click derecho en el mapa permite copiar las coordenadas"+
                        "Hacer click en uno de los iconos lo llevara\n" +
                        "al dispositivo correspondiente en la lista\n"
                );
    }
    private void setFiltros(){
        TitledPane filtrosPane = acordionPane.getPanes().get(2);
        AnchorPane filtrosAnchor = (AnchorPane) filtrosPane.getContent();
        GridPane filtrosGrid = (GridPane) filtrosAnchor.lookup("#filtrosGrid");
        Label tutorialFiltros = (Label) filtrosGrid.lookup("#tutorialFiltro");
        tutorialFiltros.setText("Los filtros se encuentran sobre la barra de acciones\n" +
                "Se pueden elegir 3 tipos de filtros a la vez\n" +
                "Localidad, Actividad, Servidor\n\n" +
                "La opcion TODAS borra los filtros de su propia categoria\n" +
                "En cada categoria se pueden seleccionar varios elementos");
        Label tutorialBuscador = (Label) filtrosGrid.lookup("#tutorialBuscador");
        tutorialBuscador.setText("El buscador se encuentra en la parte inferior\n\n" +
                "El buscador busca dispositivos cuyo parametro ubicacion\n" +
                "coincida total o parcialemente con la busqueda");

    }
    private void setAgregar(){
        TitledPane agregarPane = acordionPane.getPanes().get(3);
        AnchorPane agregarAnchor = (AnchorPane) agregarPane.getContent();
        GridPane agregarGrid = (GridPane) agregarAnchor.lookup("#agregarGrid");
        Label tutorialAgregar = (Label) agregarGrid.lookup("#tutorialAgregar");
        tutorialAgregar.setText("El menu para Agregar una camara se encuentra sobre la barra de acciones, los campos obligatorios son\n" +
                "ip, ubicacion, localidad, latitud, longitud, tipo, servidor\n\n" +
                "La funcion buscar coordenadas busca en el mapa una ubicacion y rellena todos los campos posibles.\n" +
                "La funcion Ajustar mueve el dispositivo de su posicion original segun los valores elegidos,\n" +
                "la distancia desplazada es una distancia fija \"X\" multiplicada por los valores elegidos");
    }
    private void setServidores(){
        TitledPane servidoresPane = acordionPane.getPanes().get(4);
        AnchorPane servidoresAnchor = (AnchorPane) servidoresPane.getContent();
        GridPane servidoresGrid = (GridPane) servidoresAnchor.lookup("#servidoresGrid");
        Label tutorialServidor = (Label) servidoresGrid.lookup("#tutorialServidor");
        tutorialServidor.setText("El menu de servidores se encuentra en el boton +\n" +
                "a la derecha del parametro Servidor en el menu Agregar");
        Label tutorialEditarServidor = (Label) servidoresGrid.lookup("#tutorialEditarServidor");
        tutorialEditarServidor.setText("El menu de servidores permite crear, borrar o migrar.\n" +
                "Para crear un servidor se escribe el nombre en la parte superior\n" +
                "Y se presiona Enter\n\n" +
                "Las opciones Borrar y Migrar aparecen al hacer click derecho\n" +
                "sobre el servidor correspondiente.\n" +
                "Para migrar primero se selecciona el servidor de origen\n" +
                "y luego se selecciona de la misma manera el servidor destino.");

    }

}
