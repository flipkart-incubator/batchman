package com.flipkart.batchingcore.batch;

import com.flipkart.batchingcore.Batch;
import com.flipkart.batchingcore.BatchImpl;
import com.flipkart.batchingcore.data.Tag;
import com.flipkart.batchingcore.data.TagData;

public class TagBatch<T extends TagData> extends BatchImpl<T> {
    private Tag tag;

    public TagBatch(Tag tag, Batch<T> batch) {
        super(batch.getDataCollection());
        this.tag = tag;
    }

    public Tag getTag() {
        return tag;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof TagBatch) {
            return ((TagBatch) o).getTag().equals(tag) && super.equals(o);
        }
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + (getTag() == null ? 0 : getTag().hashCode());
    }
}