package com.gyso.treeview.util;

import android.graphics.Matrix;
import android.view.View;

import com.gyso.treeview.TreeViewContainer;
import com.gyso.treeview.adapter.TreeViewHolder;
import com.gyso.treeview.model.NodeModel;

public class ViewBox {
    public static final int MIRROR_TYPE_NONE = 0;
    public static final int MIRROR_TYPE_X = 1;
    public static final int MIRROR_TYPE_Y = 2;
    public static final int MIRROR_TYPE_XY = 3;

    public int left;
    public int top;
    public int right;
    public int bottom;

    public ViewBox(){

    }
    public ViewBox(View view){
        this(view.getLeft(),view.getTop(),view.getRight(),view.getBottom());
    }

    public ViewBox(int left,int top, int right, int bottom) {
        this.top = top;
        this.left = left;
        this.right = right;
        this.bottom = bottom;
    }

    public void setValues(ViewBox viewBox) {
        this.top = viewBox.top;
        this.left = viewBox.left;
        this.right = viewBox.right;
        this.bottom = viewBox.bottom;
    }

    public void setValues(int left,int top,int right,int bottom) {
        this.top = top;
        this.left = left;
        this.bottom = bottom;
        this.right = right;
    }

    public static ViewBox getViewBox(View view) {
        return new ViewBox(view);
    }

    /**
     *.scaleX(centerM[0])
     *.translationX(centerM[2])
     *.scaleY(centerM[4])
     *.translationY(centerM[5])
     *
     *float left = v.getLeft()*v.getScaleX()+v.getTranslationX();
     *float top = v.getTop()*v.getScaleY()+v.getTranslationY();
     *float right = v.getRight()*v.getScaleX()+v.getTranslationX();
     *float bottom = v.getBottom()*v.getScaleY()+v.getTranslationY();
     */
    public ViewBox convert(Matrix matrix) {
        float[] fs = new float[9];
        matrix.getValues(fs);
        float scaleX = fs[Matrix.MSCALE_X];
        float scaleY = fs[Matrix.MSCALE_Y];
        float translX = fs[Matrix.MTRANS_X];
        float translY = fs[Matrix.MTRANS_Y];
        float leftI = left*scaleX+translX;
        float topI = top*scaleY+translY;
        float rightI = right*scaleX+translX;
        float bottomI = bottom*scaleY+translY;
        return new ViewBox((int)leftI,(int)topI,(int)rightI,(int)bottomI);
    }

    public ViewBox invert(Matrix matrix){
        float[] fs = new float[9];
        matrix.getValues(fs);
        float scaleX = fs[Matrix.MSCALE_X];
        float scaleY = fs[Matrix.MSCALE_Y];
        float translX = fs[Matrix.MTRANS_X];
        float translY = fs[Matrix.MTRANS_Y];
        float leftI = (left-translX)/scaleX;
        float topI = (top-translY)/scaleY;
        float rightI = (right-translX)/scaleX;
        float bottomI = (bottom-translY)/scaleY;
        setValues((int)leftI,(int)topI,(int)rightI,(int)bottomI);
        return new ViewBox((int)leftI,(int)topI,(int)rightI,(int)bottomI);
    }

    public boolean contain(ViewBox other){
        return other!=null &&
                top<=other.top &&
                left<=other.left &&
                right>=other.right &&
                bottom>=other.bottom;
    }

    public ViewBox mirrorByX(int centerX){
        return mirrorByXY(this, centerX,0,MIRROR_TYPE_X);
    }
    public ViewBox mirrorByY(int centerY){
        return mirrorByXY(this,0,centerY,MIRROR_TYPE_Y);
    }

    private ViewBox mirrorByXY(ViewBox box,int centerX,int centerY, int mirrorType){
        int left,right,top,bottom;
        switch (mirrorType){
            case MIRROR_TYPE_X:{
                right   = centerX*2 - box.left;
                left    = centerX*2 - box.right;
                top     = box.top;
                bottom  = box.bottom;
                break;
            }
            case MIRROR_TYPE_Y:{
                right   = box.right;
                left    = box.left;
                bottom  = centerY*2 - box.top;
                top     = centerY*2 - box.bottom;
                break;
            }
            case MIRROR_TYPE_XY:{
                right   = centerX*2 - box.left;
                left    = centerX*2 - box.right;
                bottom  = centerY*2 - box.top;
                top     = centerY*2 - box.bottom;
                break;
            }
            default:
                return box;
        }
        return new ViewBox(left, top, right, bottom);
    }

    public ViewBox multiply(float radio) {
        return new ViewBox(
                (int)(left * radio),
                (int)(top * radio),
                (int)(right * radio),
                (int)(bottom * radio)
              );
    }

    public ViewBox add(ViewBox other) {
        if (other == null) {
            return this;
        }
        return new ViewBox(
                left + other.left,
                top + other.top,
                right + other.right,
                bottom + other.bottom
        );
    }

    public ViewBox subtract(ViewBox other){
        if (other == null) {
            return this;
        }
        return new ViewBox(
                left - other.left,
                top - other.top,
                right - other.right,
                bottom - other.bottom);
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

    public void clear() {
        this.top = 0;
        this.left = 0;
        this.right = 0;
        this.bottom = 0;
    }

    @Override
    public String toString() {
        return "{" +
                " l:" + left +
                " t:" + top +
                " r:" + right +
                " b:" + bottom +
                '}';
    }
}
