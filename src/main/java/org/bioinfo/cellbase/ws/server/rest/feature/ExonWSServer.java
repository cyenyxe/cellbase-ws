package org.bioinfo.cellbase.ws.server.rest.feature;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.bioinfo.cellbase.lib.api.ExonDBAdaptor;
import org.bioinfo.cellbase.lib.api.TranscriptDBAdaptor;
import org.bioinfo.cellbase.ws.server.rest.GenericRestWSServer;
import org.bioinfo.cellbase.ws.server.rest.exception.VersionException;
import org.bioinfo.commons.utils.StringUtils;
import org.bioinfo.infrared.core.cellbase.Exon;

@Path("/{version}/{species}/feature/exon")
@Produces("text/plain")
public class ExonWSServer extends GenericRestWSServer {
	
	public ExonWSServer(@PathParam("version") String version, @PathParam("species") String species, @Context UriInfo uriInfo, @Context HttpServletRequest hsr) throws VersionException, IOException {
		super(version, species, uriInfo, hsr);
	}
	
	@GET
	@Path("/{exonId}/info")
	public Response getByEnsemblId(@PathParam("exonId") String query) {
		try {
			checkVersionAndSpecies();
			ExonDBAdaptor exonDBAdaptor = dbAdaptorFactory.getExonDBAdaptor(this.species, this.version);
			return  generateResponse(query,exonDBAdaptor.getAllByEnsemblIdList(StringUtils.toList(query, ",")));
		} catch (Exception e) {
			e.printStackTrace();
			return createErrorResponse("getByEnsemblId", e.toString());
		}
	}
	
	@SuppressWarnings("unchecked")
	@GET
	@Path("/{snpId}/bysnp")
	public Response getAllBySnpIdList(@PathParam("snpId") String query) {
		try {
			checkVersionAndSpecies();
			ExonDBAdaptor exonDBAdaptor = dbAdaptorFactory.getExonDBAdaptor(this.species, this.version);
			return generateResponse(query, Arrays.asList(exonDBAdaptor.getAllBySnpIdList(StringUtils.toList(query, ","))));
		} catch (Exception e) {
			e.printStackTrace();
			return createErrorResponse("getAllBySnpIdList", e.toString());
		}
	}
	
	@GET
	@Path("/{exonId}/aminos")
	public Response getAminoByExon(@PathParam("exonId") String query) {
		try{
			checkVersionAndSpecies();
			ExonDBAdaptor exonDBAdaptor = dbAdaptorFactory.getExonDBAdaptor(this.species, this.version);
			List<Exon> exons = exonDBAdaptor.getAllByEnsemblIdList(StringUtils.toList(query, ","));
			List<String> sequenceList = null;
			if(exons != null) {
				sequenceList = new ArrayList<String>(exons.size());
				for(Exon exon : exons) {
					if(exon != null && "-1".equals(exon.getStrand())) {
						sequenceList = exonDBAdaptor.getAllSequencesByIdList(StringUtils.toList(query, ","), -1);
					}else {
						sequenceList = exonDBAdaptor.getAllSequencesByIdList(StringUtils.toList(query, ","), 1);
					}
				}
			}
			return generateResponse(query, sequenceList);
		} catch (Exception e) {
			e.printStackTrace();
			return createErrorResponse("getAminoByExon", e.toString());
		}
	}
	
//	@GET
//	@Path("/{exonId}/sequence")
//	public Response getSequencesByIdList(@DefaultValue("1")@QueryParam("strand")String strand, @PathParam("exonId") String query) {
//		try {
//			if(strand.equals("-1")){
//				return generateResponse(query, Arrays.asList(this.getExonDBAdaptor().getAllSequencesByIdList(StringUtils.toList(query, ","), -1)));
//			}
//			else{
//				return generateResponse(query, Arrays.asList(this.getExonDBAdaptor().getAllSequencesByIdList(StringUtils.toList(query, ","))));
//				
//			}
//		} catch (IOException e) {
//			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
//		}
//	}
	
	@SuppressWarnings("unchecked")
	@GET
	@Path("/{exonId}/sequence")
	public Response getSequencesByIdList(@PathParam("exonId") String query) {
		try {
			checkVersionAndSpecies();
			ExonDBAdaptor exonDBAdaptor = dbAdaptorFactory.getExonDBAdaptor(this.species, this.version);
			return generateResponse(query, Arrays.asList(exonDBAdaptor.getAllSequencesByIdList(StringUtils.toList(query, ","))));
		} catch (Exception e) {
			e.printStackTrace();
			return createErrorResponse("getSequencesByIdList", e.toString());
		}
	}
	
	@SuppressWarnings("unchecked")
	@GET
	@Path("/{exonId}/region")
	public Response getRegionsByIdList(@PathParam("exonId") String query) {
		try {
			checkVersionAndSpecies();
			ExonDBAdaptor exonDBAdaptor = dbAdaptorFactory.getExonDBAdaptor(this.species, this.version);
			return generateResponse(query, Arrays.asList(exonDBAdaptor.getAllRegionsByIdList(StringUtils.toList(query, ","))));
		} catch (Exception e) {
			e.printStackTrace();
			return createErrorResponse("getRegionsByIdList", e.toString());
		}
	}
	
	@GET
	@Path("/{exonId}/transcript")
	public Response getTranscriptsByEnsemblId(@PathParam("exonId") String query) {
		try {
			checkVersionAndSpecies();
			TranscriptDBAdaptor transcriptDBAdaptor = dbAdaptorFactory.getTranscriptDBAdaptor(this.species, this.version);
			return generateResponse(query, transcriptDBAdaptor.getAllByEnsemblExonId(StringUtils.toList(query, ",")));
		} catch (Exception e) {
			e.printStackTrace();
			return createErrorResponse("getTranscriptsByEnsemblId", e.toString());
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
		sb.append("- info: Get exon information: name and location.\n");
		sb.append(" Output columns: Ensembl ID, chromosome, start, end, strand.\n\n");
		sb.append("- sequence: Get exon sequence.\n\n");
		sb.append("- trancript: Get all transcripts which contain this exon.\n");
		sb.append(" Output columns: Ensembl ID, external name, external name source, biotype, status, chromosome, start, end, strand, coding region start, coding region end, cdna coding start, cdna coding end, description.\n\n\n");
		sb.append("Documentation:\n");
		sb.append("http://docs.bioinfo.cipf.es/projects/cellbase/wiki/Feature_rest_ws_api#Exon");
		
		return createOkResponse(sb.toString());
	}
}
