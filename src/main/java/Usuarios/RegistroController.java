package Usuarios;

import com.example.farmaventa.database.Conexion;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

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

    @FXML
    public void initialize() {
        lblError.setVisible(false);
        lblError.setManaged(false);
        lblExito.setVisible(false);
        lblExito.setManaged(false);

        cmbRol.getItems().addAll("ADMIN", "CAJERO", "FARMACEUTICO", "SUPERVISOR");
        cargarEmpleadosSinCredencial();
    }

    private void cargarEmpleadosSinCredencial() {
        cmbEmpleado.getItems().clear();
        String sql = "SELECT e.id_empleado, " +
                "p.nombre + ' ' + p.apellido AS nombre_completo, " +
                "c.nombre AS cargo " +
                "FROM TBL_EMPLEADO e " +
                "JOIN TBL_PERSONA p ON p.id_persona = e.id_persona " +
                "JOIN TBL_CARGO   c ON c.id_cargo   = e.id_cargo " +
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

    @FXML
    public void onRegistrar(ActionEvent event) {
        lblError.setVisible(false);  lblError.setManaged(false);
        lblExito.setVisible(false);  lblExito.setManaged(false);

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

            PreparedStatement psCheck = con.prepareStatement(
                    "SELECT COUNT(*) FROM TBL_CREDENCIAL WHERE usuario = ?");
            psCheck.setString(1, txtUsuario.getText().trim());
            ResultSet rsCheck = psCheck.executeQuery();
            rsCheck.next();
            if (rsCheck.getInt(1) > 0) {
                mostrarError("Ese nombre de usuario ya está en uso."); return;
            }

            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO TBL_CREDENCIAL (usuario, contrasena, rol, estado, id_empleado) " +
                            "VALUES (?, ?, ?, 1, ?)");
            ps.setString(1, txtUsuario.getText().trim());
            ps.setString(2, txtContrasena.getText());
            ps.setString(3, cmbRol.getValue());
            ps.setInt(4, idEmpleado);
            ps.executeUpdate();

            mostrarExito("✔  Usuario registrado correctamente.");
            limpiar();
            cargarEmpleadosSinCredencial();

        } catch (SQLException e) {
            mostrarError("Error al registrar: " + e.getMessage());
        }
    }

    private void mostrarError(String msg) {
        lblError.setText("⚠  " + msg);
        lblError.setVisible(true);
        lblError.setManaged(true);
    }

    private void mostrarExito(String msg) {
        lblExito.setText(msg);
        lblExito.setVisible(true);
        lblExito.setManaged(true);
    }

    private void limpiar() {
        txtUsuario.clear();
        txtContrasena.clear();
        txtConfirmar.clear();
        cmbEmpleado.setValue(null);
        cmbRol.setValue(null);
    }
}