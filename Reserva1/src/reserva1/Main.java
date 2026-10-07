package reserva1;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Properties;
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
    static String correo;
    static int idCliente;

    static int idReserva;

    static String nombreUsuarioActual;
    static String rolUsuarioActual;

    private static final String ROL_ADMIN = "Jefe";

    // Datos para el envio de correo (reemplaza con los tuyos)
    private static final String CORREO_EMISOR = "hotelpgc2824@gmail.com";
    private static final String CLAVE_APP = "zugbdddefyfghany";

    private static final String URL =
        "jdbc:sqlserver://localhost:1433;databaseName=HotelReserva;encrypt=true;trustServerCertificate=true";
    private static final String USUARIO = "hotelapp";
    private static final String PASSWORD = obtenerPasswordBD();

    private static String obtenerPasswordBD() {
        String p = System.getenv("HOTEL_DB_PASSWORD");
        return (p == null || p.isBlank()) ? "HotelApp2026!" : p;
    }

    public static Connection conectar() {
        try {
            return DriverManager.getConnection(URL, USUARIO, PASSWORD);
        } catch (SQLException e) {
            System.out.println(" Error al conectar a la base de datos: " + e.getMessage());
            return null;
        }
    }

    public static void main(String[] args) {

        if (args.length > 0 && args[0].equalsIgnoreCase("recordatorios")) {
            enviarRecordatoriosCheckIn();
            return;
        }

        enviarRecordatoriosCheckIn();

        while (true) {

            System.out.println("\n===== HOTEL PGC =====");
            System.out.println("1. Soy Cliente");
            System.out.println("2. Soy Empleado");
            System.out.println("0. Salir");
            System.out.print("Opcion: ");

            int op = leerNumero();

            if (op == 1) {
                menuCliente();

            } else if (op == 2) {
                if (iniciarSesion()) {
                    menuEmpleado();
                } else {
                    System.out.println("\nNo fue posible iniciar sesion.");
                }

            } else if (op == 0) {
                break;
            } else {
                System.out.println(" Opcion invalida. Intenta de nuevo.");
            }
        }
    }

    // ================= MENU CLIENTE (sin login) =================

    public static void menuCliente() {

        while (true) {

            System.out.println("\n===== CLIENTE - HOTEL PGC =====");
            System.out.println("1. Reservar habitacion");
            System.out.println("2. Ver mis reservas");
            System.out.println("3. Cancelar una reserva");
            System.out.println("4. Buscar habitaciones con filtros");
            System.out.println("0. Volver al menu principal");
            System.out.print("Opcion: ");

            int op = leerNumero();

            if (op == 1) {
                boolean habitacionElegida = seleccionarHabitacion();
                if (habitacionElegida) {
                    pedirDatos();
                    crearReserva();
                    restaurante();
                }

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
                buscarHabitacionesConFiltro();

            } else if (op == 0) {
                break;

            } else {
                System.out.println(" Opcion invalida. Intenta de nuevo.");
            }
        }
    }

    public static void buscarHabitacionesConFiltro() {

        Double precioMin = leerPrecioOpcional("\nPrecio minimo (deja vacio para no filtrar): ");
        Double precioMax = leerPrecioOpcional("Precio maximo (deja vacio para no filtrar): ");

        System.out.print("Tipo de habitacion, ej: Sencilla, Doble, Suite (deja vacio para no filtrar): ");
        String textoTipo = sc.nextLine().trim();
        String tipo = textoTipo.isEmpty() ? null : textoTipo;

        new Hotel().buscarConFiltros(precioMin, precioMax, tipo);
    }

    public static Double leerPrecioOpcional(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String texto = sc.nextLine().trim();

            if (texto.isEmpty()) {
                return null;
            }

            texto = texto.replace("$", "").replace(".", "").replace(",", "").replace(" ", "");

            try {
                return Double.parseDouble(texto);
            } catch (NumberFormatException e) {
                System.out.println(" Eso no es un numero valido. Deja vacio si no quieres filtrar por precio.");
            }
        }
    }

    public static void enviarRecordatoriosCheckIn() {

        String sql =
            "SELECT r.id_reserva, c.nombre, c.email, h.numero, h.tipo, r.fecha_entrada " +
            "FROM Reservas r " +
            "JOIN Clientes c ON r.id_cliente = c.id_cliente " +
            "JOIN Habitaciones h ON r.id_habitacion = h.id_habitacion " +
            "WHERE r.estado = 'Confirmada' " +
            "AND r.fecha_entrada = CAST(DATEADD(day, 1, GETDATE()) AS DATE) " +
            "AND ISNULL(r.recordatorio_enviado, 0) = 0";

        try (Connection con = conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                String email = rs.getString("email");
                if (email == null || email.isBlank()) {
                    continue;
                }

                String mensaje =
                    "Hola " + rs.getString("nombre") + ",\n\n" +
                    "Te recordamos que tu check-in en el Hotel PGC es manana, " + rs.getDate("fecha_entrada") + ".\n" +
                    "Habitacion: " + rs.getInt("numero") + " (" + rs.getString("tipo") + ")\n\n" +
                    "Te esperamos!";

                if (enviarRecordatorioPorCorreo(email, mensaje)) {
                    marcarRecordatorioEnviado(rs.getInt("id_reserva"));
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al enviar recordatorios: " + e.getMessage());
        } catch (NullPointerException e) {
            System.out.println("No se pudo consultar recordatorios (sin conexion a la base de datos).");
        }
    }

    public static void marcarRecordatorioEnviado(int idReserva) {

        String sql = "UPDATE Reservas SET recordatorio_enviado = 1 WHERE id_reserva = ?";

        try (Connection con = conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idReserva);
            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error al marcar recordatorio enviado: " + e.getMessage());
        }
    }

    private static boolean enviarCorreo(String correoDestino, String asunto, String cuerpo) {

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");

        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(CORREO_EMISOR, CLAVE_APP);
            }
        });

        try {
            Message mensaje = new MimeMessage(session);
            mensaje.setFrom(new InternetAddress(CORREO_EMISOR));
            mensaje.setRecipients(Message.RecipientType.TO, InternetAddress.parse(correoDestino));
            mensaje.setSubject(asunto);
            mensaje.setText(cuerpo);

            Transport.send(mensaje);
            return true;

        } catch (MessagingException e) {
            System.out.println("Error al enviar el correo a " + correoDestino + ": " + e.getMessage());
            return false;
        }
    }

    public static boolean enviarRecordatorioPorCorreo(String correoDestino, String cuerpoMensaje) {
        return enviarCorreo(correoDestino, "Recordatorio de Check-in - Hotel PGC", cuerpoMensaje);
    }

    public static boolean enviarFacturaPorCorreo(String correoDestino, String cuerpoFactura) {
        boolean ok = enviarCorreo(correoDestino, "Factura Electronica - Hotel", cuerpoFactura);
        if (ok) {
            System.out.println("\n Factura enviada correctamente a " + correoDestino);
        }
        return ok;
    }

    // ================= MENU EMPLEADOS (requiere sesion) =================

    public static void menuEmpleado() {

        boolean esAdmin = rolUsuarioActual != null && rolUsuarioActual.equalsIgnoreCase(ROL_ADMIN);

        while (true) {

            System.out.println("\n===== PANEL EMPLEADOS =====");
            System.out.println("Sesion activa: " + nombreUsuarioActual + " (" + rolUsuarioActual + ")");
            System.out.println("1. Ver todas las reservas");
            System.out.println("2. Ver estado de habitaciones");
            System.out.println("3. Hacer checkout");

            if (esAdmin) {
                System.out.println("4. Editar una reserva (Administrador)");
                System.out.println("5. Eliminar una reserva definitivamente (Administrador)");
            }

            System.out.println("0. Cerrar sesion");
            System.out.print("Opcion: ");

            int op = leerNumero();

            if (op == 1) {
                new Hotel().mostrarTodasReservas();

            } else if (op == 2) {
                new Hotel().mostrarEstado();

            } else if (op == 3) {
                System.out.print("\nIngrese la cedula del cliente a hacer checkout: ");
                String cedChk = sc.nextLine();
                new Hotel().hacerCheckoutManual(cedChk);

            } else if (op == 4 && esAdmin) {

                new Hotel().mostrarTodasReservas();
                System.out.print("\nIngrese el ID de la reserva a editar: ");
                int idEditar = leerNumero();

                String nuevaEntrada = leerFecha("Nueva fecha de entrada (ej: 2026-10-07, 07/10/2026 o 07-10-2026): ");
                String nuevaSalida = leerFecha("Nueva fecha de salida (mismo formato): ");

                new Hotel().editarReserva(idEditar, nuevaEntrada, nuevaSalida);

            } else if (op == 5 && esAdmin) {

                new Hotel().mostrarTodasReservas();
                System.out.print("\nIngrese el ID de la reserva a eliminar: ");
                int idEliminar = leerNumero();

                System.out.print("¿Seguro que deseas eliminarla definitivamente? (1=Si / 0=No): ");
                int confirmar = leerNumero();

                if (confirmar == 1) {
                    new Hotel().eliminarReservaDefinitivamente(idEliminar);
                } else {
                    System.out.println("Operacion cancelada, no se elimino nada.");
                }

            } else if (op == 0) {
                nombreUsuarioActual = null;
                rolUsuarioActual = null;
                break;

            } else {
                System.out.println(" Opcion invalida. Intenta de nuevo.");
            }
        }
    }

    // ================= LOGIN =================

    public static boolean iniciarSesion() {

        int intentos = 3;

        while (intentos > 0) {

            System.out.println("\n===== INICIO DE SESION - HOTEL PGC =====");
            System.out.print("Usuario: ");
            String usuarioIngresado = sc.nextLine();
            System.out.print("Contrasena: ");
            String contrasenaIngresada = sc.nextLine();

            String hashIngresado = generarHash(contrasenaIngresada);
            if (hashIngresado == null) return false;

            String sql = "SELECT usuario, rol FROM Usuarios WHERE usuario = ? AND contrasena = ?";

            try (Connection con = conectar();
                 PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setString(1, usuarioIngresado);
                ps.setString(2, hashIngresado);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        nombreUsuarioActual = rs.getString("usuario");
                        rolUsuarioActual = rs.getString("rol");
                        System.out.println("\nBienvenido, " + nombreUsuarioActual + " (" + rolUsuarioActual + ")");
                        return true;
                    }
                }

            } catch (SQLException e) {
                System.out.println("Error al validar el usuario: " + e.getMessage());
                return false;
            }

            intentos--;
            System.out.println("Usuario o contrasena incorrectos. Intentos restantes: " + intentos);
        }

        return false;
    }

    public static String generarHash(String texto) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] resultado = md.digest(texto.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : resultado) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Error al generar el hash: " + e.getMessage());
            return null;
        }
    }

    public static int leerNumero() {
        while (true) {
            String texto = sc.nextLine().trim();

            if (texto.isEmpty()) {
                continue;
            }

            try {
                return Integer.parseInt(texto);
            } catch (NumberFormatException e) {
                System.out.print(" Ese valor no es un numero valido, intenta de nuevo: ");
            }
        }
    }

    public static String leerFecha(String mensaje) {

        String[] formatosAceptados = {"yyyy-MM-dd", "dd/MM/yyyy", "dd-MM-yyyy"};

        while (true) {
            System.out.print(mensaje);
            String textoFecha = sc.nextLine().trim();

            for (String formato : formatosAceptados) {
                try {
                    SimpleDateFormat sdfEntrada = new SimpleDateFormat(formato);
                    sdfEntrada.setLenient(false);
                    java.util.Date fechaValida = sdfEntrada.parse(textoFecha);

                    SimpleDateFormat sdfSalida = new SimpleDateFormat("yyyy-MM-dd");
                    return sdfSalida.format(fechaValida);

                } catch (ParseException e) {
                }
            }

            System.out.println(" Fecha invalida. Usa alguno de estos formatos: AAAA-MM-DD, DD/MM/AAAA o DD-MM-AAAA. Intenta de nuevo.");
        }
    }


    public static boolean mostrarHabitaciones() {
        System.out.println("\nHABITACIONES DISPONIBLES:");
        String sql = "SELECT numero, tipo, precio FROM Habitaciones WHERE disponible = 1";
        boolean hayDisponibles = false;

        try (Connection con = conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                System.out.println(
                    rs.getInt("numero") + " - " +
                    rs.getString("tipo") + " - $" +
                    rs.getDouble("precio")
                );
                hayDisponibles = true;
            }

            if (!hayDisponibles) {
                System.out.println("En este momento no hay habitaciones disponibles.");
            }

        } catch (SQLException e) {
            System.out.println("Error al consultar habitaciones: " + e.getMessage());
        }

        return hayDisponibles;
    }


    public static boolean seleccionarHabitacion() {

        while (true) {

            boolean hayDisponibles = mostrarHabitaciones();

            if (!hayDisponibles) {
                return false;
            }

            System.out.print("Elige el numero de habitacion (0 para volver al menu): ");
            int op = leerNumero();

            if (op == 0) {
                return false;
            }

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
                        return true;
                    } else {
                        System.out.println(" Habitacion invalida o no disponible. Intenta con otro numero.");
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

        System.out.print("Correo electronico: ");
        correo = sc.nextLine().trim();

        idCliente = buscarOCrearCliente(nombre, cedula, correo);
    }


    public static int buscarOCrearCliente(String nombre, String cedula, String correo) {

        String buscar = "SELECT id_cliente, email FROM Clientes WHERE cedula = ?";
        String insertar = "INSERT INTO Clientes (nombre, cedula, email) VALUES (?, ?, ?)";
        String actualizarCorreo = "UPDATE Clientes SET email = ? WHERE id_cliente = ?";

        try (Connection con = conectar()) {

            try (PreparedStatement ps = con.prepareStatement(buscar)) {
                ps.setString(1, cedula);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        int idExistente = rs.getInt("id_cliente");
                        String correoGuardado = rs.getString("email");

                        boolean correoNuevo = correo != null && !correo.isBlank()
                                && (correoGuardado == null || !correoGuardado.equalsIgnoreCase(correo));

                        if (correoNuevo) {
                            try (PreparedStatement psUpdate = con.prepareStatement(actualizarCorreo)) {
                                psUpdate.setString(1, correo);
                                psUpdate.setInt(2, idExistente);
                                psUpdate.executeUpdate();
                            }
                        }

                        return idExistente;
                    }
                }
            }

            try (PreparedStatement ps = con.prepareStatement(insertar, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, nombre);
                ps.setString(2, cedula);
                ps.setString(3, correo);
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

        String entrada = leerFecha("\nFecha de entrada (ej: 2026-10-07, 07/10/2026 o 07-10-2026): ");
        String salida = leerFecha("Fecha de salida (mismo formato): ");

        String insertar = "INSERT INTO Reservas (id_cliente, id_habitacion, fecha_entrada, fecha_salida, estado, recordatorio_enviado) "
                         + "VALUES (?, ?, ?, ?, 'Confirmada', 0)";
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
            "SELECT c.nombre, c.cedula, c.email, h.numero, h.tipo, h.precio AS precio_habitacion, " +
            "       r.fecha_entrada, r.fecha_salida, " +
            "       ISNULL(SUM(m.precio * co.cantidad), 0) AS total_consumo " +
            "FROM Reservas r " +
            "JOIN Clientes c ON r.id_cliente = c.id_cliente " +
            "JOIN Habitaciones h ON r.id_habitacion = h.id_habitacion " +
            "LEFT JOIN Consumos co ON co.id_reserva = r.id_reserva " +
            "LEFT JOIN Menus m ON co.id_menu = m.id_menu " +
            "WHERE r.id_reserva = ? " +
            "GROUP BY c.nombre, c.cedula, c.email, h.numero, h.tipo, h.precio, r.fecha_entrada, r.fecha_salida";

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
                    String emailCliente = rs.getString("email");

                    StringBuilder factura = new StringBuilder();
                    factura.append("========= FACTURA =========\n");
                    factura.append("Cliente: ").append(rs.getString("nombre")).append("\n");
                    factura.append("Cedula: ").append(rs.getString("cedula")).append("\n");
                    factura.append("Habitacion: ").append(rs.getInt("numero")).append(" (").append(rs.getString("tipo")).append(")\n");
                    factura.append("Entrada: ").append(rs.getDate("fecha_entrada")).append("\n");
                    factura.append("Salida: ").append(rs.getDate("fecha_salida")).append("\n");
                    factura.append("Precio habitacion: $").append(precioHab).append("\n");
                    factura.append("Consumo restaurante: $").append(totalConsumo).append("\n");
                    factura.append("Subtotal: $").append(subtotal).append("\n");

                    if (descuento > 0) {
                        factura.append("Descuento aplicado: -$").append(descuento).append(" (").append(motivo.trim()).append(")\n");
                    }

                    factura.append("TOTAL A PAGAR: $").append(total).append("\n");
                    factura.append("===========================\n");

                    System.out.println("\n" + factura.toString());

                    if (emailCliente != null && !emailCliente.isBlank()) {
                        enviarFacturaPorCorreo(emailCliente, factura.toString());
                    } else {
                        System.out.println("El cliente no tiene correo registrado, no se envio factura.");
                    }
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al generar la factura: " + e.getMessage());
        }
    }
}