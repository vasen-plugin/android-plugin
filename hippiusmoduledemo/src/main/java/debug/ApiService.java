package debug;

import com.hand.baselibrary.BuildConfig;
import com.hand.baselibrary.bean.AccessToken;
import com.hand.baselibrary.bean.TenantUserInfo;
import com.hand.baselibrary.bean.UserInfo;

import java.util.ArrayList;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    //获取access_token的接口
    @POST("oauth/oauth/token?client_id=" + BuildConfig.clientId + "&client_secret="
            + BuildConfig.clientSecret + "&source_type=APP&grant_type=password")
    Observable<AccessToken> getAccessToken(@Query("username") String username, @Query("password")
            String password, @Query("device_id") String ime);
    //获取当前登录用户的个人信息
    @GET("iam/hzero/v1/users/self")
    Observable<UserInfo> getUserInfo();

    @GET("iam/hzero/v1/users/{userId}/info")
    Observable<UserInfo> getUserInfo(@Path("userId") String userId);

    @GET("hipspfm/hippius/v1/contact/detail")
    Observable<ArrayList<TenantUserInfo>> getTenantUserInfos(@Query("employeeId") String employeeId,
                                                             @Query("organizationId") String organizationId);

}
