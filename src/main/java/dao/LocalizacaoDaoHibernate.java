package dao;

import modelo.Localizacao;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.List;
import java.util.Optional;

public class LocalizacaoDaoHibernate implements LocalizacaoDao {

    private final SessionFactory sessionFactory;

    public LocalizacaoDaoHibernate(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void salvar(Localizacao localizacao) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                session.persist(localizacao);
                tx.commit();
            } catch (RuntimeException e) {
                tx.rollback();
                throw e;
            }
        }
    }

    @Override
    public Optional<Localizacao> buscarPorCidade(String cidade) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("from Localizacao where city = :cidade", Localizacao.class)
                    .setParameter("cidade", cidade)
                    .setMaxResults(1)
                    .uniqueResultOptional();
        }
    }

    @Override
    public List<Localizacao> listarTodas() {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("from Localizacao order by city", Localizacao.class)
                    .getResultList();
        }
    }
}