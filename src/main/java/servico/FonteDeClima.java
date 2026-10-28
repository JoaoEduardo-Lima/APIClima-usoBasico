package servico;

import modelo.Localizacao;
import modelo.PrevisaoDoCLima;

public interface FonteDeClima extends AutoCloseable {

    PrevisaoDoCLima buscar(Localizacao local) throws Exception;

    @Override
    void close();   // sem "throws Exception", como já é no ClienteClima
}