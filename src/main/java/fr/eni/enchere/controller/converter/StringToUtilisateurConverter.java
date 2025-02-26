package fr.eni.enchere.controller.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import fr.eni.enchere.bll.ContexteService;
import fr.eni.enchere.bo.Utilisateur;

@Component
public class StringToUtilisateurConverter implements Converter<String, Utilisateur> {

	private ContexteService contexteService;

	public StringToUtilisateurConverter(ContexteService contexteService) {
		this.contexteService = contexteService;
	}

	@Override
	public Utilisateur convert(String idUtilisateur) {
		System.out.println("convert idUtilisateur = " + idUtilisateur);

		return this.contexteService.chargerAvecId(Long.parseLong(idUtilisateur));

	}

}
