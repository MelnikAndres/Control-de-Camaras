package com.example.camarasrama;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javafx.util.converter.NumberStringConverter;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GraficosControlador {

    private List<Integer> localidadesCant = new ArrayList<>();
    private List<Integer> localidadesOperativa = new ArrayList<>();
    private List<Integer> localidadesNoOperativa = new ArrayList<>();
    private List<Integer> localidadesInestable = new ArrayList<>();

    private List<Integer> servidoresCant = new ArrayList<>();
    private List<Integer> servidoresOperativa = new ArrayList<>();
    private List<Integer> servidoresNoOperativa = new ArrayList<>();
    private List<Integer> servidoresInestable = new ArrayList<>();
    @FXML
    private Pagination paginationTipoTodos;
    private Label labelChartTipo;
    @FXML
    private GridPane gridPrincipal;
    public GraficosControlador(){

    }
    public void run(Stage stage, ObservableList<Camara> listaCamaras){
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(stage);
        dialog.setTitle("Graficos y Estadisticas");
        Parent root;
        try {
            root = FXMLLoader.load(new File("src/main/java/com/example/camarasrama/graficosLayout.fxml").toURI().toURL());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        dialog.getDialogPane().setContent(root);
        paginationTipoTodos = (Pagination) dialog.getDialogPane().lookup("#paginationTipoTodos");
        labelChartTipo = (Label) dialog.getDialogPane().lookup("#labelChartTipo");
        gridPrincipal = (GridPane) dialog.getDialogPane().lookup("#gridPrincipal");

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int height = screenSize.height-100;
        dialog.getDialogPane().setPrefHeight(height);
        gridPrincipal.setPrefHeight(height);
        dialog.setResizable(true);

        for (int i=0;i<Main.SERVIDORES.size();i++){
            servidoresCant.add(0);
            servidoresOperativa.add(0);
            servidoresNoOperativa.add(0);
            servidoresInestable.add(0);
        }
        for (int i=0;i<Main.LOCALIDADES.size();i++){
            localidadesCant.add(0);
            localidadesOperativa.add(0);
            localidadesNoOperativa.add(0);
            localidadesInestable.add(0);
        }
        for (Camara listaCamara : listaCamaras) {
            int posicion = Main.SERVIDORES.indexOf(listaCamara.getServidor());
            servidoresCant.set(posicion, servidoresCant.get(posicion) + 1);
            int posicion2 = Main.LOCALIDADES.indexOf(listaCamara.getLocalidad());
            localidadesCant.set(posicion2, localidadesCant.get(posicion2) + 1);
            if (listaCamara.getActividad().equals("OPERATIVA")) {
                servidoresOperativa.set(posicion, servidoresOperativa.get(posicion) + 1);
                localidadesOperativa.set(posicion2,localidadesOperativa.get(posicion2)+1);
            }else if (listaCamara.getActividad().equals("NO OPERATIVA")) {
                servidoresNoOperativa.set(posicion, servidoresNoOperativa.get(posicion) + 1);
                localidadesNoOperativa.set(posicion2,localidadesNoOperativa.get(posicion2)+1);
            }else{
                servidoresInestable.set(posicion, servidoresInestable.get(posicion) + 1);
                localidadesInestable.set(posicion2,localidadesInestable.get(posicion2)+1);
            }
        }
        createChartLocalidad();
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                gridPrincipal.setPrefWidth(newValue.doubleValue()-40);
                dialog.getDialogPane().setPrefWidth(newValue.doubleValue());
            }
        });
        dialog.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                gridPrincipal.setPrefHeight(newValue.doubleValue()-40);
                dialog.getDialogPane().setPrefHeight(newValue.doubleValue());
            }
        });
        paginationTipoTodos.currentPageIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                clearChart();
                createChart();
            }
        });
        dialog.showAndWait();
    }

    public void createChartLocalidad(){
        CategoryAxis xAxis2 = new CategoryAxis();
        NumberAxis yAxis2 = new NumberAxis();
        yAxis2.setAutoRanging(false);
        yAxis2.setLowerBound(0);
        yAxis2.setUpperBound(100);
        yAxis2.setTickUnit(10);
        BarChart<String, Number> graficoLocalidad = new BarChart<>(xAxis2,yAxis2);
        XYChart.Series<String, Number> series2 = new XYChart.Series<>();
        for (int i = 0; i < Main.LOCALIDADES.size();i++){
            if (localidadesCant.get(i) > 0){
                int porcentaje = 100 - (((localidadesCant.get(i)*100 - localidadesOperativa.get(i)*100)/localidadesCant.get(i)));
                XYChart.Data<String, Number> data = new XYChart.Data<>(Main.LOCALIDADES.get(i), porcentaje);
                Label label = new Label(porcentaje + "%");
                label.setAlignment(Pos.TOP_CENTER);
                label.setTextFill(Paint.valueOf("WHITE"));
                label.setPadding(new Insets(10,0,0,0));
                data.setNode(label);
                series2.getData().add(data);
            }else{
                series2.getData().add(new XYChart.Data<>(Main.LOCALIDADES.get(i), 0));
            }
        }
        graficoLocalidad.getData().add(series2);
        NumberAxis numberAxis2 = (NumberAxis) graficoLocalidad.getYAxis();
        NumberStringConverter converter = new NumberStringConverter("#'%'");
        numberAxis2.setTickLabelFormatter(converter);
        graficoLocalidad.setTitle("Por Localidad");
        graficoLocalidad.setLegendVisible(false);
        gridPrincipal.add(graficoLocalidad,0,1);
    }
    public void createChartServidor(){
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(0);
        yAxis.setUpperBound(100);
        yAxis.setTickUnit(10);
        BarChart<String, Number> graficoServidor = new BarChart<>(xAxis,yAxis);
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (int i = 0; i < Main.SERVIDORES.size();i++){
            if (servidoresCant.get(i) > 0){
                int porcentaje = 100 - (((servidoresCant.get(i)*100 - servidoresOperativa.get(i)*100)/servidoresCant.get(i)));
                XYChart.Data<String, Number> data = new XYChart.Data<>(Main.SERVIDORES.get(i), porcentaje);
                Label label = new Label(porcentaje + "%");
                label.setAlignment(Pos.TOP_CENTER);
                label.setTextFill(Paint.valueOf("WHITE"));
                label.setPadding(new Insets(10,0,0,0));
                data.setNode(label);
                series.getData().add(data);
            }else{
                series.getData().add(new XYChart.Data<>(Main.SERVIDORES.get(i), 0));
            }
        }
        graficoServidor.getData().add(series);
        NumberAxis numberAxis = (NumberAxis) graficoServidor.getYAxis();
        NumberStringConverter converter = new NumberStringConverter("#'%'");
        numberAxis.setTickLabelFormatter(converter);
        graficoServidor.setTitle("Por Servidor");
        graficoServidor.setLegendVisible(false);
        gridPrincipal.add(graficoServidor,0,1);

    }

    public void clearChart(){
        ObservableList<Node> childrens = gridPrincipal.getChildren();
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> barChart = new BarChart<>(xAxis,yAxis);
        for (Node node : childrens) {
            if (node.getClass().equals(barChart.getClass())){
                childrens.remove(node);
                break;
            }
        }
    }
    public void createChart(){
        if(paginationTipoTodos.getCurrentPageIndex() == 0){
            createChartLocalidad();
        }else{
            createChartServidor();
        }
    }
}
