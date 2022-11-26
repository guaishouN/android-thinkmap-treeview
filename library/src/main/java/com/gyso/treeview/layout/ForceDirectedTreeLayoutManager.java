package com.gyso.treeview.layout;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.annotation.NonNull;

import com.gyso.treeview.R;
import com.gyso.treeview.TreeViewContainer;
import com.gyso.treeview.adapter.TreeViewHolder;
import com.gyso.treeview.algorithm.force.FLink;
import com.gyso.treeview.algorithm.force.FNode;
import com.gyso.treeview.algorithm.force.Force;
import com.gyso.treeview.algorithm.force.ForceListener;
import com.gyso.treeview.algorithm.table.Table;
import com.gyso.treeview.line.BaseLine;
import com.gyso.treeview.model.ITraversal;
import com.gyso.treeview.model.NodeModel;
import com.gyso.treeview.model.TreeModel;
import com.gyso.treeview.util.DensityUtils;
import com.gyso.treeview.util.TreeViewLog;
import com.gyso.treeview.util.ViewBox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

public class ForceDirectedTreeLayoutManager extends TreeLayoutManager implements ForceListener {
    private final static String TAG = ForceDirectedTreeLayoutManager.class.getSimpleName();
    private Force force;
    private final Handler handler;
    private TreeViewContainer treeViewContainer;
    final Map<NodeModel<?>,FNode > fNodeCache = new HashMap<>();
    protected Map<NodeModel<?>,ViewBox> boxHashMap = null;
    public ForceDirectedTreeLayoutManager(Context context, BaseLine baseline) {
        super(context, 150, 150, baseline);
        handler = new Handler(Looper.getMainLooper());
        force = new Force(this);
        init();
    }

    @Override
    public void calculateByLayoutAlgorithm(TreeModel<?> mTreeModel) {
        //new Table().reconstruction(mTreeModel,Table.COMPACT_TABLE);
    }

    private void init() {
        handler.post(() ->
                force.setStrength(0.7f)
                    .setFriction(0.8f)
                    .setDistance(50)
                    .setCharge(-320f)
                    .setGravity(0.1f)
                    .setTheta(0.8f)
                    .setAlpha(0.2f));
    }

    @Override
    public ViewBox getTreeLayoutBox() {
        return fixedViewBox;
    }

    @Override
    public int getTreeLayoutType() {
        return LAYOUT_TYPE_FORCE_DIRECTED;
    }

    @Override
    public void performMeasure(TreeViewContainer treeViewContainer) {
        this.treeViewContainer = treeViewContainer;
        final TreeModel<?> mTreeModel = treeViewContainer.getTreeModel();
        if (mTreeModel != null) {
            mContentViewBox.clear();
            floorMax.clear();
            deepMax.clear();
            ITraversal<NodeModel<?>> traversal = new ITraversal<NodeModel<?>>() {
                @Override
                public void next(NodeModel<?> next) {
                    measure(next, treeViewContainer);
                    if(boxHashMap==null){
                        boxHashMap =new HashMap<>();
                        //firstLayoutAfterMeasure(next);
                    }
                }

                @Override
                public void finish() {
                    getPadding(treeViewContainer);
                    mContentViewBox.bottom += (paddingBox.bottom+paddingBox.top);
                    mContentViewBox.right  += (paddingBox.left+paddingBox.right);
                    fixedViewBox.setValues(mContentViewBox);
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
                    setUpData();
                }
            };
            mTreeModel.doTraversalNodes(traversal);
        }
    }

    private void setUpData() {
        if(treeViewContainer==null){
            return;
        }
        final TreeModel<?> mTreeModel = treeViewContainer.getTreeModel();
        if (mTreeModel != null) {
            final ArrayList<FNode> nodes = new ArrayList<>();
            final ArrayList<FLink> links= new ArrayList<>();
            fNodeCache.clear();
            mTreeModel.doTraversalNodes(new ITraversal<NodeModel<?>>() {
                @Override
                public void next(NodeModel<?> next) {
                    float radius = getViewRadius(next);
                    FNode parentFNode = fNodeCache.get(next);
                    if(parentFNode == null){
                        //deal node
                        parentFNode = new FNode(next.toString(),radius,next.floor);
                        setFNodePosition(parentFNode,next);
                        fNodeCache.put(next,parentFNode);
                        nodes.add(parentFNode);
                    }
                    //deal children
                    LinkedList<? extends NodeModel<?>> childNodes = next.getChildNodes();
                    if(!childNodes.isEmpty()){
                        for (NodeModel<?> child:childNodes){
                            if(child!=null){
                                FNode childFNode = fNodeCache.get(child);
                                if(childFNode==null){
                                    float r = getViewRadius(child);
                                    childFNode = new FNode(child.toString(),r,child.floor);
                                    setFNodePosition(childFNode,child);
                                    fNodeCache.put(child,childFNode);
                                    nodes.add(childFNode);
                                }
                                links.add(new FLink(parentFNode,childFNode));
                            }
                        }
                    }
                }

                @Override
                public void finish() {
                    force.setNodes(nodes)
                            .setLinks(links)
                            .start();
                }
            });
        }
    }

    private float getViewRadius(@NonNull NodeModel<?> nodeMode){
        TreeViewHolder<?> viewHolder = treeViewContainer.getTreeViewHolder(nodeMode);
        View theView =  viewHolder==null?null:viewHolder.getView();
        assert theView != null;
        float w = theView.getMeasuredWidth()/2f;
        float h = theView.getMeasuredHeight()/2f;
        float radius = (float)Math.sqrt(w * w + h * h);
        return radius;
    }

    private void setFNodePosition(FNode fNode, @NonNull NodeModel<?> nodeMode){
        ViewBox viewBox = boxHashMap.get(nodeMode);
        if(viewBox==null){
            NodeModel<?> parentNode = nodeMode.getParentNode();
            viewBox = new ViewBox();
            boxHashMap.put(nodeMode,viewBox);
            ViewBox parentViewBox = null;
            if(parentNode==null){
                parentViewBox = boxHashMap.get(nodeMode.treeModel.getRootNode());
            }else{
                parentViewBox = boxHashMap.get(parentNode);
            }
            Random rand = new Random();
            int radom1 = rand.nextInt(30)+30;
            int radom2 = rand.nextInt(30)+30;
            viewBox.top = parentViewBox.top+radom1;
            viewBox.left = parentViewBox.left+radom2;
        }
        fNode.y = viewBox.top;
        fNode.x = viewBox.left;
        TreeViewLog.d(TAG, nodeMode.toString()+" ["+fNode.x+","+fNode.y+"] ");
    }

    public void measure(NodeModel<?> node, TreeViewContainer treeViewContainer) {
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


    private void firstLayoutAfterMeasure(NodeModel<?> currentNode){
        TreeViewHolder<?> currentHolder = treeViewContainer.getTreeViewHolder(currentNode);
        View currentNodeView =  currentHolder==null?null:currentHolder.getView();
        int deep = currentNode.deep;
        int floor = currentNode.floor;
        int leafCount = currentNode.leafCount;

        if(currentNodeView==null){
            throw new NullPointerException(" currentNodeView can not be null");
        }

        int currentWidth = currentNodeView.getMeasuredWidth();
        int currentHeight = currentNodeView.getMeasuredHeight();

        int verticalCenterFix = Math.abs(currentWidth - deepMax.get(deep))/2;

        int deltaWidth = 0;
        if(leafCount>1){
            deltaWidth = (deepStart.get(deep + leafCount) - deepStart.get(deep)-currentWidth)/2-verticalCenterFix;
            deltaWidth -= spacePeerToPeer/2;
        }

        int top = floorStart.get(floor);
        int left  = deepStart.get(deep)+verticalCenterFix+deltaWidth;
        int bottom = top+currentHeight;
        int right = left+currentWidth;

        ViewBox finalLocation = new ViewBox(top, left, bottom, right);
        boxHashMap.put(currentNode,finalLocation);
        if(!layoutAnimatePrepare(currentNode,currentNodeView,finalLocation,treeViewContainer)){
            currentNodeView.layout(left,top,right,bottom);
        }
    }

    @Override
    public void performLayout(TreeViewContainer treeViewContainer) {
        this.treeViewContainer = treeViewContainer;
        if(treeViewContainer!=null){
            final TreeModel<?> mTreeModel = treeViewContainer.getTreeModel();
            if (mTreeModel != null) {
                mTreeModel.doTraversalNodes(new ITraversal<NodeModel<?>>() {
                    @Override
                    public void next(NodeModel<?> next) {
                        layoutNodesByForceDirected(next, treeViewContainer);
                    }

                    @Override
                    public void finish() {

                    }
                });
            }
        }
    }

    private void layoutNodesByForceDirected(NodeModel<?> currentNode, TreeViewContainer treeViewContainer){
        //TreeViewLog.e(TAG,"onLayout: "+currentNode);
        FNode fNode = fNodeCache.get(currentNode);
        if(fNode ==null){
            return;
        }
        if(currentNode.equals(currentNode.treeModel.getRootNode())){
            updateRootNodeDelta(treeViewContainer);
            return;
        }
        TreeViewHolder<?> currentHolder = treeViewContainer.getTreeViewHolder(currentNode);
        View currentNodeView =  currentHolder==null?null:currentHolder.getView();
        if(currentNodeView==null){
            throw new NullPointerException(" currentNodeView can not be null");
        }
        int currentWidth = currentNodeView.getMeasuredWidth();
        int currentHeight = currentNodeView.getMeasuredHeight();

        int top  = (int)fNode.x+extraDeltaY;
        int left = (int)fNode.y+extraDeltaX;
        int bottom = top+currentHeight;
        int right = left+currentWidth;
        ViewBox finalLocation = new ViewBox(top, left, bottom, right);
        boxHashMap.put(currentNode,finalLocation);
        if(!layoutAnimatePrepare(currentNode,currentNodeView,finalLocation,treeViewContainer)){
            currentNodeView.layout(left,top,right,bottom);
        }
    }

    private void updateRootNodeDelta(TreeViewContainer treeViewContainer) {
        NodeModel<?> rootNode = treeViewContainer.getTreeModel().getRootNode();
        TreeViewHolder<?> currentHolder = treeViewContainer.getTreeViewHolder(rootNode);
        View rootNodeView =  currentHolder==null?null:currentHolder.getView();
        if(rootNodeView==null){
            throw new NullPointerException(" rootNodeView can not be null");
        }
        int currentWidth = rootNodeView.getMeasuredWidth();
        int currentHeight = rootNodeView.getMeasuredHeight();
        FNode fNode = fNodeCache.get(rootNode);
        extraDeltaX = fixedViewBox.getWidth()/2;
        extraDeltaY = fixedViewBox.getHeight()/2;
        int top  = extraDeltaY;
        int left = extraDeltaX;
        int bottom = top+currentHeight;
        int right = left+currentWidth;
        extraDeltaY -= (int)fNode.x;
        extraDeltaX -= (int)fNode.y;
        rootNodeView.layout(left,top,right,bottom);
    }

    @Override
    public void onManagerLayoutNode(NodeModel<?> currentNode, View currentNodeView, ViewBox finalLocation, TreeViewContainer treeViewContainer) {

    }

    @Override
    public void onManagerFinishLayoutAllNodes(TreeViewContainer treeViewContainer) {

    }


    @Override
    public void refresh() {
        if(treeViewContainer!=null){
            performLayout(treeViewContainer);
        }
    }
}
