package com.github.yqyzxd.sharedtransition

import android.os.Bundle
import android.view.View

/**
 * Copyright (C), 2015-2022, 杭州迈优文化创意有限公司
 * FileName: IReenterSharedElementProvider
 * Author: wind
 * Date: 2022/5/10 15:13
 * Description: 描述该类的作用
 * Path: 路径
 * History:
 *  <author> <time> <version> <desc>
 *
 */
interface IReenterSharedElementProvider {
    /**
     * bundle has below key :
     * SharedTransition.EXTRA_KEY_CURRENT_TRANSITION_NAME
     * SharedTransition.EXTRA_KEY_START_POSITION
     * SharedTransition.EXTRA_KEY_CURRENT_POSITION
     */
    fun sharedElements(bundle: Bundle): List<androidx.core.util.Pair<View,String>>?
}