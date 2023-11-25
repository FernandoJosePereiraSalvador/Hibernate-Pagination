package Pagination;

import Clases.Employee;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.util.List;
import java.util.Scanner;

/**
 * Muestra los registros de una base de datos en forma de ventanas
 *
 * @author Fernando
 */
public class main {

    /**
     * Método principal que inicia el programa.
     *
     * @param args Argumentos de línea de comandos (no son usados).
     */
    public static void main(String[] args) {

        // Obtener la sesión de Hibernate
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();

        Session session = sessionFactory.getCurrentSession();

        // Variable para la página actual
        int current_page = 1;

        // Número de consultas que se enseñan
        int page_size = 12;

        try {
            // Iniciar la transacción de Hibernate
            session.beginTransaction();

            // Mostrar 1 página
            MostrarVentana(session, 1, page_size);

            Scanner scanner = new Scanner(System.in);
            while (true) {

                // Preguntar la opción
                System.out.println("");
                System.out.println("Introduce su opción: <S> next, <A> previous, <G n> Go to n, <S n> Page size n <Q> exit");
                String choice = scanner.nextLine();

                // Siguiente página
                if (choice.equalsIgnoreCase("S")) {
                    System.out.println("");
                    current_page = current_page + 1;
                    MostrarVentana(session, current_page, page_size);
                } // Página anterior
                else if (choice.equalsIgnoreCase("A")) {
                    current_page = current_page - 1;
                    MostrarVentana(session, current_page, page_size);
                } // Ir a una página concreta
                else if (choice.matches("^G \\d+$")) {
                    int page = Integer.parseInt(choice.split(" ")[1]);
                    if (page < getTotalPages(session, page_size)) {
                        current_page = page;
                        MostrarVentana(session, goToPage(session, current_page, page_size), page_size);
                    } else {
                        System.out.println("La página no existe");
                    }
                    // Cambiar el número de consultas que se muestran
                } else if (choice.matches("^S \\d+$")) {
                    int size = Integer.parseInt(choice.split(" ")[1]);
                    if (size < getTotalRecords(session)) {
                        page_size = size;
                        MostrarVentana(session, current_page, page_size);
                    } else {
                        System.out.println("No se puede establecer el size");
                        MostrarVentana(session, current_page, page_size);
                    }
                    // Quitar programa  
                } else if (choice.equalsIgnoreCase("Q")) {
                    break;
                } // Opción inválida
                else {
                    System.out.println("Opción inválida.");
                }
            }

            session.getTransaction().commit();
        } finally {
            sessionFactory.close();
        }
    }

    /**
     * Muestra una página específica de la base de datos.
     *
     * @param session Sesión actual de Hibernate.
     * @param page Página actual
     * @param page_size Cantidad de registros a mostrar
     */
    private static void MostrarVentana(Session session, int page, int page_size) {
        List<Employee> employees = session.createQuery("from Employee", Employee.class)
                .setFirstResult((page - 1) * page_size)
                .setMaxResults(page_size)
                .getResultList();

        System.out.println("---------------------------------------------------------");
        System.out.println("Page " + page + " of " + getTotalPages(session, page_size));
        System.out.println("---------------------------------------------------------");
        for (Employee employee : employees) {
            System.out.println(employee);
        }
    }

    /**
     * Ir a una página específica.
     *
     * @param session Sesión actual de Hibernate.
     * @param page Número de páginas a las que queremos ir
     * @param page_size Número de registros por página
     * @return Número de la página actual después de la operación.
     */
    private static int goToPage(Session session, int page, int page_size) {
        int totalPages = getTotalPages(session, page_size);
        
        // Asegurarse que sea válida
        if (page > 0 && page <= totalPages) {
            return page;
        } else {
            return getCurrentPage(session, page_size);
        }
    }
    
    /**
     * Obtener la página actual.
     * 
     * @param session Sesión actual de Hibernate.
     * @param page_size Número de registros por página
     * @return Número de la página actual
     */
    private static int getCurrentPage(Session session, int page_size) {
        int totalRecords = getTotalRecords(session);
        return (totalRecords + page_size - 1) / page_size;
    }
    
    /**
     * Obtener el número total de páginas.
     * 
     * @param session Sesión actual de Hibernate.
     * @param page_size Número de registros por página
     * @return Número total de páginas
     */
    private static int getTotalPages(Session session, int page_size) {
        int totalRecords = getTotalRecords(session);
        return (totalRecords + page_size - 1) / page_size;
    }
    
    /**
     * Obtener el número total de registros.
     * 
     * @param session Sesión actual de Hibernate.
     * @return Total de registros
     */
    private static int getTotalRecords(Session session) {
        return session.createQuery("select count(*) from Employee", Long.class).uniqueResult().intValue();

    }
}
