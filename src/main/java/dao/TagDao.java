package dao;

import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.util.List;

import static generated.Tables.TAGS;

public class TagDao {
    DSLContext dsl;

    public TagDao(Configuration jooqConfig) {
        this.dsl = DSL.using(jooqConfig);
    }

    public void toggle(int id, String tagName) {
        int deletion = dsl
                .delete(TAGS)
                .where(TAGS.NAME.eq(tagName).and(TAGS.RECEIPT_ID.eq(id)))
                .execute();

        if (deletion == 0) {
            dsl
                    .insertInto(TAGS, TAGS.RECEIPT_ID, TAGS.NAME)
                    .values(id, tagName)
                    .execute();
        }
    }

    public List<Integer> getAllReceiptsIdWithTag(String tagName) {
        return dsl
                .selectFrom(TAGS)
                .where(TAGS.NAME.eq(tagName))
                .fetch(TAGS.RECEIPT_ID);
    }

    public List<String> getTagsOfReceiptWithId(int receiptId) {
        return dsl
                .selectFrom(TAGS)
                .where(TAGS.RECEIPT_ID.eq(receiptId))
                .fetch(TAGS.NAME);
    }
}
