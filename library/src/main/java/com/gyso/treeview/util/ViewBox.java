package com.gyso.treeview.util;

import android.graphics.Matrix;
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

    public ViewBox(int top, int left, int bottom, int right) {
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
        return new ViewBox((int)topI,(int)leftI,(int)bottomI,(int)rightI);
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
        setValues((int)topI,(int)leftI,(int)rightI,(int)bottomI);
        return new ViewBox((int)topI,(int)leftI,(int)bottomI,(int)rightI);
    }

    public boolean contain(ViewBox other){
        return other!=null &&
                top<=other.top &&
                left<=other.left &&
                right>=other.right &&
                bottom>=other.bottom;
    }

    public ViewBox multiply(float radio) {
        return new ViewBox(
                (int)(top * radio),
                (int)(left * radio),
                (int)(right * radio),
                (int)(bottom * radio)
              );
    }

    public ViewBox add(ViewBox other) {
        if (other == null) {
            return this;
        }
        return new ViewBox(
                top + other.top,
                left + other.left,
                right + other.right,
                bottom + other.bottom
        );
    }

    public ViewBox subtract(ViewBox other){
        if (other == null) {
            return this;
        }
        return new ViewBox(
                top - other.top,
                left - other.left,
                right - other.right,
                bottom - other.bottom
                );
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
                "t:" + top +
                " l:" + left +
                " r:" + right +
                " b:" + bottom +
                '}';
    }
}
