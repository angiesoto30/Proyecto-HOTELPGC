
package reserva1;

public class Almuerzo extends Reserva1 {

    
    private Habitacion habitacion;

    
    public Almuerzo(String nombreCliente,
                     int numeroHabitacion,
                     String tipoHabitacion,
                     Habitacion habitacion) {

        super(nombreCliente, numeroHabitacion, tipoHabitacion);

        this.habitacion = habitacion;
    }



    public void mostrarMenus() {

        System.out.println("========================================");
        System.out.println("          ALMUERZO EJECUTIVO");
        System.out.println("========================================");

        

        System.out.println("========================================");

        System.out.println("Menu A - Alta Mesa");
        System.out.println("- Consome de res con verduras");
        System.out.println("- Medallon de res al vino tinto");
        System.out.println("- Risotto de champinones");
        System.out.println("- Mousse de maracuya");
        System.out.println("Precio: $65.000");

        System.out.println("========================================");

        System.out.println("Menu B - Mediterraneo");
        System.out.println("- Crema de tomate al albahaca");
        System.out.println("- Pechuga rellena con espinaca");
        System.out.println("- Papas gratinadas");
        System.out.println("- Tiramisu casero");
        System.out.println("Precio: $52.000");

        System.out.println("========================================");
    }

    public double calcularPrecio(int opcion) {

        if (opcion == 1) {
            return 65000;
        }

        if (opcion == 2) {
            return 52000;
        }

        return 0;
    }
}