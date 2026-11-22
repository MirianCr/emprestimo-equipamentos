package com.empresa.emprestimos.model;

import com.empresa.emprestimos.exception.ConflitoException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDate;

@Entity
public class Emprestimo {

    private static final int DIAS_DE_EMPRESTIMO = 14;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long equipamentoId;

    @Column(nullable = false)
    private Long colaboradorId;

    @Column(nullable = false)
    private LocalDate dataEmprestimo;

    @Column(nullable = false)
    private LocalDate dataPrevistaDevolucao;

    private LocalDate dataDevolucao;

    // o JPA exige: não apaguem
    public Emprestimo() {
    }

    // R5: guarda os ids, empresta hoje e prevê a devolução para hoje + 14 dias
    public Emprestimo(Long equipamentoId, Long colaboradorId, LocalDate hoje) {
        this.equipamentoId = equipamentoId;
        this.colaboradorId = colaboradorId;
        this.dataEmprestimo = hoje;
        this.dataPrevistaDevolucao = hoje.plusDays(DIAS_DE_EMPRESTIMO);
    }

    // R6 (não devolve duas vezes) e a data da R7
    public void devolver(LocalDate data) {
        if (dataDevolucao != null) {
            throw new ConflitoException("Empréstimo já devolvido");
        }
        this.dataDevolucao = data;
    }

    // R8: atrasado quando ainda não foi devolvido e a data prevista já passou
    public boolean estaAtrasadoEm(LocalDate data) {
        return dataDevolucao == null && dataPrevistaDevolucao.isBefore(data);
    }

    // aparece no JSON como "atrasado"
    public boolean isAtrasado() {
        return estaAtrasadoEm(LocalDate.now());
    }

    public Long getId() {
        return id;
    }

    public Long getEquipamentoId() {
        return equipamentoId;
    }

    public Long getColaboradorId() {
        return colaboradorId;
    }

    public LocalDate getDataEmprestimo() {
        return dataEmprestimo;
    }

    public LocalDate getDataPrevistaDevolucao() {
        return dataPrevistaDevolucao;
    }

    public LocalDate getDataDevolucao() {
        return dataDevolucao;
    }
}
