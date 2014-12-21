package ar.rulosoft.mimanganu.componentes;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.TextView;

public class DarkFrameTextView extends TextView {

	private Paint paintB;

	public DarkFrameTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		Inicializar();
	}

	private void Inicializar() {
		paintB = new Paint();
		paintB.setColor(Color.BLACK);
		paintB.setAlpha(100);
		this.setTextColor(Color.WHITE);
	}

	public DarkFrameTextView(Context context) {
		super(context);
		Inicializar();
	}

	public DarkFrameTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		Inicializar();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawRect(0, 0, getWidth(), getHeight(), paintB);
		super.onDraw(canvas);
	}

}
