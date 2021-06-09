package com.gyso.treeview;

import com.gyso.treeview.adapter.TreeViewAdapter;
import com.gyso.treeview.model.NodeModel;

import java.lang.ref.WeakReference;

/**
 * @Author: 怪兽N
 * @Time: 2021/6/9  15:31
 * @Email: 674149099@qq.com
 * @WeChat: guaishouN
 * @Describe:
 *
 * helper you edit your tree view.
 * Move node by dragging and remove node is support now.
 *
 * Note:
 * 1 An adapter must be set to GysoTreeView before you get an editor
 * 2 If you has set a new adapter, you should get an new editor
 */
public class TreeViewEditor {
    private final WeakReference<TreeViewAdapter<?>> adapterWeakReference;
    private final WeakReference<TreeViewContainer> containerWeakReference;
    protected TreeViewEditor(TreeViewContainer container){
        this.containerWeakReference = new WeakReference<>(container);
        this.adapterWeakReference = new WeakReference<>(container.getAdapter());
    }
    private TreeViewContainer getContainer(){
        return containerWeakReference.get();
    }
    private TreeViewAdapter<?> getAdapter(){
        return adapterWeakReference.get();
    }
    /**
     * let add node in window viewport
     */
    public void focusMidLocation(){
        TreeViewContainer container = getContainer();
        if (container!=null)container.focusMidLocation();
    }

    /**
     * before you edit, requestMoveNodeByDragging(true), so than you can drag to move the node
     * @param wantEdit true for edit mode
     */
    public void requestMoveNodeByDragging(boolean wantEdit){
        TreeViewContainer container = getContainer();
        if (container!=null)container.requestMoveNodeByDragging(wantEdit);
    }

    /**
     * add child nodes
     * @param parent parent node should has been in tree model
     * @param childNodes new nodes that will be add to tree model
     */
    public void addChildNodes(NodeModel<?> parent, NodeModel<?>... childNodes) {
        TreeViewAdapter<?> adapter = getAdapter();
        if(adapter!=null){
            adapter.getTreeModel().addNode(parent,childNodes);
            adapter.notifyDataSetChange();
        }
    }

    /**
     * remove node
     * @param nodeToRemove node to remove
     */
    public void removeNode(NodeModel<?> nodeToRemove){
        TreeViewAdapter<?> adapter = getAdapter();
        if(adapter!=null){
            adapter.getTreeModel().removeNode(nodeToRemove.getParentNode(),nodeToRemove);
            adapter.notifyDataSetChange();
        }
    }
}
