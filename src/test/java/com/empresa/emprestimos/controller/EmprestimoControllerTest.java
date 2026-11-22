package com.empresa.emprestimos.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class EmprestimoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void cadastrarEquipamentoEColaborador() throws Exception {
        mockMvc.perform(post("/equipamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"patrimonio": "NB-001", "nome": "Notebook Dell", "tipo": "NOTEBOOK"}
                                """))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/colaboradores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "Ana Souza", "email": "ana@empresa.com", "cargo": "Analista"}
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void emprestarEDevolverResponde201E200() throws Exception {
        String hoje = LocalDate.now().toString();
        String prazo = LocalDate.now().plusDays(14).toString();

        mockMvc.perform(post("/emprestimos").param("equipamentoId", "1").param("colaboradorId", "1"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.dataEmprestimo").value(hoje))
                .andExpect(jsonPath("$.dataPrevistaDevolucao").value(prazo))
                .andExpect(jsonPath("$.atrasado").value(false));

        mockMvc.perform(get("/equipamentos/1"))
                .andExpect(jsonPath("$.disponivel").value(false));

        mockMvc.perform(put("/emprestimos/1/devolucao"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dataDevolucao").value(hoje));

        mockMvc.perform(get("/equipamentos/1"))
                .andExpect(jsonPath("$.disponivel").value(true));
    }

    @Test
    void emprestarEquipamentoJaEmprestadoResponde409() throws Exception {
        mockMvc.perform(post("/emprestimos").param("equipamentoId", "1").param("colaboradorId", "1"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/emprestimos").param("equipamentoId", "1").param("colaboradorId", "1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem").value("Equipamento indisponível"));
    }

    @Test
    void emprestarParaColaboradorQueNaoExisteResponde404ENaoMexeNoEquipamento() throws Exception {
        mockMvc.perform(post("/emprestimos").param("equipamentoId", "1").param("colaboradorId", "99"))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/equipamentos/1"))
                .andExpect(jsonPath("$.disponivel").value(true));
    }

    @Test
    void devolverDuasVezesResponde409() throws Exception {
        mockMvc.perform(post("/emprestimos").param("equipamentoId", "1").param("colaboradorId", "1"))
                .andExpect(status().isCreated());
        mockMvc.perform(put("/emprestimos/1/devolucao")).andExpect(status().isOk());

        mockMvc.perform(put("/emprestimos/1/devolucao"))
                .andExpect(status().isConflict());
    }
}
