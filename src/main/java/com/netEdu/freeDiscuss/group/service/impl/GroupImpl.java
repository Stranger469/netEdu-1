package com.netEdu.freeDiscuss.group.service.impl;

import com.netEdu.freeDiscuss.group.dao.GroupMapper;
import com.netEdu.entity.Group;
import com.netEdu.freeDiscuss.group.service.GroupService;
import com.netEdu.utils.netty.Connection;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GroupImpl implements GroupService{

    @Autowired
    private GroupMapper groupMapper;

    @Override
    public String newGroup(Group group) {
        //向数据库中插入新组
        groupMapper.insertGroup(group);
        String ids=group.getPerson_id();//组员id
        String[] id=ids.split(",");//split
        Channel ch=null;
        //创建一个新的ChannelGroup讨论组实体
        ChannelGroup channelGroup=new  DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
        //将channel装进去
        for(String i:id){
            ch=Connection.AllConnections.get(i);
            channelGroup.add(ch);
        }
        //存入map来维护
        Connection.chatGroup.put(group.getGroup_id() + "", channelGroup);
        //返回新创建组的id
        return group.getGroup_id()+"";
    }

    @Override
    public void delGroup(Group group) {
        //删除数据库中数据
       groupMapper.deleteGroup(group);
       //从Map中移除组
        if (Connection.chatGroup.containsKey(group.getGroup_id()+"")) {
            //ChannelGroup cg=Connection.chatGroup.get(group.getGroup_id()+"");
            Connection.chatGroup.remove(group.getGroup_id()+"");
        }
    }

    @Override
    public void editGroupMember(Group group) {
        String newIds=group.getPerson_id();//新添加/删除的组员id
        String[] newId=newIds.split(",");//split之后的
        String finalIds="";//最终结果id
        Channel ch=null;
        boolean flag=true;//判断是添加还是删除true添加 false删除
        List<Group> result=groupMapper.getMember(group);//查询老组员情况
        String oldIds=result.get(0).getPerson_id();//老组员ID
        String[] oldId=oldIds.split(",");//split之后的

        //判断是添加还是删除
        for (String oldid:oldId){
            if (newId[0].equals(oldid)){//若有一样的必为删除
                flag=false;
                break;
            }
        }

        if (flag){
            //增加组员
            finalIds+=oldIds+","+newIds;
            //更新到对象中
            group.setPerson_id(finalIds);
            //更新至数据库
            groupMapper.updateMember(group);
            //更新到实体分组中
            ChannelGroup channelGroup=Connection.chatGroup.get(group.getGroup_id()+"");
            for (String id:newId){
                ch=Connection.AllConnections.get(id);
                channelGroup.add(ch);
            }
            Connection.chatGroup.put(group.getGroup_id()+"",channelGroup);

        }else{
            //移除组员

            //加个逗号
            oldIds+=",";
            //更新组员
            for (String del:newId){
                finalIds=oldIds.replaceFirst(del+",","");
            }
            //把那逗号再去了
            finalIds=finalIds.substring(0,finalIds.length()-1);
            //向对象更新
            group.setPerson_id(finalIds);
            //向数据库更新
            groupMapper.updateMember(group);
            //向实体分组更新
            ChannelGroup channelGroup=Connection.chatGroup.get(group.getGroup_id()+"");
            for (String id:newId){
                channelGroup.remove(Connection.AllConnections.get(id));
            }
            Connection.chatGroup.put(group.getGroup_id()+"",channelGroup);
        }

    }
}