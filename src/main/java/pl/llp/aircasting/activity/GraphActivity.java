/**
 AirCasting - Share your Air!
 Copyright (C) 2011-2012 HabitatMap, Inc.

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.

 You can contact the authors by email at <info@habitatmap.org>
 */
package pl.llp.aircasting.activity;

import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.TextView;
import com.google.inject.Inject;
import pl.llp.aircasting.R;
import pl.llp.aircasting.SoundLevel;
import pl.llp.aircasting.event.DoubleTapEvent;
import pl.llp.aircasting.event.ScrollEvent;
import pl.llp.aircasting.event.TapEvent;
import pl.llp.aircasting.model.Note;
import pl.llp.aircasting.model.SoundMeasurement;
import pl.llp.aircasting.view.NoisePlot;
import pl.llp.aircasting.view.presenter.MeasurementPresenter;
import roboguice.event.Observes;
import roboguice.inject.InjectView;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.Iterables.getLast;
import static com.google.common.collect.Lists.newArrayList;

public class GraphActivity extends AirCastingActivity implements View.OnClickListener, MeasurementPresenter.Listener {
    @InjectView(R.id.noise_graph) NoisePlot plot;

    @InjectView(R.id.graph_begin_time) TextView graphBegin;
    @InjectView(R.id.graph_end_time) TextView graphEnd;
    @InjectView(R.id.graph_top_db) TextView graphTop;
    @InjectView(R.id.graph_bottom_db) TextView graphBottom;

    @InjectView(R.id.suggest_scroll_left) View scrollLeft;
    @InjectView(R.id.suggest_scroll_right) View scrollRight;

    @Inject MeasurementPresenter measurementPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.graph);

        plot.initialize(this, settingsHelper, resourceHelper, calibrationHelper);

        graphTop.setText(settingsHelper.getThreshold(SoundLevel.TOO_LOUD) + " dB");
        graphBottom.setText(settingsHelper.getThreshold(SoundLevel.QUIET) + " dB");
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
        measurementPresenter.registerListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        measurementPresenter.unregisterListener(this);
    }

    private void refresh() {
        zoomIn.setEnabled(measurementPresenter.canZoomIn());
        zoomOut.setEnabled(measurementPresenter.canZoomOut());

        List<SoundMeasurement> measurements = measurementPresenter.getTimelineView();
        ArrayList<Note> notes = newArrayList(sessionManager.getNotes());

        plot.update(measurements, notes);

        scrollLeft.setVisibility(measurementPresenter.canScrollLeft() ? View.VISIBLE : View.GONE);
        scrollRight.setVisibility(measurementPresenter.canScrollRight() ? View.VISIBLE : View.GONE);

        if (!measurements.isEmpty()) {
            graphBegin.setText(DateFormat.format("hh:mm:ss", measurements.get(0).getTime()));
            graphEnd.setText(DateFormat.format("hh:mm:ss", getLast(measurements).getTime()));
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.zoom_in:
                zoomIn();
                break;
            case R.id.zoom_out:
                zoomOut();
                break;
            default:
                super.onClick(view);
        }
    }

    private void zoomIn() {
        measurementPresenter.zoomIn();
    }

    private void zoomOut() {
        measurementPresenter.zoomOut();
    }

    @Override
    public void onViewUpdated() {
        refresh();
    }

    @Override
    public void onAveragedMeasurement(SoundMeasurement measurement) {
    }

    @Override
    public void onEvent(TapEvent event) {
        if (!plot.onTap(event)) {
            super.onEvent(event);
        }
    }

    @Override
    protected void refreshNotes() {
        refresh();
    }

    @SuppressWarnings("UnusedDeclaration")
    public void onEvent(@Observes DoubleTapEvent event) {
        zoomIn();
    }

    @SuppressWarnings("UnusedDeclaration")
    public void onEvent(@Observes ScrollEvent event) {
        float relativeScroll = event.getDistanceX() / plot.getWidth();
        measurementPresenter.scroll(relativeScroll);
    }
}
