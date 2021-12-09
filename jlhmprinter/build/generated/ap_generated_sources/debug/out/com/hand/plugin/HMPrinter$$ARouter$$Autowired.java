package com.hand.plugin;

import com.alibaba.android.arouter.facade.service.SerializationService;
import com.alibaba.android.arouter.facade.template.ISyringe;
import com.alibaba.android.arouter.launcher.ARouter;
import com.hand.baselibrary.communication.IQRCodeProvider;
import java.lang.Object;
import java.lang.Override;

/**
 * DO NOT EDIT THIS FILE!!! IT WAS GENERATED BY AROUTER. */
public class HMPrinter$$ARouter$$Autowired implements ISyringe {
  private SerializationService serializationService;

  @Override
  public void inject(Object target) {
    serializationService = ARouter.getInstance().navigation(SerializationService.class);
    HMPrinter substitute = (HMPrinter)target;
    substitute.mQRCodeProvider = ARouter.getInstance().navigation(IQRCodeProvider.class);
  }
}
