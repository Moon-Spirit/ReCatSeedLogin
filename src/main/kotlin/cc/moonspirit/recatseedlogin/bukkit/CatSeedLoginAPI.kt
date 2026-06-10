/*
 * Original work: CatSeedLogin
 * Copyright (c) 2021 CatSeed
 *
 * Licensed under the MIT License - see the LICENSE file for details.
 * (Original package: cc.baka9.catseedlogin)
 *
 * -------------------------------------------------
 * Modifications and additional code
 * Copyright (c) 2026 Yueling
 * This work is licensed under the GNU GPL v3.0-or-later
 */

package cc.moonspirit.recatseedlogin.bukkit

import cc.moonspirit.recatseedlogin.bukkit.objects.LoginPlayerHelper

/**
 * CatSeedLoginAPI
 *
 * 对外暴露的静态 API 门面，供其他插件以静态方法的形式查询登录状态。
 *
 * @author handy
 */
class CatSeedLoginAPI {
    companion object {
        /**
         * 是否已登录
         *
         * @param name 玩家名
         * @return true 已登录
         */
        @JvmStatic
        fun isLogin(name: String): Boolean {
            return LoginPlayerHelper.isLogin(name)
        }

        /**
         * 是否已注册
         *
         * @param name 玩家名
         * @return true 已注册
         */
        @JvmStatic
        fun isRegister(name: String): Boolean {
            return LoginPlayerHelper.isRegister(name)
        }

        /**
         * 获取最后登录时间戳
         *
         * @param name 玩家名
         * @return 时间戳，未注册为 null
         * @since 1.4.2
         */
        @JvmStatic
        fun getLastLoginTime(name: String): Long? {
            return LoginPlayerHelper.getLastLoginTime(name)
        }
    }
}