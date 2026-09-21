
package reserva1;



import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class VentanaPrincipal extends JFrame {

    private static final Color OSCURO = new Color(20, 20, 22);
    private static final Color DORADO = new Color(201, 168, 105);
    private static final Color DORADO_CLARO = new Color(232, 207, 155);
    private static final Color BLANCO_HUESO = new Color(245, 242, 235);
    private static final Color GRIS_TEXTO = new Color(199, 194, 180);

    private BufferedImage fondo;
    private CardLayout cardLayout;
    private JPanel panelRaiz;
    private JPanel panelBotones;
    private JLabel tituloPanel;
    private JLabel descPanel;
    private JLabel etiquetaRol;
    private String rolActual = "";
    private String usuarioActual = "";

    public VentanaPrincipal() {
        setTitle("Hotel PGC — Sistema de Gestión");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        cargarImagenFondo();

        cardLayout = new CardLayout();
        panelRaiz = new FondoPanel();
        panelRaiz.setLayout(cardLayout);

        panelRaiz.add(crearPantallaRol(), "rol");
        panelRaiz.add(crearPantallaPanel(), "panel");

        add(panelRaiz);
        cardLayout.show(panelRaiz, "rol");
    }

    private void cargarImagenFondo() {
        try {
            InputStream is = getClass().getResourceAsStream("/reserva1/imagenes/terraza.jpg");
            if (is != null) fondo = ImageIO.read(is);
        } catch (Exception e) {
            System.out.println("Error al cargar imagen: " + e.getMessage());
        }
    }

    private class FondoPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (fondo != null) {
                g2.drawImage(fondo, 0, 0, getWidth(), getHeight(), this);
            } else {
                g2.setColor(OSCURO);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }

            GradientPaint degradado = new GradientPaint(
                    0, 0, new Color(20, 20, 22, 225),
                    getWidth(), getHeight() / 2, new Color(20, 20, 22, 90)
            );
            g2.setPaint(degradado);
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
        }
    }

    private class TarjetaRedondeada extends JPanel {
        private boolean hover = false;
        private final int radio;

        public TarjetaRedondeada(int radio) {
            this.radio = radio;
            setOpaque(false);
        }

        public void setHover(boolean h) {
            hover = h;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color relleno = hover ? new Color(201, 168, 105, 45) : new Color(22, 22, 24, 185);
            g2.setColor(relleno);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, radio, radio));
            g2.setColor(hover ? DORADO : new Color(201, 168, 105, 100));
            g2.setStroke(new BasicStroke(1.2f));
            g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 2, getHeight() - 2, radio, radio));
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private void dibujarIcono(Graphics2D g2, String tipo, int cx, int cy, int size, Color color) {
        g2.setColor(color);
        g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int s = size;
        int x = cx - s / 2, y = cy - s / 2;

        switch (tipo) {
            case "reservar":
                g2.drawRoundRect(x, y + 2, s, s - 2, 3, 3);
                g2.drawLine(x + 3, y, x + 3, y + 5);
                g2.drawLine(x + s - 3, y, x + s - 3, y + 5);
                g2.drawLine(x, y + 7, x + s, y + 7);
                break;
            case "buscar":
                g2.drawOval(x, y, s - 4, s - 4);
                g2.drawLine(x + s - 6, y + s - 6, x + s, y + s);
                break;
            case "cancelar":
                g2.drawOval(x, y, s, s);
                g2.drawLine(x + 4, y + 4, x + s - 4, y + s - 4);
                g2.drawLine(x + s - 4, y + 4, x + 4, y + s - 4);
                break;
            case "lista":
                g2.drawRoundRect(x + 2, y, s - 4, s, 3, 3);
                g2.drawLine(x + 5, y + 6, x + s - 5, y + 6);
                g2.drawLine(x + 5, y + 11, x + s - 5, y + 11);
                g2.drawLine(x + 5, y + 16, x + s - 5, y + 16);
                break;
            case "puerta":
                g2.drawRoundRect(x + 2, y, s - 4, s, 2, 2);
                g2.fillOval(x + s - 8, y + s / 2 - 1, 3, 3);
                break;
            case "checkout":
                g2.drawRoundRect(x, y + 3, s, s - 6, 3, 3);
                g2.drawLine(x, y + 8, x + s, y + 8);
                break;
            case "usuario":
                g2.drawOval(x + s / 2 - 4, y, 8, 8);
                g2.drawArc(x, y + 10, s, s, 0, 180);
                break;
            case "candado":
                g2.drawRoundRect(x + 2, y + 8, s - 4, s - 8, 2, 2);
                g2.drawArc(x + 4, y, s - 8, 12, 0, 180);
                break;
        }
    }

    private JPanel crearPantallaRol() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(60, 60, 60, 60));

        JLabel nombreHotel = new JLabel("HOTEL PGC");
        nombreHotel.setFont(new Font("Serif", Font.BOLD, 42));
        nombreHotel.setForeground(DORADO);
        nombreHotel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitulo = new JLabel("SISTEMA DE GESTIÓN HOTELERA");
        subtitulo.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subtitulo.setForeground(GRIS_TEXTO);
        subtitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel filaOpciones = new JPanel();
        filaOpciones.setOpaque(false);
        filaOpciones.setLayout(new FlowLayout(FlowLayout.CENTER, 30, 0));

        JPanel tarjetaCliente = crearTarjetaRol("usuario", "Soy Cliente", "Reservar, ver o cancelar mi reserva");
        tarjetaCliente.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) { entrarComo("cliente"); }
        });

        JPanel tarjetaEmpleado = crearTarjetaRol("candado", "Soy Empleado", "Acceso al panel administrativo");
        tarjetaEmpleado.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) { pedirClaveEmpleado(); }
        });

        filaOpciones.add(tarjetaCliente);
        filaOpciones.add(tarjetaEmpleado);

        panel.add(Box.createVerticalGlue());
        panel.add(nombreHotel);
        panel.add(Box.createRigidArea(new Dimension(0, 8)));
        panel.add(subtitulo);
        panel.add(Box.createRigidArea(new Dimension(0, 50)));
        panel.add(filaOpciones);
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    private JPanel crearTarjetaRol(String icono, String titulo, String descripcion) {
        TarjetaRedondeada tarjeta = new TarjetaRedondeada(16);
        tarjeta.setLayout(null);
        tarjeta.setPreferredSize(new Dimension(240, 150));
        tarjeta.setMaximumSize(new Dimension(240, 150));
        tarjeta.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel iconoLbl = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                dibujarIcono((Graphics2D) g, icono, 17, 17, 22, DORADO);
            }
        };
        iconoLbl.setBounds(24, 22, 34, 34);

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblTitulo.setForeground(BLANCO_HUESO);
        lblTitulo.setBounds(24, 66, 190, 22);

        JLabel lblDesc = new JLabel("<html><div style='width:180px'>" + descripcion + "</div></html>");
        lblDesc.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblDesc.setForeground(GRIS_TEXTO);
        lblDesc.setBounds(24, 90, 190, 44);

        tarjeta.add(iconoLbl);
        tarjeta.add(lblTitulo);
        tarjeta.add(lblDesc);

        tarjeta.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { tarjeta.setHover(true); }
            public void mouseExited(java.awt.event.MouseEvent e) { tarjeta.setHover(false); }
        });

        return tarjeta;
    }

private void pedirClaveEmpleado() {
    JDialog dialogo = new JDialog(this, "Inicio de sesión", true);
    dialogo.setUndecorated(true);
    dialogo.setSize(340, 340);
    dialogo.setLocationRelativeTo(this);

    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setBackground(new Color(24, 24, 26));
    panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(DORADO, 1),
            new EmptyBorder(30, 30, 30, 30)
    ));

    JLabel titulo = new JLabel("ACCESO EMPLEADO");
    titulo.setFont(new Font("SansSerif", Font.BOLD, 15));
    titulo.setForeground(DORADO);
    titulo.setAlignmentX(Component.LEFT_ALIGNMENT);

    JLabel lblUsuario = new JLabel("Usuario");
    lblUsuario.setFont(new Font("SansSerif", Font.PLAIN, 11));
    lblUsuario.setForeground(GRIS_TEXTO);
    lblUsuario.setAlignmentX(Component.LEFT_ALIGNMENT);

    JTextField campoUsuario = crearCampoLogin();

    JLabel lblClave = new JLabel("Contraseña");
    lblClave.setFont(new Font("SansSerif", Font.PLAIN, 11));
    lblClave.setForeground(GRIS_TEXTO);
    lblClave.setAlignmentX(Component.LEFT_ALIGNMENT);

    JPasswordField campoClave = new JPasswordField();
    estilizarCampo(campoClave);

    JLabel lblError = new JLabel(" ");
    lblError.setFont(new Font("SansSerif", Font.PLAIN, 11));
    lblError.setForeground(new Color(224, 120, 120));
    lblError.setAlignmentX(Component.LEFT_ALIGNMENT);

    JButton btnIngresar = new JButton("Ingresar");
    btnIngresar.setFont(new Font("SansSerif", Font.BOLD, 13));
    btnIngresar.setForeground(OSCURO);
    btnIngresar.setBackground(DORADO);
    btnIngresar.setFocusPainted(false);
    btnIngresar.setBorder(new EmptyBorder(10, 0, 10, 0));
    btnIngresar.setAlignmentX(Component.LEFT_ALIGNMENT);
    btnIngresar.setMaximumSize(new Dimension(280, 40));
    btnIngresar.setCursor(new Cursor(Cursor.HAND_CURSOR));

    JButton btnCancelar = new JButton("Cancelar");
    btnCancelar.setFont(new Font("SansSerif", Font.PLAIN, 12));
    btnCancelar.setForeground(GRIS_TEXTO);
    btnCancelar.setBackground(new Color(24, 24, 26));
    btnCancelar.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 82), 1));
    btnCancelar.setFocusPainted(false);
    btnCancelar.setAlignmentX(Component.LEFT_ALIGNMENT);
    btnCancelar.setMaximumSize(new Dimension(280, 36));
    btnCancelar.setCursor(new Cursor(Cursor.HAND_CURSOR));
    btnCancelar.addActionListener(e -> dialogo.dispose());

    btnIngresar.addActionListener(e -> {
        String usuario = campoUsuario.getText().trim();
        String clave = new String(campoClave.getPassword());
        String hash = Main.generarHash(clave);

        String sql = "SELECT usuario, rol FROM Usuarios WHERE usuario = ? AND contrasena = ?";

        try (Connection con = Main.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, usuario);
            ps.setString(2, hash);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    usuarioActual = rs.getString("usuario");
                    dialogo.dispose();
                    entrarComo("empleado");
                } else {
                    lblError.setText("Usuario o contraseña incorrectos.");
                }
            }

        } catch (SQLException ex) {
            lblError.setText("Error de conexión.");
        }
    });

    panel.add(titulo);
    panel.add(Box.createRigidArea(new Dimension(0, 24)));
    panel.add(lblUsuario);
    panel.add(Box.createRigidArea(new Dimension(0, 4)));
    panel.add(campoUsuario);
    panel.add(Box.createRigidArea(new Dimension(0, 14)));
    panel.add(lblClave);
    panel.add(Box.createRigidArea(new Dimension(0, 4)));
    panel.add(campoClave);
    panel.add(Box.createRigidArea(new Dimension(0, 8)));
    panel.add(lblError);
    panel.add(Box.createRigidArea(new Dimension(0, 16)));
    panel.add(btnIngresar);
    panel.add(Box.createRigidArea(new Dimension(0, 8)));
    panel.add(btnCancelar);

    dialogo.add(panel);
    dialogo.setVisible(true);
}

private JTextField crearCampoLogin() {
    JTextField campo = new JTextField();
    estilizarCampo(campo);
    return campo;
}

private void estilizarCampo(JTextField campo) {
    campo.setFont(new Font("SansSerif", Font.PLAIN, 13));
    campo.setForeground(BLANCO_HUESO);
    campo.setBackground(new Color(34, 34, 36));
    campo.setCaretColor(BLANCO_HUESO);
    campo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(80, 80, 82), 1),
            new EmptyBorder(8, 10, 8, 10)
    ));
    campo.setAlignmentX(Component.LEFT_ALIGNMENT);
    campo.setMaximumSize(new Dimension(280, 36));
}

    private void entrarComo(String rol) {
        rolActual = rol;
        actualizarPanel();
        cardLayout.show(panelRaiz, "panel");
    }

    private JPanel crearPantallaPanel() {
        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.setOpaque(false);
        contenedor.setBorder(new EmptyBorder(36, 50, 36, 50));

        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setOpaque(false);

        JPanel logoBox = new JPanel();
        logoBox.setOpaque(false);
        logoBox.setLayout(new BoxLayout(logoBox, BoxLayout.Y_AXIS));
        JLabel nombreHotel = new JLabel("HOTEL PGC");
        nombreHotel.setFont(new Font("SansSerif", Font.BOLD, 18));
        nombreHotel.setForeground(BLANCO_HUESO);
        JLabel subLogo = new JLabel("GESTIÓN HOTELERA");
        subLogo.setFont(new Font("SansSerif", Font.PLAIN, 10));
        subLogo.setForeground(DORADO);
        logoBox.add(nombreHotel);
        logoBox.add(subLogo);

        etiquetaRol = new JLabel("");
        etiquetaRol.setFont(new Font("SansSerif", Font.PLAIN, 12));
        etiquetaRol.setForeground(GRIS_TEXTO);

        encabezado.add(logoBox, BorderLayout.WEST);
        encabezado.add(etiquetaRol, BorderLayout.EAST);
        encabezado.setBorder(new EmptyBorder(0, 0, 30, 0));

        JPanel centro = new JPanel();
        centro.setOpaque(false);
        centro.setLayout(new BoxLayout(centro, BoxLayout.Y_AXIS));

        tituloPanel = new JLabel("Bienvenida");
        tituloPanel.setFont(new Font("Serif", Font.BOLD, 30));
        tituloPanel.setForeground(BLANCO_HUESO);
        tituloPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        descPanel = new JLabel(" ");
        descPanel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        descPanel.setForeground(GRIS_TEXTO);
        descPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        panelBotones = new JPanel();
        panelBotones.setOpaque(false);
        panelBotones.setLayout(new GridLayout(0, 2, 14, 14));
        panelBotones.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelBotones.setBorder(new EmptyBorder(24, 0, 0, 0));

        centro.add(tituloPanel);
        centro.add(Box.createRigidArea(new Dimension(0, 8)));
        centro.add(descPanel);
        centro.add(panelBotones);

        JPanel pie = new JPanel(new BorderLayout());
        pie.setOpaque(false);
        pie.setBorder(new EmptyBorder(24, 0, 0, 0));

        JButton btnVolver = crearBotonSecundario("←  Cambiar de rol");
        btnVolver.addActionListener(e -> cardLayout.show(panelRaiz, "rol"));

        JButton btnSalir = crearBotonSecundario("Salir del sistema");
        btnSalir.addActionListener(e -> System.exit(0));

        pie.add(btnVolver, BorderLayout.WEST);
        pie.add(btnSalir, BorderLayout.EAST);

        contenedor.add(encabezado, BorderLayout.NORTH);
        contenedor.add(centro, BorderLayout.CENTER);
        contenedor.add(pie, BorderLayout.SOUTH);

        return contenedor;
    }

    private void actualizarPanel() {
        panelBotones.removeAll();

        if (rolActual.equals("cliente")) {
            etiquetaRol.setText("●  Cliente");
            tituloPanel.setText("Bienvenida");
            descPanel.setText("Gestiona tu reserva en Hotel PGC.");

            agregarBoton("reservar", "Reservar habitación", "Nueva estadía", this::accionReservar);
            agregarBoton("buscar", "Ver mis reservas", "Consultar reserva", this::accionVerReservas);
            agregarBoton("cancelar", "Cancelar reserva", "Gestionar cancelación", this::accionCancelar);

        } else {
            etiquetaRol.setText("●  " + usuarioActual);
            tituloPanel.setText("Panel administrativo");
            descPanel.setText("Control total de reservas y habitaciones.");

            agregarBoton("reservar", "Reservar habitación", "Nueva estadía", this::accionReservar);
            agregarBoton("buscar", "Ver mis reservas", "Consultar reserva", this::accionVerReservas);
            agregarBoton("cancelar", "Cancelar reserva", "Gestionar cancelación", this::accionCancelar);
            agregarBoton("lista", "Todas las reservas", "Listado general", this::accionVerTodas);
            agregarBoton("puerta", "Estado de habitaciones", "Disponibilidad actual", this::accionEstado);
            agregarBoton("checkout", "Hacer checkout", "Registrar salida", this::accionCheckout);
        }

        panelBotones.revalidate();
        panelBotones.repaint();
    }

    private void agregarBoton(String icono, String titulo, String subtitulo, Runnable accion) {
        TarjetaRedondeada tarjeta = new TarjetaRedondeada(12);
        tarjeta.setLayout(null);
        tarjeta.setPreferredSize(new Dimension(280, 66));
        tarjeta.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel iconoLbl = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                dibujarIcono((Graphics2D) g, icono, 19, 19, 20, DORADO_CLARO);
            }
        };
        iconoLbl.setBounds(14, 14, 38, 38);

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblTitulo.setForeground(BLANCO_HUESO);
        lblTitulo.setBounds(62, 12, 200, 18);

        JLabel lblSub = new JLabel(subtitulo);
        lblSub.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblSub.setForeground(GRIS_TEXTO);
        lblSub.setBounds(62, 32, 200, 18);

        tarjeta.add(iconoLbl);
        tarjeta.add(lblTitulo);
        tarjeta.add(lblSub);

        tarjeta.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { tarjeta.setHover(true); }
            public void mouseExited(java.awt.event.MouseEvent e) { tarjeta.setHover(false); }
            public void mouseClicked(java.awt.event.MouseEvent e) { accion.run(); }
        });

        panelBotones.add(tarjeta);
    }

    private JButton crearBotonSecundario(String texto) {
        JButton boton = new JButton(texto);
        boton.setFont(new Font("SansSerif", Font.PLAIN, 12));
        boton.setForeground(BLANCO_HUESO);
        boton.setBackground(new Color(20, 20, 22, 160));
        boton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(201, 168, 105, 100), 1),
                new EmptyBorder(10, 18, 10, 18)
        ));
        boton.setFocusPainted(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return boton;
    }

    private void accionReservar() { mostrarAviso("Función de reservar: por conectar."); }
    private void accionVerReservas() { mostrarAviso("Función de ver reservas: por conectar."); }
    private void accionCancelar() { mostrarAviso("Función de cancelar reserva: por conectar."); }

    private void accionVerTodas() {
        new Hotel().mostrarTodasReservas();
        mostrarAviso("Revisa la consola de NetBeans para ver el listado.");
    }

    private void accionEstado() {
        new Hotel().mostrarEstado();
        mostrarAviso("Revisa la consola de NetBeans para ver el estado.");
    }

    private void accionCheckout() {
        String cedula = JOptionPane.showInputDialog(this, "Ingrese la cédula del cliente:");
        if (cedula != null && !cedula.isBlank()) {
            new Hotel().hacerCheckoutManual(cedula);
            mostrarAviso("Checkout procesado. Revisa la consola para el detalle.");
        }
    }

    private void mostrarAviso(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Hotel PGC", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatLightLaf());
        } catch (Exception e) {
            System.out.println("No se pudo aplicar el estilo FlatLaf: " + e.getMessage());
        }
        SwingUtilities.invokeLater(() -> new VentanaPrincipal().setVisible(true));
    }
}
    

