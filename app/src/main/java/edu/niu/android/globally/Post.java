package edu.niu.android.globally;

public class Post {
    public String imageUrl;
    public String city;
    public String country;
    public String caption;

    // Required empty constructor for Firebase
    public Post() {}

    public Post(String imageUrl, String city, String country, String caption) {
        this.imageUrl = imageUrl;
        this.city = city;
        this.country = country;
        this.caption = caption;
    }
}
