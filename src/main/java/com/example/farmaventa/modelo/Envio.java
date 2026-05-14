package com.example.farmaventa.modelo;

import javafx.beans.property.*;


public class Envio {

    private final SimpleIntegerProperty idEnvio        = new SimpleIntegerProperty();
    private final SimpleStringProperty  fechaEnvio     = new SimpleStringProperty();
    private final SimpleDoubleProperty  costoServicio  = new SimpleDoubleProperty();
    private final SimpleStringProperty  personaRecibe  = new SimpleStringProperty();
    private final SimpleStringProperty  metodoEnvio    = new SimpleStringProperty();
    private final SimpleIntegerProperty idVenta        = new SimpleIntegerProperty();
    private final SimpleStringProperty  infoVenta      = new SimpleStringProperty(); // JOIN: cliente + fecha
    private final SimpleStringProperty  estadoEnvio    = new SimpleStringProperty(); // del patrón State

    public Envio() {}

    public Envio(int idEnvio, String fechaEnvio, double costoServicio,
                 String personaRecibe, String metodoEnvio,
                 int idVenta, String infoVenta, String estadoEnvio) {
        this.idEnvio.set(idEnvio);
        this.fechaEnvio.set(fechaEnvio     != null ? fechaEnvio    : "");
        this.costoServicio.set(costoServicio);
        this.personaRecibe.set(personaRecibe != null ? personaRecibe : "");
        this.metodoEnvio.set(metodoEnvio   != null ? metodoEnvio   : "");
        this.idVenta.set(idVenta);
        this.infoVenta.set(infoVenta       != null ? infoVenta     : "");
        this.estadoEnvio.set(estadoEnvio   != null ? estadoEnvio   : "EN_PREPARACION");
    }

    public void programarEnvio()   { estadoEnvio.set("EN_PREPARACION"); }
    public void despachar()        { estadoEnvio.set("EN_CAMINO"); }
    public void entregar()         { estadoEnvio.set("ENTREGADO"); }
    public void cancelar()         { estadoEnvio.set("CANCELADO"); }

    public double calcularCostoServicio() { return costoServicio.get(); }

    public String asignarRepartidor(String nombreEmpleado) {
        return "Repartidor asignado: " + nombreEmpleado;
    }

    public SimpleIntegerProperty idEnvioProperty()       { return idEnvio; }
    public SimpleStringProperty  fechaEnvioProperty()    { return fechaEnvio; }
    public SimpleDoubleProperty  costoServicioProperty() { return costoServicio; }
    public SimpleStringProperty  personaRecibeProperty() { return personaRecibe; }
    public SimpleStringProperty  metodoEnvioProperty()   { return metodoEnvio; }
    public SimpleIntegerProperty idVentaProperty()       { return idVenta; }
    public SimpleStringProperty  infoVentaProperty()     { return infoVenta; }
    public SimpleStringProperty  estadoEnvioProperty()   { return estadoEnvio; }

    public int    getIdEnvio()              { return idEnvio.get(); }
    public void   setIdEnvio(int v)         { idEnvio.set(v); }
    public String getFechaEnvio()           { return fechaEnvio.get(); }
    public void   setFechaEnvio(String v)   { fechaEnvio.set(v); }
    public double getCostoServicio()        { return costoServicio.get(); }
    public void   setCostoServicio(double v){ costoServicio.set(v); }
    public String getPersonaRecibe()        { return personaRecibe.get(); }
    public void   setPersonaRecibe(String v){ personaRecibe.set(v); }
    public String getMetodoEnvio()          { return metodoEnvio.get(); }
    public void   setMetodoEnvio(String v)  { metodoEnvio.set(v); }
    public int    getIdVenta()              { return idVenta.get(); }
    public void   setIdVenta(int v)         { idVenta.set(v); }
    public String getInfoVenta()            { return infoVenta.get(); }
    public void   setInfoVenta(String v)    { infoVenta.set(v); }
    public String getEstadoEnvio()          { return estadoEnvio.get(); }
    public void   setEstadoEnvio(String v)  { estadoEnvio.set(v); }
}