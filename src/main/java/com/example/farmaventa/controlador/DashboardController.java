package com.example.farmaventa.controlador;

import Usuarios.SesionUsuario;
import com.example.farmaventa.database.Conexion;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DashboardController {

    @FXML private Label lblBienvenida;
    @FXML private Label lblRol;
    @FXML private Label lblFecha;

    @FXML private Label lblVentasHoy;
    @FXML private Label lblVentasTotal;
    @FXML private Label lblClientes;
    @FXML private Label lblProductos;
    @FXML private Label lblStockBajo;
    @FXML private Label lblEmpleados;
    @FXML private Label lblComprasPend;
    @FXML private Label lblIngresos;

    @FXML private Label lblAlertas;

    @FXML private TableView<VentaResumen> tblUltimasVentas;
    @FXML private TableColumn<VentaResumen, String> colVentaId;
    @FXML private TableColumn<VentaResumen, String> colCliente;
    @FXML private TableColumn<VentaResumen, String> colMonto;
    @FXML private TableColumn<VentaResumen, String> colFecha;

    private final Conexion conexion = new Conexion();

    public static class VentaResumen {
        private final String id;
        private final String cliente;
        private final String monto;
        private final String fecha;
        public VentaResumen(String id, String cliente, String monto, String fecha) {
            this.id = id; this.cliente = cliente; this.monto = monto; this.fecha = fecha;
        }
        public String getId() { return id; }
        public String getCliente() { return cliente; }
        public String getMonto() { return monto; }
        public String getFecha() { return fecha; }
    }

    @FXML
    public void initialize() {
        var usuario = SesionUsuario.getInstance().getUsuarioActual();
        String nombre = usuario != null ? usuario.getNombreCompleto() : "Usuario";
        String rol = usuario != null ? usuario.getRol() : "";
        lblBienvenida.setText("Bienvenido, " + nombre);
        lblRol.setText(rol);
        lblFecha.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM 'de' yyyy")));

        colVentaId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCliente.setCellValueFactory(new PropertyValueFactory<>("cliente"));
        colMonto.setCellValueFactory(new PropertyValueFactory<>("monto"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));

        cargarMetricas();
        cargarUltimasVentas();
        cargarAlertas();
    }

    private void cargarMetricas() {
        lblVentasHoy.setText(contar("SELECT COUNT(*) FROM TBL_VENTA WHERE CAST(fecha_transaccion AS DATE) = CAST(GETDATE() AS DATE)"));
        lblVentasTotal.setText(contar("SELECT COUNT(*) FROM TBL_VENTA"));
        lblClientes.setText(contar("SELECT COUNT(*) FROM TBL_CLIENTE"));
        lblProductos.setText(contar("SELECT COUNT(*) FROM TBL_PRODUCTO"));
        lblStockBajo.setText(contar("SELECT COUNT(*) FROM TBL_PRODUCTO WHERE stock_actual <= stock_minimo"));
        lblEmpleados.setText(contar("SELECT COUNT(*) FROM TBL_EMPLEADO"));
        lblComprasPend.setText(contar("SELECT COUNT(*) FROM TBL_COMPRA WHERE ISNULL(monto_pendiente,0) > 0"));
        lblIngresos.setText(total("SELECT ISNULL(SUM(monto_total), 0) FROM TBL_VENTA"));
    }

    private void cargarUltimasVentas() {
        ObservableList<VentaResumen> items = FXCollections.observableArrayList();
        try (Connection con = conexion.establecerConexion();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT TOP 5 v.id_venta, ISNULL(p.nombre + ' ' + p.apellido, 'N/A') AS cliente, " +
                             "  v.monto_total, v.fecha_transaccion " +
                             "FROM TBL_VENTA v " +
                             "LEFT JOIN TBL_CLIENTE c ON v.id_cliente = c.id_cliente " +
                             "LEFT JOIN TBL_PERSONA p ON c.id_persona = p.id_persona " +
                             "ORDER BY v.fecha_transaccion DESC")) {
            while (rs.next())
                items.add(new VentaResumen(
                        String.format("#%06d", rs.getInt("id_venta")),
                        rs.getString("cliente"),
                        String.format("RD$ %,.2f", rs.getDouble("monto_total")),
                        rs.getDate("fecha_transaccion").toLocalDate()
                                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))));
        } catch (Exception e) {
            System.err.println("Dashboard ventas: " + e.getMessage());
        }
        tblUltimasVentas.setItems(items);
    }

    private void cargarAlertas() {
        StringBuilder alertas = new StringBuilder();
        try (Connection con = conexion.establecerConexion();
             Statement st = con.createStatement()) {

            ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM TBL_PRODUCTO WHERE stock_actual <= stock_minimo");
            if (rs.next() && rs.getInt(1) > 0) alertas.append("⚠ ").append(rs.getInt(1)).append(" producto(s) con stock bajo.\n");
            rs.close();

            rs = st.executeQuery("SELECT COUNT(*) FROM TBL_COMPRA WHERE ISNULL(monto_pendiente,0) > 0");
            if (rs.next() && rs.getInt(1) > 0) alertas.append("⏳ ").append(rs.getInt(1)).append(" compra(s) con pago pendiente.\n");
            rs.close();

            rs = st.executeQuery("SELECT COUNT(*) FROM TBL_VENTA WHERE ISNULL(monto_pendiente,0) > 0");
            if (rs.next() && rs.getInt(1) > 0) alertas.append("💳 ").append(rs.getInt(1)).append(" venta(s) con saldo pendiente.\n");
            rs.close();

        } catch (Exception e) {
            System.err.println("Dashboard alertas: " + e.getMessage());
        }
        if (alertas.isEmpty()) alertas.append("✅ No hay alertas pendientes.");
        lblAlertas.setText(alertas.toString().trim());
    }

    private String contar(String sql) {
        try (Connection con = conexion.establecerConexion();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return String.valueOf(rs.getInt(1));
        } catch (Exception e) {
            System.err.println("Dashboard: " + e.getMessage());
        }
        return "0";
    }

    private String total(String sql) {
        try (Connection con = conexion.establecerConexion();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return String.format("RD$ %,.2f", rs.getDouble(1));
        } catch (Exception e) {
            System.err.println("Dashboard: " + e.getMessage());
        }
        return "RD$ 0.00";
    }
}
