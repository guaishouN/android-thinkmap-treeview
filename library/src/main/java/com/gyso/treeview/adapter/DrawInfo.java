package com.gyso.treeview.adapter;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

/**
 * @Author: 怪兽N
 * @Time: 2021/5/7  21:15
 * @Email: 674149099@qq.com
 * @WeChat: guaishouN
 * @Describe:
 * In order to draw a custom line, there are too many draw info should be passed.
 * So, this class is estimated to package the draw element that need to draw a colorful line between two nodes.
 */
public class DrawInfo<T> {
    private Canvas canvas;
    private TreeViewHolder<T> fromHolder;
    private TreeViewHolder<T> toHolder;
    private Paint paint;
    private Path path;
    private int spaceX;
    private int spaceY;

    public Canvas getCanvas() {
        return canvas;
    }

    public void setCanvas(Canvas canvas) {
        this.canvas = canvas;
    }

    public TreeViewHolder<T> getFromHolder() {
        return fromHolder;
    }

    public void setFromHolder(TreeViewHolder<T> fromHolder) {
        this.fromHolder = fromHolder;
    }

    public TreeViewHolder<T> getToHolder() {
        return toHolder;
    }

    public void setToHolder(TreeViewHolder<T> toHolder) {
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
}
