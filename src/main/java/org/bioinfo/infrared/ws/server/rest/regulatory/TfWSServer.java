package org.bioinfo.infrared.ws.server.rest.regulatory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
import org.bioinfo.infrared.lib.api.TfbsDBAdaptor;
import org.bioinfo.infrared.ws.server.rest.exception.VersionException;

import com.sun.jersey.api.client.ClientResponse.Status;


@Path("/{version}/{species}/regulatory/tf")
@Produces("text/plain")
public class TfWSServer extends RegulatoryWSServer {

	
	public TfWSServer(@PathParam("version") String version, @PathParam("species") String species, @Context UriInfo uriInfo) throws VersionException, IOException {
		super(version, species, uriInfo);
	}


	@GET
	@Path("/{tfId}/info") // Devuelve los TFBSs para el TFId que le das
	public Response getTfInfo(@PathParam("tfId") String query) {
		try {
			TfbsDBAdaptor adaptor = dbAdaptorFactory.getTfbsDBAdaptor(this.species);
			return generateResponse(query, adaptor.getAllByTfGeneNameList(StringUtils.toList(query, ",")));
			
		} catch (Exception e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	

	@GET
	@Path("/{tfId}/fullinfo") // Devuelve los TFBSs para el TFId que le das
	public Response getTfFullInfo(@PathParam("tfId") String query) {
		try {
			TfbsDBAdaptor adaptor = dbAdaptorFactory.getTfbsDBAdaptor(this.species);
			return generateResponse(query, adaptor.getAllByTfGeneNameList(StringUtils.toList(query, ",")));
			
		} catch (Exception e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	@GET
	@Path("/{tfId}/tfbs")
	public Response getAllByTfbs(@PathParam("tfId") String query) {
		try {
			TfbsDBAdaptor adaptor = dbAdaptorFactory.getTfbsDBAdaptor(this.species);
			return generateResponse(query, adaptor.getAllByTfGeneNameList(StringUtils.toList(query, ",")));
			
		} catch (Exception e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	
	@GET
	@Path("/{tfId}/gene")
	public Response getEnsemblGenes(@PathParam("tfId") String query) {
		try {
			GeneDBAdaptor adaptor = dbAdaptorFactory.getGeneDBAdaptor(this.species);
			return  generateResponse(query, adaptor.getAllByTfList(StringUtils.toList(query, ",")));
		} catch (Exception e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	
	@GET
	@Path("/{tfId}/pwm")
	public Response getAllPwms(@PathParam("tfId") String query) {
		try {
			TfbsDBAdaptor adaptor = dbAdaptorFactory.getTfbsDBAdaptor(this.species);
			return  generateResponse(query, adaptor.getAllPwmByTfGeneNameList(StringUtils.toList(query, ",")));
		} catch (Exception e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	
	@GET
	@Path("/annotation")
	public Response getAnnotation(@DefaultValue("")@QueryParam("celltype") String celltype) {
		try {
			TfbsDBAdaptor adaptor = dbAdaptorFactory.getTfbsDBAdaptor(this.species);
			List<Object> results;
			if (celltype.equals("")){
				results = adaptor.getAllAnnotation();
			}
			else{
				results = adaptor.getAllAnnotationByCellTypeList(StringUtils.toList(celltype, ","));
			}
			List lista = new ArrayList<String>();			
			
			for (Object result : results) {
				lista.add(((Object [])result)[0].toString()+"\t" + ((Object [])result)[1].toString());
			}
			return  generateResponse(new String(), lista);
			
		} catch (Exception e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	
}