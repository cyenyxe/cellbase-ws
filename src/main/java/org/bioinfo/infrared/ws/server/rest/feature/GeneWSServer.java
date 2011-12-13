package org.bioinfo.infrared.ws.server.rest.feature;

import java.io.IOException;
import java.util.Arrays;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.bioinfo.commons.utils.StringUtils;
import org.bioinfo.infrared.lib.api.GeneDBAdaptor;
import org.bioinfo.infrared.lib.api.TranscriptDBAdaptor;
import org.bioinfo.infrared.ws.server.rest.GenericRestWSServer;
import org.bioinfo.infrared.ws.server.rest.exception.VersionException;

import com.sun.jersey.api.client.ClientResponse.Status;

@Path("/{version}/{species}/feature/gene")
@Produces("text/plain")
public class GeneWSServer extends GenericRestWSServer {
	
	public GeneWSServer(@PathParam("version") String version, @PathParam("species") String species, @Context UriInfo uriInfo) throws VersionException, IOException {
		super(version, species, uriInfo);
	}
	
	
	private GeneDBAdaptor getGeneDBAdaptor(){
		return dbAdaptorFactory.getGeneDBAdaptor(this.species);
	}
	
	@SuppressWarnings("unchecked")
	@GET
	@Path("/{geneId}/info")
	public Response getByEnsemblId(@PathParam("geneId") String query) {
		try {
			return generateResponse(query, this.getGeneDBAdaptor().getAllByNameList(StringUtils.toList(query, ",")));
			
		//	return generateResponse(query, Arrays.asList(this.getGeneDBAdaptor().getAllByEnsemblIdList(StringUtils.toList(query, ","))));
		} catch (Exception e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@SuppressWarnings("unchecked")
	@GET
	@Path("/{geneId}/fullinfo")
	public Response getFullInfoByEnsemblId(@PathParam("geneId") String query, @DefaultValue("") @QueryParam("sources") String sources) {
		try {
			
			// getBean()
			// getExons()
			// List<Transcript> listT = TgetTranscripts()
			// getXrefs()
			// getSequence()
			// ...
			// for(T t: listT)  t.gene= null;
			//g.setTranscriptList = listT;
			// Serialize(g);   1 result per row!!  example:   transcript:    exons:  
			return generateResponse(query, Arrays.asList(this.getGeneDBAdaptor().getAllByEnsemblIdList(StringUtils.toList(query, ","))));
		} catch (Exception e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	@SuppressWarnings("unchecked")
	@GET
	@Path("/{geneId}/transcript")
	public Response getTranscriptsByEnsemblId(@PathParam("geneId") String query) {
		try {
			TranscriptDBAdaptor dbAdaptor = dbAdaptorFactory.getTranscriptDBAdaptor(this.species);
			return  generateResponse(query, Arrays.asList(dbAdaptor.getByEnsemblGeneIdList(StringUtils.toList(query, ","))));
			
		} catch (Exception e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}

//	@GET
//	@Path("/{geneId}/exon")
//	public Response getExonsByEnsemblId2(@PathParam("geneId") String query) {
//		try {
//			return generateResponse(query, new ExonDBAdapter().getByGeneIdList(StringUtils.toList(query, ",")));
//			/** HQL 
//			Query query = this.getSession().createQuery("select e from Exon e JOIN FETCH e.exon2transcripts et JOIN et.transcript t JOIN  t.gene g where g.stableId in :stable_id").setParameterList("stable_id", StringUtils.toList(geneId, ","));  
//			return generateResponse(query);
//			**/
//		} catch (Exception e) {
//			e.printStackTrace();
//			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
//		}
//	}
	
//	@GET
//	@Path("/{geneId}/exon2transcript")
//	public Response getExon2TranscriptByEnsemblId(@PathParam("geneId") String query) {
//		try {
//			return generateResponse(query, new Exon2TranscriptDBAdapter().getByGeneIdList(StringUtils.toList(query, ",")));
//		} catch (Exception e) {
//			e.printStackTrace();
//			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
//		}
//	}
	
//	@GET
//	@Path("/{geneId}/orthologous")
//	public Response getOrthologousByEnsemblId(@PathParam("geneId") String query) {
//		try {
//			return generateResponse(query, new OrthologousDBAdapter().getByGeneIdList(StringUtils.toList(query, ",")));
//		} catch (Exception e) {
//			e.printStackTrace();
//			logger.error("", StringUtils.getStackTrace(e));
//			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
//		}
//	}
}
