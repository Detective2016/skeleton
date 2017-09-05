package controllers;

import api.CreateReceiptRequest;
import api.ReceiptResponse;
import dao.ReceiptDao;
import dao.TaggedReceiptDao;
import generated.tables.records.ReceiptsRecord;
import generated.tables.records.TagsRecord;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Path("")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ReceiptController {
    final ReceiptDao receipts;
    final TaggedReceiptDao taggedReceipts;

    public ReceiptController(ReceiptDao receipts, TaggedReceiptDao taggedReceipts) {
        this.receipts = receipts;
        this.taggedReceipts = taggedReceipts;
    }

    @POST
    @Path("/receipts")
    public int createReceipt(@Valid @NotNull CreateReceiptRequest receipt) {
        return receipts.insert(receipt.merchant, receipt.amount);
    }

    @GET
    @Path("/receipts")
    public List<ReceiptResponse> getReceipts() {
        List<ReceiptsRecord> receiptRecords = receipts.getAllReceipts();
        return receiptRecords.stream().map(ReceiptResponse::new).collect(toList());
    }

    @PUT
    @Path("/tags/{tag}")
    public void toggleTag(@PathParam("tag") String tag, int id) {
        ReceiptsRecord receiptsRecord = receipts.getReceiptById(id);
        if (receiptsRecord != null) {
            taggedReceipts.toggle(tag, id);
        }
    }

    @GET
    @Path("/tags/{tag}")
    public List<ReceiptResponse> getAllByTag(@PathParam("tag") String tag) {
        List<TagsRecord> records = taggedReceipts.getTaggedList(tag);
        List<Integer> ids = records.stream().map(TagsRecord::getIdMapped).collect(toList());
        List<ReceiptsRecord> receiptRecords = receipts.getAllReceiptsById(ids);
        return receiptRecords.stream().map(ReceiptResponse::new).collect(toList());
    }

    @GET
    @Path("/netid")
    public String getNetId() {
        return "ym434";
    }
}
