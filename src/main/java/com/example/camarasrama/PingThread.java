package com.example.camarasrama;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.function.Supplier;

public class PingThread extends Thread {
    private Camara camara;
    private Function refresh;
    public PingThread(Camara camara, Function refresh) {
        this.camara = camara;
        this.refresh = refresh;
    }

    public void run() {
        int pingsTotales = 5;
        try {
            InetAddress addr = InetAddress.getByName(camara.getIp());
            int pingsEfectivos = 0;
            for (int i = 0; i<pingsTotales;i++){
                if (addr.isReachable(1000)){
                    pingsEfectivos += 1;
                }
            }
            int perdidaDePaquetes = ((pingsTotales - pingsEfectivos) / pingsTotales) * 100;
            if (pingsEfectivos == 0){
                camara.setActividad("NO OPERATIVA");
            }else{
                if (perdidaDePaquetes != 0){
                    camara.setActividad("INESTABLE ");
                }else{
                    camara.setActividad("OPERATIVA");
                }
            }
            camara.setPerdida(perdidaDePaquetes);
        } catch (IOException e) {
            System.out.println("error en pings");
            e.printStackTrace();
        }
        refresh.exec();
    }
}
