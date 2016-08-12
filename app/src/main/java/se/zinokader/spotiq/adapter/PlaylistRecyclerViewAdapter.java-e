package se.zinokader.spotiq.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.varunest.sparkbutton.SparkButton;
import com.varunest.sparkbutton.SparkEventListener;

import java.util.ArrayList;
import java.util.ListIterator;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.BlurTransformation;
import jp.wasabeef.glide.transformations.ColorFilterTransformation;
import jp.wasabeef.glide.transformations.CropTransformation;
import se.zinokader.spotiq.R;
import se.zinokader.spotiq.activity.PartyActivity;
import se.zinokader.spotiq.model.Song;
import se.zinokader.spotiq.util.ImageUtils;

import static se.zinokader.spotiq.constants.Constants.FIRST_ITEM;
import static se.zinokader.spotiq.constants.Constants.NORMAL_ITEM;

public class PlaylistRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private LayoutInflater inflater;
    private ArrayList<Song> songs;
    private Context context;
    private PartyActivity activity;


    public PlaylistRecyclerViewAdapter(Activity activity, Context context, ArrayList<Song> songs) {
        this.activity = (PartyActivity) activity;
        this.inflater = LayoutInflater.from(context);
        this.context = context;
        this.songs = songs;
    }

    public void addSong(Song song) {
        songs.add(song);
        notifyItemInserted(songs.indexOf(song));
        notifyItemRangeChanged(songs.indexOf(song), getItemCount());
        Log.d("NotifyInserted", "Notifying inserted song at position " + songs.indexOf(song));
        Log.d("SONG ADDED RW", song.getSongName());
    }

    public void removeSong(Song song) {
        ListIterator<Song> songiterator = songs.listIterator();
        while(songiterator.hasNext()) {
            Song nextsong = songiterator.next();
            int songindex = songiterator.nextIndex() - 1; //next() börjar från 1, arraylist börjar från 0
            if(nextsong.getArtist().contentEquals(song.getArtist()) && nextsong.getSongName().contentEquals(song.getSongName())) {
                songs.remove(songindex);
                notifyItemRemoved(songindex);
                notifyItemRangeChanged(songindex, getItemCount());
                Log.d("NotifyRemoved", "Notifying removed song at position " + songindex);
                Log.d("SONG REMOVED RW", nextsong.getSongName());
                break;
            }
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        switch(viewType) {
            case FIRST_ITEM:
                view = inflater.inflate(R.layout.header_recyclerview_playlist, parent, false);
                return new HeaderViewHolder(view);
            case NORMAL_ITEM:
                view = inflater.inflate(R.layout.normal_recyclerview_playlist, parent, false);
                return new NormalItemViewHolder(view);
            default:
                view = inflater.inflate(R.layout.normal_recyclerview_playlist, parent, false);
                return new NormalItemViewHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(position == 0) {
            return FIRST_ITEM;
        } else {
            return NORMAL_ITEM;
        }
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {

        if(holder instanceof NormalItemViewHolder) {
            final NormalItemViewHolder normalitemholder = (NormalItemViewHolder) holder;

            //Songpreview longclick listener
            normalitemholder.rowview.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    activity.onItemLongClicked(songs.get(normalitemholder.getAdapterPosition()));
                    return true;
                }
            });

            //Stoppa songpreview vid lyft av finger över rowview
            normalitemholder.rowview.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    switch (motionEvent.getAction()) {
                        case MotionEvent.ACTION_UP:
                            activity.onItemLongClickEnded();
                    }
                    return false;
                }
            });

            //Likeknapp click listener
            normalitemholder.likebutton.setEventListener(new SparkEventListener() {
                @Override
                public void onEvent(ImageView button, boolean pressedlike) {
                    activity.onItemLikePressed(songs.get(normalitemholder.getAdapterPosition()), pressedlike);
                }
            });


            normalitemholder.profilename.setText(songs.get(position).getAddedByProfileName());
            normalitemholder.artistname.setText(songs.get(position).getArtist());
            normalitemholder.songname.setText(songs.get(position).getSongName());
            normalitemholder.runtime.setText(songs.get(position).getRuntime());

            //Ladda addedbyprofilepicture
            normalitemholder.profilepicture.setImageBitmap(ImageUtils.byteArrayToBitmap(songs.get(position).getAddedByProfilePicture()));

            //Skapa transformklasser till albumart
            normalitemholder.croptransform = new CropTransformation(context, 600, 200, CropTransformation.CropType.CENTER); //width, height
            normalitemholder.blurtransform = new BlurTransformation(context, 15, 1); //blurradius, downsampling (scale, 1 == ingen downsampling)
            normalitemholder.colorfiltertransform = new ColorFilterTransformation(context, R.color.colorPrimary);

            //Ladda albumart och croppa/blurra
            Glide.with(context)
                    .load(songs.get(position).getAlbumArtUrl())
                    .asBitmap()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .transform(normalitemholder.croptransform, normalitemholder.blurtransform, normalitemholder.colorfiltertransform)
                    .into(new SimpleTarget<Bitmap>(600,100) {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
                            normalitemholder.albumartdrawable = new BitmapDrawable(context.getResources(), resource);
                            normalitemholder.rowview.setBackground(normalitemholder.albumartdrawable);
                        }
                    });
        }

        if(holder instanceof HeaderViewHolder) {
            final HeaderViewHolder headerholder = (HeaderViewHolder) holder;

            //Songpreview longclick listener
            headerholder.rowview.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    activity.onItemLongClicked(songs.get(headerholder.getAdapterPosition()));
                    return true;
                }
            });

            //Stoppa songpreview vid lyft av finger över rowview
            headerholder.rowview.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    switch (motionEvent.getAction()) {
                        case MotionEvent.ACTION_UP:
                            activity.onItemLongClickEnded();
                    }
                    return false;
                }
            });

            headerholder.profilename.setText(songs.get(position).getAddedByProfileName());
            headerholder.artistname.setText(songs.get(position).getArtist());
            headerholder.songname.setText(songs.get(position).getSongName());
            headerholder.runtime.setText(songs.get(position).getRuntime());

            //Ladda addedbyprofilepicture
            headerholder.profilepicture.setImageBitmap(ImageUtils.byteArrayToBitmap(songs.get(position).getAddedByProfilePicture()));

            //Skapa transformklasser till albumart
            headerholder.croptransform = new CropTransformation(context, 600, 300, CropTransformation.CropType.CENTER); //width, height
            headerholder.blurtransform = new BlurTransformation(context, 15, 1); //blurradius, downsampling (scale, 1 == ingen downsampling)
            headerholder.colorfiltertransform = new ColorFilterTransformation(context, R.color.colorPrimary);

            //Ladda albumart och croppa/blurrra
            Glide.with(context)
                    .load(songs.get(position).getAlbumArtUrl())
                    .asBitmap()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .transform(headerholder.croptransform, headerholder.blurtransform, headerholder.colorfiltertransform)
                    .into(new SimpleTarget<Bitmap>(600,400) {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
                            headerholder.albumartdrawable = new BitmapDrawable(context.getResources(), resource);
                            headerholder.rowview.setBackground(headerholder.albumartdrawable);
                        }
                    });
        }

    }

    class NormalItemViewHolder extends RecyclerView.ViewHolder {

        Drawable albumartdrawable;
        CropTransformation croptransform;
        BlurTransformation blurtransform;
        ColorFilterTransformation colorfiltertransform;
        Typeface ROBOTO;

        @BindView(R.id.likeButton)
        SparkButton likebutton;
        @BindView(R.id.profileName)
        TextView profilename;
        @BindView(R.id.profilePicture)
        CircularImageView profilepicture;
        @BindView(R.id.artistName)
        TextView artistname;
        @BindView(R.id.songName)
        TextView songname;
        @BindView(R.id.runTime)
        TextView runtime;
        @BindView(R.id.recyclerview_item_holder_normal)
        View rowview;

        public NormalItemViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);

            ROBOTO = Typeface.createFromAsset(view.getContext().getResources().getAssets(), "fonts/robotolight.ttf");

            profilename.setTypeface(ROBOTO);
            artistname.setTypeface(ROBOTO);
            songname.setTypeface(ROBOTO);
            runtime.setTypeface(ROBOTO);
        }
    }

    class HeaderViewHolder extends RecyclerView.ViewHolder {

        Drawable albumartdrawable;
        CropTransformation croptransform;
        BlurTransformation blurtransform;
        ColorFilterTransformation colorfiltertransform;
        Typeface ROBOTO;

        @BindView(R.id.profileName)
        TextView profilename;
        @BindView(R.id.profilePicture)
        CircularImageView profilepicture;
        @BindView(R.id.artistName)
        TextView artistname;
        @BindView(R.id.songName)
        TextView songname;
        @BindView(R.id.runTime)
        TextView runtime;
        @BindView(R.id.recyclerview_item_holder_header)
        View rowview;

        public HeaderViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);

            ROBOTO = Typeface.createFromAsset(view.getContext().getResources().getAssets(), "fonts/robotolight.ttf");

            profilename.setTypeface(ROBOTO);
            artistname.setTypeface(ROBOTO);
            songname.setTypeface(ROBOTO);
            runtime.setTypeface(ROBOTO);
        }
    }

}
