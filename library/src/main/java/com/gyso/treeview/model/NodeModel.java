package com.gyso.treeview.model;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.gyso.treeview.TreeViewContainer;
import com.gyso.treeview.layout.TreeLayoutManager;
import com.gyso.treeview.util.ViewBox;

import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

/**
 *  guaishouN 674149099@qq.com
 *
 *  Note:
 *  You should override {@link #hashCode()}{@link #equals(Object)} methods for collections operations in NodeModel,
 *  so that it works well because some methods of this class are using {@link java.util.Collection#contains(Object)} and so on.
 */
public class NodeModel<T> implements Serializable {
    private final static String TAG = NodeModel.class.getSimpleName();
    public TreeModel<T>  treeModel = null;

    /**
     * for mark this node
     */
    public boolean mark = false;
    public void mark(){
        mark = true;
    }
    public boolean isMarkThenReset(){
        boolean b = mark;
        mark = false;
        return b;
    }

    /**
     * the parent node,if root node parent node=null;
     */
    public NodeModel parentNode;

    /**
     * the data value
     */
    public T value;

    /**
     * have the child nodes
     */
    public final LinkedList<NodeModel<T>> childNodes;

    /**
     * focus tag for the tree add nodes
     */
    public transient boolean focus = false;

    /**
     * level of the tree
     */
    public int floor;

    /**
     * deep in the same floor of the whole tree
     */
    public int deep = 0;

    public boolean isExpand = true;

    public ViewBox viewBox = new ViewBox();

    private TreeLayoutManager nodeLayoutManager;

    public NodeModel(T value) {
        this.value = value;
        this.childNodes = new LinkedList<>();
        this.focus = false;
        this.parentNode = null;
    }

    public NodeModel() {
        this.childNodes = new LinkedList<>();
        this.focus = false;
        this.parentNode = null;
    }

    public NodeModel<T> getParentNode() {
        return parentNode;
    }

    public void setParentNode(NodeModel<T> parentNode) {
        this.parentNode = parentNode;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    @NonNull
    public LinkedList<NodeModel<T>> getChildNodes() {
        return childNodes;
    }

    @SafeVarargs
    public final void addChildNodes(NodeModel<T>... childNodes){
        if(childNodes == null || childNodes.length==0){
            return;
        }
        if(treeModel!=null){
            treeModel.addNode(this, childNodes);
            return;
        }
        innerAddChildNodes(Arrays.asList(childNodes));
    }

    public void addChildNodes(List<NodeModel<T>> childNodes) {
        if(treeModel!=null){
            treeModel.addNode(this, (NodeModel<T>[])childNodes.toArray());
            return;
        }
        innerAddChildNodes(childNodes);
    }

    protected void innerAddChildNodes(List<NodeModel<T>> childNodes){
        for (NodeModel aChild: childNodes) {
            addChildNode(aChild);
        }
    }

    /**
     * @param aChild node
     */
    private boolean addChildNode(NodeModel aChild){
        boolean isExist = childNodes.contains(aChild);
        if(!isExist){
            aChild.setParentNode(this);
            aChild.treeModel = this.treeModel;
            childNodes.add(aChild);
        }
        return isExist;
    }

    public void removeChildNode(NodeModel<T> aChild){
        if(treeModel!=null){
            treeModel.removeNode(this,aChild);
            return;
        }
        innerRemoveChildNode(aChild);
    }

    protected void innerRemoveChildNode(NodeModel<T> aChild){
        childNodes.remove(aChild);
        aChild.setParentNode(null);
    }

    /**
     * traverse
     * @param node to deal node
     */
    private void traverse(NodeModel<T> node, INext<T> next){
        traverse(node,next,true);
    }

    private void traverse(NodeModel<T> node, INext<T> next, boolean isIncludeSelf){
        if(node==null || next==null){
            return;
        }
        Stack<NodeModel<T>> stack = new Stack<>();
        stack.add(node);
        while (!stack.isEmpty()) {
            NodeModel<T> tmp = stack.pop();
            if(isIncludeSelf){
                next.next(tmp);
            }else if(tmp!=this){
                next.next(tmp);
            }
            LinkedList<NodeModel<T>> childNodes = tmp.getChildNodes();
            stack.addAll(childNodes);
        }
    }
    /**
     * traverse all sub node include self
     * @param next callback
     */
    public void traverseIncludeSelf(INext<T> next){
        if(next==null){
            return;
        }
        traverse(this,next,true);
    }

    /**
     * traverse all sub node exclude self
     * @param next callback
     */
    public void traverseExcludeSelf(INext<T> next){
        if(next==null){
            return;
        }
        traverse(this,next,false);
    }

    /**
     * traverse all nodes on branch  from me to root exclude self
     * @param next callback
     */
    public void traverseBranchParent(INext<T> next){
        if(next==null){
            return;
        }
        NodeModel<?> parent = parentNode;
        while (parent!=null){
            next.next((NodeModel<T>) parent);
            if(next.fetch((NodeModel<T>) parent)){
                break;
            }
            parent = parent.parentNode;
        }
    }

    /**
     * traverse all nodes on branch  from me to root include self
     * @param next callback
     */
    public void traverseBranch(INext<T> next){
        if(next==null){
            return;
        }
        NodeModel<?> parent = this;
        while (parent!=null){
            next.next((NodeModel<T>) parent);
            if(next.fetch((NodeModel<T>) parent)){
                break;
            }
            parent = parent.parentNode;
        }
    }

    /**
     * traverse only direct children
     * @param next callback
     */
    public void traverseDirectChildren(INext<T> next){
        if(next==null){
            return;
        }
        for (NodeModel<T> childNode : new LinkedList<>(childNodes)) {
            next.next(childNode);
        }
    }

    public boolean isFocus() {
        return focus;
    }

    public void setFocus(boolean focus) {
        this.focus = focus;
    }

    public int getFloor() {
        return floor;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }

    public void setNodeLayoutManager(TreeLayoutManager nodeLayoutManager) {
        this.nodeLayoutManager = nodeLayoutManager;
    }

    public void expand(){
        isExpand=true;
    }

    public void collapse(){
        isExpand=false;
    }

    public ViewBox getViewBox() {
        return viewBox;
    }

    public ViewBox onMeasure(TreeLayoutManager layoutManager, TreeViewContainer container) {
        final TreeLayoutManager useLayout = nodeLayoutManager==null?layoutManager:nodeLayoutManager;
        viewBox = useLayout.onMeasureNode(this,container);
        if(!this.isExpand){
            return viewBox;
        }
        traverseDirectChildren(childNode -> {
            childNode.onMeasure(useLayout, container);
            useLayout.onMeasureNodeBox(this,childNode,container);
        });
        return viewBox;
    }

    public void onLayout(ViewBox parentLocationBox,TreeLayoutManager layoutManager, TreeViewContainer container){
        final TreeLayoutManager useLayout = nodeLayoutManager==null?layoutManager:nodeLayoutManager;
        ViewBox baseLocationBox = useLayout.onLayoutNodeBox(parentLocationBox,this,container);
        if(!this.isExpand){
            return;
        }
        traverseDirectChildren(childNode -> {
            childNode.onLayout(baseLocationBox, useLayout, container);
            useLayout.onLayoutNode(baseLocationBox,childNode,container);
        });
    }

    public interface INext<E>{
        void next(NodeModel<E> node);
        default boolean fetch(NodeModel<E> node){ return false;}
    }

    @Override
    public int hashCode() {
        return value==null?super.hashCode():value.hashCode();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj instanceof NodeModel){
            if (value!=null){
                return value.equals(((NodeModel)obj).value);
            }else{
                return super.equals(obj);
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "NodeModel{" +
                "value=" + value +
                ", floor=" + floor +
                ", deep=" + deep +
                ", parent=" + (parentNode==null?null:parentNode.value) +
                '}';
    }
}
