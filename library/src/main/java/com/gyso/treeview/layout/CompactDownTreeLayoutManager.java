package com.gyso.treeview.layout;

import android.content.Context;
import android.view.View;

import com.gyso.treeview.R;
import com.gyso.treeview.TreeViewContainer;
import com.gyso.treeview.adapter.TreeViewHolder;
import com.gyso.treeview.algorithm.table.Table;
import com.gyso.treeview.line.BaseLine;
import com.gyso.treeview.model.ITraversal;
import com.gyso.treeview.model.NodeModel;
import com.gyso.treeview.model.TreeModel;
import com.gyso.treeview.util.DensityUtils;
import com.gyso.treeview.util.TreeViewLog;
import com.gyso.treeview.util.ViewBox;

public class CompactDownTreeLayoutManager extends TreeLayoutManager {
    private static final String TAG = CompactDownTreeLayoutManager.class.getSimpleName();

    public CompactDownTreeLayoutManager(Context context, int spaceParentToChild, int spacePeerToPeer, BaseLine baseline) {
        super(context, spaceParentToChild, spacePeerToPeer, baseline);
    }

    @Override
    public void calculateByLayoutAlgorithm(TreeModel<?> mTreeModel) {
        new Table().reconstruction(mTreeModel,Table.COMPACT_TABLE);
    }

    @Override
    public int getTreeLayoutType() {
        return LAYOUT_TYPE_VERTICAL_DOWN;
    }

    public void performMeasureAndListen(TreeViewContainer treeViewContainer, TreeLayoutManager.MeasureListener measureListener) {
        final TreeModel<?> mTreeModel = treeViewContainer.getTreeModel();
        if (mTreeModel != null) {
            mContentViewBox.clear();
            floorMax.clear();
            deepMax.clear();
            ITraversal<NodeModel<?>> traversal = new ITraversal<NodeModel<?>>() {
                @Override
                public void next(NodeModel<?> next) {
                    measure(next, treeViewContainer);
                    if(measureListener!=null){
                        measureListener.onMeasureChild(next);
                    }
                }

                @Override
                public void finish() {
                    getPadding(treeViewContainer);
                    mContentViewBox.bottom += (paddingBox.bottom+paddingBox.top);
                    mContentViewBox.right  += (paddingBox.left+paddingBox.right);
                    fixedViewBox.setValues(mContentViewBox.top,mContentViewBox.left,mContentViewBox.right,mContentViewBox.bottom);
                    if(winHeight == 0 || winWidth==0){
                        return;
                    }
                    float scale = 1f*winWidth/winHeight;
                    float wr = 1f* mContentViewBox.getWidth()/winWidth;
                    float hr = 1f* mContentViewBox.getHeight()/winHeight;
                    if(wr>=hr){
                        float bh =  mContentViewBox.getWidth()/scale;
                        fixedViewBox.bottom = (int)bh;
                    }else{
                        float bw =  mContentViewBox.getHeight()*scale;
                        fixedViewBox.right = (int)bw;
                    }
                    mFixedDx = (fixedViewBox.getWidth()-mContentViewBox.getWidth())/2;
                    mFixedDy = (fixedViewBox.getHeight()-mContentViewBox.getHeight())/2;

                    //compute floor start position
                    for (int i = 0; i <= floorMax.size(); i++) {
                        int fn = (i == floorMax.size())?floorMax.size():floorMax.keyAt(i);
                        int preStart = floorStart.get(fn - 1, 0);
                        int preMax = floorMax.get(fn - 1, 0);
                        int startPos = (fn==0?(mFixedDy + paddingBox.top):spaceParentToChild) + preStart + preMax;
                        floorStart.put(fn,startPos);
                    }

                    //compute deep start position
                    for (int i = 0; i <= deepMax.size(); i++) {
                        int dn = (i == deepMax.size())?deepMax.size():deepMax.keyAt(i);
                        int preStart = deepStart.get(dn - 1, 0);
                        int preMax = deepMax.get(dn - 1, 0);
                        int startPos = (dn==0?(mFixedDx + paddingBox.left):spacePeerToPeer) + preStart + preMax;
                        deepStart.put(dn,startPos);
                    }

                    if(measureListener!=null){
                        measureListener.onMeasureFinished();
                    }
                }
            };
            mTreeModel.doTraversalNodes(traversal);
        }
    }

    @Override
    public void performMeasure(TreeViewContainer treeViewContainer) {
        performMeasureAndListen(treeViewContainer,null);
    }

    /**
     * set the padding box
     * @param treeViewContainer tree view
     */
    private void getPadding(TreeViewContainer treeViewContainer) {
        if(treeViewContainer.getPaddingStart()>0){
            paddingBox.setValues(
                    treeViewContainer.getPaddingTop(),
                    treeViewContainer.getPaddingLeft(),
                    treeViewContainer.getPaddingRight(),
                    treeViewContainer.getPaddingBottom());
        }else{
            int padding = DensityUtils.dp2px(treeViewContainer.getContext(),DEFAULT_CONTENT_PADDING_DP);
            paddingBox.setValues(padding,padding,padding,padding);
        }
    }

    private void measure(NodeModel<?> node, TreeViewContainer treeViewContainer) {
        TreeViewLog.e(TAG,node+"gyso-----");
        TreeViewHolder<?> currentHolder = treeViewContainer.getTreeViewHolder(node);
        View currentNodeView =  currentHolder==null?null:currentHolder.getView();
        if(currentNodeView==null){
            throw new NullPointerException(" currentNodeView can not be null");
        }
        int preMaxH = floorMax.get(node.floor);
        int curH = currentNodeView.getMeasuredHeight();
        if(preMaxH < curH){
            floorMax.put(node.floor,curH);
            int delta = spaceParentToChild +curH-preMaxH;
            mContentViewBox.bottom += delta;
        }

        int preMaxW = deepMax.get(node.deep);
        int curW = currentNodeView.getMeasuredWidth();
        if(preMaxW < curW){
            deepMax.put(node.deep,curW);
            int delta = spacePeerToPeer +curW-preMaxW;
            mContentViewBox.right += delta;
        }
    }

    @Override
    public void performLayout(final TreeViewContainer treeViewContainer) {
        final TreeModel<?> mTreeModel = treeViewContainer.getTreeModel();
        if (mTreeModel != null) {
            mTreeModel.doTraversalNodes(new ITraversal<NodeModel<?>>() {
                @Override
                public void next(NodeModel<?> next) {
                    layoutNodes(next, treeViewContainer);
                }

                @Override
                public void finish() {
                    onManagerFinishLayoutAllNodes(treeViewContainer);
                }
            });
        }
    }


    @Override
    public ViewBox getTreeLayoutBox() {
        return fixedViewBox;
    }

    private void layoutNodes(NodeModel<?> currentNode, TreeViewContainer treeViewContainer){
        TreeViewHolder<?> currentHolder = treeViewContainer.getTreeViewHolder(currentNode);
        View currentNodeView =  currentHolder==null?null:currentHolder.getView();
        int deep = currentNode.deep;
        int floor = currentNode.floor;

        if(currentNodeView==null){
            throw new NullPointerException(" currentNodeView can not be null");
        }

        int currentWidth = currentNodeView.getMeasuredWidth();
        int currentHeight = currentNodeView.getMeasuredHeight();

        int verticalCenterFix = Math.abs(currentWidth - deepMax.get(deep))/2;

        int top = floorStart.get(floor);
        int left  = deepStart.get(deep)+verticalCenterFix;
        int bottom = top+currentHeight;
        int right = left+currentWidth;

        ViewBox finalLocation = new ViewBox(top, left, bottom, right);
        onManagerLayoutNode(currentNode,currentNodeView,finalLocation,treeViewContainer);
    }
    public void onManagerLayoutNode(NodeModel<?> currentNode,
                                    View currentNodeView,
                                    ViewBox finalLocation,
                                    TreeViewContainer treeViewContainer){
        treeViewContainer.setTag(R.id.target_node,null);
        if (!layoutAnimatePrepare(currentNode, currentNodeView, finalLocation, treeViewContainer)) {
            currentNodeView.layout(finalLocation.left, finalLocation.top, finalLocation.right, finalLocation.bottom);
        }
    }

    public void onManagerFinishLayoutAllNodes(TreeViewContainer treeViewContainer){
        layoutAnimate(treeViewContainer);
    }
}
