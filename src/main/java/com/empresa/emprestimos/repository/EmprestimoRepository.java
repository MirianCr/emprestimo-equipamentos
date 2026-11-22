package com.empresa.emprestimos.repository;

import com.empresa.emprestimos.model.Emprestimo;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmprestimoRepository extends JpaRepository<Emprestimo, Long> {

    // histórico da pessoa
    List<Emprestimo> findByColaboradorId(Long colaboradorId);

    // R9
    boolean existsByEquipamentoId(Long equipamentoId);

    // R8, filtro
    List<Emprestimo> findByDataDevolucaoIsNullAndDataPrevistaDevolucaoBefore(LocalDate data);

    // R10
    boolean existsByColaboradorIdAndDataDevolucaoIsNullAndDataPrevistaDevolucaoBefore(
            Long colaboradorId, LocalDate data);
}
