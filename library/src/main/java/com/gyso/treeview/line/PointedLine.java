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
import com.gyso.treeview.model.NodeModel;
import com.gyso.treeview.util.DensityUtils;

/**
 * @Author: 怪兽N
 * @Time: 2021/5/8  15:55
 * @Email: 674149099@qq.com
 * @WeChat: guaishouN
 * @Describe:
 * PointedLine, looks like needle
 */
public class PointedLine extends Baseline {
    public static final int DEFAULT_LINE_WIDTH_DP = 20;
    private int lineColor = Color.parseColor("#055287");
    private int lineWidth = DEFAULT_LINE_WIDTH_DP;
    public PointedLine() {
        super();
    }

    public PointedLine(int lineColor, int lineWidth_dp) {
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
        int spaceX = drawInfo.getSpaceX();
        int spaceY = drawInfo.getSpaceY();

        //get view and node
        View fromView = fromHolder.getView();
        NodeModel<?> fromNode = fromHolder.getNode();
        View toView = toHolder.getView();
        NodeModel<?> toNode = toHolder.getNode();
        Context context = fromView.getContext();

        //set paint
        mPaint.reset();
        mPaint.setColor(lineColor);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeWidth(DensityUtils.dp2px(context,lineWidth));
        mPaint.setAntiAlias(true);

        //setPath
        int deltaY = fromView.getTop()-toView.getTop();
        int distance = (int)Math.sqrt(deltaY*deltaY+spaceX*spaceX);
        int baseWidth = (distance/spaceX)*lineWidth;

        mPath.reset();
        PointF startPoint = PointPool.obtain(fromView.getRight(),(fromView.getTop()+fromView.getBottom())/2f+(0.7F*lineWidth+0.3f*baseWidth)/2f);
        PointF endPoint =  PointPool.obtain(toView.getLeft(),(toView.getTop()+toView.getBottom())/2f);
        PointF midPoint1 = PointPool.obtain(startPoint.x+(endPoint.x-startPoint.x)/4f,startPoint.y);
        PointF midPoint2 = PointPool.obtain(startPoint.x+(endPoint.x-startPoint.x)*2/4f,endPoint.y+baseWidth/5f);
        mPath.moveTo(startPoint.x,startPoint.y);
        mPath.cubicTo(
                midPoint1.x,midPoint1.y,
                midPoint2.x,midPoint2.y,
                endPoint.x,endPoint.y);

        PointF startPoint1 = PointPool.obtain(fromView.getRight(),(fromView.getTop()+fromView.getBottom())/2f-(0.7F*lineWidth+0.3f*baseWidth)/2f);
        PointF midPoint3 = PointPool.obtain(startPoint1.x+(endPoint.x - startPoint1.x)*2/4f,endPoint.y-baseWidth/5f);
        PointF midPoint4 = PointPool.obtain(startPoint1.x+(endPoint.x - startPoint1.x)/4f,startPoint1.y);
        mPath.cubicTo(
                midPoint3.x,midPoint3.y,
                midPoint4.x,midPoint4.y,
                startPoint1.x,startPoint1.y
        );

        //draw
        canvas.drawPath(mPath,mPaint);

        //do not forget release
        PointPool.free(startPoint);
        PointPool.free(midPoint1);
        PointPool.free(midPoint2);
        PointPool.free(midPoint3);
        PointPool.free(midPoint4);
        PointPool.free(endPoint);
    }
}
