# 用户 User
## 类内部信息
权限 Permission
- User
- Admin
头像 Avatar
- change_avatar
- original_avatar
昵称 Nickname
- change_nickname
邮箱 Email
- change_email
密码 Password
- change_password
角色卡 RoleCard
- RoleCard_management
## 方法
注册 register
登录 login
退出 logout
创建房间 create_room
加入房间 join_room
离开房间 leave_room

掷骰 throw_dice（通过房间）
聊天 send_message（通过房间）
查看历史记录 view_history（通过房间）

**下述为管理员方法**
查看邀请码 view_invite_codes
- 删除邀请码 delete_invite_code
- 创建邀请码 create_invite_code
- 更新邀请码 update_invite_code
管理用户 manage_users
- 查看用户列表 view_user_list
- 查看用户详情 view_user_details

- 删除用户 delete_user
- 禁止用户 forbid_user
- 恢复用户 recover_user

# 房间
## 类内部信息
房间信息 RoomInfo
- 房间ID room_public_id
- 房间私有ID room_private_id （0 or other）
- 房间名称 room_name
- 房间描述 room_description
- 房间状态 room_status
- 房主ID owner_id
- 房间成员列表 members
- 房间创建时间 created_at

房间身份
- 房主 owner
- 主持人 GM
- 玩家 player
- 旁观者 spectator
- Bot bot
- Add_on_Bot add_on_bot
## 方法
创建房间 create_room（与用户接口）
删除房间 delete_room（与用户接口）
登记用户 register_user（与用户接口）
删除用户 delete_user（与用户接口）
发送消息 send_message （与消息、用户接口）
查看房间成员 view_members（与用户接口）
查看房间信息 view_room_info
查看历史记录 view_history（与消息接口）
掷骰 throw_dice（与用户、掷骰接口）
系统消息 send_system_message（与消息接口）
# 角色卡
## 类内部信息
角色卡信息 RoleCardInfo
- 角色卡ID role_card_id
- 角色卡名称 role_card_name
- 角色卡描述 role_card_description
- 角色卡属性 role_card_attributes
- 角色卡技能 role_card_skills
- 角色卡所属用户 user_id
## 方法
创建角色卡 create_role_card
删除角色卡 delete_role_card
修改角色卡 modify_role_card
查看角色卡 view_role_card
导入角色卡 import_role_card （房间内）
导出角色卡 export_role_card （房间外）
拷贝角色卡 copy_role_card （房间内）
# 掷骰
可以略，这些接口写好就行
# 邀请码