package com.example.userserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.userserver.entity.AccountFlow;
import org.apache.ibatis.annotations.Mapper;

/**
 * 资金流水数据访问层（基础 CRUD 由 BaseMapper 提供，无需自定义 SQL）。
 */
@Mapper
public interface AccountFlowMapper extends BaseMapper<AccountFlow> {
}
