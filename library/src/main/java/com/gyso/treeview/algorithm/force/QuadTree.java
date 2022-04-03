package com.gyso.treeview.algorithm.force;

/**
 * Created by Z.Pan on 2016/10/10.
 */
class QuadTree {
    private static final float NULL = -1;

    static class Node {
        boolean isLeaf;
        Node[] children;
        FNode point;
        float x = NULL;
        float y = NULL;

        float charge;
        float pointCharge;
        float cx, cy;

        Node(boolean isLeaf, Node[] children, FNode point, float x, float y) {
            this.isLeaf = isLeaf;
            this.children = children;
            this.point = point;
            this.x = x;
            this.y = y;
        }
    }

    Node root;

    QuadTree() {
        root = generateNode();
    }

    void insert(Node n, FNode node, float minX, float minY, float maxX, float maxY) {
        if (Float.isNaN(minX) || Float.isNaN(minY) || Float.isNaN(maxX) || Float.isNaN(maxY)) {
            return;
        }
        if (n.isLeaf) {
            float nx = n.x;
            float ny = n.y;

            if (nx != NULL) {
                if (Math.abs(nx - node.x) + Math.abs(ny - node.y) < 0.01) {
                    insertChild(n, node, minX, minY, maxX, maxY);
                } else {
                    FNode nPoint = n.point;
                    n.x = n.y = NULL;
                    n.point = null;
                    insertChild(n, nPoint, minX, minY, maxX, maxY);
                    insertChild(n, node, minX, minY, maxX, maxY);
                }
            } else {
                n.x = node.x;
                n.y = node.y;
                n.point = node;
            }
        } else {
            insertChild(n, node, minX, minY, maxX, maxY);
        }
    }

    private void insertChild(Node n, FNode node, float minX, float minY, float maxX, float maxY) {
        float sx = (minX + maxX) * 0.5f;
        float sy = (minY + maxY) * 0.5f;
        boolean isRight = node.x >= sx;
        boolean isBottom = node.y >= sy;
        int right = isRight ? 1 : 0;
        int bottom = isBottom ? 1 : 0;
        int i = (bottom << 1) + right;

        n.isLeaf = false;
        if (n.children[i] == null) {
            Node nod = generateNode();
            n.children[i] = nod;
            n = nod;
        } else {
            n = n.children[i];
        }

        if (isRight) {
            minX = sx;
        } else {
            maxX = sx;
        }

        if (isBottom) {
            minY = sy;
        } else {
            maxY = sy;
        }

        insert(n, node, minX, minY, maxX, maxY);
    }

    /** generate a leaf node for quad tree. */
    private Node generateNode() {
        return new Node(true, new Node[4], null, NULL, NULL);
    }

}
