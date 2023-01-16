package com.gyso.treeview.layout;

import android.content.Context;
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
public class BoxUpTreeLayoutManager extends BoxDownTreeLayoutManager {
    private static final String TAG = BoxUpTreeLayoutManager.class.getSimpleName();
    public int mirrorY = 0;
    public BoxUpTreeLayoutManager(Context context, int spaceParentToChild, int spacePeerToPeer, BaseLine baseline) {
        super(context, spaceParentToChild, spacePeerToPeer, baseline);
    }

    @Override
    public int getTreeLayoutType() {
        return LAYOUT_TYPE_VERTICAL_UP;
    }

    @Override
    public void onManagerFinishMeasureAllNodes(TreeViewContainer treeViewContainer) {
        super.onManagerFinishMeasureAllNodes(treeViewContainer);
        mirrorY = fixedViewBox.getHeight()/2;
    }

    @Override
    public void onManagerLayoutNode(NodeModel<?> currentNode,
                                    View currentNodeView,
                                    ViewBox finalLocation,
                                    TreeViewContainer treeViewContainer){
        finalLocation = finalLocation.mirrorByY(mirrorY);
        if (!layoutAnimatePrepare(currentNode, currentNodeView, finalLocation, treeViewContainer)) {
            currentNodeView.layout(finalLocation.left, finalLocation.top, finalLocation.right, finalLocation.bottom);
        }
    }
}
