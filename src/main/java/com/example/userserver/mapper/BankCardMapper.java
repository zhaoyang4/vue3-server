package com.example.userserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.userserver.entity.BankCard;
import org.apache.ibatis.annotations.Mapper;

/**
 * 银行卡数据访问层（基础 CRUD 由 BaseMapper 提供，逻辑删除由 @TableLogic 自动处理）。
 */
@Mapper
public interface BankCardMapper extends BaseMapper<BankCard> {
}
