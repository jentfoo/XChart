/**
 * Copyright 2015 Knowm Inc. (http://knowm.org) and contributors.
 * Copyright 2011-2015 Xeiam LLC (http://xeiam.com) and contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.knowm.xchart.internal.chartpart;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.knowm.xchart.StyleManager;
import org.knowm.xchart.internal.Utils;
import org.knowm.xchart.internal.chartpart.Axis.Direction;

/**
 * This class encapsulates the logic to generate the axis tick mark and axis tick label data for rendering the axis ticks for date axes
 *
 * @author timmolter
 */
public class AxisTickCalculator_Date extends AxisTickCalculator {

  private static final long MILLIS_SCALE = TimeUnit.MILLISECONDS.toMillis(1L);
  private static final long SEC_SCALE = TimeUnit.SECONDS.toMillis(1L);
  private static final long MIN_SCALE = TimeUnit.MINUTES.toMillis(1L);
  private static final long HOUR_SCALE = TimeUnit.HOURS.toMillis(1L);
  private static final long DAY_SCALE = TimeUnit.DAYS.toMillis(1L);
  private static final long MONTH_SCALE = TimeUnit.DAYS.toMillis(1L) * 30;
  // private static final long QUARTER_SCALE = TimeUnit.DAYS.toMillis(1L) * 120;
  private static final long YEAR_SCALE = TimeUnit.DAYS.toMillis(1L) * 365;

  private static List<TimeSpan> timeSpans = new ArrayList<TimeSpan>();

  static {
    timeSpans.add(new TimeSpan(MILLIS_SCALE, 1, "ss.SSS"));
    timeSpans.add(new TimeSpan(MILLIS_SCALE, 2, "ss.SSS"));
    timeSpans.add(new TimeSpan(MILLIS_SCALE, 5, "ss.SSS"));
    timeSpans.add(new TimeSpan(MILLIS_SCALE, 10, "ss.SSS"));
    timeSpans.add(new TimeSpan(MILLIS_SCALE, 50, "ss.SS"));
    timeSpans.add(new TimeSpan(MILLIS_SCALE, 100, "ss.SS"));
    timeSpans.add(new TimeSpan(MILLIS_SCALE, 200, "ss.SS"));
    timeSpans.add(new TimeSpan(MILLIS_SCALE, 500, "ss.SS"));

    timeSpans.add(new TimeSpan(SEC_SCALE, 1, "ss.SS"));
    timeSpans.add(new TimeSpan(SEC_SCALE, 2, "ss.S"));
    timeSpans.add(new TimeSpan(SEC_SCALE, 5, "ss.S"));
    timeSpans.add(new TimeSpan(SEC_SCALE, 10, "HH:mm:ss"));
    timeSpans.add(new TimeSpan(SEC_SCALE, 15, "HH:mm:ss"));
    timeSpans.add(new TimeSpan(SEC_SCALE, 20, "HH:mm:ss"));
    timeSpans.add(new TimeSpan(SEC_SCALE, 30, "HH:mm:ss"));

    timeSpans.add(new TimeSpan(MIN_SCALE, 1, "HH:mm:ss"));
    timeSpans.add(new TimeSpan(MIN_SCALE, 2, "HH:mm:ss"));
    timeSpans.add(new TimeSpan(MIN_SCALE, 5, "HH:mm:ss"));
    timeSpans.add(new TimeSpan(MIN_SCALE, 10, "HH:mm"));
    timeSpans.add(new TimeSpan(MIN_SCALE, 15, "HH:mm"));
    timeSpans.add(new TimeSpan(MIN_SCALE, 20, "HH:mm"));
    timeSpans.add(new TimeSpan(MIN_SCALE, 30, "HH:mm"));

    timeSpans.add(new TimeSpan(HOUR_SCALE, 1, "HH:mm"));
    timeSpans.add(new TimeSpan(HOUR_SCALE, 2, "HH:mm"));
    timeSpans.add(new TimeSpan(HOUR_SCALE, 4, "HH:mm"));
    timeSpans.add(new TimeSpan(HOUR_SCALE, 8, "HH:mm"));
    timeSpans.add(new TimeSpan(HOUR_SCALE, 12, "HH:mm"));

    timeSpans.add(new TimeSpan(DAY_SCALE, 1, "EEE HH:mm"));
    timeSpans.add(new TimeSpan(DAY_SCALE, 2, "EEE HH:mm"));
    timeSpans.add(new TimeSpan(DAY_SCALE, 3, "EEE HH:mm"));
    timeSpans.add(new TimeSpan(DAY_SCALE, 5, "MM-dd"));
    timeSpans.add(new TimeSpan(DAY_SCALE, 10, "MM-dd"));
    timeSpans.add(new TimeSpan(DAY_SCALE, 15, "MM-dd"));

    timeSpans.add(new TimeSpan(MONTH_SCALE, 1, "MM-dd"));
    timeSpans.add(new TimeSpan(MONTH_SCALE, 2, "MM-dd"));
    timeSpans.add(new TimeSpan(MONTH_SCALE, 3, "MM-dd"));
    timeSpans.add(new TimeSpan(MONTH_SCALE, 4, "MM-dd"));
    timeSpans.add(new TimeSpan(MONTH_SCALE, 6, "yyyy-MM"));

    timeSpans.add(new TimeSpan(YEAR_SCALE, 1, "yyyy-MM"));
    timeSpans.add(new TimeSpan(YEAR_SCALE, 2, "yyyy-MM"));
    timeSpans.add(new TimeSpan(YEAR_SCALE, 5, "yyyy"));
    timeSpans.add(new TimeSpan(YEAR_SCALE, 10, "yyyy"));
    timeSpans.add(new TimeSpan(YEAR_SCALE, 20, "yyyy"));
    timeSpans.add(new TimeSpan(YEAR_SCALE, 100, "yyyy"));
    timeSpans.add(new TimeSpan(YEAR_SCALE, 500, "yyyy"));
    timeSpans.add(new TimeSpan(YEAR_SCALE, 1000, "yyyy"));

  }

  /**
   * Constructor
   *
   * @param axisDirection
   * @param workingSpace
   * @param minValue
   * @param maxValue
   * @param styleManager
   */
  public AxisTickCalculator_Date(Direction axisDirection, double workingSpace, double minValue, double maxValue, StyleManager styleManager) {

    super(axisDirection, workingSpace, minValue, maxValue, styleManager);

    calculate();
  }

  private void calculate() {

    // tick space - a percentage of the working space available for ticks
    double tickSpace = styleManager.getAxisTickSpacePercentage() * workingSpace; // in plot space

    // this prevents an infinite loop when the plot gets sized really small.
    if (tickSpace < styleManager.getXAxisTickMarkSpacingHint()) {
      // System.out.println("Returning!");
      return;
    }

    // where the tick should begin in the working space in pixels
    double margin = Utils.getTickStartOffset(workingSpace, tickSpace); // in plot space double gridStep = getGridStepForDecimal(tickSpace);

    // the span of the data
    long span = (long) Math.abs(maxValue - minValue); // in data space
    // System.out.println("span: " + span);

    // Generate the labels first, see if they "look" OK and reiterate with an increased tickSpacingHint
    int tickSpacingHint = styleManager.getXAxisTickMarkSpacingHint();
    int gridStepInChartSpace = 0;

    // System.out.println("calculating ticks...");
    long gridStepHint = (long) (span / tickSpace * tickSpacingHint); // in time units (ms)
    // System.out.println("gridStepHint: " + gridStepHint);

    //////////////////////////////////////////////

    // iterate forward until the matching timespan is found
    int index = 0;
    for (int i = 0; i < timeSpans.size() - 1; i++) {

      if (span < ((timeSpans.get(i).getUnitAmount() * timeSpans.get(i).getMagnitude() + timeSpans.get(i + 1).getUnitAmount() * timeSpans.get(i + 1).getMagnitude()) / 2.0)) {
        index = i;
        break;
      }
    }

    // use the pattern from the first timeSpan
    String datePattern = timeSpans.get(index).getDatePattern();
    // System.out.println("index: " + index);

    // iterate BACWARDS from previous point until the appropriate timespan is found for the gridStepHint
    for (int i = index - 1; i > 0; i--) {

      if (gridStepHint > timeSpans.get(i).getUnitAmount() * timeSpans.get(i).getMagnitude()) {
        index = i;
        break;
      }
    }

    //////////////////////////////////////////////

    // now increase the timespan until one is found where all the labels fit nicely. It will often be the first one.
    index--;
    do {

      tickLabels.clear();
      tickLocations.clear();

      double gridStep = timeSpans.get(++index).getUnitAmount() * timeSpans.get(index).getMagnitude(); // in time units (ms)
      // System.out.println("gridStep: " + gridStep);

      gridStepInChartSpace = (int) (gridStep / span * tickSpace);
      // System.out.println("gridStepInChartSpace: " + gridStepInChartSpace);

      double firstPosition = getFirstPosition(gridStep);

      // Define Date Pattern
      // override pattern if one was explicitly given
      if (styleManager.getDatePattern() != null) {
        datePattern = styleManager.getDatePattern();
      }
      // System.out.println("datePattern: " + datePattern);

      SimpleDateFormat simpleDateformat = new SimpleDateFormat(datePattern, styleManager.getLocale());
      simpleDateformat.setTimeZone(styleManager.getTimezone());

      // return simpleDateformat.format(value);

      //////////////////////////////

      // generate all tickLabels and tickLocations from the first to last position
      for (double value = firstPosition; value <= maxValue + 2 * gridStep; value = value + gridStep) {

        // if (value <= maxValue && value >= minValue) {

        ///////////////////////////////

        tickLabels.add(simpleDateformat.format(value));
        // here we convert tickPosition finally to plot space, i.e. pixels
        double tickLabelPosition = margin + ((value - minValue) / (maxValue - minValue) * tickSpace);
        // System.out.println("tickLabelPosition: " + tickLabelPosition);
        tickLocations.add(tickLabelPosition);
        // }
      }
    } while (!willLabelsFitInTickSpaceHint(tickLabels, gridStepInChartSpace));
  }

  static class TimeSpan {

    private final long unitAmount;
    private final int magnitude;
    private final String datePattern;

    /**
     * Constructor
     *
     * @param unitAmount
     * @param magnitude
     * @param datePattern
     */
    public TimeSpan(long unitAmount, int magnitude, String datePattern) {
      this.unitAmount = unitAmount;
      this.magnitude = magnitude;
      this.datePattern = datePattern;
    }

    public long getUnitAmount() {

      return unitAmount;
    }

    public int getMagnitude() {

      return magnitude;
    }

    public String getDatePattern() {

      return datePattern;
    }

    @Override
    public String toString() {

      return "TimeSpan [unitAmount=" + unitAmount + ", magnitude=" + magnitude + ", datePattern=" + datePattern + "]";
    }

  }

}
