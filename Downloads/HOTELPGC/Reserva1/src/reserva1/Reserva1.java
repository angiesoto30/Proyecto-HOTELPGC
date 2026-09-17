
package reserva1;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class Reserva1 {

    private int idReserva;
    private Cliente cliente;
    private Habitacion habitacion;
    private String fechaEntrada;
    private String fechaSalida;
    private String estado;

    public Reserva1(int idReserva, Cliente cliente, Habitacion habitacion,
                    String fechaEntrada, String fechaSalida, String estado) {
        this.idReserva = idReserva;
        this.cliente = cliente;
        this.habitacion = habitacion;
        this.fechaEntrada = fechaEntrada;
        this.fechaSalida = fechaSalida;
        this.estado = estado;
    }

    public int getIdReserva() {
        return idReserva;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public Habitacion getHabitacion() {
        return habitacion;
    }

    public void mostrarInfo() {
        System.out.println("Reserva #" + idReserva);
        System.out.println("Cliente: " + cliente.getNombre() + " (Cedula: " + cliente.getCedula() + ")");
        System.out.println("Habitacion: " + habitacion.getNumero() + " - " + habitacion.getTipo());
        System.out.println("Entrada: " + fechaEntrada + " | Salida: " + fechaSalida);
        System.out.println("Estado: " + estado);
    }

    
    public static Reserva1 crear(Cliente cliente, Habitacion habitacion, String entrada, String salida) {

        String sql = "INSERT INTO Reservas (id_cliente, id_habitacion, fecha_entrada, fecha_salida, estado) "
                   + "VALUES (?, ?, ?, ?, 'Confirmada')";

        try (Connection con = Main.conectar();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, cliente.getIdCliente());
            ps.setInt(2, habitacion.getIdHabitacion());
            ps.setDate(3, Date.valueOf(entrada));
            ps.setDate(4, Date.valueOf(salida));
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    habitacion.setDisponible(false);
                    return new Reserva1(id, cliente, habitacion, entrada, salida, "Confirmada");
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al crear la reserva: " + e.getMessage());
        }

        return null;
    }
    public static void generarReporteVentas(String fechaInicio, String fechaFin) {

    String sqlMes = "SELECT " +
                    "YEAR(r.fecha_entrada) AS anio, " +
                    "MONTH(r.fecha_entrada) AS mes, " +
                    "SUM(h.precio * DATEDIFF(DAY, r.fecha_entrada, r.fecha_salida)) AS ingresos " +
                    "FROM Reservas r " +
                    "INNER JOIN Habitaciones h ON r.id_habitacion = h.id_habitacion " +
                    "WHERE r.fecha_entrada BETWEEN ? AND ? " +
                    "AND r.estado = 'Confirmada' " +
                    "GROUP BY YEAR(r.fecha_entrada), MONTH(r.fecha_entrada) " +
                    "ORDER BY anio, mes";

    String sqlTipo = "SELECT " +
                     "h.tipo, " +
                     "COUNT(*) AS cantidad_reservas " +
                     "FROM Reservas r " +
                     "INNER JOIN Habitaciones h ON r.id_habitacion = h.id_habitacion " +
                     "WHERE r.fecha_entrada BETWEEN ? AND ? " +
                     "AND r.estado = 'Confirmada' " +
                     "GROUP BY h.tipo";

    String sqlMayor = "SELECT TOP 1 " +
                      "YEAR(r.fecha_entrada) AS anio, " +
                      "MONTH(r.fecha_entrada) AS mes, " +
                      "SUM(h.precio * DATEDIFF(DAY, r.fecha_entrada, r.fecha_salida)) AS ingresos " +
                      "FROM Reservas r " +
                      "INNER JOIN Habitaciones h ON r.id_habitacion = h.id_habitacion " +
                      "WHERE r.fecha_entrada BETWEEN ? AND ? " +
                      "AND r.estado = 'Confirmada' " +
                      "GROUP BY YEAR(r.fecha_entrada), MONTH(r.fecha_entrada) " +
                      "ORDER BY ingresos DESC";

    try (Connection con = Main.conectar()) {

        System.out.println("\n---- INGRESOS POR MES ----");

        try (PreparedStatement ps = con.prepareStatement(sqlMes)) {

            ps.setDate(1, Date.valueOf(fechaInicio));
            ps.setDate(2, Date.valueOf(fechaFin));

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                System.out.println(
                    "Año: " + rs.getInt("anio") +
                    " | Mes: " + rs.getInt("mes") +
                    " | Ingresos: $" + rs.getDouble("ingresos")
                );
            }
        }

       
        System.out.println("\n---- RESERVAS POR TIPO DE HABITACIÓN ----");

        try (PreparedStatement ps = con.prepareStatement(sqlTipo)) {

            ps.setDate(1, Date.valueOf(fechaInicio));
            ps.setDate(2, Date.valueOf(fechaFin));

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                System.out.println(
                    "Tipo: " + rs.getString("tipo") +
                    " | Reservas: " + rs.getInt("cantidad_reservas")
                );
            }
        }

      
        System.out.println("\n---- MES CON MÁS INGRESOS ----");

        try (PreparedStatement ps = con.prepareStatement(sqlMayor)) {

            ps.setDate(1, Date.valueOf(fechaInicio));
            ps.setDate(2, Date.valueOf(fechaFin));

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                System.out.println(
                    "Año: " + rs.getInt("anio") +
                    " | Mes: " + rs.getInt("mes") +
                    " | Ingresos: $" + rs.getDouble("ingresos")
                );
            }
        }

    } catch (SQLException e) {
        System.out.println("Error al generar el reporte: " + e.getMessage());
    }
}
    }
