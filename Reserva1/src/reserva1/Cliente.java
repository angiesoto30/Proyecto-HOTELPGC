package reserva1;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class Cliente {

    private int idCliente;
    private String nombre;
    private String cedula;

    public Cliente(int idCliente, String nombre, String cedula) {
        this.idCliente = idCliente;
        this.nombre = nombre;
        this.cedula = cedula;
    }

    public int getIdCliente() {
        return idCliente;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCedula() {
        return cedula;
    }

    
    public static Cliente buscarOCrear(String nombre, String cedula) {

        String buscar = "SELECT id_cliente FROM Clientes WHERE cedula = ?";
        String insertar = "INSERT INTO Clientes (nombre, cedula) VALUES (?, ?)";

        try (Connection con = Main.conectar()) {

            try (PreparedStatement ps = con.prepareStatement(buscar)) {
                ps.setString(1, cedula);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new Cliente(rs.getInt("id_cliente"), nombre, cedula);
                    }
                }
            }

            try (PreparedStatement ps = con.prepareStatement(insertar, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, nombre);
                ps.setString(2, cedula);
                ps.executeUpdate();

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        return new Cliente(keys.getInt(1), nombre, cedula);
                    }
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al buscar/crear cliente: " + e.getMessage());
        }

        return null;
    }
}