package reserva1;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class VentanaEstadoHabitaciones extends JPanel {

    private static final Color OSCURO = new Color(20, 20, 22);
    private static final Color DORADO = new Color(201, 168, 105);
    private static final Color BLANCO_HUESO = new Color(245, 242, 235);
    private static final Color GRIS_TEXTO = new Color(199, 194, 180);
    private static final Color FONDO_CAMPO = new Color(34, 34, 36);
    private static final Color BORDE_CAMPO = new Color(80, 80, 82);
    private static final Color AVISO = new Color(224, 170, 110);

    private static final String[] COLUMNAS = {"Habitación", "Tipo", "Piso", "Capacidad", "Precio", "Estado"};
    private static final int COL_PRECIO = 4;
    private static final int COL_ESTADO = 5;

    private final DefaultTableModel modelo = new DefaultTableModel(COLUMNAS, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modelo);
    private final JLabel lblEstado = new JLabel(" ");
    private final JButton btnActualizar = new JButton("Actualizar");

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

    public VentanaEstadoHabitaciones(Runnable alVolver) {
        setOpaque(false);
        setLayout(new GridBagLayout());

        Tarjeta tarjeta = new Tarjeta();
        tarjeta.setLayout(new BorderLayout(0, 14));
        tarjeta.setBorder(new EmptyBorder(26, 32, 22, 32));
        tarjeta.setPreferredSize(new Dimension(780, 500));

        JLabel titulo = new JLabel("ESTADO DE HABITACIONES");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 15));
        titulo.setForeground(DORADO);

        configurarTabla();
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createLineBorder(BORDE_CAMPO, 1));
        scroll.getViewport().setBackground(FONDO_CAMPO);

        lblEstado.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblEstado.setForeground(GRIS_TEXTO);

        estilizarBotonPrincipal(btnActualizar);
        btnActualizar.addActionListener(e -> cargar());

        JButton btnVolver = new JButton("Volver");
        btnVolver.setFont(new Font("SansSerif", Font.PLAIN, 12));
        btnVolver.setForeground(GRIS_TEXTO);
        btnVolver.setBackground(new Color(24, 24, 26));
        btnVolver.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE_CAMPO, 1),
                new EmptyBorder(10, 22, 10, 22)));
        btnVolver.setFocusPainted(false);
        btnVolver.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnVolver.addActionListener(e -> alVolver.run());

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        botones.setOpaque(false);
        botones.add(btnVolver);
        botones.add(btnActualizar);

        JPanel pie = new JPanel(new BorderLayout(12, 0));
        pie.setOpaque(false);
        pie.add(lblEstado, BorderLayout.CENTER);
        pie.add(botones, BorderLayout.EAST);

        tarjeta.add(titulo, BorderLayout.NORTH);
        tarjeta.add(scroll, BorderLayout.CENTER);
        tarjeta.add(pie, BorderLayout.SOUTH);

        add(tarjeta);

        cargar();
    }

    private void estilizarBotonPrincipal(JButton b) {
        b.setFont(new Font("SansSerif", Font.BOLD, 13));
        b.setForeground(OSCURO);
        b.setBackground(DORADO);
        b.setBorder(new EmptyBorder(11, 24, 11, 24));
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void configurarTabla() {
        tabla.setRowHeight(32);
        tabla.setShowVerticalLines(false);
        tabla.setGridColor(new Color(55, 55, 58));
        tabla.setFillsViewportHeight(true);
        tabla.setBackground(FONDO_CAMPO);
        tabla.setForeground(BLANCO_HUESO);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getTableHeader().setReorderingAllowed(false);
        tabla.getTableHeader().setPreferredSize(new Dimension(100, 34));

        DefaultTableCellRenderer cabecera = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object valor, boolean sel,
                                                           boolean foco, int fila, int col) {
                super.getTableCellRendererComponent(t, valor, false, false, fila, col);
                setBackground(new Color(30, 30, 32));
                setForeground(DORADO);
                setFont(new Font("SansSerif", Font.BOLD, 12));
                setBorder(new EmptyBorder(0, 10, 0, 10));
                setHorizontalAlignment(col == COL_PRECIO ? SwingConstants.RIGHT : SwingConstants.LEFT);
                return this;
            }
        };
        tabla.getTableHeader().setDefaultRenderer(cabecera);

        DefaultTableCellRenderer celdas = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object valor, boolean sel,
                                                           boolean foco, int fila, int col) {
                super.getTableCellRendererComponent(t, valor, sel, false, fila, col);
                setFont(new Font("SansSerif", Font.PLAIN, 13));
                setBorder(new EmptyBorder(0, 10, 0, 10));
                setHorizontalAlignment(col == COL_PRECIO ? SwingConstants.RIGHT : SwingConstants.LEFT);

                if (sel) {
                    setBackground(new Color(70, 62, 45));
                } else {
                    setBackground(fila % 2 == 0 ? FONDO_CAMPO : new Color(40, 40, 43));
                }

                setForeground(BLANCO_HUESO);
                if (col == COL_ESTADO && valor != null) {
                    setFont(new Font("SansSerif", Font.BOLD, 12));
                    setForeground(valor.toString().equals("Libre")
                            ? new Color(130, 205, 150)
                            : new Color(224, 120, 120));
                }
                return this;
            }
        };
        tabla.setDefaultRenderer(Object.class, celdas);

        int[] anchos = {100, 180, 70, 100, 120, 100};
        for (int i = 0; i < anchos.length; i++) {
            tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }
    }

    private void mostrarMensaje(String texto, boolean esAviso) {
        lblEstado.setForeground(esAviso ? AVISO : GRIS_TEXTO);
        lblEstado.setText(texto);
    }

    private void cargar() {
        btnActualizar.setEnabled(false);
        btnActualizar.setText("Actualizando...");

        new SwingWorker<List<Object[]>, Void>() {
            @Override
            protected List<Object[]> doInBackground() throws Exception {
                return consultar();
            }

            @Override
            protected void done() {
                btnActualizar.setEnabled(true);
                btnActualizar.setText("Actualizar");
                modelo.setRowCount(0);

                try {
                    List<Object[]> filas = get();

                    if (filas.isEmpty()) {
                        mostrarMensaje("No hay habitaciones registradas.", true);
                        return;
                    }

                    int libres = 0;
                    for (Object[] fila : filas) {
                        modelo.addRow(fila);
                        if (fila[COL_ESTADO].equals("Libre")) {
                            libres++;
                        }
                    }

                    int ocupadas = filas.size() - libres;
                    mostrarMensaje(filas.size() + " habitaciones: " + libres + " libres, " + ocupadas + " ocupadas.", false);

                } catch (Exception ex) {
                    Throwable causa = ex.getCause() != null ? ex.getCause() : ex;
                    mostrarMensaje(causa.getMessage() != null ? causa.getMessage() : "Ocurrió un error inesperado.", true);
                }
            }
        }.execute();
    }

    private List<Object[]> consultar() throws Exception {
        List<Object[]> filas = new ArrayList<>();

        String sql = "SELECT numero, tipo, piso, capacidad, precio, disponible "
                   + "FROM Habitaciones ORDER BY numero";

        try (Connection con = Main.conectar()) {
            if (con == null) {
                throw new IllegalStateException("No hay conexión con la base de datos.");
            }

            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    filas.add(new Object[] {
                        "Hab " + rs.getInt("numero"),
                        rs.getString("tipo"),
                        rs.getInt("piso"),
                        rs.getInt("capacidad"),
                        "$" + String.format("%,.0f", rs.getDouble("precio")),
                        rs.getBoolean("disponible") ? "Libre" : "Ocupada"
                    });
                }
            }

        } catch (SQLException e) {
            throw new IllegalStateException("Error al consultar las habitaciones: " + e.getMessage());
        }

        return filas;
    }
}
