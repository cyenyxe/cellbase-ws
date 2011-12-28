package org.bioinfo.infrared.ws.server.rest.genomic;

import java.io.IOException;
import java.util.Arrays;
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

import org.bioinfo.infrared.lib.api.CytobandDBAdaptor;
import org.bioinfo.infrared.lib.api.ExonDBAdaptor;
import org.bioinfo.infrared.lib.api.GeneDBAdaptor;
import org.bioinfo.infrared.lib.api.SnpDBAdaptor;
import org.bioinfo.infrared.lib.api.TfbsDBAdaptor;
import org.bioinfo.infrared.lib.api.RegulatoryRegionDBAdaptor;

import org.bioinfo.infrared.lib.api.TranscriptDBAdaptor;
import org.bioinfo.infrared.lib.common.GenomeSequenceFeature;
import org.bioinfo.infrared.lib.common.Region;
import org.bioinfo.infrared.lib.impl.hibernate.GenomeSequenceDBAdaptor;
import org.bioinfo.infrared.ws.server.rest.GenericRestWSServer;
import org.bioinfo.infrared.ws.server.rest.exception.VersionException;

import com.sun.jersey.api.client.ClientResponse.Status;


@Path("/{version}/{species}/genomic/region")
@Produces("text/plain")
public class RegionWSServer extends GenericRestWSServer {
	
	public RegionWSServer(@PathParam("version") String version, @PathParam("species") String species, @Context UriInfo uriInfo) throws VersionException, IOException {
		super(version, species, uriInfo);
	}
	
	@GET
	@Path("/{chrRegionId}/gene")
	public Response getGenesByRegion(@PathParam("chrRegionId") String chregionId) {
		GeneDBAdaptor dbAdaptor = dbAdaptorFactory.getGeneDBAdaptor(this.species);
		List<Region> regions = Region.parseRegions(chregionId);
		try {
			return generateResponse(chregionId, dbAdaptor.getAllByRegionList(regions));
		} catch (IOException e) {
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
		
	}
	
	@GET
	@Path("/{chrRegionId}/transcript")
	public Response getTranscriptByRegion(@PathParam("chrRegionId") String chregionId) {
		TranscriptDBAdaptor dbAdaptor = dbAdaptorFactory.getTranscriptDBAdaptor(this.species);
		List<Region> regions = Region.parseRegions(chregionId);
		try {
			return generateResponse(chregionId, dbAdaptor.getAllByRegionList(regions));
		} catch (IOException e) {
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
		
	}
	
	@GET
	@Path("/{chrRegionId}/exon")
	public Response getExonByRegion(@PathParam("chrRegionId") String chregionId) {
		ExonDBAdaptor dbAdaptor = dbAdaptorFactory.getExonDBAdaptor(this.species);
		List<Region> regions = Region.parseRegions(chregionId);
		try {
			return generateResponse(chregionId, dbAdaptor.getAllByRegionList(regions));
		} catch (IOException e) {
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	
	@GET
	@Path("/{chrRegionId}/snp")
	public Response getSnpByRegion(@PathParam("chrRegionId") String chregionId) {
		SnpDBAdaptor dbAdaptor = dbAdaptorFactory.getSnpDBAdaptor(this.species);
		List<Region> regions = Region.parseRegions(chregionId);
		try {
			return generateResponse(chregionId, dbAdaptor.getAllByRegionList(regions));
		} catch (IOException e) {
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	@GET
	@Path("/{chrRegionId}/cytoband")
	public Response getCytobandByRegion(@PathParam("chrRegionId") String chregionId) {
		try {
			List<Region> regions = Region.parseRegions(chregionId);
			CytobandDBAdaptor dbAdaptor = dbAdaptorFactory.getCytobandDBAdaptor(this.species);
			return generateResponse(chregionId, dbAdaptor.getAllByRegionList(regions));
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	

	
	@GET
	@Path("/{chrRegionId}/sequence")
	public Response getSequenceByRegion(@PathParam("chrRegionId") String chregionId) {
		try {
			List<Region> regions = Region.parseRegions(chregionId);
			GenomeSequenceDBAdaptor dbAdaptor =  dbAdaptorFactory.getGenomeSequenceDBAdaptor(this.species);
			return this.generateResponse(chregionId, dbAdaptor.getByRegionList(regions));
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	@GET
	@Path("/{chrRegionId}/reverse")
	public Response getReverseSequenceByRegion(@PathParam("chrRegionId") String chregionId) {
		try {
			List<Region> regions = Region.parseRegions(chregionId);
			GenomeSequenceDBAdaptor dbAdaptor =  dbAdaptorFactory.getGenomeSequenceDBAdaptor(this.species);
			List<GenomeSequenceFeature> result = dbAdaptor.getByRegionList(regions, -1);
			return this.generateResponse(chregionId, result);
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	
	@GET
	@Path("/{chrRegionId}/sequencecode")
	public Response getSequenceCodeByRegion(@PathParam("chrRegionId") String chregionId) {
		return null;
//		try {
//			List<Region> regions = Region.parseRegions(chregionId);
//			List sequences =  GenomeSequenceFeatureDataAdapter.getByRegionList(regions);
//			return this.generateResponse(chregionId, sequences);
//		} catch (Exception e) {
//			e.printStackTrace();
//			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
//		}
	}
	
	@GET
	@Path("/{chrRegionId}/tfbs")
	public Response getTfByRegion(@PathParam("chrRegionId") String chregionId) {
		try {
			List<Region> regions = Region.parseRegions(chregionId);
			TfbsDBAdaptor adaptor = dbAdaptorFactory.getTfbsDBAdaptor(this.species);
			return this.generateResponse(chregionId, adaptor.getAllByRegionList(regions));
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	
	@GET
	@Path("/{chrRegionId}/regulatory")
	public Response getRegulatoryByRegion(@DefaultValue("")@QueryParam("type")String type, @PathParam("chrRegionId") String chregionId) {
		try {
			List<Region> regions = Region.parseRegions(chregionId);
			RegulatoryRegionDBAdaptor adaptor = dbAdaptorFactory.getRegulatoryRegionDBAdaptor(this.species);
			
			if (type.equals("")){
				return this.generateResponse(chregionId, adaptor.getAllByRegionList(regions));
			}
			else{
				return this.generateResponse(chregionId, adaptor.getAllByRegionList(regions, Arrays.asList(type.split(","))));
				
			}
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	
	@GET
	@Path("/{chrRegionId}/histone")
	public String getHistoneByRegion() {
//		returns all histone binding in this region
		return null;
	}
	
	@GET
	@Path("/{chrRegionId}/openchromatin")
	public String getOpenChromatinByRegion() {
//		returns all open chromatin regions in this region
		return null;
	}
	
	@GET
	@Path("/{chrRegionId}/polymerase")
	public String getPolymeraseByRegion() {
//		returns all polymerase binding in this region
		return null;
	}
	
//	
//	
//	private Response getFeaturesByRegion(String chregionId, Criteria criteria){
//		try {
//			List<Region> regions = Region.parseRegions(chregionId);
//			Disjunction disjunction = Restrictions.disjunction();
//			for (Region region : regions) {
//				Conjunction disjunctionRegion = Restrictions.conjunction();
//				disjunctionRegion.add(Restrictions.eq("chromosome", region.getChromosome())).add( Restrictions.ge("start", region.getStart())).add(Restrictions.le("end", region.getEnd()));
//				disjunction.add(disjunctionRegion);
//			}
//			criteria.add(disjunction);
//			return generateResponse(criteria);
//		} catch (Exception e) {
//			e.printStackTrace();
//			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
//		}
//	}

}
