package com.coupleblog

import androidx.lifecycle.MutableLiveData

// arrayList with liveData, use this
class ListLiveData<T> : MutableLiveData<ArrayList<T>>()
{
    init {
        value = ArrayList()
    }

    fun add(item: T)
    {
        val items: ArrayList<T>? = value
        items!!.add(item)
        value = items
    }

    fun replace(idx: Int, replaceItem : T)
    {
        val items: ArrayList<T>? = value
        items!![idx] = replaceItem
        value = items
    }

    fun addAll(list: List<T>)
    {
        val items: ArrayList<T>? = value
        items!!.addAll(list)
        value = items
    }

    fun clear(notify: Boolean)
    {
        val items: ArrayList<T>? = value
        items!!.clear()
        if (notify)
            value = items
    }

    fun remove(item: T)
    {
        val items: ArrayList<T>? = value
        items!!.remove(item)
        value = items
    }

    fun removeAt(idx : Int)
    {
        val items: ArrayList<T>? = value
        items!!.removeAt(idx)
        value = items
    }

    fun removeLast()
    {
        if(isEmpty())
            assert(false)

        val items: ArrayList<T>? = value
        items!!.removeLast()
        value = items
    }

    fun removeFirst()
    {
        if(isEmpty())
            assert(false)

        val items: ArrayList<T>? = value
        items!!.removeFirst()
        value = items
    }

    fun notifyChange()
    {
        val items: ArrayList<T>? = value
        value = items
    }

    fun isEmpty() : Boolean = value!!.isEmpty()
    fun size() : Int = value!!.size
}