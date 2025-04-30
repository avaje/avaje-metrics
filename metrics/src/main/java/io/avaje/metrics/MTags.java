package io.avaje.metrics;

import java.util.Arrays;
import java.util.Objects;

final class MTags implements Tags {

    static Tags EMPTY = new MTags(new String[]{});

    private final String[] rawTags;

    MTags(String[] rawTags) {
        this.rawTags = rawTags;
    }

    @Override
    public boolean isEmpty() {
        return rawTags.length == 0;
    }

    @Override
    public String[] array() {
        return rawTags;
    }

    @Override
    public String[] append(String... moreTags) {
        if (rawTags.length == 0) {
            return moreTags;
        }
        String[] merged = new String[rawTags.length + moreTags.length];
        System.arraycopy(rawTags, 0, merged, 0, rawTags.length);
        System.arraycopy(moreTags, 0, merged, rawTags.length, moreTags.length);
        return merged;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof MTags)) return false;
        MTags dTags = (MTags) object;
        return Objects.deepEquals(rawTags, dTags.rawTags);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(rawTags);
    }

    @Override
    public String toString() {
        return rawTags.length == 0 ? "" : "tags:" + Arrays.toString(rawTags);
    }
}
