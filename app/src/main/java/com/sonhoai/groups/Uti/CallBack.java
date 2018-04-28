package com.sonhoai.groups.Uti;

public interface CallBack<T> {
    public void isCompleted(T obj);
    public void isFail(T obj);
}
