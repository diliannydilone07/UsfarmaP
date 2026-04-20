package com.example.farmaventa;

import com.example.farmaventa.database.Conexion;
import com.example.farmaventa.modelo.Venta;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import javax.swing.JOptionPane;
import java.sql.*;
import java.util.List;

public class SeguroMedicoController {

    // ── FXML ─────────────────────────────────────────────────────────────
    @FXML private Label     lblNombreSeguro;
    @FXML private Label     lblAseguradora;
    @FXML private Label     lblCoberturaBase;
    @FXML private Label     lblTelefonoSeguro;
    @FXML private VBox      panelSinSeguro;
    @FXML private TextField txtNumAutorizacion;

    @FXML private Label lblResumenTotal;
    @FXML private Label lblMontoAseguradora;
    @FXML private Label lblMontoCliente;

    @FXML private TableView<FilaSeguro>           tablaSeguro;
    @FXML private TableColumn<FilaSeguro, String> colSegProd;
    @FXML private TableColumn<FilaSeguro, Number> colSegCant;
    @FXML private TableColumn<FilaSeguro, Number> colSegPrecio;
    @FXML private TableColumn<FilaSeguro, Number> colSegSubtotal;
    @FXML private TableColumn<FilaSeguro, String> colSegCobertura;
    @FXML private TableColumn<FilaSeguro, Number> colSegAseg;
    @FXML private TableColumn<FilaSeguro, Number> colSegCliente;

    @FXML private Button btnConfirmar;

    // ── Estado interno ────────────────────────────────────────────────────
    private final Conexion conexion = new Conexion();
    private int    idCliente    = -1;
    private int    idSeguro     = -1;
    private double coberturaBase = 0.0;
    private SeguroCallback callback;

    private final ObservableList<FilaSeguro> filas = FXCollections.observableArrayList();

    // ── Callback — 4 parámetros, sin filasDetalle ─────────────────────────
    public interface SeguroCallback {
        void onSeguroConfirmado(
                double montoAseguradora,
                double montoCliente,
                String numAutorizacion,
                int    idSeguro
        );
    }

    // ── Init ──────────────────────────────────────────────────────────────
    public void init(int idCliente, List<Venta> productos, SeguroCallback callback) {
        this.idCliente = idCliente;
        this.callback  = callback;
        cargarSeguroCliente();
        cargarProductosEnTabla(productos);
        recalcularTotales();
    }

    @FXML
    public void initialize() {
        colSegProd.setCellValueFactory(c -> c.getValue().productoProperty());
        colSegCant.setCellValueFactory(c -> c.getValue().cantidadProperty());
        colSegPrecio.setCellValueFactory(c -> c.getValue().precioUnitarioProperty());
        colSegSubtotal.setCellValueFactory(c -> c.getValue().subtotalProperty());
        colSegAseg.setCellValueFactory(c -> c.getValue().montoAseguradoraProperty());
        colSegCliente.setCellValueFactory(c -> c.getValue().montoClienteProperty());

        formatearColumnaMoneda(colSegPrecio);
        formatearColumnaMoneda(colSegSubtotal);
        formatearColumnaMoneda(colSegAseg);
        formatearColumnaMoneda(colSegCliente);

        colSegCobertura.setCellValueFactory(c -> c.getValue().coberturaTextoProperty());
        colSegCobertura.setCellFactory(col -> new TableCell<>() {
            private final TextField tf = new TextField();
            {
                tf.setStyle("-fx-background-radius: 4; -fx-border-color: #A5D6A7; -fx-border-radius: 4;");
                tf.textProperty().addListener((obs, o, n) -> {
                    if (getIndex() < 0 || getIndex() >= getTableView().getItems().size()) return;
                    FilaSeguro fila = getTableView().getItems().get(getIndex());
                    fila.setCoberturaTexto(n);
                    try {
                        double pct = Double.parseDouble(n.replace(",", ".").replace("%", "").trim());
                        pct = Math.min(100, Math.max(0, pct));
                        fila.actualizarMontos(pct);
                        recalcularTotales();
                    } catch (NumberFormatException ignored) {}
                });
            }
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                tf.setText(item);
                setGraphic(tf);
            }
        });

        tablaSeguro.setItems(filas);
    }

    // ── Cargar seguro del cliente ─────────────────────────────────────────
    private void cargarSeguroCliente() {
        String sql = "SELECT sm.id_seguro, sm.nombre_seguro, sm.telefono_seguro, sm.cobertura, " +
                "       a.nombre AS nombre_aseguradora " +
                "FROM TBL_CLIENTE c " +
                "JOIN TBL_SEGURO_MEDICO sm ON sm.id_seguro   = c.id_seguro " +
                "JOIN TBL_ASEGURADORA   a  ON a.id_aseguradora = sm.id_aseguradora " +
                "WHERE c.id_cliente = ?";
        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idCliente);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                idSeguro      = rs.getInt("id_seguro");
                coberturaBase = rs.getDouble("cobertura");
                lblNombreSeguro.setText(rs.getString("nombre_seguro"));
                lblAseguradora.setText(rs.getString("nombre_aseguradora"));
                lblCoberturaBase.setText(String.format("%.0f%%", coberturaBase));
                lblTelefonoSeguro.setText(rs.getString("telefono_seguro"));
            } else {
                mostrarPanelSinSeguro();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al cargar seguro: " + e.getMessage());
            mostrarPanelSinSeguro();
        }
    }

    private void mostrarPanelSinSeguro() {
        idSeguro = -1; coberturaBase = 0;
        lblNombreSeguro.setText("Sin seguro");
        lblAseguradora.setText("—");
        lblCoberturaBase.setText("0%");
        lblTelefonoSeguro.setText("—");
        panelSinSeguro.setVisible(true);
        panelSinSeguro.setManaged(true);
        btnConfirmar.setDisable(true);
    }

    // ── Cargar productos en tabla ─────────────────────────────────────────
    private void cargarProductosEnTabla(List<Venta> productos) {
        filas.clear();
        for (Venta v : productos) {
            filas.add(new FilaSeguro(
                    v.getIdVenta(),
                    v.getProducto(),
                    v.getCantidad(),
                    v.getTotal(),
                    v.getSubtotal(),
                    coberturaBase
            ));
        }
    }

    // ── Recalcular totales ────────────────────────────────────────────────
    private void recalcularTotales() {
        double totalVenta = 0, totalAseg = 0, totalCli = 0;
        for (FilaSeguro f : filas) {
            totalVenta += f.getSubtotal();
            totalAseg  += f.getMontoAseguradora();
            totalCli   += f.getMontoCliente();
        }
        lblResumenTotal.setText("RD$ "     + String.format("%.2f", totalVenta));
        lblMontoAseguradora.setText("RD$ " + String.format("%.2f", totalAseg));
        lblMontoCliente.setText("RD$ "     + String.format("%.2f", totalCli));
    }

    // ── Confirmar ─────────────────────────────────────────────────────────
    @FXML
    public void onConfirmarSeguro(ActionEvent event) {
        if (txtNumAutorizacion.getText().isBlank()) {
            JOptionPane.showMessageDialog(null, "El número de autorización es obligatorio."); return;
        }
        if (idSeguro == -1) {
            JOptionPane.showMessageDialog(null, "El cliente no tiene seguro médico registrado."); return;
        }
        double totalAseg = 0, totalCli = 0;
        for (FilaSeguro f : filas) {
            totalAseg += f.getMontoAseguradora();
            totalCli  += f.getMontoCliente();
        }
        if (callback != null) {
            callback.onSeguroConfirmado(totalAseg, totalCli,
                    txtNumAutorizacion.getText().trim(), idSeguro);
        }
        cerrarVentana();
    }

    @FXML public void onCerrar(ActionEvent event) { cerrarVentana(); }
    private void cerrarVentana() {
        ((Stage) txtNumAutorizacion.getScene().getWindow()).close();
    }

    private void formatearColumnaMoneda(TableColumn<FilaSeguro, Number> col) {
        col.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : "RD$ " + String.format("%.2f", item.doubleValue()));
            }
        });
    }

    // ── Modelo de fila ────────────────────────────────────────────────────
    public static class FilaSeguro {
        private final int idProducto;
        private final SimpleStringProperty  producto         = new SimpleStringProperty();
        private final SimpleIntegerProperty cantidad         = new SimpleIntegerProperty();
        private final SimpleDoubleProperty  precioUnitario   = new SimpleDoubleProperty();
        private final SimpleDoubleProperty  subtotal         = new SimpleDoubleProperty();
        private final SimpleDoubleProperty  montoAseguradora = new SimpleDoubleProperty();
        private final SimpleDoubleProperty  montoCliente     = new SimpleDoubleProperty();
        private final SimpleStringProperty  coberturaTexto   = new SimpleStringProperty();
        private double coberturaActual;

        public FilaSeguro(int idProducto, String producto, int cantidad,
                          double precioUnitario, double subtotal, double coberturaBase) {
            this.idProducto = idProducto;
            this.producto.set(producto);
            this.cantidad.set(cantidad);
            this.precioUnitario.set(precioUnitario);
            this.subtotal.set(subtotal);
            this.coberturaActual = coberturaBase;
            this.coberturaTexto.set(String.format("%.0f", coberturaBase));
            actualizarMontos(coberturaBase);
        }

        public void actualizarMontos(double pct) {
            coberturaActual = pct;
            double aseg = subtotal.get() * (pct / 100.0);
            montoAseguradora.set(aseg);
            montoCliente.set(subtotal.get() - aseg);
        }

        public void setCoberturaTexto(String v) { coberturaTexto.set(v); }

        public int    getIdProducto()        { return idProducto; }
        public double getCoberturaActual()   { return coberturaActual; }
        public double getSubtotal()          { return subtotal.get(); }
        public double getMontoAseguradora()  { return montoAseguradora.get(); }
        public double getMontoCliente()      { return montoCliente.get(); }

        public SimpleStringProperty  productoProperty()         { return producto; }
        public SimpleIntegerProperty cantidadProperty()         { return cantidad; }
        public SimpleDoubleProperty  precioUnitarioProperty()   { return precioUnitario; }
        public SimpleDoubleProperty  subtotalProperty()         { return subtotal; }
        public SimpleDoubleProperty  montoAseguradoraProperty() { return montoAseguradora; }
        public SimpleDoubleProperty  montoClienteProperty()     { return montoCliente; }
        public SimpleStringProperty  coberturaTextoProperty()   { return coberturaTexto; }
    }
}