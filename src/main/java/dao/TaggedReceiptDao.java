package dao;

import generated.tables.records.TagsRecord;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.util.List;

import static com.google.common.base.Preconditions.checkState;
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

    public List<TagsRecord> getTaggedList(String tag) {
        return dsl.selectFrom(TAGS).where(TAGS.TAG.eq(tag)).fetch();
    }
}
