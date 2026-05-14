package com.example.farmaventa.modelo;

import javafx.beans.property.*;


public class CuentaPendiente {

    public enum TipoCuenta { COMPRA, VENTA, SEGURO_ASEGURADORA, SEGURO_CLIENTE }

    private final SimpleIntegerProperty idRef          = new SimpleIntegerProperty(); // id_compra / id_venta / id_ventaseguro
    private final SimpleStringProperty  descripcion    = new SimpleStringProperty();  // proveedor / cliente / aseguradora
    private final SimpleStringProperty  fecha          = new SimpleStringProperty();
    private final SimpleDoubleProperty  montoTotal     = new SimpleDoubleProperty();
    private final SimpleDoubleProperty  montoPendiente = new SimpleDoubleProperty();
    private final SimpleDoubleProperty  montoPagado    = new SimpleDoubleProperty();
    private final SimpleStringProperty  tipo           = new SimpleStringProperty();  // "Contado" / "Credito" / etc.
    private final SimpleStringProperty  estado         = new SimpleStringProperty();  // "Parcial" / "Pendiente" / "Pagado"

    private TipoCuenta tipoCuenta;

    public CuentaPendiente() {}

    public CuentaPendiente(int idRef, String descripcion, String fecha,
                           double montoTotal, double montoPendiente,
                           String tipo, TipoCuenta tipoCuenta) {
        this.idRef.set(idRef);
        this.descripcion.set(descripcion != null ? descripcion : "");
        this.fecha.set(fecha != null ? fecha : "");
        this.montoTotal.set(montoTotal);
        this.montoPendiente.set(montoPendiente);
        this.montoPagado.set(Math.max(0, montoTotal - montoPendiente));
        this.tipo.set(tipo != null ? tipo : "");
        this.tipoCuenta = tipoCuenta;

        if (montoPendiente <= 0) {
            this.estado.set("Pagado");
        } else if (montoPendiente < montoTotal) {
            this.estado.set("Parcial");
        } else {
            this.estado.set("Pendiente");
        }
    }

    public SimpleIntegerProperty idRefProperty()          { return idRef; }
    public SimpleStringProperty  descripcionProperty()    { return descripcion; }
    public SimpleStringProperty  fechaProperty()          { return fecha; }
    public SimpleDoubleProperty  montoTotalProperty()     { return montoTotal; }
    public SimpleDoubleProperty  montoPendienteProperty() { return montoPendiente; }
    public SimpleDoubleProperty  montoPagadoProperty()    { return montoPagado; }
    public SimpleStringProperty  tipoProperty()           { return tipo; }
    public SimpleStringProperty  estadoProperty()         { return estado; }

    public int    getIdRef()               { return idRef.get(); }
    public void   setIdRef(int v)          { idRef.set(v); }
    public String getDescripcion()         { return descripcion.get(); }
    public void   setDescripcion(String v) { descripcion.set(v); }
    public String getFecha()               { return fecha.get(); }
    public void   setFecha(String v)       { fecha.set(v); }
    public double getMontoTotal()          { return montoTotal.get(); }
    public void   setMontoTotal(double v)  { montoTotal.set(v); }
    public double getMontoPendiente()      { return montoPendiente.get(); }
    public void   setMontoPendiente(double v) {
        montoPendiente.set(v);
        montoPagado.set(Math.max(0, montoTotal.get() - v));
        if (v <= 0)                    estado.set("Pagado");
        else if (v < montoTotal.get()) estado.set("Parcial");
        else                           estado.set("Pendiente");
    }
    public double getMontoPagado()         { return montoPagado.get(); }
    public String getTipo()                { return tipo.get(); }
    public void   setTipo(String v)        { tipo.set(v); }
    public String getEstado()              { return estado.get(); }
    public TipoCuenta getTipoCuenta()      { return tipoCuenta; }
    public void   setTipoCuenta(TipoCuenta v) { tipoCuenta = v; }
}