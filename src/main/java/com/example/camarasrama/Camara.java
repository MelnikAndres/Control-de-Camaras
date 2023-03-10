package com.example.camarasrama;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.File;

public class Camara {
    private String ip;
    private String ubicacion,localidad;
    private String latitud,longitud;
    private String tipo;
    private String servidor;
    private int id;
    private String actividad;
    private int perdida;
    private String zcomentario;
    @JsonIgnore
    private String iconPath;

    @JsonCreator
    public Camara(@JsonProperty("actividad")String actividad,
                  @JsonProperty("id")int id,
                  @JsonProperty("ip")String ip,
                  @JsonProperty("latitud")String latitud,
                  @JsonProperty("localidad")String localidad,
                  @JsonProperty("longitud")String longitud,
                  @JsonProperty("perdida")int perdida,
                  @JsonProperty("servidor")String servidor,
                  @JsonProperty("tipo")String tipo,
                  @JsonProperty("ubicacion")String ubicacion,
                  @JsonProperty("zcomentario")String zcomentario){
        this.ip = ip;
        this.ubicacion = ubicacion;
        this.localidad = localidad;
        this.latitud = latitud;
        this.longitud = longitud;
        this.tipo = tipo;
        this.servidor = servidor;
        this.id = 0;
        this.actividad = actividad;
        this.perdida = perdida;
        this.zcomentario = zcomentario;
        setIconPath();
    }

    public Camara(String ip, String ubicacion, String localidad,
                  String latitud, String longitud, String tipo,
                  String servidor, int id){
        this.ip = ip;
        this.ubicacion = ubicacion;
        this.localidad = localidad;
        this.latitud = latitud;
        this.longitud = longitud;
        this.tipo = tipo;
        this.servidor = servidor;
        this.id = id;
        this.actividad = "NO OPERATIVA";
        this.perdida = 0;
        this.zcomentario = "";
        setIconPath();
    }

    public String getIp() {
        return ip;
    }

    public String getLatitud() {
        return latitud;
    }

    public String getLocalidad() {
        return localidad;
    }

    public String getLongitud() {
        return longitud;
    }

    public String getServidor() {
        return servidor;
    }

    public String getTipo() {
        return tipo;
    }

    public String getUbicacion() {
        return ubicacion;
    }
    public int getId() {
        return id;
    }

    public String getActividad() {
        return actividad;
    }
    public int getPerdida(){
        return perdida;
    }

    public String getZcomentario() {
        return zcomentario;
    }

    public String getIconPath() {
        return iconPath;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setLatitud(String latitud) {
        this.latitud = latitud;
    }

    public void setLocalidad(String localidad) {
        this.localidad = localidad;
    }

    public void setLongitud(String longitud) {
        this.longitud = longitud;
    }

    public void setServidor(String servidor) {
        this.servidor = servidor;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setActividad(String actividad) {
        this.actividad = actividad;
        setIconPath();
    }

    public void setZcomentario(String zcomentario) {
        this.zcomentario = zcomentario;
    }

    public void setIconPath() {
        String actividadPath = actividad;
        if(actividad.equals("INESTABLE ")){
            actividadPath = "INESTABLE";
        }
        File file = new File("src/main/resources/iconos/"+getTipo()+actividadPath+".png");
        String text = file.getAbsolutePath() + "\"";
        text = text.replace("\\","/");
        this.iconPath = text;
    }

    public String errores() {
        String problemas = "";
        int cantidad = 0;
        if (this.ip.equals("")){
            problemas += "ERROR IP ";
            cantidad +=1;
        }
        if (this.ubicacion.equals("")){
            problemas += "ERROR UBICACION ";
            cantidad +=1;
        }
        if (this.localidad.equals("") || this.localidad.equals("Seleccionar Localidad")){
            problemas += "ERROR LOCALIDAD";
            cantidad += 1;
        }
        if (cantidad == 3){
            problemas += "\n";
            cantidad = 0;
        }
        if (this.latitud.equals("")){
            problemas += "ERROR LATITUD ";
            cantidad +=1;
        }else{
            try {
                Double.parseDouble(this.latitud);
            } catch (NumberFormatException e) {
                problemas += "ERROR LATITUD";
                cantidad +=1;
            }
        }
        if (cantidad == 3){
            problemas += "\n";
            cantidad = 0;
        }
        if (this.longitud.equals("")){
            problemas += "ERROR LONGITUD ";
            cantidad += 1;
        }else{
            try {
                Double.parseDouble(this.longitud);
            } catch (NumberFormatException e) {
                problemas += "ERROR LONGITUD";
                cantidad +=1;
            }
        }
        if (cantidad == 3){
            problemas += "\n";
            cantidad = 0;
        }
        if (this.tipo.equals("")|| this.tipo.equals("Seleccionar Tipo")){
            problemas += "ERROR TIPO ";
            cantidad += 1;
        }
        if (cantidad == 3){
            problemas += "\n";
            cantidad = 0;
        }
        if (this.servidor.equals("") || this.servidor.equals("Seleccionar Servidor")){
            problemas += "ERROR SERVIDOR ";
            cantidad += 1;
        }
        if (cantidad == 3){
            problemas += "\n";
            cantidad = 0;
        }
        if (problemas.equals("")){
            return null;
        }
        return problemas;
    }

    public void setPerdida(int perdida) {
        this.perdida = perdida;
    }
}
