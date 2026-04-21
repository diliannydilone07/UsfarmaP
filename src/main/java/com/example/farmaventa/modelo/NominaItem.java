package com.example.farmaventa.modelo;

import javafx.beans.property.*;

/**
 * Representa una fila en la tabla de nómina.
 * Cada NominaItem = un empleado con su detalle de nómina (TBL_NOMINA + TBL_DETALLE_NOMINA).
 */
public class NominaItem {

    private final SimpleIntegerProperty idNomina        = new SimpleIntegerProperty();
    private final SimpleIntegerProperty idEmpleado      = new SimpleIntegerProperty();
    private final SimpleStringProperty  nombreEmpleado  = new SimpleStringProperty();
    private final SimpleStringProperty  cargo           = new SimpleStringProperty();
    private final SimpleDoubleProperty  salarioBase     = new SimpleDoubleProperty();
    private final SimpleDoubleProperty  bonificacion    = new SimpleDoubleProperty();
    private final SimpleDoubleProperty  comisionVenta   = new SimpleDoubleProperty();
    private final SimpleDoubleProperty  horasExtras     = new SimpleDoubleProperty();   // monto
    private final SimpleDoubleProperty  descuento       = new SimpleDoubleProperty();
    private final SimpleDoubleProperty  salarioNeto     = new SimpleDoubleProperty();
    private final SimpleStringProperty  tipoPago        = new SimpleStringProperty();
    private final SimpleStringProperty  fechaPago       = new SimpleStringProperty();
    private final SimpleStringProperty  estado          = new SimpleStringProperty();   // "Pendiente" / "Pagado"

    // No expuestos en tabla pero necesarios para operaciones
    private int    idCuenta;
    private int    periodo;
    private double horasExtrasTiempo; // horas reales (time → double)

    public NominaItem() {}

    public NominaItem(int idNomina, int idEmpleado, String nombreEmpleado, String cargo,
                      double salarioBase, double bonificacion, double comisionVenta,
                      double horasExtras, double descuento, double salarioNeto,
                      String tipoPago, String fechaPago, String estado,
                      int idCuenta, int periodo) {
        this.idNomina.set(idNomina);
        this.idEmpleado.set(idEmpleado);
        this.nombreEmpleado.set(nombreEmpleado != null ? nombreEmpleado : "");
        this.cargo.set(cargo           != null ? cargo          : "");
        this.salarioBase.set(salarioBase);
        this.bonificacion.set(bonificacion);
        this.comisionVenta.set(comisionVenta);
        this.horasExtras.set(horasExtras);
        this.descuento.set(descuento);
        this.salarioNeto.set(salarioNeto);
        this.tipoPago.set(tipoPago     != null ? tipoPago       : "");
        this.fechaPago.set(fechaPago   != null ? fechaPago      : "");
        this.estado.set(estado         != null ? estado         : "Pendiente");
        this.idCuenta = idCuenta;
        this.periodo  = periodo;
    }

    // ── Properties ────────────────────────────────────────────────────────
    public SimpleIntegerProperty idNominaProperty()       { return idNomina; }
    public SimpleIntegerProperty idEmpleadoProperty()     { return idEmpleado; }
    public SimpleStringProperty  nombreEmpleadoProperty() { return nombreEmpleado; }
    public SimpleStringProperty  cargoProperty()          { return cargo; }
    public SimpleDoubleProperty  salarioBaseProperty()    { return salarioBase; }
    public SimpleDoubleProperty  bonificacionProperty()   { return bonificacion; }
    public SimpleDoubleProperty  comisionVentaProperty()  { return comisionVenta; }
    public SimpleDoubleProperty  horasExtrasProperty()    { return horasExtras; }
    public SimpleDoubleProperty  descuentoProperty()      { return descuento; }
    public SimpleDoubleProperty  salarioNetoProperty()    { return salarioNeto; }
    public SimpleStringProperty  tipoPagoProperty()       { return tipoPago; }
    public SimpleStringProperty  fechaPagoProperty()      { return fechaPago; }
    public SimpleStringProperty  estadoProperty()         { return estado; }

    // ── Getters / Setters ─────────────────────────────────────────────────
    public int    getIdNomina()                  { return idNomina.get(); }
    public void   setIdNomina(int v)             { idNomina.set(v); }
    public int    getIdEmpleado()                { return idEmpleado.get(); }
    public void   setIdEmpleado(int v)           { idEmpleado.set(v); }
    public String getNombreEmpleado()            { return nombreEmpleado.get(); }
    public void   setNombreEmpleado(String v)    { nombreEmpleado.set(v); }
    public String getCargo()                     { return cargo.get(); }
    public void   setCargo(String v)             { cargo.set(v); }
    public double getSalarioBase()               { return salarioBase.get(); }
    public void   setSalarioBase(double v)       { salarioBase.set(v); recalcular(); }
    public double getBonificacion()              { return bonificacion.get(); }
    public void   setBonificacion(double v)      { bonificacion.set(v); recalcular(); }
    public double getComisionVenta()             { return comisionVenta.get(); }
    public void   setComisionVenta(double v)     { comisionVenta.set(v); recalcular(); }
    public double getHorasExtras()               { return horasExtras.get(); }
    public void   setHorasExtras(double v)       { horasExtras.set(v); recalcular(); }
    public double getDescuento()                 { return descuento.get(); }
    public void   setDescuento(double v)         { descuento.set(v); recalcular(); }
    public double getSalarioNeto()               { return salarioNeto.get(); }
    public void   setSalarioNeto(double v)       { salarioNeto.set(v); }
    public String getTipoPago()                  { return tipoPago.get(); }
    public void   setTipoPago(String v)          { tipoPago.set(v); }
    public String getFechaPago()                 { return fechaPago.get(); }
    public void   setFechaPago(String v)         { fechaPago.set(v); }
    public String getEstado()                    { return estado.get(); }
    public void   setEstado(String v)            { estado.set(v); }
    public int    getIdCuenta()                  { return idCuenta; }
    public void   setIdCuenta(int v)             { idCuenta = v; }
    public int    getPeriodo()                   { return periodo; }
    public void   setPeriodo(int v)              { periodo = v; }
    public double getHorasExtrasTiempo()         { return horasExtrasTiempo; }
    public void   setHorasExtrasTiempo(double v) { horasExtrasTiempo = v; }

    /** Recalcula salario_neto = base + bonificacion + comision + horas_extras - descuento */
    public void recalcular() {
        double neto = salarioBase.get()
                + bonificacion.get()
                + comisionVenta.get()
                + horasExtras.get()
                - descuento.get();
        salarioNeto.set(Math.max(0, Math.round(neto * 100.0) / 100.0));
    }
}
