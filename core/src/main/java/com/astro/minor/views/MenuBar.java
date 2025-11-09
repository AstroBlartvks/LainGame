package com.astro.minor.views;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MenuBar {
    private final SpriteBatch batch;
    private final Texture menubarTexture;
    private final Texture iconButton;
    private final BitmapFont font = new BitmapFont();
    private final GlyphLayout layout;
    private float[] sizeDate;
    private float[] sizeTime;

    public MenuBar(SpriteBatch batch, GlyphLayout layout) {
        this.batch = batch;
        this.layout = layout;
        layout.setText(font, "88.88.8888");
        sizeDate = new float[]{layout.width, layout.height};

        layout.setText(font, "88.88.88");
        sizeTime = new float[]{layout.width, layout.height};

        menubarTexture = new Texture(Gdx.files.internal("menubar.png"));
        iconButton = new Texture(Gdx.files.internal("logo.png"));
    }

    public void render(float screenWidth, float screenHeight) {
        batch.draw(menubarTexture, 0, 0, screenWidth, 52f * screenHeight/1080f);
        batch.draw(iconButton, 5* screenWidth/1920f, 5*screenHeight/1080f, 52f * screenHeight/1080f-5, (52f - 5) * screenHeight/1080f);

        String timeStamp = new SimpleDateFormat("HH:mm:ss dd.MM.yyyy").format(Calendar.getInstance().getTime());

        font.draw(batch, timeStamp.split(" ")[0], screenWidth - sizeTime[0] - 15, 48f * screenHeight/2160f + 1.5f * sizeTime[1]);
        font.draw(batch, timeStamp.split(" ")[1], screenWidth - sizeTime[0] - 30, 48f * screenHeight/2160f - sizeTime[1]/2);
    }

}
