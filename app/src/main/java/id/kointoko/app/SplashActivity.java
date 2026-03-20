package id.kointoko.app;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                             WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);

        ImageView logo    = findViewById(R.id.splash_logo);
        TextView  appName = findViewById(R.id.splash_name);
        TextView  tagline = findViewById(R.id.splash_tagline);

        AlphaAnimation f1 = new AlphaAnimation(0f, 1f); f1.setDuration(700); f1.setFillAfter(true);
        AlphaAnimation f2 = new AlphaAnimation(0f, 1f); f2.setDuration(700); f2.setStartOffset(300); f2.setFillAfter(true);
        AlphaAnimation f3 = new AlphaAnimation(0f, 1f); f3.setDuration(700); f3.setStartOffset(500); f3.setFillAfter(true);
        logo.startAnimation(f1);
        appName.startAnimation(f2);
        tagline.startAnimation(f3);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startActivity(new Intent(this, MainActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }, 2500);
    }
}
