package edu.niu.android.globally;

public class Post
{
    public String imageUrl, city, country, caption, lat, lng;

    public Post() {}

    public Post(String imageUrl, String city, String country, String caption, String lat, String lng)
    {
        this.imageUrl = imageUrl;
        this.city = city;
        this.country = country;
        this.caption = caption;
        this.lat = lat;
        this.lng = lng;
    }
}
