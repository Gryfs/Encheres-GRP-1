package fr.eni.enchere.controller.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import fr.eni.enchere.bll.EnchereService;
import fr.eni.enchere.bo.Categories;

@Component
public class StringToCategorieConverter implements Converter<String, Categories> {

	private EnchereService enchereService;

	public StringToCategorieConverter(EnchereService enchereService) {
		this.enchereService = enchereService;
	}

	@Override
	public Categories convert(String idCategorie) {
		System.out.println("convert idParticipant = " + idCategorie);

		return this.enchereService.consulterCategorieparId(Long.parseLong(idCategorie));

	}

}
