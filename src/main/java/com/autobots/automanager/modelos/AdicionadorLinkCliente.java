package com.autobots.automanager.modelos;

import java.util.List;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

import com.autobots.automanager.controles.ClienteControle;
import com.autobots.automanager.controles.DocumentoControle;
import com.autobots.automanager.controles.EnderecoControle;
import com.autobots.automanager.controles.TelefoneControle;
import com.autobots.automanager.entidades.Cliente;
import com.autobots.automanager.entidades.Documento;
import com.autobots.automanager.entidades.Endereco;
import com.autobots.automanager.entidades.Telefone;

@Component
public class AdicionadorLinkCliente implements AdicionadorLink<Cliente> {

	@Override
	public void adicionarLink(List<Cliente> lista) {
		for (Cliente cliente : lista) {
			cliente.add(linkProprio(cliente));
			adicionarLinksRelacionados(cliente);
		}
	}

	@Override
	public void adicionarLink(Cliente objeto) {
		objeto.add(linkProprio(objeto));
		Link linkLista = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder
						.methodOn(ClienteControle.class)
						.obterClientes())
				.withRel("clientes");
		objeto.add(linkLista);
		adicionarLinksRelacionados(objeto);
	}

	private Link linkProprio(Cliente cliente) {
		long id = cliente.getId();
		return WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder
						.methodOn(ClienteControle.class)
						.obterCliente(id))
				.withSelfRel();
	}

	private void adicionarLinksRelacionados(Cliente cliente) {
		for (Documento documento : cliente.getDocumentos()) {
			long id = documento.getId();
			documento.add(WebMvcLinkBuilder
					.linkTo(WebMvcLinkBuilder
							.methodOn(DocumentoControle.class)
							.obterDocumento(id))
					.withSelfRel());
		}
		Endereco endereco = cliente.getEndereco();
		if (endereco != null) {
			long id = endereco.getId();
			endereco.add(WebMvcLinkBuilder
					.linkTo(WebMvcLinkBuilder
							.methodOn(EnderecoControle.class)
							.obterEndereco(id))
					.withSelfRel());
		}
		for (Telefone telefone : cliente.getTelefones()) {
			long id = telefone.getId();
			telefone.add(WebMvcLinkBuilder
					.linkTo(WebMvcLinkBuilder
							.methodOn(TelefoneControle.class)
							.obterTelefone(id))
					.withSelfRel());
		}
	}
}
