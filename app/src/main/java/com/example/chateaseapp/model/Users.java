package com.example.chateaseapp.model;

public class Users {
    String profilePic, userName, mail, password, userId,about, token;

    public Users() {
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Users(String profilePic, String userId, String userName, String mail) {
        this.profilePic = profilePic;    //constructor1 //used in SetProfileActivity
        this.userName = userName;
        this.mail = mail;
        this.userId = userId;
    }

    public Users(String profilePic, String userId,String userName, String mail, String about) {
        this.profilePic = profilePic;
        this.userName = userName;
        this.mail = mail;
        this.userId = userId;
        this.about = about;
    }

    public Users(String userName, String mail, String password) {
        this.userName = userName;    //constructor2
        this.mail = mail;
        this.password = password;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }
}