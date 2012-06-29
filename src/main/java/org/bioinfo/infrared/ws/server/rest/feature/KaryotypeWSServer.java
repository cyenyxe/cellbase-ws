package org.bioinfo.infrared.ws.server.rest.feature;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.bioinfo.commons.utils.StringUtils;
import org.bioinfo.infrared.lib.api.CytobandDBAdaptor;
import org.bioinfo.infrared.ws.server.rest.GenericRestWSServer;
import org.bioinfo.infrared.ws.server.rest.exception.VersionException;

import com.sun.jersey.api.client.ClientResponse.Status;

@Path("/{version}/{species}/feature/karyotype")
@Produces("text/plain")
public class KaryotypeWSServer extends GenericRestWSServer {
	
	
	public KaryotypeWSServer(@PathParam("version") String version, @PathParam("species") String species, @Context UriInfo uriInfo, @Context HttpServletRequest hsr) throws VersionException, IOException {
		super(version, species, uriInfo, hsr);
	}

	@GET
	@Path("/{chromosomeName}/cytoband")
	public Response getByChromosomeName(@PathParam("chromosomeName") String chromosome) {
		try {
			checkVersionAndSpecies();
			CytobandDBAdaptor dbAdaptor = dbAdaptorFactory.getCytobandDBAdaptor(this.species, this.version);
			return generateResponse(chromosome, dbAdaptor.getAllByChromosomeList(StringUtils.toList(chromosome, ",")));
		} catch (Exception e) {
			e.printStackTrace();
			return createErrorResponse("getByChromosomeName", e.toString());
		}
	}
	
	@GET
	@Path("/chromosome")
	public Response getChromosomes() {
		try {
			checkVersionAndSpecies();
			CytobandDBAdaptor dbAdaptor = dbAdaptorFactory.getCytobandDBAdaptor(this.species, this.version);
			return generateResponse("", dbAdaptor.getAllChromosomeNames());
		} catch (Exception e) {
			e.printStackTrace();
			return createErrorResponse("getChromosomes", e.toString());
		}
	}

	@GET
	@Path("/{chromosomeName}/chromosome")
	public Response getChromosomes(@PathParam("chromosomeName") String query) {
		return getChromosomes();
//		try {
//			return getChromosomes();
//		} catch (Exception e) {
//			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
//		}
	}
	
	@GET
	public Response getHelp() {
		return help();
	}
	@GET
	@Path("/help")
	public Response help() {
		return createOkResponse("Usage:");
	}
}
