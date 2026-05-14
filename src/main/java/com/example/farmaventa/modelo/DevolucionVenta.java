package com.example.farmaventa.modelo;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import java.time.LocalDate;

public class DevolucionVenta {

    private final SimpleIntegerProperty           idDevolucionventa  = new SimpleIntegerProperty();
    private final SimpleIntegerProperty           idVenta            = new SimpleIntegerProperty();
    private final SimpleIntegerProperty           idReclamacionventa = new SimpleIntegerProperty();
    private final SimpleStringProperty            nombreCliente      = new SimpleStringProperty();
    private final SimpleObjectProperty<LocalDate> fechaDevolucion    = new SimpleObjectProperty<>();
    private final SimpleStringProperty            razon              = new SimpleStringProperty();
    private final SimpleStringProperty            estadoNombre       = new SimpleStringProperty();
    private final SimpleStringProperty            montoTotal         = new SimpleStringProperty();

    public DevolucionVenta() {}

    public DevolucionVenta(int idDevolucionventa, int idVenta, String nombreCliente,
                           LocalDate fechaDevolucion, String razon,
                           String estadoNombre, String montoTotal) {
        this.idDevolucionventa.set(idDevolucionventa);
        this.idVenta.set(idVenta);
        this.nombreCliente.set(nombreCliente != null ? nombreCliente : "");
        this.fechaDevolucion.set(fechaDevolucion);
        this.razon.set(razon != null ? razon : "");
        this.estadoNombre.set(estadoNombre != null ? estadoNombre : "PENDIENTE");
        this.montoTotal.set(montoTotal != null ? montoTotal : "0.00");
    }

    public SimpleIntegerProperty           idDevolucionventaProperty()  { return idDevolucionventa; }
    public SimpleIntegerProperty           idVentaProperty()            { return idVenta; }
    public SimpleIntegerProperty           idReclamacionventaProperty() { return idReclamacionventa; }
    public SimpleStringProperty            nombreClienteProperty()      { return nombreCliente; }
    public SimpleObjectProperty<LocalDate> fechaDevolucionProperty()    { return fechaDevolucion; }
    public SimpleStringProperty            razonProperty()              { return razon; }
    public SimpleStringProperty            estadoNombreProperty()       { return estadoNombre; }
    public SimpleStringProperty            montoTotalProperty()         { return montoTotal; }

    public int       getIdDevolucionventa()            { return idDevolucionventa.get(); }
    public void      setIdDevolucionventa(int v)       { idDevolucionventa.set(v); }
    public int       getIdVenta()                      { return idVenta.get(); }
    public void      setIdVenta(int v)                 { idVenta.set(v); }
    public int       getIdReclamacionventa()           { return idReclamacionventa.get(); }
    public void      setIdReclamacionventa(int v)      { idReclamacionventa.set(v); }
    public String    getNombreCliente()                { return nombreCliente.get(); }
    public void      setNombreCliente(String v)        { nombreCliente.set(v); }
    public LocalDate getFechaDevolucion()              { return fechaDevolucion.get(); }
    public void      setFechaDevolucion(LocalDate v)   { fechaDevolucion.set(v); }
    public String    getRazon()                        { return razon.get(); }
    public void      setRazon(String v)                { razon.set(v); }
    public String    getEstadoNombre()                 { return estadoNombre.get(); }
    public void      setEstadoNombre(String v)         { estadoNombre.set(v); }
    public String    getMontoTotal()                   { return montoTotal.get(); }
    public void      setMontoTotal(String v)           { montoTotal.set(v); }
}