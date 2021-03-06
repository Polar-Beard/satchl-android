package com.satchlapp.model;

/**
 * Created by Sara on 10/20/2016.
 */
import java.util.UUID;

public class User {


    private UUID userId;
    private String emailAddress;
    private String password;
    private String firstName;
    private String lastName;
    private String profilePictureUrl;

    public User(){
        this(null,null);
    }

    public User(String emailAddress, String password){
        this.emailAddress = emailAddress;
        this.password = password;
    }

    public void setEmailAddress(String emailAddress){
        this.emailAddress = emailAddress;
    }

    public void setPassword(String password){
        this.password = password;
    }

    public void setUserId(UUID userId){
        this.userId = userId;
    }

    public void setFirstName(String firstName){
        this.firstName = firstName;
    }

    public void setLastName(String lastName){
        this.lastName = lastName;
    }

    public String getEmailAddress(){
        return emailAddress;
    }

    public String getPassword(){
        return password;
    }

    public UUID getUserId(){
        return userId;
    }

    public String getFirstName(){
        return firstName;
    }

    public String getLastName(){
        return lastName;
    }

    public String getProfilePictureUrl(){
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl){
        this.profilePictureUrl = profilePictureUrl;
    }

}
