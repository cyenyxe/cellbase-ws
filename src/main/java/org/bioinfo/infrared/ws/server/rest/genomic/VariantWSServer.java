package org.bioinfo.infrared.ws.server.rest.genomic;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.bioinfo.infrared.core.cellbase.Transcript;
import org.bioinfo.infrared.lib.api.GenomicVariantEffectDBAdaptor;
import org.bioinfo.infrared.lib.common.GenomicVariant;
import org.bioinfo.infrared.lib.common.GenomicVariantConsequenceType;
import org.bioinfo.infrared.ws.server.rest.GenericRestWSServer;
import org.bioinfo.infrared.ws.server.rest.exception.VersionException;

import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.multipart.FormDataParam;

@Path("/{version}/{species}/genomic/variant")
@Produces("text/plain")
public class VariantWSServer extends GenericRestWSServer {

	protected static HashMap<String, List<Transcript>> CACHE_TRANSCRIPT = new HashMap<String, List<Transcript>>();

	public VariantWSServer(@PathParam("version") String version, @PathParam("species") String species, @Context UriInfo uriInfo, @Context HttpServletRequest hsr) throws VersionException, IOException {
		super(version, species, uriInfo, hsr);

		//		if (CACHE_TRANSCRIPT.get(this.species) == null){
		//			logger.debug("\tCACHE_TRANSCRIPT is null");
		//			long t0 = System.currentTimeMillis();
		//			TranscriptDBAdaptor adaptor = dbAdaptorFactory.getTranscriptDBAdaptor(species);
		//			CACHE_TRANSCRIPT.put(species, adaptor.getAll());
		//			logger.debug("\t\tFilling up for " + this.species + " in " + (System.currentTimeMillis() - t0) + " ms");
		//			logger.debug("\t\tNumber of transcripts: " + CACHE_TRANSCRIPT.get(this.species).size());
		//		}
	}

	@GET
	@Path("/{positionId}/consequence_type")
	public Response getConsequenceTypeByPositionByGet(@PathParam("positionId") String query, 
			@DefaultValue("true") @QueryParam("features") String features,
			@DefaultValue("true") @QueryParam("variation") String variation,
			@DefaultValue("true") @QueryParam("regulatory") String regulatory,
			@DefaultValue("true") @QueryParam("disease") String diseases,
			@DefaultValue("") @QueryParam("exclude") String excludeSOTerms) {
		try {
			//			return getConsequenceTypeByPosition(query, features, variation, regulatory, diseases);
			return getConsequenceTypeByPosition(query, excludeSOTerms);
		} catch (Exception e) {
			e.printStackTrace();
			return createErrorResponse("getConsequenceTypeByPositionByGet", e.toString());
		}
	}

	@POST
	@Consumes({MediaType.MULTIPART_FORM_DATA, MediaType.APPLICATION_FORM_URLENCODED})//MediaType.MULTIPART_FORM_DATA, 
	@Path("/consequence_type")
	public Response getConsequenceTypeByPositionByPost( @FormDataParam("of") String outputFormat, 
			@FormDataParam("variants") String postQuery, 
			@DefaultValue("") @FormDataParam("exclude") String excludeSOTerms) {
		//		String features = "true";
		//		String variation = "true"; 
		//		String regulatory = "true";
		//		String diseases = "true";
		//		System.out.println("VariantWSServer ==> postQuery: "+postQuery);
		//		postQuery = postQuery.replace("?", "%");
		//		
		//		String query = Arrays.asList(postQuery.split("%")).get(0);
		//		String queryParams =  Arrays.asList(postQuery.split("%")).get(1);
		//		if (queryParams.toLowerCase().contains("features=false")){
		//			features = "false";
		//		}
		//		if (queryParams.toLowerCase().contains("regulatory=false")){
		//			regulatory = "false";
		//		}
		//		if (queryParams.toLowerCase().contains("variation=false")){
		//			variation = "false";
		//		}
		//		if (queryParams.toLowerCase().contains("disease=false")){
		//			diseases = "false";
		//		}

		//		return getConsequenceTypeByPosition(postQuery, features, variation, regulatory, diseases);
		return getConsequenceTypeByPosition(postQuery, excludeSOTerms);
	}

	private Response getConsequenceTypeByPosition(String variants, String excludes) {
		try {
			System.out.println("PAKO: "+ variants);
			List<GenomicVariant> genomicVariantList = GenomicVariant.parseVariants(variants);
			if(genomicVariantList != null && excludes != null) {
				logger.debug("VariantWSServer: number of variants: "+ genomicVariantList.size());
				//			GenomicVariantEffect gv = new GenomicVariantEffect(this.species);
				GenomicVariantEffectDBAdaptor gv = dbAdaptorFactory.getGenomicVariantEffectDBAdaptor(species);
				String[] excludeArray = excludes.split(",");
				Set<String> excludeSet = new HashSet<String>(Arrays.asList(excludeArray));
				//				return generateResponse(variants, gv.getAllConsequenceTypeByVariantList(genomicVariantList));
				List<GenomicVariantConsequenceType> genomicVariantConsequenceTypes = gv.getAllConsequenceTypeByVariantList(genomicVariantList, excludeSet);
				System.out.println("VariantWSServer: genomicVariantConsequenceTypes => "+genomicVariantConsequenceTypes);
				return generateResponse(variants, genomicVariantConsequenceTypes);
//				return generateResponse(variants, gv.getAllConsequenceTypeByVariantList(genomicVariantList, excludeSet));
			} else {
				logger.error("");
				return Response.status(Status.BAD_REQUEST).build();
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("VariantWSServer: response.status => "+Response.status(Status.INTERNAL_SERVER_ERROR));
			return createErrorResponse("getConsequenceTypeByPositionByGet", e.toString());
		}
	}

	
	
	@Deprecated
	private Response getConsequenceTypeByPosition(String query, String features, String variation, String regulatory, String diseases){
		try {
			System.out.println("variants: "+query);
			if(query.length() > 100){
				logger.debug("VARIANT TOOL WS: " + query.substring(0, 99) + "....");
			}
			else{
				logger.debug("VARIANT TOOL WS: " + query);
			}
			List<GenomicVariant> variants = GenomicVariant.parseVariants(query);
			System.out.println("number of variants: "+variants.size());
			//			GenomicVariantEffect gv = new GenomicVariantEffect(this.species);
			GenomicVariantEffectDBAdaptor gv = dbAdaptorFactory.getGenomicVariantEffectDBAdaptor(species);
			//			if (features.equalsIgnoreCase("true")){
			//				gv.setShowFeatures(true);
			//			}
			//			else{
			//				gv.setShowFeatures(false);
			//			}
			//			
			//			if (variation.equalsIgnoreCase("true")){
			//				gv.setShowVariation(true);
			//			}
			//			else{
			//				gv.setShowVariation(false);
			//			}
			//			
			//			if (regulatory.equalsIgnoreCase("true")){
			//				gv.setShowRegulatory(true);
			//			}
			//			else{
			//				gv.setShowRegulatory(false);
			//			}
			//			
			//			if (diseases.equalsIgnoreCase("true")){
			//				gv.setShowDiseases(true);
			//			}
			//			else{
			//				gv.setShowDiseases(false);
			//			}

			//			return generateResponse(query, gv.getConsequenceType(variants, CACHE_TRANSCRIPT.get(this.species)));
			return generateResponse(query, gv.getAllConsequenceTypeByVariantList(variants));
		} catch (Exception e) {
			e.printStackTrace();
			return createErrorResponse("getConsequenceTypeByPosition", e.toString());
		}
	}
	
	@GET
	public Response getHelp() {
		return help();
	}
	
	@GET
	@Path("/help")
	public Response help() {
		StringBuilder sb = new StringBuilder();
		sb.append("Input:\n");
		sb.append("Variant format: chr:position:new allele (i.e.: 1:150044250:G)\n\n\n");
		sb.append("Resources:\n");
		sb.append("- consequence_type: Suppose that we have obtained some variants from a resequencing analysis and we want to obtain the consequence type of a variant over the transcripts\n");
		sb.append(" Output columns: chromosome, start, end, feature ID, feature name, consequence type, biotype, feature chromosome, feature start, feature end, feature strand, snp ID, ancestral allele, alternative allele, gene Ensembl ID, Ensembl transcript ID, gene name, SO consequence type ID, SO consequence type name, consequence type description, consequence type category, aminoacid change, codon change.\n\n\n");
		sb.append("Documentation:\n");
		sb.append("http://docs.bioinfo.cipf.es/projects/cellbase/wiki/Genomic_rest_ws_api#Variant");
		
		return createOkResponse(sb.toString());
	}

}
