package io.realm;

/**
 * This interface describes the changes made to a collection during the last
 * update.
 */
public class OrderedCollectionChange {
    public long[] getDeletions() { return null; }
    public long[] getInsertertions() { return null; }
    public long[] getChanges()  { return null; }
    public Range[] getDeletionRanges() { return null; }
    public Range[] getInsertionRanges() { return null; }
    public Range[] getChangeRanges() { return null; }
    public Move[] getMoves() { return null; }

    public static class Range {
        public final long startIndex;
        public final long length;

        public Range(long startIndex, long length) {
            this.startIndex = startIndex;
            this.length = length;
        }
    }

    public static class Move {
        public final long oldIndex;
        public final long newIndex;

        public Move(long oldIndex, long newIndex) {
            this.oldIndex = oldIndex;
            this.newIndex = newIndex;
        }
    }
}
