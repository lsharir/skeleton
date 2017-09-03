package controllers;

import api.ReceiptResponse;
import api.TagReceiptRequest;
import dao.TagDao;
import dao.ReceiptDao;
import generated.tables.records.ReceiptsRecord;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Path("/tags/{tag}")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TagController {
  final TagDao tags;
  final ReceiptDao receipts;

  public TagController(TagDao tags, ReceiptDao receipts) {
    this.tags = tags;
    this.receipts = receipts;
  }

  @PUT
  public void toggleTag(TagReceiptRequest req, @PathParam("tag") String tagName) {
    tags.toggle(req.id, tagName);
  }

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
