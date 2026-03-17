package com.example.farmaventa.modelo;

import java.time.LocalDate;

/**
 * Modelo: RECLAMACION_VENTA
 * Atributos según diagrama de clases:
 *   - id_reclamacionventa : int
 *   - Venta               : Venta
 *   - fecha_reclamacion   : Date
 *   - estado_actual       : EstadoReclamacion
 *   - descripcion         : varchar
 *   - cantidadAdevolver   : int
 *
 * Métodos del diagrama:
 *   + registrarReclamacion()
 *   + validarGarantia()
 */
public class ReclamacionVenta {

    private int         idReclamacionventa;
    private String      idVenta;           // referencia a la venta asociada
    private String      nombreCliente;     // campo de conveniencia para la UI
    private LocalDate   fechaReclamacion;
    private String      estadoActualNombre; // nombre del estado (enum/clase)
    private String      descripcion;
    private int         cantidadAdevolver;

    // ── Constructor vacío ──────────────────────────────────────────────────
    public ReclamacionVenta() {}

    // ── Constructor completo ───────────────────────────────────────────────
    public ReclamacionVenta(int id, String idVenta, String nombreCliente,
                            LocalDate fecha, String estado,
                            String descripcion, int cantidadAdevolver) {
        this.idReclamacionventa = id;
        this.idVenta            = idVenta;
        this.nombreCliente      = nombreCliente;
        this.fechaReclamacion   = fecha;
        this.estadoActualNombre = estado;
        this.descripcion        = descripcion;
        this.cantidadAdevolver  = cantidadAdevolver;
    }

    // ── Métodos del diagrama ───────────────────────────────────────────────

    /**
     * registrarReclamacion()
     * Persiste la reclamación en la base de datos.
     * TODO: implementar con DAO/servicio real.
     */
    public void registrarReclamacion() {
        // reclamacionDAO.guardar(this);
        System.out.println("Reclamación registrada: " + idReclamacionventa);
    }

    /**
     * validarGarantia()
     * Verifica si la venta asociada cae dentro del período de garantía.
     * TODO: implementar lógica de negocio real.
     */
    public boolean validarGarantia() {
        // Ejemplo: garantía de 30 días desde la venta
        // Venta venta = ventaDAO.buscarPorId(idVenta);
        // return venta.getFecha().plusDays(30).isAfter(LocalDate.now());
        return true; // placeholder
    }

    // ── Getters y Setters ──────────────────────────────────────────────────

    public int getIdReclamacionventa()           { return idReclamacionventa; }
    public void setIdReclamacionventa(int id)    { this.idReclamacionventa = id; }

    public String getIdVenta()                   { return idVenta; }
    public void setIdVenta(String idVenta)       { this.idVenta = idVenta; }

    public String getNombreCliente()             { return nombreCliente; }
    public void setNombreCliente(String nombre)  { this.nombreCliente = nombre; }

    public LocalDate getFechaReclamacion()                   { return fechaReclamacion; }
    public void setFechaReclamacion(LocalDate fecha)         { this.fechaReclamacion = fecha; }

    public String getEstadoActualNombre()                    { return estadoActualNombre; }
    public void setEstadoActualNombre(String estadoNombre)   { this.estadoActualNombre = estadoNombre; }

    public String getDescripcion()               { return descripcion; }
    public void setDescripcion(String desc)      { this.descripcion = desc; }

    public int getCantidadAdevolver()            { return cantidadAdevolver; }
    public void setCantidadAdevolver(int cant)   { this.cantidadAdevolver = cant; }
}
