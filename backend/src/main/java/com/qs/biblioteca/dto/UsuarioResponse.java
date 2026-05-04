package com.qs.biblioteca.dto;

import com.qs.biblioteca.model.Role;
import com.qs.biblioteca.model.Usuario;
import lombok.Data;

import java.util.List;

@Data
public class UsuarioResponse {
    private String id;
    private String nome;
    private String nickname;
    private String email;
    private Role role;
    // Endereço legado
    private String cep;
    private String logradouro;
    private String bairro;
    private String cidade;
    private String uf;
    // Novos campos
    private List<Usuario.Endereco> enderecos;
    private List<String> telefones;
    private List<String> redesSociais;

    public UsuarioResponse(Usuario u) {
        this.id = u.getId();
        this.nome = u.getNome();
        this.nickname = u.getNickname();
        this.email = u.getEmail();
        this.role = u.getRole();
        this.cep = u.getCep();
        this.logradouro = u.getLogradouro();
        this.bairro = u.getBairro();
        this.cidade = u.getCidade();
        this.uf = u.getUf();
        this.enderecos = u.getEnderecos();
        this.telefones = u.getTelefones();
        this.redesSociais = u.getRedesSociais();
    }
}
