package com.example.farmaventa.modelo;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import java.time.LocalDate;


public class HistoricoReclamacion {

    private final SimpleIntegerProperty             idHistorico        = new SimpleIntegerProperty();
    private final SimpleStringProperty              descripcion        = new SimpleStringProperty();
    private final SimpleStringProperty              creadoPor          = new SimpleStringProperty(); // creado_por
    private final SimpleObjectProperty<LocalDate>   fechaCreacion      = new SimpleObjectProperty<>();
    private final SimpleIntegerProperty             idReclamacionventa = new SimpleIntegerProperty();

    public HistoricoReclamacion() {}

    public HistoricoReclamacion(int idHistorico, String descripcion, String creadoPor,
                                LocalDate fechaCreacion, int idReclamacionventa) {
        this.idHistorico.set(idHistorico);
        this.descripcion.set(descripcion != null ? descripcion : "");
        this.creadoPor.set(creadoPor != null ? creadoPor : "");
        this.fechaCreacion.set(fechaCreacion);
        this.idReclamacionventa.set(idReclamacionventa);
    }

    public String obtenerDetalleCambio() {
        return "ID Histórico  : " + idHistorico.get()              + "\n" +
                "Fecha         : " + (fechaCreacion.get() != null
                ? fechaCreacion.get() : "—")  + "\n" +

                "Creado por    : " + (creadoPor.get() != null
                ? creadoPor.get() : "—")      + "\n" +
                "Reclamación   : #" + idReclamacionventa.get()       + "\n\n" +
                "Descripción   :\n" + (descripcion.get() != null
                ? descripcion.get()
                : "Sin descripción.");
    }

    public SimpleIntegerProperty           idHistoricoProperty()        { return idHistorico; }
    public SimpleStringProperty            descripcionProperty()        { return descripcion; }
    public SimpleStringProperty            creadoPorProperty()          { return creadoPor; }
    public SimpleObjectProperty<LocalDate> fechaCreacionProperty()      { return fechaCreacion; }
    public SimpleIntegerProperty           idReclamacionventaProperty() { return idReclamacionventa; }

    public int       getIdHistorico()                   { return idHistorico.get(); }
    public void      setIdHistorico(int v)              { idHistorico.set(v); }
    public String    getDescripcion()                   { return descripcion.get(); }
    public void      setDescripcion(String v)           { descripcion.set(v); }
    public String    getCreadoPor()                     { return creadoPor.get(); }
    public void      setCreadoPor(String v)             { creadoPor.set(v); }
    public LocalDate getFechaCreacion()                 { return fechaCreacion.get(); }
    public void      setFechaCreacion(LocalDate v)      { fechaCreacion.set(v); }
    public int       getIdReclamacionventa()            { return idReclamacionventa.get(); }
    public void      setIdReclamacionventa(int v)       { idReclamacionventa.set(v); }
}