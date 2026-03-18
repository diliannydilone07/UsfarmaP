package com.example.farmaventa;

import com.example.farmaventa.database.Conexion;
import com.example.farmaventa.modelo.Persona;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import javax.swing.JOptionPane;
import java.sql.*;

public class PersonaController {

    Conexion conexion = new Conexion();

    // ── Rol ───────────────────────────────────────────────────────────────
    @FXML private RadioButton      rbCliente;
    @FXML private RadioButton      rbEmpleado;
    @FXML private VBox             vboxEmpleado;
    @FXML private VBox             vboxCliente;
    @FXML private ComboBox<String> cmbCargo;
    @FXML private DatePicker       dpHorario;
    @FXML private ComboBox<String> cmbAseguradora;
    @FXML private ComboBox<String> cmbSeguro;

    // ── Datos personales ──────────────────────────────────────────────────
    @FXML private TextField        txtNombre;
    @FXML private TextField        txtApellido;
    @FXML private ComboBox<String> cmbGenero;
    @FXML private TextField        txtTelefono;
    @FXML private TextField        txtCorreo;

    // ── Dirección ─────────────────────────────────────────────────────────
    @FXML private ComboBox<String> cmbRegion;
    @FXML private ComboBox<String> cmbProvincia;
    @FXML private ComboBox<String> cmbMunicipio;
    @FXML private TextField        txtSector;
    @FXML private TextField        txtCalle;
    @FXML private TextField        txtDescripcionDireccion;

    // ── Tabla ─────────────────────────────────────────────────────────────
    @FXML private TextField                      txtBuscar;
    @FXML private TableView<Persona>             tablaPersonas;
    @FXML private TableColumn<Persona, Number>   colId;
    @FXML private TableColumn<Persona, String>   colNombre;
    @FXML private TableColumn<Persona, String>   colApellido;
    @FXML private TableColumn<Persona, String>   colGenero;
    @FXML private TableColumn<Persona, String>   colTelefono;
    @FXML private TableColumn<Persona, String>   colCorreo;
    @FXML private TableColumn<Persona, String>   colDireccion;

    private final ObservableList<Persona> listaPersonas = FXCollections.observableArrayList();
    private FilteredList<Persona> listaFiltrada;
    private int idPersonaSeleccionada = -1;
    private int idMunicipio = 0;

    // ══════════════════════════════════════════════════════════════════════
    @FXML
    public void initialize() {
        cmbGenero.getItems().addAll("Masculino", "Femenino", "Otro");

        // Toggle Cliente / Empleado
        rbEmpleado.selectedProperty().addListener((obs, o, n) -> {
            vboxEmpleado.setVisible(n);
            vboxEmpleado.setManaged(n);
            vboxCliente.setVisible(!n);
            vboxCliente.setManaged(!n);
        });

        // Columnas tabla
        colId.setCellValueFactory(c -> c.getValue().idProperty());
        colNombre.setCellValueFactory(c -> c.getValue().nombreProperty());
        colApellido.setCellValueFactory(c -> c.getValue().apellidoProperty());
        colGenero.setCellValueFactory(c -> c.getValue().generoProperty());
        colTelefono.setCellValueFactory(c -> c.getValue().telefonoProperty());
        colCorreo.setCellValueFactory(c -> c.getValue().correoProperty());
        colDireccion.setCellValueFactory(c -> c.getValue().direccionProperty());

        // Filtro búsqueda
        listaFiltrada = new FilteredList<>(listaPersonas, p -> true);
        tablaPersonas.setItems(listaFiltrada);
        txtBuscar.textProperty().addListener((obs, o, n) ->
                listaFiltrada.setPredicate(p -> {
                    if (n == null || n.isBlank()) return true;
                    String lower = n.toLowerCase();
                    return p.getNombre().toLowerCase().contains(lower)
                            || p.getApellido().toLowerCase().contains(lower)
                            || p.getCorreo().toLowerCase().contains(lower);
                })
        );

        // Clic en fila
        tablaPersonas.getSelectionModel().selectedItemProperty().addListener(
                (obs, o, n) -> { if (n != null) cargarEnFormulario(n); });

        // Combos dirección encadenados
        cmbRegion.setOnAction(e -> cargarProvinciasPorRegion());
        cmbProvincia.setOnAction(e -> cargarMunicipiosPorProvincia());
        cmbMunicipio.setOnAction(e -> {
            if (cmbMunicipio.getValue() != null)
                idMunicipio = Integer.parseInt(cmbMunicipio.getValue().split(" - ")[0]);
        });

        // Aseguradora encadena seguro
        cmbAseguradora.setOnAction(e -> cargarSegurosPorAseguradora());

        // Cargar datos iniciales
        cargarCombo("SELECT id_region, nombre FROM TBL_REGION ORDER BY nombre", cmbRegion);
        cargarCombo("SELECT id_cargo, nombre FROM TBL_CARGO ORDER BY nombre", cmbCargo);
        cargarCombo("SELECT id_aseguradora, nombre FROM TBL_ASEGURADORA WHERE estado=1 ORDER BY nombre", cmbAseguradora);
        cargarPersonas();
    }

    // ── Utilidad: cargar cualquier combo desde una query ──────────────────
    private void cargarCombo(String sql, ComboBox<String> combo) {
        combo.getItems().clear();
        try (Connection con = conexion.establecerConexion();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next())
                combo.getItems().add(rs.getInt(1) + " - " + rs.getString(2));
        } catch (SQLException e) {
            System.err.println("Error cargando combo: " + e.getMessage());
        }
    }

    private void cargarProvinciasPorRegion() {
        cmbProvincia.getItems().clear();
        cmbMunicipio.getItems().clear();
        if (cmbRegion.getValue() == null) return;
        int id = Integer.parseInt(cmbRegion.getValue().split(" - ")[0]);
        cargarComboFiltrado(
                "SELECT id_provincia, nombre FROM TBL_PROVINCIA WHERE id_region=? ORDER BY nombre",
                id, cmbProvincia);
    }

    private void cargarMunicipiosPorProvincia() {
        cmbMunicipio.getItems().clear();
        if (cmbProvincia.getValue() == null) return;
        int id = Integer.parseInt(cmbProvincia.getValue().split(" - ")[0]);
        cargarComboFiltrado(
                "SELECT id_municipio, nombre FROM TBL_MUNICIPIO WHERE id_provincia=? ORDER BY nombre",
                id, cmbMunicipio);
    }

    private void cargarSegurosPorAseguradora() {
        cmbSeguro.getItems().clear();
        if (cmbAseguradora.getValue() == null) return;
        int id = Integer.parseInt(cmbAseguradora.getValue().split(" - ")[0]);
        cargarComboFiltrado(
                "SELECT id_seguro, nombre_seguro FROM TBL_SEGURO_MEDICO WHERE id_aseguradora=? ORDER BY nombre_seguro",
                id, cmbSeguro);
    }

    private void cargarComboFiltrado(String sql, int parentId, ComboBox<String> combo) {
        combo.getItems().clear();
        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, parentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                combo.getItems().add(rs.getInt(1) + " - " + rs.getString(2));
        } catch (SQLException e) {
            System.err.println("Error cargando combo filtrado: " + e.getMessage());
        }
    }

    // ── Cargar tabla ──────────────────────────────────────────────────────
    @FXML
    public void cargarPersonas() {
        listaPersonas.clear();
        idPersonaSeleccionada = -1;
        String sql = """
                SELECT p.id_persona, p.nombre, p.apellido, p.genero,
                       p.numero_telefono, p.correo_electronico,
                       ISNULL(d.descripcion,'')  AS descripcion,
                       ISNULL(ca.nombre,'')      AS calle,
                       ISNULL(se.nombre,'')      AS sector,
                       ISNULL(mu.nombre,'')      AS municipio,
                       ISNULL(pr.nombre,'')      AS provincia
                FROM TBL_PERSONA p
                LEFT JOIN TBL_DIRECCION          d  ON d.id_persona    = p.id_persona
                LEFT JOIN TBL_CALLE              ca ON ca.id_calle     = d.id_calle
                LEFT JOIN TBL_SECTOR             se ON se.id_sector    = ca.id_sector
                LEFT JOIN TBL_DISTRITO_MUNICIPAL dm ON dm.id_dm        = se.id_dm
                LEFT JOIN TBL_MUNICIPIO          mu ON mu.id_municipio = dm.id_municipio
                LEFT JOIN TBL_PROVINCIA          pr ON pr.id_provincia = mu.id_provincia
                ORDER BY p.id_persona DESC
                """;
        try (Connection con = conexion.establecerConexion();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                String dir = rs.getString("descripcion").isBlank() ? "" :
                        rs.getString("descripcion") + ", " + rs.getString("calle")
                                + ", " + rs.getString("sector") + ", "
                                + rs.getString("municipio") + ", " + rs.getString("provincia");
                listaPersonas.add(new Persona(
                        rs.getInt("id_persona"), rs.getString("nombre"),
                        rs.getString("apellido"), rs.getString("genero"),
                        rs.getString("numero_telefono"), rs.getString("correo_electronico"), dir));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al cargar: " + e.getMessage());
        }
    }

    // ── Guardar ───────────────────────────────────────────────────────────
    @FXML
    public void onGuardarPersona(ActionEvent event) {
        if (!validarFormulario()) return;
        try (Connection con = conexion.establecerConexion()) {

            // 1. Insertar persona
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO TBL_PERSONA (nombre,apellido,genero,numero_telefono,correo_electronico) VALUES(?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, txtNombre.getText().trim());
            ps.setString(2, txtApellido.getText().trim());
            ps.setString(3, cmbGenero.getValue());
            ps.setString(4, txtTelefono.getText().trim());
            ps.setString(5, txtCorreo.getText().trim());
            ps.executeUpdate();
            int idPersona = -1;
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) idPersona = keys.getInt(1);
            if (idPersona == -1) return;

            // 2. Insertar en rol correspondiente
            if (rbCliente.isSelected()) {
                if (cmbSeguro.getValue() != null) {
                    int idSeguro = Integer.parseInt(cmbSeguro.getValue().split(" - ")[0]);
                    PreparedStatement psCli = con.prepareStatement(
                            "INSERT INTO TBL_CLIENTE (id_persona, id_seguro) VALUES (?,?)");
                    psCli.setInt(1, idPersona);
                    psCli.setInt(2, idSeguro);
                    psCli.executeUpdate();
                } else {
                    PreparedStatement psCli = con.prepareStatement(
                            "INSERT INTO TBL_CLIENTE (id_persona) VALUES (?)");
                    psCli.setInt(1, idPersona);
                    psCli.executeUpdate();
                }
            } else {
                if (cmbCargo.getValue() == null || dpHorario.getValue() == null) {
                    JOptionPane.showMessageDialog(null, "Selecciona cargo y fecha de contratación.");
                    return;
                }
                int idCargo = Integer.parseInt(cmbCargo.getValue().split(" - ")[0]);
                PreparedStatement psEmp = con.prepareStatement(
                        "INSERT INTO TBL_EMPLEADO (horario_trabajo, id_persona, id_cargo) VALUES (?,?,?)");
                psEmp.setDate(1, Date.valueOf(dpHorario.getValue()));
                psEmp.setInt(2, idPersona);
                psEmp.setInt(3, idCargo);
                psEmp.executeUpdate();
            }

            // 3. Dirección (opcional)
            if (idMunicipio > 0 && !txtSector.getText().isBlank() && !txtCalle.getText().isBlank()) {
                int idCalle = obtenerOCrearCalle(con,
                        txtSector.getText().trim(), txtCalle.getText().trim(), idMunicipio);
                if (idCalle > 0) insertarDireccion(con, idPersona, idCalle,
                        txtDescripcionDireccion.getText().trim());
            }

            JOptionPane.showMessageDialog(null, "✔ " + (rbCliente.isSelected() ? "Cliente" : "Empleado") + " registrado.");
            limpiar();
            cargarPersonas();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
        }
    }

    // ── Editar ────────────────────────────────────────────────────────────
    @FXML
    public void onEditarPersona(ActionEvent event) {
        if (idPersonaSeleccionada == -1) {
            JOptionPane.showMessageDialog(null, "Selecciona una persona de la tabla primero."); return;
        }
        if (!validarFormulario()) return;
        try (Connection con = conexion.establecerConexion()) {

            // 1. Actualizar TBL_PERSONA
            PreparedStatement ps = con.prepareStatement(
                    "UPDATE TBL_PERSONA SET nombre=?,apellido=?,genero=?,numero_telefono=?,correo_electronico=? WHERE id_persona=?");
            ps.setString(1, txtNombre.getText().trim());
            ps.setString(2, txtApellido.getText().trim());
            ps.setString(3, cmbGenero.getValue());
            ps.setString(4, txtTelefono.getText().trim());
            ps.setString(5, txtCorreo.getText().trim());
            ps.setInt(6, idPersonaSeleccionada);
            ps.executeUpdate();

            // 2. Actualizar seguro en TBL_CLIENTE si es cliente
            if (rbCliente.isSelected()) {
                if (cmbSeguro.getValue() != null) {
                    int idSeguro = Integer.parseInt(cmbSeguro.getValue().split(" - ")[0]);
                    PreparedStatement psCli = con.prepareStatement(
                            "UPDATE TBL_CLIENTE SET id_seguro=? WHERE id_persona=?");
                    psCli.setInt(1, idSeguro);
                    psCli.setInt(2, idPersonaSeleccionada);
                    psCli.executeUpdate();
                } else {
                    PreparedStatement psCli = con.prepareStatement(
                            "UPDATE TBL_CLIENTE SET id_seguro=NULL WHERE id_persona=?");
                    psCli.setInt(1, idPersonaSeleccionada);
                    psCli.executeUpdate();
                }
            }

            // 3. Actualizar dirección si hay datos
            if (idMunicipio > 0 && !txtSector.getText().isBlank() && !txtCalle.getText().isBlank()) {
                int idCalle = obtenerOCrearCalle(con,
                        txtSector.getText().trim(), txtCalle.getText().trim(), idMunicipio);
                if (idCalle > 0) {
                    PreparedStatement psCheck = con.prepareStatement(
                            "SELECT id_direccion FROM TBL_DIRECCION WHERE id_persona=?");
                    psCheck.setInt(1, idPersonaSeleccionada);
                    ResultSet rs = psCheck.executeQuery();
                    if (rs.next()) {
                        PreparedStatement psUpd = con.prepareStatement(
                                "UPDATE TBL_DIRECCION SET descripcion=?, id_calle=? WHERE id_direccion=?");
                        psUpd.setString(1, txtDescripcionDireccion.getText().trim());
                        psUpd.setInt(2, idCalle);
                        psUpd.setInt(3, rs.getInt("id_direccion"));
                        psUpd.executeUpdate();
                    } else {
                        insertarDireccion(con, idPersonaSeleccionada, idCalle,
                                txtDescripcionDireccion.getText().trim());
                    }
                }
            }

            JOptionPane.showMessageDialog(null, "✔ Persona actualizada.");
            limpiar(); cargarPersonas();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
        }
    }

    // ── Eliminar ──────────────────────────────────────────────────────────
    @FXML
    public void onEliminarPersona(ActionEvent event) {
        if (idPersonaSeleccionada == -1) {
            JOptionPane.showMessageDialog(null, "Selecciona una persona primero."); return;
        }
        int ok = JOptionPane.showConfirmDialog(null,
                "¿Eliminar persona #" + idPersonaSeleccionada + "?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;
        try (Connection con = conexion.establecerConexion()) {
            for (String q : new String[]{
                    "DELETE FROM TBL_DIRECCION WHERE id_persona=" + idPersonaSeleccionada,
                    "DELETE FROM TBL_CLIENTE   WHERE id_persona=" + idPersonaSeleccionada,
                    "DELETE FROM TBL_EMPLEADO  WHERE id_persona=" + idPersonaSeleccionada,
                    "DELETE FROM TBL_PERSONA   WHERE id_persona=" + idPersonaSeleccionada})
                con.prepareStatement(q).executeUpdate();
            JOptionPane.showMessageDialog(null, "Persona eliminada.");
            limpiar(); cargarPersonas();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
        }
    }

    // ── Limpiar ───────────────────────────────────────────────────────────
    @FXML
    public void limpiar() {
        txtNombre.clear(); txtApellido.clear(); txtTelefono.clear();
        txtCorreo.clear(); txtSector.clear(); txtCalle.clear();
        txtDescripcionDireccion.clear(); txtBuscar.clear();
        cmbGenero.setValue(null); cmbCargo.setValue(null);
        dpHorario.setValue(null); cmbAseguradora.setValue(null);
        cmbSeguro.getItems().clear(); cmbRegion.setValue(null);
        cmbProvincia.getItems().clear(); cmbMunicipio.getItems().clear();
        rbCliente.setSelected(true);
        vboxEmpleado.setVisible(false); vboxEmpleado.setManaged(false);
        vboxCliente.setVisible(true);   vboxCliente.setManaged(true);
        idPersonaSeleccionada = -1; idMunicipio = 0;
        tablaPersonas.getSelectionModel().clearSelection();
    }

    // ── Helpers dirección ─────────────────────────────────────────────────
    private int obtenerOCrearCalle(Connection con, String sector, String calle, int idMun) throws SQLException {
        int idDm = obtenerOCrear(con,
                "SELECT id_dm FROM TBL_DISTRITO_MUNICIPAL WHERE id_municipio=? AND nombre=?",
                "INSERT INTO TBL_DISTRITO_MUNICIPAL (nombre,id_municipio) VALUES(?,?)", idMun, "Área General");
        int idSec = obtenerOCrear(con,
                "SELECT id_sector FROM TBL_SECTOR WHERE id_dm=? AND nombre=?",
                "INSERT INTO TBL_SECTOR (nombre,id_dm) VALUES(?,?)", idDm, sector);
        return obtenerOCrear(con,
                "SELECT id_calle FROM TBL_CALLE WHERE id_sector=? AND nombre=?",
                "INSERT INTO TBL_CALLE (nombre,id_sector) VALUES(?,?)", idSec, calle);
    }

    private int obtenerOCrear(Connection con, String sel, String ins, int pid, String nombre) throws SQLException {
        PreparedStatement ps = con.prepareStatement(sel);
        ps.setInt(1, pid); ps.setString(2, nombre);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) return rs.getInt(1);
        PreparedStatement psI = con.prepareStatement(ins, Statement.RETURN_GENERATED_KEYS);
        psI.setString(1, nombre); psI.setInt(2, pid);
        psI.executeUpdate();
        ResultSet k = psI.getGeneratedKeys();
        return k.next() ? k.getInt(1) : -1;
    }

    private void insertarDireccion(Connection con, int idPersona, int idCalle, String desc) throws SQLException {
        PreparedStatement ps = con.prepareStatement(
                "INSERT INTO TBL_DIRECCION (descripcion,id_calle,id_persona) VALUES(?,?,?)");
        ps.setString(1, desc.isBlank() ? "-" : desc);
        ps.setInt(2, idCalle); ps.setInt(3, idPersona);
        ps.executeUpdate();
    }

    private void cargarEnFormulario(Persona p) {
        idPersonaSeleccionada = p.getId();
        txtNombre.setText(p.getNombre());
        txtApellido.setText(p.getApellido());
        cmbGenero.setValue(p.getGenero());
        txtTelefono.setText(p.getTelefono());
        txtCorreo.setText(p.getCorreo());

        // Cargar datos adicionales desde BD
        try (Connection con = conexion.establecerConexion()) {

            // ¿Es cliente o empleado?
            PreparedStatement psCli = con.prepareStatement(
                    "SELECT id_seguro FROM TBL_CLIENTE WHERE id_persona=?");
            psCli.setInt(1, p.getId());
            ResultSet rsCli = psCli.executeQuery();
            if (rsCli.next()) {
                rbCliente.setSelected(true);
                // Cargar seguro si tiene
                int idSeguro = rsCli.getInt("id_seguro");
                if (!rsCli.wasNull() && idSeguro > 0) {
                    // Buscar aseguradora del seguro para encadenar el combo
                    PreparedStatement psSeg = con.prepareStatement(
                            "SELECT sm.id_seguro, sm.nombre_seguro, sm.id_aseguradora, a.nombre " +
                                    "FROM TBL_SEGURO_MEDICO sm " +
                                    "JOIN TBL_ASEGURADORA a ON a.id_aseguradora = sm.id_aseguradora " +
                                    "WHERE sm.id_seguro=?");
                    psSeg.setInt(1, idSeguro);
                    ResultSet rsSeg = psSeg.executeQuery();
                    if (rsSeg.next()) {
                        int idAseg = rsSeg.getInt("id_aseguradora");
                        String nombreAseg = rsSeg.getString("nombre");
                        // Seleccionar aseguradora en el combo
                        for (String item : cmbAseguradora.getItems()) {
                            if (item.startsWith(idAseg + " - ")) {
                                cmbAseguradora.setValue(item);
                                break;
                            }
                        }
                        // Cargar seguros de esa aseguradora y seleccionar el correcto
                        cargarSegurosPorAseguradora();
                        for (String item : cmbSeguro.getItems()) {
                            if (item.startsWith(idSeguro + " - ")) {
                                cmbSeguro.setValue(item);
                                break;
                            }
                        }
                    }
                }
            } else {
                // Es empleado
                rbEmpleado.setSelected(true);
                PreparedStatement psEmp = con.prepareStatement(
                        "SELECT e.id_cargo, c.nombre FROM TBL_EMPLEADO e " +
                                "JOIN TBL_CARGO c ON c.id_cargo = e.id_cargo WHERE e.id_persona=?");
                psEmp.setInt(1, p.getId());
                ResultSet rsEmp = psEmp.executeQuery();
                if (rsEmp.next()) {
                    int idCargo = rsEmp.getInt("id_cargo");
                    for (String item : cmbCargo.getItems()) {
                        if (item.startsWith(idCargo + " - ")) {
                            cmbCargo.setValue(item);
                            break;
                        }
                    }
                }
            }

            // Cargar dirección si tiene
            PreparedStatement psDir = con.prepareStatement(
                    "SELECT d.descripcion, ca.nombre AS calle, se.nombre AS sector, " +
                            "mu.id_municipio, mu.nombre AS municipio, " +
                            "pr.id_provincia, pr.nombre AS provincia, " +
                            "re.id_region, re.nombre AS region " +
                            "FROM TBL_DIRECCION d " +
                            "JOIN TBL_CALLE ca ON ca.id_calle = d.id_calle " +
                            "JOIN TBL_SECTOR se ON se.id_sector = ca.id_sector " +
                            "JOIN TBL_DISTRITO_MUNICIPAL dm ON dm.id_dm = se.id_dm " +
                            "JOIN TBL_MUNICIPIO mu ON mu.id_municipio = dm.id_municipio " +
                            "JOIN TBL_PROVINCIA pr ON pr.id_provincia = mu.id_provincia " +
                            "JOIN TBL_REGION re ON re.id_region = pr.id_region " +
                            "WHERE d.id_persona=?");
            psDir.setInt(1, p.getId());
            ResultSet rsDir = psDir.executeQuery();
            if (rsDir.next()) {
                txtDescripcionDireccion.setText(rsDir.getString("descripcion"));
                txtSector.setText(rsDir.getString("sector"));
                txtCalle.setText(rsDir.getString("calle"));
                idMunicipio = rsDir.getInt("id_municipio");

                // Seleccionar región → carga provincias → seleccionar provincia → carga municipios → seleccionar municipio
                int idRegion = rsDir.getInt("id_region");
                int idProvincia = rsDir.getInt("id_provincia");
                for (String item : cmbRegion.getItems()) {
                    if (item.startsWith(idRegion + " - ")) { cmbRegion.setValue(item); break; }
                }
                cargarProvinciasPorRegion();
                for (String item : cmbProvincia.getItems()) {
                    if (item.startsWith(idProvincia + " - ")) { cmbProvincia.setValue(item); break; }
                }
                cargarMunicipiosPorProvincia();
                for (String item : cmbMunicipio.getItems()) {
                    if (item.startsWith(idMunicipio + " - ")) { cmbMunicipio.setValue(item); break; }
                }
            }

        } catch (SQLException e) {
            System.err.println("Error cargando formulario: " + e.getMessage());
        }
    }

    private boolean validarFormulario() {
        if (txtNombre.getText().isBlank()) {
            JOptionPane.showMessageDialog(null, "El nombre es obligatorio."); return false; }
        if (txtApellido.getText().isBlank()) {
            JOptionPane.showMessageDialog(null, "El apellido es obligatorio."); return false; }
        if (cmbGenero.getValue() == null) {
            JOptionPane.showMessageDialog(null, "Selecciona el género."); return false; }
        return true;
    }
}