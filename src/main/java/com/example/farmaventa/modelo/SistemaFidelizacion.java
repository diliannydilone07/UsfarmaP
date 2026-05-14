package com.example.farmaventa.modelo;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import java.time.LocalDate;


public class SistemaFidelizacion {

    private final SimpleIntegerProperty           idFidelizacion    = new SimpleIntegerProperty();
    private final SimpleIntegerProperty           idCliente         = new SimpleIntegerProperty();
    private final SimpleStringProperty            nombreCliente     = new SimpleStringProperty();
    private final SimpleIntegerProperty           puntosAcumulados  = new SimpleIntegerProperty();
    private final SimpleObjectProperty<LocalDate> fechaCaducidad    = new SimpleObjectProperty<>();

    private final SimpleStringProperty            estadoPuntos      = new SimpleStringProperty();

    public SistemaFidelizacion() {}

    public SistemaFidelizacion(int idFidelizacion, int idCliente, String nombreCliente,
                               int puntosAcumulados, LocalDate fechaCaducidad) {
        this.idFidelizacion.set(idFidelizacion);
        this.idCliente.set(idCliente);
        this.nombreCliente.set(nombreCliente != null ? nombreCliente : "");
        this.puntosAcumulados.set(puntosAcumulados);
        this.fechaCaducidad.set(fechaCaducidad);

        this.estadoPuntos.set(
                fechaCaducidad != null && !fechaCaducidad.isBefore(LocalDate.now())
                        ? "ACTIVO" : "VENCIDO"
        );
    }

    public SimpleIntegerProperty           idFidelizacionProperty()   { return idFidelizacion; }
    public SimpleIntegerProperty           idClienteProperty()        { return idCliente; }
    public SimpleStringProperty            nombreClienteProperty()    { return nombreCliente; }
    public SimpleIntegerProperty           puntosAcumuladosProperty() { return puntosAcumulados; }
    public SimpleObjectProperty<LocalDate> fechaCaducidadProperty()   { return fechaCaducidad; }
    public SimpleStringProperty            estadoPuntosProperty()     { return estadoPuntos; }

    public int       getIdFidelizacion()              { return idFidelizacion.get(); }
    public void      setIdFidelizacion(int v)         { idFidelizacion.set(v); }
    public int       getIdCliente()                   { return idCliente.get(); }
    public void      setIdCliente(int v)              { idCliente.set(v); }
    public String    getNombreCliente()               { return nombreCliente.get(); }
    public void      setNombreCliente(String v)       { nombreCliente.set(v); }
    public int       getPuntosAcumulados()            { return puntosAcumulados.get(); }
    public void      setPuntosAcumulados(int v)       { puntosAcumulados.set(v); }
    public LocalDate getFechaCaducidad()              { return fechaCaducidad.get(); }
    public void      setFechaCaducidad(LocalDate v)   { fechaCaducidad.set(v); }
    public String    getEstadoPuntos()                { return estadoPuntos.get(); }
    public void      setEstadoPuntos(String v)        { estadoPuntos.set(v); }
}