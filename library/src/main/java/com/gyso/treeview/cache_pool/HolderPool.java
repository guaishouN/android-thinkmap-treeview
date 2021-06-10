package com.gyso.treeview.cache_pool;

import android.graphics.PointF;

import androidx.core.util.Pools;

import com.gyso.treeview.adapter.TreeViewHolder;

/**
 * @Author: 怪兽N
 * @Time: 2021/6/10  14:37
 * @Email: 674149099@qq.com
 * @WeChat: guaishouN
 * @Describe:
 *  * Holder pool. {@link com.gyso.treeview.adapter.TreeViewHolder}
 *  * NOTE: not safe pool, please just use in UI thread
 */
public class HolderPool  extends Pools.SimplePool<TreeViewHolder<?>> {
    public static final int DEFAULT_SIZE = 30;
    private final static HolderPool POOL = new HolderPool();
    /**
     * Creates a new instance.
     */
    public HolderPool() {
        super(DEFAULT_SIZE);
    }

    public TreeViewHolder<?> obtain(){
        return POOL.acquire();
    }

    public void free(TreeViewHolder<?> holder){
        try {
            POOL.release(holder);
        }catch (IllegalStateException e){
            e.printStackTrace();
        }

    }

    public static void freeAll(){
        while (POOL.acquire()!=null);
    }
}
