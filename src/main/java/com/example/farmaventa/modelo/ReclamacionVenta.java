package com.example.farmaventa.modelo;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import java.time.LocalDate;

/**
 * Mapea TBL_RECLAMACION_VENTA + JOIN TBL_VENTA + TBL_CLIENTE + TBL_PERSONA
 * Columnas reales:
 *   id_reclamacionventa, fecha_reclamacion, estado (BIT), id_venta
 *   + nombre del cliente (JOIN)
 *   + descripcion y cantidad (de TBL_PRODUCTO_RECLAMACION_VENTA, primer producto)
 */
public class ReclamacionVenta {

    private final SimpleIntegerProperty             idReclamacionventa = new SimpleIntegerProperty();
    private final SimpleIntegerProperty             idVenta            = new SimpleIntegerProperty();
    private final SimpleStringProperty              nombreCliente      = new SimpleStringProperty();
    private final SimpleObjectProperty<LocalDate>   fechaReclamacion   = new SimpleObjectProperty<>();
    // estado BIT: 0 = Pendiente, 1 = Resuelta — se muestra como texto
    private final SimpleStringProperty              estadoActualNombre = new SimpleStringProperty();
    private final SimpleIntegerProperty             cantidadAdevolver  = new SimpleIntegerProperty();
    private final SimpleStringProperty              descripcion        = new SimpleStringProperty();

    public ReclamacionVenta() {}

    public ReclamacionVenta(int idReclamacionventa, int idVenta, String nombreCliente,
                            LocalDate fechaReclamacion, String estadoActualNombre,
                            int cantidadAdevolver, String descripcion) {
        this.idReclamacionventa.set(idReclamacionventa);
        this.idVenta.set(idVenta);
        this.nombreCliente.set(nombreCliente != null ? nombreCliente : "");
        this.fechaReclamacion.set(fechaReclamacion);
        this.estadoActualNombre.set(estadoActualNombre != null ? estadoActualNombre : "ESTADO_RECLAMACION_PENDIENTE");
        this.cantidadAdevolver.set(cantidadAdevolver);
        this.descripcion.set(descripcion != null ? descripcion : "");
    }

    public SimpleIntegerProperty           idReclamacionventaProperty() { return idReclamacionventa; }
    public SimpleIntegerProperty           idVentaProperty()            { return idVenta; }
    public SimpleStringProperty            nombreClienteProperty()      { return nombreCliente; }
    public SimpleObjectProperty<LocalDate> fechaReclamacionProperty()   { return fechaReclamacion; }
    public SimpleStringProperty            estadoActualNombreProperty() { return estadoActualNombre; }
    public SimpleIntegerProperty           cantidadADevolverProperty()  { return cantidadAdevolver; }
    public SimpleStringProperty            descripcionProperty()        { return descripcion; }

    public int       getIdReclamacionventa()               { return idReclamacionventa.get(); }
    public void      setIdReclamacionventa(int v)          { idReclamacionventa.set(v); }
    public int       getIdVenta()                          { return idVenta.get(); }
    public void      setIdVenta(int v)                     { idVenta.set(v); }
    public String    getNombreCliente()                    { return nombreCliente.get(); }
    public void      setNombreCliente(String v)            { nombreCliente.set(v); }
    public LocalDate getFechaReclamacion()                 { return fechaReclamacion.get(); }
    public void      setFechaReclamacion(LocalDate v)      { fechaReclamacion.set(v); }
    public String    getEstadoActualNombre()               { return estadoActualNombre.get(); }
    public void      setEstadoActualNombre(String v)       { estadoActualNombre.set(v); }
    public int       getCantidadAdevolver()                { return cantidadAdevolver.get(); }
    public void      setCantidadAdevolver(int v)           { cantidadAdevolver.set(v); }
    public String    getDescripcion()                      { return descripcion.get(); }
    public void      setDescripcion(String v)              { descripcion.set(v); }
}