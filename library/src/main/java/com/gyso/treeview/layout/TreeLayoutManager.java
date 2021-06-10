package com.gyso.treeview.layout;

import android.content.Context;
import android.util.SparseIntArray;
import android.view.View;

import com.gyso.treeview.TreeViewContainer;
import com.gyso.treeview.adapter.DrawInfo;
import com.gyso.treeview.line.BaseLine;
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
    public static final int DEFAULT_SPACE_PARENT_CHILD_DP = 50;
    public static final int DEFAULT_SPACE_PEER_PEER_DP = 20;
    public static final BaseLine DEFAULT_LINE = new SmoothLine();


    protected final ViewBox mContentViewBox;
    protected int spaceParentToChild;
    protected int spacePeerToPeer;

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
    protected SparseIntArray floorMax = new SparseIntArray(200);

    /**
     * the max value of node in the same deep
     */
    protected SparseIntArray deepMax = new SparseIntArray(200);

    /**
     * the start value of node in the same floor
     */
    protected SparseIntArray floorStart = new SparseIntArray(200);

    /**
     * the start value of node in the same deep
     */
    protected SparseIntArray deepStart = new SparseIntArray(200);

    protected int winHeight;
    protected int winWidth;

    private BaseLine baseline;

    public TreeLayoutManager(Context context) {
        this(context, DEFAULT_SPACE_PARENT_CHILD_DP, DEFAULT_SPACE_PEER_PEER_DP,DEFAULT_LINE);
    }
    public TreeLayoutManager(Context context, int spacePeerToPeer, int spaceParentToChild){
        this(context, spacePeerToPeer, spaceParentToChild,DEFAULT_LINE);
    }
    public TreeLayoutManager(Context context, BaseLine baseline){
        this(context, DEFAULT_SPACE_PARENT_CHILD_DP, DEFAULT_SPACE_PEER_PEER_DP, baseline);
    }

    public TreeLayoutManager(Context context, int spaceParentToChild, int spacePeerToPeer, BaseLine baseline) {
        mContentViewBox = new ViewBox();
        fixedViewBox = new ViewBox();
        paddingBox = new ViewBox();
        this.spaceParentToChild = DensityUtils.dp2px(context, spaceParentToChild);
        this.spacePeerToPeer = DensityUtils.dp2px(context, spacePeerToPeer);
        this.baseline = baseline;
    }

    public int getSpaceParentToChild() {
        return spaceParentToChild;
    }

    public void setSpaceParentToChild(int spaceParentToChild) {
        this.spaceParentToChild = spaceParentToChild;
    }

    public int getSpacePeerToPeer() {
        return spacePeerToPeer;
    }

    public void setSpacePeerToPeer(int spacePeerToPeer) {
        this.spacePeerToPeer = spacePeerToPeer;
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

    /**
     * when child node view is layout, your should call to record current location
     * @param child
     */
    public void onChildNodeViewLayout(View child){

    }
}
