package com.gyso.treeview.layout;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.SparseIntArray;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.gyso.treeview.R;
import com.gyso.treeview.TreeViewContainer;
import com.gyso.treeview.adapter.DrawInfo;
import com.gyso.treeview.adapter.TreeViewHolder;
import com.gyso.treeview.line.BaseLine;
import com.gyso.treeview.line.SmoothLine;
import com.gyso.treeview.model.NodeModel;
import com.gyso.treeview.model.TreeModel;
import com.gyso.treeview.util.DensityUtils;
import com.gyso.treeview.util.TreeViewLog;
import com.gyso.treeview.util.ViewBox;
import java.util.Map;

/**
 * guaishouN xw 674149099@qq.com
 */

public abstract class TreeLayoutManager {
    public static final String TAG = TreeLayoutManager.class.getSimpleName();
    public static final int LAYOUT_TYPE_HORIZON_RIGHT = 0;
    public static final int LAYOUT_TYPE_VERTICAL_DOWN = 1;
    public static final int LAYOUT_TYPE_FORCE_DIRECTED = 2;
    public static final int LAYOUT_TYPE_HORIZON_LEFT = 3;
    public static final int LAYOUT_TYPE_VERTICAL_UP = 4;
    public static final int LAYOUT_TYPE_RING = 5;
    public static final int LAYOUT_TYPE_HORIZON_LEFT_AND_RIGHT = 6;
    public static final int LAYOUT_TYPE_VERTICAL_DOWN_AND_UP = 7;

    /**
     * the content padding, unit is dp;
     */
    protected static final int DEFAULT_CONTENT_PADDING_DP = 50;
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

    protected int extraDeltaX,extraDeltaY;

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

    public abstract void  calculateByLayoutAlgorithm(TreeModel<?> mTreeModel);

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
        if(baseline!=null){
            baseline.draw(drawInfo);
        }
    }

    /**
     * Prepared animate data for view.
     * @param currentNode node
     * @param currentNodeView node view
     * @param finalLocation node view final position
     * @param treeViewContainer container
     * @return true will been means layout animating.
     */
    protected boolean layoutAnimatePrepare(NodeModel<?> currentNode,
                                           View currentNodeView,
                                           ViewBox finalLocation,
                                           TreeViewContainer treeViewContainer){
        Object targetNodeTag = treeViewContainer.getTag(R.id.target_node);
        if(targetNodeTag instanceof NodeModel){
            currentNodeView.setTag(R.id.node_final_location, finalLocation);
            if(targetNodeTag.equals(currentNode)){
                TreeViewLog.e(TAG,"Get target location!");
                treeViewContainer.setTag(R.id.target_node_final_location, finalLocation);

                //remove views
                if(!animateRemoveNodes(treeViewContainer, finalLocation)){
                    //TODO remove nodes directly
                    TreeViewLog.e(TAG,"Has remove nodes directly!");
                }

                Object targetLocationOnViewPortTag =treeViewContainer.getTag(R.id.target_location_on_viewport);
                if(targetLocationOnViewPortTag instanceof ViewBox){
                    ViewBox targetLocationOnViewPort=(ViewBox)targetLocationOnViewPortTag;

                    //fix pre size and location
                    float scale = targetLocationOnViewPort.getWidth() * 1f / finalLocation.getWidth();
                    treeViewContainer.setPivotX(0);
                    treeViewContainer.setPivotY(0);
                    treeViewContainer.setScaleX(scale);
                    treeViewContainer.setScaleY(scale);
                    float dx = targetLocationOnViewPort.left-finalLocation.left*scale;
                    float dy = targetLocationOnViewPort.top-finalLocation.top*scale;
                    treeViewContainer.setTranslationX(dx);
                    treeViewContainer.setTranslationY(dy);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * For layout animator
     * @param treeViewContainer container
     */
    protected void layoutAnimate(TreeViewContainer treeViewContainer) {
        TreeModel<?> mTreeModel = treeViewContainer.getTreeModel();
        //means that smooth move from preLocation to curLocation
        Object nodeTag = treeViewContainer.getTag(R.id.target_node);
        Object targetNodeLocationTag = treeViewContainer.getTag(R.id.target_node_final_location);
        Object relativeLocationMapTag = treeViewContainer.getTag(R.id.relative_locations);
        Object animatorTag = treeViewContainer.getTag(R.id.node_trans_animator);
        if(animatorTag instanceof ValueAnimator){
            ((ValueAnimator)animatorTag).end();
        }
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
                        if(preLocation !=null && deltaLocation!=null){
                            //calculate current location
                            ViewBox currentLocation = preLocation.add(deltaLocation.multiply(ratio));
                            view.layout(currentLocation.left,
                                    currentLocation.top,
                                    currentLocation.left+view.getMeasuredWidth(),
                                    currentLocation.top+view.getMeasuredHeight());
                        }
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
                            if(preLocation==null || finalLocation==null){
                                return;
                            }

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
                    treeViewContainer.setTag(R.id.node_trans_animator,null);

                    //layout on finalLocation
                    mTreeModel.doTraversalNodes(node -> {
                        TreeViewHolder<?> treeViewHolder = treeViewContainer.getTreeViewHolder(node);
                        if (treeViewHolder != null) {
                            View view = treeViewHolder.getView();
                            ViewBox finalLocation = (ViewBox) view.getTag(R.id.node_final_location);
                            if(finalLocation!=null){
                                view.layout(finalLocation.left, finalLocation.top, finalLocation.right, finalLocation.bottom);
                            }
                            view.setTag(R.id.node_pre_location,null);
                            view.setTag(R.id.node_delta_location,null);
                            view.setTag(R.id.node_final_location, null);
                            view.setElevation(TreeViewContainer.Z_NOR);
                        }
                    });
                }
            });
            treeViewContainer.setTag(R.id.node_trans_animator,valueAnimator);
            valueAnimator.start();
        }
    }

    /**
     * remove view animate
     * @param treeViewContainer container
     * @param targetLocation target location
     */
    private boolean animateRemoveNodes(TreeViewContainer treeViewContainer, ViewBox targetLocation) {
        Object removedViewMapTag = treeViewContainer.getTag(R.id.mark_remove_views);
        Object relativeLocationMapTag = treeViewContainer.getTag(R.id.relative_locations);
        if (removedViewMapTag instanceof Map && relativeLocationMapTag instanceof Map) {

            final Map<NodeModel<?>, ViewBox> relativeLocationMap = (Map<NodeModel<?>, ViewBox>) relativeLocationMapTag;
            final Map<NodeModel<?>, View> removedViewMap = (Map) removedViewMapTag;
            int deltaDpMove = DensityUtils.dp2px(treeViewContainer.getContext(),TreeViewContainer.DEFAULT_REMOVE_ANIMATOR_DES);

            ValueAnimator removeAnimator = ValueAnimator.ofFloat(0f, 1f);
            removeAnimator.setDuration(TreeViewContainer.DEFAULT_FOCUS_DURATION);
            removeAnimator.addUpdateListener(value -> {
                for (NodeModel<?> nodeToRemove : removedViewMap.keySet()) {
                    View view = removedViewMap.get(nodeToRemove);
                    ViewBox relativeLocation = relativeLocationMap.get(nodeToRemove);
                    ViewBox location = targetLocation.add(relativeLocation);
                    float v = (float)value.getAnimatedValue();
                    if (getTreeLayoutType() == LAYOUT_TYPE_VERTICAL_DOWN) {
                        view.layout(location.left,
                                location.top+(int)(deltaDpMove*v),
                                location.left+view.getMeasuredWidth(),
                                location.top+view.getMeasuredHeight()+(int)(deltaDpMove*v));
                    } else if (getTreeLayoutType() == LAYOUT_TYPE_HORIZON_RIGHT) {
                        view.layout(location.left+(int)(deltaDpMove*v),
                                location.top,
                                location.left+view.getMeasuredWidth()+(int)(deltaDpMove*v),
                                location.top+view.getMeasuredHeight());
                    }
                    view.setAlpha(1-v);
                }
            });

            removeAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    for (NodeModel<?> nodeToRemove : removedViewMap.keySet()) {
                        TreeViewLog.e(TAG,"removeAnimator onAnimationStart "+nodeToRemove);
                        View view = removedViewMap.get(nodeToRemove);
                        ViewBox relativeLocation = relativeLocationMap.get(nodeToRemove);
                        //calculate location info
                        ViewBox location = targetLocation.add(relativeLocation);
                        view.layout(location.left, location.top, location.right, location.bottom);
                    }
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    for (NodeModel<?> nodeToRemove : removedViewMap.keySet()) {
                        TreeViewLog.e(TAG,"removeAnimator onAnimationEnd "+nodeToRemove);
                        View view = removedViewMap.get(nodeToRemove);
                        treeViewContainer.removeView(view);
                        view.setAlpha(1);
                        TreeViewHolder<?> holder = treeViewContainer.getTreeViewHolder(nodeToRemove);
                        if (holder != null) {
                            treeViewContainer.recycleHolder(holder);
                        }
                    }
                    removedViewMap.clear();
                    treeViewContainer.setTag(R.id.mark_remove_views, null);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            removeAnimator.start();
            return true;
        }
        return false;
    }

    public void onManagerMeasureNode(NodeModel<?> currentNode,View currentNodeView,ViewBox finalLocation,TreeViewContainer treeViewContainer){}

    public void onManagerFinishMeasureAllNodes(TreeViewContainer treeViewContainer){}

    public void onManagerLayoutNode(NodeModel<?> currentNode,View currentNodeView,ViewBox finalLocation,TreeViewContainer treeViewContainer){}

    public void onManagerFinishLayoutAllNodes(TreeViewContainer treeViewContainer){}


    public interface  LayoutListener{
        default void onLayoutChild(NodeModel<?> next){};
        void onLayoutFinished();
    }

    public interface  MeasureListener{
        default void onMeasureChild(NodeModel<?> next){};
        void onMeasureFinished();
    }
}
