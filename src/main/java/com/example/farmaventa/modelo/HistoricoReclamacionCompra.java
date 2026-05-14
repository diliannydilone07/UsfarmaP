package com.example.farmaventa.modelo;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import java.time.LocalDate;


public class HistoricoReclamacionCompra {

    private final SimpleIntegerProperty           idHistorico          = new SimpleIntegerProperty();
    private final SimpleStringProperty            descripcion          = new SimpleStringProperty();
    private final SimpleStringProperty            creadoPor            = new SimpleStringProperty();
    private final SimpleObjectProperty<LocalDate> fechaCreacion        = new SimpleObjectProperty<>();
    private final SimpleIntegerProperty           idReclamacioncompra  = new SimpleIntegerProperty();

    public HistoricoReclamacionCompra() {}

    public HistoricoReclamacionCompra(int idHistorico, String descripcion, String creadoPor,
                                      LocalDate fechaCreacion, int idReclamacioncompra) {
        this.idHistorico.set(idHistorico);
        this.descripcion.set(descripcion != null ? descripcion : "");
        this.creadoPor.set(creadoPor != null ? creadoPor : "");
        this.fechaCreacion.set(fechaCreacion);
        this.idReclamacioncompra.set(idReclamacioncompra);
    }

    public String obtenerDetalleCambio() {
        return "ID Histórico  : " + idHistorico.get() + "\n"
                + "Descripción   : " + descripcion.get() + "\n"
                + "Creado por    : " + creadoPor.get() + "\n"
                + "Fecha         : " + fechaCreacion.get() + "\n"
                + "ID Reclamación: " + idReclamacioncompra.get();
    }

    public SimpleIntegerProperty           idHistoricoProperty()         { return idHistorico; }
    public SimpleStringProperty            descripcionProperty()         { return descripcion; }
    public SimpleStringProperty            creadoPorProperty()           { return creadoPor; }
    public SimpleObjectProperty<LocalDate> fechaCreacionProperty()       { return fechaCreacion; }
    public SimpleIntegerProperty           idReclamacioncompraProperty() { return idReclamacioncompra; }

    public int       getIdHistorico()                    { return idHistorico.get(); }
    public void      setIdHistorico(int v)               { idHistorico.set(v); }
    public String    getDescripcion()                    { return descripcion.get(); }
    public void      setDescripcion(String v)            { descripcion.set(v); }
    public String    getCreadoPor()                      { return creadoPor.get(); }
    public void      setCreadoPor(String v)              { creadoPor.set(v); }
    public LocalDate getFechaCreacion()                  { return fechaCreacion.get(); }
    public void      setFechaCreacion(LocalDate v)       { fechaCreacion.set(v); }
    public int       getIdReclamacioncompra()            { return idReclamacioncompra.get(); }
    public void      setIdReclamacioncompra(int v)       { idReclamacioncompra.set(v); }
}