package controllers;

import api.ReceiptResponse;
import dao.TagDao;
import dao.ReceiptDao;
import generated.tables.records.ReceiptsRecord;

import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Path("/tags")
@Consumes({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
@Produces(MediaType.APPLICATION_JSON)
public class TagController {
  final TagDao tags;
  final ReceiptDao receipts;

  public TagController(TagDao tags, ReceiptDao receipts) {
    this.tags = tags;
    this.receipts = receipts;
  }

  @Path("/{tag}")
  @PUT
  public void toggleTag(@NotNull String receiptId, @PathParam("tag") String tagName) {
    tags.toggle(Integer.parseInt(receiptId), tagName);
  }

  @Path("/{tag}")
  @GET
  public List<ReceiptResponse> getReceiptsWithTag(@PathParam("tag") String tagName) {
    List<Integer> receiptsIds = tags.getAllReceiptsIdWithTag(tagName);
    List<ReceiptsRecord> receiptsRecords = receipts.getAllReceipts();
    
    return receiptsRecords
      .stream()
      .filter(receiptRecord -> receiptsIds.contains(receiptRecord.getId()))
      .map(ReceiptResponse::new).collect(toList());
  }
}
