package com.example.farmaventa;

import com.example.farmaventa.database.Conexion;
import com.example.farmaventa.modelo.Venta;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javax.swing.JOptionPane;
import java.sql.*;

public class HelloController {

    Conexion conexion = new Conexion();

    // ── Formulario venta ──────────────────────────────────────────────────
    @FXML private TextField        txtIdVenta;
    @FXML private Label            lblInfoVenta;
    @FXML private TextField        txtIdCliente;
    @FXML private Label            lblNombreCliente;   // NUEVO: nombre del cliente
    @FXML private Label            lblSeguroCliente;   // NUEVO: seguro del cliente
    @FXML private TextField        txtIdEmpleado;
    @FXML private ComboBox<String> cmbTipoVenta;
    @FXML private DatePicker       dpFechaVenta;
    @FXML private TextField        txtCondicion;
    @FXML private TextField        txtMontoPagado;
    @FXML private Label            lblMontoTotal;
    @FXML private Label            lblMontoPendiente;
    @FXML private Label            lblCantProductos;

    // ── Campos para agregar producto ──────────────────────────────────────
    @FXML private TextField txtIdProducto;
    @FXML private TextField txtNombreProducto;
    @FXML private TextField txtCantidadProducto;
    @FXML private TextField txtPrecioProducto;
    @FXML private TextField txtBuscarProducto;

    // ── Panel de cobros del seguro ────────────────────────────────────────
    @FXML private VBox  panelCobrosSeguro;
    @FXML private Label lblCobroAseguradora;
    @FXML private Label lblCobroCliente;
    @FXML private Label lblNumAutorizacion;

    // ── Tabla ─────────────────────────────────────────────────────────────
    @FXML private TableView<Venta>           tablaVentaProducto;
    @FXML private TableColumn<Venta, Number> colVentaId;
    @FXML private TableColumn<Venta, String> colVentaCliente;
    @FXML private TableColumn<Venta, String> colProductoNombre;
    @FXML private TableColumn<Venta, Number> colProductoCantidad;
    @FXML private TableColumn<Venta, Number> colProductoPrecio;
    @FXML private TableColumn<Venta, Number> colProductoSubtotal;

    // ── Lista de datos ────────────────────────────────────────────────────
    private ObservableList<Venta> listaTemporal = FXCollections.observableArrayList();
    private int idVentaSeleccionada = -1;

    // ── Datos de seguro aplicado ──────────────────────────────────────────
    private double  seguroMontoAseguradora = 0;
    private double  seguroMontoCliente     = 0;
    private String  seguroNumAutorizacion  = null;
    private int     seguroIdSeguro         = -1;
    private boolean seguroAplicado         = false;

    // ── Inicializar ───────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        cmbTipoVenta.getItems().addAll("Contado", "Credito", "Seguro");
        dpFechaVenta.setValue(java.time.LocalDate.now());
        lblMontoTotal.setText("RD$ 0.00");
        lblMontoPendiente.setText("RD$ 0.00");
        lblCantProductos.setText("0 productos");

        if (panelCobrosSeguro != null) {
            panelCobrosSeguro.setVisible(false);
            panelCobrosSeguro.setManaged(false);
        }

        colVentaId.setCellValueFactory(c -> c.getValue().idProperty());
        colVentaCliente.setCellValueFactory(c -> c.getValue().clienteProperty());
        colProductoNombre.setCellValueFactory(c -> c.getValue().productoProperty());
        colProductoCantidad.setCellValueFactory(c -> c.getValue().cantidadProperty());
        colProductoPrecio.setCellValueFactory(c -> c.getValue().totalProperty());
        colProductoSubtotal.setCellValueFactory(c -> c.getValue().subtotalProperty());
        tablaVentaProducto.setItems(listaTemporal);

        if (cmbTipoVenta != null) {
            cmbTipoVenta.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (!"Seguro".equals(newVal)) limpiarSeguroAplicado();
            });
        }

        // NUEVO: Al salir del campo ID cliente → buscar nombre y seguro automáticamente
        if (txtIdCliente != null) {
            txtIdCliente.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                if (!isNowFocused && !txtIdCliente.getText().isBlank()) {
                    buscarInfoCliente();
                }
            });
        }
    }

    // ── NUEVO: Buscar nombre y seguro del cliente ─────────────────────────
    private void buscarInfoCliente() {
        String sql = "SELECT p.nombre + ' ' + p.apellido AS nombre_completo, " +
                "       ISNULL(sm.nombre_seguro, 'Sin seguro') AS nombre_seguro, " +
                "       c.id_seguro " +
                "FROM TBL_CLIENTE c " +
                "JOIN TBL_PERSONA p ON p.id_persona = c.id_persona " +
                "LEFT JOIN TBL_SEGURO_MEDICO sm ON sm.id_seguro = c.id_seguro " +
                "WHERE c.id_cliente = ?";
        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(txtIdCliente.getText().trim()));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                if (lblNombreCliente != null)
                    lblNombreCliente.setText(rs.getString("nombre_completo"));

                if (lblSeguroCliente != null) {
                    int idSeg = rs.getInt("id_seguro");
                    if (idSeg > 0) {
                        lblSeguroCliente.setText("🏥 " + rs.getString("nombre_seguro"));
                        lblSeguroCliente.setStyle("-fx-text-fill: #2E7D32; -fx-font-size: 11px; -fx-font-weight: bold;");
                    } else {
                        lblSeguroCliente.setText("Sin seguro médico");
                        lblSeguroCliente.setStyle("-fx-text-fill: #9E9E9E; -fx-font-size: 11px;");
                    }
                }
            } else {
                if (lblNombreCliente != null) lblNombreCliente.setText("Cliente no encontrado");
                if (lblSeguroCliente != null) {
                    lblSeguroCliente.setText("");
                    lblSeguroCliente.setStyle("");
                }
            }
        } catch (Exception ignored) {}
    }

    // ── Aplicar seguro médico ─────────────────────────────────────────────
    @FXML
    public void onAplicarSeguro(ActionEvent event) {
        if (txtIdCliente.getText().isBlank()) {
            JOptionPane.showMessageDialog(null, "Primero ingresa el ID del cliente."); return;
        }
        if (listaTemporal.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Agrega al menos un producto antes de aplicar el seguro."); return;
        }
        try {
            int idCli = Integer.parseInt(txtIdCliente.getText().trim());
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/farmaventa/SeguroMedico.fxml"));
            VBox root = loader.load();
            SeguroMedicoController ctrl = loader.getController();

            ctrl.init(idCli, listaTemporal, (montoAseg, montoCli, numAut, idSeg) -> {
                seguroMontoAseguradora = montoAseg;
                seguroMontoCliente     = montoCli;
                seguroNumAutorizacion  = numAut;
                seguroIdSeguro         = idSeg;
                seguroAplicado         = true;
                cmbTipoVenta.setValue("Seguro");
                mostrarPanelCobrosSeguro(montoAseg, montoCli, numAut);
                actualizarTotales();
            });

            Stage modal = new Stage();
            modal.initModality(Modality.APPLICATION_MODAL);
            modal.setTitle("Seguro Médico – Cobertura de la Venta");
            modal.setScene(new Scene(root));
            modal.setMinWidth(1050);
            modal.setMinHeight(660);
            modal.setResizable(true);
            modal.showAndWait();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "ID de cliente inválido.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al abrir el modal de seguro: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void mostrarPanelCobrosSeguro(double montoAseg, double montoCli, String numAut) {
        if (panelCobrosSeguro == null) return;
        if (lblCobroAseguradora != null) lblCobroAseguradora.setText("RD$ " + String.format("%.2f", montoAseg));
        if (lblCobroCliente     != null) lblCobroCliente.setText("RD$ "     + String.format("%.2f", montoCli));
        if (lblNumAutorizacion  != null) lblNumAutorizacion.setText("Auth: " + numAut);
        panelCobrosSeguro.setVisible(true);
        panelCobrosSeguro.setManaged(true);
    }

    private void limpiarSeguroAplicado() {
        seguroMontoAseguradora = 0; seguroMontoCliente = 0;
        seguroNumAutorizacion  = null; seguroIdSeguro  = -1;
        seguroAplicado         = false;
        if (panelCobrosSeguro != null) {
            panelCobrosSeguro.setVisible(false);
            panelCobrosSeguro.setManaged(false);
        }
    }

    // ── Abrir catálogo ────────────────────────────────────────────────────
    @FXML
    public void onAbrirCatalogo(ActionEvent event) {
        try {
            Node nodo = (Node) event.getSource();
            StackPane contentArea = (StackPane) nodo.getScene().lookup("#contentArea");
            if (contentArea == null) { JOptionPane.showMessageDialog(null, "No se pudo encontrar el área de contenido."); return; }
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/farmaventa/SelectorProducto.fxml"));
            Node selectorVista = loader.load();
            SelectorProductoController selectorCtrl = loader.getController();
            selectorCtrl.init(this, contentArea, construirInfoVentaActual());
            contentArea.getChildren().setAll(selectorVista);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al abrir el catálogo: " + e.getMessage());
        }
    }

    public void recibirProductoDelCatalogo(int idProducto, String nombre, int cantidad, double precio) {
        listaTemporal.add(new Venta(idProducto, "", precio, cantidad, precio * cantidad, nombre, "", "", "", 0, 0, ""));
        actualizarTotales();
        if (seguroAplicado) {
            limpiarSeguroAplicado();
            JOptionPane.showMessageDialog(null, "Producto agregado. Vuelve a aplicar el seguro médico.");
        }
    }

    public void restaurarEstadoEn(HelloController destino) {
        destino.listaTemporal.setAll(this.listaTemporal);
        destino.tablaVentaProducto.setItems(destino.listaTemporal);
        destino.idVentaSeleccionada    = this.idVentaSeleccionada;
        destino.seguroMontoAseguradora = this.seguroMontoAseguradora;
        destino.seguroMontoCliente     = this.seguroMontoCliente;
        destino.seguroNumAutorizacion  = this.seguroNumAutorizacion;
        destino.seguroIdSeguro         = this.seguroIdSeguro;
        destino.seguroAplicado         = this.seguroAplicado;
        if (this.txtIdVenta     != null && destino.txtIdVenta     != null) destino.txtIdVenta.setText(this.txtIdVenta.getText());
        if (this.txtIdCliente   != null && destino.txtIdCliente   != null) destino.txtIdCliente.setText(this.txtIdCliente.getText());
        if (this.txtIdEmpleado  != null && destino.txtIdEmpleado  != null) destino.txtIdEmpleado.setText(this.txtIdEmpleado.getText());
        if (this.txtCondicion   != null && destino.txtCondicion   != null) destino.txtCondicion.setText(this.txtCondicion.getText());
        if (this.txtMontoPagado != null && destino.txtMontoPagado != null) destino.txtMontoPagado.setText(this.txtMontoPagado.getText());
        if (this.cmbTipoVenta   != null && destino.cmbTipoVenta   != null) destino.cmbTipoVenta.setValue(this.cmbTipoVenta.getValue());
        if (this.dpFechaVenta   != null && destino.dpFechaVenta   != null) destino.dpFechaVenta.setValue(this.dpFechaVenta.getValue());
        if (this.lblInfoVenta   != null && destino.lblInfoVenta   != null) {
            destino.lblInfoVenta.setText(this.lblInfoVenta.getText());
            destino.lblInfoVenta.setStyle(this.lblInfoVenta.getStyle());
        }
        if (this.seguroAplicado)
            destino.mostrarPanelCobrosSeguro(this.seguroMontoAseguradora, this.seguroMontoCliente, this.seguroNumAutorizacion);
        destino.actualizarTotales();
    }

    private String construirInfoVentaActual() {
        if (idVentaSeleccionada != -1) return "Editando Venta #" + idVentaSeleccionada + " | " + listaTemporal.size() + " producto(s)";
        String cliente = (txtIdCliente != null && !txtIdCliente.getText().isBlank())
                ? "Cliente #" + txtIdCliente.getText().trim() : "Nueva Venta";
        return cliente + " | " + listaTemporal.size() + " producto(s) en lista";
    }

    // ── Buscar venta existente ────────────────────────────────────────────
    @FXML
    public void onBuscarVenta(ActionEvent event) {
        if (txtIdVenta.getText().isBlank()) return;
        String sql = "SELECT v.id_venta, v.id_cliente, v.id_empleado, v.tipo_venta, v.condicion, " +
                "CONVERT(VARCHAR(10), v.fecha_transaccion, 120) AS fecha, " +
                "v.monto_total, v.monto_pendiente, p.nombre + ' ' + p.apellido AS cliente " +
                "FROM TBL_VENTA v " +
                "JOIN TBL_CLIENTE cl ON cl.id_cliente = v.id_cliente " +
                "JOIN TBL_PERSONA p  ON p.id_persona  = cl.id_persona " +
                "WHERE v.id_venta = ?";
        try {
            int idV = Integer.parseInt(txtIdVenta.getText().trim());
            try (Connection con = conexion.establecerConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, idV);
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) {
                    lblInfoVenta.setText("Venta no encontrada.");
                    lblInfoVenta.setStyle("-fx-text-fill: #C62828; -fx-font-size: 11px;"); return;
                }
                idVentaSeleccionada = rs.getInt("id_venta");
                txtIdCliente.setText(String.valueOf(rs.getInt("id_cliente")));
                txtIdEmpleado.setText(String.valueOf(rs.getInt("id_empleado")));
                cmbTipoVenta.setValue(rs.getString("tipo_venta"));
                txtCondicion.setText(rs.getString("condicion") != null ? rs.getString("condicion") : "");
                try { dpFechaVenta.setValue(java.time.LocalDate.parse(rs.getString("fecha"))); } catch (Exception ignored) {}
                double montoTotal = rs.getDouble("monto_total"), montoPendiente = rs.getDouble("monto_pendiente");
                double yaPagado = montoTotal - montoPendiente;
                lblInfoVenta.setText("Venta #" + idV + " - " + rs.getString("cliente")
                        + " | Total: RD$ " + String.format("%.2f", montoTotal)
                        + " | Pendiente: RD$ " + String.format("%.2f", montoPendiente));
                lblInfoVenta.setStyle("-fx-text-fill: #2E7D32; -fx-font-size: 11px;");
                if (yaPagado > 0) txtMontoPagado.setText(String.format("%.2f", yaPagado));
                cargarProductosDeVenta(con, idV);
                cargarSeguroDeVenta(con, idV);
                buscarInfoCliente(); // NUEVO: cargar nombre y seguro al buscar venta
            }
        } catch (NumberFormatException e) {
            lblInfoVenta.setText("ID invalido.");
            lblInfoVenta.setStyle("-fx-text-fill: #C62828; -fx-font-size: 11px;");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
        }
    }

    private void cargarSeguroDeVenta(Connection con, int idVenta) throws SQLException {
        PreparedStatement ps = con.prepareStatement(
                "SELECT numero_autorizacion, monto_aprobado FROM TBL_VENTA_SEGURO WHERE id_venta = ?");
        ps.setInt(1, idVenta);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            seguroNumAutorizacion  = rs.getString("numero_autorizacion");
            seguroMontoAseguradora = rs.getDouble("monto_aprobado");
            seguroMontoCliente     = calcularTotal() - seguroMontoAseguradora;
            seguroAplicado         = true;
            mostrarPanelCobrosSeguro(seguroMontoAseguradora, seguroMontoCliente, seguroNumAutorizacion);
        }
    }

    private void cargarProductosDeVenta(Connection con, int idVenta) throws SQLException {
        listaTemporal.clear();
        PreparedStatement ps = con.prepareStatement(
                "SELECT vp.id_producto, pr.nombre, vp.cantidad, vp.precio_unitario " +
                        "FROM TBL_VENTA_PRODUCTO vp " +
                        "JOIN TBL_PRODUCTO pr ON pr.id_producto = vp.id_producto " +
                        "WHERE vp.id_venta = ?");
        ps.setInt(1, idVenta);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            double precio = rs.getDouble("precio_unitario");
            int    cant   = rs.getInt("cantidad");
            listaTemporal.add(new Venta(rs.getInt("id_producto"), "", precio, cant, precio * cant,
                    rs.getString("nombre"), "", "", "", 0, 0, ""));
        }
        tablaVentaProducto.setItems(listaTemporal);
        actualizarTotales();
    }

    // ── Buscar producto por ID ────────────────────────────────────────────
    @FXML
    public void onBuscarProductoId(ActionEvent event) {
        if (txtIdProducto.getText().isBlank()) { JOptionPane.showMessageDialog(null, "Ingresa el ID del producto."); return; }
        String sql = "SELECT p.nombre, ISNULL((SELECT TOP 1 precio_venta FROM TBL_PRESENTACION_PRODUCTO " +
                "WHERE id_producto = p.id_producto ORDER BY id_presentacion ASC), 0) AS precio_venta " +
                "FROM TBL_PRODUCTO p WHERE p.id_producto = ?";
        try (Connection con = conexion.establecerConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(txtIdProducto.getText().trim()));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                txtNombreProducto.setText(rs.getString("nombre"));
                double precio = rs.getDouble("precio_venta");
                if (txtPrecioProducto != null) {
                    txtPrecioProducto.setText(precio > 0 ? String.format("%.2f", precio) : "");
                    txtPrecioProducto.setEditable(false);
                    txtPrecioProducto.setStyle("-fx-background-color: #F1F8E9; -fx-background-radius: 6; -fx-border-color: #C8E6C9; -fx-border-radius: 6;");
                }
            } else {
                JOptionPane.showMessageDialog(null, "Producto no encontrado.");
                txtNombreProducto.clear();
                if (txtPrecioProducto != null) txtPrecioProducto.clear();
            }
        } catch (SQLException e) { JOptionPane.showMessageDialog(null, "Error: " + e.getMessage()); }
    }

    // ── Agregar producto ──────────────────────────────────────────────────
    @FXML
    public void onAgregarProducto(ActionEvent event) {
        if (txtIdProducto.getText().isBlank())      { JOptionPane.showMessageDialog(null, "Ingresa el ID del producto."); return; }
        if (txtNombreProducto.getText().isBlank())   { JOptionPane.showMessageDialog(null, "Busca el producto primero."); return; }
        if (txtCantidadProducto.getText().isBlank()) { JOptionPane.showMessageDialog(null, "La cantidad es obligatoria."); return; }
        if (txtPrecioProducto.getText().isBlank())   { JOptionPane.showMessageDialog(null, "El precio es obligatorio."); return; }
        try {
            int    idProd = Integer.parseInt(txtIdProducto.getText().trim());
            int    cant   = Integer.parseInt(txtCantidadProducto.getText().trim());
            double precio = Double.parseDouble(txtPrecioProducto.getText().trim());
            listaTemporal.add(new Venta(idProd, "", precio, cant, precio * cant,
                    txtNombreProducto.getText().trim(), "", "", "", 0, 0, ""));
            tablaVentaProducto.setItems(listaTemporal);
            actualizarTotales();
            txtIdProducto.clear(); txtNombreProducto.clear(); txtCantidadProducto.clear();
            if (txtPrecioProducto != null) {
                txtPrecioProducto.clear(); txtPrecioProducto.setEditable(false);
                txtPrecioProducto.setStyle("-fx-background-color: #F1F8E9; -fx-background-radius: 6; -fx-border-color: #C8E6C9; -fx-border-radius: 6;");
            }
            if (seguroAplicado) { limpiarSeguroAplicado(); JOptionPane.showMessageDialog(null, "Producto agregado. Vuelve a aplicar el seguro médico."); }
        } catch (NumberFormatException e) { JOptionPane.showMessageDialog(null, "Cantidad y precio deben ser números."); }
    }

    @FXML
    public void fnBuscarProducto(ActionEvent event) {
        String b = txtBuscarProducto.getText().trim().toLowerCase();
        if (b.isEmpty()) { tablaVentaProducto.setItems(listaTemporal); return; }
        ObservableList<Venta> f = FXCollections.observableArrayList();
        for (Venta v : listaTemporal) if (v.getProducto().toLowerCase().contains(b)) f.add(v);
        tablaVentaProducto.setItems(f);
    }

    @FXML
    public void onQuitarProducto(ActionEvent event) {
        Venta sel = tablaVentaProducto.getSelectionModel().getSelectedItem();
        if (sel == null) { JOptionPane.showMessageDialog(null, "Selecciona un producto de la tabla."); return; }
        listaTemporal.remove(sel);
        tablaVentaProducto.setItems(listaTemporal);
        actualizarTotales();
        if (seguroAplicado) { limpiarSeguroAplicado(); JOptionPane.showMessageDialog(null, "Producto eliminado. Vuelve a aplicar el seguro médico."); }
    }

    // ── Registrar nueva venta ─────────────────────────────────────────────
    @FXML
    public void onRegistrarVenta(ActionEvent event) {
        if (txtIdCliente.getText().isBlank())  { JOptionPane.showMessageDialog(null, "El ID de Cliente es obligatorio."); return; }
        if (cmbTipoVenta.getValue() == null)   { JOptionPane.showMessageDialog(null, "Selecciona el tipo de venta."); return; }
        if (listaTemporal.isEmpty())           { JOptionPane.showMessageDialog(null, "Agrega al menos un producto."); return; }
        if ("Seguro".equals(cmbTipoVenta.getValue()) && !seguroAplicado) {
            JOptionPane.showMessageDialog(null, "Has seleccionado tipo 'Seguro' pero no has aplicado el seguro.\nHaz clic en '🏥 Aplicar Seguro Médico' para configurarlo."); return;
        }
        double montoTotal = calcularTotal();
        double pendiente  = seguroAplicado ? seguroMontoCliente : Math.max(0, montoTotal - parseMontoPagado());
        String sql = "INSERT INTO TBL_VENTA (id_empleado, tipo_venta, fecha_transaccion, monto_total, monto_pendiente, condicion, id_cliente) VALUES (?,?,?,?,?,?,?)";
        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1,    txtIdEmpleado.getText().isBlank() ? 1 : Integer.parseInt(txtIdEmpleado.getText().trim()));
            ps.setString(2, cmbTipoVenta.getValue());
            ps.setDate(3,   Date.valueOf(dpFechaVenta.getValue()));
            ps.setDouble(4, montoTotal);
            ps.setDouble(5, pendiente);
            ps.setString(6, txtCondicion.getText().trim());
            ps.setInt(7,    Integer.parseInt(txtIdCliente.getText().trim()));
            ps.executeUpdate();
            int idVenta = -1;
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) idVenta = keys.getInt(1);
            for (Venta v : listaTemporal) {
                PreparedStatement psD = con.prepareStatement(
                        "INSERT INTO TBL_VENTA_PRODUCTO (id_venta, id_producto, cantidad, precio_unitario, fecha_venta, id_presentacion) VALUES (?,?,?,?,?,1)");
                psD.setInt(1, idVenta); psD.setInt(2, v.getIdVenta()); psD.setInt(3, v.getCantidad());
                psD.setDouble(4, v.getTotal()); psD.setDate(5, Date.valueOf(dpFechaVenta.getValue()));
                psD.executeUpdate();
            }
            if (seguroAplicado) {
                PreparedStatement psS = con.prepareStatement(
                        "INSERT INTO TBL_VENTA_SEGURO (id_venta, numero_autorizacion, monto_aprobado) VALUES (?,?,?)");
                psS.setInt(1, idVenta); psS.setString(2, seguroNumAutorizacion); psS.setDouble(3, seguroMontoAseguradora);
                psS.executeUpdate();
                JOptionPane.showMessageDialog(null,
                        "Venta #" + idVenta + " registrada con seguro.\n\n" +
                                "✅ COBRO #1 – Aseguradora:  RD$ " + String.format("%.2f", seguroMontoAseguradora) + "\n" +
                                "✅ COBRO #2 – Cliente:       RD$ " + String.format("%.2f", seguroMontoCliente) + "\n" +
                                "📋 Autorización: " + seguroNumAutorizacion);
            } else {
                JOptionPane.showMessageDialog(null, "Venta #" + idVenta + " registrada correctamente.");
            }
            Limpiar();
        } catch (SQLException e) { JOptionPane.showMessageDialog(null, "Error al registrar venta: " + e.getMessage()); }
    }

    // ── Editar venta existente ────────────────────────────────────────────
    @FXML
    public void onEditarVenta(ActionEvent event) {
        if (idVentaSeleccionada == -1) { JOptionPane.showMessageDialog(null, "Primero busca una venta por ID."); return; }
        if (txtIdCliente.getText().isBlank() || cmbTipoVenta.getValue() == null) { JOptionPane.showMessageDialog(null, "Cliente y tipo de venta son obligatorios."); return; }
        double montoTotal = calcularTotal();
        double pendiente  = seguroAplicado ? seguroMontoCliente : Math.max(0, montoTotal - parseMontoPagado());
        String sql = "UPDATE TBL_VENTA SET id_empleado=?, tipo_venta=?, fecha_transaccion=?, monto_total=?, monto_pendiente=?, condicion=?, id_cliente=? WHERE id_venta=?";
        try (Connection con = conexion.establecerConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, txtIdEmpleado.getText().isBlank() ? 1 : Integer.parseInt(txtIdEmpleado.getText().trim()));
            ps.setString(2, cmbTipoVenta.getValue()); ps.setDate(3, Date.valueOf(dpFechaVenta.getValue()));
            ps.setDouble(4, montoTotal); ps.setDouble(5, pendiente);
            ps.setString(6, txtCondicion.getText().trim()); ps.setInt(7, Integer.parseInt(txtIdCliente.getText().trim()));
            ps.setInt(8, idVentaSeleccionada); ps.executeUpdate();
            con.prepareStatement("DELETE FROM TBL_VENTA_PRODUCTO WHERE id_venta=" + idVentaSeleccionada).executeUpdate();
            for (Venta v : listaTemporal) {
                PreparedStatement psD = con.prepareStatement(
                        "INSERT INTO TBL_VENTA_PRODUCTO (id_venta, id_producto, cantidad, precio_unitario, fecha_venta, id_presentacion) VALUES (?,?,?,?,?,1)");
                psD.setInt(1, idVentaSeleccionada); psD.setInt(2, v.getIdVenta()); psD.setInt(3, v.getCantidad());
                psD.setDouble(4, v.getTotal()); psD.setDate(5, Date.valueOf(dpFechaVenta.getValue()));
                psD.executeUpdate();
            }
            if (seguroAplicado) {
                con.prepareStatement("DELETE FROM TBL_VENTA_SEGURO WHERE id_venta=" + idVentaSeleccionada).executeUpdate();
                PreparedStatement psS = con.prepareStatement(
                        "INSERT INTO TBL_VENTA_SEGURO (id_venta, numero_autorizacion, monto_aprobado) VALUES (?,?,?)");
                psS.setInt(1, idVentaSeleccionada); psS.setString(2, seguroNumAutorizacion); psS.setDouble(3, seguroMontoAseguradora);
                psS.executeUpdate();
            }
            JOptionPane.showMessageDialog(null, "Venta #" + idVentaSeleccionada + " actualizada.");
            Limpiar();
        } catch (SQLException e) { JOptionPane.showMessageDialog(null, "Error al editar venta: " + e.getMessage()); }
    }

    // ── Limpiar ───────────────────────────────────────────────────────────
    @FXML
    public void Limpiar() {
        txtIdVenta.clear(); txtIdCliente.clear(); txtIdEmpleado.clear();
        txtCondicion.clear(); txtMontoPagado.clear();
        txtIdProducto.clear(); txtNombreProducto.clear(); txtCantidadProducto.clear();
        if (txtPrecioProducto != null) txtPrecioProducto.clear();
        if (txtBuscarProducto != null) txtBuscarProducto.clear();
        if (lblInfoVenta      != null) lblInfoVenta.setText("");
        if (lblNombreCliente  != null) lblNombreCliente.setText("");   // NUEVO
        if (lblSeguroCliente  != null) lblSeguroCliente.setText("");   // NUEVO
        cmbTipoVenta.setValue(null);
        dpFechaVenta.setValue(java.time.LocalDate.now());
        listaTemporal.clear();
        tablaVentaProducto.setItems(listaTemporal);
        idVentaSeleccionada = -1;
        lblMontoTotal.setText("RD$ 0.00");
        lblMontoPendiente.setText("RD$ 0.00");
        lblCantProductos.setText("0 productos");
        limpiarSeguroAplicado();
    }

    // ── Helpers ───────────────────────────────────────────────────────────
    private double calcularTotal() {
        double t = 0; for (Venta v : listaTemporal) t += v.getSubtotal(); return t;
    }
    private double parseMontoPagado() {
        if (txtMontoPagado == null || txtMontoPagado.getText().isBlank()) return 0;
        try { return Double.parseDouble(txtMontoPagado.getText().trim()); } catch (NumberFormatException e) { return 0; }
    }
    private void actualizarTotales() {
        double total     = calcularTotal();
        double pendiente = seguroAplicado ? seguroMontoCliente : Math.max(0, total - parseMontoPagado());
        lblMontoTotal.setText("RD$ "    + String.format("%.2f", total));
        lblMontoPendiente.setText("RD$ " + String.format("%.2f", pendiente));
        lblCantProductos.setText(listaTemporal.size() + " productos");
    }
}