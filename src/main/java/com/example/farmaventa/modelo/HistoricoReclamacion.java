package com.example.farmaventa.modelo;

import java.time.LocalDate;

/**
 * Modelo: HISTORICO_RECLAMACION
 * Atributos según diagrama de clases:
 *   - id_historico           : int
 *   - descripcion            : string
 *   - cliente                : cliente
 *   - fecha_creacion         : date
 *   - id_reclamacionventa    : id_reclamacion_venta
 *
 * Método:
 *   + obtenerDetalleCambio()
 */
public class HistoricoReclamacion {

    private int        idHistorico;
    private String     descripcion;
    private String     cliente;              // nombre del cliente
    private LocalDate  fechaCreacion;
    private int        idReclamacionventa;   // FK hacia ReclamacionVenta

    // ── Constructor vacío ─────────────────────────────────────────────────
    public HistoricoReclamacion() {}

    // ── Constructor completo ──────────────────────────────────────────────
    public HistoricoReclamacion(int id, String descripcion, String cliente,
                                LocalDate fechaCreacion, int idReclamacionventa) {
        this.idHistorico          = id;
        this.descripcion          = descripcion;
        this.cliente              = cliente;
        this.fechaCreacion        = fechaCreacion;
        this.idReclamacionventa   = idReclamacionventa;
    }

    // ── Método del diagrama ───────────────────────────────────────────────

    /**
     * obtenerDetalleCambio()
     * Retorna un texto formateado con todos los datos del registro histórico.
     * Se muestra en el TextArea de detalle en la UI.
     */
    public String obtenerDetalleCambio() {
        return  "ID Histórico : " + idHistorico + "\n" +
                "Fecha        : " + (fechaCreacion != null ? fechaCreacion.toString() : "—") + "\n" +
                "Cliente      : " + (cliente != null ? cliente : "—") + "\n" +
                "Reclamación  : #" + idReclamacionventa + "\n\n" +
                "Descripción  :\n" + (descripcion != null ? descripcion : "Sin descripción.");
    }

    // ── Getters y Setters ─────────────────────────────────────────────────

    public int getIdHistorico()                     { return idHistorico; }
    public void setIdHistorico(int id)              { this.idHistorico = id; }

    public String getDescripcion()                  { return descripcion; }
    public void setDescripcion(String desc)         { this.descripcion = desc; }

    public String getCliente()                      { return cliente; }
    public void setCliente(String cliente)          { this.cliente = cliente; }

    public LocalDate getFechaCreacion()             { return fechaCreacion; }
    public void setFechaCreacion(LocalDate fecha)   { this.fechaCreacion = fecha; }

    public int getIdReclamacionventa()              { return idReclamacionventa; }
    public void setIdReclamacionventa(int id)       { this.idReclamacionventa = id; }
}