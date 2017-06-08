package se.zinokader.spotiq.util.image;

public class GlideRequestOptions {

    private GlideRequestOptions() {}

    //TODO: Reimplement this when Glide v4 works with transformations again
    /*
    public static RequestOptions getProfileImageOptions() {
        return new RequestOptions()
                .placeholder(R.drawable.image_profile_placeholder)
                .dontAnimate()
                .dontTransform();
    }

    public static RequestOptions getBlurredViewBackgroundImageOptions(Context context) {
        MultiTransformation<Bitmap> multiTransformation = new MultiTransformation<>(Arrays.asList(

                        new BlurTransformation(context, 25, 4)));
        return new RequestOptions()
                .transform(multiTransformation);
    }
    */

}
