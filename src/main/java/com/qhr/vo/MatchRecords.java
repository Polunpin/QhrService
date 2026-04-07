package com.qhr.vo;


import lombok.Data;

/**
 * 匹配记录实体。
 */
@Data
public class MatchRecords {
    private Long id;
    private String finAmountRange;/*期望融资金额*/
    private String matchAmountRange;/*可融资金额*/
    private String productCount;/*可贷款产品Id*/
    //    private String status;/*状态，是否归档*/
    private String matchTime;/*创建时间*/

}
