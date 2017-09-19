package dao;

import generated.tables.records.ReceiptsRecord;
import generated.tables.records.TagsRecord;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Result;
import org.jooq.impl.DSL;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkState;
import static generated.tables.Receipts.RECEIPTS;
import static generated.tables.Tags.TAGS;

public class TaggedReceiptDao {
    DSLContext dsl;

    public TaggedReceiptDao(Configuration jooqConfig) {
        this.dsl = DSL.using(jooqConfig);
    }

    public void toggle(String tag, int receiptId) {
        TagsRecord taggedRecord = dsl.selectFrom(TAGS).where(TAGS.ID_MAPPED.eq(receiptId)).and(TAGS.TAG.eq(tag)).fetchOne();

        if (taggedRecord != null && taggedRecord.getId() != null) {
            taggedRecord.delete();
        } else {
            taggedRecord = dsl.insertInto(TAGS, TAGS.ID_MAPPED, TAGS.TAG).values(receiptId, tag).returning(TAGS.ID).fetchOne();
        }

        checkState(taggedRecord != null && taggedRecord.getId() != null, "Insert failed");
    }

    public List<ReceiptsRecord> getTaggedList(String tag) {
        List<ReceiptsRecord> receiptsRecords = new ArrayList<>();
        Result<Record1<Integer>> results = dsl.select(TAGS.ID).from(TAGS).where(TAGS.TAG.eq(tag)).fetch();
        for(Record1<Integer> result: results) {
            ReceiptsRecord receiptsRecord = dsl.selectFrom(RECEIPTS).where(RECEIPTS.ID.eq(result.value1())).fetchOne();
        }
        return receiptsRecords;
    }

    public List<TagsRecord> getTags(int receiptId) {
        return dsl.selectFrom(TAGS).where(TAGS.ID.eq(receiptId)).fetch();
    }

    public List<TagsRecord> getTaggedListById(int id) {
        return dsl.selectFrom(TAGS).where(TAGS.ID_MAPPED.eq(id)).fetch();
    }

    public List<TagsRecord> getTaggedList() { return dsl.selectFrom(TAGS).where(TAGS.TAG.isNotNull()).fetch(); }
}
