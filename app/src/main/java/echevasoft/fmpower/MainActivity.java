package echevasoft.fmpower;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.audiofx.Visualizer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Menu;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.VideoView;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import  com.google.android.exoplayer2.util.*;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.admanager.AdManagerAdRequest;
import com.google.android.gms.ads.admanager.AdManagerAdView;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.navigation.NavigationView;

import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;


import echevasoft.fmpower.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;

    private String url_radio = "http://stream.zeno.fm/rnkn0bfw8bhvv";
    private AdManagerAdView mAdManagerAdView;

    private ImageView imagen;
    private Button play;

    ExoPlayer Player;
    View animationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);
        Toolbar toolbar = findViewById(R.id.toolbar);

        play = (Button) findViewById(R.id.play);
        imagen = (ImageView) findViewById(R.id.imageView);
        animationView=   findViewById(R.id.animationView);
        animationView.setVisibility(View.GONE);
        play.setBackgroundResource(R.drawable.icons_play);

        comprobarconexion();

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        navigationView.getMenu().findItem(R.id.facebook).setOnMenuItemClickListener(item -> {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
            openFacebook();
            return true;
        });

        navigationView.getMenu().findItem(R.id.instagram).setOnMenuItemClickListener(item -> {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
            openInstagram();
            return true;
        });
        navigationView.getMenu().findItem(R.id.nav_share).setOnMenuItemClickListener(item -> {
            share();
            return true;
        });
        navigationView.getMenu().findItem(R.id.nav_qualify).setOnMenuItemClickListener(item -> {
            qualify();
            return true;
        });
        navigationView.getMenu().findItem(R.id.nav_logout).setOnMenuItemClickListener(item -> {
            logout();
            return true;
        });
        play.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (isPlaying()) {
                    Player.stop();
                    play.setBackgroundResource(R.drawable.icons_play);
                    animationView.setVisibility(View.GONE);
                } else {
                    iniciar();
                    play.setBackgroundResource(R.drawable.icons_pause);
                    animationView.setVisibility(View.VISIBLE);

                }
            }

        });


        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        mAdManagerAdView = findViewById(R.id.adManagerAdView);
        AdManagerAdRequest adRequest = new AdManagerAdRequest.Builder().build();
        mAdManagerAdView.loadAd(adRequest);

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }



    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public void iniciar() {
        try {
            BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
            TrackSelector trackSelector = new DefaultTrackSelector(new AdaptiveTrackSelection.Factory(bandwidthMeter));
            Player = ExoPlayerFactory.newSimpleInstance(this, trackSelector);

            Uri videoURI = Uri.parse(url_radio);
            DefaultHttpDataSourceFactory dataSourceFactory = new DefaultHttpDataSourceFactory("exoplayer_video");
            ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
            MediaSource mediaSource = new ExtractorMediaSource(videoURI, dataSourceFactory, extractorsFactory, null, null);
            Player.prepare(mediaSource);
            Player.setPlayWhenReady(true);
            animationView.setVisibility(View.VISIBLE);
            play.setBackgroundResource(R.drawable.icons_pause);
        } catch (Exception e) {
            Log.e("error", " error " + e.toString());
        }

    }

    // REPROD EN 2DO PLANO
    protected void onDestroy() {
        super.onDestroy();

    }

    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("¿Quieres salir de la App?");
        builder.setPositiveButton("Cerrar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
                System.exit(0);
            }
        });
        builder.setNegativeButton("Minimizar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent("android.intent.action.MAIN");
                intent.addCategory("android.intent.category.HOME");
                MainActivity.this.startActivity(intent);
            }
        });
        builder.show();
    }



    private boolean isPlaying() {
        return Player != null && Player.getPlaybackState() != Player.STATE_ENDED &&
                Player.getPlaybackState() != Player.STATE_IDLE && Player.getPlayWhenReady();

    }


    public void comprobarconexion(){

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected() && networkInfo.isAvailable()) {
            iniciar();
        } else {
            alertDialog();
        }

    }
    private void alertDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage("Verifique su conexión a internet y vuelva a intentarlo");
        dialog.setCancelable(false);
        dialog.setPositiveButton("Cancelar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                logout();
            }
        });
        dialog.setNegativeButton("Reintentar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                comprobarconexion();
            }
        });
        final AlertDialog alertDialog = dialog.create();
        alertDialog.getWindow().setGravity(Gravity.CENTER);
        alertDialog.show();

    }
    private void openFacebook(){
        Uri uri = Uri.parse("https://www.facebook.com/Fm-Power-979-104953292076209");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    private  void openInstagram(){
        Uri uri = Uri.parse("https://www.instagram.com/fmpower979/");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }
    private void share() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, "¡Descarga la App de Fm Power 97.9 y lleva la Radio a donde vayas! https://play.google.com/store/apps/details?id=echevasoft.fmpower");
        startActivity(Intent.createChooser(intent, "Compartir con"));
    }

    private void qualify() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(
                "https://play.google.com/store/apps/details?id=echevasoft.fmpower"));
        intent.setPackage("com.android.vending");
        startActivity(intent);
    }

    private void logout() {
        finish();
        System.exit(0);
    }

}
