package com.qhr.vo;


import lombok.Data;

import java.util.List;

/**
 * 匹配记录实体。
 */
@Data
public class MatchRecords {
    private Long id;
    private String finAmountRange;/*期望融资金额*/
    private String matchAmountRange;/*可融资金额*/
    private List<Integer> productIds;/*可贷款产品Id数组*/
    private String productCount;/*可贷款产品Id数量*/
    //    private String status;/*状态，是否归档*/
    private String matchTime;/*创建时间*/

}
