package com.gyso.treeview.adapter;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;

/**
 * @Author: 怪兽N
 * @Time: 2021/5/7  21:15
 * @Email: 674149099@qq.com
 * @WeChat: guaishouN
 * @Describe:
 * In order to draw a custom line, there are too many draw info should be passed.
 * So, this class is estimated to package the draw element that need to draw a colorful line between two nodes.
 */
public class DrawInfo {
    /**
     * canvas
     */
    private Canvas canvas;
    /**
     * node from view
     */
    private TreeViewHolder<?> fromHolder;
    /**
     * node to view
     */
    private TreeViewHolder<?> toHolder;
    /**
     * paint, before use you should reset
     */
    private Paint paint;
    /**
     * path, before use you should reset
     */
    private Path path;
    /**
     * space between Peer and Peer, means child and child
     */
    private int spacePeerToPeer;
    /**
     * space between parent and child
     */
    private int spaceParentToChild;
    /**
     * viewport width
     */
    private int windowWidth;
    /**
     * viewport height
     */
    private int windowHeight;
    /**
     * layout type {@link com.gyso.treeview.layout.TreeLayoutManager#LAYOUT_TYPE_HORIZON_RIGHT #LAYOUT_TYPE_VERTICAL_DOWN}
     */
    private int layoutType;

    /**
     * the end or start point of the join line from parent view to child node view
     */
    private PointF startPointFOfJoinLine;
    private PointF endPointFOfJoinLine;

    public Canvas getCanvas() {
        return canvas;
    }

    public void setCanvas(Canvas canvas) {
        this.canvas = canvas;
    }

    public TreeViewHolder<?> getFromHolder() {
        return fromHolder;
    }

    public void setFromHolder(TreeViewHolder<?> fromHolder) {
        this.fromHolder =  fromHolder;
    }

    public TreeViewHolder<?> getToHolder() {
        return toHolder;
    }

    public void setToHolder(TreeViewHolder<?> toHolder) {
        this.toHolder = toHolder;
    }

    public Paint getPaint() {
        return paint;
    }

    public void setPaint(Paint paint) {
        this.paint = paint;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public void setSpace(int spacePeerToPeer,int spaceParentToChild) {
        this.spacePeerToPeer = spacePeerToPeer;
        this.spaceParentToChild = spaceParentToChild;
    }

    public int getSpacePeerToPeer() {
        return spacePeerToPeer;
    }

    public int getSpaceParentToChild() {
        return spaceParentToChild;
    }

    public int getWindowWidth() {
        return windowWidth;
    }

    public void setWindowWidth(int windowWidth) {
        this.windowWidth = windowWidth;
    }

    public int getWindowHeight() {
        return windowHeight;
    }

    public void setWindowHeight(int windowHeight) {
        this.windowHeight = windowHeight;
    }

    public int getLayoutType() {
        return layoutType;
    }

    public void setLayoutType(int layoutType) {
        this.layoutType = layoutType;
    }

    public PointF getStartPointFOfJoinLine() {
        return startPointFOfJoinLine;
    }

    public void setStartPointFOfJoinLine(PointF startPointFOfJoinLine) {
        this.startPointFOfJoinLine = startPointFOfJoinLine;
    }

    public PointF getEndPointFOfJoinLine() {
        return endPointFOfJoinLine;
    }

    public void setEndPointFOfJoinLine(PointF endPointFOfJoinLine) {
        this.endPointFOfJoinLine = endPointFOfJoinLine;
    }
}
