package cn.fuyoushuo.fqbb.commonlib.utils;

import java.io.Serializable;

public class UserInfoStore implements Serializable {

        private String sessionId;

        private String token;

        private String userId;

        private String aliUserName;

        private String aliPassword;

        private boolean isRemAliInfo = false;

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getSessionId() {
            return sessionId;
        }

        public void setSessionId(String sessionId) {
            this.sessionId = sessionId;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getAliPassword() {
            return aliPassword;
        }

        public void setAliPassword(String aliPassword) {
            this.aliPassword = aliPassword;
        }

        public String getAliUserName() {
            return aliUserName;
        }

        public void setAliUserName(String aliUserName) {
            this.aliUserName = aliUserName;
        }

        public boolean isRemAliInfo() {
            return isRemAliInfo;
        }

        public void setRemAliInfo(boolean remAliInfo) {
            isRemAliInfo = remAliInfo;
        }
}