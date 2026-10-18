package com.autobots.automanager.controles;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.autobots.automanager.entidades.Cliente;
import com.autobots.automanager.entidades.Endereco;
import com.autobots.automanager.modelos.AdicionadorLinkEndereco;
import com.autobots.automanager.modelos.EnderecoAtualizador;
import com.autobots.automanager.modelos.EnderecoSelecionador;
import com.autobots.automanager.repositorios.ClienteRepositorio;
import com.autobots.automanager.repositorios.EnderecoRepositorio;

@RestController
public class EnderecoControle {
	@Autowired
	private EnderecoRepositorio repositorio;
	@Autowired
	private ClienteRepositorio clienteRepositorio;
	@Autowired
	private EnderecoSelecionador selecionador;
	@Autowired
	private AdicionadorLinkEndereco adicionadorLink;

	@GetMapping("/endereco/{id}")
	public ResponseEntity<Endereco> obterEndereco(@PathVariable long id) {
		List<Endereco> enderecos = repositorio.findAll();
		Endereco endereco = selecionador.selecionar(enderecos, id);
		if (endereco == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		adicionadorLink.adicionarLink(endereco);
		return new ResponseEntity<Endereco>(endereco, HttpStatus.OK);
	}

	@GetMapping("/enderecos")
	public ResponseEntity<List<Endereco>> obterEnderecos() {
		List<Endereco> enderecos = repositorio.findAll();
		if (enderecos.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		adicionadorLink.adicionarLink(enderecos);
		return new ResponseEntity<List<Endereco>>(enderecos, HttpStatus.OK);
	}

	@PostMapping("/endereco/cadastro")
	public ResponseEntity<Endereco> cadastrarEndereco(@RequestBody Endereco endereco) {
		if (endereco.getId() != null) {
			return new ResponseEntity<>(HttpStatus.CONFLICT);
		}
		try {
			Endereco salvo = repositorio.save(endereco);
			adicionadorLink.adicionarLink(salvo);
			return ResponseEntity
					.created(salvo.getRequiredLink(IanaLinkRelations.SELF).toUri())
					.body(salvo);
		} catch (DataIntegrityViolationException e) {
			// cidade, rua e numero são obrigatórios
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@PutMapping("/endereco/atualizar")
	public ResponseEntity<Endereco> atualizarEndereco(@RequestBody Endereco atualizacao) {
		if (atualizacao.getId() == null) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		Endereco endereco = selecionador.selecionar(repositorio.findAll(), atualizacao.getId());
		if (endereco == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		try {
			EnderecoAtualizador atualizador = new EnderecoAtualizador();
			atualizador.atualizar(endereco, atualizacao);
			Endereco salvo = repositorio.save(endereco);
			adicionadorLink.adicionarLink(salvo);
			return new ResponseEntity<Endereco>(salvo, HttpStatus.OK);
		} catch (DataIntegrityViolationException e) {
			return new ResponseEntity<>(HttpStatus.CONFLICT);
		}
	}

	@DeleteMapping("/endereco/excluir")
	public ResponseEntity<?> excluirEndereco(@RequestBody Endereco exclusao) {
		if (exclusao.getId() == null) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		Endereco endereco = selecionador.selecionar(repositorio.findAll(), exclusao.getId());
		if (endereco == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		Long id = endereco.getId();
		boolean vinculado = false;
		// Se pertencer a um cliente, remove do cliente (orphanRemoval apaga o registro)
		for (Cliente cliente : clienteRepositorio.findAll()) {
			Endereco atual = cliente.getEndereco();
			if (atual != null && atual.getId().equals(id)) {
				cliente.setEndereco(null);
				clienteRepositorio.save(cliente);
				vinculado = true;
			}
		}
		if (!vinculado) {
			repositorio.delete(endereco);
		}
		return new ResponseEntity<>(HttpStatus.OK);
	}
}
