package com.gyso.treeview.algorithm.force;

/**
 * Created by Z.Pan on 2016/10/9.
 */

public class FLink {

    FNode source;
    FNode target;
    private String text;

    public FLink(FNode source, FNode target) {
        this.source = source;
        this.target = target;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    double getNodeDistance() {
        float dx = source.x - target.x;
        float dy = source.y - target.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

}
