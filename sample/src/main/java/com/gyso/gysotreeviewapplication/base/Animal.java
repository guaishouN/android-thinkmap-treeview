package com.gyso.gysotreeviewapplication.base;

/**
 * @Author: 怪兽N
 * @Time: 2021/5/7  19:12
 * @Email: 674149099@qq.com
 * @WeChat: guaishouN
 * @Describe:
 * node bean
 */
public class Animal {
    public int headId;
    public String name;
    public Animal(int headId, String name) {
        this.headId = headId;
        this.name = name;
    }

    @Override
    public String toString() {
        return "Animal{" +
                "headId=" + headId +
                ", name='" + name + '\'' +
                '}';
    }
}
