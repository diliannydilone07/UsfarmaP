package com.example.farmaventa.modelo;

import javafx.beans.property.*;

/**
 * Modelo Pago — representa TBL_PAGO + JOIN TBL_PAGO_COMPRA + TBL_CUENTA
 * Cada fila en la tabla de pagos = un pago realizado contra una compra.
 */
public class Pago {

    private final SimpleIntegerProperty idPago       = new SimpleIntegerProperty();
    private final SimpleStringProperty  tipoPago     = new SimpleStringProperty();
    private final SimpleStringProperty  fecha        = new SimpleStringProperty();
    private final SimpleDoubleProperty  monto        = new SimpleDoubleProperty();
    private final SimpleStringProperty  metodoPago   = new SimpleStringProperty();
    private final SimpleStringProperty  estado       = new SimpleStringProperty(); // Pagado / Pendiente
    private final SimpleStringProperty  cuenta       = new SimpleStringProperty(); // nombre cuenta
    private final SimpleStringProperty  banco        = new SimpleStringProperty();

    // IDs para operaciones BD
    private int idCuenta;
    private int idCompra;

    public Pago() {}

    public Pago(int idPago, String tipoPago, String fecha, double monto,
                String metodoPago, boolean estadoPago, String cuenta,
                String banco, int idCuenta, int idCompra) {
        this.idPago.set(idPago);
        this.tipoPago.set(tipoPago   != null ? tipoPago   : "");
        this.fecha.set(fecha         != null ? fecha       : "");
        this.monto.set(monto);
        this.metodoPago.set(metodoPago != null ? metodoPago : "");
        this.estado.set(estadoPago ? "✔ Pagado" : "⏳ Pendiente");
        this.cuenta.set(cuenta       != null ? cuenta      : "");
        this.banco.set(banco         != null ? banco       : "");
        this.idCuenta = idCuenta;
        this.idCompra = idCompra;
    }

    // Properties
    public SimpleIntegerProperty idPagoProperty()     { return idPago; }
    public SimpleStringProperty  tipoPagoProperty()   { return tipoPago; }
    public SimpleStringProperty  fechaProperty()      { return fecha; }
    public SimpleDoubleProperty  montoProperty()      { return monto; }
    public SimpleStringProperty  metodoPagoProperty() { return metodoPago; }
    public SimpleStringProperty  estadoProperty()     { return estado; }
    public SimpleStringProperty  cuentaProperty()     { return cuenta; }
    public SimpleStringProperty  bancoProperty()      { return banco; }

    // Getters / Setters
    public int    getIdPago()              { return idPago.get(); }
    public void   setIdPago(int v)         { idPago.set(v); }
    public String getTipoPago()            { return tipoPago.get(); }
    public void   setTipoPago(String v)    { tipoPago.set(v); }
    public String getFecha()               { return fecha.get(); }
    public void   setFecha(String v)       { fecha.set(v); }
    public double getMonto()               { return monto.get(); }
    public void   setMonto(double v)       { monto.set(v); }
    public String getMetodoPago()          { return metodoPago.get(); }
    public void   setMetodoPago(String v)  { metodoPago.set(v); }
    public String getEstado()              { return estado.get(); }
    public int    getIdCuenta()            { return idCuenta; }
    public void   setIdCuenta(int v)       { idCuenta = v; }
    public int    getIdCompra()            { return idCompra; }
    public void   setIdCompra(int v)       { idCompra = v; }
    public String getCuenta()              { return cuenta.get(); }
    public String getBanco()               { return banco.get(); }
}