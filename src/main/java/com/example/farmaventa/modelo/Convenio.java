package com.example.farmaventa.modelo;

import javafx.beans.property.*;

/**
 * Modelo Convenio — basado en TBL_CONVENIO + diagrama de clases.
 *
 * BD: TBL_CONVENIO (id_convenio, fecha_inicio, fecha_fin, acuerdo, id_proveedor, id_producto)
 * Se muestra en pantalla con nombres de proveedor y producto (JOIN).
 */
public class Convenio {

    // ── JavaFX Properties para TableView ──────────────────────────────────
    private final SimpleIntegerProperty idConvenio    = new SimpleIntegerProperty();
    private final SimpleStringProperty  fechaInicio   = new SimpleStringProperty();
    private final SimpleStringProperty  fechaFin      = new SimpleStringProperty();
    private final SimpleStringProperty  acuerdo       = new SimpleStringProperty();
    private final SimpleStringProperty  proveedor     = new SimpleStringProperty(); // JOIN nombre
    private final SimpleStringProperty  producto      = new SimpleStringProperty(); // JOIN nombre
    private final SimpleStringProperty  vigencia      = new SimpleStringProperty(); // calculado

    // ── IDs raw para operaciones de BD ────────────────────────────────────
    private int idProveedor;
    private int idProducto;

    // ── Constructores ─────────────────────────────────────────────────────
    public Convenio() {}

    public Convenio(int idConvenio, String fechaInicio, String fechaFin, String acuerdo,
                    String proveedor, String producto,
                    int idProveedor, int idProducto) {
        this.idConvenio.set(idConvenio);
        this.fechaInicio.set(fechaInicio != null ? fechaInicio : "");
        this.fechaFin.set(fechaFin       != null ? fechaFin    : "");
        this.acuerdo.set(acuerdo         != null ? acuerdo     : "");
        this.proveedor.set(proveedor     != null ? proveedor   : "");
        this.producto.set(producto       != null ? producto    : "");
        this.idProveedor = idProveedor;
        this.idProducto  = idProducto;
        this.vigencia.set(calcularVigencia(fechaFin));
    }

    // ── Método del diagrama ───────────────────────────────────────────────
    /**
     * Determina si el convenio sigue vigente comparando fecha_fin con hoy.
     * Retorna "Vigente" o "Vencido".
     */
    public String validarVigencia() {
        return calcularVigencia(fechaFin.get());
    }

    private static String calcularVigencia(String fechaFinStr) {
        if (fechaFinStr == null || fechaFinStr.isBlank()) return "Sin fecha";
        try {
            java.time.LocalDate fin = java.time.LocalDate.parse(fechaFinStr.substring(0, 10));
            return fin.isBefore(java.time.LocalDate.now()) ? "Vencido" : "Vigente";
        } catch (Exception e) {
            return "Sin fecha";
        }
    }

    // ── Properties ────────────────────────────────────────────────────────
    public SimpleIntegerProperty idConvenioProperty()  { return idConvenio; }
    public SimpleStringProperty  fechaInicioProperty() { return fechaInicio; }
    public SimpleStringProperty  fechaFinProperty()    { return fechaFin; }
    public SimpleStringProperty  acuerdoProperty()     { return acuerdo; }
    public SimpleStringProperty  proveedorProperty()   { return proveedor; }
    public SimpleStringProperty  productoProperty()    { return producto; }
    public SimpleStringProperty  vigenciaProperty()    { return vigencia; }

    // ── Getters / Setters ─────────────────────────────────────────────────
    public int    getIdConvenio()            { return idConvenio.get(); }
    public void   setIdConvenio(int v)       { idConvenio.set(v); }

    public String getFechaInicio()           { return fechaInicio.get(); }
    public void   setFechaInicio(String v)   { fechaInicio.set(v); }

    public String getFechaFin()              { return fechaFin.get(); }
    public void   setFechaFin(String v)      { fechaFin.set(v); vigencia.set(calcularVigencia(v)); }

    public String getAcuerdo()               { return acuerdo.get(); }
    public void   setAcuerdo(String v)       { acuerdo.set(v); }

    public String getProveedor()             { return proveedor.get(); }
    public void   setProveedor(String v)     { proveedor.set(v); }

    public String getProducto()              { return producto.get(); }
    public void   setProducto(String v)      { producto.set(v); }

    public String getVigencia()              { return vigencia.get(); }

    public int    getIdProveedor()           { return idProveedor; }
    public void   setIdProveedor(int v)      { idProveedor = v; }

    public int    getIdProducto()            { return idProducto; }
    public void   setIdProducto(int v)       { idProducto = v; }
}