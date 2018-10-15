Current:
---
1. mongo数据库存储脚本
2. 通过ts时间参数决定程序是否刷新当前脚本
3. maven跳过groovy/commands/目录，不能编译到最后到包中，
    inteliJ则将目录exclude即可，在工程设置中调整。
4. 工具将当前目录下的脚本文件刷新到数据库
5. 工具提供一个修改脚本时间的工具


Future Plan:
---
1. 包含一个可回退的功能
2. 刷新工具需要可选择具体刷新的脚本


3. 使用ide开发调试，就要避免脚本，此时需要关闭ide中的exclude,同时取消inject application-bean.xml.