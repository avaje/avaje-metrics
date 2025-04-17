package io.avaje.metrics;

/**
 * Tags that can be associated to metrics.
 *
 * <pre>{@code
 *
 *   Tags.of("env:dev", "service:foo-service");
 *
 * }</pre>
 */
public interface Tags {

    /**
     * Empty Tags.
     */
    Tags EMPTY = MTags.EMPTY;

    /**
     * Return empty Tags.
     */
    static Tags of() {
        return EMPTY;
    }

    /**
     * Create given the raw tags in key:value format.
     * <pre>{@code
     *
     *   Tags.of("env:dev", "service:foo-service");
     *
     * }</pre>
     */
    static Tags of(String... rawTags) {
        return new MTags(rawTags);
    }

    /**
     * Return true if the tags is empty.
     */
    boolean isEmpty();

    /**
     * Return the tags as an array.
     */
    String[] array();

    /**
     * Merge and return as an array.
     *
     * @param moreTags Additional tags that we want to merge.
     * @return The merged set of tags.
     */
    String[] append(String... moreTags);
}
