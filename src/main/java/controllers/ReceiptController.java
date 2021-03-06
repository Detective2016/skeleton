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
import java.util.ArrayList;
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
        List<ReceiptResponse> receiptResponses = new ArrayList<>();
        for (ReceiptsRecord record: receiptRecords) {
            List<TagsRecord> tagsRecords = receipts.getTags(record.getId());
            ReceiptResponse receipt = new ReceiptResponse(record, tagsRecords);
            receiptResponses.add(receipt);
        }
        return receiptResponses;
    }

    @PUT
    @Path("/tags/{tag}")
    public void toggleTag(@PathParam("tag") String tag, int id) {
        ReceiptsRecord receiptsRecord = receipts.getReceiptById(id);
        System.out.println("tag: "+tag+ " id: "+id);
        if (receiptsRecord != null) {
            taggedReceipts.toggle(tag, id);
        }
    }

    @GET
    @Path("/tags/{tag}")
    public List<ReceiptResponse> getAllByTag(@PathParam("tag") String tag) {
        List<ReceiptsRecord> tagRecords = taggedReceipts.getTaggedList(tag);
        List<ReceiptResponse> receiptResponses = new ArrayList<>();
        for (ReceiptsRecord record: tagRecords) {
            List<TagsRecord> tagsRecordList = taggedReceipts.getTags(record.getId());
            ReceiptResponse receipt = new ReceiptResponse(record, tagsRecordList);
            receiptResponses.add(receipt);
        }
        return receiptResponses;
    }

    @GET
    @Path("/receipts/{id}")
    public List<String> getAllById(@PathParam("id") int id) {
        List<TagsRecord> records = taggedReceipts.getTaggedListById(id);
        List<String> tags = new ArrayList<String>();
        for (TagsRecord record: records) {
            tags.add(record.getTag());
        }
        return tags;
    }

    @GET
    @Path("/netid")
    public String getNetId() {
        return "ym434";
    }
}
