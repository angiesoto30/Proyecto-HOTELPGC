package reserva1;

public class Desayuno extends Reserva1 {

    private String cedula;
    private String telefono;

    public Desayuno(String nombreCliente,
    int numeroHabitacion,
    String tipoHabitacion,
    String cedula,
                     String telefono) {

        super(nombreCliente, numeroHabitacion, tipoHabitacion);

        this.cedula = cedula;
        this.telefono = telefono;
    }

    public void mostrarMenus() {

        System.out.println("========================================");
        System.out.println("         DESAYUNO EJECUTIVO");
        System.out.println("========================================");

        System.out.println("Menu A - Clasico Continental");
        System.out.println("- Croissant con jamon serrano");
        System.out.println("- Huevos benedictinos");
        System.out.println("- Jugo de naranja exprimido");
        System.out.println("- Cafe americano o te verde");
        System.out.println("Precio: $28.000");

        System.out.println("========================================");

        System.out.println("Menu B - Express Ejecutivo");
        System.out.println("- Tostadas artesanales con avocado");
        System.out.println("- Yogur griego con granola");
        System.out.println("- Jugo verde detox");
        System.out.println("- Espresso doble");
        System.out.println("Precio: $22.000");


        System.out.println("========================================");
    }

    public double calcularPrecio(int opcion) {

        if (opcion ==  1) {
            return 28000;
        }

        if (opcion == 2 ) {
            return 22000;
        }

        return 0;
    }
}