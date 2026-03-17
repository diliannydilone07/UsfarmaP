package com.example.farmaventa;

import com.example.farmaventa.database.Conexion;
import com.example.farmaventa.modelo.Persona;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import javax.swing.JOptionPane;
import java.sql.*;

/**
 * PersonaController — CRUD completo de personas CON dirección.
 *
 * La dirección sigue el árbol:
 *   TBL_REGION → TBL_PAIS → TBL_PROVINCIA → TBL_MUNICIPIO
 *   → TBL_DISTRITO_MUNICIPAL → TBL_SECTOR → TBL_CALLE → TBL_DIRECCION
 *
 * Al guardar/editar una persona se inserta/actualiza también su dirección.
 */
public class PersonaController {

    Conexion conexion = new Conexion();

    // ── Datos personales ──────────────────────────────────────────────────
    @FXML private TextField        txtNombre;
    @FXML private TextField        txtApellido;
    @FXML private ComboBox<String> cmbGenero;
    @FXML private TextField        txtTelefono;
    @FXML private TextField        txtCorreo;

    // ── Dirección — ComboBoxes encadenados ────────────────────────────────
    @FXML private ComboBox<String> cmbRegion;
    @FXML private ComboBox<String> cmbPais;
    @FXML private ComboBox<String> cmbProvincia;
    @FXML private ComboBox<String> cmbMunicipio;
    @FXML private ComboBox<String> cmbDistrito;
    @FXML private ComboBox<String> cmbSector;
    @FXML private ComboBox<String> cmbCalle;
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

    // ── Datos internos ────────────────────────────────────────────────────
    private final ObservableList<Persona> listaPersonas = FXCollections.observableArrayList();
    private FilteredList<Persona>         listaFiltrada;
    private int idPersonaSeleccionada = -1;

    // IDs seleccionados en los combos de dirección
    private int idRegion, idPais, idProvincia, idMunicipio, idDistrito, idSector, idCalle;

    // ══════════════════════════════════════════════════════════════════════
    @FXML
    public void initialize() {
        cmbGenero.getItems().addAll("Masculino", "Femenino", "Otro");

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
                            || p.getCorreo().toLowerCase().contains(lower)
                            || p.getTelefono().toLowerCase().contains(lower);
                })
        );

        // Clic en fila
        tablaPersonas.getSelectionModel().selectedItemProperty().addListener(
                (obs, o, n) -> { if (n != null) cargarEnFormulario(n); });

        // Encadenar combos de dirección
        cmbRegion.setOnAction(e -> cargarPaisesPorRegion());
        cmbPais.setOnAction(e -> cargarProvinciasPorPaisRegion());
        cmbProvincia.setOnAction(e -> cargarMunicipiosPorProvincia());
        cmbMunicipio.setOnAction(e -> cargarDistritosPorMunicipio());
        cmbDistrito.setOnAction(e -> cargarSectoresPorDistrito());
        cmbSector.setOnAction(e -> cargarCallesPorSector());
        cmbCalle.setOnAction(e -> actualizarIdCalle());

        cargarRegiones();
        cargarPersonas();
    }

    // ══════  CARGA DE COMBOS DE DIRECCIÓN  ═══════════════════════════════

    private void cargarRegiones() {
        cmbRegion.getItems().clear();
        try (Connection con = conexion.establecerConexion();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT id_region, nombre FROM TBL_REGION ORDER BY nombre")) {
            while (rs.next())
                cmbRegion.getItems().add(rs.getInt("id_region") + " - " + rs.getString("nombre"));
        } catch (SQLException e) { System.err.println("Error regiones: " + e.getMessage()); }
    }

    private void cargarPaisesPorRegion() {
        cmbPais.getItems().clear();
        cmbProvincia.getItems().clear(); cmbMunicipio.getItems().clear();
        cmbDistrito.getItems().clear();  cmbSector.getItems().clear();
        cmbCalle.getItems().clear();
        if (cmbRegion.getValue() == null) return;
        idRegion = Integer.parseInt(cmbRegion.getValue().split(" - ")[0]);
        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT id_pais, nombre FROM TBL_PAIS ORDER BY nombre")) {
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                cmbPais.getItems().add(rs.getInt("id_pais") + " - " + rs.getString("nombre"));
        } catch (SQLException e) { System.err.println("Error países: " + e.getMessage()); }
    }

    private void cargarProvinciasPorPaisRegion() {
        cmbProvincia.getItems().clear(); cmbMunicipio.getItems().clear();
        cmbDistrito.getItems().clear();  cmbSector.getItems().clear();
        cmbCalle.getItems().clear();
        if (cmbPais.getValue() == null) return;
        idPais = Integer.parseInt(cmbPais.getValue().split(" - ")[0]);
        String sql = "SELECT id_provincia, nombre FROM TBL_PROVINCIA WHERE id_region=? AND id_pais=? ORDER BY nombre";
        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idRegion); ps.setInt(2, idPais);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                cmbProvincia.getItems().add(rs.getInt("id_provincia") + " - " + rs.getString("nombre"));
        } catch (SQLException e) { System.err.println("Error provincias: " + e.getMessage()); }
    }

    private void cargarMunicipiosPorProvincia() {
        cmbMunicipio.getItems().clear(); cmbDistrito.getItems().clear();
        cmbSector.getItems().clear();    cmbCalle.getItems().clear();
        if (cmbProvincia.getValue() == null) return;
        idProvincia = Integer.parseInt(cmbProvincia.getValue().split(" - ")[0]);
        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT id_municipio, nombre FROM TBL_MUNICIPIO WHERE id_provincia=? ORDER BY nombre")) {
            ps.setInt(1, idProvincia);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                cmbMunicipio.getItems().add(rs.getInt("id_municipio") + " - " + rs.getString("nombre"));
        } catch (SQLException e) { System.err.println("Error municipios: " + e.getMessage()); }
    }

    private void cargarDistritosPorMunicipio() {
        cmbDistrito.getItems().clear(); cmbSector.getItems().clear(); cmbCalle.getItems().clear();
        if (cmbMunicipio.getValue() == null) return;
        idMunicipio = Integer.parseInt(cmbMunicipio.getValue().split(" - ")[0]);
        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT id_dm, nombre FROM TBL_DISTRITO_MUNICIPAL WHERE id_municipio=? ORDER BY nombre")) {
            ps.setInt(1, idMunicipio);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                cmbDistrito.getItems().add(rs.getInt("id_dm") + " - " + rs.getString("nombre"));
        } catch (SQLException e) { System.err.println("Error distritos: " + e.getMessage()); }
    }

    private void cargarSectoresPorDistrito() {
        cmbSector.getItems().clear(); cmbCalle.getItems().clear();
        if (cmbDistrito.getValue() == null) return;
        idDistrito = Integer.parseInt(cmbDistrito.getValue().split(" - ")[0]);
        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT id_sector, nombre FROM TBL_SECTOR WHERE id_dm=? ORDER BY nombre")) {
            ps.setInt(1, idDistrito);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                cmbSector.getItems().add(rs.getInt("id_sector") + " - " + rs.getString("nombre"));
        } catch (SQLException e) { System.err.println("Error sectores: " + e.getMessage()); }
    }

    private void cargarCallesPorSector() {
        cmbCalle.getItems().clear();
        if (cmbSector.getValue() == null) return;
        idSector = Integer.parseInt(cmbSector.getValue().split(" - ")[0]);
        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT id_calle, nombre FROM TBL_CALLE WHERE id_sector=? ORDER BY nombre")) {
            ps.setInt(1, idSector);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                cmbCalle.getItems().add(rs.getInt("id_calle") + " - " + rs.getString("nombre"));
        } catch (SQLException e) { System.err.println("Error calles: " + e.getMessage()); }
    }

    private void actualizarIdCalle() {
        if (cmbCalle.getValue() == null) return;
        idCalle = Integer.parseInt(cmbCalle.getValue().split(" - ")[0]);
    }

    // ══════  CRUD PERSONA  ════════════════════════════════════════════════

    @FXML
    public void cargarPersonas() {
        listaPersonas.clear();
        idPersonaSeleccionada = -1;

        String sql = """
                SELECT p.id_persona, p.nombre, p.apellido, p.genero,
                       p.numero_telefono, p.correo_electronico,
                       ISNULL(d.descripcion, '') AS descripcion,
                       ISNULL(ca.nombre, '') AS calle,
                       ISNULL(se.nombre, '') AS sector,
                       ISNULL(dm.nombre, '') AS distrito,
                       ISNULL(mu.nombre, '') AS municipio,
                       ISNULL(pr.nombre, '') AS provincia,
                       ISNULL(re.nombre, '') AS region,
                       ISNULL(pa.nombre, '') AS pais,
                       d.id_direccion
                FROM TBL_PERSONA p
                LEFT JOIN TBL_DIRECCION          d  ON d.id_persona  = p.id_persona
                LEFT JOIN TBL_CALLE              ca ON ca.id_calle   = d.id_calle
                LEFT JOIN TBL_SECTOR             se ON se.id_sector  = ca.id_sector
                LEFT JOIN TBL_DISTRITO_MUNICIPAL dm ON dm.id_dm      = se.id_dm
                LEFT JOIN TBL_MUNICIPIO          mu ON mu.id_municipio = dm.id_municipio
                LEFT JOIN TBL_PROVINCIA          pr ON pr.id_provincia = mu.id_provincia
                LEFT JOIN TBL_REGION             re ON re.id_region  = pr.id_region
                LEFT JOIN TBL_PAIS               pa ON pa.id_pais    = pr.id_pais
                ORDER BY p.id_persona DESC
                """;

        try (Connection con = conexion.establecerConexion();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                // Construir dirección completa legible
                String dir = "";
                if (!rs.getString("descripcion").isBlank()) {
                    dir = rs.getString("descripcion")
                            + ", C/" + rs.getString("calle")
                            + ", " + rs.getString("sector")
                            + ", " + rs.getString("municipio")
                            + ", " + rs.getString("provincia");
                }
                listaPersonas.add(new Persona(
                        rs.getInt("id_persona"),
                        rs.getString("nombre"),
                        rs.getString("apellido"),
                        rs.getString("genero"),
                        rs.getString("numero_telefono"),
                        rs.getString("correo_electronico"),
                        dir
                ));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al cargar personas: " + e.getMessage());
        }
    }

    // ── Guardar nueva persona + dirección ─────────────────────────────────
    @FXML
    public void onGuardarPersona(ActionEvent event) {
        if (!validarFormulario()) return;

        String sqlPersona = """
                INSERT INTO TBL_PERSONA (nombre, apellido, genero, numero_telefono, correo_electronico)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sqlPersona, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, txtNombre.getText().trim());
            ps.setString(2, txtApellido.getText().trim());
            ps.setString(3, cmbGenero.getValue());
            ps.setString(4, txtTelefono.getText().trim());
            ps.setString(5, txtCorreo.getText().trim());
            ps.executeUpdate();

            int idPersona = -1;
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) idPersona = keys.getInt(1);

            // Insertar dirección si se seleccionó una calle
            if (idPersona != -1 && idCalle > 0 && !txtDescripcionDireccion.getText().isBlank()) {
                insertarDireccion(con, idPersona, idCalle, txtDescripcionDireccion.getText().trim());
            }

            JOptionPane.showMessageDialog(null, "✔ Persona registrada correctamente.");
            limpiar();
            cargarPersonas();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al guardar persona: " + e.getMessage());
        }
    }

    // ── Editar persona + dirección ────────────────────────────────────────
    @FXML
    public void onEditarPersona(ActionEvent event) {
        if (idPersonaSeleccionada == -1) {
            JOptionPane.showMessageDialog(null, "Selecciona una persona de la tabla primero.");
            return;
        }
        if (!validarFormulario()) return;

        String sql = """
                UPDATE TBL_PERSONA
                SET nombre=?, apellido=?, genero=?, numero_telefono=?, correo_electronico=?
                WHERE id_persona=?
                """;

        try (Connection con = conexion.establecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, txtNombre.getText().trim());
            ps.setString(2, txtApellido.getText().trim());
            ps.setString(3, cmbGenero.getValue());
            ps.setString(4, txtTelefono.getText().trim());
            ps.setString(5, txtCorreo.getText().trim());
            ps.setInt(6, idPersonaSeleccionada);
            ps.executeUpdate();

            // Actualizar o insertar dirección
            if (idCalle > 0 && !txtDescripcionDireccion.getText().isBlank()) {
                // Verificar si ya tiene dirección
                PreparedStatement psCheck = con.prepareStatement(
                        "SELECT id_direccion FROM TBL_DIRECCION WHERE id_persona=?");
                psCheck.setInt(1, idPersonaSeleccionada);
                ResultSet rs = psCheck.executeQuery();

                if (rs.next()) {
                    int idDir = rs.getInt("id_direccion");
                    PreparedStatement psUpd = con.prepareStatement(
                            "UPDATE TBL_DIRECCION SET descripcion=?, id_calle=? WHERE id_direccion=?");
                    psUpd.setString(1, txtDescripcionDireccion.getText().trim());
                    psUpd.setInt(2, idCalle);
                    psUpd.setInt(3, idDir);
                    psUpd.executeUpdate();
                } else {
                    insertarDireccion(con, idPersonaSeleccionada, idCalle,
                            txtDescripcionDireccion.getText().trim());
                }
            }

            JOptionPane.showMessageDialog(null, "✔ Persona actualizada correctamente.");
            limpiar();
            cargarPersonas();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al editar persona: " + e.getMessage());
        }
    }

    // ── Eliminar persona ──────────────────────────────────────────────────
    @FXML
    public void onEliminarPersona(ActionEvent event) {
        if (idPersonaSeleccionada == -1) {
            JOptionPane.showMessageDialog(null, "Selecciona una persona primero.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(null,
                "¿Eliminar la persona #" + idPersonaSeleccionada + " y su dirección?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection con = conexion.establecerConexion()) {
            // Eliminar dirección primero (FK)
            PreparedStatement psDir = con.prepareStatement(
                    "DELETE FROM TBL_DIRECCION WHERE id_persona=?");
            psDir.setInt(1, idPersonaSeleccionada);
            psDir.executeUpdate();

            PreparedStatement ps = con.prepareStatement(
                    "DELETE FROM TBL_PERSONA WHERE id_persona=?");
            ps.setInt(1, idPersonaSeleccionada);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(null, "Persona eliminada.");
            limpiar();
            cargarPersonas();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al eliminar: " + e.getMessage());
        }
    }

    // ── Limpiar ───────────────────────────────────────────────────────────
    @FXML
    public void limpiar() {
        txtNombre.clear(); txtApellido.clear(); txtTelefono.clear();
        txtCorreo.clear(); txtDescripcionDireccion.clear(); txtBuscar.clear();
        cmbGenero.setValue(null);
        cmbRegion.setValue(null); cmbPais.setValue(null);
        cmbProvincia.setValue(null); cmbMunicipio.setValue(null);
        cmbDistrito.setValue(null); cmbSector.setValue(null); cmbCalle.setValue(null);
        idPersonaSeleccionada = -1;
        idRegion = 0; idPais = 0; idProvincia = 0; idMunicipio = 0;
        idDistrito = 0; idSector = 0; idCalle = 0;
        tablaPersonas.getSelectionModel().clearSelection();
    }

    // ── Helpers ───────────────────────────────────────────────────────────
    private void insertarDireccion(Connection con, int idPersona, int idCalle, String descripcion)
            throws SQLException {
        PreparedStatement ps = con.prepareStatement(
                "INSERT INTO TBL_DIRECCION (descripcion, id_calle, id_persona) VALUES (?, ?, ?)");
        ps.setString(1, descripcion);
        ps.setInt(2, idCalle);
        ps.setInt(3, idPersona);
        ps.executeUpdate();
    }

    private void cargarEnFormulario(Persona p) {
        idPersonaSeleccionada = p.getId();
        txtNombre.setText(p.getNombre());
        txtApellido.setText(p.getApellido());
        cmbGenero.setValue(p.getGenero());
        txtTelefono.setText(p.getTelefono());
        txtCorreo.setText(p.getCorreo());
        // La dirección ya viene como texto legible en la tabla;
        // para editar campos individuales de dirección el usuario
        // usa los combos encadenados directamente.
        txtDescripcionDireccion.setText("");
    }

    private boolean validarFormulario() {
        if (txtNombre.getText().isBlank()) {
            JOptionPane.showMessageDialog(null, "El nombre es obligatorio."); return false;
        }
        if (txtApellido.getText().isBlank()) {
            JOptionPane.showMessageDialog(null, "El apellido es obligatorio."); return false;
        }
        if (cmbGenero.getValue() == null) {
            JOptionPane.showMessageDialog(null, "Selecciona el género."); return false;
        }
        return true;
    }
}