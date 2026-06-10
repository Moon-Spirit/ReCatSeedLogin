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

package cc.moonspirit.recatseedlogin.bukkit.command

import cc.moonspirit.recatseedlogin.bukkit.Cache
import cc.moonspirit.recatseedlogin.bukkit.CatScheduler
import cc.moonspirit.recatseedlogin.bukkit.CatSeedLogin
import cc.moonspirit.recatseedlogin.bukkit.Communication
import cc.moonspirit.recatseedlogin.bukkit.Config
import cc.moonspirit.recatseedlogin.bukkit.PluginContext
import cc.moonspirit.recatseedlogin.bukkit.database.MySQL
import cc.moonspirit.recatseedlogin.bukkit.database.SQLite
import cc.moonspirit.recatseedlogin.bukkit.objects.LoginPlayerHelper
import cc.moonspirit.recatseedlogin.common.model.LoginPlayer
import cc.moonspirit.recatseedlogin.util.Util
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.function.BooleanSupplier
import java.util.function.Consumer
import java.util.regex.Pattern
import java.util.stream.Collectors

class CommandCatSeedLogin : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, lable: String, args: Array<String>): Boolean {
        return reload(sender, args)
                || setPwd(sender, args)
                || delPlayer(sender, args)
                || setIpCountLimit(sender, args)
                || limitChineseID(sender, args)
                || bedrockLoginBypass(sender, args)
                || LoginwiththesameIP(sender, args)
                || setIdLength(sender, args)
                || beforeLoginNoDamage(sender, args)
                || setReenterInterval(sender, args)
                || afterLoginBack(sender, args)
                || setSpawnLocation(sender, args)
                || commandWhiteListInfo(sender, args)
                || commandWhiteListAdd(sender, args)
                || commandWhiteListDel(sender, args)
                || canTpSpawnLocation(sender, args)
                || autoKick(sender, args)
                || setIpRegCountLimit(sender, args)
                || deathStateQuitRecordLocation(sender, args)
    }

    // ---- Helper: Boolean Toggle ----

    private class BoolSetting(
        val getter: BooleanSupplier,
        val setter: Consumer<Boolean>,
        val label: String
    )

    private fun toggle(sender: CommandSender, args: Array<String>, key: String, setting: BoolSetting): Boolean {
        if (args.isEmpty() || !args[0].equals(key, ignoreCase = true)) return false
        try {
            setting.setter.accept(!setting.getter.asBoolean)
            Config.Settings.save()
            sender.sendMessage("§e" + setting.label + " " + (if (setting.getter.asBoolean) "§a开启" else "§8关闭"))
        } catch (e: Exception) {
            sender.sendMessage("§c设置失败: " + e.message)
        }
        return true
    }

    // ---- Toggle Settings ----

    private fun deathStateQuitRecordLocation(sender: CommandSender, args: Array<String>): Boolean {
        return toggle(sender, args, "deathStateQuitRecordLocation",
            BoolSetting({ Config.Settings.DeathStateQuitRecordLocation },
                { v: Boolean -> Config.Settings.DeathStateQuitRecordLocation = v },
                "死亡状态退出游戏记录退出位置")
        )
    }

    private fun canTpSpawnLocation(sender: CommandSender, args: Array<String>): Boolean {
        return toggle(sender, args, "canTpSpawnLocation",
            BoolSetting({ Config.Settings.CanTpSpawnLocation },
                { v: Boolean -> Config.Settings.CanTpSpawnLocation = v },
                "登录之前强制在登陆地点")
        )
    }

    private fun afterLoginBack(sender: CommandSender, args: Array<String>): Boolean {
        return toggle(sender, args, "afterLoginBack",
            BoolSetting({ Config.Settings.AfterLoginBack },
                { v: Boolean -> Config.Settings.AfterLoginBack = v },
                "登陆之后返回下线地点")
        )
    }

    private fun beforeLoginNoDamage(sender: CommandSender, args: Array<String>): Boolean {
        return toggle(sender, args, "beforeLoginNoDamage",
            BoolSetting({ Config.Settings.BeforeLoginNoDamage },
                { v: Boolean -> Config.Settings.BeforeLoginNoDamage = v },
                "登陆之前不受到伤害")
        )
    }

    private fun limitChineseID(sender: CommandSender, args: Array<String>): Boolean {
        return toggle(sender, args, "limitChineseID",
            BoolSetting({ Config.Settings.LimitChineseID },
                { v: Boolean -> Config.Settings.LimitChineseID = v },
                "限制中文游戏名")
        )
    }

    private fun bedrockLoginBypass(sender: CommandSender, args: Array<String>): Boolean {
        return toggle(sender, args, "bedrockLoginBypass",
            BoolSetting({ Config.Settings.BedrockLoginBypass },
                { v: Boolean -> Config.Settings.BedrockLoginBypass = v },
                "基岩版玩家登录跳过")
        )
    }

    private fun LoginwiththesameIP(sender: CommandSender, args: Array<String>): Boolean {
        return toggle(sender, args, "LoginwiththesameIP",
            BoolSetting({ Config.Settings.LoginwiththesameIP },
                { v: Boolean -> Config.Settings.LoginwiththesameIP = v },
                "同IP玩家登录跳过")
        )
    }

    // ---- Number Settings ----

    private fun autoKick(sender: CommandSender, args: Array<String>): Boolean {
        if (args.size < 2 || !args[0].equals("setAutoKick", ignoreCase = true)) return false
        try {
            Config.Settings.AutoKick = Integer.parseInt(args[1])
            Config.Settings.save()
            sender.sendMessage(
                if (Config.Settings.AutoKick > 0)
                    "§e已设置未登录自动踢出累计时间为 §a" + Config.Settings.AutoKick + "秒"
                else
                    "§e已关闭未登录自动踢出"
            )
        } catch (e: NumberFormatException) {
            sender.sendMessage("§e秒数必须是一个数字")
        }
        return true
    }

    private fun setReenterInterval(sender: CommandSender, args: Array<String>): Boolean {
        if (args.size < 2 || !args[0].equals("setReenterInterval", ignoreCase = true)) return false
        try {
            Config.Settings.ReenterInterval = java.lang.Long.parseLong(args[1])
            Config.Settings.save()
            sender.sendMessage("§e离开服务器重新进入的间隔限制 " + Config.Settings.ReenterInterval + "tick")
        } catch (e: NumberFormatException) {
            sender.sendMessage("§c请输入一个数字")
        }
        return true
    }

    private fun setIdLength(sender: CommandSender, args: Array<String>): Boolean {
        if (args.size < 3 || !args[0].equals("setIdLength", ignoreCase = true)) return false
        try {
            Config.Settings.MinLengthID = Integer.parseInt(args[1])
            Config.Settings.MaxLengthID = Integer.parseInt(args[2])
            Config.Settings.save()
            sender.sendMessage("§e游戏名最小和最大长度为 " + Config.Settings.MinLengthID + " ~ " + Config.Settings.MaxLengthID)
        } catch (e: NumberFormatException) {
            sender.sendMessage("§c请输入数字")
        }
        return true
    }

    private fun setIpCountLimit(sender: CommandSender, args: Array<String>): Boolean {
        if (args.size < 2 || !args[0].equals("setIpCountLimit", ignoreCase = true)) return false
        try {
            Config.Settings.IpCountLimit = Integer.parseInt(args[1])
            Config.Settings.save()
            sender.sendMessage("§e相同ip登录限制数量为 " + Config.Settings.IpCountLimit)
        } catch (e: NumberFormatException) {
            sender.sendMessage("§c请输入数字")
        }
        return true
    }

    private fun setIpRegCountLimit(sender: CommandSender, args: Array<String>): Boolean {
        if (args.size < 2 || !args[0].equals("setIpRegCountLimit", ignoreCase = true)) return false
        try {
            Config.Settings.IpRegisterCountLimit = Integer.parseInt(args[1])
            Config.Settings.save()
            sender.sendMessage("§e相同ip注册限制数量为 " + Config.Settings.IpRegisterCountLimit)
        } catch (e: NumberFormatException) {
            sender.sendMessage("§c请输入数字")
        }
        return true
    }

    // ---- Command Whitelist ----

    private fun commandWhiteListInfo(sender: CommandSender, args: Array<String>): Boolean {
        if (args.isEmpty() || !args[0].equals("commandWhiteListInfo", ignoreCase = true)) return false
        sender.sendMessage("§e登录前可执行指令: ")
        Config.Settings.CommandWhiteList.forEach { cmdRegex -> sender.sendMessage(cmdRegex.toString()) }
        return true
    }

    private fun commandWhiteListAdd(sender: CommandSender, args: Array<String>): Boolean {
        if (args.size < 2 || !args[0].equals("commandWhiteListAdd", ignoreCase = true)) return false
        val regex = joinArgs(args, 1)
        val pattern = Pattern.compile(regex)
        if (containsRegex(regex)) {
            sender.sendMessage("§c已经存在 $regex")
        } else {
            Config.Settings.CommandWhiteList.add(pattern)
            Config.Settings.save()
            sender.sendMessage("§e已添加登录前可执行指令 $regex")
        }
        return true
    }

    private fun commandWhiteListDel(sender: CommandSender, args: Array<String>): Boolean {
        if (args.size < 2 || !args[0].equals("commandWhiteListDel", ignoreCase = true)) return false
        val regex = joinArgs(args, 1)
        if (containsRegex(regex)) {
            removeRegex(regex)
            Config.Settings.save()
            sender.sendMessage("§e已删除登录前可执行指令 $regex")
        } else {
            sender.sendMessage("§c不存在 $regex")
        }
        return true
    }

    private fun joinArgs(args: Array<String>, from: Int): String {
        val cmd = arrayOfNulls<String>(args.size - from)
        System.arraycopy(args, from, cmd, 0, cmd.size)
        return cmd.filterNotNull().joinToString(" ")
    }

    private fun containsRegex(regex: String): Boolean {
        return Config.Settings.CommandWhiteList.stream()
            .map { obj: Pattern -> obj.toString() }.collect(Collectors.toList()).contains(regex)
    }

    private fun removeRegex(regex: String) {
        Config.Settings.CommandWhiteList.removeIf { p: Pattern -> p.toString() == regex }
    }

    // ---- Spawn Location ----

    private fun setSpawnLocation(sender: CommandSender, args: Array<String>): Boolean {
        if (args.isEmpty() || !args[0].equals("setSpawnLocation", ignoreCase = true)) return false
        if (sender !is Player) {
            sender.sendMessage("§c不能在控制台使用这个指令")
            return true
        }
        Config.Settings.SpawnLocation = sender.location
        Config.Settings.save()
        sender.sendMessage("§e已设置玩家登陆坐标为你站着的位置")
        return true
    }

    // ---- Reload ----

    private fun reload(sender: CommandSender, args: Array<String>): Boolean {
        if (args.isEmpty() || !args[0].equals("reload", ignoreCase = true)) return false
        Config.reload()
        val plugin = CatSeedLogin.instance
        PluginContext.setSql(if (Config.MySQL.Enable && plugin != null) MySQL(plugin) else if (plugin != null) SQLite(plugin) else null)
        try {
            PluginContext.getSql()?.init()
            Cache.refreshAll()
        } catch (e: Exception) {
            PluginContext.getLogger()?.warning("§c加载数据库时出错")
            e.printStackTrace()
        }
        try {
            Communication.socketServerStopAsync()
        } catch (e: Exception) {
            PluginContext.getLogger()?.warning("§c停止通信服务时出错")
            e.printStackTrace()
        }
        if (Config.BungeeCord.Enable) {
            try {
                Communication.socketServerStartAsync()
            } catch (e: Exception) {
                PluginContext.getLogger()?.warning("§c启动通信服务时出错")
                e.printStackTrace()
            }
        }
        sender.sendMessage("配置已重载!")
        return true
    }

    // ---- Delete Player ----

    private fun delPlayer(sender: CommandSender, args: Array<String>): Boolean {
        if (args.size < 2 || !args[0].equals("delplayer", ignoreCase = true)) return false
        val name = args[1]
        val lp: LoginPlayer? = Cache.getIgnoreCase(name)
        if (lp == null) {
            sender.sendMessage(String.format("§c账户 §a%s §c不存在", name))
            return true
        }
        delPlayerAsync(sender, lp)
        return true
    }

    private fun delPlayerAsync(sender: CommandSender, lp: LoginPlayer) {
        CatScheduler.runTaskAsync(Runnable {
            try {
                PluginContext.getSql()?.del(lp.name)
                Cache.refresh(lp.name)
                LoginPlayerHelper.remove(lp)
                sender.sendMessage("§e已删除账户 §a" + lp.name)
                kickPlayerIfOnline(lp.name)
            } catch (e: Exception) {
                sender.sendMessage("§c数据库异常!")
                e.printStackTrace()
            }
        })
    }

    private fun kickPlayerIfOnline(name: String) {
        CatScheduler.runTask(Runnable {
            val p: Player? = Bukkit.getPlayerExact(name)
            if (p != null && p.isOnline) {
                p.kickPlayer("§c你的账户已被删除!")
            }
        })
    }

    // ---- Set Password ----

    private fun setPwd(sender: CommandSender, args: Array<String>): Boolean {
        if (args.size < 3 || !args[0].equals("setpwd", ignoreCase = true)) return false
        val name = args[1]
        val pwd = args[2]
        if (Util.passwordIsDifficulty(pwd)) {
            sender.sendMessage("§c密码必须是6~16位之间的数字和字母组成")
            return true
        }
        sender.sendMessage("§e设置中..")
        CatScheduler.runTaskAsync { setPwdLookup(sender, name, pwd) }
        return true
    }

    private fun setPwdLookup(sender: CommandSender, name: String, pwd: String) {
        val lp: LoginPlayer? = Cache.getIgnoreCase(name)
        if (lp == null) {
            setPwdRegisterNew(sender, name, pwd)
        } else {
            setPwdUpdateExisting(sender, lp, pwd)
        }
    }

    private fun setPwdRegisterNew(sender: CommandSender, name: String, pwd: String) {
        try {
            val lp = LoginPlayer(name, pwd)
            lp.crypt()
            PluginContext.getSql()?.add(lp)
            Cache.refresh(lp.name)
            sender.sendMessage("§a指定账户不存在,现已注册..")
        } catch (e: Exception) {
            sender.sendMessage("§c数据库异常!")
            e.printStackTrace()
        }
    }

    private fun setPwdUpdateExisting(sender: CommandSender, lp: LoginPlayer, pwd: String) {
        try {
            lp.password = pwd
            lp.crypt()
            PluginContext.getSql()?.edit(lp)
            Cache.refresh(lp.name)
            LoginPlayerHelper.remove(lp)
            sender.sendMessage(joinArgs(arrayOf("§a玩家", lp.name, "密码已设置"), 0))
            notifyPlayerPasswordChanged(lp)
        } catch (e: Exception) {
            sender.sendMessage("§c数据库异常!")
            e.printStackTrace()
        }
    }

    private fun notifyPlayerPasswordChanged(lp: LoginPlayer) {
        CatScheduler.runTask(Runnable {
            val p: Player? = Bukkit.getPlayer(lp.name)
            if (p == null || !p.isOnline) return@Runnable
            p.sendMessage("§c密码已被管理员重新设置,请重新登录")
            if (!Config.Settings.CanTpSpawnLocation) return@Runnable
            val spawn = Config.Settings.SpawnLocation
            if (spawn != null) {
                p.teleport(spawn)
            }
            if (PluginContext.isLoadProtocolLib()) {
                LoginPlayerHelper.sendBlankInventoryPacket(p)
            }
        })
    }
}