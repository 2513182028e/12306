package com.java.train.member.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.java.train.member.entity.Member;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MemberMapper extends BaseMapper<Member>   {


    int count();


    }