package dao;

import modelo.Localizacao;

import java.util.List;
import java.util.Optional;

public interface LocalizacaoDao {
    void salvar(Localizacao localizacao);
    Optional<Localizacao> buscarPorCidade(String cidade);
    List<Localizacao> listarTodas();
}