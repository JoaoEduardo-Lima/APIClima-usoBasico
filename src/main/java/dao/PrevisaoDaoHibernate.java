package dao;

import modelo.Localizacao;
import modelo.PrevisaoDoCLima;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import java.time.Instant;

import java.util.List;

public class PrevisaoDaoHibernate implements PrevisaoDao {

    private final SessionFactory sessionFactory;

    public PrevisaoDaoHibernate(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void salvar(PrevisaoDoCLima previsao) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                session.persist(previsao);
                tx.commit();
            } catch (RuntimeException e) {
                tx.rollback();
                throw e;
            }
        }
    }

    @Override
    public List<PrevisaoDoCLima> listarPorLocalizacao(Localizacao localizacao) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery(
                            "from PrevisaoDoCLima p join fetch p.location "
                                    + "where p.location = :loc order by p.forecastAt desc",
                            PrevisaoDoCLima.class)
                    .setParameter("loc", localizacao)
                    .getResultList();
        }
    }

    @Override
    public boolean existe(Localizacao localizacao, Instant forecastAt) {
        try (Session session = sessionFactory.openSession()) {
            Long total = session.createQuery(
                            "select count(p) from PrevisaoDoCLima p "
                                    + "where p.location = :loc and p.forecastAt = :quando", Long.class)
                    .setParameter("loc", localizacao)
                    .setParameter("quando", forecastAt)
                    .getSingleResult();
            return total > 0;
        }
    }
}

