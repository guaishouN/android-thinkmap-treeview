package com.gyso.treeview.layout;

import android.content.Context;
import android.graphics.PointF;
import android.view.View;

import com.gyso.treeview.TreeViewContainer;
import com.gyso.treeview.adapter.TreeViewHolder;
import com.gyso.treeview.algorithm.ring.RingForCompact;
import com.gyso.treeview.algorithm.table.Table;
import com.gyso.treeview.line.BaseLine;
import com.gyso.treeview.model.ITraversal;
import com.gyso.treeview.model.NodeModel;
import com.gyso.treeview.model.TreeModel;
import com.gyso.treeview.util.DensityUtils;
import com.gyso.treeview.util.TreeViewLog;
import com.gyso.treeview.util.ViewBox;
import java.util.Map;

public class CompactRingTreeLayoutManager extends TreeLayoutManager {
    private static final String TAG = CompactRingTreeLayoutManager.class.getSimpleName();
    private RingForCompact ring=null;
    private Map<NodeModel<?>, PointF> ringPositions = null;
    public CompactRingTreeLayoutManager(Context context, int spaceParentToChild, int spacePeerToPeer, BaseLine baseline) {
        super(context, spaceParentToChild, spacePeerToPeer, baseline);
    }

    @Override
    public int getTreeLayoutType() {
        return LAYOUT_TYPE_RING;
    }

    @Override
    public void calculateByLayoutAlgorithm(TreeModel<?> mTreeModel) {
        new Table().reconstruction(mTreeModel,Table.COMPACT_TABLE);
    }

    public void performMeasureAndListen(TreeViewContainer treeViewContainer, TreeLayoutManager.MeasureListener measureListener) {
        final TreeModel<?> mTreeModel = treeViewContainer.getTreeModel();
        if (mTreeModel != null) {
            floorStart.clear();
            mContentViewBox.clear();
            floorMax.clear();
            deepMax.clear();
            ITraversal<NodeModel<?>> traversal = new ITraversal<NodeModel<?>>() {
                @Override
                public void next(NodeModel<?> next) {
                    measure(next, treeViewContainer);
                    if (measureListener != null) {
                        measureListener.onMeasureChild(next);
                    }
                }

                @Override
                public void finish() {
                    //add padding
                    getPadding(treeViewContainer);
                    mContentViewBox.bottom += (paddingBox.bottom + paddingBox.top);
                    mContentViewBox.right += (paddingBox.left + paddingBox.right);
                    fixedViewBox.setValues(mContentViewBox);
                    if (winHeight == 0 || winWidth == 0) {
                        return;
                    }
                    float scale = 1f * winWidth / winHeight;
                    float wr = 1f * mContentViewBox.getWidth() / winWidth;
                    float hr = 1f * mContentViewBox.getHeight() / winHeight;
                    if (wr >= hr) {
                        float bh = mContentViewBox.getWidth() / scale;
                        fixedViewBox.bottom = (int) bh;
                    } else {
                        float bw = mContentViewBox.getHeight() * scale;
                        fixedViewBox.right = (int) bw;
                    }
                    mFixedDx = (fixedViewBox.getWidth() - mContentViewBox.getWidth()) / 2;
                    mFixedDy = (fixedViewBox.getHeight() - mContentViewBox.getHeight()) / 2;

                    int rootCenterX = mFixedDx + fixedViewBox.getWidth() / 2;
                    int rootCenterY = mFixedDx + fixedViewBox.getHeight() / 2;
                    ring = RingForCompact.getInstance(mTreeModel).setCenter(rootCenterX,rootCenterY).setFloorStart(floorStart);
                    ringPositions = ring.genPositions();
                    if (measureListener != null) {
                        measureListener.onMeasureFinished();
                    }
                }
            };
            mTreeModel.doTraversalNodes(traversal);
        }
    }

    @Override
    public void performMeasure(TreeViewContainer treeViewContainer) {
        performMeasureAndListen(treeViewContainer, null);
    }

    private void measure(NodeModel<?> node, TreeViewContainer treeViewContainer) {
        TreeViewHolder<?> currentHolder = treeViewContainer.getTreeViewHolder(node);
        View currentNodeView = currentHolder == null ? null : currentHolder.getView();
        if (currentNodeView == null) {
            throw new NullPointerException(" currentNodeView can not be null");
        }
        int curH = currentNodeView.getMeasuredHeight();
        int curW = currentNodeView.getMeasuredWidth();
        int maxR = (int) Math.hypot(curH, curW)+(spacePeerToPeer/2);
        int preMaxR = floorMax.get(node.floor);
        if (preMaxR < maxR) {
            floorMax.put(node.floor, maxR);
        }
        int maxChildFl = floorMax.get(node.floor);
        NodeModel<?> parentNode = node.getParentNode();
        int contentMax = 0;
        if (parentNode != null) {
            int maxParentFl = floorMax.get(parentNode.floor);
            int parentStart = floorStart.get(parentNode.floor);
            int start = parentStart+maxParentFl / 2 + spaceParentToChild + maxChildFl / 2;
            floorStart.put(node.floor, start);
            contentMax = start+maxChildFl;
        } else {
            //only root will do this
            floorStart.put(node.floor, 0);
        }
        TreeViewLog.e(TAG,floorStart+"");
        mContentViewBox.bottom = mContentViewBox.right = Math.max(mContentViewBox.right,2*contentMax);
        TreeViewLog.e(TAG,"measure--"+mContentViewBox);
    }

    @Override
    public void performLayout(final TreeViewContainer treeViewContainer) {
        final TreeModel<?> mTreeModel = treeViewContainer.getTreeModel();
        if (mTreeModel != null) {
            mTreeModel.doTraversalNodes(new ITraversal<NodeModel<?>>() {
                @Override
                public void next(NodeModel<?> next) {
                    TreeViewLog.e(TAG, "performLayout next: "+next);
                    layoutNodes(next, treeViewContainer);
                }

                @Override
                public void finish() {
                    layoutAnimate(treeViewContainer);
                }
            });
        }
    }


    @Override
    public ViewBox getTreeLayoutBox() {
        return fixedViewBox;
    }

    private void layoutNodes(NodeModel<?> currentNode, TreeViewContainer treeViewContainer) {
        TreeViewHolder<?> currentHolder = treeViewContainer.getTreeViewHolder(currentNode);
        View currentNodeView = currentHolder == null ? null : currentHolder.getView();
        int deep = currentNode.deep;
        int floor = currentNode.floor;
        if (currentNodeView == null) {
            throw new NullPointerException(" currentNodeView can not be null");
        }

        int currentWidth = currentNodeView.getMeasuredWidth();
        int currentHeight = currentNodeView.getMeasuredHeight();

        int top = floorStart.get(floor);
        int left = deepStart.get(deep);
        if(ringPositions!=null){
            PointF position = ringPositions.get(currentNode);
            if(position!=null){
                top = (int)position.x;
                left =(int)position.y;
            }else{
                TreeViewLog.e(TAG,"layout "+currentNode+" error!! Position is null");
            }
        }else{
            TreeViewLog.e(TAG,"layout "+currentNode+" error!! PingPositions is null!!!");
            return;
        }
        int bottom = top + currentHeight;
        int right = left + currentWidth;
        TreeViewLog.e(TAG,"top["+top+"]left["+left+"]bottom["+bottom+"]right["+right+"]");
        ViewBox finalLocation = new ViewBox(top, left, bottom, right);
        if (!layoutAnimatePrepare(currentNode, currentNodeView, finalLocation, treeViewContainer)) {
            currentNodeView.layout(left, top, right, bottom);
        }
    }
}