package com.gyso.treeview.adapter;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

import com.gyso.treeview.model.NodeModel;

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
    private Canvas canvas;
    private TreeViewHolder<?> fromHolder;
    private TreeViewHolder<?> toHolder;
    private Paint paint;
    private Path path;
    private int spaceX;
    private int spaceY;
    private int windowWidth;
    private int windowHeight;
    private int layoutType;

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

    public void setSpace(int spaceX,int spaceY) {
        this.spaceX = spaceX;
        this.spaceY = spaceY;
    }

    public int getSpaceX() {
        return spaceX;
    }

    public int getSpaceY() {
        return spaceY;
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
}
