
package com.life.android.models.single_details;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

import com.life.android.models.CustomAddsModel;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SingleDetails implements Parcelable {

    @SerializedName("videos_id")
    @Expose
    private String videosId;
    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("description")
    @Expose
    private String description;
    @SerializedName("slug")
    @Expose
    private String slug;
    @SerializedName("release")
    @Expose
    private String release;
    @SerializedName("runtime")
    @Expose
    private String runtime;
    @SerializedName("video_quality")
    @Expose
    private String videoQuality;
    @SerializedName("is_tvseries")
    @Expose
    private String isTvseries;
    @SerializedName("is_paid")
    @Expose
    private String isPaid;
    @SerializedName("enable_download")
    @Expose
    private String enableDownload;
    @SerializedName("download_links")
    @Expose
    private List<DownloadLink> downloadLinks = null;
    @SerializedName("thumbnail_url")
    @Expose
    private String thumbnailUrl;
    @SerializedName("poster_url")
    @Expose
    private String posterUrl;
    @SerializedName("videos")
    @Expose
    private List<Video> videos = null;
    @SerializedName("genre")
    @Expose
    private List<Genre> genre = null;
    @SerializedName("country")
    @Expose
    private List<Country> country = null;
    @SerializedName("director")
    @Expose
    private List<Director> director = null;
    @SerializedName("writer")
    @Expose
    private List<Writer> writer = null;
    @SerializedName("cast")
    @Expose
    private List<Cast> cast = null;
    @SerializedName("cast_and_crew")
    @Expose
    private List<CastAndCrew> castAndCrew = null;
    @SerializedName("related_movie")
    @Expose
    private List<RelatedMovie> relatedMovie = null;
    @SerializedName("season")
    @Expose
    private List<Season> season = null;
    @SerializedName("related_tvseries")
    @Expose
    private List<RelatedMovie> relatedTvseries = null;
    @SerializedName("trailler_youtube_source")
    @Expose
    private String trailerUrl = null;

    @SerializedName("video_view_type")
    @Expose
    private String video_view_type = null;

    @SerializedName("pre_booking_enabled")
    @Expose
    private String preBookingEnabled = null;

    @SerializedName("is_video_pre_booked_subscription_started")
    @Expose
    private String videoPreBookingSubscriptionStarted = null;

    @SerializedName("is_video_pre_booked")
    @Expose
    private String isVideoPreBooked = null;

    @SerializedName("start_date_time")
    @Expose
    private String startDateTime = null;

    @SerializedName("expiry_date_time")
    @Expose
    private String expiryDateTime = null;

    @SerializedName("how_many_hours_available_for_watch")
    @Expose
    private String hoursAvailable = null;

    @SerializedName("price")
    @Expose
    private String price = null;

    @SerializedName("is_rent_expired")
    @Expose
    private String is_rent_expired = null;

    @SerializedName("remain_hours")
    @Expose
    private String remainHours = null;

    @SerializedName("expiry_on")
    @Expose
    private String expiryOn = null;

    @SerializedName("gst_amount")
    @Expose
    private String gstAmount = null;


    @SerializedName("gst_percentage_in_usd")
    @Expose
    private String gstPercentageinUsd = null;

    @SerializedName("price_in_usd")
    @Expose
    private String priceInUsd = null;

    @SerializedName("gst_amount_in_usd")
    @Expose
    private String gstAmountInUsd = null;


    @SerializedName("final_price")
    @Expose
    private String finalPrice = null;

    @SerializedName("gst_percentage")
    @Expose
    private String gstPercentage = null;

    @SerializedName("pay_watch_description")
    @Expose
    private String payWatchDescription = null;

    @SerializedName("is_subscribed_previously")
    @Expose
    private String is_subscribed_previously = null;

    @SerializedName("show_ads")
    @Expose
    private String showAds = null;
    @SerializedName("trailer_aws_source")
    @Expose
    private String trailer_aws_source = null;

    @SerializedName("ads")
    @Expose
    private List<CustomAddsModel> customAddsModelList = null;

    protected SingleDetails(Parcel in) {
        videosId = in.readString();
        title = in.readString();
        description = in.readString();
        slug = in.readString();
        release = in.readString();
        runtime = in.readString();
        videoQuality = in.readString();
        isTvseries = in.readString();
        isPaid = in.readString();
        enableDownload = in.readString();
        thumbnailUrl = in.readString();
        posterUrl = in.readString();
        trailerUrl = in.readString();
        video_view_type = in.readString();
        preBookingEnabled = in.readString();
        videoPreBookingSubscriptionStarted = in.readString();
        isVideoPreBooked = in.readString();
        startDateTime = in.readString();
        expiryDateTime = in.readString();
        hoursAvailable = in.readString();
        price = in.readString();
        is_rent_expired = in.readString();
        remainHours = in.readString();
        expiryOn = in.readString();
        payWatchDescription = in.readString();
        gstAmount = in.readString();

        gstPercentageinUsd = in.readString();
        priceInUsd = in.readString();
        gstAmountInUsd = in.readString();

        finalPrice = in.readString();
        gstPercentage = in.readString();
        is_subscribed_previously = in.readString();
        showAds = in.readString();
    }

    public static final Creator<SingleDetails> CREATOR = new Creator<SingleDetails>() {
        @Override
        public SingleDetails createFromParcel(Parcel in) {
            return new SingleDetails(in);
        }

        @Override
        public SingleDetails[] newArray(int size) {
            return new SingleDetails[size];
        }
    };

    public String getVideosId() {
        return videosId;
    }

    public void setVideosId(String videosId) {
        this.videosId = videosId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getRelease() {
        return release;
    }

    public void setRelease(String release) {
        this.release = release;
    }

    public String getRuntime() {
        return runtime;
    }

    public void setRuntime(String runtime) {
        this.runtime = runtime;
    }

    public String getVideoQuality() {
        return videoQuality;
    }

    public void setVideoQuality(String videoQuality) {
        this.videoQuality = videoQuality;
    }

    public String getIsTvseries() {
        return isTvseries;
    }

    public void setIsTvseries(String isTvseries) {
        this.isTvseries = isTvseries;
    }

    public String getIsPaid() {
        return isPaid;
    }

    public void setIsPaid(String isPaid) {
        this.isPaid = isPaid;
    }

    public String getEnableDownload() {
        return enableDownload;
    }

    public void setEnableDownload(String enableDownload) {
        this.enableDownload = enableDownload;
    }

    public List<DownloadLink> getDownloadLinks() {
        return downloadLinks;
    }

    public void setDownloadLinks(List<DownloadLink> downloadLinks) {
        this.downloadLinks = downloadLinks;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    public List<Video> getVideos() {
        return videos;
    }

    public void setVideos(List<Video> videos) {
        this.videos = videos;
    }

    public List<Genre> getGenre() {
        return genre;
    }

    public void setGenre(List<Genre> genre) {
        this.genre = genre;
    }

    public List<Country> getCountry() {
        return country;
    }

    public void setCountry(List<Country> country) {
        this.country = country;
    }

    public List<Director> getDirector() {
        return director;
    }

    public void setDirector(List<Director> director) {
        this.director = director;
    }

    public List<Writer> getWriter() {
        return writer;
    }

    public void setWriter(List<Writer> writer) {
        this.writer = writer;
    }

    public List<Cast> getCast() {
        return cast;
    }

    public void setCast(List<Cast> cast) {
        this.cast = cast;
    }

    public List<CastAndCrew> getCastAndCrew() {
        return castAndCrew;
    }

    public void setCastAndCrew(List<CastAndCrew> castAndCrew) {
        this.castAndCrew = castAndCrew;
    }

    public List<RelatedMovie> getRelatedMovie() {
        return relatedMovie;
    }

    public void setRelatedMovie(List<RelatedMovie> relatedMovie) {
        this.relatedMovie = relatedMovie;
    }

    public List<Season> getSeason() {
        return season;
    }

    public void setSeason(List<Season> season) {
        this.season = season;
    }

    public List<RelatedMovie> getRelatedTvseries() {
        return relatedTvseries;
    }

    public void setRelatedTvseries(List<RelatedMovie> relatedTvseries) {
        this.relatedTvseries = relatedTvseries;
    }

    public String getTrailerUrl() {
        return trailerUrl;
    }

    public void setTrailerUrl(String trailerUrl) {
        this.trailerUrl = trailerUrl;
    }

    public String getVideo_view_type() {
        return video_view_type;
    }

    public void setVideo_view_type(String video_view_type) {
        this.video_view_type = video_view_type;
    }

    public String getPreBookingEnabled() {
        return preBookingEnabled;
    }

    public void setPreBookingEnabled(String preBookingEnabled) {
        this.preBookingEnabled = preBookingEnabled;
    }

    public String getVideoPreBookingSubscriptionStarted() {
        return videoPreBookingSubscriptionStarted;
    }

    public void setVideoPreBookingSubscriptionStarted(String videoPreBookingSubscriptionStarted) {
        this.videoPreBookingSubscriptionStarted = videoPreBookingSubscriptionStarted;
    }

    public String getIsVideoPreBooked() {
        return isVideoPreBooked;
    }

    public void setIsVideoPreBooked(String isVideoPreBooked) {
        this.isVideoPreBooked = isVideoPreBooked;
    }

    public String getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(String startDateTime) {
        this.startDateTime = startDateTime;
    }

    public String getExpiryDateTime() {
        return expiryDateTime;
    }

    public void setExpiryDateTime(String expiryDateTime) {
        this.expiryDateTime = expiryDateTime;
    }

    public String getHoursAvailable() {
        return hoursAvailable;
    }

    public void setHoursAvailable(String hoursAvailable) {
        this.hoursAvailable = hoursAvailable;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getIs_rent_expired() {
        return is_rent_expired;
    }

    public void setIs_rent_expired(String is_rent_expired) {
        this.is_rent_expired = is_rent_expired;
    }

    public String getRemainHours() {
        return remainHours;
    }

    public void setRemainHours(String remainHours) {
        this.remainHours = remainHours;
    }

    public String getExpiryOn() {
        return expiryOn;
    }

    public void setExpiryOn(String expiryOn) {
        this.expiryOn = expiryOn;
    }

    public String getPayWatchDescription() {
        return payWatchDescription;
    }

    public void setPayWatchDescription(String payWatchDescription) {
        this.payWatchDescription = payWatchDescription;
    }

    public String getGstAmount() {
        return gstAmount;
    }

    public void setGstAmount(String gstAmount) {
        this.gstAmount = gstAmount;
    }

    public String getGstPercentageinUsd() {
        return gstPercentageinUsd;
    }

    public void setGstPercentageinUsd(String gstPercentageinUsd) {
        this.gstPercentageinUsd = gstPercentageinUsd;
    }

    public String getPriceInUsd() {
        return priceInUsd;
    }

    public void setPriceInUsd(String priceInUsd) {
        this.priceInUsd = priceInUsd;
    }

    public String getGstAmountInUsd() {
        return gstAmountInUsd;
    }

    public void setGstAmountInUsd(String gstAmountInUsd) {
        this.gstAmountInUsd = gstAmountInUsd;
    }

    public String getFinalPrice() {
        return finalPrice;
    }

    public void setFinalPrice(String finalPrice) {
        this.finalPrice = finalPrice;
    }

    public String getGstPercentage() {
        return gstPercentage;
    }

    public void setGstPercentage(String gstPercentage) {
        this.gstPercentage = gstPercentage;
    }

    public String getIs_subscribed_previously() {
        return is_subscribed_previously;
    }

    public void setIs_subscribed_previously(String is_subscribed_previously) {
        this.is_subscribed_previously = is_subscribed_previously;
    }

    public boolean showAds() {
        return showAds != null && showAds.equals("1");
    }

    public String getTrailer_aws_source() {
        return trailer_aws_source;
    }

    public void setTrailer_aws_source(String trailer_aws_source) {
        this.trailer_aws_source = trailer_aws_source;
    }

    public List<CustomAddsModel> getCustomAddsModelList() {
        return customAddsModelList;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(videosId);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(slug);
        dest.writeString(release);
        dest.writeString(runtime);
        dest.writeString(videoQuality);
        dest.writeString(isTvseries);
        dest.writeString(isPaid);
        dest.writeString(enableDownload);
        dest.writeString(thumbnailUrl);
        dest.writeString(posterUrl);
        dest.writeString(trailerUrl);
        dest.writeString(video_view_type);
        dest.writeString(preBookingEnabled);
        dest.writeString(videoPreBookingSubscriptionStarted);
        dest.writeString(isVideoPreBooked);
        dest.writeString(startDateTime);
        dest.writeString(expiryDateTime);
        dest.writeString(hoursAvailable);
        dest.writeString(price);
        dest.writeString(is_rent_expired);
        dest.writeString(remainHours);
        dest.writeString(expiryOn);
        dest.writeString(payWatchDescription);
        dest.writeString(gstAmount);

        dest.writeString(gstPercentageinUsd);
        dest.writeString(priceInUsd);
        dest.writeString(gstAmountInUsd);

        dest.writeString(finalPrice);
        dest.writeString(gstPercentage);
        dest.writeString(is_subscribed_previously);
        dest.writeString(showAds);
        dest.writeString(trailer_aws_source);
    }
}
