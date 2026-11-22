package com.empresa.emprestimos.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
class EquipamentoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void cadastrarEquipamentoResponde201ENasceDisponivel() throws Exception {
        mockMvc.perform(post("/equipamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"patrimonio": "MN-001", "nome": "Monitor LG",
                                 "tipo": "MONITOR", "disponivel": false}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.disponivel").value(true));
    }

    @Test
    void buscarEquipamentoQueNaoExisteResponde404() throws Exception {
        mockMvc.perform(get("/equipamentos/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Equipamento não encontrado"));
    }

    @Test
    void cadastrarComPatrimonioRepetidoResponde409() throws Exception {
        String corpo = """
                {"patrimonio": "NB-001", "nome": "Notebook Dell", "tipo": "NOTEBOOK"}
                """;
        mockMvc.perform(post("/equipamentos").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/equipamentos").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isConflict());
    }

    @Test
    void cadastrarSemNomeResponde400ComOsCampos() throws Exception {
        mockMvc.perform(post("/equipamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"patrimonio": "NB-002", "tipo": "NOTEBOOK"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos[0].campo").value("nome"));
    }
}
