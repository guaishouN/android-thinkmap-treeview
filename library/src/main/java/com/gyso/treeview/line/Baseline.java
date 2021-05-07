package com.gyso.treeview.line;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

import com.gyso.treeview.adapter.TreeViewHolder;

/**
 * @Author: 怪兽N
 * @Time: 2021/5/7  21:02
 * @Email: 674149099@qq.com
 * @WeChat: guaishouN
 * @Describe:
 * line to connect the fromNodeView and toNodeView
 */
public abstract class Baseline<T>{
    /**
     * this method will be invoke when the tree view is onDispatchDraw
     * @param canvas tree view canvas
     * @param fromHolder from holder, you can get view and node data by this holder
     * @param toHolder to holder, you can get view and node data by this holder
     * @param mPaint the paint, you should not new Paint by yourself
     * @param mPath the path, just as the paint, strongly suggest get a path by new method
     */
    public abstract void onDrawLine(Canvas canvas, TreeViewHolder<T> fromHolder, TreeViewHolder<T> toHolder, Paint mPaint, Path mPath);
}
