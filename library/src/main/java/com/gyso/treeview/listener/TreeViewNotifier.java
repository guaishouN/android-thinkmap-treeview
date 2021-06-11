package com.gyso.treeview.listener;


import com.gyso.treeview.model.NodeModel;

/**
 * @Author: 怪兽N
 * @Time: 2021/4/23
 * @Email: 674149099@qq.com
 * @WeChat: guaishouN
 * @Describe:
 */
public interface TreeViewNotifier{
    void onDataSetChange();
    void onRemoveNodes(NodeModel<?>... nodeModel);
    void onItemViewChange(NodeModel<?> nodeModel);
    void onAddNodes(NodeModel<?> parent, NodeModel<?>... childNodes);
}