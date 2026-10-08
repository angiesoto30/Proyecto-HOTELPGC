package reserva1;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

public class VentanaReservar extends JPanel {

    private static final Color OSCURO = new Color(20, 20, 22);
    private static final Color DORADO = new Color(201, 168, 105);
    private static final Color BLANCO_HUESO = new Color(245, 242, 235);
    private static final Color GRIS_TEXTO = new Color(199, 194, 180);
    private static final Color FONDO_CAMPO = new Color(34, 34, 36);
    private static final Color BORDE_CAMPO = new Color(80, 80, 82);

    private static final DateTimeFormatter[] FORMATOS = {
        DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT),
        DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT),
        DateTimeFormatter.ofPattern("dd-MM-uuuu").withResolverStyle(ResolverStyle.STRICT)
    };

    private final Runnable alVolver;

    private final CardLayout vistas = new CardLayout();
    private final JPanel contenido = new JPanel(vistas);

    private final JComboBox<Opcion> cbHabitacion = new JComboBox<>();
    private final JComboBox<Opcion> cbDesayuno = new JComboBox<>();
    private final JComboBox<Opcion> cbAlmuerzo = new JComboBox<>();
    private final JComboBox<Opcion> cbCena = new JComboBox<>();

    private final JTextField tfNombre = new JTextField();
    private final JTextField tfCedula = new JTextField();
    private final JTextField tfCorreo = new JTextField();
    private final JTextField tfEntrada = new JTextField();
    private final JTextField tfSalida = new JTextField();

    private final JLabel lblError = new JLabel(" ");
    private final JButton btnConfirmar = new JButton("Confirmar reserva");

    private final JLabel lblExitoId = new JLabel(" ", SwingConstants.CENTER);
    private final JLabel lblExitoHab = new JLabel(" ", SwingConstants.CENTER);
    private final JLabel lblExitoFechas = new JLabel(" ", SwingConstants.CENTER);
    private final JLabel lblExitoCorreo = new JLabel(" ", SwingConstants.CENTER);
    private final JLabel lblExitoAviso = new JLabel(" ", SwingConstants.CENTER);

    private volatile String avisoFactura = "";

    private static class Opcion {
        final int id;
        final String texto;

        Opcion(int id, String texto) {
            this.id = id;
            this.texto = texto;
        }

        @Override
        public String toString() {
            return texto;
        }
    }

    private static class Tarjeta extends JPanel {
        Tarjeta() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(22, 22, 24, 215));
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 18, 18));
            g2.setColor(new Color(201, 168, 105, 140));
            g2.setStroke(new BasicStroke(1.2f));
            g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 2, getHeight() - 2, 18, 18));
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public VentanaReservar(Runnable alVolver) {
        this.alVolver = alVolver;
        setOpaque(false);
        setLayout(new GridBagLayout());

        DateTimeFormatter visible = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        tfEntrada.setText(LocalDate.now().format(visible));
        tfSalida.setText(LocalDate.now().plusDays(1).format(visible));

        Tarjeta tarjeta = new Tarjeta();
        tarjeta.setLayout(new BorderLayout());
        tarjeta.setBorder(new EmptyBorder(26, 32, 24, 32));
        tarjeta.setPreferredSize(new Dimension(700, 540));

        contenido.setOpaque(false);
        contenido.add(construirFormulario(), "form");
        contenido.add(construirExito(), "exito");
        tarjeta.add(contenido, BorderLayout.CENTER);

        add(tarjeta);

        cargarHabitaciones();
        cargarMenu(cbDesayuno, "Desayuno");
        cargarMenu(cbAlmuerzo, "Almuerzo");
        cargarMenu(cbCena, "Cena");
    }

    private JPanel construirFormulario() {
        JPanel raiz = new JPanel(new BorderLayout(0, 16));
        raiz.setOpaque(false);

        JLabel titulo = new JLabel("RESERVAR HABITACIÓN");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 15));
        titulo.setForeground(DORADO);

        JPanel columnas = new JPanel(new GridLayout(1, 2, 28, 0));
        columnas.setOpaque(false);

        JPanel izquierda = nuevaColumna();
        agregar(izquierda, "Habitación disponible", estilizarCombo(cbHabitacion));
        agregar(izquierda, "Nombre completo", estilizarCampo(tfNombre));
        agregar(izquierda, "Cédula", estilizarCampo(tfCedula));
        agregar(izquierda, "Correo electrónico (para la factura)", estilizarCampo(tfCorreo));

        JPanel derecha = nuevaColumna();
        agregar(derecha, "Fecha de entrada (dd/mm/aaaa)", estilizarCampo(tfEntrada));
        agregar(derecha, "Fecha de salida (dd/mm/aaaa)", estilizarCampo(tfSalida));
        agregar(derecha, "Desayuno", estilizarCombo(cbDesayuno));
        agregar(derecha, "Almuerzo", estilizarCombo(cbAlmuerzo));
        agregar(derecha, "Cena", estilizarCombo(cbCena));

        columnas.add(izquierda);
        columnas.add(derecha);

        lblError.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblError.setForeground(new Color(224, 120, 120));

        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.setFont(new Font("SansSerif", Font.PLAIN, 12));
        btnCancelar.setForeground(GRIS_TEXTO);
        btnCancelar.setBackground(new Color(24, 24, 26));
        btnCancelar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE_CAMPO, 1),
                new EmptyBorder(10, 22, 10, 22)));
        btnCancelar.setFocusPainted(false);
        btnCancelar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCancelar.addActionListener(e -> alVolver.run());

        btnConfirmar.setFont(new Font("SansSerif", Font.BOLD, 13));
        btnConfirmar.setForeground(OSCURO);
        btnConfirmar.setBackground(DORADO);
        btnConfirmar.setBorder(new EmptyBorder(11, 26, 11, 26));
        btnConfirmar.setFocusPainted(false);
        btnConfirmar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnConfirmar.addActionListener(e -> confirmar());

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        botones.setOpaque(false);
        botones.add(btnCancelar);
        botones.add(btnConfirmar);

        JPanel pie = new JPanel(new BorderLayout(0, 8));
        pie.setOpaque(false);
        pie.add(lblError, BorderLayout.NORTH);
        pie.add(botones, BorderLayout.SOUTH);

        raiz.add(titulo, BorderLayout.NORTH);
        raiz.add(columnas, BorderLayout.CENTER);
        raiz.add(pie, BorderLayout.SOUTH);

        return raiz;
    }

    private JPanel construirExito() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        JLabel titulo = new JLabel("RESERVA CONFIRMADA");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 15));
        titulo.setForeground(DORADO);
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblExitoId.setFont(new Font("Serif", Font.BOLD, 38));
        lblExitoId.setForeground(BLANCO_HUESO);
        lblExitoId.setAlignmentX(Component.CENTER_ALIGNMENT);

        for (JLabel l : new JLabel[]{lblExitoHab, lblExitoFechas, lblExitoCorreo}) {
            l.setFont(new Font("SansSerif", Font.PLAIN, 14));
            l.setForeground(GRIS_TEXTO);
            l.setAlignmentX(Component.CENTER_ALIGNMENT);
        }

        lblExitoAviso.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblExitoAviso.setForeground(new Color(224, 170, 110));
        lblExitoAviso.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btnVolver = new JButton("Volver al panel");
        btnVolver.setFont(new Font("SansSerif", Font.BOLD, 13));
        btnVolver.setForeground(OSCURO);
        btnVolver.setBackground(DORADO);
        btnVolver.setBorder(new EmptyBorder(11, 30, 11, 30));
        btnVolver.setFocusPainted(false);
        btnVolver.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnVolver.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnVolver.addActionListener(e -> alVolver.run());

        p.add(Box.createVerticalGlue());
        p.add(titulo);
        p.add(Box.createRigidArea(new Dimension(0, 14)));
        p.add(lblExitoId);
        p.add(Box.createRigidArea(new Dimension(0, 18)));
        p.add(lblExitoHab);
        p.add(Box.createRigidArea(new Dimension(0, 6)));
        p.add(lblExitoFechas);
        p.add(Box.createRigidArea(new Dimension(0, 18)));
        p.add(lblExitoCorreo);
        p.add(Box.createRigidArea(new Dimension(0, 14)));
        p.add(lblExitoAviso);
        p.add(Box.createRigidArea(new Dimension(0, 28)));
        p.add(btnVolver);
        p.add(Box.createVerticalGlue());

        return p;
    }

    private void mostrarExito(int id, String habitacion, LocalDate entrada, LocalDate salida, String correo) {
        DateTimeFormatter f = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        lblExitoId.setText("Reserva #" + id);
        lblExitoHab.setText(habitacion);
        lblExitoFechas.setText(entrada.format(f) + "  →  " + salida.format(f));
        lblExitoCorreo.setText("La factura se envía a " + correo + ".");
        lblExitoAviso.setText(avisoFactura.isEmpty() ? " " : avisoFactura.trim());

        vistas.show(contenido, "exito");
    }

    private JPanel nuevaColumna() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        return p;
    }

    private void agregar(JPanel columna, String etiqueta, JComponent campo) {
        JLabel l = new JLabel(etiqueta);
        l.setFont(new Font("SansSerif", Font.PLAIN, 11));
        l.setForeground(GRIS_TEXTO);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);

        campo.setAlignmentX(Component.LEFT_ALIGNMENT);
        campo.setMaximumSize(new Dimension(300, 36));
        campo.setPreferredSize(new Dimension(240, 36));

        columna.add(l);
        columna.add(Box.createRigidArea(new Dimension(0, 4)));
        columna.add(campo);
        columna.add(Box.createRigidArea(new Dimension(0, 14)));
    }

    private JTextField estilizarCampo(JTextField campo) {
        campo.setFont(new Font("SansSerif", Font.PLAIN, 13));
        campo.setForeground(BLANCO_HUESO);
        campo.setBackground(FONDO_CAMPO);
        campo.setCaretColor(BLANCO_HUESO);
        campo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE_CAMPO, 1),
                new EmptyBorder(8, 10, 8, 10)));
        return campo;
    }

    private JComboBox<Opcion> estilizarCombo(JComboBox<Opcion> combo) {
        combo.setFont(new Font("SansSerif", Font.PLAIN, 13));
        combo.setForeground(BLANCO_HUESO);
        combo.setBackground(FONDO_CAMPO);
        combo.setBorder(BorderFactory.createLineBorder(BORDE_CAMPO, 1));
        combo.putClientProperty("FlatLaf.style",
                "buttonBackground: #222224; buttonArrowColor: #C9A869");
        return combo;
    }

    private void cargarHabitaciones() {
        String sql = "SELECT id_habitacion, numero, tipo, precio FROM Habitaciones "
                   + "WHERE disponible = 1 ORDER BY numero";

        try (Connection con = Main.conectar()) {
            if (con == null) {
                lblError.setText("No hay conexión con la base de datos.");
                btnConfirmar.setEnabled(false);
                return;
            }

            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    String texto = "Hab " + rs.getInt("numero") + " — " + rs.getString("tipo")
                            + " — $" + String.format("%,.0f", rs.getDouble("precio"));
                    cbHabitacion.addItem(new Opcion(rs.getInt("id_habitacion"), texto));
                }
            }

        } catch (SQLException e) {
            lblError.setText("Error al consultar habitaciones.");
            btnConfirmar.setEnabled(false);
            return;
        }

        if (cbHabitacion.getItemCount() == 0) {
            lblError.setText("En este momento no hay habitaciones disponibles.");
            btnConfirmar.setEnabled(false);
        }
    }

    private void cargarMenu(JComboBox<Opcion> combo, String tipo) {
        combo.addItem(new Opcion(-1, "Ninguno"));

        String sql = "SELECT id_menu, nombre, precio FROM Menus WHERE tipo = ?";

        try (Connection con = Main.conectar()) {
            if (con == null) {
                return;
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, tipo);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String texto = rs.getString("nombre") + " — $"
                                + String.format("%,.0f", rs.getDouble("precio"));
                        combo.addItem(new Opcion(rs.getInt("id_menu"), texto));
                    }
                }
            }

        } catch (SQLException e) {
            lblError.setText("Error al consultar el menú de " + tipo.toLowerCase() + ".");
        }
    }

    private static LocalDate parsearFecha(String texto) {
        for (DateTimeFormatter f : FORMATOS) {
            try {
                return LocalDate.parse(texto, f);
            } catch (DateTimeParseException e) {
            }
        }
        return null;
    }

    private void confirmar() {
        lblError.setText(" ");

        String nombre = tfNombre.getText().trim();
        String cedula = tfCedula.getText().trim();
        String correo = tfCorreo.getText().trim();
        Opcion hab = (Opcion) cbHabitacion.getSelectedItem();

        if (hab == null) {
            mostrarError("Elige una habitación.");
            return;
        }
        if (nombre.isEmpty() || cedula.isEmpty() || correo.isEmpty()) {
            mostrarError("Completa nombre, cédula y correo.");
            return;
        }
        if (!correo.contains("@") || !correo.contains(".")) {
            mostrarError("El correo no parece válido.");
            return;
        }

        LocalDate entrada = parsearFecha(tfEntrada.getText().trim());
        LocalDate salida = parsearFecha(tfSalida.getText().trim());

        if (entrada == null || salida == null) {
            mostrarError("Fecha inválida. Usa dd/mm/aaaa, por ejemplo 07/10/2026.");
            return;
        }
        if (entrada.isBefore(LocalDate.now())) {
            mostrarError("La fecha de entrada no puede ser anterior a hoy.");
            return;
        }
        if (!salida.isAfter(entrada)) {
            mostrarError("La fecha de salida debe ser posterior a la de entrada.");
            return;
        }

        final int idHab = hab.id;
        final String textoHab = hab.texto;
        final int[] menus = {
            ((Opcion) cbDesayuno.getSelectedItem()).id,
            ((Opcion) cbAlmuerzo.getSelectedItem()).id,
            ((Opcion) cbCena.getSelectedItem()).id
        };
        final LocalDate fEntrada = entrada;
        final LocalDate fSalida = salida;

        btnConfirmar.setEnabled(false);
        btnConfirmar.setText("Procesando...");

        new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() throws Exception {
                return procesarReserva(nombre, cedula, correo, idHab, fEntrada, fSalida, menus);
            }

            @Override
            protected void done() {
                try {
                    int id = get();
                    mostrarExito(id, textoHab, fEntrada, fSalida, correo);
                } catch (Exception ex) {
                    Throwable causa = ex.getCause() != null ? ex.getCause() : ex;
                    String msg = causa.getMessage() != null ? causa.getMessage() : "Ocurrió un error inesperado.";
                    mostrarError(msg);
                    btnConfirmar.setEnabled(true);
                    btnConfirmar.setText("Confirmar reserva");
                }
            }
        }.execute();
    }

    private void mostrarError(String mensaje) {
        lblError.setText("<html>" + mensaje + "</html>");
    }

    private void verificarCedula(String cedula, String nombre) throws Exception {
        try (Connection con = Main.conectar()) {
            if (con == null) {
                throw new IllegalStateException("No hay conexión con la base de datos.");
            }

            try (PreparedStatement ps = con.prepareStatement("SELECT nombre FROM Clientes WHERE cedula = ?")) {
                ps.setString(1, cedula);

                try (ResultSet rs = ps.executeQuery()) {
                    boolean existe = false;
                    boolean coincide = false;

                    while (rs.next()) {
                        existe = true;
                        String guardado = rs.getString("nombre");
                        if (guardado != null && guardado.trim().equalsIgnoreCase(nombre.trim())) {
                            coincide = true;
                        }
                    }

                    if (existe && !coincide) {
                        throw new IllegalStateException("Esa cédula ya está registrada con otro nombre. Verifica la cédula y el nombre.");
                    }
                }
            }

        } catch (SQLException e) {
            throw new IllegalStateException("Error al verificar la cédula: " + e.getMessage());
        }
    }

    private int procesarReserva(String nombre, String cedula, String correo, int idHab,
                                LocalDate entrada, LocalDate salida, int[] menus) throws Exception {

        verificarCedula(cedula, nombre);

        int idCliente = Main.buscarOCrearCliente(nombre, cedula, correo);
        if (idCliente == -1) {
            throw new IllegalStateException("No se pudo registrar al cliente.");
        }

        int idNueva = -1;

        try (Connection con = Main.conectar()) {
            if (con == null) {
                throw new IllegalStateException("No hay conexión con la base de datos.");
            }

            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE Habitaciones SET disponible = 0 WHERE id_habitacion = ? AND disponible = 1")) {
                    ps.setInt(1, idHab);
                    if (ps.executeUpdate() != 1) {
                        throw new IllegalStateException("Esa habitación ya no está disponible. Elige otra.");
                    }
                }

                String insertar = "INSERT INTO Reservas (id_cliente, id_habitacion, fecha_entrada, fecha_salida, estado, recordatorio_enviado) "
                                + "VALUES (?, ?, ?, ?, 'Confirmada', 0)";

                try (PreparedStatement ps = con.prepareStatement(insertar, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, idCliente);
                    ps.setInt(2, idHab);
                    ps.setDate(3, Date.valueOf(entrada));
                    ps.setDate(4, Date.valueOf(salida));
                    ps.executeUpdate();

                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("No se obtuvo el ID de la reserva.");
                        }
                        idNueva = keys.getInt(1);
                    }
                }

                con.commit();

            } catch (Exception e) {
                con.rollback();
                throw e;
            }
        }

        Main.idReserva = idNueva;
        Main.idCliente = idCliente;

        int comidas = 0;
        for (int idMenu : menus) {
            if (idMenu != -1) {
                Main.guardarConsumo(idMenu);
                comidas++;
            }
        }

        try {
            Main.mostrarFactura(comidas, Main.esClienteFrecuente(idCliente));
        } catch (Throwable t) {
            avisoFactura = "Ojo: no se pudo enviar la factura por correo (" + t.getMessage() + ").";
        }

        return idNueva;
    }
}