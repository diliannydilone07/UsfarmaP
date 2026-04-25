package Usuarios;

import com.example.farmaventa.database.Conexion;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.*;
import java.time.format.DateTimeFormatter;

public class RegistroController {

    Conexion conexion = new Conexion();

    // Formulario
    @FXML private ComboBox<String>  cmbEmpleado;
    @FXML private TextField         txtUsuario;
    @FXML private PasswordField     txtContrasena;
    @FXML private PasswordField     txtConfirmar;
    @FXML private ComboBox<String>  cmbRol;
    @FXML private Label             lblError;
    @FXML private Label             lblExito;

    // Tabla
    @FXML private TableView<UsuarioRow>          tablaUsuarios;
    @FXML private TableColumn<UsuarioRow,String> colId;
    @FXML private TableColumn<UsuarioRow,String> colNombre;
    @FXML private TableColumn<UsuarioRow,String> colUsuario;
    @FXML private TableColumn<UsuarioRow,String> colRol;
    @FXML private TableColumn<UsuarioRow,String> colEstado;
    @FXML private TableColumn<UsuarioRow,String> colAcceso;
    @FXML private TextField                      txtBuscarUsuario;

    // Botón principal — su texto cambia entre "Registrar" y "Actualizar"
    @FXML private Button btnAccion;

    private ObservableList<UsuarioRow> listaUsuarios = FXCollections.observableArrayList();
    private Integer idEditando = null; // null = modo registro, >0 = modo edición

    // ─────────────────────────────────────────────
    // Modelo de fila
    // ─────────────────────────────────────────────
    public static class UsuarioRow {
        public int    id;
        public String nombre, usuario, rol, estado, acceso;

        public UsuarioRow(int id, String nombre, String usuario,
                          String rol, String estado, String acceso) {
            this.id = id;
            this.nombre  = nombre;
            this.usuario = usuario;
            this.rol     = rol;
            this.estado  = estado;
            this.acceso  = acceso;
        }
    }

    // ─────────────────────────────────────────────
    // initialize
    // ─────────────────────────────────────────────
    @FXML
    public void initialize() {
        ocultarMensajes();
        cmbRol.getItems().addAll("ADMIN", "CAJERO", "FARMACEUTICO", "SUPERVISOR");

        // Columnas
        colId.setCellValueFactory(c      -> new SimpleStringProperty(String.valueOf(c.getValue().id)));
        colNombre.setCellValueFactory(c  -> new SimpleStringProperty(c.getValue().nombre));
        colUsuario.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().usuario));
        colRol.setCellValueFactory(c     -> new SimpleStringProperty(c.getValue().rol));
        colEstado.setCellValueFactory(c  -> new SimpleStringProperty(c.getValue().estado));
        colAcceso.setCellValueFactory(c  -> new SimpleStringProperty(c.getValue().acceso));

        // Colorear estado
        colEstado.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                setStyle(item.equals("Activo")
                        ? "-fx-text-fill: #2E7D32; -fx-font-weight: bold;"
                        : "-fx-text-fill: #C62828; -fx-font-weight: bold;");
            }
        });

        // Búsqueda en vivo
        FilteredList<UsuarioRow> filtrada = new FilteredList<>(listaUsuarios, p -> true);
        txtBuscarUsuario.textProperty().addListener((obs, old, val) ->
                filtrada.setPredicate(row -> val == null || val.isEmpty()
                        || row.nombre.toLowerCase().contains(val.toLowerCase())
                        || row.usuario.toLowerCase().contains(val.toLowerCase())
                        || row.rol.toLowerCase().contains(val.toLowerCase()))
        );
        tablaUsuarios.setItems(filtrada);

        // ── CLAVE: al seleccionar una fila → llenar formulario automáticamente ──
        tablaUsuarios.getSelectionModel().selectedItemProperty().addListener(
                (obs, anterior, seleccionada) -> {
                    if (seleccionada != null) {
                        cargarEnFormulario(seleccionada);
                    }
                }
        );

        cargarEmpleadosSinCredencial();
        cargarUsuarios();
    }

    // ─────────────────────────────────────────────
    // Llena el formulario con los datos de la fila
    // ─────────────────────────────────────────────
    private void cargarEnFormulario(UsuarioRow row) {
        idEditando = row.id;
        txtUsuario.setText(row.usuario);
        cmbRol.setValue(row.rol);
        txtContrasena.clear();
        txtConfirmar.clear();
        cmbEmpleado.setDisable(true); // No se puede cambiar el empleado al editar

        // Cambiar el botón a modo edición
        btnAccion.setText("Actualizar");

        ocultarMensajes();
        mostrarExito("Editando: " + row.nombre + "  —  deja la contraseña en blanco para no cambiarla.");
    }

    // ─────────────────────────────────────────────
    // Cargar datos
    // ─────────────────────────────────────────────
    private void cargarEmpleadosSinCredencial() {
        cmbEmpleado.getItems().clear();
        String sql =
                "SELECT e.id_empleado, p.nombre + ' ' + p.apellido AS nombre_completo, " +
                        "c.nombre AS cargo FROM TBL_EMPLEADO e " +
                        "JOIN TBL_PERSONA p ON p.id_persona = e.id_persona " +
                        "JOIN TBL_CARGO   c ON c.id_cargo   = e.id_cargo " +
                        "WHERE e.id_empleado NOT IN (SELECT id_empleado FROM TBL_CREDENCIAL) " +
                        "ORDER BY p.nombre";
        try (Connection con = conexion.establecerConexion();
             Statement st  = con.createStatement();
             ResultSet rs  = st.executeQuery(sql)) {
            while (rs.next())
                cmbEmpleado.getItems().add(
                        rs.getInt("id_empleado") + " - " +
                                rs.getString("nombre_completo") + " (" + rs.getString("cargo") + ")");
        } catch (SQLException e) {
            System.err.println("Error cargando empleados: " + e.getMessage());
        }
    }

    private void cargarUsuarios() {
        listaUsuarios.clear();
        String sql =
                "SELECT c.id_credencial, p.nombre + ' ' + p.apellido AS nombre_completo, " +
                        "c.usuario, c.rol, c.estado, c.ultimo_acceso " +
                        "FROM TBL_CREDENCIAL c " +
                        "JOIN TBL_EMPLEADO e ON e.id_empleado = c.id_empleado " +
                        "JOIN TBL_PERSONA  p ON p.id_persona  = e.id_persona " +
                        "ORDER BY p.nombre";
        try (Connection con = conexion.establecerConexion();
             Statement st  = con.createStatement();
             ResultSet rs  = st.executeQuery(sql)) {
            while (rs.next()) {
                String acceso = rs.getTimestamp("ultimo_acceso") != null
                        ? rs.getTimestamp("ultimo_acceso").toLocalDateTime()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                        : "Nunca";
                listaUsuarios.add(new UsuarioRow(
                        rs.getInt("id_credencial"),
                        rs.getString("nombre_completo"),
                        rs.getString("usuario"),
                        rs.getString("rol"),
                        rs.getBoolean("estado") ? "Activo" : "Inactivo",
                        acceso));
            }
        } catch (SQLException e) {
            mostrarError("Error cargando usuarios: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────
    // Registrar / Actualizar  (mismo botón)
    // ─────────────────────────────────────────────
    @FXML
    public void onRegistrar(ActionEvent event) {
        ocultarMensajes();

        // Validaciones
        if (cmbEmpleado.getValue() == null && idEditando == null) {
            mostrarError("Selecciona un empleado."); return;
        }
        if (txtUsuario.getText().isBlank()) {
            mostrarError("El nombre de usuario es obligatorio."); return;
        }
        if (txtUsuario.getText().trim().length() < 4) {
            mostrarError("El usuario debe tener al menos 4 caracteres."); return;
        }
        if (idEditando == null || !txtContrasena.getText().isEmpty()) {
            if (txtContrasena.getText().length() < 6) {
                mostrarError("La contraseña debe tener al menos 6 caracteres."); return;
            }
            if (!txtContrasena.getText().equals(txtConfirmar.getText())) {
                mostrarError("Las contraseñas no coinciden."); return;
            }
        }
        if (cmbRol.getValue() == null) {
            mostrarError("Selecciona un rol."); return;
        }

        try (Connection con = conexion.establecerConexion()) {

            // Verificar usuario duplicado
            PreparedStatement psCheck = con.prepareStatement(
                    "SELECT COUNT(*) FROM TBL_CREDENCIAL WHERE usuario = ?" +
                            (idEditando != null ? " AND id_credencial <> ?" : ""));
            psCheck.setString(1, txtUsuario.getText().trim());
            if (idEditando != null) psCheck.setInt(2, idEditando);
            ResultSet rsCheck = psCheck.executeQuery();
            rsCheck.next();
            if (rsCheck.getInt(1) > 0) {
                mostrarError("Ese nombre de usuario ya está en uso."); return;
            }

            if (idEditando == null) {
                // ── INSERT ──
                int idEmpleado = Integer.parseInt(cmbEmpleado.getValue().split(" - ")[0]);
                PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO TBL_CREDENCIAL (usuario, contrasena, rol, estado, id_empleado) " +
                                "VALUES (?, ?, ?, 1, ?)");
                ps.setString(1, txtUsuario.getText().trim());
                ps.setString(2, txtContrasena.getText());
                ps.setString(3, cmbRol.getValue());
                ps.setInt(4, idEmpleado);
                ps.executeUpdate();
                mostrarExito("✔  Usuario registrado correctamente.");

            } else {
                // ── UPDATE ──
                boolean cambiaClave = !txtContrasena.getText().isEmpty();
                String sql = cambiaClave
                        ? "UPDATE TBL_CREDENCIAL SET usuario=?, contrasena=?, rol=? WHERE id_credencial=?"
                        : "UPDATE TBL_CREDENCIAL SET usuario=?, rol=? WHERE id_credencial=?";
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setString(1, txtUsuario.getText().trim());
                if (cambiaClave) {
                    ps.setString(2, txtContrasena.getText());
                    ps.setString(3, cmbRol.getValue());
                    ps.setInt(4, idEditando);
                } else {
                    ps.setString(2, cmbRol.getValue());
                    ps.setInt(3, idEditando);
                }
                ps.executeUpdate();
                mostrarExito("✔  Usuario actualizado correctamente.");
            }

            onLimpiar(null);
            cargarEmpleadosSinCredencial();
            cargarUsuarios();

        } catch (SQLException e) {
            mostrarError("Error: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────
    // onEditar: ya no es necesario pulsarlo,
    // se mantiene por compatibilidad con el FXML
    // ─────────────────────────────────────────────
    @FXML
    public void onEditar(ActionEvent event) {
        UsuarioRow row = tablaUsuarios.getSelectionModel().getSelectedItem();
        if (row == null) { mostrarError("Selecciona un usuario de la tabla."); return; }
        cargarEnFormulario(row);
    }

    // ─────────────────────────────────────────────
    // Toggle estado Activo / Inactivo
    // ─────────────────────────────────────────────
    @FXML
    public void onToggleEstado(ActionEvent event) {
        UsuarioRow row = tablaUsuarios.getSelectionModel().getSelectedItem();
        if (row == null) { mostrarError("Selecciona un usuario."); return; }

        boolean nuevoEstado = row.estado.equals("Inactivo"); // inactivo → activar
        try (Connection con = conexion.establecerConexion()) {
            PreparedStatement ps = con.prepareStatement(
                    "UPDATE TBL_CREDENCIAL SET estado=? WHERE id_credencial=?");
            ps.setBoolean(1, nuevoEstado);
            ps.setInt(2, row.id);
            ps.executeUpdate();
            mostrarExito("✔  Estado actualizado a " + (nuevoEstado ? "Activo" : "Inactivo") + ".");
            cargarUsuarios();
        } catch (SQLException e) {
            mostrarError("Error: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────
    // Eliminar
    // ─────────────────────────────────────────────
    @FXML
    public void onEliminar(ActionEvent event) {
        UsuarioRow row = tablaUsuarios.getSelectionModel().getSelectedItem();
        if (row == null) { mostrarError("Selecciona un usuario."); return; }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Eliminar al usuario \"" + row.usuario + "\"? Esta acción no se puede deshacer.",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirmar eliminación");
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.YES) {
                try (Connection con = conexion.establecerConexion()) {
                    PreparedStatement ps = con.prepareStatement(
                            "DELETE FROM TBL_CREDENCIAL WHERE id_credencial=?");
                    ps.setInt(1, row.id);
                    ps.executeUpdate();
                    mostrarExito("✔  Usuario eliminado.");
                    cargarEmpleadosSinCredencial();
                    cargarUsuarios();
                } catch (SQLException e) {
                    mostrarError("Error: " + e.getMessage());
                }
            }
        });
    }

    // ─────────────────────────────────────────────
    // Limpiar / cancelar edición
    // ─────────────────────────────────────────────
    @FXML
    public void onLimpiar(ActionEvent event) {
        txtUsuario.clear();
        txtContrasena.clear();
        txtConfirmar.clear();
        cmbEmpleado.setValue(null);
        cmbRol.setValue(null);
        cmbEmpleado.setDisable(false);
        idEditando = null;

        // Deseleccionar tabla sin disparar el listener de nuevo
        tablaUsuarios.getSelectionModel().clearSelection();

        // Volver el botón a modo registro
        btnAccion.setText("Registrar");

        ocultarMensajes();
    }

    // ─────────────────────────────────────────────
    // Mensajes
    // ─────────────────────────────────────────────
    private void mostrarError(String msg) {
        lblError.setText("⚠  " + msg);
        lblError.setVisible(true);  lblError.setManaged(true);
        lblExito.setVisible(false); lblExito.setManaged(false);
    }

    private void mostrarExito(String msg) {
        lblExito.setText(msg);
        lblExito.setVisible(true);  lblExito.setManaged(true);
        lblError.setVisible(false); lblError.setManaged(false);
    }

    private void ocultarMensajes() {
        lblError.setVisible(false); lblError.setManaged(false);
        lblExito.setVisible(false); lblExito.setManaged(false);
    }
}