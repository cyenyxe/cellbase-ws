package org.bioinfo.infrared.ws.server.rest.genomic;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.bioinfo.infrared.lib.api.GeneDBAdaptor;
import org.bioinfo.infrared.lib.api.SnpDBAdaptor;
import org.bioinfo.infrared.lib.api.TranscriptDBAdaptor;
import org.bioinfo.infrared.lib.common.Position;
import org.bioinfo.infrared.ws.server.rest.GenericRestWSServer;
import org.bioinfo.infrared.ws.server.rest.exception.VersionException;

import com.sun.jersey.api.client.ClientResponse.Status;

@Path("/{version}/{species}/genomic/position")
@Produces("text/plain")
public class PositionWSServer extends GenericRestWSServer {

	public PositionWSServer(@PathParam("version") String version, @PathParam("species") String species, @Context UriInfo uriInfo) throws VersionException, IOException {
		super(version, species, uriInfo);
	}

//	@GET
//	@Path("/{positionId}/consequence_type")
//	public Response getConsequenceTypeByPositionGet(@PathParam("positionId") String positionId, @DefaultValue("") @QueryParam("gene") String gene, @DefaultValue("") @QueryParam("transcript") String transcript) {
//		String chromosome = Arrays.asList(positionId.split(":")).get(0);
//		int position = Integer.valueOf(Arrays.asList(positionId.split(":")).get(1));
//		
//		System.out.println("Position: " + position);
//		
//		
//		
//		try {
//			List todo = new ArrayList();
////			List<GenomicRegionFeatures> result = new ArrayList<GenomicRegionFeatures>();
////			
////			
//			GenomicRegionFeatureDBAdaptor adaptor = dbAdaptorFactory.getFeatureMapDBAdaptor(this.species);
////			GenomicRegionFeatures maps = adaptor.getByRegion(new Region(chromosome, position, position));
////			System.out.println(maps.getGenes().size());
////			result.add(maps);
////			
////			todo.addAll(maps.getGenes());
////			todo.addAll(maps.getTranscripts());
////			todo.addAll(maps.getExons());
////			todo.addAll(maps.getSnp());
//			
//			Gson gson = new Gson();
//			todo.add(adaptor.getConsequenceType(chromosome, position));
//			return generateResponse(positionId, todo);
//			
//			
//			
//		} catch (Exception e) {
//			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
//		}
//	}
//	
	
	@GET
	@Path("/{geneId}/gene")
	public Response getGeneByPosition(@PathParam("geneId") String query) {
			try {
				List<Position> positionList = Position.parsePositions(query);
				GeneDBAdaptor geneAdaptor = dbAdaptorFactory.getGeneDBAdaptor(this.species);
				return  generateResponse(query,  geneAdaptor.getAllByPositionList(positionList));
			} catch (Exception e) {
				return Response.status(Status.INTERNAL_SERVER_ERROR).build();
			}
	}
	
	@GET
	@Path("/{geneId}/transcript")
	public Response getTranscriptByPosition(@PathParam("geneId") String query) {
			try {
				List<Position> positionList = Position.parsePositions(query);
				TranscriptDBAdaptor transcriptAdaptor = dbAdaptorFactory.getTranscriptDBAdaptor(this.species);
				return  generateResponse(query,  transcriptAdaptor.getAllByPositionList(positionList));
			} catch (Exception e) {
				return Response.status(Status.INTERNAL_SERVER_ERROR).build();
			}
	}
	
	@GET
	@Path("/{geneId}/snp")
	public Response getSNPByPosition(@PathParam("geneId") String query) {
			try {
				List<Position> positionList = Position.parsePositions(query);
				SnpDBAdaptor snpAdaptor = dbAdaptorFactory.getSnpDBAdaptor(this.species);
				return  generateResponse(query,  snpAdaptor.getAllByPositionList(positionList));
			} catch (Exception e) {
				return Response.status(Status.INTERNAL_SERVER_ERROR).build();
			}
	}
	
	@POST
	@Path("/{positionId}/consequence_type")
	public Response getConsequenceTypeByPositionPost(@PathParam("positionId") String positionId) {
		return getConsequenceTypeByPosition(positionId);
	}
	
	public Response getConsequenceTypeByPosition(@PathParam("positionId") String positionId) {
		List<Position> positionList = Position.parsePositions(positionId);
		return null;
	}
	
	@GET
	@Path("/{positionId}/functional")
	public Response getFunctionalByPositionGet(@PathParam("positionId") String positionId, @DefaultValue("") @QueryParam("source") String source) {
		return Response.ok("/{positionId}/functional").build();
	}
	
	@POST
	@Path("/{positionId}/functional")
	public Response getFunctionalTypeByPositionPost(@PathParam("positionId") String positionId) {
		return getFunctionalByPosition(positionId);
	}
	
	public Response getFunctionalByPosition(@PathParam("positionId") String positionId) {
		List<Position> positionList = Position.parsePositions(positionId);
		return null;
	}
}