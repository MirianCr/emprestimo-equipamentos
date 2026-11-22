package com.empresa.emprestimos.service;

import com.empresa.emprestimos.exception.ConflitoException;
import com.empresa.emprestimos.exception.NaoEncontradoException;
import com.empresa.emprestimos.model.Emprestimo;
import com.empresa.emprestimos.model.Equipamento;
import com.empresa.emprestimos.repository.ColaboradorRepository;
import com.empresa.emprestimos.repository.EmprestimoRepository;
import com.empresa.emprestimos.repository.EquipamentoRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmprestimoService {

    private final EmprestimoRepository emprestimoRepository;
    private final EquipamentoRepository equipamentoRepository;
    private final ColaboradorRepository colaboradorRepository;

    public EmprestimoService(EmprestimoRepository emprestimoRepository,
                             EquipamentoRepository equipamentoRepository,
                             ColaboradorRepository colaboradorRepository) {
        this.emprestimoRepository = emprestimoRepository;
        this.equipamentoRepository = equipamentoRepository;
        this.colaboradorRepository = colaboradorRepository;
    }

    public List<Emprestimo> listar(Boolean atrasados) {
        // R8: o filtro só entra se veio exatamente true
        if (Boolean.TRUE.equals(atrasados)) {
            return emprestimoRepository
                    .findByDataDevolucaoIsNullAndDataPrevistaDevolucaoBefore(LocalDate.now());
        }
        return emprestimoRepository.findAll();
    }

    public Emprestimo buscar(Long id) {
        return emprestimoRepository.findById(id)
                .orElseThrow(() -> new NaoEncontradoException("Empréstimo não encontrado"));
    }

    public List<Emprestimo> listarDoColaborador(Long colaboradorId) {
        if (!colaboradorRepository.existsById(colaboradorId)) {
            throw new NaoEncontradoException("Colaborador não encontrado");
        }
        return emprestimoRepository.findByColaboradorId(colaboradorId);
    }

    @Transactional
    public Emprestimo emprestar(Long equipamentoId, Long colaboradorId) {
        LocalDate hoje = LocalDate.now();

        Equipamento equipamento = equipamentoRepository.findById(equipamentoId)
                .orElseThrow(() -> new NaoEncontradoException("Equipamento não encontrado"));

        if (!colaboradorRepository.existsById(colaboradorId)) {
            throw new NaoEncontradoException("Colaborador não encontrado");
        }

        // R10: quem está com empréstimo atrasado não pega outro equipamento
        if (emprestimoRepository.existsByColaboradorIdAndDataDevolucaoIsNullAndDataPrevistaDevolucaoBefore(
                colaboradorId, hoje)) {
            throw new ConflitoException("Colaborador está com um empréstimo atrasado");
        }

        // R4 e R5: só empresta se estiver disponível; o equipamento fica indisponível
        equipamento.emprestar();
        Emprestimo emprestimo = new Emprestimo(equipamentoId, colaboradorId, hoje);

        equipamentoRepository.save(equipamento);
        return emprestimoRepository.save(emprestimo);
    }

    @Transactional
    public Emprestimo devolver(Long id) {
        Emprestimo emprestimo = buscar(id);
        Equipamento equipamento = equipamentoRepository.findById(emprestimo.getEquipamentoId())
                .orElseThrow(() -> new NaoEncontradoException("Equipamento não encontrado"));

        // R6 e R7: não devolve duas vezes; data de devolução é hoje e o equipamento volta
        emprestimo.devolver(LocalDate.now());
        equipamento.devolver();

        equipamentoRepository.save(equipamento);
        return emprestimoRepository.save(emprestimo);
    }
}
