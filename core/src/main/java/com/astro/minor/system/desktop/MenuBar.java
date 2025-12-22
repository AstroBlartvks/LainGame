package com.astro.minor.system.desktop;

import com.astro.minor.core.ServiceLocator;
import com.astro.minor.core.config.AppConfig;
import com.astro.minor.ui.Button;
import com.astro.minor.system.apps.terminal.commands.Start;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class MenuBar {
    private final SpriteBatch batch;
    private final Texture menubarTexture;
    private final BitmapFont font = new BitmapFont();
    private final GlyphLayout layout;
    private float[] sizeDate;
    private float[] sizeTime;
    private final ShapeRenderer shapeRenderer;

    private final Button commandButton = new Button(15, 10, 35, 35, "");
    private final Button startTerminalButton = new Button(65, 10, 35, 35, "");
    private final Button startExplorerButton = new Button(115, 10, 35, 35, "");

    public MenuBar(SpriteBatch batch, GlyphLayout layout, ShapeRenderer shapeRenderer) {
        this.batch = batch;
        this.layout = layout;
        this.shapeRenderer = shapeRenderer;

        layout.setText(font, "88.88.8888");
        sizeDate = new float[]{layout.width, layout.height};

        layout.setText(font, "88.88.88");
        sizeTime = new float[]{layout.width, layout.height};

        menubarTexture = new Texture(Gdx.files.internal("menubar.png"));
        Texture iconButton = new Texture(Gdx.files.internal("logo.png"));
        Texture cmdTexture = new Texture(Gdx.files.internal("terminal.png"));
        Texture explorerTexture = new Texture(Gdx.files.internal("explorer.png"));

        commandButton.setIconButton(true);
        commandButton.setBackgroundIcon(iconButton, 40, 40);
        commandButton.setClickListener(btn -> {
            System.exit(0);
        });

        startTerminalButton.setIconButton(true);
        startTerminalButton.setBackgroundIcon(cmdTexture, 40, 40);
        startTerminalButton.setClickListener(btn -> {
            new Start().execute(new String[]{"console"});
        });

        startExplorerButton.setIconButton(true);
        startExplorerButton.setBackgroundIcon(explorerTexture, 40, 40);
        startExplorerButton.setClickListener(btn -> {
            new Start().execute(new String[]{"explorer"});
        });
    }

    public void render(float screenWidth, float screenHeight) {
        batch.draw(menubarTexture, 0, 0, screenWidth, 52f * screenHeight/1080f);

        Calendar cal1998 = new GregorianCalendar(1998, Calendar.SEPTEMBER, 28);
        Calendar now = Calendar.getInstance();
        cal1998.set(Calendar.HOUR_OF_DAY, now.get(Calendar.HOUR_OF_DAY));
        cal1998.set(Calendar.MINUTE, now.get(Calendar.MINUTE));
        cal1998.set(Calendar.SECOND, now.get(Calendar.SECOND));

        String timeStamp = new SimpleDateFormat("HH:mm:ss dd.MM.yyyy").format(cal1998.getTime());

        font.draw(batch, timeStamp.split(" ")[0], screenWidth - sizeTime[0] - 15, 48f * screenHeight/2160f + 1.5f * sizeTime[1] + 1);
        font.draw(batch, timeStamp.split(" ")[1], screenWidth - sizeTime[0] - 30, 48f * screenHeight/2160f - sizeTime[1]/2 + 3);

        commandButton.update(0);
        commandButton.render(batch, shapeRenderer);
        startTerminalButton.update(0);
        startTerminalButton.render(batch, shapeRenderer);
        startExplorerButton.update(0);
        startExplorerButton.render(batch, shapeRenderer);
    }

}
