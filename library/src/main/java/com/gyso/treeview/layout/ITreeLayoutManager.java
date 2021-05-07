package com.gyso.treeview.layout;

import com.gyso.treeview.TreeViewContainer;
import com.gyso.treeview.util.ViewBox;

/**
 * guaishouN xw 674149099@qq.com
 */

public interface ITreeLayoutManager {

    void performMeasure(TreeViewContainer treeViewContainer);

    void performLayout(TreeViewContainer treeViewContainer);

    ViewBox getTreeLayoutBox();

    void setViewport(int winHeight, int winWidth);
}
