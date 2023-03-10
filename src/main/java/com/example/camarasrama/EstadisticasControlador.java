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
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EstadisticasControlador {
    @FXML
    private Pagination paginationTipo;
    @FXML
    private ChoiceBox<String> selectorOpcion;
    @FXML
    private Pagination paginationChart;
    private BorderPane borderPrincipal;
    private GridPane centerGrid;
    private int cantidad = 0;
    private int operativas = 0;
    private int inestables = 0;
    private int noOperativas = 0;
    public EstadisticasControlador(){

    }
    public void run(Stage stage, ObservableList<Camara> listaCamaras){
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(stage);
        dialog.setTitle("Estadisticas");
        Parent root;
        try {
            root = FXMLLoader.load(new File("src/main/java/com/example/camarasrama/estadisticasLayout.fxml").toURI().toURL());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        dialog.getDialogPane().setContent(root);
        VBox boxSuperior = (VBox) dialog.getDialogPane().lookup("#boxSuperior");
        borderPrincipal = (BorderPane)dialog.getDialogPane().lookup("#borderPrincipal");
        centerGrid = (GridPane)borderPrincipal.getCenter();
        paginationTipo = (Pagination) boxSuperior.lookup("#paginationTipo");
        paginationChart = (Pagination) dialog.getDialogPane().lookup("#paginationChart");
        selectorOpcion = (ChoiceBox<String>) boxSuperior.lookup("#selectorOpcion");
        selectorOpcion.setValue("Seleccionar Localidad");
        selectorOpcion.setItems(Main.LOCALIDADES);
        paginationTipo.setPageFactory((index) -> {
            if (index == 0){
                return new Label("Localidades");
            }else{
                return new Label("Servidores");
            }
        });
        paginationTipo.currentPageIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if (newValue.intValue() == 0){
                    selectorOpcion.setValue("Seleccionar Localidad");
                    selectorOpcion.setItems(Main.LOCALIDADES);
                }else{
                    selectorOpcion.setValue("Seleccionar Servidor");
                    selectorOpcion.setItems(Main.SERVIDORES);
                }
            }
        });
        selectorOpcion.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                cantidad = 0;
                operativas = 0;
                inestables = 0;
                noOperativas = 0;
                for(Camara camara: listaCamaras){
                    if (paginationTipo.getCurrentPageIndex() == 0){
                        if (camara.getLocalidad().equals(selectorOpcion.getValue())){
                            cantidad += 1;
                            if (camara.getActividad().equals("OPERATIVA")){
                                operativas += 1;
                            }else if (camara.getActividad().equals("NO OPERATIVA")){
                                noOperativas += 1;
                            }else{
                                inestables+=1;
                            }
                        }
                    }else{
                        if (camara.getServidor().equals(selectorOpcion.getValue())){
                            cantidad += 1;
                            if (camara.getActividad().equals("OPERATIVA")){
                                operativas += 1;
                            }else if (camara.getActividad().equals("NO OPERATIVA")){
                                noOperativas += 1;
                            }else{
                                inestables+=1;
                            }
                        }
                    }
                }
                createChart();
            }
        });
        Label labelChartType = (Label) borderPrincipal.getBottom().lookup("#labelChartType");
        paginationChart.currentPageIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if (newValue.intValue() == 0){
                    labelChartType.setText("Barras");
                }else{
                    labelChartType.setText("Torta");
                }
                createChart();
            }
        });
        createChart();
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();

    }
    public void createBarChart(){
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        XYChart.Data<String, Number> data1 = new XYChart.Data<>("Totales", cantidad);
        XYChart.Data<String, Number> data2 = new XYChart.Data<>("Operativa", operativas);
        XYChart.Data<String, Number> data3 = new XYChart.Data<>("No operativa", noOperativas);
        XYChart.Data<String, Number> data4 = new XYChart.Data<>("Inestbale", inestables);
        series.getData().addAll(data1,data2,data3,data4);
        barChart.getData().add(series);
        barChart.setLegendVisible(false);
        ObservableList<Node> childrens = centerGrid.getChildren();
        for (Node node : childrens) {
            if(centerGrid.getRowIndex(node) == 0 && centerGrid.getColumnIndex(node) == 0) {
                childrens.remove(node);
                break;
            }
        }
        centerGrid.add(barChart,0,0);
        StackPane stackPane1 = (StackPane) data1.getNode();
        stackPane1.setBackground(Background.fill(new Color(0.6,0.6,1,1)));
        StackPane stackPane2 = (StackPane) data2.getNode();
        stackPane2.setBackground(Background.fill(new Color(0.6,1,0.6,1)));
        StackPane stackPane3 = (StackPane) data3.getNode();
        stackPane3.setBackground(Background.fill(new Color(1,0.6,0.6,1)));
        StackPane stackPane4 = (StackPane) data4.getNode();
        stackPane4.setBackground(Background.fill(new Color(1,1,0.6,1)));
    }
    public void createPieChart(){
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        PieChart.Data data2 = new PieChart.Data("Operativas: "+operativas, operativas);
        PieChart.Data data3 = new PieChart.Data("No operativas: "+noOperativas, noOperativas);
        PieChart.Data data4 = new PieChart.Data("Inestables: "+ inestables, inestables);
        pieChartData.addAll(data2,data3,data4);
        PieChart chart = new PieChart(pieChartData);
        ObservableList<Node> childrens = centerGrid.getChildren();
        for (Node node : childrens) {
            if(centerGrid.getRowIndex(node) == 0 && centerGrid.getColumnIndex(node) == 0) {
                childrens.remove(node);
                break;
            }
        }
        centerGrid.add(chart,0,0);
        Region region2 = (Region)data2.getNode();
        region2.setBackground(Background.fill(new Color(0.6,1,0.6,1)));
        Region region3 = (Region)data3.getNode();
        region3.setBackground(Background.fill(new Color(1,0.6,0.6,1)));
        Region region4 = (Region)data4.getNode();
        region4.setBackground(Background.fill(new Color(1,1,0.6,1)));
        chart.setLegendVisible(false);
    }

    public void createChart(){
        if (paginationChart.getCurrentPageIndex() == 0){
            createBarChart();
        }else {
            createPieChart();
        }
    }
}
