package com.gyso.treeview.layout;

import android.content.Context;
import android.util.Log;
import android.view.View;

import com.gyso.treeview.TreeViewContainer;
import com.gyso.treeview.adapter.TreeViewHolder;
import com.gyso.treeview.line.BaseLine;
import com.gyso.treeview.model.NodeModel;
import com.gyso.treeview.model.TreeModel;
import com.gyso.treeview.util.ViewBox;

import java.util.HashMap;
import java.util.Map;

/**
 * xw guaishouN
 * qq 674149099@qq.com
 */
public class BoxLeftTreeLayoutManager extends BoxRightTreeLayoutManager {
    public static final String TAG = BoxLeftTreeLayoutManager.class.getSimpleName();
    public int mirrorX = 0;
    public BoxLeftTreeLayoutManager(Context context, int spaceParentToChild, int spacePeerToPeer, BaseLine baseline) {
        super(context, spaceParentToChild, spacePeerToPeer, baseline);
    }

    @Override
    public int getTreeLayoutType() {
        return LAYOUT_TYPE_HORIZON_LEFT;
    }

    @Override
    public void onManagerFinishMeasureAllNodes(TreeViewContainer treeViewContainer) {
        super.onManagerFinishMeasureAllNodes(treeViewContainer);
        mirrorX = fixedViewBox.getWidth()/2;
    }

    @Override
    public void onManagerLayoutNode(NodeModel<?> currentNode,
                                    View currentNodeView,
                                    ViewBox finalLocation,
                                    TreeViewContainer treeViewContainer){
        finalLocation = finalLocation.mirrorByX(mirrorX);
        if (!layoutAnimatePrepare(currentNode, currentNodeView, finalLocation, treeViewContainer)) {
            currentNodeView.layout(finalLocation.left, finalLocation.top, finalLocation.right, finalLocation.bottom);
        }
    }
}
