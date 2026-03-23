package com.example.farmaventa.modelo;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import java.time.LocalDate;

public class DevolucionCompra {

    private final SimpleIntegerProperty           idDevolucioncompra  = new SimpleIntegerProperty();
    private final SimpleIntegerProperty           idCompra            = new SimpleIntegerProperty();
    private final SimpleIntegerProperty           idReclamacioncompra = new SimpleIntegerProperty();
    private final SimpleStringProperty            nombreProveedor     = new SimpleStringProperty();
    private final SimpleObjectProperty<LocalDate> fechaDevolucion     = new SimpleObjectProperty<>();
    private final SimpleStringProperty            estadoNombre        = new SimpleStringProperty();
    private final SimpleStringProperty            montoTotal          = new SimpleStringProperty();

    public DevolucionCompra() {}

    public DevolucionCompra(int idDevolucioncompra, int idCompra, String nombreProveedor,
                            LocalDate fechaDevolucion, String estadoNombre, String montoTotal) {
        this.idDevolucioncompra.set(idDevolucioncompra);
        this.idCompra.set(idCompra);
        this.nombreProveedor.set(nombreProveedor != null ? nombreProveedor : "");
        this.fechaDevolucion.set(fechaDevolucion);
        this.estadoNombre.set(estadoNombre != null ? estadoNombre : "PENDIENTE");
        this.montoTotal.set(montoTotal != null ? montoTotal : "0.00");
    }

    // ── Properties ────────────────────────────────────────────────────────────
    public SimpleIntegerProperty           idDevolucioncompraProperty()  { return idDevolucioncompra; }
    public SimpleIntegerProperty           idCompraProperty()            { return idCompra; }
    public SimpleIntegerProperty           idReclamacioncompraProperty() { return idReclamacioncompra; }
    public SimpleStringProperty            nombreProveedorProperty()     { return nombreProveedor; }
    public SimpleObjectProperty<LocalDate> fechaDevolucionProperty()     { return fechaDevolucion; }
    public SimpleStringProperty            estadoNombreProperty()        { return estadoNombre; }
    public SimpleStringProperty            montoTotalProperty()          { return montoTotal; }

    // ── Getters / Setters ─────────────────────────────────────────────────────
    public int       getIdDevolucioncompra()            { return idDevolucioncompra.get(); }
    public void      setIdDevolucioncompra(int v)       { idDevolucioncompra.set(v); }
    public int       getIdCompra()                      { return idCompra.get(); }
    public void      setIdCompra(int v)                 { idCompra.set(v); }
    public int       getIdReclamacioncompra()           { return idReclamacioncompra.get(); }
    public void      setIdReclamacioncompra(int v)      { idReclamacioncompra.set(v); }
    public String    getNombreProveedor()               { return nombreProveedor.get(); }
    public void      setNombreProveedor(String v)       { nombreProveedor.set(v); }
    public LocalDate getFechaDevolucion()               { return fechaDevolucion.get(); }
    public void      setFechaDevolucion(LocalDate v)    { fechaDevolucion.set(v); }
    public String    getEstadoNombre()                  { return estadoNombre.get(); }
    public void      setEstadoNombre(String v)          { estadoNombre.set(v); }
    public String    getMontoTotal()                    { return montoTotal.get(); }
    public void      setMontoTotal(String v)            { montoTotal.set(v); }
}