package com.example.farmaventa.modelo;

import javafx.beans.property.*;


public class PresentacionDetalle {

    private final SimpleIntegerProperty idPresentacion     = new SimpleIntegerProperty();
    private final SimpleStringProperty  nombrePresentacion = new SimpleStringProperty();
    private final SimpleDoubleProperty  precio             = new SimpleDoubleProperty();
    private final SimpleStringProperty  fechaCaducidad     = new SimpleStringProperty(); // "yyyy-MM-dd"

    public PresentacionDetalle() {}

    public PresentacionDetalle(int idPresentacion, String nombrePresentacion,
                               double precio, String fechaCaducidad) {
        this.idPresentacion.set(idPresentacion);
        this.nombrePresentacion.set(nombrePresentacion != null ? nombrePresentacion : "");
        this.precio.set(precio);
        this.fechaCaducidad.set(fechaCaducidad != null ? fechaCaducidad : "");
    }

    public SimpleIntegerProperty idPresentacionProperty()     { return idPresentacion; }
    public SimpleStringProperty  nombrePresentacionProperty() { return nombrePresentacion; }
    public SimpleDoubleProperty  precioProperty()             { return precio; }
    public SimpleStringProperty  fechaCaducidadProperty()     { return fechaCaducidad; }

    public int    getIdPresentacion()                { return idPresentacion.get(); }
    public void   setIdPresentacion(int v)           { idPresentacion.set(v); }

    public String getNombrePresentacion()            { return nombrePresentacion.get(); }
    public void   setNombrePresentacion(String v)    { nombrePresentacion.set(v); }

    public double getPrecio()                        { return precio.get(); }
    public void   setPrecio(double v)                { precio.set(v); }

    public String getFechaCaducidad()                { return fechaCaducidad.get(); }
    public void   setFechaCaducidad(String v)        { fechaCaducidad.set(v); }
}