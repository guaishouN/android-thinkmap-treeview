package com.gyso.treeview.util;

import android.view.View;

public class ViewBox {

    public int top;
    public int left;
    public int right;
    public int bottom;

    public ViewBox(){

    }
    public ViewBox(View view){
        this(view.getTop(),view.getLeft(),view.getBottom(),view.getRight());
    }

    private ViewBox(int top, int left, int bottom, int right) {
        this.top = top;
        this.left = left;
        this.right = right;
        this.bottom = bottom;
    }

    public void setValues(int top, int left, int right, int bottom) {
        this.top = top;
        this.left = left;
        this.right = right;
        this.bottom = bottom;
    }

    public boolean contain(ViewBox other){
        return other!=null &&
                top<=other.top &&
                left<=other.left &&
                right>=other.right &&
                bottom>=other.bottom;
    }

    public void clear() {
        this.top = 0;
        this.left = 0;
        this.right = 0;
        this.bottom = 0;
    }

    /**
     * get the box height which added 2*dy
     * @return height
     */
    public int getHeight(){
        return bottom-top;
    }

    /**
     * get the box width which added 2*dy
     * @return width
     */
    public int getWidth(){
        return right-left;
    }

    @Override
    public String toString() {
        return "ViewBox{" +
                "top=" + top +
                ", left=" + left +
                ", right=" + right +
                ", bottom=" + bottom +
                '}';
    }
}
