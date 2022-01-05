package com.life.android;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.life.android.utils.Constants.POSITION;
import static com.life.android.utils.Constants.STREAM_URL;
import static com.life.android.utils.Constants.YOUTUBE;
import static com.life.android.utils.Constants.YOUTUBE_LIVE;
import static com.google.android.gms.ads.AdActivity.CLASS_NAME;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PersistableBundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.mediarouter.app.MediaRouteButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.life.android.adapters.CastCrewAdapter;
import com.life.android.adapters.CommentsAdapter;
import com.life.android.adapters.DownloadAdapter;
import com.life.android.adapters.EpisodeAdapter;
import com.life.android.adapters.EpisodeDownloadAdapter;
import com.life.android.adapters.HomePageAdapter;
import com.life.android.adapters.ProgramAdapter;
import com.life.android.adapters.RelatedTvAdapter;
import com.life.android.adapters.SeasonsAdapter;
import com.life.android.adapters.ServerAdapter;
import com.life.android.adapters.SmallGenreAdapter;
import com.life.android.database.DatabaseHelper;
import com.life.android.database.continueWatching.ContinueWatchingModel;
import com.life.android.database.continueWatching.ContinueWatchingViewModel;
import com.life.android.database.downlaod.DownloadViewModel;
import com.life.android.helper.IConstants;
import com.life.android.helper.VideoPlayerConfig;
import com.life.android.models.CastCrew;
import com.life.android.models.CommonModels;
import com.life.android.models.CustomAddsModel;
import com.life.android.models.EpiModel;
import com.life.android.models.GetCommentsModel;
import com.life.android.models.PostCommentModel;
import com.life.android.models.Program;
import com.life.android.models.SubtitleModel;
import com.life.android.models.single_details.Cast;
import com.life.android.models.single_details.Director;
import com.life.android.models.single_details.DownloadLink;
import com.life.android.models.single_details.Episode;
import com.life.android.models.single_details.Genre;
import com.life.android.models.single_details.RelatedMovie;
import com.life.android.models.single_details.Season;
import com.life.android.models.single_details.SingleDetails;
import com.life.android.models.single_details.Subtitle;
import com.life.android.models.single_details.Video;
import com.life.android.models.single_details_tv.AdditionalMediaSource;
import com.life.android.models.single_details_tv.AllTvChannel;
import com.life.android.models.single_details_tv.ProgramGuide;
import com.life.android.models.single_details_tv.SingleDetailsTV;
import com.life.android.network.RetrofitClient;
import com.life.android.network.apis.CommentApi;
import com.life.android.network.apis.ContinueWatchApi;
import com.life.android.network.apis.FavouriteApi;
import com.life.android.network.apis.ReportApi;
import com.life.android.network.apis.SingleDetailsApi;
import com.life.android.network.apis.SingleDetailsTVApi;
import com.life.android.network.apis.SubscriptionApi;
import com.life.android.network.model.APIResponse;
import com.life.android.network.model.ActiveStatus;
import com.life.android.network.model.FavoriteModel;
import com.life.android.utils.Constants;
import com.life.android.utils.HelperUtils;
import com.life.android.utils.HtmlTagHelper;
import com.life.android.utils.PreferenceUtils;
import com.life.android.utils.RtlUtils;
import com.life.android.utils.ScreenShot;
import com.life.android.utils.ToastMsg;
import com.life.android.utils.Tools;
import com.life.android.utils.TrackSelectionDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.ext.cast.CastPlayer;
import com.google.android.exoplayer2.ext.rtmp.RtmpDataSourceFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MergingMediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.SingleSampleMediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlaybackControlView;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.ui.SubtitleView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultAllocator;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.Util;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaQueueItem;
import com.google.android.gms.cast.MediaStatus;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.CastState;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;
import com.google.android.gms.common.images.WebImage;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.romainpiel.shimmer.Shimmer;
import com.romainpiel.shimmer.ShimmerTextView;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import at.huber.youtubeExtractor.VideoMeta;
import at.huber.youtubeExtractor.YouTubeExtractor;
import at.huber.youtubeExtractor.YtFile;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

@SuppressLint("StaticFieldLeak")
public class DetailsActivity extends AppCompatActivity implements CastPlayer.SessionAvailabilityListener, ProgramAdapter.OnProgramClickListener,
        EpisodeAdapter.OnTVSeriesEpisodeItemClickListener, RelatedTvAdapter.RelatedTvClickListener, SeasonsAdapter.SeasonsAdapterListener {
    private final int PERMISSION_REQUEST_CODE = 1;
    private final int PRELOAD_TIME_S = 20;
    public static final String TAG = DetailsActivity.class.getSimpleName();
    private TextView tvName;
    private TextView tvDirector;
    private TextView tvRelease;
    private TextView tvDes;
    private TextView tvRelated;
    private RecyclerView rvServer;
    private RecyclerView rvServerForTV;
    private RecyclerView rvRelated;
    private RecyclerView rvComment;
    private RecyclerView castRv;
    private RecyclerView rvSmallGenre;
    private LinearLayout seasonSpinnerContainer;
    public RelativeLayout lPlay;
    private RelativeLayout contentDetails;
    private LinearLayout subscriptionLayout;
    private Button subscribeBt;
    private ImageView backIv;
    private ImageView trailerImgView;

    private ServerAdapter serverAdapter;
    private HomePageAdapter relatedAdapter;
    private RelatedTvAdapter relatedTvAdapter;
    private CastCrewAdapter castCrewAdapter;

    private final List<CommonModels> listServer = new ArrayList<>();
    private final List<CommonModels> listRelated = new ArrayList<>();
    private final List<GetCommentsModel> listComment = new ArrayList<>();
    private final List<CommonModels> listInternalDownload = new ArrayList<>();
    private final List<CommonModels> listExternalDownload = new ArrayList<>();
    private final List<CastCrew> castCrews = new ArrayList<>();
    private String strDirector = "";
    private String strGenre = "";
    public LinearLayout llBottom, llBottomParent;
    public RelativeLayout llcomment;
    private SwipeRefreshLayout swipeRefreshLayout;
    private String categoryType = "", id = "";
    private ImageView imgAddFav, shareIv2, reportIv;
    public ImageView imgBack, serverIv;
    private TextView trailerBt;
    private ShimmerTextView downloadBt, payAndWatchBtn, watchNowBt;
    private LinearLayout downloadAndTrailerBtContainer;
    private ImageView posterIv, thumbIv, descriptionBackIv;
    private String V_URL = "";
    public WebView webView;
    public ProgressBar progressBar;
    private boolean isFav = false;
    private boolean isWatchLater = false;
    private TextView chromeCastTv;
    private ShimmerFrameLayout shimmerFrameLayout;
    private Button btnComment;
    private EditText etComment;
    private CommentsAdapter commentsAdapter;

    public SimpleExoPlayer player, player_v2;
    public PlayerView simpleExoPlayerView, simpleExoPlayerView_v2;
    public FrameLayout youtubePlayerView;
    private RelativeLayout exoplayerLayout;
    public PlayerControlView castControlView;
    public SubtitleView subtitleView;
    private DefaultTrackSelector trackSelector;

    public ImageView imgFull;
    public ImageView aspectRatioIv, externalPlayerIv, volumControlIv, imgWatchList;
    private LinearLayout volumnControlLayout;
    private SeekBar volumnSeekbar;
    public MediaRouteButton mediaRouteButton;

    public static boolean isPlaying, isFullScr;
    public static View playerLayout;

    private int playerHeight;
    public static boolean isVideo = true;
    private final String strSubtitle = "Null";
    public static MediaSource mediaSource = null;
    public static ImageView imgSubtitle, imgAudio;
    private final List<SubtitleModel> listSub = new ArrayList<>();
    private AlertDialog alertDialog;
    private String mediaUrl;
    private boolean tv = false;
    private String download_check = "";
    private String trailerUrl = "", trailer_aws_source = "";


    private CastPlayer castPlayer;
    private boolean castSession;
    private String title;
    String castImageUrl;

    private LinearLayout tvLayout;
    private TextView tvTitleTv, watchStatusTv, timeTv, programTv, proGuideTv, watchLiveTv, trailerTitleView;
    private ProgramAdapter programAdapter;
    List<Program> programs = new ArrayList<>();
    private RecyclerView programRv;
    private ImageView tvThumbIv, shareIv, tvReportIV;

    private LinearLayout exoRewind, exoForward, seekbarLayout;
    private TextView liveTv;

    private String serverType;

    private String currentProgramTime;
    private String currentProgramTitle;
    private String userId, selectedCountry = "";

    private RelativeLayout descriptionLayout;
    private boolean activeMovie;

    private TextView sereisTitleTv;
    private RelativeLayout seriestLayout;
    private ImageView favIv;

    private boolean intLeft, intRight;
    private int sWidth;
    private float downX, downY;
    private AudioManager mAudioManager;
    private int aspectClickCount = 1;

    private HelperUtils helperUtils;
    private boolean vpnStatus;
    private ContinueWatchingViewModel viewModel;
    private long playerCurrentPosition = 0L;
    //season download
    private LinearLayout seasonDownloadLayout;
    private Spinner seasonDownloadSpinner;
    private RecyclerView seasonDownloadRecyclerView;
    private DownloadViewModel downloadViewModel;
    private RecyclerView seasonRecyclerView;

    final private ArrayList<CustomAddsModel> customAddsModelArrayList = new ArrayList<>();

    private View viewAdsLayout;
    private TextView tvAddSkip, tvAddLink;
    private ShimmerTextView payWatchExpireStatusView;
    private SingleDetails singleDetails;
    private ImageView progressImgView;
    private Uri appLinkData;
    private ImageButton exo_rew, exo_ffwd;

    private RelativeLayout mainContentLayout;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        RtlUtils.setScreenDirection(this);
        setTheme(R.style.TransDetailsScreenTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        Intent appLinkIntent = getIntent();
        appLinkData = appLinkIntent.getData();

        progressImgView = findViewById(R.id.details_progress_img_view);
        Glide.with(this).load(R.drawable.logo_anim).into(progressImgView);

        SharedPreferences sharedPreferences = getSharedPreferences("push", MODE_PRIVATE);
        selectedCountry = sharedPreferences.getString("country", "");

        //check vpn connection
        helperUtils = new HelperUtils(DetailsActivity.this);
        vpnStatus = helperUtils.isVpnConnectionAvailable();
        if (vpnStatus) {
            helperUtils.showWarningDialog(DetailsActivity.this, getString(R.string.vpn_detected), getString(R.string.close_vpn));
            return;
        }

        DatabaseHelper db = new DatabaseHelper(DetailsActivity.this);

        mAudioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);

        //---analytics-----------
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "id");
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "details_activity");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "activity");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        initViews();

        // chrome cast
        CastButtonFactory.setUpMediaRouteButton(getApplicationContext(), mediaRouteButton);
        CastContext castContext = CastContext.getSharedInstance(this);
        castPlayer = new CastPlayer(castContext);
        castPlayer.setSessionAvailabilityListener(this);

        //Player
        exo_ffwd.setOnClickListener(view -> player.seekTo(player.getContentPosition() + 10000));

        exo_rew.setOnClickListener(view -> player.seekTo(player.getContentPosition() - 10000));

        // cast button will show if the cast device will be available
        if (castContext.getCastState() != CastState.NO_DEVICES_AVAILABLE)
            mediaRouteButton.setVisibility(View.VISIBLE);
        // start the shimmer effect
        shimmerFrameLayout.startShimmer();
        playerHeight = lPlay.getLayoutParams().height;
        progressBar.setMax(100); // 100 maximum value for the progress value
        progressBar.setProgress(50);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.setWebChromeClient(new WebChromeClient());

        backIv.setOnClickListener(v -> {
            if (activeMovie) {
                setPlayerNormalScreen();
                if (player != null) {
                    player.setPlayWhenReady(false);
                    player.stop();
                }
                showDescriptionLayout();
                activeMovie = false;
            } else {
                onBackPressed();
            }
        });

        categoryType = getIntent().getStringExtra("vType");
        id = getIntent().getStringExtra("id");
        castSession = getIntent().getBooleanExtra("castSession", false);


        userId = db.getUserData().getUser_id();
        viewModel = new ViewModelProvider(DetailsActivity.this).get(ContinueWatchingViewModel.class);
        downloadViewModel = new ViewModelProvider(DetailsActivity.this).get(DownloadViewModel.class);


        if (PreferenceUtils.isLoggedIn(DetailsActivity.this)) {
            imgAddFav.setVisibility(VISIBLE);
            favIv.setVisibility(VISIBLE);
            imgWatchList.setVisibility(VISIBLE);
        } else {
            imgAddFav.setVisibility(GONE);
            favIv.setVisibility(GONE);
            imgWatchList.setVisibility(GONE);
        }

        commentsAdapter = new CommentsAdapter(this, listComment);
        rvComment.setLayoutManager(new LinearLayoutManager(this));
        rvComment.setHasFixedSize(false);
        rvComment.setNestedScrollingEnabled(false);
        rvComment.setAdapter(commentsAdapter);
        getComments();
        imgFull.setOnClickListener(v -> controlFullScreenPlayer());
        imgSubtitle.setOnClickListener(v -> showSubtitleDialog(DetailsActivity.this, listSub));

        imgAudio.setOnClickListener(view -> {
            TrackSelectionDialog trackSelectionDialog =
                    TrackSelectionDialog.createForTrackSelector(
                            trackSelector,
                            dismissedDialog -> {
                            });
            trackSelectionDialog.show(getSupportFragmentManager(), null);
        });

        btnComment.setOnClickListener(v -> {
            if (!PreferenceUtils.isLoggedIn(DetailsActivity.this)) {
                startActivity(new Intent(DetailsActivity.this, LoginActivity.class));
                overridePendingTransition(R.anim.enter, R.anim.exit);
                new ToastMsg(DetailsActivity.this).toastIconError(getString(R.string.login_first));
            } else if (etComment.getText().toString().equals("")) {
                new ToastMsg(DetailsActivity.this).toastIconError(getString(R.string.comment_empty));
            } else {
                String comment = etComment.getText().toString();
                addComment(id, PreferenceUtils.getUserId(DetailsActivity.this), comment);
            }
        });

        imgAddFav.setOnClickListener(v -> {
            if (isFav) {
                removeFromFav(Constants.WishListType.fav);
            } else {
                addToFav(Constants.WishListType.fav);
            }
        });

        // its for tv series only when description layout visibility gone.
        favIv.setOnClickListener(v -> {
            if (isFav) {
                removeFromFav(Constants.WishListType.fav);
            } else {
                addToFav(Constants.WishListType.fav);
            }
        });

        imgWatchList.setOnClickListener(v -> {
            if (isWatchLater) {
                removeFromFav(Constants.WishListType.watch_later);
            } else {
                addToFav(Constants.WishListType.watch_later);
            }
        });


        if (!isNetworkAvailable()) {
            new ToastMsg(DetailsActivity.this).toastIconError(getString(R.string.no_internet));
        }
        swipeRefreshLayout.setOnRefreshListener(() -> {
            clear_previous();
            initGetData();
        });

        payAndWatchBtn.setOnClickListener(view -> {
                    if (payAndWatchBtn.getText().toString().equals("Already Booked")) {
                        Toast.makeText(this, "Watch this movie on " + singleDetails.getStartDateTime(), Toast.LENGTH_SHORT).show();
                    } else {
                        Intent fragmentLoaderIntent = new Intent(DetailsActivity.this, FragmentLoaderActivity.class);
                        fragmentLoaderIntent.putExtra("fragmentLoaderIntent", Constants.TICKET_FRAGMENT);
                        fragmentLoaderIntent.putExtra("single_details", singleDetails);
                        fragmentLoaderIntent.putExtra(IConstants.IntentString.type, IConstants.Fragments.ticket);

                        someActivityResultLauncher.launch(fragmentLoaderIntent);

                        overridePendingTransition(R.anim.enter, R.anim.exit);
                    }
                }
        );
    }

    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    clear_previous();
                    initGetData();
                }
            });

    public void initViews() {
        llBottom = findViewById(R.id.llbottom);
        tvDes = findViewById(R.id.tv_details);
        tvRelease = findViewById(R.id.tv_release_date);
        tvName = findViewById(R.id.text_name);
        tvDirector = findViewById(R.id.tv_director);
        swipeRefreshLayout = findViewById(R.id.swipe_layout);
        imgAddFav = findViewById(R.id.add_fav);
        imgBack = findViewById(R.id.img_back);
        webView = findViewById(R.id.webView);
        progressBar = findViewById(R.id.progressBar);
        llBottomParent = findViewById(R.id.llbottomparent);
        lPlay = findViewById(R.id.play);
        rvRelated = findViewById(R.id.rv_related);
        tvRelated = findViewById(R.id.tv_related);
        shimmerFrameLayout = findViewById(R.id.shimmer_view_container);
        btnComment = findViewById(R.id.btn_comment);
        etComment = findViewById(R.id.et_comment);
        rvComment = findViewById(R.id.recyclerView_comment);
        llcomment = findViewById(R.id.llcomments);
        simpleExoPlayerView = findViewById(R.id.video_view);
        youtubePlayerView = findViewById(R.id.youtubePlayerView);
        exoplayerLayout = findViewById(R.id.exoPlayerLayout);
        subtitleView = findViewById(R.id.subtitle);
        playerLayout = findViewById(R.id.player_layout);
        imgFull = findViewById(R.id.img_full_scr);
        aspectRatioIv = findViewById(R.id.aspect_ratio_iv);
        externalPlayerIv = findViewById(R.id.external_player_iv);
        volumControlIv = findViewById(R.id.volumn_control_iv);
        volumnControlLayout = findViewById(R.id.volumn_layout);
        volumnSeekbar = findViewById(R.id.volumn_seekbar);
        rvServer = findViewById(R.id.rv_server_list);
        rvServerForTV = findViewById(R.id.rv_server_list_for_tv);
        seasonSpinnerContainer = findViewById(R.id.spinner_container);
        imgSubtitle = findViewById(R.id.img_subtitle);
        imgAudio = findViewById(R.id.img_audio);
        mediaRouteButton = findViewById(R.id.media_route_button);
        chromeCastTv = findViewById(R.id.chrome_cast_tv);
        castControlView = findViewById(R.id.cast_control_view);
        tvLayout = findViewById(R.id.tv_layout);
        tvTitleTv = findViewById(R.id.tv_title_tv);
        programRv = findViewById(R.id.program_guide_rv);
        tvThumbIv = findViewById(R.id.tv_thumb_iv);
        shareIv = findViewById(R.id.share_iv);
        tvReportIV = findViewById(R.id.tv_report_iv);
        watchStatusTv = findViewById(R.id.watch_status_tv);
        timeTv = findViewById(R.id.time_tv);
        programTv = findViewById(R.id.program_type_tv);
        exoRewind = findViewById(R.id.rewind_layout);
        exoForward = findViewById(R.id.forward_layout);
        seekbarLayout = findViewById(R.id.seekbar_layout);
        liveTv = findViewById(R.id.live_tv);
        castRv = findViewById(R.id.cast_rv);
        rvSmallGenre = findViewById(R.id.small_genre_rv);
        proGuideTv = findViewById(R.id.pro_guide_tv);
        watchLiveTv = findViewById(R.id.watch_live_tv);

        contentDetails = findViewById(R.id.content_details);
        subscriptionLayout = findViewById(R.id.subscribe_layout);
        subscribeBt = findViewById(R.id.subscribe_bt);
        backIv = findViewById(R.id.des_back_iv);

        descriptionLayout = findViewById(R.id.description_layout);
        watchNowBt = findViewById(R.id.watch_now_bt);
        downloadBt = findViewById(R.id.download_bt);
        trailerBt = findViewById(R.id.trailer_bt);
        downloadAndTrailerBtContainer = findViewById(R.id.downloadBt_container);
        posterIv = findViewById(R.id.poster_iv);
        thumbIv = findViewById(R.id.image_thumb);
        descriptionBackIv = findViewById(R.id.back_iv);
        serverIv = findViewById(R.id.img_server);

        seriestLayout = findViewById(R.id.series_layout);
        favIv = findViewById(R.id.add_fav2);
        sereisTitleTv = findViewById(R.id.seriest_title_tv);
        shareIv2 = findViewById(R.id.share_iv2);
        reportIv = findViewById(R.id.report_iv);
        payAndWatchBtn = findViewById(R.id.details_buy_now_btn);
        payWatchExpireStatusView = findViewById(R.id.pay_watch_expire_status);
        //season download
        seasonDownloadLayout = findViewById(R.id.seasonDownloadLayout);
        seasonDownloadSpinner = findViewById(R.id.seasonSpinnerField);
        seasonDownloadRecyclerView = findViewById(R.id.seasonDownloadRecyclerView);
        seasonRecyclerView = findViewById(R.id.seasons_recycler_view);

        //adds
        viewAdsLayout = findViewById(R.id.view_ads_layout);
        tvAddSkip = findViewById(R.id.tv_add_skip);
        tvAddLink = findViewById(R.id.tv_add_link);
        simpleExoPlayerView_v2 = findViewById(R.id.video_view_v2);

        imgWatchList = findViewById(R.id.imgWatchList);
        trailerImgView = findViewById(R.id.trailer_img_view);
        trailerTitleView = findViewById(R.id.trailer_title_view);
        mainContentLayout = findViewById(R.id.mainContentLayout);

        exo_rew = findViewById(R.id.exo_rew);
        exo_ffwd = findViewById(R.id.exo_ffwd);

        Shimmer shimmer = new Shimmer();
        shimmer.setDuration(2000);
        shimmer.start(watchNowBt);
        shimmer.start(downloadBt);
        shimmer.start(payAndWatchBtn);
        shimmer.start(payWatchExpireStatusView);
    }

    @SuppressLint("SourceLockedOrientationActivity")
    public void controlFullScreenPlayer() {
        if (isFullScr) {
            isFullScr = false;
            swipeRefreshLayout.setVisibility(VISIBLE);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

            lPlay.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, playerHeight));

            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

        } else {
            isFullScr = true;
            swipeRefreshLayout.setVisibility(GONE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

            lPlay.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (!AppConfig.ENABLE_EXTERNAL_PLAYER && externalPlayerIv != null) {
            externalPlayerIv.setVisibility(GONE);
        }
        initGetData();
        if (mAudioManager != null) {
            volumnSeekbar.setMax(mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
            int currentVolumn = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            volumnSeekbar.setProgress(currentVolumn);
        }

        volumnSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b) {
                    //volumnTv.setText(i+"");
                    mAudioManager.setStreamVolume(player.getAudioStreamType(), i, 0);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        volumControlIv.setOnClickListener(view -> volumnControlLayout.setVisibility(VISIBLE));

        aspectRatioIv.setOnClickListener(view -> {
            if (aspectClickCount == 1) {
                simpleExoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
                simpleExoPlayerView_v2.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
                player.setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
                aspectClickCount = 2;
            } else if (aspectClickCount == 2) {
                simpleExoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
                simpleExoPlayerView_v2.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
                player.setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
                aspectClickCount = 3;
            } else if (aspectClickCount == 3) {
                simpleExoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
                simpleExoPlayerView_v2.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
                player.setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
                aspectClickCount = 1;
            }
        });

        externalPlayerIv.setOnClickListener(view -> {

            if (mediaUrl != null) {
                if (!tv) {
                    // set player normal/ portrait screen if not tv
                    descriptionLayout.setVisibility(VISIBLE);
                    setPlayerNormalScreen();
                }
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(mediaUrl), "video/*");
                startActivity(Intent.createChooser(intent, "Complete action using"));
            }
        });

        watchNowBt.setOnClickListener(v -> {
            playerCurrentPosition = 0L;
            if (!listServer.isEmpty()) {
                if (watchNowBt.getText().toString().equalsIgnoreCase(getString(R.string.play_episode))) {
                    final EpiModel obj = listServer.get(0).getListEpi().get(0);
                    hideDescriptionLayout();
                    showSeriesLayout();
                    setMediaUrlForTvSeries(obj.getStreamURL(), obj.getSeson(), obj.getEpi());
                    boolean castSession = getCastSession();
                    if (!castSession) {
                        if (obj.getServerType().equalsIgnoreCase("embed")) {
                            onEpisodeItemClickTvSeries("embed", v, obj, 0, null);
                        } else {
                            onEpisodeItemClickTvSeries("normal", v, obj, 0, null);
                        }
                    } else {
                        showQueuePopup(DetailsActivity.this, null, getMediaInfo());
                    }
                } else {
                    if (listServer.size() == 1) {
                        releasePlayer();
                        preparePlayer(listServer.get(0));
                        descriptionLayout.setVisibility(GONE);
                        lPlay.setVisibility(VISIBLE);
                    } else {
                        openServerDialog();
                    }
                }
            } else {
                Toast.makeText(DetailsActivity.this, R.string.no_video_found, Toast.LENGTH_SHORT).show();
            }
        });

        downloadBt.setOnClickListener(v -> {
            if (!listInternalDownload.isEmpty() || !listExternalDownload.isEmpty()) {
                if (AppConfig.ENABLE_DOWNLOAD_TO_ALL) {
                    openDownloadServerDialog();
                } else {
                    if (PreferenceUtils.isLoggedIn(DetailsActivity.this) && PreferenceUtils.isActivePlan(DetailsActivity.this)) {
                        openDownloadServerDialog();
                    } else {
                        Toast.makeText(DetailsActivity.this, R.string.download_not_permitted, Toast.LENGTH_SHORT).show();
                        Log.e("Download", "not permitted");
                    }
                }
            } else {
                Toast.makeText(DetailsActivity.this, R.string.no_download_server_found, Toast.LENGTH_SHORT).show();
            }
        });

        trailerBt.setOnClickListener(v -> {
            playerCurrentPosition = 0L;
            if (trailer_aws_source != null && !trailer_aws_source.equalsIgnoreCase("")) {
                serverType = STREAM_URL;
                mediaUrl = trailer_aws_source;
                CommonModels commonModels = new CommonModels();
                commonModels.setStremURL(trailer_aws_source);
                commonModels.setServerType(serverType);
                descriptionLayout.setVisibility(GONE);
                lPlay.setVisibility(VISIBLE);
                releasePlayer();
                preparePlayer(commonModels);
            } else if (trailerUrl != null && !trailerUrl.equalsIgnoreCase("")) {
                if (trailerUrl.contains("youtu")) {
                    serverType = YOUTUBE;
                } else {
                    serverType = STREAM_URL;
                }
                mediaUrl = trailerUrl;
                CommonModels commonModels = new CommonModels();
                commonModels.setStremURL(trailerUrl);
                //  commonModels.setServerType(YOUTUBE);
                commonModels.setServerType(serverType);
                descriptionLayout.setVisibility(GONE);
                lPlay.setVisibility(VISIBLE);
                releasePlayer();
                preparePlayer(commonModels);
            }

        });

        watchLiveTv.setOnClickListener(v -> {
            hideExoControlForTv();
            initMoviePlayer(mediaUrl, serverType, DetailsActivity.this);
            String textToShow = getString(R.string.watching_on) + " " + getString(R.string.app_name);
            watchStatusTv.setText(textToShow);
            watchLiveTv.setVisibility(GONE);

            timeTv.setText(currentProgramTime);
            programTv.setText(currentProgramTitle);
        });

        shareIv.setOnClickListener(v -> Tools.share(DetailsActivity.this, title));

        tvReportIV.setOnClickListener(v -> reportMovie());

        shareIv2.setOnClickListener(v -> {
            if (title == null) {
                new ToastMsg(DetailsActivity.this).toastIconError("Title should not be empty.");
                return;
            }

            String msg = getString(R.string.share_message);
            String content_two = title.replace(" ", "-");
            if (msg.contains(Constants.HASH_CONTENT_NAME)) {
                msg = msg.replace(Constants.HASH_CONTENT_NAME, TextUtils.isEmpty(title) ? "" : title);
                msg = msg.concat("\n" + Constants.BASE_URL_SHARE + content_two + "?" + categoryType + "&" + id);
            }

            //Sharing taking screen short to other app
            Uri uri = new ScreenShot(this).getBitmapUri(thumbIv); //get screen short
            Tools tools = new Tools();
            tools.shareMovieDetails(this, msg, uri);
        });

        //report icon
        reportIv.setOnClickListener(v -> reportMovie());

        castPlayer.addListener(new Player.DefaultEventListener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

                if (playWhenReady && playbackState == CastPlayer.STATE_READY) {
                    progressBar.setVisibility(View.GONE);

                    Log.e("STATE PLAYER:::", String.valueOf(isPlaying));

                } else if (playbackState == CastPlayer.STATE_READY) {
                    progressBar.setVisibility(View.GONE);
                    Log.e("STATE PLAYER:::", String.valueOf(isPlaying));
                } else if (playbackState == CastPlayer.STATE_BUFFERING) {
                    progressBar.setVisibility(VISIBLE);
                    Log.e("STATE PLAYER:::", String.valueOf(isPlaying));
                } else {
                    Log.e("STATE PLAYER:::", String.valueOf(isPlaying));
                }

            }
        });

        serverIv.setOnClickListener(v -> {

        });

        simpleExoPlayerView.setControllerVisibilityListener(visibility -> {
            if (visibility == 0) {
                imgBack.setVisibility(VISIBLE);

                if (categoryType.equals("tv") || categoryType.equals("tvseries")) {
                    imgFull.setVisibility(VISIBLE);
                } else {
                    imgFull.setVisibility(GONE);
                }

                if (download_check.equals("1")) {
                    if (!tv) {
                        if (activeMovie) {
                            serverIv.setVisibility(GONE);
                        }
                    }
                }

                if (listSub.size() != 0) {
                    imgSubtitle.setVisibility(GONE);
                }
            } else {
                imgBack.setVisibility(GONE);
                imgFull.setVisibility(GONE);
                imgSubtitle.setVisibility(GONE);
                volumnControlLayout.setVisibility(GONE);
            }
        });

        subscribeBt.setOnClickListener(v -> {
            if (userId == null) {
                new ToastMsg(DetailsActivity.this).toastIconError(getResources().getString(R.string.subscribe_error));
                startActivity(new Intent(DetailsActivity.this, LoginActivity.class));
                finish();
            } else {
                startActivity(new Intent(DetailsActivity.this, PurchasePlanActivity.class));
                overridePendingTransition(R.anim.enter, R.anim.exit);
            }

        });

        imgBack.setOnClickListener(v -> onBackPressed());

        descriptionBackIv.setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.pop_enter, R.anim.pop_exit);
        });


    }

    String videoReport = "", audioReport = "", subtitleReport = "", messageReport = "";

    private void reportMovie() {
        //open movie report dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.movie_report_dialog, null);
        builder.setView(view);
        final AlertDialog dialog = builder.create();
        dialog.setCancelable(false);

        TextView movieTitle = view.findViewById(R.id.movie_title);
        RadioGroup videoGroup = view.findViewById(R.id.radio_group_video);
        RadioGroup audioGroup = view.findViewById(R.id.radio_group_audio);
        RadioGroup subtitleGroup = view.findViewById(R.id.radio_group_subtitle);
        //EditText message = view.findViewById(R.id.report_message_et);
        TextInputEditText message = view.findViewById(R.id.report_message_et);
        Button submitButton = view.findViewById(R.id.submit_button);
        Button cancelButton = view.findViewById(R.id.cancel_button);
        LinearLayout subtitleLayout = view.findViewById(R.id.subtitleLayout);
        if (this.categoryType.equalsIgnoreCase("tv")) {
            subtitleLayout.setVisibility(GONE);
        }
        String textToShow = "Report for: " + title;
        movieTitle.setText(textToShow);

        videoGroup.setOnCheckedChangeListener((group, checkedId) -> {
            // find the radiobutton by returned id
            RadioButton radioButton = view.findViewById(checkedId);
            videoReport = radioButton.getText().toString();
        });

        audioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            // find the radiobutton by returned id
            RadioButton radioButton = view.findViewById(checkedId);
            audioReport = radioButton.getText().toString();
        });

        subtitleGroup.setOnCheckedChangeListener((group, checkedId) -> {
            // find the radiobutton by returned id
            RadioButton radioButton = view.findViewById(checkedId);
            subtitleReport = radioButton.getText().toString();
        });

        submitButton.setOnClickListener(v -> {
            if (message.getText() != null) {
                messageReport = message.getText().toString().trim();
            }
            Retrofit retrofit = RetrofitClient.getAuthRetrofitInstance();
            ReportApi api = retrofit.create(ReportApi.class);
            Call<ResponseBody> call = api.submitReport(AppConfig.API_KEY, categoryType, id, videoReport, audioReport, subtitleReport, messageReport);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    if (response.code() == 200) {
                        new ToastMsg(getApplicationContext()).toastIconSuccess("Report submitted");
                    } else {
                        new ToastMsg(getApplicationContext()).toastIconError(getResources().getString(R.string.something_went_text));
                    }
                    dialog.dismiss();
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    new ToastMsg(getApplicationContext()).toastIconError(getResources().getString(R.string.something_went_text));
                    dialog.dismiss();
                }
            });
        });
        cancelButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();

    }


    private void updateContinueWatchingDataToServer() {
        if ((playerCurrentPosition / 1000) > 5) {
            Retrofit retrofit = RetrofitClient.getRetrofitInstance();
            ContinueWatchApi api = retrofit.create(ContinueWatchApi.class);
            Call<APIResponse<Object>> call = api.saveContinueWatch(AppConfig.API_KEY, userId, id,
                    String.valueOf((playerCurrentPosition / 1000)),
                    categoryType, AppConfig.Device_Type);

            call.enqueue(new Callback<APIResponse<Object>>() {
                @Override
                public void onResponse(@NonNull Call<APIResponse<Object>> call, @NonNull Response<APIResponse<Object>> response) {
                    try {
                        Log.d(TAG, "onResponse: " + response.body());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<APIResponse<Object>> call, @NonNull Throwable t) {
                    t.printStackTrace();
                }
            });

        }
    }


    @SuppressLint("SourceLockedOrientationActivity")
    public void setPlayerNormalScreen() {
        swipeRefreshLayout.setVisibility(VISIBLE);
        lPlay.setVisibility(GONE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        //close embed link playing
        if (webView.getVisibility() == VISIBLE) {
            if (webView != null) {
                Intent intent = new Intent(DetailsActivity.this, DetailsActivity.class);
                intent.putExtra("vType", categoryType);
                intent.putExtra("id", id);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        }

        lPlay.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, playerHeight));
    }

    @SuppressLint("SourceLockedOrientationActivity")
    public void setPlayerFullScreen() {
        swipeRefreshLayout.setVisibility(GONE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        lPlay.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
    }

    private void openDownloadServerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.layout_download_server_dialog, null);
        LinearLayout internalDownloadLayout = view.findViewById(R.id.internal_download_layout);
        LinearLayout externalDownloadLayout = view.findViewById(R.id.external_download_layout);
        if (listExternalDownload.isEmpty()) {
            externalDownloadLayout.setVisibility(GONE);
        }
        if (listInternalDownload.isEmpty()) {
            internalDownloadLayout.setVisibility(GONE);
        }
        RecyclerView internalServerRv = view.findViewById(R.id.internal_download_rv);
        RecyclerView externalServerRv = view.findViewById(R.id.external_download_rv);
        DownloadAdapter internalDownloadAdapter = new DownloadAdapter(this, listInternalDownload, true, downloadViewModel);
        internalServerRv.setLayoutManager(new LinearLayoutManager(this));
        internalServerRv.setHasFixedSize(false);
        internalServerRv.setAdapter(internalDownloadAdapter);

        DownloadAdapter externalDownloadAdapter = new DownloadAdapter(this, listExternalDownload, true, downloadViewModel);
        externalServerRv.setLayoutManager(new LinearLayoutManager(this));
        externalServerRv.setHasFixedSize(false);
        externalServerRv.setAdapter(externalDownloadAdapter);

        builder.setView(view);

        final AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void openServerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.layout_server_dialog, null);
        RecyclerView serverRv = view.findViewById(R.id.serverRv);
        serverAdapter = new ServerAdapter(this, listServer, "movie");
        serverRv.setLayoutManager(new LinearLayoutManager(this));
        serverRv.setHasFixedSize(false);
        serverRv.setAdapter(serverAdapter);

        ImageView closeIv = view.findViewById(R.id.close_iv);
        builder.setView(view);

        final AlertDialog dialog = builder.create();
        dialog.show();

        closeIv.setOnClickListener(v -> dialog.dismiss());

        serverAdapter.setOnItemClickListener(new ServerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, CommonModels obj, int position, ServerAdapter.OriginalViewHolder holder) {
                releasePlayer();
                preparePlayer(obj);
            }

            @Override
            public void getFirstUrl(String url) {
                mediaUrl = url;
            }

            @Override
            public void hideDescriptionLayout() {
                descriptionLayout.setVisibility(GONE);
                lPlay.setVisibility(VISIBLE);
                dialog.dismiss();

            }
        });

    }

    public void preparePlayer(CommonModels obj) {
        activeMovie = true;
        setPlayerFullScreen();
        mediaUrl = obj.getStremURL();
        if (!castSession) {
            initMoviePlayer(obj.getStremURL(), obj.getServerType(), DetailsActivity.this);

            listSub.clear();
            if (obj.getListSub() != null) {
                listSub.addAll(obj.getListSub());
            }

            if (listSub.size() != 0) {
                imgSubtitle.setVisibility(GONE);
            } else {
                imgSubtitle.setVisibility(GONE);
            }

        } else {
            if (obj.getServerType().equalsIgnoreCase("embed")) {

                castSession = false;
                castPlayer.setSessionAvailabilityListener(null);
                castPlayer.release();

                // invisible control ui of exoplayer
                player.setPlayWhenReady(true);
                simpleExoPlayerView.setUseController(true);

                // invisible control ui of casting
                castControlView.setVisibility(GONE);
                chromeCastTv.setVisibility(GONE);


            } else {
                showQueuePopup(DetailsActivity.this, null, getMediaInfo());
            }
        }
    }

    void clear_previous() {
        strDirector = "";
        strGenre = "";
        listInternalDownload.clear();
        listExternalDownload.clear();
        programs.clear();
        castCrews.clear();
    }

    public void showSubtitleDialog(Context context, List<SubtitleModel> list) {
        ViewGroup viewGroup = findViewById(android.R.id.content);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.custom_dialog_subtitle, viewGroup, false);
        ImageView cancel = dialogView.findViewById(R.id.cancel);

        RecyclerView recyclerView = dialogView.findViewById(R.id.recyclerView);
        SubtitleAdapter adapter = new SubtitleAdapter(context, list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        alertDialog = builder.create();
        alertDialog.show();

        cancel.setOnClickListener(v -> alertDialog.cancel());

    }

    @Override
    public void onCastSessionAvailable() {
        castSession = true;
        MediaMetadata movieMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);
        movieMetadata.putString(MediaMetadata.KEY_TITLE, title);
        //movieMetadata.putString(MediaMetadata.KEY_ALBUM_ARTIST, "Test Artist");
        movieMetadata.addImage(new WebImage(Uri.parse(castImageUrl)));
        MediaInfo mediaInfo = new MediaInfo.Builder(mediaUrl)
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setContentType(MimeTypes.VIDEO_UNKNOWN)
                .setMetadata(movieMetadata).build();

        //array of media sources
        final MediaQueueItem[] mediaItems = {new MediaQueueItem.Builder(mediaInfo).build()};

        castPlayer.loadItems(mediaItems, 0, 3000, Player.REPEAT_MODE_OFF);

        // visible control ui of casting
        try {
            castControlView.setVisibility(VISIBLE);
            castControlView.setPlayer(castPlayer);
            castControlView.addVisibilityListener(visibility -> {
                if (visibility == GONE) {
                    castControlView.setVisibility(VISIBLE);
                    chromeCastTv.setVisibility(VISIBLE);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }


        // invisible control ui of exoplayer
        player.setPlayWhenReady(false);
        simpleExoPlayerView.setUseController(false);
    }

    @Override
    public void onCastSessionUnavailable() {
        // make cast session false
        castSession = false;
        // invisible control ui of exoplayer
        player.setPlayWhenReady(true);
        simpleExoPlayerView.setUseController(true);

        // invisible control ui of casting
        castControlView.setVisibility(GONE);
        chromeCastTv.setVisibility(GONE);
    }

    public void initServerTypeForTv(String serverType) {
        this.serverType = serverType;
    }

    @Override
    public void onProgramClick(Program program) {
        if (program.getProgramStatus().equals("onaired") && program.getVideoUrl() != null) {
            showExoControlForTv();
            initMoviePlayer(program.getVideoUrl(), "tv", this);
            timeTv.setText(program.getTime());
            programTv.setText(program.getTitle());
        } else {
            new ToastMsg(DetailsActivity.this).toastIconError("Not Yet");
        }
    }

    //this method will be called when related tv channel is clicked
    @Override
    public void onRelatedTvClicked(CommonModels obj) {
        categoryType = obj.getVideoType();
        id = obj.getId();
        initGetData();
    }

    // this will call when any episode is clicked
    //if it is embed player will go full screen
    @Override
    public void onEpisodeItemClickTvSeries(String type, View view, EpiModel obj, int position, EpisodeAdapter.OriginalViewHolder holder) {
        if (type.equalsIgnoreCase("embed")) {
            CommonModels model = new CommonModels();
            model.setStremURL(obj.getStreamURL());
            model.setServerType(obj.getServerType());
            model.setListSub(null);
            releasePlayer();
            // resetCastPlayer();
            preparePlayer(model);
        } else {
            if (obj != null) {
                if (obj.getSubtitleList().size() != 0) {
                    listSub.clear();
                    listSub.addAll(obj.getSubtitleList());
                    imgSubtitle.setVisibility(GONE);
                } else {
                    listSub.clear();
                    imgSubtitle.setVisibility(GONE);
                }

                initMoviePlayer(obj.getStreamURL(), obj.getServerType(), DetailsActivity.this);
            }
        }
    }


    private class SubtitleAdapter extends RecyclerView.Adapter<SubtitleAdapter.OriginalViewHolder> {
        private List<SubtitleModel> items;
        private final Context ctx;

        public SubtitleAdapter(Context context, List<SubtitleModel> items) {
            this.items = items;
            ctx = context;
        }

        @NonNull
        @Override
        public SubtitleAdapter.OriginalViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            SubtitleAdapter.OriginalViewHolder vh;
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_subtitle, parent, false);
            vh = new SubtitleAdapter.OriginalViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(SubtitleAdapter.OriginalViewHolder holder, final int position) {
            final SubtitleModel obj = items.get(position);
            holder.name.setText(obj.getLanguage());

            holder.lyt_parent.setOnClickListener(v -> {
                setSelectedSubtitle(mediaSource, obj.getUrl(), ctx);
                alertDialog.cancel();
            });

        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public class OriginalViewHolder extends RecyclerView.ViewHolder {
            public TextView name;
            private final View lyt_parent;

            public OriginalViewHolder(View v) {
                super(v);
                name = v.findViewById(R.id.name);
                lyt_parent = v.findViewById(R.id.lyt_parent);
            }
        }
    }


    @SuppressLint("SetTextI18n")
    private void initGetData() {
        //check vpn connection
        helperUtils = new HelperUtils(DetailsActivity.this);
        vpnStatus = helperUtils.isVpnConnectionAvailable();
        if (appLinkData != null) {
            String appLinkString = String.valueOf(appLinkData);
            if (appLinkString.contains("&")) {
                String contentId = appLinkString.substring(appLinkString.indexOf("&") + 1);
                if (contentId.length() > 10) {
                    Intent launchIntent = new Intent(this, SplashScreenActivity.class);
                    startActivity(launchIntent);
                }
                categoryType = appLinkString.substring(appLinkString.indexOf("?") + 1, appLinkString.indexOf("&"));
                id = contentId;
            }
        }
        if (vpnStatus) {
            helperUtils.showWarningDialog(DetailsActivity.this, getString(R.string.vpn_detected), getString(R.string.close_vpn));
        } else {
            if (!categoryType.equals("tv")) {
                //----related rv----------
                relatedAdapter = new HomePageAdapter(this, listRelated, "");
                rvRelated.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,
                        false));
                rvRelated.setHasFixedSize(false);
                rvRelated.setAdapter(relatedAdapter);
                if (categoryType.equals("tvseries")) {

                    seasonSpinnerContainer.setVisibility(VISIBLE);
                    rvServer.setVisibility(VISIBLE);
                    serverIv.setVisibility(GONE);

                    rvRelated.removeAllViews();
                    listRelated.clear();
                    rvServer.removeAllViews();
                    listServer.clear();

                    downloadBt.setVisibility(GONE);
                    watchNowBt.setVisibility(GONE);
                    trailerBt.setVisibility(GONE);

                    // cast & crew adapter
                    castCrewAdapter = new CastCrewAdapter(this, castCrews);
                    castRv.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
                    castRv.setHasFixedSize(false);
                    castRv.setAdapter(castCrewAdapter);

                    getSeriesData(categoryType, id);

                    if (listSub.size() == 0) {
                        imgSubtitle.setVisibility(GONE);
                    }

                } else {
                    imgFull.setVisibility(GONE);
                    listServer.clear();
                    rvRelated.removeAllViews();
                    listRelated.clear();
                    if (listSub.size() == 0) {
                        imgSubtitle.setVisibility(GONE);
                    }

                    // cast & crew adapter
                    castCrewAdapter = new CastCrewAdapter(this, castCrews);
                    castRv.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
                    castRv.setHasFixedSize(false);
                    castRv.setAdapter(castCrewAdapter);

                    getMovieData(categoryType, id);

                    final ServerAdapter.OriginalViewHolder[] viewHolder = {null};
                }

                if (PreferenceUtils.isLoggedIn(DetailsActivity.this)) {
                    getFavStatus();
                }

            } else {
                tv = true;
                imgSubtitle.setVisibility(GONE);
                llcomment.setVisibility(GONE);
                serverIv.setVisibility(GONE);

                rvServer.setVisibility(VISIBLE);
                descriptionLayout.setVisibility(GONE);
                lPlay.setVisibility(VISIBLE);

                // hide exo player some control
                hideExoControlForTv();

                tvLayout.setVisibility(VISIBLE);

                // hide program guide if its disable from api
                if (!PreferenceUtils.isProgramGuideEnabled(DetailsActivity.this)) {
                    proGuideTv.setVisibility(GONE);
                    programRv.setVisibility(GONE);
                }

                watchStatusTv.setText(getString(R.string.watching_on) + " " + getString(R.string.app_name));

                tvRelated.setText(getString(R.string.all_tv_channel));

                rvServer.removeAllViews();
                listServer.clear();
                rvRelated.removeAllViews();
                listRelated.clear();

                programAdapter = new ProgramAdapter(programs, this);
                programRv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
                programRv.setHasFixedSize(false);
                programRv.setAdapter(programAdapter);
                programAdapter.setOnProgramClickListener(this);

                //----related rv----------
                //relatedTvAdapter = new LiveTvHomeAdapter(this, listRelated, TAG);
                relatedTvAdapter = new RelatedTvAdapter(listRelated, DetailsActivity.this);
                rvRelated.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
                rvRelated.setHasFixedSize(false);
                rvRelated.setAdapter(relatedTvAdapter);
                relatedTvAdapter.setListener(DetailsActivity.this);

                imgAddFav.setVisibility(GONE);
                favIv.setVisibility(GONE);

                serverAdapter = new ServerAdapter(this, listServer, "tv");
                rvServerForTV.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
                rvServerForTV.setHasFixedSize(false);
                rvServerForTV.setAdapter(serverAdapter);
                Log.e(TAG, "initGetData: TV");
                getTvData(categoryType, id);
                llBottom.setVisibility(GONE);

                final ServerAdapter.OriginalViewHolder[] viewHolder = {null};
                serverAdapter.setOnItemClickListener(new ServerAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, CommonModels obj, int position, ServerAdapter.OriginalViewHolder holder) {
                        mediaUrl = obj.getStremURL();
                        if (!castSession) {
                            initMoviePlayer(obj.getStremURL(), obj.getServerType(), DetailsActivity.this);
                        } else {
                            if (obj.getServerType().equalsIgnoreCase("embed")) {
                                castSession = false;
                                castPlayer.setSessionAvailabilityListener(null);
                                castPlayer.release();
                                // invisible control ui of exoplayer
                                player.setPlayWhenReady(true);
                                simpleExoPlayerView.setUseController(true);
                                // invisible control ui of casting
                                castControlView.setVisibility(GONE);
                                chromeCastTv.setVisibility(GONE);
                            } else {
                                showQueuePopup(DetailsActivity.this, null, getMediaInfo());
                            }
                        }
                        serverAdapter.chanColor(viewHolder[0], position);
                        holder.name.setTextColor(getResources().getColor(R.color.white));
                        viewHolder[0] = holder;
                    }

                    @Override
                    public void getFirstUrl(String url) {
                        mediaUrl = url;
                    }

                    @Override
                    public void hideDescriptionLayout() {

                    }
                });
            }

        }
    }

    private void openWebActivity(String s, Context context, String videoType) {

        if (isPlaying) {
            player.release();
        }
        progressBar.setVisibility(GONE);
        playerLayout.setVisibility(GONE);

        webView.loadUrl(s);
        webView.setWebChromeClient(new WebChromeClient());
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.setVisibility(VISIBLE);

    }

    public void initMoviePlayer(String url, String type, Context context) {
        serverType = type;
        if (type.equals("embed") || type.equals("vimeo") || type.equals("gdrive") /*|| type.equals("youtube-live")*/) {
            isVideo = false;
            openWebActivity(url, context, type);
        } else {
            isVideo = true;
            initVideoPlayer(url, context, type);
        }
    }

    public void initVideoPlayer(String url, Context context, String type) {
        progressBar.setVisibility(VISIBLE);
        Log.e(TAG, "initVideoPlayer: type: " + type);
        if (!categoryType.equals("tv")) {
            ContinueWatchingModel model = new ContinueWatchingModel(id, title, castImageUrl, 0, 0, url, categoryType, type);
            viewModel.insert(model);
        }

        if (player != null) {
            player.stop();
            player.release();
        }

        webView.setVisibility(GONE);
        playerLayout.setVisibility(VISIBLE);
        exoplayerLayout.setVisibility(VISIBLE);
        youtubePlayerView.setVisibility(GONE);
        swipeRefreshLayout.setVisibility(VISIBLE);

        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory = new
                AdaptiveTrackSelection.Factory(bandwidthMeter);

        trackSelector = new
                DefaultTrackSelector(videoTrackSelectionFactory);
        DefaultRenderersFactory renderersFactory = new DefaultRenderersFactory(this);
        renderersFactory.setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER);


        player = new SimpleExoPlayer.Builder(context, renderersFactory)
                .setTrackSelector(trackSelector)
                .build();

        Uri uri = Uri.parse(url);
        switch (type) {
            case "hls":
                mediaSource = hlsMediaSource(uri, context);
                break;
            case YOUTUBE:
                /**tag 18 : 360p, tag: 22 : 720p, 133: live*/
                extractYoutubeUrl(url, context, 18);
                // initYoutubePlayer(url);
                break;
            case YOUTUBE_LIVE:
                /**play Youtube-live video**/
                initYoutubePlayer(url);
                break;
            case "rtmp":
                mediaSource = rtmpMediaSource(uri);
                break;
            default:
                mediaSource = mediaSource(uri, context);
                break;
        }

        if (!type.equalsIgnoreCase(YOUTUBE) &&
                !type.equalsIgnoreCase(YOUTUBE_LIVE)) {
            try {
                player.prepare(mediaSource, true, false);
                simpleExoPlayerView.setPlayer(player);
                player.setPlayWhenReady(true);

                if (playerCurrentPosition > 0) {
                    player.seekTo(playerCurrentPosition);
                    player.setPlayWhenReady(true);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //add listener to player
        if (player != null) {
            player.addListener(playerListener);
        }

        if (singleDetails.showAds()) {
            long startAddAt = (long) customAddsModelArrayList.get(0).getPlayAddAtThisPosInMins() * 60 * 1000;
            new Handler(Looper.getMainLooper()).postDelayed(() -> initializePlayerForAdds(customAddsModelArrayList.get(0)), startAddAt);
        }

    }

    private int initialValue = 0;

    private void initializePlayerForAdds(CustomAddsModel addsModel) {

        viewAdsLayout.setVisibility(VISIBLE);
        simpleExoPlayerView_v2.setVisibility(VISIBLE);

        imgBack.setVisibility(View.INVISIBLE);
        backIv.setVisibility(View.INVISIBLE);

        playerLayout.setVisibility(GONE);
        exoplayerLayout.setVisibility(GONE);
        swipeRefreshLayout.setVisibility(GONE);

        simpleExoPlayerView.setUseController(false);

        player_v2 = new SimpleExoPlayer.Builder(this).build();
        simpleExoPlayerView_v2.setPlayer(player_v2);
        simpleExoPlayerView_v2.setUseController(false);


        DataSource.Factory dataSourceFactory =
                new DefaultDataSourceFactory(this, Util.getUserAgent(this, getString(R.string.app_name)));

        ProgressiveMediaSource.Factory mediaSourceFactory =
                new ProgressiveMediaSource.Factory(dataSourceFactory);

        MediaSource mediaSource =
                mediaSourceFactory.createMediaSource(Uri.parse(addsModel.getVideo_url()));
        player_v2.prepare(mediaSource);
        player_v2.setPlayWhenReady(true);

        initialValue = addsModel.getSkip_time();

        Log.d(TAG, "initializePlayerForAdds: SKIP TIME : " + initialValue);

        player_v2.addListener(new Player.EventListener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                Log.d(TAG, "onPlayerStateChanged: " + playbackState);
                if (playbackState == Player.STATE_ENDED) {
                    addsModel.setPlayed(true);
                    skipAddFunctionality();
                } else if (playWhenReady && playbackState == Player.STATE_READY) {
                    if (initialValue > 0) {
                        tvAddSkip.setVisibility(VISIBLE);
                        Timer t = new Timer();
                        t.schedule(new TimerTask() {
                            @SuppressLint("SetTextI18n")
                            @Override
                            public void run() {
                                runOnUiThread(() -> {
                                    if (initialValue <= 0) {
                                        tvAddSkip.setText("Skip");
                                        t.cancel();
                                    } else {
                                        tvAddSkip.setText("Skip in " + initialValue);
                                    }
                                    initialValue = initialValue - 1;
                                });
                            }
                        }, 1000, 1000);
                    } else {
                        tvAddSkip.setVisibility(GONE);
                    }
                    if (player != null) {
                        player.setPlayWhenReady(false);
                    }
                } else if (playbackState == Player.STATE_READY) {
                    imgBack.setVisibility(VISIBLE);
                }
            }

        });

        tvAddSkip.setOnClickListener(view -> {
            Log.d(TAG, "initializePlayerForAdds: " + initialValue + " : " + addsModel.getSkip_time());
            if (addsModel.getSkip_time() > 0) {
                if (initialValue <= 0) {
                    addsModel.setPlayed(true);
                    skipAddFunctionality();
                }
            } else {
                addsModel.setPlayed(true);
                skipAddFunctionality();
            }
        });

        tvAddLink.setOnClickListener(view -> {
            try {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(addsModel.getRedirect_url()));
                startActivity(browserIntent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        new Handler().postDelayed(() -> {
            if (!isFinishing()) {
                Log.d(TAG, "initializePlayerForAdds: Call for Check Fot Adds");
                for (int i = 0; i < customAddsModelArrayList.size(); i++) {
                    if (!customAddsModelArrayList.get(i).isPlayed()) {
                        checkForAds();
                        break;
                    }
                }
            }
        }, player_v2.getDuration());
    }

    private Timer adsCheckTimer;

    private void checkForAds() {
        adsCheckTimer = new Timer();
        adsCheckTimer.schedule(new TimerTask() {
            @SuppressLint("SetTextI18n")
            @Override
            public void run() {
                runOnUiThread(() -> {
                    if (player == null) {
                        if (adsCheckTimer != null) adsCheckTimer.cancel();
                        return;
                    }
                    long playerPosInMins = (player.getCurrentPosition() / 1000) / 60;
                    Log.d(TAG, "checkForAddsRun: " + playerPosInMins + " : " + player_v2.isPlaying());
                    boolean isPlayed = false;
                    for (int i = 0; i < customAddsModelArrayList.size(); i++) {
                        CustomAddsModel customAddsModel = customAddsModelArrayList.get(i);
                        if (!customAddsModel.isPlayed()) {
                            isPlayed = true;
                            if (playerPosInMins == customAddsModel.getPlayAddAtThisPosInMins() && !player_v2.isPlaying()) {
                                initializePlayerForAdds(customAddsModel);
                                adsCheckTimer.cancel();
                                break;
                            }
                        }
                    }
                    //
                    if (!isPlayed) {
                        Log.d(TAG, "run: CANCEL AFTER isPlayed : " + isPlayed);
                        adsCheckTimer.cancel();
                    }
                });
            }
        }, 15000, 15000);
    }

    private void skipAddFunctionality() {
        backIv.setVisibility(VISIBLE);
        imgBack.setVisibility(VISIBLE);

        viewAdsLayout.setVisibility(GONE);
        simpleExoPlayerView_v2.setVisibility(GONE);

        playerLayout.setVisibility(VISIBLE);
        exoplayerLayout.setVisibility(VISIBLE);
        swipeRefreshLayout.setVisibility(VISIBLE);

        if (player_v2 != null && simpleExoPlayerView_v2 != null) {
            player_v2.setPlayWhenReady(false);
            player_v2.stop();
            player_v2.release();
            //player_v2 = null;
            simpleExoPlayerView_v2.setPlayer(null);
            simpleExoPlayerView.setUseController(true);

            if (player != null) {
                player.setPlayWhenReady(true);
            }
        }
    }

    private void initYoutubePlayer(String url) {
        Log.e(TAG, "youtube_live: " + url);
        progressBar.setVisibility(GONE);

        playerLayout.setVisibility(GONE);
        exoplayerLayout.setVisibility(GONE);
        youtubePlayerView.setVisibility(VISIBLE);
        swipeRefreshLayout.setVisibility(VISIBLE);
        releasePlayer();
        String[] separated = url.split("=");

        YouTubePlayerFragment fragment = YouTubePlayerFragment.newInstance();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.youtubePlayerView, fragment);
        transaction.commit();
        fragment.initialize(AppConfig.YOUTUBE_API_KEY, new YouTubePlayer.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean wasRestored) {
                if (!wasRestored) {
                    youTubePlayer.cueVideo(separated[1]);
                    youTubePlayer.play();
                }
            }

            @Override
            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {

            }
        });

    }

    private final Player.EventListener playerListener = new Player.EventListener() {
        @Override
        public void onTimelineChanged(@NonNull Timeline timeline, int reason) {
        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            if (playWhenReady && playbackState == Player.STATE_READY) {
                isPlaying = true;
                progressBar.setVisibility(View.GONE);
            } else if (playbackState == Player.STATE_READY) {
                progressBar.setVisibility(View.GONE);
                isPlaying = false;
            } else if (playbackState == Player.STATE_BUFFERING) {
                isPlaying = false;
                progressBar.setVisibility(VISIBLE);
            } else if (playbackState == Player.STATE_ENDED) {
                //---delete into continueWatching------
                ContinueWatchingModel model = new ContinueWatchingModel(id, title,
                        castImageUrl, 0, 0, mediaUrl,
                        categoryType, serverType);
                viewModel.delete(model);
            } else {
                // player paused in any state
                isPlaying = false;
                playerCurrentPosition = player.getCurrentPosition();
            }
            // Toast.makeText(DetailsActivity.this, String.valueOf((playerCurrentPosition / 1000)), Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {
            isPlaying = false;
            progressBar.setVisibility(VISIBLE);
        }
    };

    private long calculateProgress(long position, long duration) {
        return (position * 100 / duration);
    }


    @SuppressLint("StaticFieldLeak")
    private void extractYoutubeUrl(String url, final Context context, final int tag) {
        Log.e("Trailer", "onExtractUrl");
        new YouTubeExtractor(context) {
            @Override
            public void onExtractionComplete(SparseArray<YtFile> ytFiles, VideoMeta vMeta) {
                if (ytFiles != null) {
                    int itag = tag;

                    try {
                        Log.e("Trailer", "onPlayUrl");
                        String extractedUrl = ytFiles.get(itag).getUrl();
                        //youtubeUrl = extractedUrl;
                        MediaSource source = mediaSource(Uri.parse(extractedUrl), context);
                        player.prepare(source, true, false);
                        simpleExoPlayerView.setPlayer(player);
                        player.setPlayWhenReady(AppConfig.YOUTUBE_VIDEO_AUTO_PLAY);
                        if (playerCurrentPosition > 0) {
                            player.seekTo(playerCurrentPosition);
                            player.setPlayWhenReady(true);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }.extract(url, true, true);
    }

    private MediaSource rtmpMediaSource(Uri uri) {
        MediaSource videoSource = null;
        RtmpDataSourceFactory dataSourceFactory = new RtmpDataSourceFactory();
        videoSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                .createMediaSource(uri);
        return videoSource;
    }

    private MediaSource hlsMediaSource(Uri uri, Context context) {
        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(context,
                Util.getUserAgent(context, "oxoo"), bandwidthMeter);

        MediaSource videoSource = new HlsMediaSource.Factory(dataSourceFactory)
                .createMediaSource(uri);


        return videoSource;
    }

    private MediaSource mediaSource(Uri uri, Context context) {
        return new ExtractorMediaSource.Factory(
                new DefaultHttpDataSourceFactory("exoplayer")).
                createMediaSource(uri);
    }

    public void setSelectedSubtitle(MediaSource mediaSource, String subtitle, Context context) {
        MergingMediaSource mergedSource;
        if (subtitle != null) {
            Uri subtitleUri = Uri.parse(subtitle);

            Format subtitleFormat = Format.createTextSampleFormat(
                    null, // An identifier for the track. May be null.
                    MimeTypes.TEXT_VTT, // The mime type. Must be set correctly.
                    Format.NO_VALUE, // Selection flags for the track.
                    "en"); // The subtitle language. May be null.

            DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(context,
                    Util.getUserAgent(context, CLASS_NAME), new DefaultBandwidthMeter());


            MediaSource subtitleSource = new SingleSampleMediaSource
                    .Factory(dataSourceFactory)
                    .createMediaSource(subtitleUri, subtitleFormat, C.TIME_UNSET);


            mergedSource = new MergingMediaSource(mediaSource, subtitleSource);
            player.prepare(mergedSource, false, false);
            player.setPlayWhenReady(true);
            //resumePlayer();

        } else {
            Toast.makeText(context, "there is no subtitle", Toast.LENGTH_SHORT).show();
        }
    }

    private void addToFav(String type) {
        Retrofit retrofit = RetrofitClient.getAuthRetrofitInstance();
        FavouriteApi api = retrofit.create(FavouriteApi.class);
        Call<FavoriteModel> call = api.addToFavorite(AppConfig.API_KEY, userId, id, type);
        call.enqueue(new Callback<FavoriteModel>() {
            @Override
            public void onResponse(Call<FavoriteModel> call, retrofit2.Response<FavoriteModel> response) {
                if (response.code() == 200) {
                    if (response.body().getStatus().equalsIgnoreCase("success")) {
                        new ToastMsg(DetailsActivity.this).toastIconSuccess(response.body().getMessage());
                        if (type.equals(Constants.WishListType.watch_later)) {
                            isWatchLater = true;
                            imgWatchList.setColorFilter(ContextCompat.getColor(DetailsActivity.this, R.color.paypalColor), android.graphics.PorterDuff.Mode.SRC_IN);
                        } else {
                            isFav = true;
                            imgAddFav.setImageResource(R.drawable.ic_circle_fav);
                            favIv.setImageResource(R.drawable.ic_circle_fav);
                        }
                    } else {
                        new ToastMsg(DetailsActivity.this).toastIconError(response.body().getMessage());
                    }
                } else {
                    new ToastMsg(DetailsActivity.this).toastIconError(getString(R.string.error_toast));
                }
            }

            @Override
            public void onFailure(Call<FavoriteModel> call, Throwable t) {
                new ToastMsg(DetailsActivity.this).toastIconError(getString(R.string.error_toast));

            }
        });
    }

    private void paidControl(String isPaid, boolean isPayAndWatch) {
        if (isPaid.equals("1")) {
            if (PreferenceUtils.isLoggedIn(DetailsActivity.this)) {
                if (isPayAndWatch) {
                    contentDetails.setVisibility(VISIBLE);
                    subscriptionLayout.setVisibility(GONE);
                } else {
                    if (PreferenceUtils.isActivePlan(DetailsActivity.this)) {
                        if (PreferenceUtils.isValid(DetailsActivity.this)) {
                            contentDetails.setVisibility(VISIBLE);
                            subscriptionLayout.setVisibility(GONE);
                        } else {
                            PreferenceUtils.updateSubscriptionStatus(DetailsActivity.this);
                        }
                    } else {
                        contentDetails.setVisibility(GONE);
                        subscriptionLayout.setVisibility(VISIBLE);
                        releasePlayer();
                        return;
                    }
                }

            } else {
                startActivity(new Intent(DetailsActivity.this, LoginActivity.class));
                finish();
            }

        } else {
            //free content
            contentDetails.setVisibility(VISIBLE);
            subscriptionLayout.setVisibility(GONE);
        }
    }

    private void getActiveStatus(String userId) {
        Retrofit retrofit = RetrofitClient.getAuthRetrofitInstance();
        SubscriptionApi subscriptionApi = retrofit.create(SubscriptionApi.class);

        Call<ActiveStatus> call = subscriptionApi.getActiveStatus(AppConfig.API_KEY, userId);
        call.enqueue(new Callback<ActiveStatus>() {
            @Override
            public void onResponse(Call<ActiveStatus> call, retrofit2.Response<ActiveStatus> response) {
                ActiveStatus activeStatus = response.body();
                if (!activeStatus.getStatus().equals("active")) {
                    contentDetails.setVisibility(GONE);
                    subscriptionLayout.setVisibility(VISIBLE);
                } else {
                    contentDetails.setVisibility(VISIBLE);
                    subscriptionLayout.setVisibility(GONE);
                }

                PreferenceUtils.updateSubscriptionStatus(DetailsActivity.this);
            }

            @Override
            public void onFailure(Call<ActiveStatus> call, Throwable t) {
                t.printStackTrace();
            }
        });

    }

    private void getTvData(final String vtype, final String vId) {
        Retrofit retrofit = RetrofitClient.getAuthRetrofitInstance();
        SingleDetailsTVApi api = retrofit.create(SingleDetailsTVApi.class);
        Call<SingleDetailsTV> call = api.getSingleDetails(AppConfig.API_KEY, vtype, vId);
        call.enqueue(new Callback<SingleDetailsTV>() {
            @Override
            public void onResponse(Call<SingleDetailsTV> call, retrofit2.Response<SingleDetailsTV> response) {
                if (response.code() == 200) {
                    Log.d(TAG, "onResponse: " + response);
                    if (response.body() != null) {
                        mainContentLayout.animate().alpha(1f);
                        swipeRefreshLayout.setRefreshing(false);
                        progressImgView.setVisibility(GONE);
                        shimmerFrameLayout.stopShimmer();
                        shimmerFrameLayout.setVisibility(GONE);
                        if (response.body().getIsPaid().equalsIgnoreCase("1")) {
                            paidControl(response.body().getIsPaid(), false);
                        }

                        SingleDetailsTV detailsModel = response.body();

                        title = detailsModel.getTvName();
                        tvName.setText(title);
                        tvName.setVisibility(GONE);
                        tvTitleTv.setText(title);

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            tvDes.setText(Html.fromHtml(detailsModel.getDescription(), Html.FROM_HTML_MODE_COMPACT));
                        } else {
                            tvDes.setText(Html.fromHtml(detailsModel.getDescription(), null,
                                    new HtmlTagHelper()));
                        }
                        V_URL = detailsModel.getStreamUrl();
                        castImageUrl = detailsModel.getThumbnailUrl();

                        Glide.with(getApplicationContext())
                                .load(detailsModel.getThumbnailUrl())
                                .placeholder(R.drawable.album_art_placeholder)
                                .error(R.drawable.poster_placeholder)
                                .into(tvThumbIv);

                        listServer.clear();
                        CommonModels model = new CommonModels();
                        model.setTitle("HD");
                        model.setStremURL(V_URL);
                        model.setServerType(detailsModel.getStreamFrom());
                        listServer.add(model);

                        initMoviePlayer(detailsModel.getStreamUrl(), detailsModel.getStreamFrom(), DetailsActivity.this);

                        currentProgramTime = detailsModel.getCurrentProgramTime();
                        currentProgramTitle = detailsModel.getCurrentProgramTitle();

                        timeTv.setText(currentProgramTime);
                        programTv.setText(currentProgramTitle);
                        if (PreferenceUtils.isProgramGuideEnabled(DetailsActivity.this)) {
                            List<ProgramGuide> programGuideList = response.body().getProgramGuide();
                            for (int i = 0; i < programGuideList.size(); i++) {
                                ProgramGuide programGuide = programGuideList.get(i);
                                Program program = new Program();
                                program.setId(programGuide.getId());
                                program.setTitle(programGuide.getTitle());
                                program.setProgramStatus(programGuide.getProgramStatus());
                                program.setTime(programGuide.getTime());
                                program.setVideoUrl(programGuide.getVideoUrl());

                                programs.add(program);
                            }

                            if (programs.size() <= 0) {
                                proGuideTv.setVisibility(GONE);
                                programRv.setVisibility(GONE);
                            } else {
                                proGuideTv.setVisibility(VISIBLE);
                                programRv.setVisibility(VISIBLE);
                                programAdapter.notifyDataSetChanged();
                            }
                        }
                        //all tv channel data
                        List<AllTvChannel> allTvChannelList = response.body().getAllTvChannel();
                        for (int i = 0; i < allTvChannelList.size(); i++) {
                            AllTvChannel allTvChannel = allTvChannelList.get(i);
                            CommonModels models = new CommonModels();
                            models.setImageUrl(allTvChannel.getPosterUrl());
                            models.setTitle(allTvChannel.getTvName());
                            models.setVideoType("tv");
                            models.setIsPaid(allTvChannel.getIsPaid());
                            models.setId(allTvChannel.getLiveTvId());
                            listRelated.add(models);
                        }
                        if (listRelated.size() == 0) {
                            tvRelated.setVisibility(GONE);
                        }
                        relatedTvAdapter.notifyDataSetChanged();

                        //additional media source data
                        List<AdditionalMediaSource> serverArray = response.body().getAdditionalMediaSource();
                        for (int i = 0; i < serverArray.size(); i++) {
                            AdditionalMediaSource jsonObject = serverArray.get(i);
                            CommonModels models = new CommonModels();
                            models.setTitle(jsonObject.getLabel());
                            models.setStremURL(jsonObject.getUrl());
                            models.setServerType(jsonObject.getSource());
                            listServer.add(models);
                        }
                        serverAdapter.notifyDataSetChanged();
                        //validateForContinueWatching();
                    }
                }
            }

            @Override
            public void onFailure(@NotNull Call<SingleDetailsTV> call, @NotNull Throwable t) {
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void getSeriesData(String vtype, String vId) {
        Log.e(TAG, "getSeriesData: " + vId + ", userId: " + userId);
        final List<String> seasonList = new ArrayList<>();
        final List<String> seasonListForDownload = new ArrayList<>();
        Retrofit retrofit = RetrofitClient.getAuthRetrofitInstance();
        SingleDetailsApi api = retrofit.create(SingleDetailsApi.class);
        Call<SingleDetails> call = api.getSingleDetails(AppConfig.API_KEY, vtype, vId, PreferenceUtils.isLoggedIn(this) ? PreferenceUtils.getUserId(this) : "0");
        call.enqueue(new Callback<SingleDetails>() {
            @Override
            public void onResponse(@NotNull Call<SingleDetails> call, @NotNull retrofit2.Response<SingleDetails> response) {
                if (response.code() == 200) {
                    mainContentLayout.animate().alpha(1f);
                    swipeRefreshLayout.setRefreshing(false);
                    progressImgView.setVisibility(GONE);
                    shimmerFrameLayout.stopShimmer();
                    shimmerFrameLayout.setVisibility(GONE);

                    singleDetails = response.body();
                    if (singleDetails.showAds()) {
                        customAddsModelArrayList.addAll(singleDetails.getCustomAddsModelList());
                    }
                    String isPaid = singleDetails.getIsPaid();
                    paidControl(isPaid, false);

                    title = singleDetails.getTitle();
                    sereisTitleTv.setText(title);
                    castImageUrl = singleDetails.getThumbnailUrl();
                    tvName.setText(title);
                    tvRelease.setText(singleDetails.getRelease());
                    tvDes.setText(singleDetails.getDescription());

                    Glide.with(getApplicationContext())
                            .load(singleDetails.getThumbnailUrl())
                            .placeholder(R.drawable.album_art_placeholder_large)
                            .error(R.drawable.album_art_placeholder_large)
                            .into(posterIv);
                    Picasso.get().load(singleDetails.getThumbnailUrl()).placeholder(R.drawable.poster_placeholder)
                            .into(thumbIv);

                    download_check = singleDetails.getEnableDownload();
                    trailerUrl = singleDetails.getTrailerUrl();
                    trailer_aws_source = singleDetails.getTrailer_aws_source();
                    castImageUrl = singleDetails.getThumbnailUrl();
                    downloadBt.setVisibility(GONE);
                    trailerBt.setVisibility(GONE);
                    downloadAndTrailerBtContainer.setVisibility(GONE);
                    if (singleDetails.getPosterUrl() != null && !singleDetails.getPosterUrl().equalsIgnoreCase("")) {
                        downloadAndTrailerBtContainer.setVisibility(VISIBLE);
                        trailerBt.setVisibility(VISIBLE);
                        trailerImgView.setVisibility(VISIBLE);
                        downloadBt.setVisibility(GONE);
                        loadTrailerThumbnail(singleDetails.getPosterUrl());
                    } else {
                        trailerImgView.setVisibility(GONE);
                        trailerTitleView.setVisibility(GONE);
                    }

                    //----director---------------
                    for (int i = 0; i < singleDetails.getDirector().size(); i++) {
                        Director director = singleDetails.getDirector().get(i);
                        if (i == singleDetails.getDirector().size() - 1) {
                            strDirector = strDirector + director.getName();
                        } else {
                            strDirector = strDirector + director.getName() + ", ";
                        }
                    }
                    tvDirector.setText(strDirector);

                    //----cast---------------
                    castCrews.clear();
                    for (int i = 0; i < singleDetails.getCast().size(); i++) {
                        Cast cast = singleDetails.getCast().get(i);
                        CastCrew castCrew = new CastCrew();
                        castCrew.setId(cast.getStarId());
                        castCrew.setName(cast.getName());
                        castCrew.setUrl(cast.getUrl());
                        castCrew.setImageUrl(cast.getImageUrl());
                        castCrews.add(castCrew);
                    }
                    castCrewAdapter.notifyDataSetChanged();
                    //---genre---------------
                    for (int i = 0; i < singleDetails.getGenre().size(); i++) {
                        Genre genre = singleDetails.getGenre().get(i);
                        if (i == singleDetails.getCast().size() - 1) {
                            strGenre = strGenre + genre.getName();
                        } else {
                            if (i == singleDetails.getGenre().size() - 1) {
                                strGenre = strGenre + genre.getName();
                            } else {
                                strGenre = strGenre + genre.getName() + ", ";
                            }
                        }
                    }
                    setGenreText();

                    //----related tv series---------------
                    for (int i = 0; i < singleDetails.getRelatedTvseries().size(); i++) {
                        RelatedMovie relatedTvSeries = singleDetails.getRelatedTvseries().get(i);

                        CommonModels models = new CommonModels();
                        models.setTitle(relatedTvSeries.getTitle());
                        models.setImageUrl(relatedTvSeries.getThumbnailUrl());
                        models.setId(relatedTvSeries.getVideosId());
                        models.setVideoType("tvseries");
                        models.setIsPaid(relatedTvSeries.getIsPaid());
                        models.setIsPaid(relatedTvSeries.getIsPaid());
                        models.setVideo_view_type(relatedTvSeries.getVideo_view_type());
                        listRelated.add(models);
                    }

                    if (listRelated.size() == 0) {
                        tvRelated.setVisibility(GONE);
                    }
                    relatedAdapter.notifyDataSetChanged();

                    listServer.clear();
                    //----season and episode download------------
                    for (int i = 0; i < singleDetails.getSeason().size(); i++) {
                        Season season = singleDetails.getSeason().get(i);
                        CommonModels models = new CommonModels();
                        String season_name = season.getSeasonsName();
                        models.setTitle(season.getSeasonsName());
                        seasonList.add("Season: " + season.getSeasonsName());
                        seasonListForDownload.add(season.getSeasonsName());

                        //----episode------
                        List<EpiModel> epList = new ArrayList<>();
                        for (int j = 0; j < singleDetails.getSeason().get(i).getEpisodes().size(); j++) {
                            Episode episode = singleDetails.getSeason().get(i).getEpisodes().get(j);

                            EpiModel model = new EpiModel();
                            model.setSeson(season_name);
                            model.setEpi(episode.getEpisodesName());
                            model.setStreamURL(episode.getFileUrl());
                            model.setServerType(episode.getFileType());
                            model.setImageUrl(episode.getImageUrl());
                            model.setSubtitleList(episode.getSubtitle());
                            epList.add(model);
                        }
                        models.setListEpi(epList);
                        listServer.add(models);
                    }

                    if (!listServer.isEmpty() && !listServer.get(0).getListEpi().isEmpty()) {
                        watchNowBt.setText(R.string.play_episode);
                        watchNowBt.setVisibility(VISIBLE);
                        downloadAndTrailerBtContainer.setVisibility(VISIBLE);
                    }

                     /*if season downloads are enable
                        generate a list of downloads of every season*/
                    //----download list--------
                    if (seasonList.size() > 0) {
                        setSeasonData(seasonList);
                        //check if download is enabled
                        if (singleDetails.getEnableDownload().equalsIgnoreCase("1")) {
                            setSeasonDownloadData(seasonListForDownload, singleDetails.getSeason());
                        } else {
                            seasonDownloadLayout.setVisibility(GONE);
                        }
                    } else {
                        seasonSpinnerContainer.setVisibility(GONE);
                    }
                    //validateForContinueWatching();
                }
            }

            @Override
            public void onFailure(@NotNull Call<SingleDetails> call, @NotNull Throwable t) {
                t.printStackTrace();
            }
        });
    }

    public void setSeasonData(List<String> seasonData) {
        SeasonsAdapter seasonsAdapter = new SeasonsAdapter(this, seasonData);
        seasonRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        seasonsAdapter.setSeasonsAdapterListener(this);
        seasonRecyclerView.setAdapter(seasonsAdapter);
        if (!listServer.isEmpty())
            onSeasonItemClick(0);
    }

    @Override
    public void onSeasonItemClick(int position) {
        rvServer.removeAllViewsInLayout();
        rvServer.setLayoutManager(new LinearLayoutManager(DetailsActivity.this, RecyclerView.HORIZONTAL, false));
        EpisodeAdapter episodeAdapter = new EpisodeAdapter(DetailsActivity.this,
                listServer.get(position).getListEpi());
        rvServer.setAdapter(episodeAdapter);
        episodeAdapter.setOnEmbedItemClickListener(DetailsActivity.this);
    }

    private void setSeasonDownloadData(List<String> seasonListArray, List<Season> seasonList) {
        ArrayAdapter arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, seasonListArray);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        seasonDownloadSpinner.setAdapter(arrayAdapter);

        seasonDownloadSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                List<DownloadLink> selectedSeasonDownloadList = new ArrayList<>();
                selectedSeasonDownloadList.clear();
                selectedSeasonDownloadList.addAll(seasonList.get(position).getDownloadLinks());
                seasonDownloadRecyclerView.removeAllViewsInLayout();
                seasonDownloadRecyclerView.setLayoutManager(new LinearLayoutManager(DetailsActivity.this,
                        RecyclerView.VERTICAL, false));
                EpisodeDownloadAdapter adapter = new EpisodeDownloadAdapter(DetailsActivity.this, selectedSeasonDownloadList, downloadViewModel);
                seasonDownloadRecyclerView.setAdapter(adapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void setGenreText() {
        rvSmallGenre.setAdapter(new SmallGenreAdapter(singleDetails.getGenre()));
    }

    private void getMovieData(String vtype, String vId) {
        strDirector = "";
        strGenre = "";
        Retrofit retrofit = RetrofitClient.getAuthRetrofitInstance();
        SingleDetailsApi api = retrofit.create(SingleDetailsApi.class);
        Call<SingleDetails> call = api.getSingleDetails(AppConfig.API_KEY, vtype, vId, PreferenceUtils.isLoggedIn(this) ? PreferenceUtils.getUserId(this) : "0");
        call.enqueue(new Callback<SingleDetails>() {
            @Override
            public void onResponse(Call<SingleDetails> call, retrofit2.Response<SingleDetails> response) {
                if (response.code() == 200) {
                    mainContentLayout.animate().alpha(1f);
                    progressImgView.setVisibility(GONE);
                    shimmerFrameLayout.stopShimmer();
                    shimmerFrameLayout.setVisibility(GONE);
                    swipeRefreshLayout.setRefreshing(false);

                    singleDetails = response.body();
                    paidControl(singleDetails.getIsPaid(), singleDetails.getVideo_view_type().equalsIgnoreCase("Pay and Watch"));
                    if (appLinkData != null) {
                        if (singleDetails.getVideo_view_type().equalsIgnoreCase("Pay and Watch")) {
                            downloadAndTrailerBtContainer.setVisibility(GONE);
                        }
                    }

                    download_check = singleDetails.getEnableDownload();
                    trailerUrl = singleDetails.getTrailerUrl();
                    trailer_aws_source = singleDetails.getTrailer_aws_source();
                    castImageUrl = singleDetails.getThumbnailUrl();
                    if (singleDetails.showAds()) {
                        customAddsModelArrayList.addAll(singleDetails.getCustomAddsModelList());
                    }
                    if (download_check.equals("1")) {
                        downloadBt.setVisibility(VISIBLE);
                    } else {
                        downloadBt.setVisibility(GONE);
                    }
                    if (singleDetails.getPosterUrl() == null || singleDetails.getPosterUrl().equalsIgnoreCase("")) {
                        trailerBt.setVisibility(GONE);
                        trailerImgView.setVisibility(GONE);
                        trailerTitleView.setVisibility(GONE);
                    } else {
                        loadTrailerThumbnail(singleDetails.getPosterUrl());
                        trailerImgView.setVisibility(VISIBLE);
                        trailerBt.setVisibility(VISIBLE);
                    }
                    //check if download and trailer is unable or not
                    // control button container

                    if (singleDetails.getVideo_view_type().equalsIgnoreCase("Subscription Based")) {
                        payAndWatchBtn.setVisibility(GONE);
                        watchNowBt.setVisibility(VISIBLE);
                    } /*else if (singleDetails.getPreBookingEnabled().equals("true")) {
                        if (singleDetails.getVideoPreBookingSubscriptionStarted().equals("true")) {
                            payAndWatchBtn.setVisibility(GONE);
                            watchNowBt.setVisibility(VISIBLE);
                            payAndWatchBtn.setClickable(true);
                            payWatchExpireStatusView.setVisibility(VISIBLE);
                            payWatchExpireStatusView.setText(String.format("%s %s", getString(R.string.your_movie_subscription_will_expires_in), singleDetails.getExpiryDateTime()));
                            YoYo.with(Techniques.FadeInLeft)
                                    .duration(1500).playOn(payWatchExpireStatusView);
                            if (singleDetails.getIs_rent_expired().equalsIgnoreCase("1")) {
                                payWatchExpireStatusView.setVisibility(VISIBLE);
                                payWatchExpireStatusView.setText(getString(R.string.subscription_expired));
                            }
                        } else if (singleDetails.getIsVideoPreBooked().equals("true")) {
                            payAndWatchBtn.setVisibility(VISIBLE);
                            watchNowBt.setVisibility(GONE);
                            payAndWatchBtn.setText("Already Booked");

                        } else {
                            payAndWatchBtn.setVisibility(VISIBLE);
                            watchNowBt.setVisibility(GONE);
                            payAndWatchBtn.setText(String.format("Pre Booking", singleDetails.getPrice()));
                        }
                    }*/ else if (singleDetails.getVideo_view_type().equalsIgnoreCase("Pay and Watch")) {
                        payAndWatchBtn.setVisibility(VISIBLE);
                        watchNowBt.setVisibility(GONE);
                        if (singleDetails.getIs_subscribed_previously().equalsIgnoreCase("1")) {
                            if (selectedCountry.equals(Constants.BANGLA)) {
                                payAndWatchBtn.setText(String.format("Pay Again and watch for ৳%s", singleDetails.getPrice()));
                            } else {
                                payAndWatchBtn.setText(String.format("Pay Again and watch for $%s", singleDetails.getPrice()));
                            }

                            if (singleDetails.getIs_rent_expired().equalsIgnoreCase("0")) {
                                payAndWatchBtn.setVisibility(GONE);
                                watchNowBt.setVisibility(VISIBLE);
                                payWatchExpireStatusView.setVisibility(VISIBLE);
                                payWatchExpireStatusView.setText(String.format("%s %s", getString(R.string.your_movie_subscription_will_expires_in), singleDetails.getExpiryOn()));
                                YoYo.with(Techniques.FadeInLeft)
                                        .duration(1500).playOn(payWatchExpireStatusView);
                            } else if (singleDetails.getIs_rent_expired().equalsIgnoreCase("1")) {
                                payWatchExpireStatusView.setVisibility(VISIBLE);
                                payWatchExpireStatusView.setText(getString(R.string.subscription_expired));
                            }
                        } else {
                            if (selectedCountry.equals(Constants.BANGLA)) {
                                payAndWatchBtn.setText(String.format("Pay and watch for ৳%s", singleDetails.getPrice()));
                            } else {
                                payAndWatchBtn.setText(String.format("Pay and watch for $%s", singleDetails.getPriceInUsd()));
                            }
                        }
                    } else {
                        watchNowBt.setVisibility(VISIBLE);
                        payAndWatchBtn.setVisibility(GONE);
                    }

                    title = singleDetails.getTitle();


                    tvName.setText(title);
                    tvRelease.setText(singleDetails.getRelease());
                    tvDes.setText(singleDetails.getDescription());

                    Picasso.get().load(singleDetails.getThumbnailUrl()).placeholder(R.drawable.album_art_placeholder_large)
                            .into(posterIv);
                    Picasso.get().load(singleDetails.getThumbnailUrl()).placeholder(R.drawable.poster_placeholder)
                            .into(thumbIv);
                    if (response.body() != null && response.body().getDirector() != null) {
                        for (int i = 0; i < singleDetails.getDirector().size(); i++) {
                            Director director = response.body().getDirector().get(i);
                            if (i == singleDetails.getDirector().size() - 1) {
                                strDirector = strDirector + director.getName();
                            } else {
                                strDirector = strDirector + director.getName() + ", ";
                            }
                        }
                        tvDirector.setText(strDirector);
                    }

                    //----cast---------------
                    castCrews.clear();
                    for (int i = 0; i < singleDetails.getCast().size(); i++) {
                        Cast cast = singleDetails.getCast().get(i);

                        CastCrew castCrew = new CastCrew();
                        castCrew.setId(cast.getStarId());
                        castCrew.setName(cast.getName());
                        castCrew.setUrl(cast.getUrl());
                        castCrew.setImageUrl(cast.getImageUrl());
                        castCrews.add(castCrew);
                    }
                    castCrewAdapter.notifyDataSetChanged();

                    //---genre---------------
                    for (int i = 0; i < singleDetails.getGenre().size(); i++) {
                        Genre genre = singleDetails.getGenre().get(i);
                        if (i == singleDetails.getCast().size() - 1) {
                            strGenre = strGenre + genre.getName();
                        } else {
                            if (i == singleDetails.getGenre().size() - 1) {
                                strGenre = strGenre + genre.getName();
                            } else {
                                strGenre = strGenre + genre.getName() + ", ";
                            }
                        }
                    }
                    setGenreText();

                    //-----server----------
                    List<Video> serverList = new ArrayList<>();
                    serverList.addAll(singleDetails.getVideos());
                    listServer.clear();
                    for (int i = 0; i < serverList.size(); i++) {
                        Video video = serverList.get(i);

                        CommonModels models = new CommonModels();
                        models.setTitle(video.getLabel());
                        models.setStremURL(video.getFileUrl());
                        models.setServerType(video.getFileType());

                        if (video.getFileType().equals("mp4")) {
                            V_URL = video.getFileUrl();
                        }

                        //----subtitle-----------
                        List<Subtitle> subArray = new ArrayList<>();
                        subArray.addAll(singleDetails.getVideos().get(i).getSubtitle());
                        if (subArray.size() != 0) {

                            List<SubtitleModel> list = new ArrayList<>();
                            for (int j = 0; j < subArray.size(); j++) {
                                Subtitle subtitle = subArray.get(j);
                                SubtitleModel subtitleModel = new SubtitleModel();
                                subtitleModel.setUrl(subtitle.getUrl());
                                subtitleModel.setLanguage(subtitle.getLanguage());
                                list.add(subtitleModel);
                            }
                            if (i == 0) {
                                listSub.addAll(list);
                            }
                            models.setListSub(list);
                        } else {
                            models.setSubtitleURL(strSubtitle);
                        }
                        listServer.add(models);
                    }

                    if (serverAdapter != null) {
                        serverAdapter.notifyDataSetChanged();
                    }

                    //----related post---------------
                    for (int i = 0; i < singleDetails.getRelatedMovie().size(); i++) {
                        RelatedMovie relatedMovie = singleDetails.getRelatedMovie().get(i);
                        CommonModels models = new CommonModels();
                        models.setTitle(relatedMovie.getTitle());
                        models.setImageUrl(relatedMovie.getThumbnailUrl());
                        models.setId(relatedMovie.getVideosId());
                        models.setVideoType("movie");
                        models.setIsPaid(relatedMovie.getIsPaid());
                        models.setIsPaid(relatedMovie.getIsPaid());
                        models.setVideo_view_type(relatedMovie.getVideo_view_type());
                        listRelated.add(models);
                    }

                    if (listRelated.size() == 0) {
                        tvRelated.setVisibility(GONE);
                    }
                    relatedAdapter.notifyDataSetChanged();

                    //----download list---------
                    listExternalDownload.clear();
                    listInternalDownload.clear();
                    for (int i = 0; i < singleDetails.getDownloadLinks().size(); i++) {
                        DownloadLink downloadLink = singleDetails.getDownloadLinks().get(i);

                        CommonModels models = new CommonModels();
                        models.setTitle(downloadLink.getLabel());
                        models.setStremURL(downloadLink.getDownloadUrl());
                        models.setFileSize(downloadLink.getFileSize());
                        models.setResulation(downloadLink.getResolution());
                        models.setInAppDownload(downloadLink.isInAppDownload());
                        if (downloadLink.isInAppDownload()) {
                            listInternalDownload.add(models);
                        } else {
                            listExternalDownload.add(models);
                        }
                    }
                    //validateForContinueWatching();
                } else {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }

            @Override
            public void onFailure(Call<SingleDetails> call, Throwable t) {
                t.printStackTrace();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void loadTrailerThumbnail(String posterUrl) {
        Glide.with(getApplicationContext()).load(posterUrl).placeholder(R.drawable.poster_placeholder)
                .centerCrop()
                .apply(new RequestOptions().transform(new RoundedCorners(20)))
                .into(trailerImgView);
        trailerImgView.setOnClickListener(v -> trailerBt.performClick());
    }


    private void getFavStatus() {
        Retrofit retrofit = RetrofitClient.getAuthRetrofitInstance();
        FavouriteApi api = retrofit.create(FavouriteApi.class);
        Call<FavoriteModel> call = api.verifyFavoriteList(AppConfig.API_KEY, userId, id, Constants.WishListType.fav);
        call.enqueue(new Callback<FavoriteModel>() {
            @Override
            public void onResponse(Call<FavoriteModel> call, retrofit2.Response<FavoriteModel> response) {
                if (response.code() == 200) {
                    if (response.body().getStatus().equalsIgnoreCase("success")) {
                        isFav = true;
                        imgAddFav.setImageResource(R.drawable.ic_circle_fav);
                        favIv.setImageResource(R.drawable.ic_circle_fav);
                    } else {
                        isFav = false;
                        imgAddFav.setImageResource(R.drawable.ic_bottom_fav);
                        favIv.setImageResource(R.drawable.ic_bottom_fav);
                    }
                    imgAddFav.setVisibility(VISIBLE);
                    favIv.setVisibility(VISIBLE);
                }
            }

            @Override
            public void onFailure(@NotNull Call<FavoriteModel> call, @NotNull Throwable t) {

            }
        });

    }

    private void getWatchLaterStatus() {
        Retrofit retrofit = RetrofitClient.getAuthRetrofitInstance();
        FavouriteApi api = retrofit.create(FavouriteApi.class);
        Call<FavoriteModel> call = api.verifyFavoriteList(AppConfig.API_KEY, userId, id, Constants.WishListType.watch_later);
        call.enqueue(new Callback<FavoriteModel>() {
            @Override
            public void onResponse(@NotNull Call<FavoriteModel> call, @NotNull retrofit2.Response<FavoriteModel> response) {
                if (response.code() == 200) {
                    if (response.body().getStatus().equalsIgnoreCase("success")) {
                        isWatchLater = true;
                        imgWatchList.setColorFilter(ContextCompat.getColor(DetailsActivity.this, R.color.colorGold), android.graphics.PorterDuff.Mode.SRC_IN);
                    } else {
                        isWatchLater = false;
                        imgWatchList.setColorFilter(ContextCompat.getColor(DetailsActivity.this, R.color.colorHint), android.graphics.PorterDuff.Mode.SRC_IN);
                    }
                }
            }

            @Override
            public void onFailure(@NotNull Call<FavoriteModel> call, @NotNull Throwable t) {

            }
        });

    }

    private void removeFromFav(String type) {
        Retrofit retrofit = RetrofitClient.getAuthRetrofitInstance();
        FavouriteApi api = retrofit.create(FavouriteApi.class);
        Call<FavoriteModel> call = api.removeFromFavorite(AppConfig.API_KEY, userId, id, type);
        call.enqueue(new Callback<FavoriteModel>() {
            @Override
            public void onResponse(Call<FavoriteModel> call, retrofit2.Response<FavoriteModel> response) {
                if (response.code() == 200) {
                    if (response.body().getStatus().equalsIgnoreCase("success")) {
                        new ToastMsg(DetailsActivity.this).toastIconSuccess(response.body().getMessage());
                        if (type.equals(Constants.WishListType.fav)) {
                            isFav = false;
                            imgAddFav.setImageResource(R.drawable.ic_bottom_fav);
                            favIv.setImageResource(R.drawable.ic_bottom_fav);
                        } else {
                            isWatchLater = false;
                            imgWatchList.setColorFilter(ContextCompat.getColor(DetailsActivity.this, R.color.colorHint), android.graphics.PorterDuff.Mode.SRC_IN);
                        }
                    } else {
                        new ToastMsg(DetailsActivity.this).toastIconError(response.body().getMessage());
                        if (type.equals(Constants.WishListType.fav)) {
                            isFav = true;
                            imgAddFav.setImageResource(R.drawable.ic_circle_fav);
                            favIv.setImageResource(R.drawable.ic_circle_fav);
                        } else {
                            isWatchLater = true;
                            imgWatchList.setColorFilter(ContextCompat.getColor(DetailsActivity.this, R.color.colorGold), android.graphics.PorterDuff.Mode.SRC_IN);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<FavoriteModel> call, Throwable t) {
                new ToastMsg(DetailsActivity.this).toastIconError(getString(R.string.fetch_error));
            }
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void addComment(String videoId, String userId, final String comments) {

        Retrofit retrofit = RetrofitClient.getAuthRetrofitInstance();
        CommentApi api = retrofit.create(CommentApi.class);
        Call<PostCommentModel> call = api.postComment(AppConfig.API_KEY, videoId, userId, comments);
        call.enqueue(new Callback<PostCommentModel>() {
            @Override
            public void onResponse(Call<PostCommentModel> call, retrofit2.Response<PostCommentModel> response) {
                if (response.body().getStatus().equals("success")) {
                    rvComment.removeAllViews();
                    listComment.clear();
                    getComments();
                    etComment.setText("");
                    new ToastMsg(DetailsActivity.this).toastIconSuccess(response.body().getMessage());
                } else {
                    new ToastMsg(DetailsActivity.this).toastIconError(response.body().getMessage());
                }
            }

            @Override
            public void onFailure(Call<PostCommentModel> call, Throwable t) {

            }
        });
    }

    private void getComments() {
        Retrofit retrofit = RetrofitClient.getAuthRetrofitInstance();
        CommentApi api = retrofit.create(CommentApi.class);
        Call<List<GetCommentsModel>> call = api.getAllComments(AppConfig.API_KEY, id);
        call.enqueue(new Callback<List<GetCommentsModel>>() {
            @Override
            public void onResponse(Call<List<GetCommentsModel>> call, retrofit2.Response<List<GetCommentsModel>> response) {
                if (response.code() == 200) {
                    listComment.addAll(response.body());
                    commentsAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<GetCommentsModel>> call, Throwable t) {

            }
        });

    }

    public void hideDescriptionLayout() {
        lPlay.setVisibility(VISIBLE);
        descriptionLayout.setVisibility(GONE);
    }

    public void showSeriesLayout() {
        seriestLayout.setVisibility(VISIBLE);
    }

    public void showDescriptionLayout() {
        descriptionLayout.setVisibility(VISIBLE);
        lPlay.setVisibility(GONE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isPlaying && player != null) {
            player.setPlayWhenReady(false);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        updateContinueWatchingDataToServer();
        resetCastPlayer();
        releasePlayer();
    }

    @Override
    public void onBackPressed() {
        if (adsCheckTimer != null) {
            adsCheckTimer.cancel();
            adsCheckTimer = null;

        }
        if (activeMovie) {
            setPlayerNormalScreen();
            if (player != null) {
                player.setPlayWhenReady(false);
                player.stop();
            }
            if (player_v2 != null) {
                player_v2.setPlayWhenReady(false);
                player_v2.stop();
            }
            showDescriptionLayout();
            activeMovie = false;
            updateContinueWatchingDataToServer();
        } else {
            releasePlayer();
            finish();
            overridePendingTransition(R.anim.pop_enter, R.anim.pop_exit);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //check vpn connection
        helperUtils = new HelperUtils(DetailsActivity.this);
        vpnStatus = helperUtils.isVpnConnectionAvailable();
        if (vpnStatus && player != null) {
            helperUtils.showWarningDialog(DetailsActivity.this, getString(R.string.vpn_detected), getString(R.string.close_vpn));
            player.setPlayWhenReady(false);
        }
    }

    public void releasePlayer() {
        try {
            if (player != null) {
                player.setPlayWhenReady(false);
                player.stop();
                player.release();
                player = null;
                simpleExoPlayerView.setPlayer(null);
                //simpleExoPlayerView = null;
            }
            if (player_v2 != null) {
                player_v2.setPlayWhenReady(false);
                player_v2.stop();
                player_v2.release();
                player_v2 = null;
                simpleExoPlayerView_v2.setPlayer(null);
                //simpleExoPlayerView_v2 = null;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setMediaUrlForTvSeries(String url, String season, String episod) {
        mediaUrl = url;
    }

    public boolean getCastSession() {
        return castSession;
    }

    public void resetCastPlayer() {
        if (castPlayer != null) {
            castPlayer.setPlayWhenReady(false);
            castPlayer.release();
        }
    }

    public void showQueuePopup(final Context context, View view, final MediaInfo mediaInfo) {
        CastSession castSession =
                CastContext.getSharedInstance(context).getSessionManager().getCurrentCastSession();
        if (castSession == null || !castSession.isConnected()) {
            Log.w(TAG, "showQueuePopup(): not connected to a cast device");
            return;
        }
        final RemoteMediaClient remoteMediaClient = castSession.getRemoteMediaClient();
        if (remoteMediaClient == null) {
            Log.w(TAG, "showQueuePopup(): null RemoteMediaClient");
            return;
        }
        MediaQueueItem queueItem = new MediaQueueItem.Builder(mediaInfo).setAutoplay(
                true).setPreloadTime(PRELOAD_TIME_S).build();
        MediaQueueItem[] newItemArray = new MediaQueueItem[]{queueItem};
        remoteMediaClient.queueLoad(newItemArray, 0,
                MediaStatus.REPEAT_MODE_REPEAT_OFF, null);

    }

    public void playNextCast(MediaInfo mediaInfo) {

        //simpleExoPlayerView.setPlayer(castPlayer);
        simpleExoPlayerView.setUseController(false);
        castControlView.setVisibility(VISIBLE);
        castControlView.setPlayer(castPlayer);
        //simpleExoPlayerView.setDefaultArtwork();
        castControlView.addVisibilityListener(new PlaybackControlView.VisibilityListener() {
            @Override
            public void onVisibilityChange(int visibility) {
                if (visibility == GONE) {
                    castControlView.setVisibility(VISIBLE);
                    chromeCastTv.setVisibility(VISIBLE);
                }
            }
        });
        CastSession castSession =
                CastContext.getSharedInstance(this).getSessionManager().getCurrentCastSession();

        if (castSession == null || !castSession.isConnected()) {
            Log.w(TAG, "showQueuePopup(): not connected to a cast device");
            return;
        }

        final RemoteMediaClient remoteMediaClient = castSession.getRemoteMediaClient();

        if (remoteMediaClient == null) {
            Log.w(TAG, "showQueuePopup(): null RemoteMediaClient");
            return;
        }
        MediaQueueItem queueItem = new MediaQueueItem.Builder(mediaInfo).setAutoplay(
                true).setPreloadTime(PRELOAD_TIME_S).build();
        MediaQueueItem[] newItemArray = new MediaQueueItem[]{queueItem};

        remoteMediaClient.queueLoad(newItemArray, 0,
                MediaStatus.REPEAT_MODE_REPEAT_OFF, null);
        castPlayer.setPlayWhenReady(true);

    }

    public MediaInfo getMediaInfo() {
        MediaMetadata movieMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);
        movieMetadata.putString(MediaMetadata.KEY_TITLE, title);
        //movieMetadata.putString(MediaMetadata.KEY_ALBUM_ARTIST, "Test Artist");
        movieMetadata.addImage(new WebImage(Uri.parse(castImageUrl)));
        MediaInfo mediaInfo = new MediaInfo.Builder(mediaUrl)
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setContentType(MimeTypes.VIDEO_UNKNOWN)
                .setMetadata(movieMetadata).build();

        return mediaInfo;

    }


    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(this, "Write External Storage permission allows us to do store images. Please allow this permission in App Settings.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                new ToastMsg(DetailsActivity.this).toastIconSuccess("Now You can download.");
                Log.e("value", "Permission Granted, Now you can use local drive .");
            } else {
                Log.e("value", "Permission Denied, You cannot use local drive .");
            }
        }
    }

    public void hideExoControlForTv() {
        exoRewind.setVisibility(GONE);
        exoForward.setVisibility(GONE);
        liveTv.setVisibility(VISIBLE);
        seekbarLayout.setVisibility(GONE);
    }

    public void showExoControlForTv() {
        exoRewind.setVisibility(VISIBLE);
        exoForward.setVisibility(VISIBLE);
        liveTv.setVisibility(GONE);
        seekbarLayout.setVisibility(VISIBLE);
        watchLiveTv.setVisibility(VISIBLE);
        liveTv.setVisibility(GONE);
        watchStatusTv.setText(getResources().getString(R.string.watching_catch_up_tv));
    }

    private void getScreenSize() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        sWidth = size.x;
        //Toast.makeText(this, "fjiaf", Toast.LENGTH_SHORT).show();
    }

    public class RelativeLayoutTouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:

                    //touch is start
                    downX = event.getX();
                    downY = event.getY();
                    if (event.getX() < (sWidth / 2)) {

                        //here check touch is screen left or right side
                        intLeft = true;
                        intRight = false;

                    } else if (event.getX() > (sWidth / 2)) {

                        //here check touch is screen left or right side
                        intLeft = false;
                        intRight = true;
                    }
                    break;

                case MotionEvent.ACTION_UP:

                case MotionEvent.ACTION_MOVE:

                    //finger move to screen
                    float y2 = event.getY();

                    long diffX = (long) (Math.ceil(event.getX() - downX));
                    long diffY = (long) (Math.ceil(event.getY() - downY));

                    if (Math.abs(diffY) > Math.abs(diffX)) {
                        if (intLeft) {
                            //if left its for brightness

                            if (downY < y2) {
                                //down swipe brightness decrease
                            } else if (downY > y2) {
                                //up  swipe brightness increase
                            }

                        } else if (intRight) {

                            //if right its for audio
                            if (downY < y2) {
                                //down swipe volume decrease
                                mAudioManager.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND);

                            } else if (downY > y2) {
                                //up  swipe volume increase
                                mAudioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND);
                            }
                        }
                    }
            }
            return true;
        }


    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 5002 && resultCode == RESULT_OK) {
            clear_previous();
            initGetData();
        }
    }

    private void validateForContinueWatching() {
        if (singleDetails != null) {
            mediaUrl = singleDetails.getVideos().get(0).getFileUrl();
            serverType = singleDetails.getVideos().get(0).getFileType();
            if (getIntent().hasExtra(POSITION) && mediaUrl != null) {
                releasePlayer();
                resetCastPlayer();
                setPlayerFullScreen();
                progressBar.setVisibility(VISIBLE);
                swipeRefreshLayout.setVisibility(GONE);
                lPlay.setVisibility(VISIBLE);
                imgFull.setVisibility(GONE);

                String pos = getIntent().getStringExtra(POSITION);
                if (pos != null) {
                    playerCurrentPosition = Long.parseLong(pos) * 1000;
                }
                preparePlayer(listServer.get(0));
            }
        }
    }

    private LoadControl getLoadController() {
        return new DefaultLoadControl.Builder()
                .setAllocator(new DefaultAllocator(true, 16))
                .setBufferDurationsMs(VideoPlayerConfig.MIN_BUFFER_DURATION,
                        VideoPlayerConfig.MAX_BUFFER_DURATION,
                        VideoPlayerConfig.MIN_PLAYBACK_START_BUFFER,
                        VideoPlayerConfig.MIN_PLAYBACK_RESUME_BUFFER)
                .setTargetBufferBytes(-1)
                .setPrioritizeTimeOverSizeThresholds(true).createDefaultLoadControl();
    }
}
