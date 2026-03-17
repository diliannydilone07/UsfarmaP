package com.example.farmaventa.modelo;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * Mapea TBL_VENTA + JOIN TBL_CLIENTE + TBL_PERSONA para el nombre del cliente
 * Columnas reales:
 *   id_venta, id_empleado, tipo_venta, fecha_transaccion,
 *   monto_total, monto_pendiente, condicion, id_cliente
 */
public class Venta {

    private final SimpleIntegerProperty idVenta        = new SimpleIntegerProperty();
    private final SimpleIntegerProperty idCliente      = new SimpleIntegerProperty();
    private final SimpleIntegerProperty idEmpleado     = new SimpleIntegerProperty();
    private final SimpleStringProperty  tipoVenta      = new SimpleStringProperty();
    private final SimpleStringProperty  fechaVenta     = new SimpleStringProperty(); // fecha_transaccion
    private final SimpleStringProperty  condicion      = new SimpleStringProperty();
    private final SimpleDoubleProperty  montoTotal     = new SimpleDoubleProperty();
    private final SimpleDoubleProperty  montoPendiente = new SimpleDoubleProperty();
    private final SimpleStringProperty  nombreCliente  = new SimpleStringProperty(); // JOIN

    public Venta() {}

    public Venta(int idVenta, int idCliente, int idEmpleado, String tipoVenta,
                 String fechaVenta, String condicion,
                 double montoTotal, double montoPendiente, String nombreCliente) {
        this.idVenta.set(idVenta);
        this.idCliente.set(idCliente);
        this.idEmpleado.set(idEmpleado);
        this.tipoVenta.set(tipoVenta != null ? tipoVenta : "");
        this.fechaVenta.set(fechaVenta != null ? fechaVenta : "");
        this.condicion.set(condicion != null ? condicion : "");
        this.montoTotal.set(montoTotal);
        this.montoPendiente.set(montoPendiente);
        this.nombreCliente.set(nombreCliente != null ? nombreCliente : "");
    }

    public SimpleIntegerProperty idVentaProperty()        { return idVenta; }
    public SimpleIntegerProperty idClienteProperty()      { return idCliente; }
    public SimpleIntegerProperty idEmpleadoProperty()     { return idEmpleado; }
    public SimpleStringProperty  tipoVentaProperty()      { return tipoVenta; }
    public SimpleStringProperty  fechaVentaProperty()     { return fechaVenta; }
    public SimpleStringProperty  condicionProperty()      { return condicion; }
    public SimpleDoubleProperty  montoTotalProperty()     { return montoTotal; }
    public SimpleDoubleProperty  montoPendienteProperty() { return montoPendiente; }
    public SimpleStringProperty  nombreClienteProperty()  { return nombreCliente; }

    public int    getIdVenta()                 { return idVenta.get(); }
    public void   setIdVenta(int v)            { idVenta.set(v); }
    public int    getIdCliente()               { return idCliente.get(); }
    public void   setIdCliente(int v)          { idCliente.set(v); }
    public int    getIdEmpleado()              { return idEmpleado.get(); }
    public void   setIdEmpleado(int v)         { idEmpleado.set(v); }
    public String getTipoVenta()               { return tipoVenta.get(); }
    public void   setTipoVenta(String v)       { tipoVenta.set(v); }
    public String getFechaVenta()              { return fechaVenta.get(); }
    public void   setFechaVenta(String v)      { fechaVenta.set(v); }
    public String getCondicion()               { return condicion.get(); }
    public void   setCondicion(String v)       { condicion.set(v); }
    public double getMontoTotal()              { return montoTotal.get(); }
    public void   setMontoTotal(double v)      { montoTotal.set(v); }
    public double getMontoPendiente()          { return montoPendiente.get(); }
    public void   setMontoPendiente(double v)  { montoPendiente.set(v); }
    public String getNombreCliente()           { return nombreCliente.get(); }
    public void   setNombreCliente(String v)   { nombreCliente.set(v); }
}