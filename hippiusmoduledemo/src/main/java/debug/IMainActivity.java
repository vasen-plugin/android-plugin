package debug;

import com.hand.baselibrary.activity.IBaseActivity;

public interface IMainActivity extends IBaseActivity {
    void onMessage(String message);
    void onComplete();
}
