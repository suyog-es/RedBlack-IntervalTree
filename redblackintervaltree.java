import java.util.ArrayList;
import java.util.List;

public class RedBlackIntervalTree {

    private static final boolean RED = true;
    private static final boolean BLACK = false;

    private Node root;

    private static class Interval {
        int start, end;

        Interval(int start, int end) {
            if (start > end) {
                throw new IllegalArgumentException("Invalid interval: start cannot be greater than end");
            }
            this.start = start;
            this.end = end;
        }

        boolean overlaps(Interval other) {
            return this.start <= other.end && other.start <= this.end;
        }

        boolean contains(int point) {
            return this.start <= point && point <= this.end;
        }

        @Override
        public String toString() {
            return "[" + start + ", " + end + "]";
        }
    }

    private class Node {
        Interval interval;
        Node left, right;
        boolean color;
        int max;
        int count;

        Node(Interval interval) {
            this.interval = interval;
            this.color = RED;
            this.max = interval.end;
            this.count = 1;
        }
    }

    // Helper methods
    private boolean isRed(Node x) {
        if (x == null) return false;
        return x.color == RED;
    }

    private void flipColors(Node h) {
        h.color = RED;
        h.left.color = BLACK;
        h.right.color = BLACK;
    }

    private Node rotateLeft(Node h) {
        Node x = h.right;
        h.right = x.left;
        x.left = h;
        x.color = h.color;
        h.color = RED;
        x.max = h.max;
        h.max = Math.max(h.interval.end, Math.max(max(h.left), max(h.right)));
        x.count = h.count;
        h.count = 1 + size(h.left) + size(h.right);
        return x;
    }

    private Node rotateRight(Node h) {
        Node x = h.left;
        h.left = x.right;
        x.right = h;
        x.color = h.color;
        h.color = RED;
        x.max = h.max;
        h.max = Math.max(h.interval.end, Math.max(max(h.left), max(h.right)));
        x.count = h.count;
        h.count = 1 + size(h.left) + size(h.right);
        return x;
    }

    private int max(Node x) {
        if (x == null) return Integer.MIN_VALUE;
        return x.max;
    }

    private int size(Node x) {
        if (x == null) return 0;
        return x.count;
    }

    // Insert Interval
    public void insert(int start, int end) {
        try {
            root = insert(root, new Interval(start, end));
            root.color = BLACK;
        } catch (IllegalArgumentException e) {
            System.err.println("Error inserting interval: " + e.getMessage());
        }
    }

    private Node insert(Node h, Interval interval) {
        if (h == null) return new Node(interval);

        int cmp = interval.start - h.interval.start;
        if (cmp < 0) h.left = insert(h.left, interval);
        else if (cmp > 0) h.right = insert(h.right, interval);
        else if (interval.end > h.interval.end) h.interval.end = interval.end;

        if (isRed(h.right) && !isRed(h.left)) h = rotateLeft(h);
        if (isRed(h.left) && isRed(h.left.left)) h = rotateRight(h);
        if (isRed(h.left) && isRed(h.right)) flipColors(h);

        h.max = Math.max(h.interval.end, Math.max(max(h.left), max(h.right)));
        h.count = 1 + size(h.left) + size(h.right);
        return h;
    }

    // Delete Interval
    public void delete(int start, int end) {
        try {
            root = delete(root, new Interval(start, end));
            if (root != null) root.color = BLACK;
        } catch (IllegalArgumentException e) {
            System.err.println("Error deleting interval: " + e.getMessage());
        }
    }

    private Node delete(Node h, Interval interval) {
        if (h == null) return null;

        int cmp = interval.start - h.interval.start;
        if (cmp < 0) {
            if (!isRed(h.left) && !isRed(h.left.left))
                h = moveRedLeft(h);
            h.left = delete(h.left, interval);
        } else {
            if (isRed(h.left))
                h = rotateRight(h);
            if (cmp == 0 && interval.end == h.interval.end && h.right == null)
                return null;
            if (!isRed(h.right) && !isRed(h.right.left))
                h = moveRedRight(h);
            if (cmp == 0 && interval.end == h.interval.end) {
                Node x = min(h.right);
                h.interval = x.interval;
                h.right = deleteMin(h.right);
            } else {
                h.right = delete(h.right, interval);
            }
        }
        return balance(h);
    }

    private Node moveRedLeft(Node h) {
        flipColors(h);
        if (isRed(h.right.left)) {
            h.right = rotateRight(h.right);
            h = rotateLeft(h);
            flipColors(h);
        }
        return h;
    }

    private Node moveRedRight(Node h) {
        flipColors(h);
        if (isRed(h.left.left)) {
            h = rotateRight(h);
            flipColors(h);
        }
        return h;
    }

    private Node min(Node x) {
        if (x.left == null) return x;
        return min(x.left);
    }

    private Node deleteMin(Node h) {
        if (h.left == null) return null;
        if (!isRed(h.left) && !isRed(h.left.left))
            h = moveRedLeft(h);
        h.left = deleteMin(h.left);
        return balance(h);
    }

    private Node balance(Node h) {
        if (isRed(h.right)) h = rotateLeft(h);
        if (isRed(h.left) && isRed(h.left.left)) h = rotateRight(h);
        if (isRed(h.left) && isRed(h.right)) flipColors(h);

        h.max = Math.max(h.interval.end, Math.max(max(h.left), max(h.right)));
        h.count = 1 + size(h.left) + size(h.right);
        return h;
    }

    // Find Overlapping Intervals
    public List<Interval> findOverlapping(int start, int end) {
        try {
            List<Interval> result = new ArrayList<>();
            findOverlapping(root, new Interval(start, end), result);
            return result;
        } catch (IllegalArgumentException e) {
            System.err.println("Error finding overlapping intervals: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private void findOverlapping(Node x, Interval interval, List<Interval> result) {
        if (x == null) return;
        if (interval.overlaps(x.interval)) {
            result.add(x.interval);
        }
        if (x.left != null && x.left.max >= interval.start) {
            findOverlapping(x.left, interval, result);
        }
        if (x.right != null && x.interval.start <= interval.end) {
            findOverlapping(x.right, interval, result);
        }
    }

    // Find Maximum Overlapping Intervals
    public Interval findMaxOverlapping() {
        if (root == null) return null;
        return findMaxOverlapping(root, 0).interval;
    }

    private Node findMaxOverlapping(Node x, int maxOverlaps) {
        if (x == null) return null;

        int overlaps = countOverlaps(root, x.interval);
        Node maxNode = overlaps > maxOverlaps ? x : null;
        maxOverlaps = Math.max(maxOverlaps, overlaps);

        Node leftMax = findMaxOverlapping(x.left, maxOverlaps);
        if (leftMax != null && countOverlaps(root, leftMax.interval) > maxOverlaps) {
            maxNode = leftMax;
            maxOverlaps = countOverlaps(root, leftMax.interval);
        }

        Node rightMax = findMaxOverlapping(x.right, maxOverlaps);
        if (rightMax != null && countOverlaps(root, rightMax.interval) > maxOverlaps) {
            maxNode = rightMax;
        }

        return maxNode;
    }

    private int countOverlaps(Node x, Interval interval) {
        if (x == null) return 0;
        int count = interval.overlaps(x.interval) ? 1 : 0;
        if (x.left != null && x.left.max >= interval.start) {
            count += countOverlaps(x.left, interval);
        }
        if (x.right != null && x.interval.start <= interval.end) {
            count += countOverlaps(x.right, interval);
        }
        return count;
    }

    // Find All Contained Intervals
    public List<Interval> findContaining(int point) {
        List<Interval> result = new ArrayList<>();
        findContaining(root, point, result);
        return result;
    }

    private void findContaining(Node x, int point, List<Interval> result) {
        if (x == null) return;
        if (x.interval.contains(point)) {
            result.add(x.interval);
        }
        if (x.left != null && x.left.max >= point) {
            findContaining(x.left, point, result);
        }
        if (x.right != null && x.interval.start <= point) {
            findContaining(x.right, point, result);
        }
    }

    // Utility method to print the tree (for debugging)
    public void printTree() {
        printTree(root, 0);
    }

    private void printTree(Node x, int level) {
        if (x == null) return;
        printTree(x.right, level + 1);
        for (int i = 0; i < level; i++) System.out.print("    ");
        System.out.println(x.interval + " (max: " + x.max + ", color: " + (x.color == RED ? "RED" : "BLACK") + ")");
        printTree(x.left, level + 1);
    }
}