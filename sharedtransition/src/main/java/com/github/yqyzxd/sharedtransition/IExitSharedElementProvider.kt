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
interface IExitSharedElementProvider {

    fun shareElements():List<androidx.core.util.Pair<View,String>>
}