package com.gyso.treeview.algorithm.table;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class TableKey {
    public int floor;
    public int deep;

    public TableKey(int floor, int deep) {
        this.floor = floor;
        this.deep = deep;
    }

    @Override
    public int hashCode() {
        return floor;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj instanceof TableKey){
            TableKey o = (TableKey)obj;
            return floor == o.floor && deep==o.deep;
        }
        return false;
    }

    @NonNull
    @Override
    public String toString() {
        return "["+floor+","+deep+"]";
    }
}
