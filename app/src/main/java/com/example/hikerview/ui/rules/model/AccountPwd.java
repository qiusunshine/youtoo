package com.example.hikerview.ui.rules.model;

/**
 * 作者：By 15968
 * 日期：On 2021/1/2
 * 时间：At 22:18
 */

public class AccountPwd {

    private String account;
    private String password;

    public AccountPwd(String account, String password) {
        this.account = account;
        this.password = password;
    }

    public AccountPwd() {
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }
}
