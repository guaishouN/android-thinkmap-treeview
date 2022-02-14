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
        int layoutType = drawInfo.getLayoutType();
        int spacePeerToPeer = drawInfo.getSpacePeerToPeer();
        int spaceParentToChild = drawInfo.getSpaceParentToChild();
        //get view and node
        View fromView = fromHolder.getView();
        NodeModel<?> fromNode = fromHolder.getNode();
        View toView = toHolder.getView();
        NodeModel<?> toNode = toHolder.getNode();
        Context context = fromView.getContext();

        //set paint
        mPaint.reset();
        mPaint.setColor(lineColor);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(DensityUtils.dp2px(context,lineWidth));
        mPaint.setAntiAlias(true);

        //setPath
        mPath.reset();
        if(layoutType == TreeLayoutManager.LAYOUT_TYPE_HORIZON_RIGHT){
            if(toView.getLeft()-fromView.getRight()<spaceParentToChild){
                return;
            }
            PointF startPoint =  PointPool.obtain(fromView.getRight(),(fromView.getTop()+fromView.getBottom())/2f);
            PointF endPoint =  PointPool.obtain(toView.getLeft(),(toView.getTop()+toView.getBottom())/2f);
            mPath.moveTo(startPoint.x,startPoint.y);
            mPath.lineTo(endPoint.x,endPoint.y);
            //release
            PointPool.free(startPoint);
            PointPool.free(endPoint);
        }else if (layoutType == TreeLayoutManager.LAYOUT_TYPE_VERTICAL_DOWN){
            if(toView.getTop()-fromView.getBottom()<spaceParentToChild){
                return;
            }
            PointF startPoint =  PointPool.obtain((fromView.getLeft()+fromView.getRight())/2f,fromView.getBottom());
            PointF endPoint =  PointPool.obtain((toView.getLeft()+toView.getRight())/2f,toView.getTop());
            mPath.moveTo(startPoint.x,startPoint.y);
            mPath.lineTo(endPoint.x,endPoint.y);
            //release
            PointPool.free(startPoint);
            PointPool.free(endPoint);
        }else if (layoutType == TreeLayoutManager.LAYOUT_TYPE_FORCE_DIRECTED){
            PointF startPoint =  PointPool.obtain(fromView.getLeft()+fromView.getWidth()/2f,fromView.getTop()+fromView.getHeight()/2f);
            PointF endPoint =  PointPool.obtain(toView.getLeft()+toView.getWidth()/2f,toView.getTop()+toView.getHeight()/2f);
            mPath.moveTo(startPoint.x,startPoint.y);
            mPath.lineTo(endPoint.x,endPoint.y);
            //release
            PointPool.free(startPoint);
            PointPool.free(endPoint);
        }

        //draw
        canvas.drawPath(mPath,mPaint);
    }
}
