package com.example.camarasrama;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseWheelEvent;
import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main extends Application {
    public static final ObservableList<String> LOCALIDADES = FXCollections.observableArrayList("CASEROS SUR","CASEROS NORTE","CHURRUCA","CIUDADELA","CIUDAD JARDIN",
            "EL LIBERTADOR","JOSÉ INGENIEROS","LOMA HERMOSA","MARTÍN CORONADO","PABLO PODESTÁ","REMEDIOS DE ESCALADA","SÁENZ PEÑA",
            "SANTOS LUGARES","VILLA BOSCH","VILLA RAFFO","11 DE SEPTIEMBRE");


    public static final ObservableList<String> TIPOS = FXCollections.observableArrayList(
            "Fija","Domo","Switch","Radio enlace","LPR","Parada Segura");
    public static ObservableList<String> SERVIDORES = FXCollections.observableArrayList();
    private int markerCount;
    @FXML
    private ListView<Camara> visorCamaras;
    @FXML
    private MenuBar menuBar;
    private WebEngine webEngine;
    private WebView webView;
    private ObservableList<Camara> listaCamaras;
    private FilteredList<Camara> listaFiltradaCamaras;
    private HashSet<String> remark;
    private Predicate<Camara> predicadoBusqueda;
    private HashMap<String, Predicate<Camara>> predicadosFiltroLocalidad;
    private HashMap<String, Predicate<Camara>> predicadosFiltroActividad;
    private HashMap<String, Predicate<Camara>> predicadosFiltroServidor;
    private Insets inset;
    private Scene scene;
    private double cellHeight;
    @FXML
    private TextField buscadorCamaras;
    private Rectangle recBuscador;
    private boolean isClicking,fromDrag;
    private String tipoDrag;
    private BorderPane borderPane;
    private GridPane gridIconos;
    public static Camara recordada = null;
    @FXML
    private ToggleButton toggleHide;
    @FXML
    private Slider zoomer;

    @Override
    public void start(Stage stage) throws IOException {
        setUp(stage);
        setComponents(stage);
        cargarCamaras(stage);
    }
    public void setUp(Stage stage)throws IOException{
        markerCount = 0;
        Parent root = FXMLLoader.load(new File("src/main/java/com/example/camarasrama/mapLayout.fxml").toURI().toURL());
        scene = new Scene(root);
        webView = (WebView) root.lookup("#visorMapa");
        // Get the WebEngine
        webEngine = webView.getEngine();
        webEngine.setJavaScriptEnabled(true);
        webView.setContextMenuEnabled(false);
        webView.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.CONTROL) {
                event.consume();
            }
        });
        cargarServidores();
        SERVIDORES.sort(String::compareTo);
        // Load the Google Maps URL
        String script = readFile("src/main/java/com/example/camarasrama/inicializar.js");
        webEngine.loadContent(script);
        webView.cursorProperty().addListener(new ChangeListener<Cursor>() {
            @Override
            public void changed(ObservableValue<? extends Cursor> observable, Cursor oldValue, Cursor newValue) {
                if(newValue == Cursor.TEXT){
                    webView.fireEvent(new MouseEvent(MouseEvent.MOUSE_RELEASED, 0, 0, 0, 0, MouseButton.PRIMARY,
                            1, true, true, true, true,
                            true, true, true, true,
                            true, true, null));
                }
            }
        });
        AnchorPane anchorPane =(AnchorPane) root.lookup("#baseAnchor");
        borderPane = (BorderPane) root.lookup("#baseBorder");
        gridIconos = (GridPane) borderPane.lookup("#gridIconos");
        setIconos();
        Circle circle = (Circle) borderPane.lookup("#circuloAgregar");
        ImageView botonIconos = (ImageView) borderPane.lookup("#botonIconos");
        botonIconos.setOnMouseClicked(event -> {
            gridIconos.setVisible(!gridIconos.isVisible());
        });
        botonIconos.setOnMouseEntered(event -> {
            circle.setFill(new Color(1,1,1,0.8));
        });
        botonIconos.setOnMouseExited(event -> {
            if (!buscadorCamaras.isFocused() && predicadoBusqueda == null){
                circle.setFill(new Color(0.9,0.9,0.9,0.7));
            }
        });
        zoomer = (Slider) borderPane.lookup("#zoomer");
        zoomer.setValue(16);
        zoomer.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                webEngine.executeScript("map.zoomTo("+newValue+")");
            }
        });
        webView.setOnScroll(event -> {
            double deltaY = event.getDeltaY();
            if (deltaY > 0){
                zoomer.setValue(zoomer.getValue() + 1);
            }else{
                if(zoomer.getValue() <=14){
                    return;
                }
                zoomer.setValue(zoomer.getValue() - 1);
            }
        });
        visorCamaras = (ListView<Camara>) borderPane.lookup("#visorCamaras");
        listaCamaras = FXCollections.observableList(new ArrayList<>());
        cargarCamarasDesdeCSV("src/main/java/com/example/camarasrama/Camaras.csv", false);
        CSVManager csvManager = new CSVManager();
        listaCamaras.addListener(new ListChangeListener<Camara>() {
            @Override
            public void onChanged(Change<? extends Camara> c) {
                csvManager.guardarCSV(new File("src/main/java/com/example/camarasrama/Camaras.csv"), listaCamaras);
            }
        });
        listaFiltradaCamaras = new FilteredList<>(listaCamaras);
        listaFiltradaCamaras.setPredicate(camara -> true);
        menuBar = (MenuBar) borderPane.lookup("#menuBar");
        remark = new HashSet<>();
        buscadorCamaras =(TextField) borderPane.lookup("#buscadorCamaras");
        recBuscador = (Rectangle) borderPane.lookup("#cuadradoRelleno");
        buscadorCamaras.setStyle("-fx-prompt-text-fill: #8A8A8A;");
        buscadorCamaras.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if(newValue){
                    recBuscador.setFill(new Color(1,1,1,0.8));
                }else{
                    if (predicadoBusqueda == null){
                        recBuscador.setFill(new Color(0.9,0.9,0.9,0.7));
                    }
                }
            }
        });
        buscadorCamaras.setOnMouseEntered(event -> {
            recBuscador.setFill(new Color(1,1,1,0.8));
        });
        buscadorCamaras.setOnMouseExited(event -> {
            if (!buscadorCamaras.isFocused() && predicadoBusqueda == null){
                recBuscador.setFill(new Color(0.9,0.9,0.9,0.7));
            }
        });

        buscadorCamaras.setOnKeyReleased(event ->{
            visorCamaras.scrollTo(0);
            if (buscadorCamaras.getText().equals("")) {
                predicadoBusqueda = null;
                resetFilters();
            }else{
                predicadoBusqueda = camara -> searchAndFilter(camara,buscadorCamaras.getText());
            }
            listaFiltradaCamaras.setPredicate(predicates());
        });
        agregarFast();
        stage.setScene(scene);
        stage.setTitle("Control de Camaras");
        File imagen = new File("src/main/resources/iconos/camara.png");
        String imagenPath = imagen.getAbsolutePath();
        stage.getIcons().add(new Image(imagenPath));
        stage.show();
        scene.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                anchorPane.setPrefWidth(newValue.doubleValue());
                borderPane.setPrefWidth(newValue.doubleValue());
            }
        });
        scene.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                anchorPane.setPrefHeight(newValue.doubleValue());
                borderPane.setPrefHeight(newValue.doubleValue());
            }
        });
        stage.setMaximized(true);
        setMenu(stage);
    }
    public void setIconos(){
        ImageView iconoFija = (ImageView) borderPane.lookup("#iconoFija");
        ImageView iconoDomo = (ImageView) borderPane.lookup("#iconoDomo");
        ImageView iconoSwitch = (ImageView) borderPane.lookup("#iconoSwitch");
        ImageView iconoRadio = (ImageView) borderPane.lookup("#iconoRadio");
        ImageView iconoLPR = (ImageView) borderPane.lookup("#iconoLPR");
        ImageView iconoParada = (ImageView) borderPane.lookup("#iconoParada");
        setIcono(iconoFija);
        setIcono(iconoDomo);
        setIcono(iconoSwitch);
        setIcono(iconoRadio);
        setIcono(iconoLPR);
        setIcono(iconoParada);

    }
    public void setIcono(ImageView imageView){
        imageView.setCursor(Cursor.CLOSED_HAND);
        imageView.setOnDragDetected((MouseEvent event) -> {
            // start a drag-and-drop gesture
            Dragboard db = imageView.startDragAndDrop(TransferMode.COPY);
            // put the image on the dragboard
            ClipboardContent content = new ClipboardContent();
            content.putImage(imageView.getImage());
            db.setContent(content);
            event.consume();
            ObservableList<Node> gridChildren = gridIconos.getChildren();
            for(int i=0;i<gridChildren.size();i++){
                if(gridChildren.get(i) == imageView){
                    tipoDrag = TIPOS.get(i);
                }
            }
        });
        imageView.setOnDragDone(event -> {
            Robot robot;
            try {
                robot = new Robot();
            } catch (AWTException e) {
                throw new RuntimeException(e);
            }
            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
            fromDrag = true;
        });
    }

    public void cargarServidores(){
        try {
            FileReader fileReader = new FileReader(new File("src/main/java/com/example/camarasrama/servidores.txt"));
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            while((line = bufferedReader.readLine()) != null) {
                SERVIDORES.add(line);
            }
            bufferedReader.close();
            fileReader.close();
        } catch(FileNotFoundException ex) {
            System.out.println("El archivo no pudo ser encontrado: " + ex.getMessage());
        } catch(IOException ex) {
            System.out.println("Error al leer el archivo: " + ex.getMessage());
        }
    }

    public void startExecutors(Stage stage){
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                pingATodo();
            }
        }, 0, 2, TimeUnit.MINUTES);
        stage.setOnCloseRequest(event -> {
            // shutdown the executor when the stage is closed
            executor.shutdown();
        });
    }

    public boolean searchAndFilter(Camara camara, String busqueda){
        if (camara.getUbicacion().toLowerCase().contains(busqueda.toLowerCase())){
            if (remark.contains(String.valueOf(camara.getId()))){
                webEngine.executeScript(reintegrarMarker(camara.getId()));
                remark.remove(String.valueOf(camara.getId()));
            }
            return true;
        }else{
            webEngine.executeScript(removeMarker(camara.getId()));
            remark.add(String.valueOf(camara.getId()));
            return false;
        }
    }

    public void generarRandom(){
        Random random = new Random();
        List<String> actividades = new ArrayList<>();
        actividades.add("OPERATIVA");
        actividades.add("NO OPERATIVA");
        actividades.add("INESTABLE ");
        List<String> letras = new ArrayList<>();
        letras.add("a");
        letras.add("b");
        letras.add("c");
        letras.add("d");
        letras.add("e");
        letras.add("f");
        letras.add("g");
        letras.add("h");
        letras.add("i");
        letras.add("j");
        letras.add("k");
        letras.add("l");
        letras.add("m");
        letras.add("n");
        letras.add("o");
        letras.add("p");

        for(int i = 0; i<3000;i++){
            int indexLocalidad = random.nextInt(LOCALIDADES.size());
            String localidad = LOCALIDADES.get(indexLocalidad);
            int latitud = random.nextInt(300000,600000);
            latitud = -latitud;
            long latitud2 = latitud/10000;
            int longitud = random.nextInt(300000,600000);
            longitud = -longitud;
            long longitud2 = longitud/10000;
            int indexTipo = random.nextInt(TIPOS.size());
            String tipo = TIPOS.get(indexTipo);
            int indexServidor = random.nextInt(SERVIDORES.size());
            String servidor = SERVIDORES.get(indexServidor);
            int indexName1 = random.nextInt(letras.size());
            String name1 = letras.get(indexName1);
            int indexName2 = random.nextInt(letras.size());
            String name2 = letras.get(indexName2);
            Camara camara = new Camara("1."+markerCount,name1 + name2,localidad,String.valueOf(latitud2),String.valueOf(longitud2),tipo,servidor,markerCount);
            int indexActividad = random.nextInt(actividades.size());
            String actividad = actividades.get(indexActividad);
            camara.setActividad(actividad);
            agregarCamara(camara,false);
        }
    }
    public void agregarFast(){
        TextField buscadorIr =(TextField) borderPane.lookup("#buscadorIr");
        Rectangle cuadradoIr = (Rectangle) borderPane.lookup("#cuadradoRelleno");
        buscadorIr.setStyle("-fx-prompt-text-fill: #8A8A8A;");
        buscadorIr.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if(newValue){
                    cuadradoIr.setFill(new Color(1,1,1,0.8));
                }else{
                    if (buscadorIr.getText().equals("")){
                        cuadradoIr.setFill(new Color(0.9,0.9,0.9,0.7));
                    }
                }
            }
        });
        buscadorIr.setOnMouseEntered(event -> {
            cuadradoIr.setFill(new Color(1,1,1,0.8));
        });
        buscadorIr.setOnMouseExited(event -> {
            if (!buscadorIr.isFocused() && buscadorIr.getText().equals("")){
                cuadradoIr.setFill(new Color(0.9,0.9,0.9,0.7));
            }
        });

        buscadorIr.setOnKeyReleased(event ->{
            if(event.getCode() == KeyCode.ENTER){
                List<String> coordenadas = buscarCoordenadas(buscadorIr.getText());
                if(coordenadas != null && coordenadas.size() == 2){
                    webEngine.executeScript("var newLonLat = new OpenLayers.LonLat( "+ coordenadas.get(1) + " ,"+ coordenadas.get(0) +
                            " ).transform(new OpenLayers.Projection(\"EPSG:4326\"), map.getProjectionObject());" +
                            "map.panTo(newLonLat)"
                    );
                    buscadorIr.setText("");
                }else{
                    buscadorIr.setText("Ubicacion no encontrada");
                }

            }
        });
    }

    private List<String> buscarCoordenadas(String texto){
        String query = (texto + ", Tres de Febrero, Provincia de Buenos Aires").replace(" ", "+");
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

    public void cargarCamaras(Stage stage){
        webEngine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Worker.State.SUCCEEDED) {
                visorCamaras.setItems(listaFiltradaCamaras);
                for(Camara camara: listaCamaras){
                    webEngine.executeScript(addMarker(camara.getLatitud(),camara.getLongitud(),camara.getId(), camara.getIconPath()));
                }
                webEngine.executeScript("var id = -1;"+"function getLastId() {" +
                        "var retorno = id;" +
                        "id = -1;"+
                        "return retorno;}");
                startExecutors(stage);
                ContextMenu contextMenu = new ContextMenu();
                MenuItem copyCoordItem = new MenuItem("Copiar coordenadas");
                contextMenu.getItems().addAll(copyCoordItem);
                webEngine.executeScript("var element = 0;" +
                        "function getCoord() {" +
                        "return element;}");
                webEngine.executeScript("map.events.register('click',map, function (e) {"+
                        "var lonlat = map.getLonLatFromViewPortPx(e.xy);"+
                        "var lonlatWGS84 = lonlat.transform(map.getProjectionObject(), new OpenLayers.Projection(\"EPSG:4326\"));"+
                        "element = lonlatWGS84;" +
                        "});");
                copyCoordItem.setOnAction(event -> {
                    // Execute JavaScript to retrieve the image source URL
                    Object coord = webEngine.executeScript("getCoord();");
                    // Create a clipboard image
                    ClipboardContent content = new ClipboardContent();
                    content.putString(String.valueOf(coord));
                    // Put the image in the clipboard
                    Clipboard.getSystemClipboard().setContent(content);
                });
                isClicking = false;
                webView.setOnMouseClicked(event -> {
                    if(fromDrag){
                        Object coord = webEngine.executeScript("getCoord();");
                        DialogoAgregarControlador dialogoAgregarControlador = new DialogoAgregarControlador();
                        Camara camara = dialogoAgregarControlador.run(stage,markerCount,null, webEngine, visorCamaras,coord.toString(),tipoDrag);
                        if (camara != null){
                            agregarCamara(camara, true);
                        }
                        fromDrag = false;
                        tipoDrag = null;
                        gridIconos.setVisible(false);
                        return;
                    }
                    if (event.getButton() == MouseButton.SECONDARY) {
                        Robot robot;
                        try {
                            robot = new Robot();
                        } catch (AWTException e) {
                            throw new RuntimeException(e);
                        }
                        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);

                        isClicking = true;
                    } else {
                        if(isClicking){
                            contextMenu.show(webView, event.getScreenX(), event.getScreenY());
                            isClicking = false;
                        }else{
                            contextMenu.hide();
                        }
                    }
                });
                webView.setOnKeyPressed(event -> {
                    if (event.getCode() == KeyCode.LEFT){
                        Object ids = webEngine.executeScript("getLastId();");
                        long idFinal = Long.parseLong(String.valueOf(ids));
                        if (idFinal != -1){
                            for(int i = 0; i< listaFiltradaCamaras.size();i++){
                                Camara camara = listaFiltradaCamaras.get(i);
                                if (camara.getId() == idFinal){
                                    final int i2 = i;
                                    ObservableList<Node> children = visorCamaras.getChildrenUnmodifiable();
                                    VirtualFlow<IndexedCell> virtualFlowPre = null;
                                    for (Node child : children) {
                                        if (child instanceof VirtualFlow) {
                                            virtualFlowPre = (VirtualFlow<IndexedCell>) child;
                                            break;
                                        }
                                    }
                                    final VirtualFlow<IndexedCell> virtualFlow = virtualFlowPre;
                                    int lastCell = virtualFlow.getCellCount();
                                    int firstVisible = virtualFlow.getFirstVisibleCell().getIndex();
                                    int lastVisible = virtualFlow.getLastVisibleCell().getIndex();
                                    final int range = (lastVisible-firstVisible);
                                    Platform.runLater(() -> {

                                        if (i2 < lastCell-(range)){
                                            visorCamaras.scrollTo(i2);
                                        }else{
                                            visorCamaras.scrollTo(lastCell-range);
                                        }
                                        visorCamaras.getSelectionModel().select(i2);
                                    });
                                }
                            }
                        }
                    }
                });
                webView.setOnMouseReleased(event -> {
                    Robot robot;
                    try {
                        robot = new Robot();
                    } catch (AWTException e) {
                        throw new RuntimeException(e);
                    }
                    robot.keyPress(java.awt.event.KeyEvent.VK_LEFT);
                    robot.keyRelease(java.awt.event.KeyEvent.VK_LEFT);
                });
            }
        });
    }
    public void setMenu(Stage stage){
        predicadosFiltroLocalidad = new HashMap<>();
        predicadosFiltroActividad = new HashMap<>();
        predicadosFiltroServidor = new HashMap<>();
        Rectangle rec = (Rectangle) menuBar.getParent().lookup("#menuFiller");
        menuBar.setOnMouseEntered(event -> {
            rec.setFill(new Color(0.95,0.95,0.95,0.8));
        });
        menuBar.setOnMouseExited(event -> {
            if (!menuBar.isFocused()){
                rec.setFill(new Color(0.816,0.816,0.816,0.5));
            }
        });
        MenuItem menuFull = menuBar.getMenus().get(4).getItems().get(0);
        menuFull.setOnAction(event -> stage.setFullScreen(true));
        MenuItem menuItemAgregar = menuBar.getMenus().get(1).getItems().get(0);
        menuItemAgregar.setOnAction(actionEvent -> {
            DialogoAgregarControlador controlador = new DialogoAgregarControlador();
            Camara camara = controlador.run(stage,markerCount, null, webEngine, visorCamaras, null,null);
            if (camara != null){
                agregarCamara(camara, true);
            }
        });
        MenuItem menuItemTutorial = menuBar.getMenus().get(5).getItems().get(0);
        menuItemTutorial.setOnAction(actionEvent -> {
            DialogoTutorial dialogoTutorial = new DialogoTutorial();
            dialogoTutorial.run(stage);
        });

        Menu menuFilterLocalidad = (Menu) menuBar.getMenus().get(2).getItems().get(0);
        setHandlersFiltros(menuFilterLocalidad, 0);
        Menu menuFilterActividad = (Menu) menuBar.getMenus().get(2).getItems().get(1);
        setHandlersFiltros(menuFilterActividad,1);
        Menu menuFilterServidor = (Menu) menuBar.getMenus().get(2).getItems().get(2);
        menuFilterServidor.getItems().add(new MenuItem("TODAS"));
        for(String servidor : SERVIDORES){
            CheckMenuItem item = new CheckMenuItem(servidor);
            menuFilterServidor.getItems().add(item);
        }
        SERVIDORES.addListener(new ListChangeListener<String>() {
            @Override
            public void onChanged(Change<? extends String> c) {
                menuFilterServidor.getItems().clear();
                menuFilterServidor.getItems().add(new MenuItem("TODAS"));
                for(String servidor : SERVIDORES){
                    CheckMenuItem item = new CheckMenuItem(servidor);
                    menuFilterServidor.getItems().add(item);
                }
                setHandlersFiltros(menuFilterServidor,2);
            }
        });
        setHandlersFiltros(menuFilterServidor,2);
        MenuItem menuPing = menuBar.getMenus().get(4).getItems().get(1);
        menuPing.setOnAction(event -> pingATodo());
        MenuItem menuSortActividad = menuBar.getMenus().get(3).getItems().get(0);
        menuSortActividad.setOnAction(event -> listaCamaras.sort((camara, t1) -> {
            if (camara.getActividad().length() > t1.getActividad().length()){
                return -1;
            }else if (camara.getActividad().length() == t1.getActividad().length()){
                return 0;
            }
            return 1;
        }));
        MenuItem menuSortLocalidad = menuBar.getMenus().get(3).getItems().get(1);
        menuSortLocalidad.setOnAction(event -> listaCamaras.sort(Comparator.comparing(Camara::getLocalidad)));
        MenuItem menuSortServidor = menuBar.getMenus().get(3).getItems().get(2);
        menuSortServidor.setOnAction(event -> listaCamaras.sort(Comparator.comparing(Camara::getServidor)));
        MenuItem menuSortIp = menuBar.getMenus().get(3).getItems().get(3);
        menuSortIp.setOnAction(event -> listaCamaras.sort(Comparator.comparing(Camara::getIp)));

        MenuItem menuItemImportar = menuBar.getMenus().get(0).getItems().get(0);
        menuItemImportar.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Selecciona el CSV a cargar");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Text Files", "*.txt","*.csv"));
            File selectedFile = fileChooser.showOpenDialog(stage);
            if (selectedFile != null){
                cargarCamarasDesdeCSV(selectedFile.toString(),true);

            }
        });
        MenuItem menuItemExportar = menuBar.getMenus().get(0).getItems().get(1);
        menuItemExportar.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Exportar CSV");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
            File selectedFile = fileChooser.showSaveDialog(stage);
            if (selectedFile != null) {
                CSVManager csvManager = new CSVManager();
                csvManager.guardarCSV(selectedFile,listaCamaras);
                }
            });
        MenuItem menuItemGuardar = menuBar.getMenus().get(0).getItems().get(2);
        menuItemGuardar.setOnAction(event ->{
            CSVManager csvManager = new CSVManager();
            csvManager.guardarCSV(new File("src/main/java/com/example/camarasrama/Camaras.csv"), listaCamaras);
        });
        MenuItem menuItemGraficos = menuBar.getMenus().get(6).getItems().get(0);
        menuItemGraficos.setOnAction(event ->{
            GraficosControlador graficosControlador = new GraficosControlador();
            graficosControlador.run(stage, listaCamaras);
        });
        MenuItem menuItemEstadisticas = menuBar.getMenus().get(6).getItems().get(1);
        menuItemEstadisticas.setOnAction(event ->{
            EstadisticasControlador estadisticasControlador = new EstadisticasControlador();
            estadisticasControlador.run(stage, listaCamaras);
        });
    }
    public void resetFilters(){
        for (Camara camara : listaCamaras){
            webEngine.executeScript(reintegrarMarker(camara.getId()));
            remark.remove(String.valueOf(camara.getId()));
        }
    }

    public void setHandlersFiltros(Menu menu, int filtro){
        ObservableList<MenuItem> items = menu.getItems();
        MenuItem opcionTodas = items.get(0);
        opcionTodas.setOnAction(event -> {
            for(int i = 1; i<items.size(); i++){
                CheckMenuItem item = (CheckMenuItem) items.get(i);
                item.setSelected(false);
            }
                if (filtro == 0){
                    predicadosFiltroLocalidad.clear();
                }
                if(filtro == 1){
                    predicadosFiltroActividad.clear();
                }
                if (filtro == 2){
                    predicadosFiltroServidor.clear();
                }
                resetFilters();
                listaFiltradaCamaras.setPredicate(predicates());
        });
        for(int i = 1; i<items.size(); i++){
            CheckMenuItem item = (CheckMenuItem) items.get(i);
            item.setOnAction(event -> {
                visorCamaras.scrollTo(0);
                String text = item.getText();
                if (!item.isSelected()) {
                    if (filtro == 0) {
                        predicadosFiltroLocalidad.remove(text);
                        if (predicadosFiltroLocalidad.size() == 0){
                            resetFilters();
                        }

                    }
                    if (filtro == 1){
                        predicadosFiltroActividad.remove(text);
                        if (predicadosFiltroActividad.size() == 0){
                            resetFilters();
                        }
                    }
                    if (filtro == 2){
                        predicadosFiltroServidor.remove(text);
                        if (predicadosFiltroServidor.size() == 0){
                            resetFilters();
                        }
                    }
                }else{
                    if (filtro == 0){
                        predicadosFiltroLocalidad.put(text, camara -> localidadFilter(camara, text));
                    }
                    if (filtro == 1){
                        predicadosFiltroActividad.put(text, camara -> actividadFilter(camara, text));
                    }
                    if (filtro ==2){
                        predicadosFiltroServidor.put(text, camara -> servidorFilter(camara, text));
                    }
                }
                listaFiltradaCamaras.setPredicate(predicates());
            });
        }

    }

    public Predicate<Camara> predicates(){
        Predicate<Camara> localidadPredicate = null;
        for(String predicateName: predicadosFiltroLocalidad.keySet()){
            if (localidadPredicate == null){
                localidadPredicate = predicadosFiltroLocalidad.get(predicateName);
            }else{
                localidadPredicate = localidadPredicate.or(predicadosFiltroLocalidad.get(predicateName));
            }
        }
        Predicate<Camara> actividadPredicate = null;
        for(String predicateName: predicadosFiltroActividad.keySet()){
            if (actividadPredicate == null){
                actividadPredicate = predicadosFiltroActividad.get(predicateName);
            }else{
                actividadPredicate = actividadPredicate.or(predicadosFiltroActividad.get(predicateName));
            }
        }
        Predicate<Camara> servidorPredicate = null;
        for(String predicateName: predicadosFiltroServidor.keySet()){
            if (servidorPredicate == null){
                servidorPredicate = predicadosFiltroServidor.get(predicateName);
            }else{
                servidorPredicate = servidorPredicate.or(predicadosFiltroServidor.get(predicateName));
            }
        }

        Predicate<Camara> finalPredicate = null;
        if (localidadPredicate != null){
            finalPredicate = localidadPredicate;
        }
        if (actividadPredicate != null){
            if (finalPredicate != null){
                finalPredicate = finalPredicate.and(actividadPredicate);
            }else{
                finalPredicate = actividadPredicate;
            }
        }
        if (servidorPredicate != null){
            if (finalPredicate != null){
                finalPredicate = finalPredicate.and(servidorPredicate);
            }else{
                finalPredicate = servidorPredicate;
            }
        }
        if (predicadoBusqueda != null){
            if (finalPredicate != null){
                finalPredicate = finalPredicate.and(predicadoBusqueda);
            }else{
                finalPredicate = predicadoBusqueda;
            }
        }
        return finalPredicate;
    }
    public void pingATodo(){
        ExecutorService executor = Executors.newFixedThreadPool(300);
        Thread pings = new Thread(() -> {
            List<Camara> listaCamarasCopia = List.copyOf(listaCamaras);
            for (Camara camara : listaCamarasCopia) {
                Function function = new Function(){
                    @Override
                    public void exec(){
                        Platform.runLater(() -> {
                            webEngine.executeScript(setMarkerIcon(camara.getId(),camara.getIconPath()));
                            visorCamaras.refresh();
                        });
                    }
                };
                executor.execute(new PingThread(camara,function));
            }
            executor.shutdown();
            try {
                if (executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS)){
                    CSVManager csvManager = new CSVManager();
                    csvManager.guardarCSV(new File("src/main/java/com/example/camarasrama/Camaras.csv"), listaCamaras);
                }
            } catch (InterruptedException e) {
                //Handle interruption
            }
        });
        pings.setDaemon(true);
        pings.start();

    }
    public boolean localidadFilter(Camara camara, String text){
        if (camara.getLocalidad().equals(text)){
            if (remark.contains(String.valueOf(camara.getId()))){
                webEngine.executeScript(reintegrarMarker(camara.getId()));
                remark.remove(String.valueOf(camara.getId()));
            }
            return true;
        }else{
            webEngine.executeScript(removeMarker(camara.getId()));
            remark.add(String.valueOf(camara.getId()));
            return false;
        }
    }
    public boolean actividadFilter(Camara camara, String text){
        if (camara.getActividad().equals(text)){
            if (remark.contains(String.valueOf(camara.getId()))){
                webEngine.executeScript(reintegrarMarker(camara.getId()));
                remark.remove(String.valueOf(camara.getId()));
            }
            return true;
        }else{
            webEngine.executeScript(removeMarker(camara.getId()));
            remark.add(String.valueOf(camara.getId()));
            return false;
        }
    }

    public boolean servidorFilter(Camara camara, String text){
        if (camara.getServidor().equals(text)){
            if (remark.contains(String.valueOf(camara.getId()))){
                webEngine.executeScript(reintegrarMarker(camara.getId()));
                remark.remove(String.valueOf(camara.getId()));
            }
            return true;
        }else{
            webEngine.executeScript(removeMarker(camara.getId()));
            remark.add(String.valueOf(camara.getId()));
            return false;
        }
    }


    public void setComponents(Stage stage){
        ContextMenu contextMenu = new ContextMenu();
        contextMenu.setAutoHide(true);
        MenuItem editItem = new MenuItem("Editar\t");
        MenuItem comentarioItem = new MenuItem("Ver comentario\t");
        MenuItem pingItem = new MenuItem("Ping\t");
        MenuItem deleteItem = new MenuItem("Borrar\t");
        contextMenu.getItems().addAll(editItem, comentarioItem,pingItem, deleteItem);
        inset = visorCamaras.getInsets();
        visorCamaras.setCellFactory(new Callback<>() {
            @Override
            public ListCell<Camara> call(ListView<Camara> listView) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(Camara item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setText(null);
                            setBackground(Background.EMPTY);
                        } else {
                            String texto = item.getUbicacion() + "\n" + item.getIp() + "\t  " + item.getServidor() + "\n" + item.getLocalidad() + "\t  " + item.getActividad();
                            setText(texto);
                            if (isSelected()) {
                                setBackground(Background.fill(cameraColor(item, true)));
                                Paint pintura = new Color(0, 0, 0, 1);
                                setTextFill(pintura);
                            } else {
                                setBackground(Background.fill(cameraColor(item, false)));
                            }
                        }
                    }
                };
            }
        });
        visorCamaras.setOnMouseClicked(click -> {
            ListView<Camara> list = (ListView<Camara>) click.getSource();
            Camara camara = list.getSelectionModel().getSelectedItem();
            if (click.getButton() == MouseButton.SECONDARY) {
                if (camara == null){
                    return;
                }
                contextMenu.show(visorCamaras, click.getScreenX(), click.getScreenY());
                deleteItem.setOnAction(e -> {
                    removeCamera(camara);
                });
                editItem.setOnAction(e -> {
                    DialogoAgregarControlador controlador = new DialogoAgregarControlador();
                    Camara nuevaCamara = controlador.run(stage, camara.getId(), camara, webEngine, visorCamaras, null,null);
                    if (nuevaCamara != null){
                        listaCamaras.set(list.getSelectionModel().getSelectedIndex(),nuevaCamara);
                        recolocarCamara(nuevaCamara);
                    }
                });
                comentarioItem.setOnAction(e ->{
                    ComentarioControlador controlador = new ComentarioControlador();
                    Camara editada = controlador.run(stage, camara);
                    if (editada != null){
                        listaCamaras.set(list.getSelectionModel().getSelectedIndex(),editada);
                    }
                });
                pingItem.setOnAction(e ->{
                    Function function = new Function(){
                        @Override
                        public void exec(){
                            Platform.runLater(() -> {
                                webEngine.executeScript(setMarkerIcon(camara.getId(),camara.getIconPath()));
                                visorCamaras.refresh();
                            });
                        }
                    };
                    PingThread pingThread = new PingThread(camara,function);
                    pingThread.start();
                });
            } else {
                contextMenu.hide();
                if (camara != null){
                    webEngine.executeScript(goToCamara(camara));
                }
            }
        });
        visorCamaras.setOnMousePressed(event -> {
                    if (event.getX() < visorCamaras.getPadding().getLeft()+10) {
                        visorCamaras.setCursor(Cursor.H_RESIZE);
                    }
                });
        toggleHide= (ToggleButton) visorCamaras.getParent().lookup("#toggleHide");
        cellHeight = visorCamaras.getHeight()/13;

        visorCamaras.setOnMouseDragged(event -> {
            if (visorCamaras.getCursor() == Cursor.H_RESIZE) {
                if (event.getX() <-25){
                    double x = event.getX();
                    visorCamaras.setPadding(new Insets(0,0,0,x));
                    inset = visorCamaras.getInsets();
                    buscadorCamaras.setPadding(inset);
                    toggleHide.setTranslateX(x);
                    GridPane.setMargin(recBuscador,inset);
                    recBuscador.setWidth(-x);
                }
            }
        });
        visorCamaras.setOnMouseReleased(event -> visorCamaras.setCursor(Cursor.DEFAULT));
        ImageView imageView = (ImageView) visorCamaras.getParent().lookup("#toggleHideImage");
        toggleHide.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue){
                    visorCamaras.setPadding(new Insets(0,0,0,0));
                    toggleHide.setTranslateX(-10);
                    buscadorCamaras.setPadding(new Insets(0,0,0,0));
                    GridPane.setMargin(recBuscador,new Insets(0,0,0,0));
                    recBuscador.setWidth(0);
                    File file =  new File("src/main/resources/iconos/left.png");
                    imageView.setImage(new Image(file.getAbsolutePath()));
                }else{
                    visorCamaras.setPadding(inset);
                    toggleHide.setTranslateX(inset.getLeft());
                    buscadorCamaras.setPadding(inset);
                    GridPane.setMargin(recBuscador,inset);
                    recBuscador.setWidth(-inset.getLeft());
                    File file =  new File("src/main/resources/iconos/right.png");
                    imageView.setImage(new Image(file.getAbsolutePath()));
                }
            }
        });
        listaFiltradaCamaras.addListener(new ListChangeListener<Camara>() {
            @Override
            public void onChanged(Change<? extends Camara> c) {
                if(listaFiltradaCamaras.size()<13){
                    if (listaFiltradaCamaras.size() == 0){
                        toggleHide.setTranslateY(-visorCamaras.getHeight()/2);
                    }else{
                        toggleHide.setTranslateY(-((13-listaFiltradaCamaras.size())*cellHeight)/2);
                    }
                }else{
                    toggleHide.setTranslateY(0);
                }
            }
        });
        if(listaFiltradaCamaras.size()<13){
            if (listaFiltradaCamaras.size() == 0){
                toggleHide.setTranslateY(-visorCamaras.getHeight()/2);
            }else{
                toggleHide.setTranslateY(-((13-listaFiltradaCamaras.size())*cellHeight)/2);
            }
        }else{
            toggleHide.setTranslateY(0);
        }
    }

    public Color cameraColor(Camara camara, Boolean highlight){
        String actividad = camara.getActividad();
        if (actividad.equals("NO OPERATIVA")){
            if (highlight){
                return new Color(1,0.25,0.25,0.9);
            }
            return new Color(0.9,0.5,0.5,0.85);
        }else if (actividad.equals("OPERATIVA")){
            if (highlight){
                return new Color(0.25,0.9,0.25,0.9);
            }
            return new Color(0.5,0.85,0.5,0.85);
        }else{
            if (highlight){
                return new Color(0.95,0.95,0.2,0.9);
            }
            return new Color(0.9,0.9,0.55,0.85);
        }
    }

    public void recolocarCamara(Camara camara){
        webEngine.executeScript(removeMarkerCenter(camara.getId()));
        webEngine.executeScript(addMarkerCenter(camara.getLatitud(),camara.getLongitud(),camara.getId(), camara.getIconPath()));
    }
    public void agregarNuevaCamara(String ip, String ubicacion, String localidad,
                              String latitud, String longitud, String tipo, String servidor, boolean cargar){
        Camara camara = new Camara(ip,ubicacion,localidad,latitud,longitud,tipo,servidor,markerCount);
        markerCount+= 1;
        listaCamaras.add(camara);
        if(cargar){
            webEngine.executeScript(addMarkerCenter(camara.getLatitud(),camara.getLongitud(),camara.getId(), camara.getIconPath()));
        }
    }

    public void agregarCamara(Camara camara, boolean cargar){
        if (listaCamaras.size()>200){
            return;
        }
        markerCount+= 1;
        listaCamaras.add(camara);
        if(cargar){
            webEngine.executeScript(addMarkerCenter(camara.getLatitud(),camara.getLongitud(),camara.getId(),camara.getIconPath()));
        }
    }

    public void removeCamera(Camara camara){
        webEngine.executeScript(removeMarkerCenter(camara.getId()));
        if (markerCount <0){
            markerCount = 0;
        }
        listaCamaras.remove(camara);
        remark.remove(String.valueOf(camara.getId()));
    }
    public String removeMarkerCenter(int id){
        return "map.panTo(marker"+id+".lonlat);"+
                "markers.removeMarker(marker"+id+");";
    }
    public String removeMarker(int id){
        return "markers.removeMarker(marker"+id+");";
    }
    public String reintegrarMarker(int id){
        return "markers.addMarker(marker"+id+");";
    }
    public String addMarker(String latitud, String longitud, int id, String text){
        return "var newLonLat = new OpenLayers.LonLat( "+ longitud + " ,"+ latitud +
                " ).transform(new OpenLayers.Projection(\"EPSG:4326\"), map.getProjectionObject());" +
                "var marker"+id+ " = new OpenLayers.Marker(newLonLat, new OpenLayers.Icon(\"file:///"+ text +", new OpenLayers.Size(32, 32)));"+
                "markers.addMarker(marker"+id+");"+
                "marker"+id+".events.register(\"click\", marker"+id+", function(e) {" +
                "map.panTo(marker"+id+".lonlat);"+
                "id = "+id+";" +
                "var lonlat = map.getLonLatFromViewPortPx(e.xy);"+
                "var lonlatWGS84 = lonlat.transform(map.getProjectionObject(), new OpenLayers.Projection(\"EPSG:4326\"));"+
                "element = lonlatWGS84;" +
                "});";
    }
    public String setMarkerIcon(int id, String text){
        return "marker"+id+".icon.setUrl(\"file:///" + text + ");";
    }

    public String addMarkerCenter(String latitud, String longitud,int id, String text){
        return addMarker(latitud,longitud,id, text) + "map.panTo(newLonLat);";
    }

    public String goToCamara(Camara camara){
        return "map.panTo(marker"+camara.getId()+".lonlat);";
    }
    public String readFile(String fileName) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
            }
        }
        return sb.toString();
    }
    public void cargarCamarasDesdeCSV(String rutaArchivo, boolean cargar) {
        if (cargar){
            listaCamaras.addListener(new ListChangeListener<Camara>() {
                @Override
                public void onChanged(Change<? extends Camara> c) {

                }
            });
        }
        CSVManager csvManager = new CSVManager();
        List<Camara> camaras = csvManager.cargarCamarasDesdeCSV(rutaArchivo);
        for (Camara camara: camaras){
            camara.setId(markerCount);
            agregarCamara(camara,cargar);
        }
        if (cargar){
            csvManager.guardarCSV(new File(rutaArchivo), listaCamaras);
            listaCamaras.addListener(new ListChangeListener<Camara>() {
                @Override
                public void onChanged(Change<? extends Camara> c) {
                    csvManager.guardarCSV(new File("src/main/java/com/example/camarasrama/Camaras.csv"), listaCamaras);
                }
            });
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
