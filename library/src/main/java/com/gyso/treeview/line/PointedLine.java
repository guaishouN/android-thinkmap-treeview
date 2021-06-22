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
 * @Time: 2021/5/8  15:55
 * @Email: 674149099@qq.com
 * @WeChat: guaishouN
 * @Describe:
 * PointedLine, looks like needle
 */
public class PointedLine extends BaseLine {
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
        int spacePeer = drawInfo.getSpacePeerToPeer();
        int spacePc = drawInfo.getSpaceParentToChild();
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
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeWidth(DensityUtils.dp2px(context,lineWidth));
        mPaint.setAntiAlias(true);

        //setPath
        mPath.reset();
        if (layoutType == TreeLayoutManager.LAYOUT_TYPE_HORIZON_RIGHT) {
            if(toView.getLeft()-fromView.getRight()<spaceParentToChild){
                return;
            }
            int deltaY = fromView.getTop()-toView.getTop();
            int distance = (int)Math.sqrt(deltaY*deltaY+spacePeer*spacePeer);
            int baseWidth = (distance/spacePeer)*lineWidth;

            PointF startPoint = PointPool.obtain(fromView.getRight(), (fromView.getTop() + fromView.getBottom()) / 2f + (0.7F * lineWidth + 0.3f * baseWidth) / 2f);
            PointF endPoint = PointPool.obtain(toView.getLeft(), (toView.getTop() + toView.getBottom()) / 2f);
            PointF midPoint1 = PointPool.obtain(startPoint.x + (endPoint.x - startPoint.x) / 4f, startPoint.y);
            PointF midPoint2 = PointPool.obtain(startPoint.x + (endPoint.x - startPoint.x) * 2 / 4f, endPoint.y + baseWidth / 5f);
            mPath.moveTo(startPoint.x, startPoint.y);
            mPath.cubicTo(
                    midPoint1.x, midPoint1.y,
                    midPoint2.x, midPoint2.y,
                    endPoint.x, endPoint.y+2);

            mPath.lineTo(endPoint.x, endPoint.y-2);
            PointF startPoint1 = PointPool.obtain(fromView.getRight(), (fromView.getTop() + fromView.getBottom()) / 2f - (0.7F * lineWidth + 0.3f * baseWidth) / 2f);
            PointF midPoint3 = PointPool.obtain(startPoint1.x + (endPoint.x - startPoint1.x) * 2 / 4f, endPoint.y - baseWidth / 5f);
            PointF midPoint4 = PointPool.obtain(startPoint1.x + (endPoint.x - startPoint1.x) / 4f, startPoint1.y);
            mPath.cubicTo(
                    midPoint3.x, midPoint3.y,
                    midPoint4.x, midPoint4.y,
                    startPoint1.x, startPoint1.y
            );
            //do not forget release
            PointPool.free(startPoint);
            PointPool.free(midPoint1);
            PointPool.free(midPoint2);
            PointPool.free(midPoint3);
            PointPool.free(midPoint4);
            PointPool.free(endPoint);
        }else if (layoutType == TreeLayoutManager.LAYOUT_TYPE_VERTICAL_DOWN) {
            if(toView.getTop()-fromView.getBottom()<spaceParentToChild){
                return;
            }
            int deltaX = fromView.getLeft()-toView.getLeft();
            int distance = (int)Math.sqrt(deltaX*deltaX+spacePc*spacePc);
            int baseWidth = (distance/spacePc)*lineWidth;

            PointF startPoint = PointPool.obtain((fromView.getLeft() + fromView.getRight()) / 2f - (0.7F * lineWidth + 0.3f * baseWidth) / 2f, fromView.getBottom());
            PointF endPoint = PointPool.obtain((toView.getLeft() + toView.getRight()) / 2f, toView.getTop());

            PointF midPoint1 = PointPool.obtain(startPoint.x , startPoint.y+ (endPoint.y - startPoint.y) / 4f);
            PointF midPoint2 = PointPool.obtain(endPoint.x - baseWidth / 5f, startPoint.y + (endPoint.y - startPoint.y) * 2 / 4f);
            mPath.moveTo(startPoint.x, startPoint.y);
            mPath.cubicTo(
                    midPoint1.x, midPoint1.y,
                    midPoint2.x, midPoint2.y,
                    endPoint.x-2, endPoint.y);

            mPath.lineTo(endPoint.x+2, endPoint.y);
            PointF startPoint1 = PointPool.obtain((fromView.getLeft() + fromView.getRight()) / 2f + (0.7F * lineWidth + 0.3f * baseWidth) / 2f, fromView.getBottom());
            PointF midPoint3 = PointPool.obtain(endPoint.x + baseWidth / 5f, startPoint1.y + (endPoint.y - startPoint1.y) * 2 / 4f);
            PointF midPoint4 = PointPool.obtain(startPoint1.x, startPoint1.y + (endPoint.y - startPoint1.y) / 4f);
            mPath.cubicTo(
                    midPoint3.x, midPoint3.y,
                    midPoint4.x, midPoint4.y,
                    startPoint1.x, startPoint1.y
            );
            //do not forget release
            PointPool.free(startPoint);
            PointPool.free(midPoint1);
            PointPool.free(midPoint2);
            PointPool.free(midPoint3);
            PointPool.free(midPoint4);
            PointPool.free(endPoint);
        } else {
            return;
        }

        //draw
        canvas.drawPath(mPath,mPaint);
    }
}
