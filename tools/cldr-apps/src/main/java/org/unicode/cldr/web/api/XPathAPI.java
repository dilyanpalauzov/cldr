package org.unicode.cldr.web.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.unicode.cldr.web.CookieSession;
import org.unicode.cldr.web.XPathTable;

@Path("/xpath")
public class XPathAPI {

    @Schema(description = "Return value for XPath query")
    public final class XPathInfo {
        @Schema(description = "XPath (String) value")
        private String xpath;
        @Schema(description = "Decimal (serial number). Not portable across instances.")
        private Integer decimalId;
        @Schema(description = "Hex (hash) of XPath.")
        private String hexId;

        public String getXpath() {
            return xpath;
        }
        public XPathInfo setXpath(String xpath) {
            this.xpath = xpath;
            return this;
        }
        public Integer getDecimalId() {
            return decimalId;
        }
        public XPathInfo setDecimalId(Integer decimalId) {
            this.decimalId = decimalId;
            return this;
        }
        public String getHexId() {
            return hexId;
        }
        public XPathInfo setHexId(String hexId) {
            this.hexId = hexId;
            return this;
        }
    }

    private final XPathTable getXPathTable() {
        return CookieSession.sm.xpt;
    }

    @GET @Path("/hex/{hexId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "Fetch XPath string from hex id",
        description = "Looks up a specific hex ID.")
    @APIResponses(
        value = {
            @APIResponse(
                responseCode = "404",
                description = "XPath not found",
                content =  @Content(mediaType = "application/json",
                schema = @Schema(implementation = STError.class))),
            @APIResponse(
                responseCode = "200",
                description = "Details about a given XPath",
                content =  @Content(mediaType = "application/json",
                schema = @Schema(implementation = XPathInfo.class))) })
    public Response getByHex(
        @Parameter(required = true, example = "1234abcd", schema = @Schema(type = SchemaType.STRING))
        @PathParam("hexId") String hexId) {

        // the actual implementation
        XPathTable xpt = getXPathTable();
        final String xpath = xpt.getByStringID(hexId);
        if(xpath == null) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(new STError("XPath " + hexId + " not found"))
                .build();
        }
        return Response.ok(new XPathInfo()
            .setHexId(hexId)
            .setDecimalId(xpt.getXpathIdFromStringId(hexId))
            .setXpath(xpath)).build();
    }

    @GET @Path("/dec/{decId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "Fetch XPath string from decimal id",
        description = "Looks up a specific decimal ID.")
    @APIResponses(
        value = {
            @APIResponse(
                responseCode = "404",
                description = "XPath not found",
                content =  @Content(mediaType = "application/json",
                schema = @Schema(implementation = STError.class))),
            @APIResponse(
                responseCode = "200",
                description = "Details about a given XPath",
                content =  @Content(mediaType = "application/json",
                schema = @Schema(implementation = XPathInfo.class))) })
    public Response getByDecimal(
        @Parameter(required = true, example = "5678", schema = @Schema(type = SchemaType.INTEGER))
        @PathParam("decId") int decId) {

        // the actual implementation
        XPathTable xpt = getXPathTable();
        final String xpath = xpt.getById(decId);
        if(xpath == null) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(new STError("XPath #" + decId + " not found"))
                .build();
        }
        return Response.ok(new XPathInfo()
            .setDecimalId(decId)
            .setHexId(xpt.getStringIDString(decId))
            .setXpath(xpath)).build();
    }
}
