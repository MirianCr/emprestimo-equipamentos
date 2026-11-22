package com.empresa.emprestimos.controller;

import com.empresa.emprestimos.model.Colaborador;
import com.empresa.emprestimos.model.Emprestimo;
import com.empresa.emprestimos.service.ColaboradorService;
import com.empresa.emprestimos.service.EmprestimoService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/colaboradores")
public class ColaboradorController {

    private final ColaboradorService colaboradorService;
    private final EmprestimoService emprestimoService;

    public ColaboradorController(ColaboradorService colaboradorService,
                                 EmprestimoService emprestimoService) {
        this.colaboradorService = colaboradorService;
        this.emprestimoService = emprestimoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Colaborador cadastrar(@Valid @RequestBody Colaborador colaborador) {
        return colaboradorService.cadastrar(colaborador);
    }

    @GetMapping
    public List<Colaborador> listar() {
        return colaboradorService.listar();
    }

    @GetMapping("/{id}")
    public Colaborador buscar(@PathVariable Long id) {
        return colaboradorService.buscar(id);
    }

    @GetMapping("/{id}/emprestimos")
    public List<Emprestimo> historico(@PathVariable Long id) {
        return emprestimoService.listarDoColaborador(id);
    }
}
