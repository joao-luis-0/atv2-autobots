package com.autobots.automanager.modelos;

import java.util.List;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

import com.autobots.automanager.controles.TelefoneControle;
import com.autobots.automanager.entidades.Telefone;

@Component
public class AdicionadorLinkTelefone implements AdicionadorLink<Telefone> {

	@Override
	public void adicionarLink(List<Telefone> lista) {
		for (Telefone telefone : lista) {
			telefone.add(linkProprio(telefone));
		}
	}

	@Override
	public void adicionarLink(Telefone objeto) {
		objeto.add(linkProprio(objeto));
		Link linkLista = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder
						.methodOn(TelefoneControle.class)
						.obterTelefones())
				.withRel("telefones");
		objeto.add(linkLista);
	}

	private Link linkProprio(Telefone telefone) {
		long id = telefone.getId();
		return WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder
						.methodOn(TelefoneControle.class)
						.obterTelefone(id))
				.withSelfRel();
	}
}
