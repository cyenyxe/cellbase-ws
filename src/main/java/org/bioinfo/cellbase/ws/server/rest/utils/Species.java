package org.bioinfo.cellbase.ws.server.rest.utils;

public class Species {

	private String species;
	private String common;
	private String scientific;
	private String assembly;
	
	public Species(String shortName, String commonName, String scientificName, String assembly) {
		super();
		this.species = shortName;
		this.common = commonName;
		this.scientific = scientificName;
		this.assembly = assembly;
	}

	
	@Override
	public String toString() {
		return species + "\t" + common	+ "\t" + scientific + "\t" + assembly;
	}


	public String getSpecies() {
		return species;
	}


	public void setSpecies(String species) {
		this.species = species;
	}


	public String getCommon() {
		return common;
	}


	public void setCommon(String common) {
		this.common = common;
	}


	public String getScientific() {
		return scientific;
	}


	public void setScientific(String scientific) {
		this.scientific = scientific;
	}


	public String getAssembly() {
		return assembly;
	}


	public void setAssembly(String assembly) {
		this.assembly = assembly;
	}

}
