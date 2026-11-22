package com.empresa.emprestimos.service;

import com.empresa.emprestimos.exception.ConflitoException;
import com.empresa.emprestimos.exception.NaoEncontradoException;
import com.empresa.emprestimos.model.Colaborador;
import com.empresa.emprestimos.repository.ColaboradorRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ColaboradorService {

    private final ColaboradorRepository colaboradorRepository;

    public ColaboradorService(ColaboradorRepository colaboradorRepository) {
        this.colaboradorRepository = colaboradorRepository;
    }

    public List<Colaborador> listar() {
        return colaboradorRepository.findAll();
    }

    public Colaborador buscar(Long id) {
        return colaboradorRepository.findById(id)
                .orElseThrow(() -> new NaoEncontradoException("Colaborador não encontrado"));
    }

    public Colaborador cadastrar(Colaborador colaborador) {
        // R3: não existem duas pessoas colaboradoras com o mesmo e-mail
        if (colaboradorRepository.existsByEmail(colaborador.getEmail())) {
            throw new ConflitoException("E-mail já cadastrado");
        }
        colaborador.setId(null);
        return colaboradorRepository.save(colaborador);
    }
}
