package cc.baka9.catseedlogin

import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

class CommandLogin : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: org.bukkit.command.Command, label: String, args: Array<String>): Boolean {
        if (sender !is org.bukkit.entity.Player) {
            sender.sendMessage("只有玩家才能使用此指令")
            return true
        }
        
        if (args.isEmpty()) {
            sender.sendMessage("请输入密码: /login <密码>")
            return true
        }
        
        val password = args.joinToString(" ")
        val player = sender
        val name = player.name
        
        if (LoginPlayerHelper.isLogin(name)) {
            sender.sendMessage(Config.Language.ALREADY_LOGIN)
            return true
        }
        
        val lp = Database.Cache.get(name)
        if (lp == null) {
            sender.sendMessage(Config.Language.NEED_REGISTER)
            return true
        }
        
        if (Crypt.match(name, password, lp.password)) {
            LoginPlayerHelper.addLoggedPlayer(name)
            sender.sendMessage(Config.Language.LOGIN_SUCCESS)
            
            if (Config.Settings.afterLoginBack && Config.Settings.canTpSpawnLocation) {
                Config.getOfflineLocation(player).ifPresent { location ->
                    CatScheduler.runTaskLater({ CatScheduler.teleport(player, location) }, 1L)
                }
            }
        } else {
            sender.sendMessage(Config.Language.LOGIN_FAILED)
        }
        
        return true
    }
}

class CommandRegister : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: org.bukkit.command.Command, label: String, args: Array<String>): Boolean {
        if (sender !is org.bukkit.entity.Player) {
            sender.sendMessage("只有玩家才能使用此指令")
            return true
        }
        
        if (args.size < 2) {
            sender.sendMessage("请输入密码: /register <密码> <重复密码>")
            return true
        }
        
        val password = args[0]
        val confirmPassword = args[1]
        
        if (password != confirmPassword) {
            sender.sendMessage(Config.Language.PASSWORD_MISMATCH)
            return true
        }
        
        val player = sender
        val name = player.name
        
        if (LoginPlayerHelper.isLogin(name)) {
            sender.sendMessage(Config.Language.ALREADY_LOGIN)
            return true
        }
        
        val existingLp = Database.Cache.get(name)
        if (existingLp != null) {
            sender.sendMessage(Config.Language.ALREADY_REGISTER)
            return true
        }
        
        val lp = LoginPlayer(name, password)
        lp.crypt()
        try {
            Database.sql.add(lp)
            LoginPlayerHelper.addLoggedPlayer(name)
            sender.sendMessage(Config.Language.REGISTER_SUCCESS)
            
            if (Config.Settings.canTpSpawnLocation) {
                CatScheduler.runTaskLater({ 
                    Config.Settings.spawnLocation?.let { CatScheduler.teleport(player, it) }
                }, 1L)
            }
        } catch (e: Exception) {
            sender.sendMessage("注册失败: ${e.message}")
            e.printStackTrace()
        }
        
        return true
    }
}

class CommandChangePassword : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: org.bukkit.command.Command, label: String, args: Array<String>): Boolean {
        if (sender !is org.bukkit.entity.Player) {
            sender.sendMessage("只有玩家才能使用此指令")
            return true
        }
        
        if (args.size < 3) {
            sender.sendMessage("请输入: /changepassword <旧密码> <新密码> <重复新密码>")
            return true
        }
        
        val oldPassword = args[0]
        val newPassword = args[1]
        val confirmPassword = args[2]
        
        if (newPassword != confirmPassword) {
            sender.sendMessage(Config.Language.PASSWORD_MISMATCH)
            return true
        }
        
        val player = sender
        val name = player.name
        
        val lp = Database.Cache.get(name)
        if (lp == null) {
            sender.sendMessage(Config.Language.NEED_REGISTER)
            return true
        }
        
        if (!Crypt.match(name, oldPassword, lp.password)) {
            sender.sendMessage("旧密码错误!")
            return true
        }
        
        lp.password = Crypt.encrypt(name, newPassword) ?: newPassword
        try {
            Database.sql.edit(lp)
            sender.sendMessage("密码修改成功!")
        } catch (e: Exception) {
            sender.sendMessage("密码修改失败: ${e.message}")
        }
        
        return true
    }
}

class CommandCatSeedLogin : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: org.bukkit.command.Command, label: String, args: Array<String>): Boolean {
        if (!sender.hasPermission("catseedlogin.command.catseedlogin")) {
            sender.sendMessage(Config.Language.NO_PERMISSION)
            return true
        }
        
        if (args.isEmpty()) {
            sender.sendMessage("""
                §d§lCatSeedLogin 管理指令:
                §a/<command> commandWhiteListAdd [指令] §9添加登陆之前允许执行的指令
                §a/<command> commandWhiteListDel [指令] §9删除登陆之前允许执行的指令
                §a/<command> commandWhiteListInfo §9查看登陆之前允许执行的指令
                §b/<command> setIpRegCountLimit [数量] §9设置相同ip注册限制
                §b/<command> setIpCountLimit [数量] §9设置相同ip登录限制
                §b/<command> setIdLength [最短] [最长] §9设置游戏名长度
                §b/<command> setSpawnLocation §9设置玩家登陆地点
                §c/<command> limitChineseID §9打开/关闭限制中文游戏名
                §c/<command> bedrockLoginBypass §9打开/关闭基岩版登录跳过
                §e/<command> reload §9重载配置文件
            """.trimIndent().replace("<command>", label))
            return true
        }
        
        return true
    }
}