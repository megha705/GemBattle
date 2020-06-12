package com.xrbpowered.android.gembattle.ui;

import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;

import com.xrbpowered.android.gembattle.GemBattle;
import com.xrbpowered.android.gembattle.effects.EffectSet;
import com.xrbpowered.android.gembattle.game.BattlePlayer;
import com.xrbpowered.android.gembattle.game.Gem;
import com.xrbpowered.android.gembattle.game.Board;
import com.xrbpowered.android.zoomui.UIContainer;
import com.xrbpowered.android.zoomui.UIElement;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class GamePane extends UIContainer {

	public static final int targetWidth = 1680;
	public static final int targetHeight = 1080;

	public final BoardPane boardPane;
	public final ProgressBar turnTimerProgress;
	public final BattlePlayerPane humanPlayerPane;
	public final BattlePlayerPane aiPlayerPane;
	public final GlassButton pauseButton;
	public final GlassButton skipButton;
	public final UIElement attackEffectPane;

	public boolean paused = false;

	private final LinearGradient bgFill = new LinearGradient(0, 0, targetWidth, 0,
			new int[] {0xff373833, 0xff000000, 0xff373833},
			new float[] {0f, 0.5f, 1f},
			Shader.TileMode.CLAMP);

	private long prevt = 0L;

	public GamePane(UIContainer parent) {
		super(parent);
		GemBattle.particles = new EffectSet();
		GemBattle.attackEffects = new EffectSet();

		boardPane = new BoardPane(this, new Board());

		humanPlayerPane = new BattlePlayerPane(this, boardPane.board.humanPlayer);
		aiPlayerPane = new BattlePlayerPane(this, boardPane.board.aiPlayer);

		pauseButton = new GlassButton(this, "Pause") {
			@Override
			public void onClick() {
				paused = !paused;
				prevt = System.currentTimeMillis();
			}
		};
		pauseButton.setSize(350, pauseButton.getHeight());
		skipButton = new GlassButton(this, "Skip") {
			@Override
			public boolean isEnabled() {
				return boardPane.isActive();
			}
			@Override
			public void onClick() {
				boardPane.skip();
			}
		};
		skipButton.setSize(350, skipButton.getHeight());

		turnTimerProgress = new ProgressBar(this, 0xff77ff00) {
			@Override
			public float getProgress() {
				return boardPane.board.getTurnTimer()/Board.turnTime;
			}
			@Override
			public String getText() {
				return Integer.toString((int) boardPane.board.getTurnTimer());
			}
			@Override
			public void paint(Canvas canvas) {
				if(boardPane.board.player.human)
					super.paint(canvas);
			}
		};

		attackEffectPane = new UIElement(this) {
			@Override
			public void paint(Canvas canvas) {
				GemBattle.particles.draw(canvas, paint);
				GemBattle.attackEffects.draw(canvas, paint);
			}
		};

		humanPlayerPane.damageText = new DamageTextFloat(this);
		aiPlayerPane.damageText = new DamageTextFloat(this);

		GemBattle.popupMessageFloat = new PopupMessageFloat(this);
	}

	public BattlePlayerPane getPlayerPane(BattlePlayer player) {
		if(humanPlayerPane.player==player)
			return humanPlayerPane;
		else if(aiPlayerPane.player==player)
			return aiPlayerPane;
		else
			return null;
	}

	@Override
	public void layout() {
		boardPane.setLocation((getWidth()-boardPane.getWidth())/2, BoardPane.marginTop);

		humanPlayerPane.setLocation(10, 10);
		humanPlayerPane.damageText.setLocation(humanPlayerPane.getX()+humanPlayerPane.getWidth()/2, humanPlayerPane.getY()+360);
		aiPlayerPane.setLocation(getWidth()-aiPlayerPane.getWidth()-10, 10);
		aiPlayerPane.damageText.setLocation(aiPlayerPane.getX()+aiPlayerPane.getWidth()/2, aiPlayerPane.getY()+360);

		pauseButton.setLocation(10, 1020-pauseButton.getHeight()/2);
		skipButton.setLocation(getWidth()-skipButton.getWidth()-10, pauseButton.getY());

		turnTimerProgress.setSize(boardPane.getWidth()+12, turnTimerProgress.getHeight());
		turnTimerProgress.setLocation(boardPane.getX()-6, boardPane.getY()/2 - turnTimerProgress.getHeight()/2);

		GemBattle.popupMessageFloat.setLocation(
			(getWidth()-GemBattle.popupMessageFloat.getWidth())/2,
			(getHeight()-GemBattle.popupMessageFloat.getHeight())/2);

		attackEffectPane.setSize(getWidth(), getHeight());
		attackEffectPane.setLocation(0, 0);

		super.layout();
	}

	private final static SimpleDateFormat clockFormat = new SimpleDateFormat("HH:mm");

	public void updateTime() {
		if(paused)
			return;

		long t = System.currentTimeMillis();
		if(prevt==0L)
			prevt = t;
		float dt = (t-prevt)/1000f;

		GemBattle.particles.update(dt);
		GemBattle.attackEffects.update(dt);
		humanPlayerPane.damageText.updateTime(dt);
		aiPlayerPane.damageText.updateTime(dt);
		boardPane.updateTime(dt);

		prevt = t;
	}

	@Override
	protected void paintSelf(Canvas canvas) {
		if(!Gem.isLoaded())
			Gem.loadBitmaps(GemBattle.resources);

		updateTime();

		paint.setColor(0xff000000);
		paint.setStyle(Paint.Style.FILL);
		paint.setShader(bgFill);
		canvas.drawRect(-getX(), -getY(), getWidth()+getX(), getHeight()+getY(), paint);
		paint.setShader(null);

		paint.setStyle(Paint.Style.FILL);
		paint.setColor(0xffffffff);
		paint.setTypeface(GemBattle.boldFont);

		paint.setTextSize(25);
		paint.setTextAlign(Paint.Align.RIGHT);
		canvas.drawText(clockFormat.format(Calendar.getInstance().getTime()), getWidth()-10, 25, paint);

		if(boardPane.board.player.human) {
			paint.setColor(0xff999999);
			paint.setTextSize(25);
			paint.setTextAlign(Paint.Align.CENTER);
			canvas.drawText(String.format("You can match %d", boardPane.board.targetMatches), getWidth()/2, getHeight()-50, paint);
		}
	}
}
