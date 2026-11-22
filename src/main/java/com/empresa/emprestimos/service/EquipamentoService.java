package com.empresa.emprestimos.service;

import com.empresa.emprestimos.exception.ConflitoException;
import com.empresa.emprestimos.exception.NaoEncontradoException;
import com.empresa.emprestimos.model.Equipamento;
import com.empresa.emprestimos.model.TipoEquipamento;
import com.empresa.emprestimos.repository.EmprestimoRepository;
import com.empresa.emprestimos.repository.EquipamentoRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class EquipamentoService {

    private final EquipamentoRepository equipamentoRepository;
    private final EmprestimoRepository emprestimoRepository;

    public EquipamentoService(EquipamentoRepository equipamentoRepository,
                              EmprestimoRepository emprestimoRepository) {
        this.equipamentoRepository = equipamentoRepository;
        this.emprestimoRepository = emprestimoRepository;
    }

    public List<Equipamento> listar(TipoEquipamento tipo, Boolean disponivel) {
        if (tipo != null && disponivel != null) {
            return equipamentoRepository.findByTipoAndDisponivel(tipo, disponivel);
        }
        if (tipo != null) {
            return equipamentoRepository.findByTipo(tipo);
        }
        if (disponivel != null) {
            return equipamentoRepository.findByDisponivel(disponivel);
        }
        return equipamentoRepository.findAll();
    }

    public Equipamento buscar(Long id) {
        return equipamentoRepository.findById(id)
                .orElseThrow(() -> new NaoEncontradoException("Equipamento não encontrado"));
    }

    public Equipamento cadastrar(Equipamento equipamento) {
        // R1: não existem dois equipamentos com o mesmo patrimônio
        if (equipamentoRepository.existsByPatrimonio(equipamento.getPatrimonio())) {
            throw new ConflitoException("Patrimônio já cadastrado");
        }
        // R2: todo equipamento novo nasce disponível, mesmo que o JSON diga o contrário
        equipamento.setId(null);
        equipamento.setDisponivel(true);
        return equipamentoRepository.save(equipamento);
    }

    public Equipamento atualizar(Long id, Equipamento dados) {
        // copia só nome e tipo: patrimônio e disponibilidade não mudam pelo PUT
        Equipamento equipamento = buscar(id);
        equipamento.setNome(dados.getNome());
        equipamento.setTipo(dados.getTipo());
        return equipamentoRepository.save(equipamento);
    }

    public void remover(Long id) {
        Equipamento equipamento = buscar(id);
        // R9: equipamento com histórico de empréstimo não pode ser removido
        if (emprestimoRepository.existsByEquipamentoId(id)) {
            throw new ConflitoException("Equipamento tem histórico de empréstimos e não pode ser removido");
        }
        equipamentoRepository.delete(equipamento);
    }
}
