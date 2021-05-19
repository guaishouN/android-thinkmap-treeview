package com.gyso.treeview.line;

import com.gyso.treeview.adapter.DrawInfo;

/**
 * @Author: 怪兽N
 * @Time: 2021/5/7  21:02
 * @Email: 674149099@qq.com
 * @WeChat: guaishouN
 * @Describe:
 * line to connect the fromNodeView and toNodeView
 */
public abstract class BaseLine {
    /**
     * this method will be invoke when the tree view is onDispatchDraw
    */
    public abstract void draw(DrawInfo drawInfo);
}
