// Generated code from Butter Knife. Do not modify!
package com.hand.jlhmprinter;

import android.view.View;
import android.widget.TextView;
import androidx.annotation.CallSuper;
import androidx.annotation.UiThread;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import java.lang.IllegalStateException;
import java.lang.Override;

public class BTListActivity_ViewBinding implements Unbinder {
  private BTListActivity target;

  @UiThread
  public BTListActivity_ViewBinding(BTListActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public BTListActivity_ViewBinding(BTListActivity target, View source) {
    this.target = target;

    target.rcvDevices = Utils.findRequiredViewAsType(source, R.id.rcv_device, "field 'rcvDevices'", RecyclerView.class);
    target.swipeRefreshLayout = Utils.findRequiredViewAsType(source, R.id.swipe_refresh, "field 'swipeRefreshLayout'", SwipeRefreshLayout.class);
    target.tvSearchTip = Utils.findRequiredViewAsType(source, R.id.tv_search_tip, "field 'tvSearchTip'", TextView.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    BTListActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.rcvDevices = null;
    target.swipeRefreshLayout = null;
    target.tvSearchTip = null;
  }
}
