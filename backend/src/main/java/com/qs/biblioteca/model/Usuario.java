package com.qs.biblioteca.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "usuarios")
public class Usuario {
	@Id
	private String id;

	private String nome;

	@Indexed(unique = true)
	private String email;

	private String senhaHash;
	private Role role = Role.USER;

	private String cep;
	private String logradouro;
	private String bairro;
	private String cidade;
	private String uf;
}
