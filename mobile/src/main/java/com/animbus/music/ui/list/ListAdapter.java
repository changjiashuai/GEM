package com.animbus.music.ui.list;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.databinding.ViewDataBinding;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.annotation.IntDef;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.animbus.music.BR;
import com.animbus.music.R;
import com.animbus.music.util.SettingsManager;
import com.animbus.music.media.stable.PlaybackManager;
import com.animbus.music.media.objects.Album;
import com.animbus.music.media.objects.Genre;
import com.animbus.music.media.objects.Playlist;
import com.animbus.music.media.objects.Song;
import com.animbus.music.ui.ItemAlbumDetailsList;
import com.animbus.music.ui.ItemAlbumGrid;
import com.animbus.music.ui.ItemGenre;
import com.animbus.music.ui.ItemPlaylist;
import com.animbus.music.ui.ItemSongList;
import com.animbus.music.ui.activity.PlaylistDetails;
import com.animbus.music.ui.activity.albumDetails.AlbumDetails;
import com.animbus.music.ui.activity.theme.ThemeManager;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import static com.animbus.music.media.objects.Album.FRAME_COLOR;
import static com.animbus.music.media.objects.Album.SUBTITLE_COLOR;
import static com.animbus.music.media.objects.Album.TITLE_COLOR;

/**
 * Created by Adrian on 10/28/2015.
 */
public class ListAdapter extends RecyclerView.Adapter<ListAdapter.BasicViewHolder> {
    public static final int TYPE_SONG = 0, TYPE_ALBUM = 1, TYPE_PLAYLIST = 2, TYPE_GENRE = 3, TYPE_ARTIST = 4;
    public static final int TYPE_ALBUM_DETAILS = -1;
    List data;
    int type;
    LayoutInflater inflater;
    Context context;

    @IntDef({TYPE_SONG, TYPE_ALBUM, TYPE_PLAYLIST, TYPE_GENRE, TYPE_ARTIST, TYPE_ALBUM_DETAILS})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Type {
    }

    public ListAdapter(@Type int type, List data, Context cxt) {
        this.type = type;
        this.data = data;
        this.context = cxt;
        this.inflater = LayoutInflater.from(cxt);
    }

    @Override
    public BasicViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (type == TYPE_SONG) {
            return new SongsViewHolder(ItemSongList.inflate(inflater, parent, false));
        } else if (type == TYPE_ALBUM) {
            return new AlbumsViewHolder(ItemAlbumGrid.inflate(inflater, parent, false));
        } else if (type == TYPE_PLAYLIST) {
            return new PlaylistsViewHolder(ItemPlaylist.inflate(inflater, parent, false));
        } else if (type == TYPE_ARTIST) {
            return null;
        } else if (type == TYPE_GENRE) {
            return new GenresViewHolder(ItemGenre.inflate(inflater, parent, false));
        } else if (type == TYPE_ALBUM_DETAILS) {
            return new AlbumDetailsViewHolder(ItemAlbumDetailsList.inflate(inflater, parent, false));
        } else {
            return null;
        }
    }

    @Override
    public void onBindViewHolder(BasicViewHolder holder, int position) {
        holder.update(data.get(position));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    Toolbar transitionAppBar;
    View listOrigin;
    Activity transitionActivity;

    public void setTransitionToAlbumDetails(Activity activity, Toolbar toolbar, View listOrigin) {
        this.transitionActivity = activity;
        this.transitionAppBar = toolbar;
        this.listOrigin = listOrigin;
    }


    ///////////////////////////////////////////////////////////////////////////
    // Holders
    ///////////////////////////////////////////////////////////////////////////

    protected abstract class BasicViewHolder<BINDING extends ViewDataBinding, TYPE> extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener {
        protected BINDING binding;

        protected final int COLOR_DUR = 300;
        protected final int COLOR_DELAY_BASE = 550;
        protected final int COLOR_DELAY_MAX = 750;

        protected final int LIST_ANIM_DELAY = 10;
        protected final int LIST_ANIM_DUR = 500;

        protected BasicViewHolder(BINDING binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void update(TYPE object) {
            binding.setVariable(getVarId(), object);
            binding.getRoot().setOnClickListener(this);
            binding.getRoot().setOnLongClickListener(this);
            configure(object);
            animate();
        }

        private int getVarId() {
            int varId;
            switch (type) {
                case TYPE_ALBUM:
                    varId = BR.album;
                    break;
                case TYPE_SONG:
                    varId = BR.song;
                    break;
                case TYPE_ALBUM_DETAILS:
                    varId = BR.song;
                    break;
                case TYPE_GENRE:
                    varId = BR.genre;
                    break;
                case TYPE_PLAYLIST:
                    varId = BR.playlist;
                    break;
                default:
                    varId = -1;
                    break;
            }
            return varId;
        }

        protected abstract void configure(TYPE object);

        protected void animate() {

        }

        @Override
        public boolean onLongClick(View v) {
            //Do nothing. Can be overridden if it is necessary to do anything on a long click
            return false;
        }

    }

    protected abstract class SimpleViewHolder<BINDING extends ViewDataBinding, TYPE> extends BasicViewHolder<BINDING, TYPE> {
        protected SimpleViewHolder(BINDING binding) {
            super(binding);
        }

        @Override
        protected void configure(TYPE object) {
            //Do nothing. The default impl should do everything automatically
        }
    }

    protected class SongsViewHolder extends BasicViewHolder<ItemSongList, Song> {

        public SongsViewHolder(ItemSongList binding) {
            super(binding);
        }

        @Override
        public void configure(Song object) {
            object.getAlbum().requestArt(context, binding.songlistSongAlbumart);
        }

        @Override
        public void onClick(View v) {
            PlaybackManager.get().play(data, getAdapterPosition());
        }
    }

    protected class AlbumsViewHolder extends BasicViewHolder<ItemAlbumGrid, Album> implements RequestListener<String, GlideDrawable>, Palette.PaletteAsyncListener {

        private AsyncTask<Bitmap, Void, Palette> paletteTask;
        private ObjectAnimator backgroundAnimator, titleAnimator, subtitleAnimator;
        private int defaultBackground = context.getResources().getColor(!ThemeManager.get().useLightTheme ? R.color.primaryGreyDark : R.color.primaryLight);
        private int defaultTitle = context.getResources().getColor(!ThemeManager.get().useLightTheme ? R.color.primary_text_default_material_dark : R.color.primary_text_default_material_light);
        private int defaultSubtitle = context.getResources().getColor(!ThemeManager.get().useLightTheme ? R.color.secondary_text_default_material_dark : R.color.secondary_text_default_material_light);

        public AlbumsViewHolder(ItemAlbumGrid binding) {
            super(binding);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void configure(Album object) {
            resetPalette();
            binding.getAlbum().requestArt(context, binding.AlbumArtGridItemAlbumArt, this);
        }

        @Override
        protected void animate() {
            if (!binding.getAlbum().animated) {
                binding.getAlbum().animated = true;

                int animateTill;
                if (!SettingsManager.get().getBooleanSetting(SettingsManager.KEY_USE_TABS, false)) {
                    animateTill = 5;
                } else {
                    Configuration configuration = context.getResources().getConfiguration();
                    if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                        animateTill = 3;
                    } else {
                        animateTill = 2;
                    }
                }

                if (getAdapterPosition() <= animateTill) {
                    binding.AlbumGridItemRootView.setTranslationY(800.0f);
                    binding.AlbumGridItemRootView.animate()
                            .translationY(0.0f)
                            .alpha(1.0f)
                            .setDuration(LIST_ANIM_DUR)
                            .setStartDelay(LIST_ANIM_DELAY + (getAdapterPosition() * 100))
                            .start();
                } else binding.AlbumGridItemRootView.setAlpha(1.0f);
            }
        }

        @Override
        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
            return false;
        }

        @Override
        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target,
                                       boolean isFromMemoryCache, boolean isFirstResource) {
            if (isFromMemoryCache || binding.getAlbum().colorAnimated) {
                updatePalette(resource);
            } else {
                animatePalette(resource);
            }
            return false;
        }

        private void resetPalette() {
            if (paletteTask != null && !paletteTask.isCancelled()) paletteTask.cancel(true);

            if (backgroundAnimator != null) backgroundAnimator.cancel();
            if (titleAnimator != null) titleAnimator.cancel();
            if (subtitleAnimator != null) subtitleAnimator.cancel();

            binding.AlbumInfoToolbar.setBackgroundColor(defaultBackground);
            binding.AlbumTitle.setTextColor(defaultTitle);
            binding.AlbumArtist.setTextColor(defaultSubtitle);
        }

        private void updatePalette(GlideDrawable drawable) {
            int[] colors = binding.getAlbum().mainColors;
            if (colors != null) {
                binding.AlbumInfoToolbar.setBackgroundColor(colors[FRAME_COLOR]);
                binding.AlbumTitle.setTextColor(colors[TITLE_COLOR]);
                binding.AlbumArtist.setTextColor(colors[SUBTITLE_COLOR]);
            } else {
                resetPalette();
                generatePalette(drawable);
            }
        }

        private void animatePalette(GlideDrawable drawable) {
            int[] colors = binding.getAlbum().mainColors;
            if (colors != null) {
                Random colorDelayRandom = new Random();
                int COLOR_DELAY = colorDelayRandom.nextInt(COLOR_DELAY_MAX) + COLOR_DELAY_BASE;

                backgroundAnimator = ObjectAnimator.ofObject(
                        binding.AlbumInfoToolbar,
                        "backgroundColor",
                        new ArgbEvaluator(),
                        defaultBackground,
                        colors[FRAME_COLOR]);
                backgroundAnimator.setDuration(COLOR_DUR).setStartDelay(COLOR_DELAY);
                backgroundAnimator.start();

                titleAnimator = ObjectAnimator.ofObject(
                        binding.AlbumTitle,
                        "textColor",
                        new ArgbEvaluator(),
                        defaultTitle,
                        colors[TITLE_COLOR]);
                titleAnimator.setDuration(COLOR_DUR).setStartDelay(COLOR_DELAY);
                titleAnimator.start();

                subtitleAnimator = ObjectAnimator.ofObject(
                        binding.AlbumArtist,
                        "textColor",
                        new ArgbEvaluator(),
                        defaultSubtitle,
                        colors[SUBTITLE_COLOR]);
                subtitleAnimator.setDuration(COLOR_DUR).setStartDelay(COLOR_DELAY);
                subtitleAnimator.start();

                binding.getAlbum().colorAnimated = true;
            } else {
                generatePalette(drawable);
            }
        }

        private void generatePalette(GlideDrawable drawable) {
            if (binding.getAlbum().mainColors == null) {
                Bitmap art = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(art);
                drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                drawable.draw(canvas);
                paletteTask = Palette.from(art).generate(this);
            }
        }

        @Override
        public void onGenerated(Palette palette) {
            int back = defaultBackground, title = defaultTitle, subtitle = defaultSubtitle;
            int accent = Color.BLACK, accentIcon = Color.WHITE, accentSubIcon = Color.GRAY;

            if (!binding.getAlbum().defaultArt && SettingsManager.get().getBooleanSetting(SettingsManager.KEY_USE_PALETTE_IN_GRID, true)) {

                //Gets main swatches
                ArrayList<Palette.Swatch> sortedSwatches = new ArrayList<>(palette.getSwatches());
                Collections.sort(sortedSwatches, new Comparator<Palette.Swatch>() {
                    @Override
                    public int compare(Palette.Swatch a, Palette.Swatch b) {
                        return ((Integer) a.getPopulation()).compareTo(b.getPopulation());
                    }
                });

                Palette.Swatch[] swatches = null;

                try {
                    swatches = new Palette.Swatch[]{sortedSwatches.get(sortedSwatches.size() - 1), sortedSwatches.get(0)};
                } catch (Exception ignored) {
                }

                try {
                    back = swatches[0].getRgb();
                } catch (Exception ignored) {
                }
                try {
                    title = swatches[0].getTitleTextColor();
                } catch (Exception ignored) {
                }

                try {
                    subtitle = swatches[0].getBodyTextColor();
                } catch (Exception ignored) {
                }

                try {
                    accent = swatches[1].getRgb();
                } catch (Exception ignored) {
                }

                try {
                    accentIcon = swatches[1].getTitleTextColor();
                } catch (Exception ignored) {
                }

                try {
                    accentSubIcon = swatches[1].getBodyTextColor();
                } catch (Exception ignored) {
                }

            }

            binding.getAlbum().mainColors = new int[]{back, title, subtitle};
            binding.getAlbum().accentColors = new int[]{accent, accentIcon, accentSubIcon};

            animatePalette(null);
        }

        @Override
        public void onClick(View v) {
            try {
                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(transitionActivity,
                        new Pair<View, String>(transitionAppBar, "appbar"),
                        new Pair<View, String>(transitionAppBar, "appbar_text_protection"),
                        new Pair<View, String>(binding.getRoot().findViewById(R.id.AlbumArtGridItemAlbumArt), "art"),
                        new Pair<View, String>(listOrigin, "list"),
                        new Pair<View, String>(binding.getRoot().findViewById(R.id.AlbumInfoToolbar), "info")
                );
                ActivityCompat.startActivity(transitionActivity, new Intent(context, AlbumDetails.class)
                        .putExtra("album_id", binding.getAlbum().getId()), options.toBundle());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public boolean onLongClick(View v) {
            Snackbar.make(v, R.string.playing_album, Snackbar.LENGTH_SHORT).show();
            PlaybackManager.get().play(binding.getAlbum().getSongs(), 0);
            return false;
        }
    }

    protected class AlbumDetailsViewHolder extends SimpleViewHolder<ItemAlbumDetailsList, Song> {

        protected AlbumDetailsViewHolder(ItemAlbumDetailsList binding) {
            super(binding);
        }

        @Override
        public void onClick(View v) {
            PlaybackManager.get().play(data, getAdapterPosition());
        }
    }

    protected class PlaylistsViewHolder extends SimpleViewHolder<ItemPlaylist, Playlist> {

        protected PlaylistsViewHolder(ItemPlaylist binding) {
            super(binding);
        }

        @Override
        public void onClick(View v) {
            context.startActivity(new Intent(context, PlaylistDetails.class).putExtra("playlist_id", binding.getPlaylist().getId()));
        }

        @Override
        public boolean onLongClick(View v) {
            PlaybackManager.get().play(binding.getPlaylist().getSongs(), 0);
            return true;
        }
    }

    protected class GenresViewHolder extends SimpleViewHolder<ItemGenre, Genre> {

        protected GenresViewHolder(ItemGenre binding) {
            super(binding);
        }

        @Override
        public void onClick(View v) {
            Toast.makeText(v.getContext(), "Genre Clicked", Toast.LENGTH_SHORT).show();
        }
    }

}

