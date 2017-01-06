package com.flipkart.batching.core.batch;

import com.flipkart.batching.core.Batch;
import com.flipkart.batching.core.BatchImpl;
import com.flipkart.batching.core.data.Tag;
import com.flipkart.batching.core.data.TagData;

import java.util.Collections;

public class TagBatch<T extends TagData> extends BatchImpl<T> {
    public Tag tag;

    public TagBatch() {
        super(Collections.EMPTY_LIST);
    }

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