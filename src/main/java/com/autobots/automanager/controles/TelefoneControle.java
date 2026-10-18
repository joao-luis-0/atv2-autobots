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
import com.autobots.automanager.entidades.Telefone;
import com.autobots.automanager.modelos.AdicionadorLinkTelefone;
import com.autobots.automanager.modelos.TelefoneAtualizador;
import com.autobots.automanager.modelos.TelefoneSelecionador;
import com.autobots.automanager.repositorios.ClienteRepositorio;
import com.autobots.automanager.repositorios.TelefoneRepositorio;

@RestController
public class TelefoneControle {
	@Autowired
	private TelefoneRepositorio repositorio;
	@Autowired
	private ClienteRepositorio clienteRepositorio;
	@Autowired
	private TelefoneSelecionador selecionador;
	@Autowired
	private AdicionadorLinkTelefone adicionadorLink;

	@GetMapping("/telefone/{id}")
	public ResponseEntity<Telefone> obterTelefone(@PathVariable long id) {
		List<Telefone> telefones = repositorio.findAll();
		Telefone telefone = selecionador.selecionar(telefones, id);
		if (telefone == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		adicionadorLink.adicionarLink(telefone);
		return new ResponseEntity<Telefone>(telefone, HttpStatus.OK);
	}

	@GetMapping("/telefones")
	public ResponseEntity<List<Telefone>> obterTelefones() {
		List<Telefone> telefones = repositorio.findAll();
		if (telefones.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		adicionadorLink.adicionarLink(telefones);
		return new ResponseEntity<List<Telefone>>(telefones, HttpStatus.OK);
	}

	@PostMapping("/telefone/cadastro")
	public ResponseEntity<Telefone> cadastrarTelefone(@RequestBody Telefone telefone) {
		if (telefone.getId() != null) {
			return new ResponseEntity<>(HttpStatus.CONFLICT);
		}
		try {
			Telefone salvo = repositorio.save(telefone);
			adicionadorLink.adicionarLink(salvo);
			return ResponseEntity
					.created(salvo.getRequiredLink(IanaLinkRelations.SELF).toUri())
					.body(salvo);
		} catch (DataIntegrityViolationException e) {
			return new ResponseEntity<>(HttpStatus.CONFLICT);
		}
	}

	@PutMapping("/telefone/atualizar")
	public ResponseEntity<Telefone> atualizarTelefone(@RequestBody Telefone atualizacao) {
		if (atualizacao.getId() == null) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		Telefone telefone = selecionador.selecionar(repositorio.findAll(), atualizacao.getId());
		if (telefone == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		try {
			TelefoneAtualizador atualizador = new TelefoneAtualizador();
			atualizador.atualizar(telefone, atualizacao);
			Telefone salvo = repositorio.save(telefone);
			adicionadorLink.adicionarLink(salvo);
			return new ResponseEntity<Telefone>(salvo, HttpStatus.OK);
		} catch (DataIntegrityViolationException e) {
			return new ResponseEntity<>(HttpStatus.CONFLICT);
		}
	}

	@DeleteMapping("/telefone/excluir")
	public ResponseEntity<?> excluirTelefone(@RequestBody Telefone exclusao) {
		if (exclusao.getId() == null) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		Telefone telefone = selecionador.selecionar(repositorio.findAll(), exclusao.getId());
		if (telefone == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		Long id = telefone.getId();
		boolean vinculado = false;
		// Se pertencer a um cliente, remove do cliente (orphanRemoval apaga o registro)
		for (Cliente cliente : clienteRepositorio.findAll()) {
			if (cliente.getTelefones().removeIf(item -> item.getId().equals(id))) {
				clienteRepositorio.save(cliente);
				vinculado = true;
			}
		}
		if (!vinculado) {
			repositorio.delete(telefone);
		}
		return new ResponseEntity<>(HttpStatus.OK);
	}
}
