package dao;

import modelo.Localizacao;
import modelo.PrevisaoDoCLima;

import java.util.List;

public interface PrevisaoDao {
    void salvar(PrevisaoDoCLima previsao);
    List<PrevisaoDoCLima> listarPorLocalizacao(Localizacao localizacao);
}