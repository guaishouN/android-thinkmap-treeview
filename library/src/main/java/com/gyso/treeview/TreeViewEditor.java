package com.gyso.treeview;

import android.view.View;

import com.gyso.treeview.adapter.TreeViewAdapter;
import com.gyso.treeview.model.NodeModel;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.List;

/**
 * @Author: 怪兽N
 * @Time: 2021/6/9  15:31
 * @Email: 674149099@qq.com
 * @WeChat: guaishouN
 * @Describe: Android developer
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

    public View anchorNodeOnMidViewport(NodeModel<?> targetNode){
        //TODO move targetNode at center  of viewport
        return null;
    }

    /**
     *  change layout algorithm
     */
    public void changeLayoutAlgorithm(){

    }

    /**
     *  get current relation ship, you can use when you change
     * @param traverse relations node
     */
    public<T> void getCurrentRelationships(TraverseRelationshipCallback traverse){

    }

    /**
     *  get current relation ship, you can use when you change
     * @param traverse relations node
     * @param jsonStringFilePath jsonStringFilePath
     */
    public boolean save(String jsonStringFilePath, TraverseRelationshipCallback traverse){
            return false;
    }

    public boolean save(File jsonStringFile, TraverseRelationshipCallback traverse){
        return false;
    }

    /**
     * load data  from json String
     * @param jsonString string
     */
    public void load(String jsonString){

    }

    /**
     * load data  from file
     * @param jsonStringFile file
     */
    public void load(File jsonStringFile){

    }

    /**
     *  expand by node
     * @param targetParentNode targetParentNode
     */
    public void collapse(NodeModel<?> targetParentNode){

    }

    /**
     *  expand by node
     * @param targetParentNode targetParentNode
     */
    public void expand(NodeModel<?> targetParentNode){

    }

    /**
     *  focus on node
     * @param targetNode targetNode
     */
    public void focusOn(NodeModel<?> targetNode){

    }

    /**
     *  un focus on node
     * @param targetNode targetNode
     */
    public void unFocusOn(NodeModel<?> targetNode){

    }

    /**
     *  for support scroll view
     */
    public void  lockDragDirection(){

    }

    /**
     *  save  current state
     */
    public void saveLastSate(){

    }

    /**
    *  keep last location and
     */
    public void restoreLastSate(){

    }

    /**
     *  add on select listener
     */
    public void addOnSelectedListener(){

    }

    /**
     *  default: auto restructure by dragging;
     *  totally free drag;
     * @param status status
     */
    public void setEditStatus(int status){

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
        TreeViewContainer container = getContainer();
        if(container!=null){
            container.onAddNodes(parent,childNodes);
        }
    }

    /**
     * remove node
     * @param nodeToRemove node to remove
     */
    public void removeNode(NodeModel<?> nodeToRemove){
        TreeViewContainer container = getContainer();
        if(container!=null){
            container.onRemoveNode(nodeToRemove);
        }
    }

    /**
     * remove children nodes by parent node
     * @param parentNode parent node to remove children
     */
    public void removeNodeChildren(NodeModel<?> parentNode){
        TreeViewContainer container = getContainer();
        if(container!=null){
            container.onRemoveChildNodes(parentNode);
        }
    }

    public interface  TraverseRelationshipCallback{
       <T> void callback(T root, T parent , T child);
       default  void callbackView(View rootView, View parentView , View childView){};
    }

    public interface OnNodeSelectedCallback{
        <T> void callback(T clickNode, List<T> selectedList);
    }
}
