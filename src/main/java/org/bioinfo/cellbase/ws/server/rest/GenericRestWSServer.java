package org.bioinfo.cellbase.ws.server.rest;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.bioinfo.cellbase.lib.impl.DBAdaptorFactory;
import org.bioinfo.cellbase.lib.impl.mongodb.MongoDBAdaptorFactory;
import org.bioinfo.cellbase.ws.server.rest.exception.SpeciesException;
import org.bioinfo.cellbase.ws.server.rest.exception.VersionException;
import org.bioinfo.cellbase.ws.server.rest.utils.Species;
import org.bioinfo.commons.Config;
import org.bioinfo.commons.utils.ListUtils;
import org.bioinfo.commons.utils.StringUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

@Path("/{version}")
@Produces("text/plain")
public class GenericRestWSServer implements IWSServer {

	// Common application parameters
	protected String version;
	protected String species;
	protected UriInfo uriInfo;
	protected HttpServletRequest httpServletRequest;

	// Common output parameters
	protected String resultSeparator;
	protected String querySeparator;

	// output format file type: null or txt or text, xml, excel
	protected String fileFormat;

	// file name without extension which server will give back when file format
	// is !null
	private String filename;

	// output content format: txt or text, json, xml, das
	protected String outputFormat;

	// in file output produces a zip file, in text outputs generates a gzipped
	// output
	protected String outputCompress;

	// only in text format
	protected String outputRowNames;
	protected String outputHeader;

	protected String user;
	protected String password;

	protected Type listType;

	// private MediaType mediaType;
	protected Gson gson;
	// protected Logger logger;
	protected Logger logger = Logger.getLogger(this.getClass().getSimpleName());

	private static final String NEW_LINE = "newline";
	private static final String TAB = "tab";

	/**
	 * DBAdaptorFactory creation, this object can be initialize with an
	 * HibernateDBAdaptorFactory or an HBaseDBAdaptorFactory. This object is a
	 * factory for creating adaptors like GeneDBAdaptor
	 */
	protected static DBAdaptorFactory dbAdaptorFactory;
	static {
		BasicConfigurator.configure();

		// dbAdaptorFactory = new HibernateDBAdaptorFactory();
		dbAdaptorFactory = new MongoDBAdaptorFactory();
		System.out.println("static1");
	}

	/**
	 * Loading properties file just one time to be more efficient. All methods
	 * will check parameters so to avoid extra operations this config can load
	 * versions and species
	 */
	protected static Config config;
	protected static Map<String, Set<String>> availableVersionSpeciesMap; // stores
																			// species
																			// for
																			// each
																			// version
	static {
		try {
			config = new Config(ResourceBundle.getBundle("org.bioinfo.infrared.ws.application"));
			availableVersionSpeciesMap = new HashMap<String, Set<String>>();
			if (config != null && config.containsKey("CELLBASE.AVAILABLE.VERSIONS")) {
				// read all versions available
				List<String> versionList = config.getListProperty("CELLBASE.AVAILABLE.VERSIONS", ",");
				if (versionList != null) {
					for (String version : versionList) {
						availableVersionSpeciesMap.put(version.trim(), new HashSet<String>());
						if (config.containsKey("CELLBASE." + version.toUpperCase() + ".AVAILABLE.SPECIES")) {
							// read the species available for each version
							List<String> speciesList = config.getListProperty("CELLBASE." + version.toUpperCase()
									+ ".AVAILABLE.SPECIES", ",");
							if (speciesList != null) {
								for (String species : speciesList) {
									availableVersionSpeciesMap.get(version.trim()).add(species.trim());
								}
							}
						}
					}
				}
			}
			// System.out.println("static2: "+availableVersions.toString());
			System.out.println("static2: " + availableVersionSpeciesMap.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Preparing headers columns for text output
	 */
	protected static Map<String, String> headers;
	static {
		System.out.println("static 3: Adding headers to static Map...");
		headers = new HashMap<String, String>();
		headers.put("GENE",
				"Ensembl gene,external name,external name source,biotype,status,chromosome,start,end,strand,source,description"
						.replaceAll(",", "\t"));
		headers.put(
				"TRANSCRIPT",
				"Ensembl ID,external name,external name source,biotype,status,chromosome,start,end,strand,coding region start,coding region end,cdna coding start,cdna coding end,description"
						.replaceAll(",", "\t"));
		headers.put("EXON", "Ensembl ID,chromosome,start,end,strand".replaceAll(",", "\t"));
		headers.put("SNP",
				"rsID,chromosome,position,Ensembl consequence type,SO consequence type,sequence".replaceAll(",", "\t"));
		headers.put(
				"SNP_PHENOTYPE",
				"SNP name,source,associated gene name,risk allele,risk allele freq in controls,p-value,phenotype name,phenotype description,study name,study type,study URL,study description"
						.replaceAll(",", "\t"));
		headers.put(
				"SNP_POPULATION_FREQUENCY",
				"SNP name,population,source,ref allele,ref allele freq,other allele,other allele freq,ref allele homozygote,ref allele homozygote freq,allele heterozygote,allele heterozygote freq,ref other allele homozygote, ref other allele homozygote freq"
						.replaceAll(",", "\t"));
		headers.put("SNP_REGULATORY",
				"SNP name,feature name,feature type,chromsome,start,end,strand,Ensembl transcript ID,Ensembl gene ID,gene name,biotype"
						.replaceAll(",", "\t"));
		headers.put(
				"GENOMIC_VARIANT_EFFECT",
				"chromosome,position,reference allele,alternative allele,feature ID,feature name,feature type,feature chromsomome,feature start,feature end,feature strand,SNP name,ancestral allele,alternative allele,Ensembl gene ID,Ensembl transcript ID,gene name,SO consequence type ID,SO consequence type name,consequence type description,consequence type category,aminoacid position,aminoacid change,codon change"
						.replaceAll(",", "\t"));
		headers.put("SNP_CONSEQUENCE_TYPE",
				"SNP name,chromosome,start,end,strand,allele,transcript ID,gene,SO accession,SO term,label,description"
						.replaceAll(",", "\t"));
		headers.put(
				"MUTATION",
				"chromosome,start,end,gene_name,uniprot_name,ensembl_transcript,primary_site,site_subtype,primary_histology,mutation_cds,mutation_aa,mutation_description,mutation_zigosity,pubmed_id,description,source"
						.replaceAll(",", "\t"));
		headers.put("STRUCTURAL_VARIATION",
				"display_id,chromosome,start,end,strand,so_term,study_name,study_url,study_description,source,source_description"
						.replaceAll(",", "\t"));
		headers.put("TFBS",
				"TF name,target gene name,chromosome,start,end,cell type,sequence,score".replaceAll(",", "\t"));
		headers.put("MIRNA_GENE", "miRBase accession,miRBase ID,status,sequence,source".replaceAll(",", "\t"));
		headers.put("MIRNA_MATURE", "miRBase accession,miRBase ID,sequence".replaceAll(",", "\t"));
		headers.put("MIRNA_TARGET",
				"miRBase ID,gene target name,chromosome,start,end,strand,pubmed ID,source".replaceAll(",", "\t"));
		headers.put("MIRNA_DISEASE", "miRBase ID,disease name,pubmed ID,description".replaceAll(",", "\t"));
		headers.put("REGULATORY_REGION", "name,type,chromosome,start,end,cell type,source".replaceAll(",", "\t"));
		headers.put("PROTEIN", "UniProt accession,protein name,full name,gene name,organism".replaceAll(",", "\t"));
		headers.put("PROTEIN_FEATURE",
				"feature type,aa start,aa end,original,variation,identifier,description".replaceAll(",", "\t"));
		headers.put("XREF", "ID,description".replaceAll(",", "\t"));
		headers.put("PATHWAY", "".replaceAll(",", "\t"));
	}

	@Deprecated
	public GenericRestWSServer(@PathParam("version") String version) {
		this.version = version;
	}

	@Deprecated
	public GenericRestWSServer(@PathParam("version") String version, @Context UriInfo uriInfo,
			@Context HttpServletRequest hsr) throws VersionException, IOException {
		this.version = version;
		this.species = "";
		this.uriInfo = uriInfo;
		this.httpServletRequest = hsr;

		init(version, this.species, uriInfo);
		// if(version != null && this.species != null) {
		// }
		// System.out.println("constructor");
	}

	public GenericRestWSServer(@PathParam("version") String version, @PathParam("species") String species,
			@Context UriInfo uriInfo, @Context HttpServletRequest hsr) throws VersionException, IOException {

		this.version = version;
		this.species = species;
		this.uriInfo = uriInfo;
		this.httpServletRequest = hsr;

		init(version, species, uriInfo);

		System.out.println("constructor4");

		// if(version != null && species != null) {
		// }
	}

	protected void init(String version, String species, UriInfo uriInfo) throws VersionException, IOException {
		// load properties file
		// ResourceBundle databaseConfig =
		// ResourceBundle.getBundle("org.bioinfo.infrared.ws.application");
		// config = new Config(databaseConfig);

		// mediaType = MediaType.valueOf("text/plain");
		gson = new GsonBuilder().serializeNulls().setExclusionStrategies(new FeatureExclusionStrategy()).create();

		// logger = new Logger();
		// logger.setLevel(Logger.DEBUG_LEVEL);
		logger.debug("GenericrestWSServer init method");

		/**
		 * Check version parameter, must be: v1, v2, ... If 'latest' then is
		 * converted to appropriate version
		 */
		// if(version != null && version.equals("latest") &&
		// config.getProperty("CELLBASE.LATEST.VERSION") != null) {
		// version = config.getProperty("CELLBASE.LATEST.VERSION");
		// System.out.println("version init: "+version);
		// }

		// this code MUST be run before the checking
		parseCommonQueryParameters(uriInfo.getQueryParameters());
	}

	/**
	 * This method parse common query parameters from the URL
	 * 
	 * @param multivaluedMap
	 */
	private void parseCommonQueryParameters(MultivaluedMap<String, String> multivaluedMap) {
		if (multivaluedMap.get("result_separator") != null) {
			if (multivaluedMap.get("result_separator").get(0).equalsIgnoreCase(NEW_LINE)) {
				resultSeparator = "\n";
			} else {
				if (multivaluedMap.get("result_separator").get(0).equalsIgnoreCase(TAB)) {
					resultSeparator = "\t";
				} else {
					resultSeparator = multivaluedMap.get("result_separator").get(0);
				}
			}
		} else {
			resultSeparator = "//";
		}

		if (multivaluedMap.get("query_separator") != null) {
			if (multivaluedMap.get("query_separator").get(0).equalsIgnoreCase(NEW_LINE)) {
				querySeparator = "\n";
			} else {
				if (multivaluedMap.get("query_separator").get(0).equalsIgnoreCase(TAB)) {
					querySeparator = "\t";
				} else {
					querySeparator = multivaluedMap.get("query_separator").get(0);
				}
			}
		} else {
			querySeparator = "\n";
		}

		fileFormat = (multivaluedMap.get("fileformat") != null) ? multivaluedMap.get("fileformat").get(0) : "";
		outputFormat = (multivaluedMap.get("of") != null) ? multivaluedMap.get("of").get(0) : "txt";
		// outputFormat = (multivaluedMap.get("contentformat") != null) ?
		// multivaluedMap.get("contentformat").get(0) : "txt";
		filename = (multivaluedMap.get("filename") != null) ? multivaluedMap.get("filename").get(0) : "result";
		outputRowNames = (multivaluedMap.get("outputrownames") != null) ? multivaluedMap.get("outputrownames").get(0)
				: "false";
		outputHeader = (multivaluedMap.get("header") != null) ? multivaluedMap.get("header").get(0) : "true";
		outputCompress = (multivaluedMap.get("outputcompress") != null) ? multivaluedMap.get("outputcompress").get(0)
				: "false";

		user = (multivaluedMap.get("user") != null) ? multivaluedMap.get("user").get(0) : "anonymous";
		password = (multivaluedMap.get("password") != null) ? multivaluedMap.get("password").get(0) : "";
	}

	/**
	 * Overriden methods
	 */

	@Override
	public void checkVersionAndSpecies() throws VersionException, SpeciesException {
		if (version == null) {
			throw new VersionException("Version not valid: '" + version + "'");
		}
		if (species == null) {
			throw new SpeciesException("Species not valid: '" + species + "'");
		}

		/**
		 * Check version parameter, must be: v1, v2, ... If 'latest' then is
		 * converted to appropriate version
		 */
		if (version != null && version.equals("latest") && config.getProperty("CELLBASE.LATEST.VERSION") != null) {
			version = config.getProperty("CELLBASE.LATEST.VERSION");
			System.out.println("version: " + version);
		}

		if (availableVersionSpeciesMap.containsKey(version)) {
			if (!availableVersionSpeciesMap.get(version).contains(species)) {
				throw new SpeciesException("Species not valid: '" + species + "' for version: '" + version + "'");
			}
		} else {
			throw new VersionException("Version not valid: '" + version + "'");
		}
	}

	@Override
	public String stats() {
		return null;
	}

	@GET
	@Path("/help")
	public Response help() {
		return createOkResponse("No help available");
	}

	/**
	 * Auxiliar methods
	 */

	@GET
	@Path("/{species}")
	public Response getCategories(@PathParam("species") String species) {
		if (isSpecieAvailable(species)) {
			return createOkResponse("feature\ngenomic\nnetwork\nregulatory");
		}
		return getSpecies();
	}

	@GET
	@Path("/{species}/{category}")
	public Response getCategory(@PathParam("species") String species, @PathParam("category") String category) {
		if (isSpecieAvailable(species)) {
			if ("feature".equalsIgnoreCase(category)) {
				return createOkResponse("exon\ngene\nkaryotype\nprotein\nsnp\ntranscript");
			}
			if ("genomic".equalsIgnoreCase(category)) {
				return createOkResponse("position\nregion\nvariant");
			}
			if ("network".equalsIgnoreCase(category)) {
				return createOkResponse("pathway");
			}
			if ("regulatory".equalsIgnoreCase(category)) {
				return createOkResponse("mirna_gene\nmirna_mature\ntf");
			}
			return createOkResponse("feature\ngenomic\nnetwork\nregulatory");
		} else {
			return getSpecies();
		}
	}

	@GET
	@Path("/{species}/{category}/{subcategory}")
	public Response getSubcategory(@PathParam("species") String species, @PathParam("category") String category,
			@PathParam("subcategory") String subcategory) {
		return getCategory(species, category);
	}

	@GET
	@Path("/version")
	public Response getVersion() {
		StringBuilder versionMessage = new StringBuilder();
		versionMessage.append("Homo sapiens").append("\t").append("Ensembl 64").append("\n");
		versionMessage.append("Mus musculus").append("\t").append("Ensembl 65").append("\n");
		versionMessage.append("Rattus norvegicus").append("\t").append("Ensembl 65").append("\n");
		versionMessage.append("Drosophila melanogaster").append("\t").append("Ensembl 65").append("\n");
		versionMessage.append("Canis familiaris").append("\t").append("Ensembl 65").append("\n");
		versionMessage.append("...").append("\n\n");
		versionMessage
				.append("The rest of nfo will be added soon, sorry for the inconveniences. You can find mor info at:")
				.append("\n\n").append("http://docs.bioinfo.cipf.es/projects/variant/wiki/Databases");
		return createOkResponse(versionMessage.toString(), MediaType.valueOf("text/plain"));
	}

	@GET
	@Path("/species")
	public Response getSpecies() {
		List<Species> speciesList = getSpeciesList();
		MediaType mediaType = MediaType.valueOf("application/javascript");
		if (uriInfo.getQueryParameters().get("of") != null
				&& uriInfo.getQueryParameters().get("of").get(0).equalsIgnoreCase("json")) {
			return createOkResponse(gson.toJson(speciesList), mediaType);
		} else {
			StringBuilder stringBuilder = new StringBuilder();
			for (Species sp : speciesList) {
				stringBuilder.append(sp.toString()).append("\n");
			}
			mediaType = MediaType.valueOf("text/plain");
			return createOkResponse(stringBuilder.toString(), mediaType);
		}
	}

	@GET
	@Path("/{species}/chromosomes")
	public Response getChromosomes(@PathParam("species") String species) {
		return createOkResponse(config.getProperty("CELLBASE." + species.toUpperCase() + ".CHROMOSOMES"),
				MediaType.valueOf("text/plain"));
	}

	@SuppressWarnings("unchecked")
	protected Response generateResponse(String queryString, List features) throws IOException {
		return generateResponse(queryString, null, features);
	}

	@SuppressWarnings("unchecked")
	protected Response generateResponse(String queryString, String headerTag, List features) throws IOException {
		logger.debug("CellBase - GenerateResponse, QueryString: "
				+ ((queryString.length() > 50) ? queryString.substring(0, 49) + "..." : queryString));

		// default mediaType
		MediaType mediaType = MediaType.valueOf("text/plain");
		String response = "outputformat 'of' parameter not valid: " + outputFormat;

		switch (outputFormat.toLowerCase()) {
		case "txt":
		case "text":
			// TODO
			break;
		case "xml":
			mediaType = MediaType.TEXT_XML_TYPE;
			response = ListUtils.toString(features, resultSeparator);
		case "das":
			mediaType = MediaType.TEXT_XML_TYPE;
			response = ListUtils.toString(features, resultSeparator);
		case "json":
			mediaType = MediaType.valueOf("application/json");
			response = gson.toJson(features);
			break;
		}

		// if (outputFormat != null) {
		// // if((outputFormat.equalsIgnoreCase("json") ||
		// // outputFormat.equalsIgnoreCase("jsonp"))) {
		// if (outputFormat.equalsIgnoreCase("json")) {
		// // mediaType = MediaType.APPLICATION_JSON_TYPE;
		// mediaType = MediaType.valueOf("application/javascript");
		// response = gson.toJson(features);
		// // if(features != null && features.size() > 0) {
		// // response = gson.toJson(features);
		// // o:
		// // JsonWriter jsonWriter = new JsonWriter(new
		// // FeatureExclusionStrategy());
		// // response = jsonWriter.serialize(features);
		// // }
		//
		// // if(outputFormat.equals("jsonp")) {
		// // mediaType = MediaType.valueOf("application/javascript");
		// // response = convertToJson(response);
		// // }GENE
		// } else {
		// if (outputFormat.equalsIgnoreCase("txt") ||
		// outputFormat.equalsIgnoreCase("text")) { // ||
		// // outputFormat.equalsIgnoreCase("jsontext")
		// // if(outputFormat.equalsIgnoreCase("jsontext")) {
		// // mediaType = MediaType.valueOf("application/javascript");
		// // response = convertToJsonText(response);
		// // }else {
		// // mediaType = MediaType.TEXT_PLAIN_TYPE;
		//
		// // CONCATENAR el nombre de la query
		// // String[] query.split(",");
		// mediaType = MediaType.valueOf("text/plain");
		// if (headerTag != null && headers.containsKey(headerTag) &&
		// outputHeader != null
		// && outputHeader.equalsIgnoreCase("true")) {
		// // response = "#" + headers.get(headerTag) + "\n" +
		// // StringWriter.serialize(features);
		// } else {
		// // response = StringWriter.serialize(features);
		// }
		// // }
		// }
		//
		// if (outputFormat.equalsIgnoreCase("xml")) {
		// mediaType = MediaType.TEXT_XML_TYPE;
		// response = ListUtils.toString(features, resultSeparator);
		// }
		//
		// if (outputFormat.equalsIgnoreCase("das")) {
		// mediaType = MediaType.TEXT_XML_TYPE;
		// response = ListUtils.toString(features, resultSeparator);
		// }
		// }
		// }

		return createResponse(response, mediaType);
	}

	protected Response createResponse(String response, MediaType mediaType) throws IOException {
		logger.debug("CellBase - CreateResponse, QueryParams: FileFormat => " + fileFormat + ", OutputFormat => "
				+ outputFormat + ", Compress => " + outputCompress);
		logger.debug("CellBase - CreateResponse, Inferred media type: " + mediaType.toString());
		logger.debug("CellBase - CreateResponse, Response: "
				+ ((response.length() > 50) ? response.substring(0, 49) + "..." : response));

		if (fileFormat == null || fileFormat.equalsIgnoreCase("")) {
			if (outputCompress != null && outputCompress.equalsIgnoreCase("true")
					&& !outputFormat.equalsIgnoreCase("jsonp") && !outputFormat.equalsIgnoreCase("jsontext")) {
				response = Arrays.toString(StringUtils.gzipToBytes(response)).replace(" ", "");
			}
		} else {
			mediaType = MediaType.APPLICATION_OCTET_STREAM_TYPE;
			logger.debug("\t\t - Creating byte stream ");

			if (outputCompress != null && outputCompress.equalsIgnoreCase("true")) {
				OutputStream bos = new ByteArrayOutputStream();
				bos.write(response.getBytes());

				ZipOutputStream zipstream = new ZipOutputStream(bos);
				zipstream.setLevel(9);

				logger.debug("CellBase - CreateResponse, zipping... Final media Type: " + mediaType.toString());

				return this.createOkResponse(zipstream, mediaType, filename + ".zip");

			} else {
				if (fileFormat.equalsIgnoreCase("xml")) {
					// mediaType = MediaType.valueOf("application/xml");
				}

				if (fileFormat.equalsIgnoreCase("excel")) {
					// mediaType =
					// MediaType.valueOf("application/vnd.ms-excel");
				}
				if (fileFormat.equalsIgnoreCase("txt") || fileFormat.equalsIgnoreCase("text")) {
					logger.debug("\t\t - text File ");

					byte[] streamResponse = response.getBytes();
					// return Response.ok(streamResponse,
					// mediaType).header("content-disposition","attachment; filename = "+
					// filename + ".txt").build();
					return this.createOkResponse(streamResponse, mediaType, filename + ".txt");
				}
			}
		}
		logger.debug("CellBase - CreateResponse, Final media Type: " + mediaType.toString());
		// return Response.ok(response, mediaType).build();
		return this.createOkResponse(response, mediaType);
	}

	protected Response createErrorResponse(String method, String errorMessage) {
		if (!errorMessage.contains("Species") && !errorMessage.contains("Version")) {
			// StringBuilder message = new StringBuilder();
			// message.append("URI: "+uriInfo.getAbsolutePath().toString()).append("\n");
			// message.append("Method: "+httpServletRequest.getMethod()+" "+method).append("\n");
			// message.append("Message: "+errorMessage).append("\n");
			// message.append("Remote Addr: http://ipinfodb.com/ip_locator.php?ip="+httpServletRequest.getRemoteAddr()).append("\n");
			// HttpUtils.send("correo.cipf.es", "fsalavert@cipf.es",
			// "babelomics@cipf.es", "Infrared error notice",
			// message.toString());
		}
		if (outputFormat.equalsIgnoreCase("json")) {
			JsonObject jsonRes = new JsonObject();
			jsonRes.addProperty("error", errorMessage);
			return buildResponse(Response.ok(jsonRes.toString(), MediaType.valueOf("text/plain")));
		} else {
			String error = "An error occurred: " + errorMessage;
			return buildResponse(Response.ok(error, MediaType.valueOf("text/plain")));
		}
	}

	protected Response createErrorResponse(Object o) {
		String objMsg = o.toString();
		if (objMsg.startsWith("ERROR:")) {
			return buildResponse(Response.ok("" + o));
		} else {
			return buildResponse(Response.ok("ERROR: " + o));
		}
	}

	protected Response createOkResponse(Object o) {
		return buildResponse(Response.ok(o));
	}

	protected Response createOkResponse(Object o1, MediaType o2) {
		return buildResponse(Response.ok(o1, o2));
	}

	protected Response createOkResponse(Object o1, MediaType o2, String fileName) {
		return buildResponse(Response.ok(o1, o2).header("content-disposition", "attachment; filename =" + fileName));
	}

	private Response buildResponse(ResponseBuilder responseBuilder) {
		return responseBuilder.header("Access-Control-Allow-Origin", "*").build();
	}

	@GET
	public Response getHelp() {
		return getSpecies();
	}

	private List<Species> getSpeciesList() {
		List<Species> speciesList = new ArrayList<Species>(11);
		speciesList.add(new Species("hsa", "human", "Homo sapiens", "GRCh37.p7"));
		speciesList.add(new Species("mmu", "mouse", "Mus musculus", "NCBIM37"));
		speciesList.add(new Species("rno", "rat", "Rattus norvegicus", "RGSC 3.4"));
		speciesList.add(new Species("dre", "zebrafish", "Danio rerio", "Zv9"));
		speciesList.add(new Species("cel", "worm", "Caenorhabditis elegans", "WS230"));
		speciesList.add(new Species("dme", "fruitfly", "Drosophila melanogaster", "BDGP 5.39"));
		speciesList.add(new Species("sce", "yeast", "Saccharomyces cerevisiae", "EF 4"));
		speciesList.add(new Species("cfa", "dog", "Canis familiaris", "CanFam 2.0"));
		speciesList.add(new Species("ssc", "pig", "Sus scrofa", "Sscrofa10.2"));
		speciesList.add(new Species("aga", "mosquito", "Anopheles gambiae", "AgamP3"));
		speciesList.add(new Species("pfa", "malaria parasite", "Plasmodium falciparum", "3D7"));

		speciesList.add(new Species("hsapiens", "", "", ""));
		speciesList.add(new Species("mmusculus", "", "", ""));
		speciesList.add(new Species("rnorvegicus", "", "", ""));
		speciesList.add(new Species("ptroglodytes", "", "", ""));
		speciesList.add(new Species("ggorilla", "", "", ""));
		speciesList.add(new Species("pabelii", "", "", ""));
		speciesList.add(new Species("mmulatta", "", "", ""));
		speciesList.add(new Species("sscrofa", "", "", ""));
		speciesList.add(new Species("cfamiliaris", "", "", ""));
		speciesList.add(new Species("ecaballus", "", "", ""));
		speciesList.add(new Species("ocuniculus", "", "", ""));
		speciesList.add(new Species("ggallus", "", "", ""));
		speciesList.add(new Species("btaurus", "", "", ""));
		speciesList.add(new Species("fcatus", "", "", ""));
		speciesList.add(new Species("drerio", "", "", ""));
		speciesList.add(new Species("cintestinalis", "", "", ""));
		speciesList.add(new Species("dmelanogaster", "", "", ""));
		speciesList.add(new Species("dsimulans", "", "", ""));
		speciesList.add(new Species("dyakuba", "", "", ""));
		speciesList.add(new Species("agambiae", "", "", ""));
		speciesList.add(new Species("celegans", "", "", ""));
		speciesList.add(new Species("scerevisiae", "", "", ""));
		speciesList.add(new Species("spombe", "", "", ""));
		speciesList.add(new Species("afumigatus", "", "", ""));
		speciesList.add(new Species("aniger", "", "", ""));
		speciesList.add(new Species("anidulans", "", "", ""));
		speciesList.add(new Species("aoryzae", "", "", ""));
		speciesList.add(new Species("pfalciparum", "", "", ""));
		speciesList.add(new Species("lmajor", "", "", ""));
		speciesList.add(new Species("athaliana", "", "", ""));
		speciesList.add(new Species("alyrata", "", "", ""));
		speciesList.add(new Species("bdistachyon", "", "", ""));
		speciesList.add(new Species("osativa", "", "", ""));
		speciesList.add(new Species("gmax", "", "", ""));
		speciesList.add(new Species("vvinifera", "", "", ""));
		speciesList.add(new Species("zmays", "", "", ""));

		return speciesList;
	}

	/**
	 * TO DELETE
	 */

	@Deprecated
	private boolean isSpecieAvailable(String species) {
		List<Species> speciesList = getSpeciesList();
		for (int i = 0; i < speciesList.size(); i++) {
			// This only allows to show the information if species is in 3
			// letters format
			if (species.equalsIgnoreCase(speciesList.get(i).getSpecies())) {
				return true;
			}
		}
		return false;
	}

	@Deprecated
	protected Response generateErrorResponse(String errorMessage) {
		return Response.ok("An error occurred: " + errorMessage, MediaType.valueOf("text/plain")).build();
	}

	@Deprecated
	protected List<String> getPathsNicePrint() {
		return new ArrayList<String>();
	}

	// protected Response generateResponse(Criteria criteria) throws IOException
	// {
	// List result = criteria.list();
	// this.getSession().close();
	// return generateResponse("", result);
	// }
	//
	// protected Response generateResponse(Query query) throws IOException {
	// List result = query.list();
	// this.getSession().close();
	// return generateResponse("", result);
	// }

	@Deprecated
	private String convertToJsonText(String response) {
		String jsonpQueryParam = (uriInfo.getQueryParameters().get("callbackParam") != null) ? uriInfo
				.getQueryParameters().get("callbackParam").get(0) : "callbackParam";
		response = "var " + jsonpQueryParam + " = \"" + response + "\"";
		return response;
	}

	@Deprecated
	protected String convertToJson(String response) {
		String jsonpQueryParam = (uriInfo.getQueryParameters().get("callbackParam") != null) ? uriInfo
				.getQueryParameters().get("callbackParam").get(0) : "callbackParam";
		response = "var " + jsonpQueryParam + " = (" + response + ")";
		return response;
	}

	// protected Session getSession(){
	// return HibernateUtil.getSessionFactory().openSession();
	// }

}
