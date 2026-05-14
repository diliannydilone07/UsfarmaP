package Usuarios;


public class SesionUsuario {

    private static SesionUsuario instancia;
    private Usuario usuarioActual;

    private SesionUsuario() {}

    public static SesionUsuario getInstance() {
        if (instancia == null) instancia = new SesionUsuario();
        return instancia;
    }

    public Usuario getUsuarioActual()          { return usuarioActual; }
    public void    setUsuarioActual(Usuario u) { this.usuarioActual = u; }

    public boolean isAdmin() {
        return usuarioActual != null && "ADMIN".equalsIgnoreCase(usuarioActual.getRol());
    }

    public void cerrarSesion() { usuarioActual = null; }
}