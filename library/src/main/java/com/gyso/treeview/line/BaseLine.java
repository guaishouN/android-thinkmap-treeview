package com.gyso.treeview.line;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;
import com.gyso.treeview.adapter.DrawInfo;
import com.gyso.treeview.adapter.TreeViewHolder;
import com.gyso.treeview.util.DensityUtils;

/**
 * @Author: 怪兽N
 * @Time: 2021/5/7  21:02
 * @Email: 674149099@qq.com
 * @WeChat: guaishouN
 * @Describe:
 * line to connect the fromNodeView and toNodeView
 */
public class BaseLine {
    /**
     * this method will be invoke when the tree view is onDispatchDraw
    */
    public void draw(DrawInfo drawInfo){
        Canvas canvas = drawInfo.getCanvas();
        TreeViewHolder<?> fromHolder = drawInfo.getFromHolder();
        TreeViewHolder<?> toHolder = drawInfo.getToHolder();
        Paint mPaint = drawInfo.getPaint();
        Path mPath = drawInfo.getPath();

        //get view and node
        View fromView = fromHolder.getView();
        View toView = toHolder.getView();
        Context context = fromView.getContext();

        //set paint
        mPaint.reset();
        mPaint.setColor(Color.MAGENTA);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(DensityUtils.dp2px(context,3));
        mPaint.setAntiAlias(true);

        int fromCenterX = (fromView.getLeft()+fromView.getRight())/2;
        int fromCenterY = (fromView.getTop()+fromView.getBottom())/2;
        int toCenterX = (toView.getLeft()+toView.getRight())/2;
        int toCenterY = (toView.getTop()+toView.getBottom())/2;

        mPath.moveTo(fromCenterX, fromCenterY);
        mPath.lineTo(toCenterX, toCenterY);

        //draw
        canvas.drawPath(mPath,mPaint);
    }
}
