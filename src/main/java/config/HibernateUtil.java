package config;


import modelo.Localizacao;
import modelo.PrevisaoDoCLima;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;


public class HibernateUtil  {

    private static final SessionFactory FACTORY = criar();

    private HibernateUtil() {}

    private static SessionFactory criar() {
        return new Configuration()
                .setProperty("jakarta.persistence.jdbc.url", "jdbc:sqlite:estacao.db")
                .setProperty("jakarta.persistence.jdbc.driver", "org.sqlite.JDBC")   // some o aviso HHH10001006
                .setProperty("hibernate.dialect", "org.hibernate.community.dialect.SQLiteDialect")
                .setProperty("hibernate.hbm2ddl.auto", "update")
                .setProperty("hibernate.show_sql", "false")   // era "true"
                .addAnnotatedClass(Localizacao.class)
                .addAnnotatedClass(PrevisaoDoCLima.class)
                .buildSessionFactory();
    }

    public static SessionFactory getSessionFactory() {
        return FACTORY;
    }

    public static void fechar() {
        FACTORY.close();
    }


}
