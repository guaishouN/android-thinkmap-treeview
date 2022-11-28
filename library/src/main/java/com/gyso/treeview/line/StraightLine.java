package com.gyso.treeview.line;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.view.View;

import com.gyso.treeview.adapter.DrawInfo;
import com.gyso.treeview.adapter.TreeViewHolder;
import com.gyso.treeview.cache_pool.PointPool;
import com.gyso.treeview.layout.TreeLayoutManager;
import com.gyso.treeview.model.NodeModel;
import com.gyso.treeview.util.DensityUtils;

/**
 * @Author: 怪兽N
 * @Time: 2021/5/8  9:40
 * @Email: 674149099@qq.com
 * @WeChat: guaishouN
 * @Describe:
 * Straight Line
 */
public class StraightLine extends BaseLine {
    public static final int DEFAULT_LINE_WIDTH_DP = 3;
    private int lineColor = Color.parseColor("#055287");
    private int lineWidth = DEFAULT_LINE_WIDTH_DP;
    public StraightLine() {
        super();
    }

    public StraightLine(int lineColor, int lineWidth_dp) {
        this();
        this.lineColor = lineColor;
        this.lineWidth = lineWidth_dp;
    }

    public void setLineColor(int lineColor) {
        this.lineColor = lineColor;
    }

    public void setLineWidth(int lineWidth) {
        this.lineWidth = lineWidth;
    }

    @Override
    public void draw(DrawInfo drawInfo) {
        Canvas canvas = drawInfo.getCanvas();
        TreeViewHolder<?> fromHolder = drawInfo.getFromHolder();
        TreeViewHolder<?> toHolder = drawInfo.getToHolder();
        Paint mPaint = drawInfo.getPaint();
        Path mPath = drawInfo.getPath();
        int holderLayoutType = toHolder.getHolderLayoutType();
        int spacePeerToPeer = drawInfo.getSpacePeerToPeer();
        int spaceParentToChild = drawInfo.getSpaceParentToChild();
        //get view and node
        View fromView = fromHolder.getView();
        NodeModel<?> fromNode = fromHolder.getNode();
        View toView = toHolder.getView();
        NodeModel<?> toNode = toHolder.getNode();
        Context context = fromView.getContext();


        PointF startPoint,endPoint;
        if(holderLayoutType == TreeLayoutManager.LAYOUT_TYPE_HORIZON_RIGHT){
            startPoint =  PointPool.obtain(fromView.getRight(),(fromView.getTop()+fromView.getBottom())/2f);
            endPoint =  PointPool.obtain(toView.getLeft(),(toView.getTop()+toView.getBottom())/2f);

        }else if(holderLayoutType == TreeLayoutManager.LAYOUT_TYPE_HORIZON_LEFT){
            startPoint =  PointPool.obtain(fromView.getLeft(),(fromView.getTop()+fromView.getBottom())/2f);
            endPoint =  PointPool.obtain(toView.getRight(),(toView.getTop()+toView.getBottom())/2f);

        }else if(holderLayoutType == TreeLayoutManager.LAYOUT_TYPE_VERTICAL_UP){
            startPoint =  PointPool.obtain((fromView.getLeft()+fromView.getRight())/2f,fromView.getTop());
            endPoint =  PointPool.obtain((toView.getLeft()+toView.getRight())/2f,toView.getBottom());

        }else if (holderLayoutType == TreeLayoutManager.LAYOUT_TYPE_VERTICAL_DOWN){
            startPoint =  PointPool.obtain((fromView.getLeft()+fromView.getRight())/2f,fromView.getBottom());
            endPoint =  PointPool.obtain((toView.getLeft()+toView.getRight())/2f,toView.getTop());

        }else{
            super.draw(drawInfo);
            return;
        }

        //set paint
        mPaint.reset();
        mPaint.setColor(lineColor);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(DensityUtils.dp2px(context,lineWidth));
        mPaint.setAntiAlias(true);

        mPath.moveTo(startPoint.x,startPoint.y);
        mPath.lineTo(endPoint.x,endPoint.y);
        //release
        PointPool.free(startPoint);
        PointPool.free(endPoint);
        //draw
        canvas.drawPath(mPath,mPaint);
    }
}
