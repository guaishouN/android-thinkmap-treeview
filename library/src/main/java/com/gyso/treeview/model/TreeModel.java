package com.gyso.treeview.model;

import android.util.SparseArray;
import android.util.SparseIntArray;

import com.gyso.treeview.util.TreeViewLog;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

/**
 * manager the tree data nodes
 * guaishouN 674149099@qq.com
 */
public class TreeModel<T> implements Serializable {
    private static final String TAG = TreeModel.class.getSimpleName();
    /**
     * the root for the tree
     */
    private NodeModel<T> rootNode;
    private NodeModel<?> maxChildNode;
    private SparseArray<LinkedList<NodeModel<T>>> arrayByFloor = new SparseArray<>(10);
    private transient ITraversal<NodeModel<?>> iTraversal;
    private int maxDeep =0;
    private int minDeep =0;
    public TreeModel(NodeModel<T> rootNode) {
        this.rootNode = rootNode;
        this.maxChildNode = rootNode;
    }

    private boolean finishTraversal = false;

    /**
     * add the node in some father node
     * @param parent
     * @param childNodes
     */
    @SafeVarargs
    public final void addNode(NodeModel<?> parent, NodeModel<?>... childNodes) {
        if(parent!=null&&childNodes!=null && childNodes.length>0){
            parent.treeModel = this;
            List<NodeModel<T>> nodeModels = new LinkedList<>();
            for (int i = 0; i < childNodes.length; i++) {
                nodeModels.add((NodeModel<T>)childNodes[i]);
                ((NodeModel<T>)childNodes[i]).treeModel = this;
            }
            ((NodeModel<T>)parent).addChildNodes(nodeModels);
            List<NodeModel<T>> floorList = getFloorList(nodeModels.get(0).floor);
            floorList.addAll(nodeModels);
        }
        recordMaxChildrenNode(parent);
    }

    public void recordMaxChildrenNode(NodeModel<?> aChildNode){
        if(aChildNode==null){
            return;
        }
        LinkedList<?> cLs = aChildNode.getChildNodes();
        if(!cLs.isEmpty()){
            LinkedList<?> mLs = maxChildNode.getChildNodes();
           float k = (cLs.size()+aChildNode.leafCount)/2f;
           float l = (mLs.size()+maxChildNode.leafCount)/2f;
            if(rootNode.equals(maxChildNode)){
                if( cLs.size()>mLs.size()){
                    maxChildNode = aChildNode;
                }
            } else  if(k>l ){
                maxChildNode = aChildNode;
            }else if(k==l && cLs.size()>mLs.size()){
                maxChildNode = aChildNode;
            }
        }
    }

    /**
     * remove
     * @param parent p node
     * @param childNode c node
     */
    public void removeNode(NodeModel<?> parent, NodeModel<?> childNode) {
        if(parent!=null&&childNode!=null){
            parent.removeChildNode(childNode);
            List<NodeModel<T>> floorList = getFloorList(childNode.floor);
            floorList.remove(childNode);
        }
    }

    public NodeModel<T> getRootNode() {
        return rootNode;
    }

    public NodeModel<?> getMaxChildrenNodeAsRootNode() {
        if(rootNode.equals(maxChildNode)){
            return rootNode;
        }
        NodeModel parent = maxChildNode.getParentNode();
        while (parent!=null){
            //exchange parent
            NodeModel graP = parent.parentNode;
            graP.removeChildNode(parent);
            maxChildNode.parentNode = graP;
            parent.removeChildNode(maxChildNode);
            addNode(maxChildNode,parent);
            parent = graP;
        }
        return maxChildNode;
    }

    public void calculateTreeNodesDeep(){
        //calculateTreeNodesDeepLoose();
        calculateTreeNodesDeepCompact();
    }
    /**
     * calculate the deep of all tree nodes  by loose
     * deep has sort 0--->n
     */
    public void calculateTreeNodesDeepLoose(){
        TreeViewLog.e(TAG,"calculateTreeNodesDeep start");
        Stack<NodeModel<T>> stack = new Stack<>();
        NodeModel<T> rootNode = getRootNode();
        stack.add(rootNode);
        while (!stack.isEmpty()) {
            NodeModel<T> cur = stack.pop();
            NodeModel<T> parentNode = cur.getParentNode();
            cur.deep = 0;
            //if has peer, peer.deep+peer.leafCount+1/*peer self*/
            //if has no peer, parentNode.deep;
            if(parentNode!=null){
                int indexOfCur = parentNode.childNodes.indexOf(cur);
                if(indexOfCur>0){
                    NodeModel<T> prePeer = parentNode.childNodes.get(indexOfCur - 1);
                    cur.deep += prePeer.deep;
                    cur.deep += (prePeer.leafCount ==0?1:prePeer.leafCount);
                }else{
                    cur.deep = parentNode.deep;
                }
            }
            TreeViewLog.e(TAG,"calculateTreeNodesDeep--->"+cur.toString());
            LinkedList<NodeModel<T>> childNodes = cur.getChildNodes();
            for (int i = childNodes.size()-1; i >=0; i--) {
                stack.add(childNodes.get(i));
            }
        }
        TreeViewLog.e(TAG,"calculateTreeNodesDeep end");
    }

    /**
     * calculate the deep of all tree nodes  by compact way
     * deep has sort 0--->n
     */
    public void calculateTreeNodesDeepCompact(){
        TreeViewLog.e(TAG,"calculateTreeNodesDeep start");
        Deque<NodeModel<T>> deque = new ArrayDeque<>();
        NodeModel<T> rootNode = getRootNode();
        deque.add(rootNode);
        SparseIntArray deepSum = new SparseIntArray();
        //calculate base deep
        while (!deque.isEmpty()) {
            NodeModel<T> cur = deque.poll();
            cur.deep = deepSum.get(cur.floor,0);
            recordDeep(cur);
            deepSum.put(cur.floor,cur.deep+1);
            LinkedList<NodeModel<T>> childNodes = cur.getChildNodes();
            if (childNodes.size() > 0) {
                deque.addAll(childNodes);
            }
        }
        compactTable();
        TreeViewLog.e(TAG,"calculateTreeNodesDeepCompact end");
    }

    private void recordDeep(NodeModel<?> node) {
        if(node==null){
            return;
        }
        maxDeep = Math.max(node.deep, maxDeep);
        minDeep = Math.min(node.deep, minDeep);
    }

    public int getMinDeep(){
        return minDeep;
    }

    public int getMaxDeep(){
        return maxDeep;
    }


    private void compactTable(){
        //calculate final deep
        Deque<NodeModel<T>> deque = new ArrayDeque<>();
        LinkedList<NodeModel<T>> hasDealNode  = new LinkedList<>();
        deque.add(rootNode);
        SparseIntArray deepSum = new SparseIntArray();
        while (!deque.isEmpty()) {
            NodeModel<T> cur = deque.poll();
            if(hasDealNode.contains(cur)){
                continue;
            }else{
                hasDealNode.add(cur);
            }
            int peerMidDeep;
            int parentDeep;

            //deal parent and me
            NodeModel<T> parentNode = cur.getParentNode();
            if(parentNode!=null){
                LinkedList<NodeModel<T>> peers = parentNode.getChildNodes();
                if(peers !=null && !peers.isEmpty()){
                    int sum=0;
                    for(NodeModel<T> peer: peers){
                        sum +=peer.deep;
                    }
                    peerMidDeep=sum/peers.size();
                    parentDeep = parentNode.deep;
                    if(parentDeep!=peerMidDeep){
                        if (parentDeep<peerMidDeep){
                            //parent all left move to peers' mid
                            final int d = peerMidDeep-parentDeep;
                            fromRootToMyUpRight(cur, next -> {
                                next.deep +=d;
                                recordDeep(next);
                                //next's parent fit center
                                NodeModel<?> np = next.getParentNode();
                                int nSum=0;
                                if(np!=null){
                                    LinkedList<? extends NodeModel<?>> npChildNodes = np.getChildNodes();
                                    for(NodeModel<?> nPeer:npChildNodes ){
                                        nSum +=nPeer.deep;
                                    }
                                    np.deep = nSum/npChildNodes.size();
                                    recordDeep(np);
                                }
                            });
                        }else{
                            final int d = parentDeep - peerMidDeep;
                            //peers' mid move to parent
                            LinkedList<NodeModel<T>> nodeModels = arrayByFloor.get(cur.floor);
                            int md = cur.deep;
                            for (NodeModel<T> afterMe:nodeModels) {
                                if(afterMe.deep >= md){
                                    afterMe.deep += d;
                                    recordDeep(afterMe);
                                }
                            }
                        }
                    }
                }
            }
            LinkedList<NodeModel<T>> childNodes = cur.getChildNodes();
            maxDeep = Math.max(cur.deep, maxDeep);
            deepSum.put(cur.floor,cur.deep+1);
            if (childNodes.size() > 0) {
                deque.addAll(childNodes);
            }
        }
    }

    private void fromRootToMyUpRight(final NodeModel<T> node, ITraversal<NodeModel<?>> traversal){
        //mark line
        NodeModel<?> markNode = node;
        SparseIntArray modelSparseArray = new SparseIntArray();
        while (markNode!=null){
            modelSparseArray.put(markNode.floor,markNode.deep);
            markNode = markNode.parentNode;
        }
        Deque<NodeModel<T>> deque = new ArrayDeque<>();
        NodeModel<T> tmpNode = getRootNode();
        deque.add(tmpNode);
        while (!deque.isEmpty()) {
            tmpNode = deque.poll();
            int markDeep= modelSparseArray.get(tmpNode.floor);
            if(markDeep>tmpNode.deep){
                continue;
            }
            if(tmpNode.floor>=node.floor){
                break;
            }
            if (traversal != null) {
                traversal.next(tmpNode);
            }
            if(tmpNode==null){
                continue;
            }
            LinkedList<NodeModel<T>> childNodes = tmpNode.getChildNodes();
            if (childNodes.size() > 0) {
                deque.addAll(childNodes);
            }
        }
        if (traversal != null) {
            traversal.finish();
        }
    }

    private void moveDeepForRightAndUp(){

    }

    private void moveDeepForRightAndDown(){

    }


    /**
     *child nodes will ergodic in the last
     * 广度遍历
     * breadth search
     * For every floor , child nodes  has been  sort 0--->n;
     * And node will been display one by one floor.
     */
    private void ergodicTreeByFloor() {
        Deque<NodeModel<T>> deque = new ArrayDeque<>();
        NodeModel<T> rootNode = getRootNode();
        deque.add(rootNode);
        while (!deque.isEmpty()) {
            rootNode = deque.poll();
            if (iTraversal != null) {
                iTraversal.next(rootNode);
            }
            if(this.finishTraversal){
                break;
            }
            if(rootNode==null){
                continue;
            }
            LinkedList<NodeModel<T>> childNodes = rootNode.getChildNodes();
            if (childNodes.size() > 0) {
                deque.addAll(childNodes);
            }
        }
        if (iTraversal != null) {
            iTraversal.finish();
            this.finishTraversal = false;
        }
    }

    /**
     * @param floor  level
     * @return all nodes in the same floor
     */
    private List<NodeModel<T>> getFloorList(int floor){
        LinkedList<NodeModel<T>> nodeModels = arrayByFloor.get(floor);
        if(nodeModels==null){
            nodeModels = new LinkedList<>();
            arrayByFloor.put(floor,nodeModels);
        }
        return nodeModels;
    }

    public void setFinishTraversal(boolean finishTraversal) {
        this.finishTraversal = finishTraversal;
    }

    /**
     * when ergodic this tree, it will call back on {@link ITraversal)}
     * @param ITraversal node
     */
    public void doTraversalNodes(ITraversal<NodeModel<?>> ITraversal) {
        this.iTraversal = ITraversal;
        this.finishTraversal = false;
        ergodicTreeByFloor();
    }

    /**
     *child nodes will ergodic by deep
     * 深度遍历
     * depth search
     * For every  child node list  has been  sort 0--->n;
     * And node will been display one then the children by deep until end.
     */
    private void ergodicTreeByDeep(){
        Stack<NodeModel<T>> stack = new Stack<>();
        NodeModel<T> rootNode = getRootNode();
        stack.add(rootNode);
        while (!stack.isEmpty()) {
            rootNode = stack.pop();
            if (iTraversal != null) {
                iTraversal.next(rootNode);
            }
            if(this.finishTraversal){
                break;
            }
            if(rootNode==null){
                continue;
            }
            LinkedList<NodeModel<T>> childNodes = rootNode.getChildNodes();
            if (childNodes.size() > 0) {
                stack.addAll(childNodes);
            }
        }
        if (iTraversal != null) {
            iTraversal.finish();
            this.finishTraversal = false;
        }
    }
}
