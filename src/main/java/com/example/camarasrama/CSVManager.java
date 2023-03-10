package com.example.camarasrama;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import javafx.collections.ObservableList;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CSVManager {
    public List<Camara> cargarCamarasDesdeCSV(String rutaArchivo) {
        CsvMapper mapper = new CsvMapper();
        CsvSchema schema = mapper.schemaFor(Camara.class).withHeader();
        ObjectReader objectReader = mapper.readerFor(Camara.class).with(schema);
        try {
            MappingIterator<Camara> objectMappingIterator = objectReader.readValues(new File(rutaArchivo));
            return objectMappingIterator.readAll();
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }
    public void guardarCSV(File file, ObservableList<Camara> listaCamaras) {
        try {
            CsvMapper mapper = new CsvMapper();
            CsvSchema schema = mapper.schemaFor(Camara.class).withHeader();
            ObjectWriter writer = mapper.writer(schema);
            writer.writeValue(file, listaCamaras);

        } catch (IOException e) {
            System.out.println("error de guardado");
            e.printStackTrace();
        }
    }
}
