package com.github.yqyzxd.sharedtransition

import android.util.SparseArray

/**
 * Copyright (C), 2015-2022, 杭州迈优文化创意有限公司
 * FileName: SharedTransitions
 * Author: wind
 * Date: 2022/5/10 16:20
 * Description: 描述该类的作用
 * Path: 路径
 * History:
 *  <author> <time> <version> <desc>
 *
 */
object SharedTransitions {
    @JvmStatic
    private var mTransitions: SparseArray<SharedTransition?> = SparseArray()

    @JvmStatic
    fun getTransition(id: Int): SharedTransition {
        var transition = mTransitions.get(id)
        if (transition == null) {
            transition=SharedTransition()
            mTransitions.put(id,transition)
        }
        return transition
    }

}