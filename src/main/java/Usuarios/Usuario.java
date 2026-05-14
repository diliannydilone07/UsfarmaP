package Usuarios;

import javafx.beans.property.*;

public class Usuario {

    private final SimpleIntegerProperty idCredencial = new SimpleIntegerProperty();
    private final SimpleStringProperty  usuario      = new SimpleStringProperty();
    private final SimpleStringProperty  rol          = new SimpleStringProperty();
    private final SimpleBooleanProperty estado       = new SimpleBooleanProperty();
    private final SimpleIntegerProperty idEmpleado   = new SimpleIntegerProperty();
    private final SimpleStringProperty  nombreCompleto = new SimpleStringProperty();

    public Usuario() {}

    public Usuario(int idCredencial, String usuario, String rol,
                   boolean estado, int idEmpleado, String nombreCompleto) {
        this.idCredencial.set(idCredencial);
        this.usuario.set(usuario != null ? usuario : "");
        this.rol.set(rol != null ? rol : "");
        this.estado.set(estado);
        this.idEmpleado.set(idEmpleado);
        this.nombreCompleto.set(nombreCompleto != null ? nombreCompleto : "");
    }

    public SimpleIntegerProperty idCredencialProperty()   { return idCredencial; }
    public SimpleStringProperty  usuarioProperty()        { return usuario; }
    public SimpleStringProperty  rolProperty()            { return rol; }
    public SimpleBooleanProperty estadoProperty()         { return estado; }
    public SimpleIntegerProperty idEmpleadoProperty()     { return idEmpleado; }
    public SimpleStringProperty  nombreCompletoProperty() { return nombreCompleto; }

    public int     getIdCredencial()              { return idCredencial.get(); }
    public void    setIdCredencial(int v)         { idCredencial.set(v); }
    public String  getUsuario()                   { return usuario.get(); }
    public void    setUsuario(String v)           { usuario.set(v); }
    public String  getRol()                       { return rol.get(); }
    public void    setRol(String v)               { rol.set(v); }
    public boolean isEstado()                     { return estado.get(); }
    public void    setEstado(boolean v)           { estado.set(v); }
    public int     getIdEmpleado()                { return idEmpleado.get(); }
    public void    setIdEmpleado(int v)           { idEmpleado.set(v); }
    public String  getNombreCompleto()            { return nombreCompleto.get(); }
    public void    setNombreCompleto(String v)    { nombreCompleto.set(v); }
}