package cn.fuyoushuo.fqbb.commonlib.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.alibaba.fastjson.JSONObject;

/**
 * 用于单例保存用户信息
 * Created by QA on 2016/10/31.
 */
public class LoginInfoStore {


    private static class LoginInfoStoreHolder{
         private static LoginInfoStore INTANCE = new LoginInfoStore();
    }

    public static LoginInfoStore getIntance(){
         return LoginInfoStoreHolder.INTANCE;
    }

    public void init(Context context){
        this.context = context;
        sharedPreferences = context.getSharedPreferences(share_userinfo_key,Context.MODE_PRIVATE);
    }

    private Context context;

    private SharedPreferences sharedPreferences;

    private String share_userinfo_key = "user_info";

    public void writeUserInfo(UserInfoStore userInfoStore){
         if(userInfoStore == null) return;
         SharedPreferences.Editor edit = sharedPreferences.edit();
         if(!TextUtils.isEmpty(userInfoStore.getSessionId())){
             edit.putString("sessionId",userInfoStore.getSessionId());
         }
         if(!TextUtils.isEmpty(userInfoStore.getToken())){
             edit.putString("token",userInfoStore.getToken());
         }
         if(!TextUtils.isEmpty(userInfoStore.getUserId())){
             edit.putString("userId",userInfoStore.getUserId());
        }
        if(!TextUtils.isEmpty(userInfoStore.getAliUserName())){
             edit.putString("aliUserName",userInfoStore.getAliUserName());
        }
        if(!TextUtils.isEmpty(userInfoStore.getAliPassword())){
             edit.putString("aliPassword",userInfoStore.getAliPassword());
        }
        //是否记住阿里账号信息
        edit.putBoolean("isRemAliInfo",userInfoStore.isRemAliInfo());
        edit.commit();
    }

    //写入用户登录的账号
    public void writeUserAccount(String account){
       if(TextUtils.isEmpty(account)) return;
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putString("login_account",account);
        edit.commit();
    }

    //获取我们自己的用户信息
    public UserInfoStore getUserInfoStore(){
        String sessionId = sharedPreferences.getString("sessionId","");
        String token = sharedPreferences.getString("token","");
        String userId = sharedPreferences.getString("userId","");
        if(TextUtils.isEmpty(sessionId) || TextUtils.isEmpty(token) || TextUtils.isEmpty(userId)){
            return null;
        }
        UserInfoStore userInfoStore = new UserInfoStore();
        userInfoStore.setSessionId(sessionId);
        userInfoStore.setToken(token);
        userInfoStore.setUserId(userId);
        return userInfoStore;
    }

    //获取的阿里登录信息
    public UserInfoStore getAliInfoStore(){
        String userName = sharedPreferences.getString("aliUserName","");
        String passWord = sharedPreferences.getString("aliPassword","");
        boolean isRemAliInfo = sharedPreferences.getBoolean("isRemAliInfo",false);
        UserInfoStore userInfoStore = new UserInfoStore();
        userInfoStore.setAliUserName(userName);
        userInfoStore.setAliPassword(passWord);
        userInfoStore.setRemAliInfo(isRemAliInfo);
        return userInfoStore;
    }

    /**
     * 获取用户账号
     * @return
     */
    public String getUserAccount(){
        String login_account = sharedPreferences.getString("login_account", "");
        return login_account;
    }

    public void clearUserInfo(){
        SharedPreferences.Editor edit = sharedPreferences.edit();
        if(sharedPreferences.contains("sessionId")){
            edit.remove("sessionId");
        }
        if(sharedPreferences.contains("token")){
            edit.remove("token");
        }
        if(sharedPreferences.contains("userId")){
            edit.remove("userId");
        }
        if(sharedPreferences.contains("login_account")){
            edit.remove("login_account");
        }
        edit.commit();
    }

    public String getAliInfoJson(){
        JSONObject result = new JSONObject();
        result.put("username",sharedPreferences.getString("aliUserName",""));
        result.put("password",sharedPreferences.getString("aliPassword",""));
        result.put("rempwd",sharedPreferences.getBoolean("isRemAliInfo",false));
        return result.toJSONString();
    }

    public void cleanAliInfo(){
        SharedPreferences.Editor edit = sharedPreferences.edit();
        if(sharedPreferences.contains("aliUserName")){
            edit.remove("aliUserName");
        }
        if(sharedPreferences.contains("aliPassword")){
            edit.remove("aliPassword");
        }
        if(sharedPreferences.contains("isRemAliInfo")){
            edit.remove("isRemAliInfo");
        }
        edit.commit();
    }
}
