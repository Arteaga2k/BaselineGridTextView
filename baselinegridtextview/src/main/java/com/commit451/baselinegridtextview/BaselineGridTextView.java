/*
 * Copyright 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.commit451.baselinegridtextview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.os.Build;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.util.TypedValue;


/**
 * An extension to {@link AppCompatTextView} which aligns text to a 4dp baseline grid.
 * <p>
 * To achieve this we expose a {@code lineHeightHint} allowing you to specify the desired line
 * height (alternatively a {@code lineHeightMultiplierHint} to use a multiplier of the text size).
 * This line height will be adjusted to be a multiple of 4dp to ensure that baselines sit on
 * the grid.
 * <p>
 * We also adjust the {@code topPadding} to ensure that the first line's baseline is on the grid
 * (relative to the view's top) and the {@code bottomPadding} to ensure this view's height is a
 * multiple of 4dp so that subsequent views start on the grid.
 */
public class BaselineGridTextView extends AppCompatTextView {

    private final int FOUR_DIP;

    private float lineHeightMultiplierHint = 1f;
    private float lineHeightHint = 0f;
    private int unalignedTopPadding = 0;

    public BaselineGridTextView(Context context) {
        this(context, null);
    }

    public BaselineGridTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaselineGridTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        if (attrs != null) {
            final TypedArray a = context.obtainStyledAttributes(
                    attrs, R.styleable.BaselineGridTextView, defStyleAttr, 0);

            lineHeightMultiplierHint =
                    a.getFloat(R.styleable.BaselineGridTextView_lineHeightMultiplierHint, 1f);
            lineHeightHint =
                    a.getDimensionPixelSize(R.styleable.BaselineGridTextView_lineHeightHint, 0);
            unalignedTopPadding = getPaddingTop();
            a.recycle();
        }

        FOUR_DIP = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics());

        setIncludeFontPadding(false);

        if (Build.VERSION.SDK_INT >= 21) {
            setElegantTextHeight(false);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        recomputeLineHeight();
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final int height = getMeasuredHeight();
        final int gridOverhang = height % FOUR_DIP;
        if (gridOverhang != 0) {
            final int addition = FOUR_DIP - gridOverhang;
            super.setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(),
                    getPaddingBottom() + addition);
            setMeasuredDimension(getMeasuredWidth(), height + addition);
        }
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        super.setPadding(left, top, right, bottom);
        if (unalignedTopPadding != top) {
            unalignedTopPadding = top;
            recomputeLineHeight();
        }
    }

    @Override
    public void setPaddingRelative(int start, int top, int end, int bottom) {
        super.setPaddingRelative(start, top, end, bottom);
        if (unalignedTopPadding != top) {
            unalignedTopPadding = top;
            recomputeLineHeight();
        }
    }

    /**
     * Get the line height multiplier hint
     * @return the multiplier
     */
    public float getLineHeightMultiplierHint() {
        return lineHeightMultiplierHint;
    }

    /**
     * Set the line height multiplier hint
     * @param lineHeightMultiplierHint the multiplier hint
     */
    public void setLineHeightMultiplierHint(float lineHeightMultiplierHint) {
        this.lineHeightMultiplierHint = lineHeightMultiplierHint;
        recomputeLineHeight();
    }

    /**
     * Get the line height hint
     * @return the line height hint
     */
    public float getLineHeightHint() {
        return lineHeightHint;
    }

    /**
     * Set the lint height hint
     * @param lineHeightHint the line height hint, in pixels
     */
    public void setLineHeightHint(float lineHeightHint) {
        this.lineHeightHint = lineHeightHint;
        recomputeLineHeight();
    }

    private void recomputeLineHeight() {
        // ensure that the first line's baselines sits on 4dp grid by setting the top padding
        final Paint.FontMetricsInt fm = getPaint().getFontMetricsInt();
        final int gridAlignedTopPadding = (int) (FOUR_DIP * (float)
                Math.ceil((unalignedTopPadding + Math.abs(fm.ascent)) / FOUR_DIP)
                - Math.ceil(Math.abs(fm.ascent)));
        super.setPadding(
                getPaddingLeft(), gridAlignedTopPadding, getPaddingRight(), getPaddingBottom());

        // ensures line height is a multiple of 4dp
        final int fontHeight = Math.abs(fm.ascent - fm.descent) + fm.leading;
        final float desiredLineHeight = (lineHeightHint > 0)
                ? lineHeightHint
                : lineHeightMultiplierHint * fontHeight;

        final int baselineAlignedLineHeight =
                (int) (FOUR_DIP * (float) Math.ceil(desiredLineHeight / FOUR_DIP));
        setLineSpacing(baselineAlignedLineHeight - fontHeight, 1f);
    }
}