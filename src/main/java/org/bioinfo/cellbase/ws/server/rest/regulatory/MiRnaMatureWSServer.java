package org.bioinfo.cellbase.ws.server.rest.regulatory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.bioinfo.cellbase.lib.api.GeneDBAdaptor;
import org.bioinfo.cellbase.lib.api.MirnaDBAdaptor;
import org.bioinfo.cellbase.lib.api.TranscriptDBAdaptor;
import org.bioinfo.cellbase.lib.common.core.Gene;
import org.bioinfo.cellbase.lib.common.core.Transcript;
import org.bioinfo.cellbase.lib.common.regulatory.MirnaDisease;
import org.bioinfo.cellbase.lib.common.regulatory.MirnaGene;
import org.bioinfo.cellbase.lib.common.regulatory.MirnaMature;
import org.bioinfo.cellbase.ws.server.rest.exception.VersionException;
import org.bioinfo.commons.utils.StringUtils;


@Path("/{version}/{species}/regulatory/mirna_mature")
@Produces("text/plain")
public class MiRnaMatureWSServer extends RegulatoryWSServer {

	public MiRnaMatureWSServer(@PathParam("version") String version, @PathParam("species") String species, @Context UriInfo uriInfo, @Context HttpServletRequest hsr) throws VersionException, IOException {
		super(version, species, uriInfo, hsr);
	}

	@GET
	@Path("/{mirnaId}/info")
	public Response getMiRnaMatureInfo(@PathParam("mirnaId") String query) {
		try {
			checkVersionAndSpecies();
			// miRnaGene y Ensembl Genes + Transcripts
			// mirnaDiseases
			// mirnaTargets
			MirnaDBAdaptor mirnaDBAdaptor = dbAdaptorFactory.getMirnaDBAdaptor(this.species, this.version);
			logger.debug("En getMiRnaMatureInfo: "+query);
			return generateResponse(query, mirnaDBAdaptor.getAllMiRnaMaturesByNameList(StringUtils.toList(query, ",")));
		} catch (Exception e) {
			e.printStackTrace();
			return createErrorResponse("getMiRnaMatureInfo", e.toString());
		}
	}
		
	@GET
	@Path("/{mirnaId}/fullinfo")
	public Response getMiRnaMatureFullInfo(@PathParam("mirnaId") String query) {
		try {
			checkVersionAndSpecies();
			MirnaDBAdaptor mirnaDBAdaptor = dbAdaptorFactory.getMirnaDBAdaptor(this.species, this.version);
			GeneDBAdaptor geneDBAdaptor = dbAdaptorFactory.getGeneDBAdaptor(this.species, this.version);
			TranscriptDBAdaptor transcriptDBAdaptor = dbAdaptorFactory.getTranscriptDBAdaptor(this.species, this.version);
			
			List<List<MirnaMature>> mirnaMature = mirnaDBAdaptor.getAllMiRnaMaturesByNameList(StringUtils.toList(query, ","));
			List<List<MirnaGene>> mirnaGenes = mirnaDBAdaptor.getAllMiRnaGenesByMiRnaMatureList(StringUtils.toList(query, ","));
			
			List<List<Gene>> genes = geneDBAdaptor.getAllByMiRnaMatureList(StringUtils.toList(query, ","));
			List<List<Transcript>> transcripts = transcriptDBAdaptor.getAllByMirnaMatureList(StringUtils.toList(query, ","));
			
			List<List<Gene>> targetGenes = geneDBAdaptor.getAllTargetsByMiRnaMatureList(StringUtils.toList(query, ","));
			List<List<MirnaDisease>> mirnaDiseases = mirnaDBAdaptor.getAllMiRnaDiseasesByMiRnaMatureList(StringUtils.toList(query, ""));
			
			StringBuilder response = new StringBuilder();
			response.append("[");
			for (int i = 0; i < genes.size(); i++) {
				if(genes.get(i).size() > 0){
					response.append("{");
					response.append("\"mirna\":{");
					response.append("\"mirnaMature\":"+gson.toJson(mirnaMature.get(i))+",");
					response.append("\"mirnaGenes\":"+gson.toJson(mirnaGenes.get(i))+"");
					response.append("},");
					response.append("\"genes\":"+gson.toJson(genes.get(i))+",");
					response.append("\"transcripts\":"+gson.toJson(transcripts.get(i))+",");
					response.append("\"targetGenes\":"+gson.toJson(targetGenes.get(i))+",");
					response.append("\"mirnaDiseases\":"+gson.toJson(mirnaDiseases.get(i))+"");
					response.append("},");
				}else{
					response.append("null,");
				}
			}
			response.replace(response.length()-1, response.length(), "");
			response.append("]");
			//Remove the last comma
			
			return  generateResponse(query,Arrays.asList(response));
		} catch (Exception e) {
			e.printStackTrace();
			return createErrorResponse("getMiRnaMatureFullInfo", e.toString());
		}
	}
	
	@GET
	@Path("/{mirnaId}/gene")
	public Response getEnsemblGene(@PathParam("mirnaId") String query) {
		try {
			checkVersionAndSpecies();
			GeneDBAdaptor adaptor = dbAdaptorFactory.getGeneDBAdaptor(this.species, this.version);
			return  generateResponse(query, adaptor.getAllByMiRnaMatureList(StringUtils.toList(query, ",")));
		} catch (Exception e) {
			e.printStackTrace();
			return createErrorResponse("getEnsemblGene", e.toString());
		}
	}
	
	@GET
	@Path("/{mirnaId}/mirna_gene")
	public Response getMiRnaGene(@PathParam("mirnaId") String query) {
		try {
			checkVersionAndSpecies();
			MirnaDBAdaptor mirnaDBAdaptor = dbAdaptorFactory.getMirnaDBAdaptor(this.species, this.version);
			return generateResponse(query, mirnaDBAdaptor.getAllMiRnaGenesByMiRnaMatureList(StringUtils.toList(query, ",")));
//			return  generateResponse(query, mirnaDBAdaptor.getAllByMiRnaList(StringUtils.toList(query, ",")));
		} catch (Exception e) {
			e.printStackTrace();
			return createErrorResponse("getMiRnaGene", e.toString());
		}
	}
	
	@GET
	@Path("/{mirnaId}/target_gene")
	public Response getEnsemblTargetGenes(@PathParam("mirnaId") String query) {
		try {
			checkVersionAndSpecies();
			GeneDBAdaptor adaptor = dbAdaptorFactory.getGeneDBAdaptor(this.species, this.version);
			return  generateResponse(query, adaptor.getAllTargetsByMiRnaMatureList(StringUtils.toList(query, ","))); // Renombrar a getAllTargetGenesByMiRnaList
		} catch (Exception e) {
			e.printStackTrace();
			return createErrorResponse("getEnsemblTargetGenes", e.toString());
		}
	}

	@GET
	@Path("/{mirnaId}/target")
	public Response getMirnaTargets(@PathParam("mirnaId") String query, @DefaultValue("")@QueryParam("source") String source) {
		try {
			checkVersionAndSpecies();
			MirnaDBAdaptor adaptor = dbAdaptorFactory.getMirnaDBAdaptor(this.species, this.version);
			return  generateResponse(query, adaptor.getAllMiRnaTargetsByMiRnaMatureList(StringUtils.toList(query, ","), StringUtils.toList(source, ",")));
		} catch (Exception e) {
			e.printStackTrace();
			return createErrorResponse("getMirnaTargets", e.toString());
		}
	}

	@GET
	@Path("/{mirnaId}/disease")
	public Response getMinaDisease(@PathParam("mirnaId") String query) {
		try {
			checkVersionAndSpecies();
			MirnaDBAdaptor mirnaDBAdaptor = dbAdaptorFactory.getMirnaDBAdaptor(this.species, this.version);
			return  generateResponse(query, mirnaDBAdaptor.getAllMiRnaDiseasesByMiRnaMatureList(StringUtils.toList(query, ",")));
		} catch (Exception e) {
			e.printStackTrace();
			return createErrorResponse("getMinaDisease", e.toString());
		}
	}
	
	@GET
	@Path("/annotation")
	public Response getAnnotation(@DefaultValue("") @QueryParam("source") String source) {
		try {
			checkVersionAndSpecies();
			MirnaDBAdaptor adaptor = dbAdaptorFactory.getMirnaDBAdaptor(this.species, this.version);

			List<?> results;
			if(source.equals("")){
				results = adaptor.getAllAnnotation();
			}else{
				results = adaptor.getAllAnnotationBySourceList(StringUtils.toList(source, ","));
			}

			List<String> lista = new ArrayList<String>();
			if(results != null && results.size() > 0) {
				for(Object result : results) {
					lista.add(((Object [])result)[0].toString()+"\t" + ((Object [])result)[1].toString());
				}	
			}

			return generateResponse(new String(), lista);
		} catch (Exception e) {
			e.printStackTrace();
			return createErrorResponse("getAnnotation", e.toString());
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
		sb.append("all id formats are accepted.\n\n\n");
		sb.append("Resources:\n");
		sb.append("- info: Get information about a miRNA mature: name, accession and sequence.\n");
		sb.append(" Output columns: miRBase accession, miRBase ID, sequence.\n\n");
		sb.append("- gene: Get the gene associated to this miRNA mature.\n");
		sb.append(" Output columns: Ensembl gene, external name, external name source, biotype, status, chromosome, start, end, strand, source, description.\n\n");
		sb.append("- mirna_gene: Get the miRNA gene information associated to this miRNA mature.\n");
		sb.append(" Output columns: miRBase accession, miRBase ID, status, sequence, source.\n\n");
		sb.append("- target_gene: Get all genes that are regulated by this miRNA mature.\n");
		sb.append(" Output columns: Ensembl gene, external name, external name source, biotype, status, chromosome, start, end, strand, source, description.\n\n");
		sb.append("- target: Get all binding sites associated to this miRNA.\n");
		sb.append(" Output columns: miRBase ID, gene target name, chromosome, start, end, strand, pubmed ID, source.\n\n");
		sb.append("- disease: Get all diseases related with this miRNA.\n");
		sb.append(" Output columns: miRBase ID, disease name, pubmed ID, description.\n\n\n");
		sb.append("Documentation:\n");
		sb.append("http://docs.bioinfo.cipf.es/projects/cellbase/wiki/Regulatory_rest_ws_api#MicroRNA-mature");
		
		return createOkResponse(sb.toString());
	}
}
