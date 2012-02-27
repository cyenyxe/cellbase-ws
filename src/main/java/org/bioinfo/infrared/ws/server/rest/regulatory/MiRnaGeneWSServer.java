package org.bioinfo.infrared.ws.server.rest.regulatory;

import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.bioinfo.commons.utils.StringUtils;
import org.bioinfo.infrared.lib.api.MirnaDBAdaptor;
import org.bioinfo.infrared.ws.server.rest.exception.VersionException;

@Path("/{version}/{species}/regulatory/mirna_gene")
@Produces("text/plain")
public class MiRnaGeneWSServer extends RegulatoryWSServer {

	
	public MiRnaGeneWSServer(@PathParam("version") String version, @PathParam("species") String species, @Context UriInfo uriInfo) throws VersionException, IOException {
		super(version, species, uriInfo);
	}
	
	@GET
	@Path("/{mirnaId}/info")
	public Response getMiRnaMatureInfo(@PathParam("mirnaId") String query) {
		try {
			MirnaDBAdaptor mirnaDBAdaptor = dbAdaptorFactory.getMirnaDBAdaptor(this.species);
			return generateResponse(query, mirnaDBAdaptor.getAllMiRnaGenesByNameList(StringUtils.toList(query, ",")));
		} catch (Exception e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}
		
	@GET
	@Path("/{mirnaId}/fullinfo")
	public Response getMiRnaMatureFullInfo(@PathParam("mirnaId") String query) {
		try {
			// miRnaGene y Ensembl Genes + Transcripts
			// miRnaMatures
			// mirnaDiseases
			// mirnaTargets
			MirnaDBAdaptor mirnaDBAdaptor = dbAdaptorFactory.getMirnaDBAdaptor(this.species);
			return generateResponse(query, mirnaDBAdaptor.getAllMiRnaGenesByNameList(StringUtils.toList(query, ",")));
		} catch (Exception e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GET
	@Path("/{mirnaId}/target")
	public Response getMirnaTargets(@PathParam("mirnaId") String query) {
		try {
			MirnaDBAdaptor mirnaDBAdaptor = dbAdaptorFactory.getMirnaDBAdaptor(this.species);
			return  generateResponse(query, mirnaDBAdaptor.getAllMiRnaTargetsByMiRnaGeneList(StringUtils.toList(query, ",")));
		} catch (Exception e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	@GET
	@Path("/{mirnaId}/disease")
	public Response getMinaDisease(@PathParam("mirnaId") String query) {
		try {
			MirnaDBAdaptor mirnaDBAdaptor = dbAdaptorFactory.getMirnaDBAdaptor(this.species);
			return  generateResponse(query, mirnaDBAdaptor.getAllMiRnaDiseasesByMiRnaGeneList(StringUtils.toList(query, ",")));
		} catch (Exception e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}
}