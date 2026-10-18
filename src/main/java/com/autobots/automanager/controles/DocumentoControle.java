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
import com.autobots.automanager.entidades.Documento;
import com.autobots.automanager.modelos.AdicionadorLinkDocumento;
import com.autobots.automanager.modelos.DocumentoAtualizador;
import com.autobots.automanager.modelos.DocumentoSelecionador;
import com.autobots.automanager.repositorios.ClienteRepositorio;
import com.autobots.automanager.repositorios.DocumentoRepositorio;

@RestController
public class DocumentoControle {
	@Autowired
	private DocumentoRepositorio repositorio;
	@Autowired
	private ClienteRepositorio clienteRepositorio;
	@Autowired
	private DocumentoSelecionador selecionador;
	@Autowired
	private AdicionadorLinkDocumento adicionadorLink;

	@GetMapping("/documento/{id}")
	public ResponseEntity<Documento> obterDocumento(@PathVariable long id) {
		List<Documento> documentos = repositorio.findAll();
		Documento documento = selecionador.selecionar(documentos, id);
		if (documento == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		adicionadorLink.adicionarLink(documento);
		return new ResponseEntity<Documento>(documento, HttpStatus.OK);
	}

	@GetMapping("/documentos")
	public ResponseEntity<List<Documento>> obterDocumentos() {
		List<Documento> documentos = repositorio.findAll();
		if (documentos.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		adicionadorLink.adicionarLink(documentos);
		return new ResponseEntity<List<Documento>>(documentos, HttpStatus.OK);
	}

	@PostMapping("/documento/cadastro")
	public ResponseEntity<Documento> cadastrarDocumento(@RequestBody Documento documento) {
		if (documento.getId() != null) {
			return new ResponseEntity<>(HttpStatus.CONFLICT);
		}
		try {
			Documento salvo = repositorio.save(documento);
			adicionadorLink.adicionarLink(salvo);
			return ResponseEntity
					.created(salvo.getRequiredLink(IanaLinkRelations.SELF).toUri())
					.body(salvo);
		} catch (DataIntegrityViolationException e) {
			return new ResponseEntity<>(HttpStatus.CONFLICT);
		}
	}

	@PutMapping("/documento/atualizar")
	public ResponseEntity<Documento> atualizarDocumento(@RequestBody Documento atualizacao) {
		if (atualizacao.getId() == null) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		Documento documento = selecionador.selecionar(repositorio.findAll(), atualizacao.getId());
		if (documento == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		try {
			DocumentoAtualizador atualizador = new DocumentoAtualizador();
			atualizador.atualizar(documento, atualizacao);
			Documento salvo = repositorio.save(documento);
			adicionadorLink.adicionarLink(salvo);
			return new ResponseEntity<Documento>(salvo, HttpStatus.OK);
		} catch (DataIntegrityViolationException e) {
			return new ResponseEntity<>(HttpStatus.CONFLICT);
		}
	}

	@DeleteMapping("/documento/excluir")
	public ResponseEntity<?> excluirDocumento(@RequestBody Documento exclusao) {
		if (exclusao.getId() == null) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		Documento documento = selecionador.selecionar(repositorio.findAll(), exclusao.getId());
		if (documento == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		Long id = documento.getId();
		boolean vinculado = false;
		// Se pertencer a um cliente, remove do cliente (orphanRemoval apaga o registro)
		for (Cliente cliente : clienteRepositorio.findAll()) {
			if (cliente.getDocumentos().removeIf(item -> item.getId().equals(id))) {
				clienteRepositorio.save(cliente);
				vinculado = true;
			}
		}
		if (!vinculado) {
			repositorio.delete(documento);
		}
		return new ResponseEntity<>(HttpStatus.OK);
	}
}
