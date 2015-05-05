package cz.cvut.sigmet;

import static cz.cvut.sigmet.dbUtils.SigmetSignalUtils.getSignalByReflection;

import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Fragment;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Shader;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.util.FloatMath;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;

import com.androidplot.Plot.BorderStyle;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.PointLabelFormatter;
import com.androidplot.xy.PointLabeler;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.XYStepMode;

import cz.cvut.sigmet.dbUtils.SigmetDataListener;
import cz.cvut.sigmet.model.CellDTO;
import cz.cvut.sigmet.model.SignalDTO;

public class GSMGRaphFragment extends Fragment implements SigmetDataListener, OnTouchListener {

	private XYPlot plot;
	
	private List<Long> dbm_time_values = new ArrayList<Long>();

	private SimpleXYSeries dbm = new SimpleXYSeries("dbm");

	private Timer timer = new Timer();

	private static final long PERIOD = 3000;

	private SignalStrength currentSignal;

	public final Signallistener listener = new Signallistener();

	private PointF minXY;
	private PointF maxXY;

	// Definition of the touch states
	static final int NONE = 0;
	static final int ONE_FINGER_DRAG = 1;
	static final int TWO_FINGERS_DRAG = 2;
	int mode = NONE;

	PointF firstFinger;
	float lastScrolling;
	float distBetweenFingers;
	float lastZooming;

	private LineAndPointFormatter dbmFormater;

	private PointLabeler voidLabeler = new PointLabeler() {
		@Override
		public String getLabel(XYSeries arg0, int arg1) {
			return "";
		}

	};

	private PointLabeler pointLabeler = new PointLabeler() {
		@Override
		public String getLabel(XYSeries series, int index) {
			return series.getY(index) + "";
		}
	};

	public GSMGRaphFragment() {
		timer.scheduleAtFixedRate(new FeedGraph(), 0, PERIOD);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_graph, container, false);
		rootView.setBackgroundColor(Color.WHITE);
		plot = (XYPlot) rootView.findViewById(R.id.graph);

		plot.setOnTouchListener(this);
		plot.getGraphWidget().setTicksPerDomainLabel(3);
		plot.getGraphWidget().getBackgroundPaint().setColor(Color.WHITE);
		plot.getGraphWidget().setRangeValueFormat(new DecimalFormat("#####"));
		plot.getGraphWidget().setRangeLabelWidth(20);
		plot.setBackgroundColor(Color.TRANSPARENT);
		plot.getBackgroundPaint().setColor(Color.TRANSPARENT);

		plot.getGraphWidget().getBackgroundPaint().setColor(Color.WHITE);
		plot.getGraphWidget().getGridBackgroundPaint().setColor(Color.WHITE);

		plot.getGraphWidget().getDomainLabelPaint().setColor(Color.BLACK);
		plot.getGraphWidget().getRangeLabelPaint().setColor(Color.BLACK);

		plot.getGraphWidget().getDomainOriginLabelPaint().setColor(Color.BLACK);
		plot.getGraphWidget().getDomainOriginLinePaint().setColor(Color.BLACK);
		plot.getGraphWidget().getRangeOriginLinePaint().setColor(Color.BLACK);

		plot.setBackgroundColor(Color.WHITE);
		plot.getBackgroundPaint().setColor(Color.WHITE);
		plot.getBorderPaint().setColor(Color.WHITE);
		plot.getBackground().setVisible(false, false);

		plot.setRangeStep(XYStepMode.INCREMENT_BY_VAL, 30);
		plot.setRangeBoundaries(-120, 30, BoundaryMode.FIXED);
		plot.setTicksPerRangeLabel(1);
		plot.getGraphWidget().setRangeLabelTickExtension(5);
		
		
		plot.setKeepScreenOn(true);

		plot.getLayoutManager().remove(plot.getLegendWidget());
		plot.getLayoutManager().remove(plot.getDomainLabelWidget());
		plot.getLayoutManager().remove(plot.getRangeLabelWidget());
		plot.getLayoutManager().remove(plot.getTitleWidget());
		
		plot.getGraphWidget().setMarginLeft(10);
		
		plot.setDomainValueFormat(new Format() {
			
			private static final long serialVersionUID = 1L;
			private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
			
			@Override
			public Object parseObject(String string, ParsePosition position) {
				return null;
			}
			
			@Override
			public StringBuffer format(Object object, StringBuffer buffer, FieldPosition field) {
				Integer timestamp = ((Number) object).intValue();
                Date date = new Date(dbm_time_values.get(timestamp));
                return dateFormat.format(date, buffer, field);
			}
		});

		int line_clr = Color.BLACK;
		int pint_clr = Color.DKGRAY;

		PointLabelFormatter dbmPointLabel = new PointLabelFormatter(Color.BLACK);

		dbmFormater = new LineAndPointFormatter(line_clr, pint_clr, null, dbmPointLabel);
		dbmFormater.getLinePaint().setStrokeWidth(4);
		dbmFormater.getVertexPaint().setStrokeWidth(7);
		
		dbm.useImplicitXVals();

		plot.addSeries(dbm, dbmFormater);
		plot.setOnTouchListener(this);
		minXY = new PointF(plot.getCalculatedMinX().floatValue(), plot.getCalculatedMinY().floatValue());
		maxXY = new PointF(plot.getCalculatedMaxX().floatValue(), plot.getCalculatedMaxY().floatValue());
		return rootView;
	}

	@Override
	public void onCellChange(CellDTO dto) {
		// TODO draw handover into graph

	}

	@Override
	public void onLocationCange(SignalDTO dto) {

	}

	private class Signallistener extends PhoneStateListener {
		@Override
		public void onSignalStrengthsChanged(SignalStrength signalStrength) {
			currentSignal = signalStrength;
		}
	}

	private class FeedGraph extends TimerTask {
		public void run() {
			if(currentSignal == null){
				return;
			}
			dbm.addLast(null, getSignalByReflection(currentSignal));
			dbm_time_values.add(System.currentTimeMillis());
			if (plot != null) {
				plot.redraw();
				minXY = new PointF(plot.getCalculatedMinX().floatValue(), plot.getCalculatedMinY().floatValue());
				maxXY = new PointF(plot.getCalculatedMaxX().floatValue(), plot.getCalculatedMaxY().floatValue());
			}
		}
	}

	@Override
	public boolean onTouch(View arg0, MotionEvent event) {
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN: // Start gesture
			firstFinger = new PointF(event.getX(), event.getY());
			mode = ONE_FINGER_DRAG;
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_POINTER_UP:
			mode = NONE;
			break;
		case MotionEvent.ACTION_POINTER_DOWN: // second finger
			distBetweenFingers = spacing(event);
			// the distance check is done to avoid false alarms
			if (distBetweenFingers > 5f) {
				mode = TWO_FINGERS_DRAG;
			}
			break;
		case MotionEvent.ACTION_MOVE:
			float stopScroll_right = dbm.getX(dbm.size() - 1).floatValue();
			float stopScroll_left = dbm.getX(0).floatValue();
			if (mode == ONE_FINGER_DRAG) {
				PointF oldFirstFinger = firstFinger;
				firstFinger = new PointF(event.getX(), event.getY());
				scroll(oldFirstFinger.x - firstFinger.x);
				if (stopScroll_right < maxXY.x && stopScroll_left > minXY.x)
					;
				plot.setDomainBoundaries(minXY.x, maxXY.x, BoundaryMode.FIXED);
				plot.redraw();

			} else if (mode == TWO_FINGERS_DRAG) {
				float oldDist = distBetweenFingers;
				distBetweenFingers = spacing(event);
				zoom(oldDist / distBetweenFingers);
				plot.setDomainBoundaries(minXY.x, maxXY.x, BoundaryMode.FIXED);
				plot.redraw();
			}
			break;
		}
		return true;
	}

	private void zoom(float scale) {
		float domainSpan = maxXY.x - minXY.x;
		if (domainSpan >= 19) {
			dbmFormater.setPointLabeler(voidLabeler);
		} else {
			dbmFormater.setPointLabeler(pointLabeler);
		}
		float domainMidPoint = maxXY.x - domainSpan / 2.0f;
		float offset = domainSpan * scale / 2.0f;

		minXY.x = domainMidPoint - offset;
		maxXY.x = domainMidPoint + offset;
		minXY.x = Math.min(minXY.x, dbm.getX(dbm.size() - 3).floatValue());
		maxXY.x = Math.max(maxXY.x, dbm.getX(1).floatValue());
		clampToDomainBounds(domainSpan);
	}

	private void scroll(float pan) {
		float domainSpan = maxXY.x - minXY.x;
		float step = domainSpan / plot.getWidth();
		float offset = pan * step;
		minXY.x = minXY.x + offset;
		maxXY.x = maxXY.x + offset;
		clampToDomainBounds(domainSpan);
	}

	private void clampToDomainBounds(float domainSpan) {
		float leftBoundary = dbm.getX(0).floatValue();
		float rightBoundary = dbm.getX(dbm.size() - 1).floatValue();
		// enforce left scroll boundary:
		if (minXY.x < leftBoundary) {
			minXY.x = leftBoundary;
			maxXY.x = leftBoundary + domainSpan;
		} else if (maxXY.x > dbm.getX(dbm.size() - 1).floatValue()) {
			maxXY.x = rightBoundary;
			minXY.x = rightBoundary - domainSpan;
		}
	}

	private float spacing(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return FloatMath.sqrt(x * x + y * y);
	}

}
