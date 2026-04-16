package Usuarios;

import com.example.farmaventa.database.Conexion;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import javax.swing.JOptionPane;
import java.io.IOException;
import java.sql.*;

public class RegistroController {

    Conexion conexion = new Conexion();

    @FXML private ComboBox<String> cmbEmpleado;
    @FXML private TextField        txtUsuario;
    @FXML private PasswordField    txtContrasena;
    @FXML private PasswordField    txtConfirmar;
    @FXML private ComboBox<String> cmbRol;
    @FXML private Label            lblError;
    @FXML private Label            lblExito;

    // ── Inicializar ───────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        lblError.setVisible(false);
        lblExito.setVisible(false);

        cmbRol.getItems().addAll("ADMIN", "CAJERO", "FARMACEUTICO", "SUPERVISOR");

        cargarEmpleadosSinCredencial();
    }

    // ── Cargar empleados que aún no tienen usuario ────────────────────────
    private void cargarEmpleadosSinCredencial() {
        cmbEmpleado.getItems().clear();
        String sql = "SELECT e.id_empleado, p.nombre + ' ' + p.apellido AS nombre_completo, c.nombre AS cargo " +
                "FROM TBL_EMPLEADO e " +
                "JOIN TBL_PERSONA  p ON p.id_persona = e.id_persona " +
                "JOIN TBL_CARGO    c ON c.id_cargo   = e.id_cargo " +
                "WHERE e.id_empleado NOT IN " +
                "      (SELECT id_empleado FROM TBL_CREDENCIAL) " +
                "ORDER BY p.nombre";
        try (Connection con = conexion.establecerConexion();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                cmbEmpleado.getItems().add(
                        rs.getInt("id_empleado") + " - " +
                                rs.getString("nombre_completo") + " (" +
                                rs.getString("cargo") + ")");
            }
        } catch (SQLException e) {
            System.err.println("Error cargando empleados: " + e.getMessage());
        }
    }

    // ── Acción Registrar ──────────────────────────────────────────────────
    @FXML
    public void onRegistrar(ActionEvent event) {
        lblError.setVisible(false);
        lblExito.setVisible(false);

        // Validaciones
        if (cmbEmpleado.getValue() == null) {
            mostrarError("Selecciona un empleado."); return;
        }
        if (txtUsuario.getText().isBlank()) {
            mostrarError("El nombre de usuario es obligatorio."); return;
        }
        if (txtUsuario.getText().trim().length() < 4) {
            mostrarError("El usuario debe tener al menos 4 caracteres."); return;
        }
        if (txtContrasena.getText().length() < 6) {
            mostrarError("La contraseña debe tener al menos 6 caracteres."); return;
        }
        if (!txtContrasena.getText().equals(txtConfirmar.getText())) {
            mostrarError("Las contraseñas no coinciden."); return;
        }
        if (cmbRol.getValue() == null) {
            mostrarError("Selecciona un rol."); return;
        }

        int idEmpleado = Integer.parseInt(cmbEmpleado.getValue().split(" - ")[0]);

        try (Connection con = conexion.establecerConexion()) {

            // Verificar que el usuario no exista ya
            PreparedStatement psCheck = con.prepareStatement(
                    "SELECT COUNT(*) FROM TBL_CREDENCIAL WHERE usuario = ?");
            psCheck.setString(1, txtUsuario.getText().trim());
            ResultSet rsCheck = psCheck.executeQuery();
            rsCheck.next();
            if (rsCheck.getInt(1) > 0) {
                mostrarError("Ese nombre de usuario ya está en uso."); return;
            }

            // Insertar credencial
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO TBL_CREDENCIAL (usuario, contrasena, rol, estado, id_empleado) " +
                            "VALUES (?, ?, ?, 1, ?)");
            ps.setString(1, txtUsuario.getText().trim());
            ps.setString(2, txtContrasena.getText()); // producción: hashear
            ps.setString(3, cmbRol.getValue());
            ps.setInt(4, idEmpleado);
            ps.executeUpdate();

            lblExito.setText("✔  Usuario registrado correctamente.");
            lblExito.setVisible(true);
            limpiar();
            cargarEmpleadosSinCredencial();

        } catch (SQLException e) {
            mostrarError("Error al registrar: " + e.getMessage());
        }
    }

    // ── Volver al Login ───────────────────────────────────────────────────
    @FXML
    public void onVolverLogin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/Usuarios/Login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) txtUsuario.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("FarmaVenta — Iniciar Sesión");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error al volver: " + e.getMessage());
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────
    private void mostrarError(String msg) {
        lblError.setText("⚠  " + msg);
        lblError.setVisible(true);
    }

    private void limpiar() {
        txtUsuario.clear();
        txtContrasena.clear();
        txtConfirmar.clear();
        cmbEmpleado.setValue(null);
        cmbRol.setValue(null);
    }
}