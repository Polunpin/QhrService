package com.tencent.config;

import com.tencent.model.CreditProduct;
import com.tencent.model.CustomServiceOrder;
import com.tencent.model.Enterprise;
import com.tencent.model.FinancingIntention;
import com.tencent.model.MatchRecord;
import com.tencent.model.ProductRedirectConfig;
import com.tencent.model.Staff;
import com.tencent.model.StatusLog;
import com.tencent.model.User;
import com.tencent.model.UserEnterpriseRelation;
import com.tencent.vo.CreditProductStats;
import com.tencent.vo.CreditProducts;
import com.tencent.vo.MatchRecords;
import com.tencent.vo.Staffs;
import com.tencent.vo.Users;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection(targets = {
    ApiResponse.class,
    PageResult.class,
    CreditProduct.class,
    CustomServiceOrder.class,
    Enterprise.class,
    FinancingIntention.class,
    MatchRecord.class,
    ProductRedirectConfig.class,
    Staff.class,
    StatusLog.class,
    User.class,
    UserEnterpriseRelation.class,
    CreditProductStats.class,
    CreditProducts.class,
    MatchRecords.class,
    MatchRecords.Product.class,
    Staffs.class,
    Users.class
})
public final class NativeReflectionConfig {

  private NativeReflectionConfig() {
  }
}
