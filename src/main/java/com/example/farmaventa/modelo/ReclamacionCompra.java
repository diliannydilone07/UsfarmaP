package com.example.farmaventa.modelo;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import java.time.LocalDate;


public class ReclamacionCompra {

    private final SimpleIntegerProperty           idReclamacioncompra  = new SimpleIntegerProperty();
    private final SimpleIntegerProperty           idCompra             = new SimpleIntegerProperty();
    private final SimpleStringProperty            nombreProveedor      = new SimpleStringProperty();
    private final SimpleObjectProperty<LocalDate> fechaReclamacion     = new SimpleObjectProperty<>();
    private final SimpleStringProperty            estadoActualNombre   = new SimpleStringProperty();
    private final SimpleIntegerProperty           cantidadAdevolver    = new SimpleIntegerProperty();
    private final SimpleStringProperty            descripcion          = new SimpleStringProperty();

    public ReclamacionCompra() {}

    public ReclamacionCompra(int idReclamacioncompra, int idCompra, String nombreProveedor,
                             LocalDate fechaReclamacion, String estadoActualNombre,
                             int cantidadAdevolver, String descripcion) {
        this.idReclamacioncompra.set(idReclamacioncompra);
        this.idCompra.set(idCompra);
        this.nombreProveedor.set(nombreProveedor != null ? nombreProveedor : "");
        this.fechaReclamacion.set(fechaReclamacion);
        this.estadoActualNombre.set(estadoActualNombre != null ? estadoActualNombre : "PENDIENTE");
        this.cantidadAdevolver.set(cantidadAdevolver);
        this.descripcion.set(descripcion != null ? descripcion : "");
    }

    public SimpleIntegerProperty           idReclamacioncompraProperty()  { return idReclamacioncompra; }
    public SimpleIntegerProperty           idCompraProperty()             { return idCompra; }
    public SimpleStringProperty            nombreProveedorProperty()      { return nombreProveedor; }
    public SimpleObjectProperty<LocalDate> fechaReclamacionProperty()     { return fechaReclamacion; }
    public SimpleStringProperty            estadoActualNombreProperty()   { return estadoActualNombre; }
    public SimpleIntegerProperty           cantidadAdevolverProperty()    { return cantidadAdevolver; }
    public SimpleStringProperty            descripcionProperty()          { return descripcion; }

    public int       getIdReclamacioncompra()              { return idReclamacioncompra.get(); }
    public void      setIdReclamacioncompra(int v)         { idReclamacioncompra.set(v); }
    public int       getIdCompra()                         { return idCompra.get(); }
    public void      setIdCompra(int v)                    { idCompra.set(v); }
    public String    getNombreProveedor()                  { return nombreProveedor.get(); }
    public void      setNombreProveedor(String v)          { nombreProveedor.set(v); }
    public LocalDate getFechaReclamacion()                 { return fechaReclamacion.get(); }
    public void      setFechaReclamacion(LocalDate v)      { fechaReclamacion.set(v); }
    public String    getEstadoActualNombre()               { return estadoActualNombre.get(); }
    public void      setEstadoActualNombre(String v)       { estadoActualNombre.set(v); }
    public int       getCantidadAdevolver()                { return cantidadAdevolver.get(); }
    public void      setCantidadAdevolver(int v)           { cantidadAdevolver.set(v); }
    public String    getDescripcion()                      { return descripcion.get(); }
    public void      setDescripcion(String v)              { descripcion.set(v); }
}