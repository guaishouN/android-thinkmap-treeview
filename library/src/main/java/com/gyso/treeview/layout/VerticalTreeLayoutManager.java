package com.gyso.treeview.layout;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.gyso.treeview.R;
import com.gyso.treeview.TreeViewContainer;
import com.gyso.treeview.adapter.TreeViewHolder;
import com.gyso.treeview.line.BaseLine;
import com.gyso.treeview.model.ITraversal;
import com.gyso.treeview.model.NodeModel;
import com.gyso.treeview.model.TreeModel;
import com.gyso.treeview.util.DensityUtils;
import com.gyso.treeview.util.TreeViewLog;
import com.gyso.treeview.util.ViewBox;

import java.util.Map;

/**
 * @Author: 怪兽N
 * @Time: 2021/5/8  19:06
 * @Email: 674149099@qq.com
 * @WeChat: guaishouN
 * @Describe:
 * Vertically down layout the tree view
 */
public class VerticalTreeLayoutManager extends TreeLayoutManager {
    private static final String TAG = VerticalTreeLayoutManager.class.getSimpleName();

    public VerticalTreeLayoutManager(Context context, int spaceParentToChild, int spacePeerToPeer, BaseLine baseline) {
        super(context, spaceParentToChild, spacePeerToPeer, baseline);
    }

    @Override
    public int getTreeLayoutType() {
        return LAYOUT_TYPE_VERTICAL_DOWN;
    }

    @Override
    public void performMeasure(TreeViewContainer treeViewContainer) {
        final TreeModel<?> mTreeModel = treeViewContainer.getTreeModel();
        if (mTreeModel != null) {
            mContentViewBox.clear();
            floorMax.clear();
            deepMax.clear();
            ITraversal<NodeModel<?>> traversal = new ITraversal<NodeModel<?>>() {
                @Override
                public void next(NodeModel<?> next) {
                    measure(next, treeViewContainer);
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
                }
            };
            mTreeModel.doTraversalNodes(traversal);
        }
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
                    //means that smooth move from preLocation to curLocation
                    Object nodeTag = treeViewContainer.getTag(R.id.target_node);
                    Object targetNodeLocationTag = treeViewContainer.getTag(R.id.target_node_final_location);
                    Object relativeLocationMapTag = treeViewContainer.getTag(R.id.relative_locations);
                    if (nodeTag instanceof NodeModel
                            && targetNodeLocationTag instanceof ViewBox
                            && relativeLocationMapTag instanceof Map) {
                        ViewBox targetNodeLocation = (ViewBox) targetNodeLocationTag;
                        Map<NodeModel<?>,ViewBox> relativeLocationMap = (Map<NodeModel<?>,ViewBox>)relativeLocationMapTag;

                        AccelerateDecelerateInterpolator interpolator = new AccelerateDecelerateInterpolator();
                        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0f, 1f);
                        valueAnimator.setDuration(TreeViewContainer.DEFAULT_FOCUS_DURATION);
                        valueAnimator.setInterpolator(interpolator);
                        valueAnimator.addUpdateListener(value -> {
                            float ratio = (float) value.getAnimatedValue();
                            TreeViewLog.e(TAG, "valueAnimator update ratio[" + ratio + "]");
                            mTreeModel.doTraversalNodes(node -> {
                                TreeViewHolder<?> treeViewHolder = treeViewContainer.getTreeViewHolder(node);
                                if (treeViewHolder != null) {
                                    View view = treeViewHolder.getView();
                                    ViewBox preLocation = (ViewBox) view.getTag(R.id.node_pre_location);
                                    ViewBox deltaLocation = (ViewBox) view.getTag(R.id.node_delta_location);

                                    //calculate current location
                                    ViewBox currentLocation = preLocation.add(deltaLocation.multiply(ratio));
                                    view.layout(currentLocation.left,
                                                currentLocation.top,
                                              currentLocation.left+view.getMeasuredWidth(),
                                             currentLocation.top+view.getMeasuredHeight());
                                }
                            });
                        });

                        valueAnimator.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationStart(Animator animation, boolean isReverse) {
                                TreeViewLog.e(TAG, "onAnimationStart ");
                                //calculate and layout on preLocation
                                mTreeModel.doTraversalNodes(node -> {
                                    TreeViewHolder<?> treeViewHolder = treeViewContainer.getTreeViewHolder(node);
                                    if (treeViewHolder != null) {
                                        View view = treeViewHolder.getView();
                                        ViewBox relativeLocation = relativeLocationMap.get(treeViewHolder.getNode());

                                        //calculate location info
                                        ViewBox preLocation = targetNodeLocation.add(relativeLocation);
                                        ViewBox finalLocation = (ViewBox) view.getTag(R.id.node_final_location);
                                        ViewBox deltaLocation = finalLocation.subtract(preLocation);

                                        //save as tag
                                        view.setTag(R.id.node_pre_location, preLocation);
                                        view.setTag(R.id.node_delta_location, deltaLocation);

                                        //layout on preLocation
                                        view.layout(preLocation.left, preLocation.top, preLocation.left+view.getMeasuredWidth(), preLocation.top+view.getMeasuredHeight());
                                    }
                                });

                            }

                            @Override
                            public void onAnimationEnd(Animator animation, boolean isReverse) {
                                //clear tag
                                treeViewContainer.setTag(R.id.target_location_on_viewport, null);
                                treeViewContainer.setTag(R.id.relative_locations, null);
                                treeViewContainer.setTag(R.id.target_node, null);
                                treeViewContainer.setTag(R.id.target_node_final_location, null);

                                //layout on finalLocation
                                mTreeModel.doTraversalNodes(node -> {
                                    TreeViewHolder<?> treeViewHolder = treeViewContainer.getTreeViewHolder(node);
                                    if (treeViewHolder != null) {
                                        View view = treeViewHolder.getView();
                                        ViewBox finalLocation = (ViewBox) view.getTag(R.id.node_final_location);
                                        view.layout(finalLocation.left, finalLocation.top, finalLocation.right, finalLocation.bottom);
                                        view.setTag(R.id.node_pre_location,null);
                                        view.setTag(R.id.node_delta_location,null);
                                        view.setTag(R.id.node_final_location, null);
                                    }
                                });
                            }
                        });
                        valueAnimator.start();
                    }
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
        }

        int top = floorStart.get(floor);
        int left  = deepStart.get(deep)+verticalCenterFix+deltaWidth;
        int bottom = top+currentHeight;
        int right = left+currentWidth;

        Object tag = treeViewContainer.getTag(R.id.target_node);
        if(tag instanceof NodeModel){
            ViewBox finalLocation = new ViewBox(top, left, bottom, right);
            currentNodeView.setTag(R.id.node_final_location, finalLocation);
            if(tag.equals(currentNode)){
                treeViewContainer.setTag(R.id.target_node_final_location, finalLocation);
                TreeViewLog.e(TAG,"Get target location!");
            }
        }
        else{
            currentNodeView.layout(left,top,right,bottom);
        }
    }
}
