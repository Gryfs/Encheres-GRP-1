package fr.eni.enchere.dal;

import fr.eni.enchere.bo.Retrait;

public interface RetraitDAO {
	
	Retrait consulterRetraitParIdarticle(long id);

}
