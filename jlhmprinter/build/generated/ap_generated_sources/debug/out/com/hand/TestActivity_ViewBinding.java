// Generated code from Butter Knife. Do not modify!
package com.hand;

import android.view.View;
import android.widget.ImageView;
import androidx.annotation.CallSuper;
import androidx.annotation.UiThread;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import com.hand.jlhmprinter.R;
import java.lang.IllegalStateException;
import java.lang.Override;

public class TestActivity_ViewBinding implements Unbinder {
  private TestActivity target;

  @UiThread
  public TestActivity_ViewBinding(TestActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public TestActivity_ViewBinding(TestActivity target, View source) {
    this.target = target;

    target.ivImage = Utils.findRequiredViewAsType(source, R.id.iv_image, "field 'ivImage'", ImageView.class);
    target.ivImage2 = Utils.findRequiredViewAsType(source, R.id.iv_image2, "field 'ivImage2'", ImageView.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    TestActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.ivImage = null;
    target.ivImage2 = null;
  }
}
