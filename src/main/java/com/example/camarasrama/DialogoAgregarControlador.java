package com.example.camarasrama;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.web.WebEngine;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DialogoAgregarControlador {
    @FXML
    private Button botonAjustarUp,botonAjustarDown,botonAjustarLeft,botonAjustarRight;
    private ButtonType botonConfirmar;
    @FXML
    private TextField textoIp,textoUbicacion,textoLatitud,textoLongitud,ajustarUp,ajustarSide,textoBusquedaCoordenadas;
    @FXML
    private ChoiceBox<String> selectorServidor;
    @FXML
            private ChoiceBox<String> selectorTipo;
    @FXML
            private ChoiceBox<String> selectorLocalidad;
    private Dialog<ButtonType> dialog;
    private Stage stage;
    private Camara camaraEditable;
    private WebEngine engine;
    private String latDrop, longDrop;
    private RadioButton botonRecordar;
    final double latitudAjuste = 0.0000900;
    final double longitudAjuste = 0.0001200;
    private boolean delete;
    public DialogoAgregarControlador(){
    }

    public Camara run(Stage stage, int id, Camara camaraEditable,
                      WebEngine engineParam, ListView<Camara> listaCamaras,String coords, String tipo){
        if (coords == null){
            latDrop = "";
            longDrop = "";
        }else{
            String[] coordenadasPre = coords.split(",");
            List<String> lista = FXCollections.observableArrayList(coordenadasPre);
            latDrop = lista.get(1).substring(4);
            longDrop = lista.get(0).substring(4);
        }
        engine = engineParam;
        dialog = new Dialog<>();
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(stage);
        if (camaraEditable == null){
            dialog.setTitle("Agregar Dispositivo");
        }else{
            dialog.setTitle("Editar Dispositivo");
        }
        this.stage = stage;
        this.camaraEditable = camaraEditable;
        if (camaraEditable == null){
            this.camaraEditable = Main.recordada;
        }

        Parent root;
        try {
            root = FXMLLoader.load(new File("src/main/java/com/example/camarasrama/dialogoAgregar.fxml").toURI().toURL());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        dialog.getDialogPane().setOnMouseClicked(event -> dialog.getDialogPane().requestFocus());
        dialog.getDialogPane().setContent(root);
        // Agregar botones al diálogo
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        botonConfirmar = new ButtonType("Confirmar", ButtonType.APPLY.getButtonData());
        dialog.getDialogPane().getButtonTypes().add(botonConfirmar);
        selectorLocalidad = (ChoiceBox<String>) dialog.getDialogPane().lookup("#selectorLocalidad");
        selectorTipo = (ChoiceBox<String>) dialog.getDialogPane().lookup("#selectorTipo");
        selectorServidor = (ChoiceBox<String>) dialog.getDialogPane().lookup("#selectorServidor");
        textoIp = (TextField) dialog.getDialogPane().lookup("#textoIp");
        textoUbicacion = (TextField) dialog.getDialogPane().lookup("#textoUbicacion");
        textoLatitud = (TextField) dialog.getDialogPane().lookup("#textoLatitud");
        textoLongitud = (TextField) dialog.getDialogPane().lookup("#textoLongitud");
        Button botonIr = (Button) dialog.getDialogPane().lookup("#botonIr");
        Button botonAgregarServidor = (Button) dialog.getDialogPane().lookup("#botonAgregarServidor");
        botonAjustarUp = (Button)dialog.getDialogPane().lookup("#botonAjustarUp");
        botonAjustarDown = (Button)dialog.getDialogPane().lookup("#botonAjustarDown");
        botonAjustarLeft = (Button)dialog.getDialogPane().lookup("#botonAjustarLeft");
        botonAjustarRight = (Button)dialog.getDialogPane().lookup("#botonAjustarRight");
        ajustarUp = (TextField)dialog.getDialogPane().lookup("#ajustarUp");
        ajustarSide = (TextField)dialog.getDialogPane().lookup("#ajustarSide");
        botonRecordar = (RadioButton) dialog.getDialogPane().lookup("#botonRecordar");
        textoBusquedaCoordenadas = (TextField) dialog.getDialogPane().lookup("#textoBusquedaCoordenadas");
        textoBusquedaCoordenadas.setOnKeyReleased(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER){
                List<String> coordenadas =  buscarCoordenadas(textoBusquedaCoordenadas.getText(), true);
                if (coordenadas != null){
                    textoLatitud.setText(coordenadas.get(0));
                    textoLongitud.setText(coordenadas.get(1));
                    if (selectorLocalidad.getValue().equals("CASEROS SUR") && Double.parseDouble(textoLatitud.getText()) >-34.60468833448273){
                        selectorLocalidad.setValue("CASEROS NORTE");
                    }
                }
                botonIr.fire();
            }
        });
        if(!latDrop.equals("")){
            textoLatitud.setText(latDrop);
        }
        textoLatitud.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (newValue.contains("lon=") && newValue.contains("lat=")) {
                    String[] coordenadas = newValue.split(",");
                    List<String> lista = FXCollections.observableArrayList(coordenadas);
                    String latitud = lista.get(1).substring(4);
                    String longitud = lista.get(0).substring(4);
                    textoLatitud.setText(latitud);
                    textoLongitud.setText(longitud);
                    botonIr.fire();
                }
            }
        });
        if(!longDrop.equals("")){
            textoLongitud.setText(longDrop);
        }
        textoUbicacion.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if(!botonRecordar.isSelected() && newValue.toLowerCase().contains("3f") && newValue.toLowerCase().contains("com")){
                    botonRecordar.setSelected(true);
                }
            }
        });
        textoLongitud.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (newValue.contains("lon=") && newValue.contains("lat=")) {
                    String[] coordenadas = newValue.split(",");
                    List<String> lista = FXCollections.observableArrayList(coordenadas);
                    textoLatitud.setText(lista.get(1).substring(4));
                    textoLongitud.setText(lista.get(0).substring(4));
                    botonIr.fire();
                }
            }
        });
        botonIr.setOnAction(event -> {
            if(!textoLatitud.getText().equals("") && !textoLongitud.getText().equals("")){
                try {
                    Double.parseDouble(textoLongitud.getText());
                } catch (NumberFormatException e) {
                    return;
                }
                try {
                    Double.parseDouble(textoLatitud.getText());
                } catch (NumberFormatException e) {
                    return;
                }
                engine.executeScript("var newLonLat = new OpenLayers.LonLat( " + textoLongitud.getText() + " ,"
                        + textoLatitud.getText() + " ).transform(new OpenLayers.Projection(\"EPSG:4326\"), map.getProjectionObject());"
                        + "map.setCenter (newLonLat, 18);");
                double latitud;
                try{
                    latitud =Double.parseDouble(textoLatitud.getText()) + (Double.parseDouble(ajustarUp.getText())*latitudAjuste);
                }catch (NumberFormatException e){
                    latitud = Double.parseDouble(textoLatitud.getText());
                    if (ajustarUp.getText().equals("")){
                        ajustarUp.setText("0");
                    }else{
                        ajustarUp.setText("E!");
                    }
                }
                double longitud;
                try{
                    longitud =Double.parseDouble(textoLongitud.getText()) + (Double.parseDouble(ajustarSide.getText())*longitudAjuste);
                }catch(NumberFormatException e){
                    longitud = Double.parseDouble(textoLongitud.getText());
                    if (ajustarSide.getText().equals("")){
                        ajustarSide.setText("0");
                    }else{
                        ajustarSide.setText("E!");
                    }
                }
                if (delete){
                    engine.executeScript(removeMarker());
                }
                engine.executeScript(addMarker(String.valueOf(latitud), String.valueOf(longitud)));
                delete = true;
            }
        });
        botonAgregarServidor.setOnAction(event ->{
            AgregarServidorControlador agregarServidorControlador = new AgregarServidorControlador();
            agregarServidorControlador.run(stage, listaCamaras);
        });
        botonAjustarUp.setOnAction(event -> {
            try{
                double actual = Double.parseDouble(ajustarUp.getText());
                ajustarUp.setText(String.valueOf(actual+1));
                botonIr.fire();
            }catch (NumberFormatException e){
                if (ajustarUp.getText().equals("")){
                    ajustarUp.setText("0");
                }else{
                    ajustarUp.setText("E!");
                }
            }

        });
        botonAjustarDown.setOnAction(event -> {
            try{
                double actual = Double.parseDouble(ajustarUp.getText());
                ajustarUp.setText(String.valueOf(actual-1));
                botonIr.fire();
            }catch (NumberFormatException e){
                if (ajustarUp.getText().equals("")){
                    ajustarUp.setText("0");
                }else{
                    ajustarUp.setText("E!");
                }
            }

        });
        botonAjustarLeft.setOnAction(event -> {
            try{
                double actual = Double.parseDouble(ajustarSide.getText());
                ajustarSide.setText(String.valueOf(actual-1));
                botonIr.fire();
            }catch (NumberFormatException e){
                if (ajustarSide.getText().equals("")){
                    ajustarSide.setText("0");
                }else{
                    ajustarSide.setText("E!");
                }
            }

        });
        botonAjustarRight.setOnAction(event -> {
            try{
                double actual = Double.parseDouble(ajustarSide.getText());
                ajustarSide.setText(String.valueOf(actual+1));
                botonIr.fire();
            }catch (NumberFormatException e){
                if (ajustarSide.getText().equals("")){
                    ajustarSide.setText("0");
                }else{
                    ajustarSide.setText("E!");
                }
            }
        });
        ajustarSide.setOnKeyReleased(event -> {
            if(event.getCode() == KeyCode.ENTER){
                try{
                    Double.parseDouble(ajustarSide.getText());
                    botonIr.fire();
                }catch (NumberFormatException e){
                    if (ajustarSide.getText().equals("")){
                        ajustarSide.setText("0");
                    }else{
                        ajustarSide.setText("E!");
                    }
                }
            }
        });
        ajustarUp.setOnKeyReleased(event -> {
            if(event.getCode() == KeyCode.ENTER){
                try{
                    Double.parseDouble(ajustarUp.getText());
                    botonIr.fire();
                }catch (NumberFormatException e){
                    if (ajustarUp.getText().equals("")){
                        ajustarUp.setText("0");
                    }else{
                        ajustarUp.setText("E!");
                    }
                }
            }
        });
        selectorLocalidad.setItems(Main.LOCALIDADES);
        selectorTipo.setItems(Main.TIPOS);
        selectorServidor.setItems(Main.SERVIDORES);
        selectorTipo.setValue("Seleccionar Tipo");
        selectorLocalidad.setValue("Seleccionar Localidad");
        if(tipo != null){
            selectorTipo.setValue(tipo);
            engine.executeScript("var newLonLat = new OpenLayers.LonLat( " + textoLongitud.getText() + " ,"
                    + textoLatitud.getText() + " ).transform(new OpenLayers.Projection(\"EPSG:4326\"), map.getProjectionObject());"
                    + "map.setCenter (newLonLat, 18);");
            engine.executeScript(addMarker(textoLatitud.getText(), textoLongitud.getText()));
            delete = true;
            buscarCoordenadas(textoLatitud.getText() + ","+textoLongitud.getText(), false);
            if (selectorLocalidad.getValue().equals("CASEROS SUR") && Double.parseDouble(textoLatitud.getText()) >-34.60468833448273){
                selectorLocalidad.setValue("CASEROS NORTE");
            }
            botonIr.fire();
        }
        selectorServidor.setValue("Seleccionar Servidor");
        if (camaraEditable != null){
            textoIp.setText(camaraEditable.getIp());
            textoUbicacion.setText(camaraEditable.getUbicacion());
            selectorLocalidad.setValue(camaraEditable.getLocalidad());
            textoLatitud.setText(camaraEditable.getLatitud());
            textoLongitud.setText(camaraEditable.getLongitud());
            selectorTipo.setValue(camaraEditable.getTipo());
            selectorServidor.setValue(camaraEditable.getServidor());
        }else if(this.camaraEditable != null){
            textoIp.setText(this.camaraEditable.getIp());
            textoUbicacion.setText(this.camaraEditable.getUbicacion());
            selectorLocalidad.setValue(this.camaraEditable.getLocalidad());
            selectorServidor.setValue(this.camaraEditable.getServidor());
        }
        selectorTipo.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (delete){
                    engine.executeScript(removeMarker());
                }
                String longFinal;
                String latFinal;
                double latitud;
                try{
                    latitud =Double.parseDouble(textoLatitud.getText()) + (Double.parseDouble(ajustarUp.getText())*latitudAjuste);
                    latFinal = String.valueOf(latitud);
                }catch (NumberFormatException e){
                    try{
                        Double.parseDouble(textoLatitud.getText());
                        latFinal = textoLatitud.getText();

                    }catch (NumberFormatException e2){
                        return;
                    }
                }
                double longitud;
                try{
                    longitud =Double.parseDouble(textoLongitud.getText()) + (Double.parseDouble(ajustarSide.getText())*longitudAjuste);
                    longFinal = String.valueOf(longitud);
                }catch (NumberFormatException e){
                    try{
                        Double.parseDouble(textoLongitud.getText());
                        longFinal =textoLongitud.getText();
                    }catch (NumberFormatException e2){
                        return;
                    }
                }
                engine.executeScript(addMarker(latFinal, longFinal));
                delete = true;
            }
        });
        return procesOutput(id);
    }
    public Camara procesOutput(int id){
        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == botonConfirmar) {
            double latitudAjuste = 0.0000900;
            double longitudAjuste = 0.0001200;
            String latFinal;
            String longFinal;
            double latitud;
            try{
                latitud =Double.parseDouble(textoLatitud.getText()) + (Double.parseDouble(ajustarUp.getText())*latitudAjuste);
                latFinal = String.valueOf(latitud);
            }catch (NumberFormatException e){
                latFinal = textoLatitud.getText();
            }
            double longitud;
            try{
                longitud =Double.parseDouble(textoLongitud.getText()) + (Double.parseDouble(ajustarSide.getText())*longitudAjuste);
                longFinal = String.valueOf(longitud);
            }catch (NumberFormatException e){
                longFinal =textoLongitud.getText();
            }
            Camara nuevaCamara = new Camara(textoIp.getText(),textoUbicacion.getText(),selectorLocalidad.getValue(),
                    latFinal,longFinal,selectorTipo.getValue(),selectorServidor.getValue(),id);
            String errores = nuevaCamara.errores();
            if (errores == null){
                if (camaraEditable != null){
                    nuevaCamara.setZcomentario(camaraEditable.getZcomentario());
                }
                if (delete){
                    engine.executeScript(removeMarker());
                }
                if(botonRecordar.isSelected()){
                    Main.recordada = nuevaCamara;
                }else{
                    Main.recordada = null;
                }
                return nuevaCamara;
            }else{
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.initModality(Modality.WINDOW_MODAL);
                alert.initOwner(stage);
                alert.setTitle("Error de parametros");
                alert.setHeaderText("Cuidado\nVerifica los campos");
                alert.setContentText(errores);
                alert.showAndWait();
                dialog.setResult(ButtonType.CLOSE);
                return procesOutput(id);
            }
        } else{
            if (delete){
                engine.executeScript(removeMarker());
            }
            return null;
        }
    }
    private List<String> buscarCoordenadas(String texto, boolean preciso){
        String query;
        boolean isCoordinate = texto.charAt(0) == '-';
        if (preciso && !isCoordinate){
            query = (texto + ", Tres de Febrero, Provincia de Buenos Aires").replace(" ", "+");
        }else{
            query = texto.replace(" ", "+");
        }
        String encodedQuery;
        encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url = "https://www.google.com/maps/search/"+encodedQuery;
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet request = new HttpGet(url);
        try {
            CloseableHttpResponse response = httpClient.execute(request);
            if (response.getStatusLine().getStatusCode() == 200) {
                String responseString = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
                String coordenadas;
                Pattern pattern = Pattern.compile("/@(.{28})");
                Matcher matcher = pattern.matcher(responseString);
                Pattern pattern2 = Pattern.compile("preview/place/(.{300})");
                Matcher matcher2 = pattern2.matcher(responseString);
                String localidaFinal = "";
                if (!isCoordinate && matcher2.find()) {
                    String resultado = matcher2.group(1);
                    resultado = URLDecoder.decode(resultado, StandardCharsets.UTF_8);
                    resultado = resultado.replace("+", " ").toLowerCase();
                    for (String localidad : Main.LOCALIDADES){
                        int indice = resultado.indexOf(localidad.toLowerCase());
                        if (indice != -1){
                            localidaFinal = localidad;
                            break;
                        }else{
                            if (localidad.equals("CIUDAD JARDIN")){
                                indice = resultado.indexOf("cdad. jardin");
                                if (indice != -1){
                                    localidaFinal = localidad;
                                    break;
                                }
                            }else if (localidad.equals("CASEROS SUR")){
                                indice = resultado.indexOf("caseros");
                                if (indice != -1){
                                    localidaFinal = localidad;
                                    break;
                                }
                            }
                        }
                    }
                }else if (isCoordinate){
                    responseString = responseString.toLowerCase();
                    for (String localidad : Main.LOCALIDADES){
                        int indice = responseString.indexOf(localidad.toLowerCase());
                        if (indice != -1){
                            localidaFinal = localidad;
                            break;
                        }else{
                            if (localidad.equals("CIUDAD JARDIN")){
                                indice = responseString.indexOf("cdad. jardin");
                                if (indice != -1){
                                    localidaFinal = localidad;
                                    break;
                                }
                            }else if (localidad.equals("CASEROS SUR")){
                                indice = responseString.indexOf("caseros");
                                if (indice != -1){
                                    localidaFinal = localidad;
                                    break;
                                }
                            }
                        }
                    }
                }
                if (localidaFinal.length() >0){
                    selectorLocalidad.setValue(localidaFinal);
                    if (!isCoordinate){
                        textoUbicacion.setText(texto);
                    }
                }else{
                    selectorLocalidad.setValue("Seleccionar Localidad");
                }
                char menos = '-';
                char punto = '.';
                if (matcher.find()) {
                    coordenadas = matcher.group(1);
                    List<String> coordenadasRetorno = new ArrayList<>();
                    for(String coordenada: coordenadas.split(",",2)){
                        String coordenadaCompleta = "";
                        for(int i = 0; i<coordenada.length();i++){
                            char character = coordenada.charAt(i);
                            if (Character.isDigit(character) || character == menos || character == punto){
                                coordenadaCompleta += character;
                            }else{
                                break;
                            }
                        }
                        coordenadasRetorno.add(coordenadaCompleta);
                    }
                    return coordenadasRetorno;
                }
            } else {
                System.out.println("Error al obtener la respuesta del servidor");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
    private String addMarker(String latitud, String longitud){
        String icono = selectorTipo.getValue();
        if(icono.equals("Seleccionar Tipo")){
            icono = "marker";
        }
        File file = new File("src/main/resources/iconos/"+icono+"Temp.png");
        String text = file.getAbsolutePath() + "\"";
        text = text.replace("\\","/");
        return "var newLonLat = new OpenLayers.LonLat( "+ longitud + " ,"+ latitud +
                " ).transform(new OpenLayers.Projection(\"EPSG:4326\"), map.getProjectionObject());" +
                "var markerTemp = new OpenLayers.Marker(newLonLat, new OpenLayers.Icon(\"file:///"+ text +", new OpenLayers.Size(32, 32)));"+
                "markers.addMarker(markerTemp);";
    }
    private String removeMarker(){
        return "markers.removeMarker(markerTemp);";
    }

}
