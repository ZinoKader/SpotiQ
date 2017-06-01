package se.zinokader.spotiq.util.helper;

import com.bumptech.glide.request.RequestOptions;

import se.zinokader.spotiq.R;

public class GlideRequestOptions {

    private GlideRequestOptions() {}

    public static RequestOptions getProfileImageOptions() {
        return new RequestOptions()
                .placeholder(R.drawable.image_profile_placeholder)
                .dontAnimate()
                .dontTransform();
    }
}
