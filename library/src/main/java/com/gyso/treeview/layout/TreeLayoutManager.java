package com.gyso.treeview.layout;

import android.content.Context;
import android.util.SparseIntArray;

import com.gyso.treeview.TreeViewContainer;
import com.gyso.treeview.adapter.DrawInfo;
import com.gyso.treeview.line.Baseline;
import com.gyso.treeview.line.SmoothLine;
import com.gyso.treeview.util.DensityUtils;
import com.gyso.treeview.util.ViewBox;

/**
 * guaishouN xw 674149099@qq.com
 */

public abstract class TreeLayoutManager {
    public static final int LAYOUT_TYPE_HORIZON_RIGHT = 0;
    public static final int LAYOUT_TYPE_VERTICAL_DOWN = 1;
    /**
     * the content padding, unit is dp;
     */
    protected static final int DEFAULT_CONTENT_PADDING_DP = 100;
    public static final int  DEFAULT_SPACE_X_DP = 50;
    public static final int  DEFAULT_SPACE_Y_DP = 20;
    public static final Baseline  DEFAULT_LINE = new SmoothLine();


    protected final ViewBox mContentViewBox;
    protected int spaceY;
    protected int spaceX;

    /**
     * the fixedViewBox means that the fixedViewBox's width/height is the same as the given viewPort's.
     */
    protected final ViewBox fixedViewBox;
    protected int mFixedDx;
    protected int mFixedDy;

    /**
     * content padding box
     */
    protected final ViewBox paddingBox;

    /**
     * the max value of node in the same floor
     */
    protected SparseIntArray floorMax = new SparseIntArray(10);
    /**
     * the max value of node in the same deep
     */
    protected SparseIntArray deepMax = new SparseIntArray(200);
    protected int winHeight;
    protected int winWidth;

    private Baseline baseline;

    public TreeLayoutManager(Context context) {
        this(context,DEFAULT_SPACE_X_DP,DEFAULT_SPACE_Y_DP,DEFAULT_LINE);
    }
    public TreeLayoutManager(Context context,int spaceX, int spaceY){
        this(context,spaceX,spaceY,DEFAULT_LINE);
    }
    public TreeLayoutManager(Context context,Baseline baseline){
        this(context,DEFAULT_SPACE_X_DP,DEFAULT_SPACE_Y_DP, baseline);
    }

    public TreeLayoutManager(Context context,int spaceX, int spaceY, Baseline baseline) {
        mContentViewBox = new ViewBox();
        fixedViewBox = new ViewBox();
        paddingBox = new ViewBox();
        this.spaceX = DensityUtils.dp2px(context,spaceX);
        this.spaceY = DensityUtils.dp2px(context,spaceY);
        this.baseline = baseline;
    }

    public int getSpaceY() {
        return spaceY;
    }

    public void setSpaceY(int spaceY) {
        this.spaceY = spaceY;
    }

    public int getSpaceX() {
        return spaceX;
    }

    public void setSpaceX(int spaceX) {
        this.spaceX = spaceX;
    }

    public void setViewport(int winHeight, int winWidth) {
        this.winHeight =winHeight;
        this.winWidth = winWidth;
    }

    public abstract void performMeasure(TreeViewContainer treeViewContainer);

    public abstract void performLayout(TreeViewContainer treeViewContainer);

    public abstract ViewBox getTreeLayoutBox();

    public abstract int getTreeLayoutType();

    /**
     * draw line between node and node by you decision, you can get all draw element drawInfo;
     * canvas->total canvas for this tree view
     * fromHolder->holder from which one, you can get fromView and fromNodeModel from this holder
     * toHolder->holder to which one, you can get toView and toNodeModel from this holder
     * paint->paint from parent, don't create a new one because the method of onDraw(Canvas canvas) exec too much
     * path->path from parent, same as paint
     */
    public void performDrawLine(DrawInfo drawInfo){
        baseline.draw(drawInfo);
    }
}
