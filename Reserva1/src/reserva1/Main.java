package reserva1;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class Main {

    static Scanner sc = new Scanner(System.in);

    static int idHabitacionSeleccionada;
    static int numeroHabitacion;
    static String tipoHabitacion;
    static double precioHabitacion;

    static String nombre;
    static String cedula;
    static String correoCliente; 
    static int idCliente;

    static int idReserva;

    
    private static final String CORREO_REMITENTE = "HotelPGC2824@gmail.com";
    private static final String CLAVE_APP = "zugb ddde fyfg hany";
   

    private static final String URL =
        "jdbc:sqlserver://localhost:1433;databaseName=HotelReserva;encrypt=true;trustServerCertificate=true";
    private static final String USUARIO = "hotelapp";
    private static final String PASSWORD = "HotelApp2026!";

    public static Connection conectar() {
        try {
            return DriverManager.getConnection(URL, USUARIO, PASSWORD);
        } catch (SQLException e) {
            System.out.println(" Error al conectar a la base de datos: " + e.getMessage());
            return null;
        }
    }

    public static void main(String[] args) {

        while (true) {

            System.out.println("\n===== HOTEL =====");
            System.out.println("1. Reservar");
            System.out.println("2. Ver mis reservas ");
            System.out.println("3. Cancelar una reserva");
            System.out.println("4. Ver todas las reservas");
            System.out.println("5. Ver estado de habitaciones");
            System.out.println("0. Salir");
            System.out.print("Opcion: ");

            int op = leerNumero();

            if (op == 1) {
                seleccionarHabitacion();
                pedirDatos();
                crearReserva();
                restaurante();

            } else if (op == 2) {
                System.out.print("\nIngrese su cedula: ");
                String ced = sc.nextLine();
                System.out.print("Ingrese su nombre completo: ");
                String nom = sc.nextLine();
                new Hotel().verReserva(ced, nom);

            } else if (op == 3) {
                System.out.print("\nIngrese la cedula de la reserva a cancelar: ");
                String ced = sc.nextLine();
                System.out.print("Ingrese su nombre completo: ");
                String nom = sc.nextLine();
                new Hotel().cancelarReserva(ced, nom);

            } else if (op == 4) {
                new Hotel().mostrarTodasReservas();

            } else if (op == 5) {
                new Hotel().mostrarEstado();

            } else if (op == 0) {
                break;
            }
        }
    }

    public static int leerNumero() {
        while (true) {
            try {
                return Integer.parseInt(sc.nextLine());
            } catch (Exception e) {
                System.out.print(" Ingrese numero valido: ");
            }
        }
    }


    public static void mostrarHabitaciones() {
        System.out.println("\nHABITACIONES DISPONIBLES:");
        String sql = "SELECT numero, tipo, precio FROM Habitaciones WHERE disponible = 1";

        try (Connection con = conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                System.out.println(
                    rs.getInt("numero") + " - " +
                    rs.getString("tipo") + " - $" +
                    rs.getDouble("precio")
                );
            }

        } catch (SQLException e) {
            System.out.println("Error al consultar habitaciones: " + e.getMessage());
        }
    }


    public static void seleccionarHabitacion() {

        while (true) {

            mostrarHabitaciones();
            System.out.print("Elige el numero de habitacion: ");
            int op = leerNumero();

            String sql = "SELECT id_habitacion, tipo, precio FROM Habitaciones "
                       + "WHERE numero = ? AND disponible = 1";

            try (Connection con = conectar();
                 PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setInt(1, op);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        idHabitacionSeleccionada = rs.getInt("id_habitacion");
                        numeroHabitacion = op;
                        tipoHabitacion = rs.getString("tipo");
                        precioHabitacion = rs.getDouble("precio");
                        return;
                    } else {
                        System.out.println(" Habitacion invalida o no disponible.");
                    }
                }

            } catch (SQLException e) {
                System.out.println("Error al validar habitacion: " + e.getMessage());
            }
        }
    }

    public static void pedirDatos() {

        System.out.print("\nNombre: ");
        nombre = sc.nextLine();

        System.out.print("Cedula: ");
        cedula = sc.nextLine();

        System.out.print("Correo electronico (para enviarle la factura): ");
        correoCliente = sc.nextLine();

        idCliente = buscarOCrearCliente(nombre, cedula);
    }


    public static int buscarOCrearCliente(String nombre, String cedula) {

        String buscar = "SELECT id_cliente FROM Clientes WHERE cedula = ?";
        String insertar = "INSERT INTO Clientes (nombre, cedula) VALUES (?, ?)";

        try (Connection con = conectar()) {

            try (PreparedStatement ps = con.prepareStatement(buscar)) {
                ps.setString(1, cedula);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("id_cliente");
                    }
                }
            }

            try (PreparedStatement ps = con.prepareStatement(insertar, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, nombre);
                ps.setString(2, cedula);
                ps.executeUpdate();

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        return keys.getInt(1);
                    }
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al buscar/crear cliente: " + e.getMessage());
        }

        return -1;
    }

    public static void crearReserva() {

        System.out.print("\nFecha de entrada (AAAA-MM-DD): ");
        String entrada = sc.nextLine();

        System.out.print("Fecha de salida (AAAA-MM-DD): ");
        String salida = sc.nextLine();

        String insertar = "INSERT INTO Reservas (id_cliente, id_habitacion, fecha_entrada, fecha_salida, estado) "
                         + "VALUES (?, ?, ?, ?, 'Confirmada')";
        String actualizar = "UPDATE Habitaciones SET disponible = 0 WHERE id_habitacion = ?";

        try (Connection con = conectar()) {

            try (PreparedStatement ps = con.prepareStatement(insertar, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, idCliente);
                ps.setInt(2, idHabitacionSeleccionada);
                ps.setDate(3, Date.valueOf(entrada));
                ps.setDate(4, Date.valueOf(salida));
                ps.executeUpdate();

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        idReserva = keys.getInt(1);
                    }
                }
            }

            try (PreparedStatement ps = con.prepareStatement(actualizar)) {
                ps.setInt(1, idHabitacionSeleccionada);
                ps.executeUpdate();
            }

            System.out.println("\n Reserva registrada con exito (ID " + idReserva + ")");

        } catch (SQLException e) {
            System.out.println("Error al crear la reserva: " + e.getMessage());
        }
    }

    public static void restaurante() {

        int comidasElegidas = 0;
        if (pedirConsumo("Desayuno")) comidasElegidas++;
        if (pedirConsumo("Almuerzo")) comidasElegidas++;
        if (pedirConsumo("Cena")) comidasElegidas++;

        boolean clienteFrecuente = esClienteFrecuente(idCliente);

        mostrarFactura(comidasElegidas, clienteFrecuente);
    }


    public static boolean pedirConsumo(String tipo) {

        System.out.print("\n¿Desea " + tipo.toLowerCase() + "? (1=Si / 0=No): ");
        int quiere = leerNumero();

        if (quiere != 1) {
            return false;
        }

        String sql = "SELECT id_menu, nombre, precio FROM Menus WHERE tipo = ?";
        List<Integer> ids = new ArrayList<>();

        try (Connection con = conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, tipo);

            try (ResultSet rs = ps.executeQuery()) {
                int contador = 1;
                System.out.println("\nMenus de " + tipo + ":");
                while (rs.next()) {
                    ids.add(rs.getInt("id_menu"));
                    System.out.println(contador + ". " + rs.getString("nombre") + " - $" + rs.getDouble("precio"));
                    contador++;
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al consultar menus: " + e.getMessage());
            return false;
        }

        System.out.print("Elige una opcion (0 = ninguna): ");
        int op = leerNumero();

        if (op >= 1 && op <= ids.size()) {
            guardarConsumo(ids.get(op - 1));
            return true;
        }
        return false;
    }

    public static void guardarConsumo(int idMenu) {

        String sql = "INSERT INTO Consumos (id_reserva, id_menu, cantidad) VALUES (?, ?, 1)";

        try (Connection con = conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idReserva);
            ps.setInt(2, idMenu);
            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error al guardar consumo: " + e.getMessage());
        }
    }


    public static boolean esClienteFrecuente(int idCliente) {

        String sql = "SELECT COUNT(*) AS total FROM Reservas WHERE id_cliente = ?";

        try (Connection con = conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idCliente);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total") > 1;
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al verificar cliente frecuente: " + e.getMessage());
        }

        return false;
    }


    public static void mostrarFactura(int comidasElegidas, boolean clienteFrecuente) {

        String sql =
            "SELECT c.nombre, c.cedula, h.numero, h.tipo, h.precio AS precio_habitacion, " +
            "       r.fecha_entrada, r.fecha_salida, " +
            "       ISNULL(SUM(m.precio * co.cantidad), 0) AS total_consumo " +
            "FROM Reservas r " +
            "JOIN Clientes c ON r.id_cliente = c.id_cliente " +
            "JOIN Habitaciones h ON r.id_habitacion = h.id_habitacion " +
            "LEFT JOIN Consumos co ON co.id_reserva = r.id_reserva " +
            "LEFT JOIN Menus m ON co.id_menu = m.id_menu " +
            "WHERE r.id_reserva = ? " +
            "GROUP BY c.nombre, c.cedula, h.numero, h.tipo, h.precio, r.fecha_entrada, r.fecha_salida";

        try (Connection con = conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idReserva);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double totalConsumo = rs.getDouble("total_consumo");
                    double precioHab = rs.getDouble("precio_habitacion");
                    double subtotal = precioHab + totalConsumo;

                    double descuento = 0;
                    String motivo = "";

                    if (comidasElegidas == 3) {
                        descuento += subtotal * 0.10;
                        motivo += "Paquete completo (10%) ";
                    }

                    if (clienteFrecuente) {
                        descuento += subtotal * 0.05;
                        motivo += "Cliente frecuente (5%) ";
                    }

                    double total = subtotal - descuento;

                    // Armamos el texto de la factura UNA sola vez,
                    // para imprimirlo en consola y tambien enviarlo por correo.
                    StringBuilder factura = new StringBuilder();
                    factura.append("========= FACTURA =========\n");
                    factura.append("Reserva N°: ").append(idReserva).append("\n");
                    factura.append("Cliente: ").append(rs.getString("nombre")).append("\n");
                    factura.append("Cedula: ").append(rs.getString("cedula")).append("\n");
                    factura.append("Habitacion: ").append(rs.getInt("numero"))
                           .append(" (").append(rs.getString("tipo")).append(")\n");
                    factura.append("Entrada: ").append(rs.getDate("fecha_entrada")).append("\n");
                    factura.append("Salida: ").append(rs.getDate("fecha_salida")).append("\n");
                    factura.append("Precio habitacion: $").append(precioHab).append("\n");
                    factura.append("Consumo restaurante: $").append(totalConsumo).append("\n");
                    factura.append("Subtotal: $").append(subtotal).append("\n");

                    if (descuento > 0) {
                        factura.append("Descuento aplicado: -$").append(descuento)
                               .append(" (").append(motivo.trim()).append(")\n");
                    }

                    factura.append("TOTAL A PAGAR: $").append(total).append("\n");
                    factura.append("===========================\n");

                    // 1. Se muestra en consola (igual que antes)
                    System.out.println("\n" + factura.toString());

                    // 2. Se envia por correo al cliente
                    if (correoCliente != null && !correoCliente.isBlank()) {
                        enviarFactura(correoCliente, factura.toString());
                    }
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al generar la factura: " + e.getMessage());
        }
    }

    // ===================== ENVIO DE CORREO =====================
    public static void enviarFactura(String destinatario, String contenidoFactura) {

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(CORREO_REMITENTE, CLAVE_APP);
            }
        });

        try {
            Message mensaje = new MimeMessage(session);
            mensaje.setFrom(new InternetAddress(CORREO_REMITENTE));
            mensaje.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
            mensaje.setSubject("Factura de tu reserva - Hotel PGC");
            mensaje.setText(
                "Gracias por reservar con nosotros.\n\n" +
                contenidoFactura +
                "\nEste es un correo generado automaticamente, por favor no respondas a este mensaje."
            );

            Transport.send(mensaje);
            System.out.println(" Factura enviada correctamente a " + destinatario);

        } catch (MessagingException e) {
            System.out.println(" No se pudo enviar el correo: " + e.getMessage());
        }
    }
}