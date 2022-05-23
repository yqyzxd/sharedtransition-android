package com.github.yqyzxd.sharedtransition

import android.app.Activity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.view.doOnPreDraw
import androidx.viewpager.widget.ViewPager

/**
 * Copyright (C), 2015-2022, 杭州迈优文化创意有限公司
 * FileName: RecyclerViewReenterHandler
 * Author: wind
 * Date: 2022/5/10 15:59
 * Description: 针对RecyclerView实现过渡动画
 * Path: 路径
 * History:
 *  <author> <time> <version> <desc>
 *
 */
abstract class ViewPagerReenterHandler(private val mCallingActivity: Activity) : IRenterHandler {

    override fun onReenter(bundle: Bundle) {
        val startingPosition: Int =
            bundle.getInt(SharedTransition.EXTRA_KEY_START_POSITION)
        val currentPosition: Int =
            bundle.getInt(SharedTransition.EXTRA_KEY_CURRENT_POSITION)

        val viewPager=getViewPager(bundle)
        viewPager?.apply {
            if (startingPosition != currentPosition) {
                setCurrentItem(currentPosition,false)
            }
            ActivityCompat.postponeEnterTransition(mCallingActivity)
            doOnPreDraw {
                //it is necessary to request layout here in order to get a smooth transition.
                requestLayout()
                ActivityCompat.startPostponedEnterTransition(mCallingActivity)
            }
        }


    }

    abstract fun getViewPager(bundle: Bundle): ViewPager?
}