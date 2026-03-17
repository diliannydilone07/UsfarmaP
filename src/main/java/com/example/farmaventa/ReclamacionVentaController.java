package com.example.farmaventa;

import com.example.farmaventa.modelo.HistoricoReclamacion;
import com.example.farmaventa.modelo.ReclamacionVenta;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

/**
 * Controlador de ReclamacionVenta.fxml
 *
 * Conecta con las clases del diagrama:
 *   - ReclamacionVenta  (modelo)
 *   - HistoricoReclamacion (modelo)
 *   - EstadoReclamacion y sus implementaciones (enum o clases)
 */
public class ReclamacionVentaController implements Initializable {

    // ══════════  Formulario  ══════════
    @FXML private TextField   txtIdReclamacion;
    @FXML private TextField   txtIdVenta;
    @FXML private TextField   txtCliente;
    @FXML private DatePicker  dpFechaReclamacion;
    @FXML private ComboBox<String> cmbEstadoActual;
    @FXML private TextField   txtCantidadDevolver;
    @FXML private TextArea    txtDescripcion;

    // ══════════  Tabla principal  ══════════
    @FXML private TableView<ReclamacionVenta>   tablaReclamaciones;
    @FXML private TableColumn<ReclamacionVenta, Integer>    colId;
    @FXML private TableColumn<ReclamacionVenta, String>     colVenta;
    @FXML private TableColumn<ReclamacionVenta, String>     colCliente;
    @FXML private TableColumn<ReclamacionVenta, LocalDate>  colFecha;
    @FXML private TableColumn<ReclamacionVenta, String>     colEstado;
    @FXML private TableColumn<ReclamacionVenta, Integer>    colCantidad;
    @FXML private TableColumn<ReclamacionVenta, String>     colDescripcion;

    // ══════════  Filtros y búsqueda  ══════════
    @FXML private ComboBox<String> cmbFiltroEstado;
    @FXML private TextField        txtBusqueda;

    // ══════════  Historial  ══════════
    @FXML private TableView<HistoricoReclamacion>  tablaHistorico;
    @FXML private TableColumn<HistoricoReclamacion, Integer> colHistId;
    @FXML private TableColumn<HistoricoReclamacion, LocalDate> colHistFecha;
    @FXML private TableColumn<HistoricoReclamacion, String>  colHistDescripcion;
    @FXML private TextArea  txtDetalleHistorial;
    @FXML private TextArea  txtNuevaNotaHistorial;
    @FXML private Label     lblHistorialDe;

    // ══════════  Pastillas de conteo  ══════════
    @FXML private Label lblContPendiente;
    @FXML private Label lblContRevision;
    @FXML private Label lblContAprobada;
    @FXML private Label lblContRechazada;

    // ══════════  Datos  ══════════
    private final ObservableList<ReclamacionVenta>    listaReclamaciones = FXCollections.observableArrayList();
    private final ObservableList<HistoricoReclamacion> listaHistorico    = FXCollections.observableArrayList();
    private FilteredList<ReclamacionVenta> listaFiltrada;

    // Estados disponibles (del diagrama)
    private static final String PENDIENTE   = "ESTADO_RECLAMACION_PENDIENTE";
    private static final String EN_REVISION = "ESTADO_EN_REVISION";
    private static final String APROBADA    = "ESTADO_APROBADA";
    private static final String RECHAZADA   = "ESTADO_RECHAZADA";

    // ══════════════════════════════════════════════════════════════════════
    //  INICIALIZACIÓN
    // ══════════════════════════════════════════════════════════════════════

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        configurarColumnas();
        configurarComboEstados();
        configurarFiltros();
        configurarSeleccionTabla();
        configurarColumnaHistorico();
        dpFechaReclamacion.setValue(LocalDate.now());
    }

    // ──────────────────────────────────────────────────────────────────────
    //  Configuración inicial
    // ──────────────────────────────────────────────────────────────────────

    private void configurarColumnas() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idReclamacionventa"));
        colVenta.setCellValueFactory(new PropertyValueFactory<>("idVenta"));
        colCliente.setCellValueFactory(new PropertyValueFactory<>("nombreCliente"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fechaReclamacion"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estadoActualNombre"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidadAdevolver"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));

        // Colorear la columna de estado según el valor
        colEstado.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String estado, boolean empty) {
                super.updateItem(estado, empty);
                if (empty || estado == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(estado.replace("ESTADO_", "").replace("_", " "));
                    switch (estado) {
                        case PENDIENTE   -> setStyle("-fx-text-fill: #F57F17; -fx-font-weight: bold;");
                        case EN_REVISION -> setStyle("-fx-text-fill: #1565C0; -fx-font-weight: bold;");
                        case APROBADA    -> setStyle("-fx-text-fill: #2E7D32; -fx-font-weight: bold;");
                        case RECHAZADA   -> setStyle("-fx-text-fill: #C62828; -fx-font-weight: bold;");
                        default          -> setStyle("");
                    }
                }
            }
        });

        listaFiltrada = new FilteredList<>(listaReclamaciones, p -> true);
        tablaReclamaciones.setItems(listaFiltrada);
    }

    private void configurarComboEstados() {
        ObservableList<String> estados = FXCollections.observableArrayList(
                PENDIENTE, EN_REVISION, APROBADA, RECHAZADA
        );
        cmbEstadoActual.setItems(estados);
        cmbEstadoActual.setValue(PENDIENTE);

        // ComboBox de filtro incluye "Todos"
        ObservableList<String> filtros = FXCollections.observableArrayList("Todos");
        filtros.addAll(estados);
        cmbFiltroEstado.setItems(filtros);
        cmbFiltroEstado.setValue("Todos");
    }

    private void configurarFiltros() {
        // Filtrar por estado
        cmbFiltroEstado.valueProperty().addListener((obs, old, nuevo) -> aplicarFiltros());

        // Filtrar por búsqueda de texto
        txtBusqueda.textProperty().addListener((obs, old, nuevo) -> aplicarFiltros());
    }

    private void aplicarFiltros() {
        String estado  = cmbFiltroEstado.getValue();
        String busq    = txtBusqueda.getText().toLowerCase();

        listaFiltrada.setPredicate(r -> {
            boolean coincideEstado = "Todos".equals(estado) || estado == null
                    || r.getEstadoActualNombre().equals(estado);
            boolean coincideBusq = busq.isEmpty()
                    || String.valueOf(r.getIdReclamacionventa()).contains(busq)
                    || r.getNombreCliente().toLowerCase().contains(busq)
                    || r.getIdVenta().toLowerCase().contains(busq);
            return coincideEstado && coincideBusq;
        });
    }

    private void configurarSeleccionTabla() {
        tablaReclamaciones.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, seleccionada) -> {
                    if (seleccionada != null) {
                        cargarEnFormulario(seleccionada);
                        cargarHistorial(seleccionada);
                    }
                }
        );
    }

    private void configurarColumnaHistorico() {
        colHistId.setCellValueFactory(new PropertyValueFactory<>("idHistorico"));
        colHistFecha.setCellValueFactory(new PropertyValueFactory<>("fechaCreacion"));
        colHistDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        tablaHistorico.setItems(listaHistorico);

        // Al seleccionar un historial, mostrar detalle
        tablaHistorico.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, h) -> {
                    if (h != null) {
                        txtDetalleHistorial.setText(h.obtenerDetalleCambio());
                    }
                }
        );
    }

    // ══════════════════════════════════════════════════════════════════════
    //  HANDLERS DEL FXML (onAction)
    // ══════════════════════════════════════════════════════════════════════

    /** Busca la venta y autocompleta el cliente */
    @FXML
    private void onBuscarVenta() {
        String idVenta = txtIdVenta.getText().trim();
        if (idVenta.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campo vacío", "Ingresa un ID de venta para buscar.");
            return;
        }
        // TODO: reemplazar con llamada real al servicio/DAO
        // Venta venta = ventaService.buscarPorId(idVenta);
        // txtCliente.setText(venta.getCliente().getNombreCompleto());
        txtCliente.setText("Cliente de ejemplo para " + idVenta); // placeholder
    }

    /** Registra una nueva reclamación */
    @FXML
    private void onRegistrarReclamacion() {
        if (!validarFormulario()) return;

        // TODO: reemplazar con llamada al servicio real
        // ReclamacionVenta reclamacion = new ReclamacionVenta(...);
        // reclamacionService.registrarReclamacion(reclamacion);

        // Simulación de registro
        int nuevoId = listaReclamaciones.size() + 1;
        // listaReclamaciones.add(reclamacion);

        actualizarContadores();
        limpiarFormulario();
        mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito",
                "Reclamación #" + nuevoId + " registrada correctamente.");
    }

    /** Carga el historial de la reclamación seleccionada */
    @FXML
    private void onVerHistorial() {
        ReclamacionVenta sel = tablaReclamaciones.getSelectionModel().getSelectedItem();
        if (sel == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Sin selección",
                    "Selecciona una reclamación para ver su historial.");
            return;
        }
        cargarHistorial(sel);
    }

    /** Aprueba la reclamación seleccionada (llama aprobar() del estado) */
    @FXML
    private void onAprobar() {
        ReclamacionVenta sel = tablaReclamaciones.getSelectionModel().getSelectedItem();
        if (sel == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Sin selección", "Selecciona una reclamación."); return;
        }
        // TODO: sel.getEstadoActual().aprobar();
        // sel.setEstadoActualNombre(APROBADA);
        tablaReclamaciones.refresh();
        actualizarContadores();
        mostrarAlerta(Alert.AlertType.INFORMATION, "Aprobada",
                "Reclamación aprobada correctamente.");
    }

    /** Rechaza la reclamación seleccionada (llama rechazar() del estado) */
    @FXML
    private void onRechazar() {
        ReclamacionVenta sel = tablaReclamaciones.getSelectionModel().getSelectedItem();
        if (sel == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Sin selección", "Selecciona una reclamación."); return;
        }
        // TODO: sel.getEstadoActual().rechazar();
        // sel.setEstadoActualNombre(RECHAZADA);
        tablaReclamaciones.refresh();
        actualizarContadores();
        mostrarAlerta(Alert.AlertType.INFORMATION, "Rechazada",
                "Reclamación rechazada correctamente.");
    }

    /** Elimina la reclamación seleccionada */
    @FXML
    private void onEliminar() {
        ReclamacionVenta sel = tablaReclamaciones.getSelectionModel().getSelectedItem();
        if (sel == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Sin selección", "Selecciona una reclamación."); return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Eliminar la reclamación #" + sel.getIdReclamacionventa() + "?",
                ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText("Confirmar eliminación");
        confirm.showAndWait().ifPresent(resp -> {
            if (resp == ButtonType.YES) {
                listaReclamaciones.remove(sel);
                listaHistorico.clear();
                lblHistorialDe.setText("—");
                actualizarContadores();
            }
        });
    }

    /** Agrega una nota manual al historial de la reclamación seleccionada */
    @FXML
    private void onAgregarNota() {
        ReclamacionVenta sel = tablaReclamaciones.getSelectionModel().getSelectedItem();
        if (sel == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Sin selección",
                    "Selecciona una reclamación antes de agregar una nota."); return;
        }
        String nota = txtNuevaNotaHistorial.getText().trim();
        if (nota.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campo vacío", "Escribe una nota antes de agregar."); return;
        }
        // TODO: reemplazar con llamada al servicio
        // HistoricoReclamacion h = new HistoricoReclamacion(nota, sel.getIdReclamacionventa());
        // listaHistorico.add(h);
        txtNuevaNotaHistorial.clear();
        mostrarAlerta(Alert.AlertType.INFORMATION, "Nota agregada", "Nota registrada en el historial.");
    }

    /** Limpia el formulario */
    @FXML
    private void onLimpiarFormulario() {
        limpiarFormulario();
    }

    // ══════════════════════════════════════════════════════════════════════
    //  HELPERS PRIVADOS
    // ══════════════════════════════════════════════════════════════════════

    private void cargarEnFormulario(ReclamacionVenta r) {
        txtIdReclamacion.setText(String.valueOf(r.getIdReclamacionventa()));
        txtIdVenta.setText(r.getIdVenta());
        txtCliente.setText(r.getNombreCliente());
        dpFechaReclamacion.setValue(r.getFechaReclamacion());
        cmbEstadoActual.setValue(r.getEstadoActualNombre());
        txtCantidadDevolver.setText(String.valueOf(r.getCantidadAdevolver()));
        txtDescripcion.setText(r.getDescripcion());
    }

    private void cargarHistorial(ReclamacionVenta r) {
        listaHistorico.clear();
        lblHistorialDe.setText("Rec. #" + r.getIdReclamacionventa());
        // TODO: listaHistorico.addAll(historicoService.buscarPorReclamacion(r.getId()));
    }

    private void limpiarFormulario() {
        txtIdReclamacion.clear();
        txtIdVenta.clear();
        txtCliente.clear();
        dpFechaReclamacion.setValue(LocalDate.now());
        cmbEstadoActual.setValue(PENDIENTE);
        txtCantidadDevolver.clear();
        txtDescripcion.clear();
        tablaReclamaciones.getSelectionModel().clearSelection();
    }

    private boolean validarFormulario() {
        if (txtIdVenta.getText().isBlank()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campo requerido", "El ID de Venta es obligatorio."); return false;
        }
        if (dpFechaReclamacion.getValue() == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campo requerido", "Selecciona una fecha de reclamación."); return false;
        }
        if (txtDescripcion.getText().isBlank()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campo requerido", "La descripción es obligatoria."); return false;
        }
        return true;
    }

    private void actualizarContadores() {
        long pend = listaReclamaciones.stream().filter(r -> PENDIENTE.equals(r.getEstadoActualNombre())).count();
        long rev  = listaReclamaciones.stream().filter(r -> EN_REVISION.equals(r.getEstadoActualNombre())).count();
        long aprob= listaReclamaciones.stream().filter(r -> APROBADA.equals(r.getEstadoActualNombre())).count();
        long rech = listaReclamaciones.stream().filter(r -> RECHAZADA.equals(r.getEstadoActualNombre())).count();

        lblContPendiente.setText("⏳ " + pend + " Pendientes");
        lblContRevision.setText("🔍 " + rev + " En Revisión");
        lblContAprobada.setText("✔ " + aprob + " Aprobadas");
        lblContRechazada.setText("✖ " + rech + " Rechazadas");
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
