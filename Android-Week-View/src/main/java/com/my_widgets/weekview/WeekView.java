package com.my_widgets.weekview;

import static com.my_widgets.weekview.WeekViewUtil.daysBetween;
import static com.my_widgets.weekview.WeekViewUtil.getPassedMinutesInDay;
import static com.my_widgets.weekview.WeekViewUtil.isSameDay;
import static com.my_widgets.weekview.WeekViewUtil.today;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.DragEvent;
import android.view.GestureDetector;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.OverScroller;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.view.GestureDetectorCompat;
import androidx.core.view.ViewCompat;
import androidx.interpolator.view.animation.FastOutLinearInInterpolator;

import com.alamkanak.weekview.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Created by Raquib-ul-Alam Kanak on 7/21/2014.
 * Website: http://alamkanak.github.io/
 */
public class WeekView extends View {

    private enum Direction {
        NONE, LEFT, RIGHT, VERTICAL
    }

    @Deprecated
    public static final int LENGTH_SHORT = 1;
    @Deprecated
    public static final int LENGTH_LONG = 2;
    private final Context mContext;
    private Calendar mHomeDate;
    private Calendar mMinDate;
    private Calendar mMaxDate;
    private Paint mTimeTextPaint;
    private float mTimeTextWidth;
    private float mTimeTextHeight;
    private Paint mHeaderTextPaint;
    private float mHeaderTextHeight;
    private float mHeaderHeight;
    private GestureDetectorCompat mGestureDetector;
    private OverScroller mScroller;
    private PointF mCurrentOrigin = new PointF(0f, 0f);
    private Direction mCurrentScrollDirection = Direction.NONE;
    private Paint mHeaderBackgroundPaint;
    private float mWidthPerDay;
    private Paint mDayBackgroundPaint;
    private Paint mHourSeparatorPaint;
    private float mHeaderMarginBottom;
    private Paint mTodayBackgroundPaint;
    private Paint mFutureBackgroundPaint;
    private Paint mPastBackgroundPaint;
    private Paint mFutureWeekendBackgroundPaint;
    private Paint mPastWeekendBackgroundPaint;
    private Paint mNowLinePaint;
    private Paint mTodayHeaderTextPaint;
    private Paint mEventBackgroundPaint;
    private Paint mNewEventBackgroundPaint;
    private float mHeaderColumnWidth;
    private List<EventRect> mEventRects;
    private List<WeekViewEvent> mEvents;
    private TextPaint mEventTextPaint;
    private TextPaint mNewEventTextPaint;
    private Paint mHeaderColumnBackgroundPaint;
    private int mFetchedPeriod = -1; // the middle period the calendar has fetched.
    private boolean mRefreshEvents = false;
    private Direction mCurrentFlingDirection = Direction.NONE;
    private ScaleGestureDetector mScaleDetector;
    private boolean mIsZooming;
    private Calendar mFirstVisibleDay;
    private Calendar mLastVisibleDay;
    private int mMinimumFlingVelocity = 0;
    private int mScaledTouchSlop = 0;
    private EventRect mNewEventRect;
    private TextColorPicker textColorPicker;

    // Attributes and their default values.
    private int mHourHeight = 50;
    private int mNewHourHeight = -1;
    private int mMinHourHeight = 0; //no minimum specified (will be dynamic, based on screen)
    private int mEffectiveMinHourHeight = mMinHourHeight; //compensates for the fact that you can't keep zooming out.
    private int mMaxHourHeight = 250;
    private int mColumnGap = 10;
    private int mFirstDayOfWeek = Calendar.MONDAY;
    private int mTextSize = 12;
    private int mHeaderColumnPadding = 10;
    private int mHeaderColumnTextColor = Color.BLACK;
    private int mNumberOfVisibleDays = 3;
    private int mHeaderRowPadding = 10;
    private int mHeaderRowBackgroundColor = Color.WHITE;
    private int mDayBackgroundColor = Color.rgb(245, 245, 245);
    private int mPastBackgroundColor = Color.rgb(227, 227, 227);
    private int mFutureBackgroundColor = Color.rgb(245, 245, 245);
    private int mPastWeekendBackgroundColor = 0;
    private int mFutureWeekendBackgroundColor = 0;
    private int mNowLineColor = Color.rgb(102, 102, 102);
    private int mNowLineThickness = 5;
    private int mHourSeparatorColor = Color.rgb(230, 230, 230);
    private int mTodayBackgroundColor = Color.rgb(239, 247, 254);
    private int mHourSeparatorHeight = 2;
    private int mTodayHeaderTextColor = Color.rgb(39, 137, 228);
    private int mEventTextSize = 12;
    private int mEventTextColor = Color.BLACK;
    private int mEventPadding = 8;
    private int mHeaderColumnBackgroundColor = Color.WHITE;
    private int mDefaultEventColor;
    private int mNewEventColor;
    private String mNewEventIdentifier = "-100";
    private Drawable mNewEventIconDrawable;
    private int mNewEventLengthInMinutes = 60;
    private int mNewEventTimeResolutionInMinutes = 15;
    private boolean mShowFirstDayOfWeekFirst = false;

    private boolean mIsFirstDraw = true;
    private boolean mAreDimensionsInvalid = true;
    @Deprecated
    private int mDayNameLength = LENGTH_LONG;
    private int mOverlappingEventGap = 0;
    private int mEventMarginVertical = 0;
    private float mXScrollingSpeed = 1f;
    private Calendar mScrollToDay = null;
    private double mScrollToHour = -1;
    private int mEventCornerRadius = 0;
    private boolean mShowDistinctWeekendColor = false;
    private boolean mShowNowLine = false;
    private boolean mShowDistinctPastFutureColor = false;
    private boolean mHorizontalFlingEnabled = true;
    private boolean mVerticalFlingEnabled = true;
    private int mAllDayEventHeight = 100;
    private float mZoomFocusPoint = 0;
    private boolean mZoomFocusPointEnabled = true;
    private int mScrollDuration = 250;
    private int mTimeColumnResolution = 60;
    private Typeface mTypeface = Typeface.DEFAULT_BOLD;
    private int mMinTime = 0;
    private int mMaxTime = 24;
    private boolean mAutoLimitTime = false;
    private boolean mEnableDropListener = false;
    private int mMinOverlappingMinutes = 0;

    // Listeners.
    private EventClickListener mEventClickListener;
    private EventLongPressListener mEventLongPressListener;
    private WeekViewLoader mWeekViewLoader;
    private EmptyViewClickListener mEmptyViewClickListener;
    private EmptyViewLongPressListener mEmptyViewLongPressListener;
    private DateTimeInterpreter mDateTimeInterpreter;
    private ScrollListener mScrollListener;
    private AddEventClickListener mAddEventClickListener;
    private DropListener mDropListener;
    private ZoomEndListener mZoomEndListener;

    private final GestureDetector.SimpleOnGestureListener mGestureListener = new GestureDetector.SimpleOnGestureListener() {

        @Override
        public boolean onDown(MotionEvent e) {
            stopScrolling();
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            goToNearestOrigin();
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            // Check if view is zoomed.
            if (mIsZooming) return true;

            switch (mCurrentScrollDirection) {
                case NONE: {
                    // Allow scrolling only in one direction.
                    if (Math.abs(distanceX) > Math.abs(distanceY)) {
                        if (distanceX > 0) {
                            mCurrentScrollDirection = Direction.LEFT;
                        } else {
                            mCurrentScrollDirection = Direction.RIGHT;
                        }
                    } else {
                        mCurrentScrollDirection = Direction.VERTICAL;
                    }
                    break;
                }
                case LEFT: {
                    // Change direction if there was enough change.
                    if (Math.abs(distanceX) > Math.abs(distanceY) && (distanceX < -mScaledTouchSlop)) {
                        mCurrentScrollDirection = Direction.RIGHT;
                    }
                    break;
                }
                case RIGHT: {
                    // Change direction if there was enough change.
                    if (Math.abs(distanceX) > Math.abs(distanceY) && (distanceX > mScaledTouchSlop)) {
                        mCurrentScrollDirection = Direction.LEFT;
                    }
                    break;
                }
                default:
                    break;
            }

            // Calculate the new origin after scroll.
            switch (mCurrentScrollDirection) {
                case LEFT:
                case RIGHT:
                    float minX = getXMinLimit();
                    float maxX = getXMaxLimit();
                    if ((mCurrentOrigin.x - (distanceX * mXScrollingSpeed)) > maxX) {
                        mCurrentOrigin.x = maxX;
                    } else if ((mCurrentOrigin.x - (distanceX * mXScrollingSpeed)) < minX) {
                        mCurrentOrigin.x = minX;
                    } else {
                        mCurrentOrigin.x -= distanceX * mXScrollingSpeed;
                    }
                    ViewCompat.postInvalidateOnAnimation(WeekView.this);
                    break;
                case VERTICAL:
                    float minY = getYMinLimit();
                    float maxY = getYMaxLimit();
                    if ((mCurrentOrigin.y - (distanceY)) > maxY) {
                        mCurrentOrigin.y = maxY;
                    } else if ((mCurrentOrigin.y - (distanceY)) < minY) {
                        mCurrentOrigin.y = minY;
                    } else {
                        mCurrentOrigin.y -= distanceY;
                    }
                    ViewCompat.postInvalidateOnAnimation(WeekView.this);
                    break;
                default:
                    break;
            }
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (mIsZooming) return true;

            if ((mCurrentFlingDirection == Direction.LEFT && !mHorizontalFlingEnabled) || (mCurrentFlingDirection == Direction.RIGHT && !mHorizontalFlingEnabled) || (mCurrentFlingDirection == Direction.VERTICAL && !mVerticalFlingEnabled)) {
                return true;
            }

            mScroller.forceFinished(true);

            mCurrentFlingDirection = mCurrentScrollDirection;
            switch (mCurrentFlingDirection) {
                case LEFT:
                case RIGHT:
                    mScroller.fling((int) mCurrentOrigin.x, (int) mCurrentOrigin.y, (int) (velocityX * mXScrollingSpeed), 0, (int) getXMinLimit(), (int) getXMaxLimit(), (int) getYMinLimit(), (int) getYMaxLimit());
                    break;
                case VERTICAL:
                    mScroller.fling((int) mCurrentOrigin.x, (int) mCurrentOrigin.y, 0, (int) velocityY, (int) getXMinLimit(), (int) getXMaxLimit(), (int) getYMinLimit(), (int) getYMaxLimit());
                    break;
                default:
                    break;
            }

            ViewCompat.postInvalidateOnAnimation(WeekView.this);
            return true;
        }


        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            goToNearestOrigin();

            // If the tap was on an event then trigger the callback.
            if (mEventRects != null && mEventClickListener != null) {
                List<EventRect> reversedEventRects = mEventRects;
                Collections.reverse(reversedEventRects);
                for (EventRect eventRect : reversedEventRects) {
                    if (!mNewEventIdentifier.equals(eventRect.event.getIdentifier()) && eventRect.rectF != null && e.getX() > eventRect.rectF.left && e.getX() < eventRect.rectF.right && e.getY() > eventRect.rectF.top && e.getY() < eventRect.rectF.bottom) {
                        mEventClickListener.onEventClick(eventRect.originalEvent, eventRect.rectF);
                        playSoundEffect(SoundEffectConstants.CLICK);
                        return super.onSingleTapConfirmed(e);
                    }
                }
            }

            float xOffset = getXStartPixel();

            float x = e.getX() - xOffset;
            float y = e.getY() - mCurrentOrigin.y;
            // If the tap was on add new Event space, then trigger the callback
            if (mAddEventClickListener != null && mNewEventRect != null && mNewEventRect.rectF != null && mNewEventRect.rectF.contains(x, y)) {
                mAddEventClickListener.onAddEventClicked(mNewEventRect.event.getStartTime(), mNewEventRect.event.getEndTime());
                return super.onSingleTapConfirmed(e);
            }

            // If the tap was on an empty space, then trigger the callback.
            if ((mEmptyViewClickListener != null || mAddEventClickListener != null) && e.getX() > mHeaderColumnWidth && e.getY() > (mHeaderHeight + mHeaderRowPadding * 2 + mHeaderMarginBottom)) {
                Calendar selectedTime = getTimeFromPoint(e.getX(), e.getY());

                if (selectedTime != null) {
                    List<WeekViewEvent> tempEvents = new ArrayList<>(mEvents);
                    if (mNewEventRect != null) {
                        tempEvents.remove(mNewEventRect.event);
                        mNewEventRect = null;
                    }

                    playSoundEffect(SoundEffectConstants.CLICK);

                    if (mEmptyViewClickListener != null)
                        mEmptyViewClickListener.onEmptyViewClicked((Calendar) selectedTime.clone());

                    if (mAddEventClickListener != null) {
                        //round selectedTime to resolution
                        selectedTime.add(Calendar.MINUTE, -(mNewEventLengthInMinutes / 2));
                        //Fix selected time if before the minimum hour
                        if (selectedTime.get(Calendar.HOUR_OF_DAY) < mMinTime) {
                            selectedTime.set(Calendar.HOUR_OF_DAY, mMinTime);
                            selectedTime.set(Calendar.MINUTE, 0);
                        }
                        int unroundedMinutes = selectedTime.get(Calendar.MINUTE);
                        int mod = unroundedMinutes % mNewEventTimeResolutionInMinutes;
                        selectedTime.add(Calendar.MINUTE, mod < Math.ceil(mNewEventTimeResolutionInMinutes / 2) ? -mod : (mNewEventTimeResolutionInMinutes - mod));

                        Calendar endTime = (Calendar) selectedTime.clone();

                        //Minus one to ensure it is the same day and not midnight (next day)
                        int maxMinutes = (mMaxTime - selectedTime.get(Calendar.HOUR_OF_DAY)) * 60 - selectedTime.get(Calendar.MINUTE) - 1;
                        endTime.add(Calendar.MINUTE, Math.min(maxMinutes, mNewEventLengthInMinutes));
                        //If clicked at end of the day, fix selected startTime
                        if (maxMinutes < mNewEventLengthInMinutes) {
                            selectedTime.add(Calendar.MINUTE, maxMinutes - mNewEventLengthInMinutes);
                        }

                        WeekViewEvent newEvent = new WeekViewEvent(mNewEventIdentifier, "", null, selectedTime, endTime);

                        float top = mHourHeight * getPassedMinutesInDay(selectedTime) / 60 + getEventsTop();
                        float bottom = mHourHeight * getPassedMinutesInDay(endTime) / 60 + getEventsTop();

                        // Calculate left and right.
                        float left = mWidthPerDay * WeekViewUtil.daysBetween(getFirstVisibleDay(), selectedTime);
                        float right = left + mWidthPerDay;

                        // Add the new event if its bounds are valid
                        if (left < right && left < getWidth() && top < getHeight() && right > mHeaderColumnWidth && bottom > 0) {
                            RectF dayRectF = new RectF(left, top, right, bottom - mCurrentOrigin.y);
                            newEvent.setColor(mNewEventColor);
                            mNewEventRect = new EventRect(newEvent, newEvent, dayRectF);
                            tempEvents.add(newEvent);
                            WeekView.this.clearEvents();
                            cacheAndSortEvents(tempEvents);
                            computePositionOfEvents(mEventRects);
                            invalidate();
                        }

                    }
                }

            }
            return super.onSingleTapConfirmed(e);
        }

        @Override
        public void onLongPress(MotionEvent e) {
            super.onLongPress(e);
            goToNearestOrigin();

            if (mEventLongPressListener != null && mEventRects != null) {
                List<EventRect> reversedEventRects = mEventRects;
                Collections.reverse(reversedEventRects);
                for (EventRect event : reversedEventRects) {
                    if (event.rectF != null && e.getX() > event.rectF.left && e.getX() < event.rectF.right && e.getY() > event.rectF.top && e.getY() < event.rectF.bottom) {
                        mEventLongPressListener.onEventLongPress(event.originalEvent, event.rectF);
                        performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                        return;
                    }
                }
            }

            // If the tap was on in an empty space, then trigger the callback.
            if (mEmptyViewLongPressListener != null && e.getX() > mHeaderColumnWidth && e.getY() > (mHeaderHeight + mHeaderRowPadding * 2 + mHeaderMarginBottom)) {
                Calendar selectedTime = getTimeFromPoint(e.getX(), e.getY());
                if (selectedTime != null) {
                    performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                    mEmptyViewLongPressListener.onEmptyViewLongPress(selectedTime);
                }
            }
        }
    };

    public WeekView(Context context) {
        this(context, null);
    }

    public WeekView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WeekView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        // Hold references.
        mContext = context;

        // Get the attribute values (if any).
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.WeekView, 0, 0);
        try {
            mFirstDayOfWeek = a.getInteger(R.styleable.WeekView_firstDayOfWeek, mFirstDayOfWeek);
            mHourHeight = a.getDimensionPixelSize(R.styleable.WeekView_hourHeight, mHourHeight);
            mMinHourHeight = a.getDimensionPixelSize(R.styleable.WeekView_minHourHeight, mMinHourHeight);
            mEffectiveMinHourHeight = mMinHourHeight;
            mMaxHourHeight = a.getDimensionPixelSize(R.styleable.WeekView_maxHourHeight, mMaxHourHeight);
            mTextSize = a.getDimensionPixelSize(R.styleable.WeekView_textSize, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, mTextSize, context.getResources().getDisplayMetrics()));
            mHeaderColumnPadding = a.getDimensionPixelSize(R.styleable.WeekView_headerColumnPadding, mHeaderColumnPadding);
            mColumnGap = a.getDimensionPixelSize(R.styleable.WeekView_columnGap, mColumnGap);
            mHeaderColumnTextColor = a.getColor(R.styleable.WeekView_headerColumnTextColor, mHeaderColumnTextColor);
            mNumberOfVisibleDays = a.getInteger(R.styleable.WeekView_noOfVisibleDays, mNumberOfVisibleDays);
            mShowFirstDayOfWeekFirst = a.getBoolean(R.styleable.WeekView_showFirstDayOfWeekFirst, mShowFirstDayOfWeekFirst);
            mHeaderRowPadding = a.getDimensionPixelSize(R.styleable.WeekView_headerRowPadding, mHeaderRowPadding);
            mHeaderRowBackgroundColor = a.getColor(R.styleable.WeekView_headerRowBackgroundColor, mHeaderRowBackgroundColor);
            mDayBackgroundColor = a.getColor(R.styleable.WeekView_dayBackgroundColor, mDayBackgroundColor);
            mFutureBackgroundColor = a.getColor(R.styleable.WeekView_futureBackgroundColor, mFutureBackgroundColor);
            mPastBackgroundColor = a.getColor(R.styleable.WeekView_pastBackgroundColor, mPastBackgroundColor);
            mFutureWeekendBackgroundColor = a.getColor(R.styleable.WeekView_futureWeekendBackgroundColor, mFutureBackgroundColor); // If not set, use the same color as in the week
            mPastWeekendBackgroundColor = a.getColor(R.styleable.WeekView_pastWeekendBackgroundColor, mPastBackgroundColor);
            mNowLineColor = a.getColor(R.styleable.WeekView_nowLineColor, mNowLineColor);
            mNowLineThickness = a.getDimensionPixelSize(R.styleable.WeekView_nowLineThickness, mNowLineThickness);
            mHourSeparatorColor = a.getColor(R.styleable.WeekView_hourSeparatorColor, mHourSeparatorColor);
            mTodayBackgroundColor = a.getColor(R.styleable.WeekView_todayBackgroundColor, mTodayBackgroundColor);
            mHourSeparatorHeight = a.getDimensionPixelSize(R.styleable.WeekView_hourSeparatorHeight, mHourSeparatorHeight);
            mTodayHeaderTextColor = a.getColor(R.styleable.WeekView_todayHeaderTextColor, mTodayHeaderTextColor);
            mEventTextSize = a.getDimensionPixelSize(R.styleable.WeekView_eventTextSize, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, mEventTextSize, context.getResources().getDisplayMetrics()));
            mEventTextColor = a.getColor(R.styleable.WeekView_eventTextColor, mEventTextColor);
            mNewEventColor = a.getColor(R.styleable.WeekView_newEventColor, mNewEventColor);
            mNewEventIconDrawable = a.getDrawable(R.styleable.WeekView_newEventIconResource);
            // For backward compatibility : Set "mNewEventIdentifier" if the attribute is "WeekView_newEventId" of type int
            setNewEventId(a.getInt(R.styleable.WeekView_newEventId, Integer.parseInt(mNewEventIdentifier)));
            mNewEventIdentifier = (a.getString(R.styleable.WeekView_newEventIdentifier) != null) ? a.getString(R.styleable.WeekView_newEventIdentifier) : mNewEventIdentifier;
            mNewEventLengthInMinutes = a.getInt(R.styleable.WeekView_newEventLengthInMinutes, mNewEventLengthInMinutes);
            mNewEventTimeResolutionInMinutes = a.getInt(R.styleable.WeekView_newEventTimeResolutionInMinutes, mNewEventTimeResolutionInMinutes);
            mEventPadding = a.getDimensionPixelSize(R.styleable.WeekView_eventPadding, mEventPadding);
            mHeaderColumnBackgroundColor = a.getColor(R.styleable.WeekView_headerColumnBackground, mHeaderColumnBackgroundColor);
            mDayNameLength = a.getInteger(R.styleable.WeekView_dayNameLength, mDayNameLength);
            mOverlappingEventGap = a.getDimensionPixelSize(R.styleable.WeekView_overlappingEventGap, mOverlappingEventGap);
            mEventMarginVertical = a.getDimensionPixelSize(R.styleable.WeekView_eventMarginVertical, mEventMarginVertical);
            mXScrollingSpeed = a.getFloat(R.styleable.WeekView_xScrollingSpeed, mXScrollingSpeed);
            mEventCornerRadius = a.getDimensionPixelSize(R.styleable.WeekView_eventCornerRadius, mEventCornerRadius);
            mShowDistinctPastFutureColor = a.getBoolean(R.styleable.WeekView_showDistinctPastFutureColor, mShowDistinctPastFutureColor);
            mShowDistinctWeekendColor = a.getBoolean(R.styleable.WeekView_showDistinctWeekendColor, mShowDistinctWeekendColor);
            mShowNowLine = a.getBoolean(R.styleable.WeekView_showNowLine, mShowNowLine);
            mHorizontalFlingEnabled = a.getBoolean(R.styleable.WeekView_horizontalFlingEnabled, mHorizontalFlingEnabled);
            mVerticalFlingEnabled = a.getBoolean(R.styleable.WeekView_verticalFlingEnabled, mVerticalFlingEnabled);
            mAllDayEventHeight = a.getDimensionPixelSize(R.styleable.WeekView_allDayEventHeight, mAllDayEventHeight);
            mZoomFocusPoint = a.getFraction(R.styleable.WeekView_zoomFocusPoint, 1, 1, mZoomFocusPoint);
            mZoomFocusPointEnabled = a.getBoolean(R.styleable.WeekView_zoomFocusPointEnabled, mZoomFocusPointEnabled);
            mScrollDuration = a.getInt(R.styleable.WeekView_scrollDuration, mScrollDuration);
            mTimeColumnResolution = a.getInt(R.styleable.WeekView_timeColumnResolution, mTimeColumnResolution);
            mAutoLimitTime = a.getBoolean(R.styleable.WeekView_autoLimitTime, mAutoLimitTime);
            mMinTime = a.getInt(R.styleable.WeekView_minTime, mMinTime);
            mMaxTime = a.getInt(R.styleable.WeekView_maxTime, mMaxTime);
            if (a.getBoolean(R.styleable.WeekView_dropListenerEnabled, false)) this.enableDropListener();
            mMinOverlappingMinutes = a.getInt(R.styleable.WeekView_minOverlappingMinutes, 0);
        } finally {
            a.recycle();
        }

        init();
    }

    private void init() {
        resetHomeDate();

        // Scrolling initialization.
        mGestureDetector = new GestureDetectorCompat(mContext, mGestureListener);
        mScroller = new OverScroller(mContext, new FastOutLinearInInterpolator());

        mMinimumFlingVelocity = ViewConfiguration.get(mContext).getScaledMinimumFlingVelocity();
        mScaledTouchSlop = ViewConfiguration.get(mContext).getScaledTouchSlop();

        // Measure settings for time column.
        mTimeTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTimeTextPaint.setTextAlign(Paint.Align.RIGHT);
        mTimeTextPaint.setTextSize(mTextSize);
        mTimeTextPaint.setColor(mHeaderColumnTextColor);

        Rect rect = new Rect();
        final String exampleTime = (mTimeColumnResolution % 60 != 0) ? "00:00 PM" : "00 PM";
        mTimeTextPaint.getTextBounds(exampleTime, 0, exampleTime.length(), rect);
        mTimeTextWidth = mTimeTextPaint.measureText(exampleTime);
        mTimeTextHeight = rect.height();
        mHeaderMarginBottom = mTimeTextHeight / 2;
        initTextTimeWidth();

        // Measure settings for header row.
        mHeaderTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mHeaderTextPaint.setColor(mHeaderColumnTextColor);
        mHeaderTextPaint.setTextAlign(Paint.Align.CENTER);
        mHeaderTextPaint.setTextSize(mTextSize);
        mHeaderTextPaint.getTextBounds(exampleTime, 0, exampleTime.length(), rect);
        mHeaderTextHeight = rect.height();
        mHeaderTextPaint.setTypeface(mTypeface);


        // Prepare header background paint.
        mHeaderBackgroundPaint = new Paint();
        mHeaderBackgroundPaint.setColor(mHeaderRowBackgroundColor);

        // Prepare day background color paint.
        mDayBackgroundPaint = new Paint();
        mDayBackgroundPaint.setColor(mDayBackgroundColor);
        mFutureBackgroundPaint = new Paint();
        mFutureBackgroundPaint.setColor(mFutureBackgroundColor);
        mPastBackgroundPaint = new Paint();
        mPastBackgroundPaint.setColor(mPastBackgroundColor);
        mFutureWeekendBackgroundPaint = new Paint();
        mFutureWeekendBackgroundPaint.setColor(mFutureWeekendBackgroundColor);
        mPastWeekendBackgroundPaint = new Paint();
        mPastWeekendBackgroundPaint.setColor(mPastWeekendBackgroundColor);

        // Prepare hour separator color paint.
        mHourSeparatorPaint = new Paint();
        mHourSeparatorPaint.setStyle(Paint.Style.STROKE);
        mHourSeparatorPaint.setStrokeWidth(mHourSeparatorHeight);
        mHourSeparatorPaint.setColor(mHourSeparatorColor);

        // Prepare the "now" line color paint
        mNowLinePaint = new Paint();
        mNowLinePaint.setStrokeWidth(mNowLineThickness);
        mNowLinePaint.setColor(mNowLineColor);

        // Prepare today background color paint.
        mTodayBackgroundPaint = new Paint();
        mTodayBackgroundPaint.setColor(mTodayBackgroundColor);

        // Prepare today header text color paint.
        mTodayHeaderTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTodayHeaderTextPaint.setTextAlign(Paint.Align.CENTER);
        mTodayHeaderTextPaint.setTextSize(mTextSize);
        mTodayHeaderTextPaint.setTypeface(mTypeface);

        mTodayHeaderTextPaint.setColor(mTodayHeaderTextColor);

        // Prepare event background color.
        mEventBackgroundPaint = new Paint();
        mEventBackgroundPaint.setColor(Color.rgb(174, 208, 238));
        // Prepare empty event background color.
        mNewEventBackgroundPaint = new Paint();
        mNewEventBackgroundPaint.setColor(Color.rgb(60, 147, 217));

        // Prepare header column background color.
        mHeaderColumnBackgroundPaint = new Paint();
        mHeaderColumnBackgroundPaint.setColor(mHeaderColumnBackgroundColor);

        // Prepare event text size and color.
        mEventTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG);
        mEventTextPaint.setStyle(Paint.Style.FILL);
        mEventTextPaint.setColor(mEventTextColor);
        mEventTextPaint.setTextSize(mEventTextSize);

        // Set default event color.
        mDefaultEventColor = Color.parseColor("#9fc6e7");
        // Set default empty event color.
        mNewEventColor = Color.parseColor("#3c93d9");

        mScaleDetector = new ScaleGestureDetector(mContext, new WeekViewGestureListener());
    }

    private void resetHomeDate() {
        Calendar newHomeDate = today();

        if (mMinDate != null && newHomeDate.before(mMinDate)) {
            newHomeDate = (Calendar) mMinDate.clone();
        }
        if (mMaxDate != null && newHomeDate.after(mMaxDate)) {
            newHomeDate = (Calendar) mMaxDate.clone();
        }

        if (mMaxDate != null) {
            Calendar date = (Calendar) mMaxDate.clone();
            date.add(Calendar.DATE, 1 - getRealNumberOfVisibleDays());
            while (date.before(mMinDate)) {
                date.add(Calendar.DATE, 1);
            }

            if (newHomeDate.after(date)) {
                newHomeDate = date;
            }
        }

        mHomeDate = newHomeDate;
    }

    private float getXOriginForDate(Calendar date) {
        return -daysBetween(mHomeDate, date) * (mWidthPerDay + mColumnGap);
    }

    private int getNumberOfPeriods() {
        return (int) ((mMaxTime - mMinTime) * (60.0 / mTimeColumnResolution));
    }

    private float getYMinLimit() {
        return -(mHourHeight * (mMaxTime - mMinTime) + mHeaderHeight + mHeaderRowPadding * 2 + mHeaderMarginBottom + mTimeTextHeight / 2 - getHeight());
    }

    private float getYMaxLimit() {
        return 0;
    }

    private float getXMinLimit() {
        if (mMaxDate == null) {
            return Integer.MIN_VALUE;
        } else {
            Calendar date = (Calendar) mMaxDate.clone();
            date.add(Calendar.DATE, 1 - getRealNumberOfVisibleDays());
            while (date.before(mMinDate)) {
                date.add(Calendar.DATE, 1);
            }

            return getXOriginForDate(date);
        }
    }

    private float getXMaxLimit() {
        if (mMinDate == null) {
            return Integer.MAX_VALUE;
        } else {
            return getXOriginForDate(mMinDate);
        }
    }

    // fix rotation changes
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mAreDimensionsInvalid = true;
    }

    /**
     * Initialize time column width. Calculate value with all possible hours (supposed widest text).
     */
    private void initTextTimeWidth() {
        mTimeTextWidth = 0;
        for (int i = 0; i < getNumberOfPeriods(); i++) {
            // Measure time string and get max width.
            String time = getDateTimeInterpreter().interpretTime(i, (i % 2) * 30);
            if (time == null) throw new IllegalStateException("A DateTimeInterpreter must not return null time");
            mTimeTextWidth = Math.max(mTimeTextWidth, mTimeTextPaint.measureText(time));
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw the header row.
        drawHeaderRowAndEvents(canvas);

        // Draw the time column and all the axes/separators.
        drawTimeColumnAndAxes(canvas);
    }

    private void calculateHeaderHeight() {
        //Make sure the header is the right size (depends on AllDay events)
        boolean containsAllDayEvent = false;
        if (mEventRects != null && mEventRects.size() > 0) {
            for (int dayNumber = 0; dayNumber < getRealNumberOfVisibleDays(); dayNumber++) {
                Calendar day = (Calendar) getFirstVisibleDay().clone();
                day.add(Calendar.DATE, dayNumber);
                for (int i = 0; i < mEventRects.size(); i++) {

                    if (isSameDay(mEventRects.get(i).event.getStartTime(), day) && mEventRects.get(i).event.isAllDay()) {
                        containsAllDayEvent = true;
                        break;
                    }
                }
                if (containsAllDayEvent) {
                    break;
                }
            }
        }
        if (containsAllDayEvent) {
            mHeaderHeight = mHeaderTextHeight + (mAllDayEventHeight + mHeaderMarginBottom);
        } else {
            mHeaderHeight = mHeaderTextHeight;
        }
    }

    private void drawTimeColumnAndAxes(Canvas canvas) {
        // Draw the background color for the header column.
        canvas.drawRect(0, mHeaderHeight + mHeaderRowPadding * 2, mHeaderColumnWidth, getHeight(), mHeaderColumnBackgroundPaint);

        // Clip to paint in left column only.
        canvas.save();
        canvas.clipRect(0, mHeaderHeight + mHeaderRowPadding * 2, mHeaderColumnWidth, getHeight());

        for (int i = 0; i < getNumberOfPeriods(); i++) {
            // If we are showing half hours (eg. 5:30am), space the times out by half the hour height
            // and need to provide 30 minutes on each odd period, otherwise, minutes is always 0.
            float timeSpacing;
            int minutes;
            int hour;

            float timesPerHour = (float) 60.0 / mTimeColumnResolution;

            timeSpacing = mHourHeight / timesPerHour;
            hour = mMinTime + i / (int) (timesPerHour);
            minutes = i % ((int) timesPerHour) * (60 / (int) timesPerHour);


            // Calculate the top of the rectangle where the time text will go
            float top = mHeaderHeight + mHeaderRowPadding * 2 + mCurrentOrigin.y + timeSpacing * i + mHeaderMarginBottom;

            // Get the time to be displayed, as a String.
            String time = getDateTimeInterpreter().interpretTime(hour, minutes);
            // Draw the text if its y position is not outside of the visible area. The pivot point of the text is the point at the bottom-right corner.
            if (time == null) throw new IllegalStateException("A DateTimeInterpreter must not return null time");
            if (top < getHeight())
                canvas.drawText(time, mTimeTextWidth + mHeaderColumnPadding, top + mTimeTextHeight, mTimeTextPaint);
        }
        canvas.restore();
    }

    private void drawHeaderRowAndEvents(Canvas canvas) {
        // Calculate the available width for each day.
        mHeaderColumnWidth = mTimeTextWidth + mHeaderColumnPadding * 2;
        mWidthPerDay = getWidth() - mHeaderColumnWidth - mColumnGap * (getRealNumberOfVisibleDays() - 1);
        mWidthPerDay = mWidthPerDay / getRealNumberOfVisibleDays();

        calculateHeaderHeight(); //Make sure the header is the right size (depends on AllDay events)

        Calendar today = today();

        if (mAreDimensionsInvalid) {
            mEffectiveMinHourHeight = Math.max(mMinHourHeight, (int) ((getHeight() - mHeaderHeight - mHeaderRowPadding * 2 - mHeaderMarginBottom) / (mMaxTime - mMinTime)));

            mAreDimensionsInvalid = false;
            if (mScrollToDay != null) goToDate(mScrollToDay);

            mAreDimensionsInvalid = false;
            if (mScrollToHour >= 0) goToHour(mScrollToHour);

            mScrollToDay = null;
            mScrollToHour = -1;
            mAreDimensionsInvalid = false;
        }
        if (mIsFirstDraw) {
            mIsFirstDraw = false;

            // If the week view is being drawn for the first time, then consider the first day of the week.
            if (getRealNumberOfVisibleDays() >= 7 && mHomeDate.get(Calendar.DAY_OF_WEEK) != mFirstDayOfWeek && mShowFirstDayOfWeekFirst) {
                int difference = (mHomeDate.get(Calendar.DAY_OF_WEEK) - mFirstDayOfWeek);
                mCurrentOrigin.x += (mWidthPerDay + mColumnGap) * difference;
            }
            setLimitTime(mMinTime, mMaxTime);
        }

        // Calculate the new height due to the zooming.
        if (mNewHourHeight > 0) {
            if (mNewHourHeight < mEffectiveMinHourHeight) mNewHourHeight = mEffectiveMinHourHeight;
            else if (mNewHourHeight > mMaxHourHeight) mNewHourHeight = mMaxHourHeight;

            mHourHeight = mNewHourHeight;
            mNewHourHeight = -1;
        }

        // If the new mCurrentOrigin.y is invalid, make it valid.
        if (mCurrentOrigin.y < getHeight() - mHourHeight * (mMaxTime - mMinTime) - mHeaderHeight - mHeaderRowPadding * 2 - mHeaderMarginBottom - mTimeTextHeight / 2)
            mCurrentOrigin.y = getHeight() - mHourHeight * (mMaxTime - mMinTime) - mHeaderHeight - mHeaderRowPadding * 2 - mHeaderMarginBottom - mTimeTextHeight / 2;

        // Don't put an "else if" because it will trigger a glitch when completely zoomed out and
        // scrolling vertically.
        if (mCurrentOrigin.y > 0) {
            mCurrentOrigin.y = 0;
        }

        int leftDaysWithGaps = getLeftDaysWithGaps();
        // Consider scroll offset.
        float startFromPixel = getXStartPixel();
        float startPixel = startFromPixel;

        // Prepare to iterate for each day.
        Calendar day = (Calendar) today.clone();
        day.add(Calendar.HOUR_OF_DAY, 6);

        // Prepare to iterate for each hour to draw the hour lines.
        int lineCount = (int) ((getHeight() - mHeaderHeight - mHeaderRowPadding * 2 - mHeaderMarginBottom) / mHourHeight) + 1;

        lineCount = (lineCount) * (getRealNumberOfVisibleDays() + 1);

        float[] hourLines = new float[lineCount * 4];

        // Clear the cache for event rectangles.
        if (mEventRects != null) {
            for (EventRect eventRect : mEventRects) {
                eventRect.rectF = null;
            }
        }

        // Clip to paint events only.
        canvas.save();
        canvas.clipRect(mHeaderColumnWidth, mHeaderHeight + mHeaderRowPadding * 2 + mHeaderMarginBottom + mTimeTextHeight / 2, getWidth(), getHeight());

        // Iterate through each day.
        Calendar oldFirstVisibleDay = mFirstVisibleDay;
        mFirstVisibleDay = (Calendar) mHomeDate.clone();
        mFirstVisibleDay.add(Calendar.DATE, -(Math.round(mCurrentOrigin.x / (mWidthPerDay + mColumnGap))));
        if (!mFirstVisibleDay.equals(oldFirstVisibleDay) && mScrollListener != null) {
            mScrollListener.onFirstVisibleDayChanged(mFirstVisibleDay, oldFirstVisibleDay);
        }

        if (mAutoLimitTime) {
            List<Calendar> days = new ArrayList<>();
            for (int dayNumber = leftDaysWithGaps + 1; dayNumber <= leftDaysWithGaps + getRealNumberOfVisibleDays(); dayNumber++) {
                day = (Calendar) mHomeDate.clone();
                day.add(Calendar.DATE, dayNumber - 1);
                days.add(day);
            }
            limitEventTime(days);
        }

        for (int dayNumber = leftDaysWithGaps + 1; dayNumber <= leftDaysWithGaps + getRealNumberOfVisibleDays() + 1; dayNumber++) {

            // Check if the day is today.
            day = (Calendar) mHomeDate.clone();
            mLastVisibleDay = (Calendar) day.clone();
            day.add(Calendar.DATE, dayNumber - 1);
            mLastVisibleDay.add(Calendar.DATE, dayNumber - 2);
            boolean isToday = isSameDay(day, today);

            // Don't draw days which are outside requested range
            if (!dateIsValid(day)) {
                continue;
            }

            // Get more events if necessary. We want to store the events 3 months beforehand. Get
            // events only when it is the first iteration of the loop.
            if (mEventRects == null || mRefreshEvents || (dayNumber == leftDaysWithGaps + 1 && mFetchedPeriod != (int) mWeekViewLoader.toWeekViewPeriodIndex(day) && Math.abs(mFetchedPeriod - mWeekViewLoader.toWeekViewPeriodIndex(day)) > 0.5)) {
                getMoreEvents(day);
                mRefreshEvents = false;
            }

            // Draw background color for each day.
            float start = Math.max(startPixel, mHeaderColumnWidth);
            if (mWidthPerDay + startPixel - start > 0) {
                if (mShowDistinctPastFutureColor) {
                    boolean isWeekend = day.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || day.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY;
                    Paint pastPaint = isWeekend && mShowDistinctWeekendColor ? mPastWeekendBackgroundPaint : mPastBackgroundPaint;
                    Paint futurePaint = isWeekend && mShowDistinctWeekendColor ? mFutureWeekendBackgroundPaint : mFutureBackgroundPaint;
                    float startY = mHeaderHeight + mHeaderRowPadding * 2 + mTimeTextHeight / 2 + mHeaderMarginBottom + mCurrentOrigin.y;

                    if (isToday) {
                        Calendar now = Calendar.getInstance();
                        float beforeNow = (now.get(Calendar.HOUR_OF_DAY) - mMinTime + now.get(Calendar.MINUTE) / 60.0f) * mHourHeight;
                        canvas.drawRect(start, startY, startPixel + mWidthPerDay, startY + beforeNow, pastPaint);
                        canvas.drawRect(start, startY + beforeNow, startPixel + mWidthPerDay, getHeight(), futurePaint);
                    } else if (day.before(today)) {
                        canvas.drawRect(start, startY, startPixel + mWidthPerDay, getHeight(), pastPaint);
                    } else {
                        canvas.drawRect(start, startY, startPixel + mWidthPerDay, getHeight(), futurePaint);
                    }
                } else {
                    canvas.drawRect(start, mHeaderHeight + mHeaderRowPadding * 2 + mTimeTextHeight / 2 + mHeaderMarginBottom, startPixel + mWidthPerDay, getHeight(), isToday ? mTodayBackgroundPaint : mDayBackgroundPaint);
                }
            }

            // Prepare the separator lines for hours.
            int i = 0;
            for (int hourNumber = mMinTime; hourNumber < mMaxTime; hourNumber++) {
                float top = mHeaderHeight + mHeaderRowPadding * 2 + mCurrentOrigin.y + mHourHeight * (hourNumber - mMinTime) + mTimeTextHeight / 2 + mHeaderMarginBottom;
                if (top > mHeaderHeight + mHeaderRowPadding * 2 + mTimeTextHeight / 2 + mHeaderMarginBottom - mHourSeparatorHeight && top < getHeight() && startPixel + mWidthPerDay - start > 0) {
                    hourLines[i * 4] = start;
                    hourLines[i * 4 + 1] = top;
                    hourLines[i * 4 + 2] = startPixel + mWidthPerDay;
                    hourLines[i * 4 + 3] = top;
                    i++;
                }
            }

            // Draw the lines for hours.
            canvas.drawLines(hourLines, mHourSeparatorPaint);

            // Draw the events.
            drawEvents(day, startPixel, canvas);

            // Draw the line at the current time.
            if (mShowNowLine && isToday) {
                float startY = mHeaderHeight + mHeaderRowPadding * 2 + mTimeTextHeight / 2 + mHeaderMarginBottom + mCurrentOrigin.y;
                Calendar now = Calendar.getInstance();
                float beforeNow = (now.get(Calendar.HOUR_OF_DAY) - mMinTime + now.get(Calendar.MINUTE) / 60.0f) * mHourHeight;
                float top = startY + beforeNow;
                canvas.drawLine(start, top, startPixel + mWidthPerDay, top, mNowLinePaint);
            }

            // In the next iteration, start from the next day.
            startPixel += mWidthPerDay + mColumnGap;
        }
        canvas.restore();

        // Hide everything in the first cell (top left corner).
        canvas.save();
        canvas.clipRect(0, 0, mTimeTextWidth + mHeaderColumnPadding * 2, mHeaderHeight + mHeaderRowPadding * 2);
        canvas.drawRect(0, 0, mTimeTextWidth + mHeaderColumnPadding * 2, mHeaderHeight + mHeaderRowPadding * 2, mHeaderBackgroundPaint);
        canvas.restore();

        // Clip to paint header row only.
        canvas.save();
        canvas.clipRect(mHeaderColumnWidth, 0, getWidth(), mHeaderHeight + mHeaderRowPadding * 2);

        // Draw the header background.
        canvas.drawRect(0, 0, getWidth(), mHeaderHeight + mHeaderRowPadding * 2, mHeaderBackgroundPaint);

        // Draw the header row texts.
        startPixel = startFromPixel;
        for (int dayNumber = leftDaysWithGaps + 1; dayNumber <= leftDaysWithGaps + getRealNumberOfVisibleDays() + 1; dayNumber++) {
            // Check if the day is today.
            day = (Calendar) mHomeDate.clone();
            day.add(Calendar.DATE, dayNumber - 1);
            boolean isToday = isSameDay(day, today);

            // Don't draw days which are outside requested range
            if (!dateIsValid(day)) continue;

            // Draw the day labels.
            String dayLabel = getDateTimeInterpreter().interpretDate(day);
            if (dayLabel == null) throw new IllegalStateException("A DateTimeInterpreter must not return null date");
            canvas.drawText(dayLabel, startPixel + mWidthPerDay / 2, mHeaderTextHeight + mHeaderRowPadding, isToday ? mTodayHeaderTextPaint : mHeaderTextPaint);
            drawAllDayEvents(day, startPixel, canvas);
            startPixel += mWidthPerDay + mColumnGap;
        }
        canvas.restore();
    }

    /**
     * Get the time and date where the user clicked on.
     *
     * @param x The x position of the touch event.
     * @param y The y position of the touch event.
     * @return The time and date at the clicked position.
     */
    private Calendar getTimeFromPoint(float x, float y) {
        int leftDaysWithGaps = getLeftDaysWithGaps();
        float startPixel = getXStartPixel();
        for (int dayNumber = leftDaysWithGaps + 1; dayNumber <= leftDaysWithGaps + getRealNumberOfVisibleDays() + 1; dayNumber++) {
            float start = Math.max(mHeaderColumnWidth, startPixel);
            if (mWidthPerDay + startPixel - start > 0 && x > start && x < startPixel + mWidthPerDay) {
                Calendar day = (Calendar) mHomeDate.clone();
                day.add(Calendar.DATE, dayNumber - 1);
                float pixelsFromZero = y - mCurrentOrigin.y - mHeaderHeight - mHeaderRowPadding * 2 - mTimeTextHeight / 2 - mHeaderMarginBottom;
                int hour = (int) (pixelsFromZero / mHourHeight);
                int minute = (int) (60 * (pixelsFromZero - hour * mHourHeight) / mHourHeight);
                day.add(Calendar.HOUR_OF_DAY, hour + mMinTime);
                day.set(Calendar.MINUTE, minute);
                return day;
            }
            startPixel += mWidthPerDay + mColumnGap;
        }
        return null;
    }

    /**
     * limit current time of event by update mMinTime & mMaxTime
     * find smallest of start time & latest of end time
     */
    private void limitEventTime(List<Calendar> dates) {
        if (mEventRects != null && mEventRects.size() > 0) {
            Calendar startTime = null;
            Calendar endTime = null;

            for (EventRect eventRect : mEventRects) {
                for (Calendar date : dates) {
                    if (isSameDay(eventRect.event.getStartTime(), date) && !eventRect.event.isAllDay()) {

                        if (startTime == null || getPassedMinutesInDay(startTime) > getPassedMinutesInDay(eventRect.event.getStartTime())) {
                            startTime = eventRect.event.getStartTime();
                        }

                        if (endTime == null || getPassedMinutesInDay(endTime) < getPassedMinutesInDay(eventRect.event.getEndTime())) {
                            endTime = eventRect.event.getEndTime();
                        }
                    }
                }
            }

            if (startTime != null && endTime != null && startTime.before(endTime)) {
                setLimitTime(Math.max(0, startTime.get(Calendar.HOUR_OF_DAY)), Math.min(24, endTime.get(Calendar.HOUR_OF_DAY) + 1));
            }
        }
    }

    private int getMinHourOffset() {
        return mHourHeight * mMinTime;
    }

    private float getEventsTop() {
        // Calculate top.
        return mCurrentOrigin.y + mHeaderHeight + mHeaderRowPadding * 2 + mHeaderMarginBottom + mTimeTextHeight / 2 + mEventMarginVertical - getMinHourOffset();

    }

    private int getLeftDaysWithGaps() {
        return (int) -(Math.ceil(mCurrentOrigin.x / (mWidthPerDay + mColumnGap)));
    }

    private float getXStartPixel() {
        return mCurrentOrigin.x + (mWidthPerDay + mColumnGap) * getLeftDaysWithGaps() + mHeaderColumnWidth;
    }

    /**
     * Draw all the events of a particular day.
     *
     * @param date           The day.
     * @param startFromPixel The left position of the day area. The events will never go any left from this value.
     * @param canvas         The canvas to draw upon.
     */
    private void drawEvents(Calendar date, float startFromPixel, Canvas canvas) {
        if (mEventRects != null && mEventRects.size() > 0) {
            for (int i = 0; i < mEventRects.size(); i++) {
                if (isSameDay(mEventRects.get(i).event.getStartTime(), date) && !mEventRects.get(i).event.isAllDay()) {
                    float top = mHourHeight * mEventRects.get(i).top / 60 + getEventsTop();
                    float bottom = mHourHeight * mEventRects.get(i).bottom / 60 + getEventsTop();

                    // Calculate left and right.
                    float left = startFromPixel + mEventRects.get(i).left * mWidthPerDay;
                    if (left < startFromPixel) left += mOverlappingEventGap;
                    float right = left + mEventRects.get(i).width * mWidthPerDay;
                    if (right < startFromPixel + mWidthPerDay) right -= mOverlappingEventGap;

                    // Draw the event and the event name on top of it.
                    if (left < right && left < getWidth() && top < getHeight() && right > mHeaderColumnWidth && bottom > mHeaderHeight + mHeaderRowPadding * 2 + mTimeTextHeight / 2 + mHeaderMarginBottom) {
                        mEventRects.get(i).rectF = new RectF(left, top, right, bottom);
                        mEventBackgroundPaint.setColor(mEventRects.get(i).event.getColor() == 0 ? mDefaultEventColor : mEventRects.get(i).event.getColor());
                        mEventBackgroundPaint.setShader(mEventRects.get(i).event.getShader());
                        canvas.drawRoundRect(mEventRects.get(i).rectF, mEventCornerRadius, mEventCornerRadius, mEventBackgroundPaint);
                        float topToUse = top;
                        if (mEventRects.get(i).event.getStartTime().get(Calendar.HOUR_OF_DAY) < mMinTime)
                            topToUse = mHourHeight * getPassedMinutesInDay(mMinTime, 0) / 60.0f + getEventsTop();

                        if (!mNewEventIdentifier.equals(mEventRects.get(i).event.getIdentifier()))
                            drawEventTitle(mEventRects.get(i).event, mEventRects.get(i).rectF, canvas, topToUse, left);
                        else drawEmptyImage(mEventRects.get(i).event, mEventRects.get(i).rectF, canvas, topToUse, left);

                    } else mEventRects.get(i).rectF = null;
                }
            }
        }
    }

    /**
     * Draw all the Allday-events of a particular day.
     *
     * @param date           The day.
     * @param startFromPixel The left position of the day area. The events will never go any left from this value.
     * @param canvas         The canvas to draw upon.
     */
    private void drawAllDayEvents(Calendar date, float startFromPixel, Canvas canvas) {
        if (mEventRects != null && mEventRects.size() > 0) {
            for (int i = 0; i < mEventRects.size(); i++) {
                if (isSameDay(mEventRects.get(i).event.getStartTime(), date) && mEventRects.get(i).event.isAllDay()) {

                    // Calculate top.
                    float top = mHeaderRowPadding * 2 + mHeaderMarginBottom + +mTimeTextHeight / 2 + mEventMarginVertical;

                    // Calculate bottom.
                    float bottom = top + mEventRects.get(i).bottom;

                    // Calculate left and right.
                    float left = startFromPixel + mEventRects.get(i).left * mWidthPerDay;
                    if (left < startFromPixel) left += mOverlappingEventGap;
                    float right = left + mEventRects.get(i).width * mWidthPerDay;
                    if (right < startFromPixel + mWidthPerDay) right -= mOverlappingEventGap;

                    // Draw the event and the event name on top of it.
                    if (left < right && left < getWidth() && top < getHeight() && right > mHeaderColumnWidth && bottom > 0) {
                        mEventRects.get(i).rectF = new RectF(left, top, right, bottom);
                        mEventBackgroundPaint.setColor(mEventRects.get(i).event.getColor() == 0 ? mDefaultEventColor : mEventRects.get(i).event.getColor());
                        mEventBackgroundPaint.setShader(mEventRects.get(i).event.getShader());
                        canvas.drawRoundRect(mEventRects.get(i).rectF, mEventCornerRadius, mEventCornerRadius, mEventBackgroundPaint);
                        drawEventTitle(mEventRects.get(i).event, mEventRects.get(i).rectF, canvas, top, left);
                    } else mEventRects.get(i).rectF = null;
                }
            }
        }
    }

    /**
     * Draw the name of the event on top of the event rectangle.
     *
     * @param event        The event of which the title (and location) should be drawn.
     * @param rect         The rectangle on which the text is to be drawn.
     * @param canvas       The canvas to draw upon.
     * @param originalTop  The original top position of the rectangle. The rectangle may have some of its portion outside of the visible area.
     * @param originalLeft The original left position of the rectangle. The rectangle may have some of its portion outside of the visible area.
     */
    private void drawEventTitle(WeekViewEvent event, RectF rect, Canvas canvas, float originalTop, float originalLeft) {
        if (rect.right - rect.left - mEventPadding * 2 < 0) return;
        if (rect.bottom - rect.top - mEventPadding * 2 < 0) return;

        // Prepare the name of the event.
        SpannableStringBuilder bob = new SpannableStringBuilder();
        if (!TextUtils.isEmpty(event.getName())) {
            bob.append(event.getName());
            bob.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, bob.length(), 0);
        }
        // Prepare the location of the event.
        if (!TextUtils.isEmpty(event.getLocation())) {
            if (bob.length() > 0) bob.append(' ');
            bob.append(event.getLocation());
        }

        int availableHeight = (int) (rect.bottom - originalTop - mEventPadding * 2);
        int availableWidth = (int) (rect.right - originalLeft - mEventPadding * 2);

        // Get text color if necessary
        if (textColorPicker != null) {
            mEventTextPaint.setColor(textColorPicker.getTextColor(event));
        }
        // Get text dimensions.
        StaticLayout textLayout = new StaticLayout(bob, mEventTextPaint, availableWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
        if (textLayout.getLineCount() > 0) {
            int lineHeight = textLayout.getHeight() / textLayout.getLineCount();

            if (availableHeight >= lineHeight) {
                // Calculate available number of line counts.
                int availableLineCount = availableHeight / lineHeight;
                do {
                    // Ellipsize text to fit into event rect.
                    if (!mNewEventIdentifier.equals(event.getIdentifier()))
                        textLayout = new StaticLayout(TextUtils.ellipsize(bob, mEventTextPaint, availableLineCount * availableWidth, TextUtils.TruncateAt.END), mEventTextPaint, (int) (rect.right - originalLeft - mEventPadding * 2), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);

                    // Reduce line count.
                    availableLineCount--;

                    // Repeat until text is short enough.
                } while (textLayout.getHeight() > availableHeight);

                // Draw text.
                canvas.save();
                canvas.translate(originalLeft + mEventPadding, originalTop + mEventPadding);
                textLayout.draw(canvas);
                canvas.restore();
            }
        }
    }

    /**
     * Draw the text on top of the rectangle in the empty event.
     */
    private void drawEmptyImage(WeekViewEvent event, RectF rect, Canvas canvas, float originalTop, float originalLeft) {
        int size = Math.max(1, (int) Math.floor(Math.min(0.8 * rect.height(), 0.8 * rect.width())));
        if (mNewEventIconDrawable == null)
            mNewEventIconDrawable = getResources().getDrawable(android.R.drawable.ic_input_add);
        Bitmap icon = ((BitmapDrawable) mNewEventIconDrawable).getBitmap();
        icon = Bitmap.createScaledBitmap(icon, size, size, false);
        canvas.drawBitmap(icon, originalLeft + (rect.width() - icon.getWidth()) / 2, originalTop + (rect.height() - icon.getHeight()) / 2, new Paint());

    }

    /**
     * A class to hold reference to the events and their visual representation. An EventRect is
     * actually the rectangle that is drawn on the calendar for a given event. There may be more
     * than one rectangle for a single event (an event that expands more than one day). In that
     * case two instances of the EventRect will be used for a single event. The given event will be
     * stored in "originalEvent". But the event that corresponds to rectangle the rectangle
     * instance will be stored in "event".
     */
    private class EventRect {
        public WeekViewEvent event;
        public WeekViewEvent originalEvent;
        public RectF rectF;
        public float left;
        public float width;
        public float top;
        public float bottom;

        /**
         * Create a new instance of event rect. An EventRect is actually the rectangle that is drawn
         * on the calendar for a given event. There may be more than one rectangle for a single
         * event (an event that expands more than one day). In that case two instances of the
         * EventRect will be used for a single event. The given event will be stored in
         * "originalEvent". But the event that corresponds to rectangle the rectangle instance will
         * be stored in "event".
         *
         * @param event         Represents the event which this instance of rectangle represents.
         * @param originalEvent The original event that was passed by the user.
         * @param rectF         The rectangle.
         */
        public EventRect(WeekViewEvent event, WeekViewEvent originalEvent, RectF rectF) {
            this.event = event;
            this.rectF = rectF;
            this.originalEvent = originalEvent;
        }
    }


    /**
     * Gets more events of one/more month(s) if necessary. This method is called when the user is
     * scrolling the week view. The week view stores the events of three months: the visible month,
     * the previous month, the next month.
     *
     * @param day The day where the user is currently is.
     */
    private void getMoreEvents(Calendar day) {

        // Get more events if the month is changed.
        if (mEventRects == null) mEventRects = new ArrayList<>();

        if (mEvents == null) mEvents = new ArrayList<>();

        if (mWeekViewLoader == null && !isInEditMode())
            throw new IllegalStateException("You must provide a MonthChangeListener");

        // If a refresh was requested then reset some variables.
        if (mRefreshEvents) {
            this.clearEvents();
            mFetchedPeriod = -1;
        }

        if (mWeekViewLoader != null) {
            int periodToFetch = (int) mWeekViewLoader.toWeekViewPeriodIndex(day);
            if (!isInEditMode() && (mFetchedPeriod < 0 || mFetchedPeriod != periodToFetch || mRefreshEvents)) {
                List<? extends WeekViewEvent> newEvents = mWeekViewLoader.onLoad(periodToFetch);

                // Clear events.
                this.clearEvents();
                cacheAndSortEvents(newEvents);
                calculateHeaderHeight();

                mFetchedPeriod = periodToFetch;
            }
        }

        // Prepare to calculate positions of each events.
        List<EventRect> tempEvents = mEventRects;
        mEventRects = new ArrayList<>();

        // Iterate through each day with events to calculate the position of the events.
        while (tempEvents.size() > 0) {
            ArrayList<EventRect> eventRects = new ArrayList<>(tempEvents.size());

            // Get first event for a day.
            EventRect eventRect1 = tempEvents.remove(0);
            eventRects.add(eventRect1);

            int i = 0;
            while (i < tempEvents.size()) {
                // Collect all other events for same day.
                EventRect eventRect2 = tempEvents.get(i);
                if (isSameDay(eventRect1.event.getStartTime(), eventRect2.event.getStartTime())) {
                    tempEvents.remove(i);
                    eventRects.add(eventRect2);
                } else {
                    i++;
                }
            }
            computePositionOfEvents(eventRects);
        }
    }

    private void clearEvents() {
        mEventRects.clear();
        mEvents.clear();
    }

    /**
     * Cache the event for smooth scrolling functionality.
     *
     * @param event The event to cache.
     */
    private void cacheEvent(WeekViewEvent event) {
        if (event.getStartTime().compareTo(event.getEndTime()) >= 0) return;
        List<WeekViewEvent> splitedEvents = event.splitWeekViewEvents();
        for (WeekViewEvent splitedEvent : splitedEvents) {
            mEventRects.add(new EventRect(splitedEvent, event, null));
        }

        mEvents.add(event);
    }

    /**
     * Cache and sort events.
     *
     * @param events The events to be cached and sorted.
     */
    private void cacheAndSortEvents(List<? extends WeekViewEvent> events) {
        for (WeekViewEvent event : events) {
            cacheEvent(event);
        }
        sortEventRects(mEventRects);
    }

    /**
     * Sorts the events in ascending order.
     *
     * @param eventRects The events to be sorted.
     */
    private void sortEventRects(List<EventRect> eventRects) {
        Collections.sort(eventRects, new Comparator<EventRect>() {
            @Override
            public int compare(EventRect left, EventRect right) {
                long start1 = left.event.getStartTime().getTimeInMillis();
                long start2 = right.event.getStartTime().getTimeInMillis();
                int comparator = start1 > start2 ? 1 : (start1 < start2 ? -1 : 0);
                if (comparator == 0) {
                    long end1 = left.event.getEndTime().getTimeInMillis();
                    long end2 = right.event.getEndTime().getTimeInMillis();
                    comparator = end1 > end2 ? 1 : (end1 < end2 ? -1 : 0);
                }
                return comparator;
            }
        });
    }

    /**
     * Calculates the left and right positions of each events. This comes handy specially if events
     * are overlapping.
     *
     * @param eventRects The events along with their wrapper class.
     */
    private void computePositionOfEvents(List<EventRect> eventRects) {
        // Make "collision groups" for all events that collide with others.
        List<List<EventRect>> collisionGroups = new ArrayList<List<EventRect>>();
        for (EventRect eventRect : eventRects) {
            boolean isPlaced = false;

            outerLoop:
            for (List<EventRect> collisionGroup : collisionGroups) {
                for (EventRect groupEvent : collisionGroup) {
                    if (isEventsCollide(groupEvent.event, eventRect.event) && groupEvent.event.isAllDay() == eventRect.event.isAllDay()) {
                        collisionGroup.add(eventRect);
                        isPlaced = true;
                        break outerLoop;
                    }
                }
            }

            if (!isPlaced) {
                List<EventRect> newGroup = new ArrayList<EventRect>();
                newGroup.add(eventRect);
                collisionGroups.add(newGroup);
            }
        }

        for (List<EventRect> collisionGroup : collisionGroups) {
            expandEventsToMaxWidth(collisionGroup);
        }
    }

    /**
     * Expands all the events to maximum possible width. The events will try to occupy maximum
     * space available horizontally.
     *
     * @param collisionGroup The group of events which overlap with each other.
     */
    private void expandEventsToMaxWidth(List<EventRect> collisionGroup) {
        // Expand the events to maximum possible width.
        List<List<EventRect>> columns = new ArrayList<List<EventRect>>();
        columns.add(new ArrayList<EventRect>());
        for (EventRect eventRect : collisionGroup) {
            boolean isPlaced = false;
            for (List<EventRect> column : columns) {
                if (column.size() == 0) {
                    column.add(eventRect);
                    isPlaced = true;
                } else if (!isEventsCollide(eventRect.event, column.get(column.size() - 1).event)) {
                    column.add(eventRect);
                    isPlaced = true;
                    break;
                }
            }
            if (!isPlaced) {
                List<EventRect> newColumn = new ArrayList<EventRect>();
                newColumn.add(eventRect);
                columns.add(newColumn);
            }
        }

        // Calculate left and right position for all the events.
        // Get the maxRowCount by looking in all columns.
        int maxRowCount = 0;
        for (List<EventRect> column : columns) {
            maxRowCount = Math.max(maxRowCount, column.size());
        }
        for (int i = 0; i < maxRowCount; i++) {
            // Set the left and right values of the event.
            float j = 0;
            for (List<EventRect> column : columns) {
                if (column.size() >= i + 1) {
                    EventRect eventRect = column.get(i);
                    eventRect.width = 1f / columns.size();
                    eventRect.left = j / columns.size();
                    if (!eventRect.event.isAllDay()) {
                        eventRect.top = getPassedMinutesInDay(eventRect.event.getStartTime());
                        eventRect.bottom = getPassedMinutesInDay(eventRect.event.getEndTime());
                    } else {
                        eventRect.top = 0;
                        eventRect.bottom = mAllDayEventHeight;
                    }
                    mEventRects.add(eventRect);
                }
                j++;
            }
        }
    }

    /**
     * Checks if two events overlap.
     *
     * @param event1 The first event.
     * @param event2 The second event.
     * @return true if the events overlap.
     */
    private boolean isEventsCollide(WeekViewEvent event1, WeekViewEvent event2) {
        long start1 = event1.getStartTime().getTimeInMillis();
        long end1 = event1.getEndTime().getTimeInMillis();
        long start2 = event2.getStartTime().getTimeInMillis();
        long end2 = event2.getEndTime().getTimeInMillis();

        long minOverlappingMillis = mMinOverlappingMinutes * 60 * 1000;

        return !((start1 + minOverlappingMillis >= end2) || (end1 <= start2 + minOverlappingMillis));
    }


    /**
     * Checks if time1 occurs after (or at the same time) time2.
     *
     * @param time1 The time to check.
     * @param time2 The time to check against.
     * @return true if time1 and time2 are equal or if time1 is after time2. Otherwise false.
     */
    private boolean isTimeAfterOrEquals(Calendar time1, Calendar time2) {
        return !(time1 == null || time2 == null) && time1.getTimeInMillis() >= time2.getTimeInMillis();
    }

    @Override
    public void invalidate() {
        super.invalidate();
        mAreDimensionsInvalid = true;
    }

    /////////////////////////////////////////////////////////////////
    //
    //      Functions related to setting and getting the properties.
    //
    /////////////////////////////////////////////////////////////////

    public void setOnEventClickListener(EventClickListener listener) {
        this.mEventClickListener = listener;
    }

    public void setDropListener(DropListener dropListener) {
        this.mDropListener = dropListener;
    }

    public EventClickListener getEventClickListener() {
        return mEventClickListener;
    }

    public @Nullable MonthLoader.MonthChangeListener getMonthChangeListener() {
        if (mWeekViewLoader instanceof MonthLoader) return ((MonthLoader) mWeekViewLoader).getOnMonthChangeListener();
        return null;
    }

    public void setMonthChangeListener(MonthLoader.MonthChangeListener monthChangeListener) {
        this.mWeekViewLoader = new MonthLoader(monthChangeListener);
    }

    /**
     * Get event loader in the week view. Event loaders define the  interval after which the events
     * are loaded in week view. For a MonthLoader events are loaded for every month. You can define
     * your custom event loader by extending WeekViewLoader.
     *
     * @return The event loader.
     */
    public WeekViewLoader getWeekViewLoader() {
        return mWeekViewLoader;
    }

    /**
     * Set event loader in the week view. For example, a MonthLoader. Event loaders define the
     * interval after which the events are loaded in week view. For a MonthLoader events are loaded
     * for every month. You can define your custom event loader by extending WeekViewLoader.
     *
     * @param loader The event loader.
     */
    public void setWeekViewLoader(WeekViewLoader loader) {
        this.mWeekViewLoader = loader;
    }

    public EventLongPressListener getEventLongPressListener() {
        return mEventLongPressListener;
    }

    public void setEventLongPressListener(EventLongPressListener eventLongPressListener) {
        this.mEventLongPressListener = eventLongPressListener;
    }

    public void setEmptyViewClickListener(EmptyViewClickListener emptyViewClickListener) {
        this.mEmptyViewClickListener = emptyViewClickListener;
    }

    public EmptyViewClickListener getEmptyViewClickListener() {
        return mEmptyViewClickListener;
    }

    public void setEmptyViewLongPressListener(EmptyViewLongPressListener emptyViewLongPressListener) {
        this.mEmptyViewLongPressListener = emptyViewLongPressListener;
    }

    public EmptyViewLongPressListener getEmptyViewLongPressListener() {
        return mEmptyViewLongPressListener;
    }

    public void setScrollListener(ScrollListener scrolledListener) {
        this.mScrollListener = scrolledListener;
    }

    public ScrollListener getScrollListener() {
        return mScrollListener;
    }

    public void setTimeColumnResolution(int resolution) {
        mTimeColumnResolution = resolution;
    }

    public int getTimeColumnResolution() {
        return mTimeColumnResolution;
    }

    public void setAddEventClickListener(AddEventClickListener addEventClickListener) {
        this.mAddEventClickListener = addEventClickListener;
    }

    public void setZoomEndListener(ZoomEndListener zoomEndListener) {
        this.mZoomEndListener = zoomEndListener;
    }

    public AddEventClickListener getAddEventClickListener() {
        return mAddEventClickListener;
    }

    /**
     * Get the interpreter which provides the text to show in the header column and the header row.
     *
     * @return The date, time interpreter.
     */
    public DateTimeInterpreter getDateTimeInterpreter() {
        if (mDateTimeInterpreter == null) {
            mDateTimeInterpreter = new DateTimeInterpreter() {
                @Override
                public String interpretDate(Calendar date) {
                    try {
                        SimpleDateFormat sdf = mDayNameLength == LENGTH_SHORT ? new SimpleDateFormat("EEEEE M/dd", Locale.getDefault()) : new SimpleDateFormat("EEE M/dd", Locale.getDefault());
                        return sdf.format(date.getTime()).toUpperCase();
                    } catch (Exception e) {
                        e.printStackTrace();
                        return "";
                    }
                }

                @Override
                public String interpretTime(int hour, int minutes) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.HOUR_OF_DAY, hour);
                    calendar.set(Calendar.MINUTE, minutes);

                    try {
                        SimpleDateFormat sdf;
                        if (DateFormat.is24HourFormat(getContext())) {
                            sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                        } else {
                            if ((mTimeColumnResolution % 60 != 0)) {
                                sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                            } else {
                                sdf = new SimpleDateFormat("hh a", Locale.getDefault());
                            }
                        }
                        return sdf.format(calendar.getTime());
                    } catch (Exception e) {
                        e.printStackTrace();
                        return "";
                    }
                }
            };
        }
        return mDateTimeInterpreter;
    }

    /**
     * Set the interpreter which provides the text to show in the header column and the header row.
     *
     * @param dateTimeInterpreter The date, time interpreter.
     */
    public void setDateTimeInterpreter(DateTimeInterpreter dateTimeInterpreter) {
        this.mDateTimeInterpreter = dateTimeInterpreter;

        // Refresh time column width.
        initTextTimeWidth();
    }


    /**
     * Get the real number of visible days
     * If the amount of days between max date and min date is smaller, that value is returned
     *
     * @return The real number of visible days
     */
    public int getRealNumberOfVisibleDays() {
        if (mMinDate == null || mMaxDate == null) return getNumberOfVisibleDays();

        return Math.min(mNumberOfVisibleDays, daysBetween(mMinDate, mMaxDate) + 1);
    }

    /**
     * Get the number of visible days
     *
     * @return The set number of visible days.
     */
    public int getNumberOfVisibleDays() {
        return mNumberOfVisibleDays;
    }

    /**
     * Set the number of visible days in a week.
     *
     * @param numberOfVisibleDays The number of visible days in a week.
     */
    public void setNumberOfVisibleDays(int numberOfVisibleDays) {
        this.mNumberOfVisibleDays = numberOfVisibleDays;
        resetHomeDate();
        mCurrentOrigin.x = 0;
        mCurrentOrigin.y = 0;
        invalidate();
    }

    public int getHourHeight() {
        return mHourHeight;
    }

    public void setHourHeight(int hourHeight) {
        mNewHourHeight = hourHeight;
        invalidate();
    }

    public int getColumnGap() {
        return mColumnGap;
    }

    public void setColumnGap(int columnGap) {
        mColumnGap = columnGap;
        invalidate();
    }

    public int getFirstDayOfWeek() {
        return mFirstDayOfWeek;
    }

    /**
     * Set the first day of the week. First day of the week is used only when the week view is first
     * drawn. It does not of any effect after user starts scrolling horizontally.
     * <p>
     * <b>Note:</b> This method will only work if the week view is set to display more than 6 days at
     * once.
     * </p>
     *
     * @param firstDayOfWeek The supported values are {@link java.util.Calendar#SUNDAY},
     *                       {@link java.util.Calendar#MONDAY}, {@link java.util.Calendar#TUESDAY},
     *                       {@link java.util.Calendar#WEDNESDAY}, {@link java.util.Calendar#THURSDAY},
     *                       {@link java.util.Calendar#FRIDAY}.
     */
    public void setFirstDayOfWeek(int firstDayOfWeek) {
        mFirstDayOfWeek = firstDayOfWeek;
        invalidate();
    }

    public boolean isShowFirstDayOfWeekFirst() {
        return mShowFirstDayOfWeekFirst;
    }

    public void setShowFirstDayOfWeekFirst(boolean show) {
        mShowFirstDayOfWeekFirst = show;
    }

    public int getTextSize() {
        return mTextSize;
    }

    public void setTextSize(int textSize) {
        mTextSize = textSize;
        mTodayHeaderTextPaint.setTextSize(mTextSize);
        mHeaderTextPaint.setTextSize(mTextSize);
        mTimeTextPaint.setTextSize(mTextSize);
        invalidate();
    }

    public int getHeaderColumnPadding() {
        return mHeaderColumnPadding;
    }

    public void setHeaderColumnPadding(int headerColumnPadding) {
        mHeaderColumnPadding = headerColumnPadding;
        invalidate();
    }

    public int getHeaderColumnTextColor() {
        return mHeaderColumnTextColor;
    }

    public void setHeaderColumnTextColor(int headerColumnTextColor) {
        mHeaderColumnTextColor = headerColumnTextColor;
        mHeaderTextPaint.setColor(mHeaderColumnTextColor);
        mTimeTextPaint.setColor(mHeaderColumnTextColor);
        invalidate();
    }

    public void setTypeface(Typeface typeface) {
        if (typeface != null) {
            mEventTextPaint.setTypeface(typeface);
            mTodayHeaderTextPaint.setTypeface(typeface);
            mTimeTextPaint.setTypeface(typeface);
            mTypeface = typeface;
            init();
        }
    }

    public int getHeaderRowPadding() {
        return mHeaderRowPadding;
    }

    public void setHeaderRowPadding(int headerRowPadding) {
        mHeaderRowPadding = headerRowPadding;
        invalidate();
    }

    public int getHeaderRowBackgroundColor() {
        return mHeaderRowBackgroundColor;
    }

    public void setHeaderRowBackgroundColor(int headerRowBackgroundColor) {
        mHeaderRowBackgroundColor = headerRowBackgroundColor;
        mHeaderBackgroundPaint.setColor(mHeaderRowBackgroundColor);
        invalidate();
    }

    public int getDayBackgroundColor() {
        return mDayBackgroundColor;
    }

    public void setDayBackgroundColor(int dayBackgroundColor) {
        mDayBackgroundColor = dayBackgroundColor;
        mDayBackgroundPaint.setColor(mDayBackgroundColor);
        invalidate();
    }

    public int getHourSeparatorColor() {
        return mHourSeparatorColor;
    }

    public void setHourSeparatorColor(int hourSeparatorColor) {
        mHourSeparatorColor = hourSeparatorColor;
        mHourSeparatorPaint.setColor(mHourSeparatorColor);
        invalidate();
    }

    public int getTodayBackgroundColor() {
        return mTodayBackgroundColor;
    }

    public void setTodayBackgroundColor(int todayBackgroundColor) {
        mTodayBackgroundColor = todayBackgroundColor;
        mTodayBackgroundPaint.setColor(mTodayBackgroundColor);
        invalidate();
    }

    public int getHourSeparatorHeight() {
        return mHourSeparatorHeight;
    }

    public void setHourSeparatorHeight(int hourSeparatorHeight) {
        mHourSeparatorHeight = hourSeparatorHeight;
        mHourSeparatorPaint.setStrokeWidth(mHourSeparatorHeight);
        invalidate();
    }

    public int getTodayHeaderTextColor() {
        return mTodayHeaderTextColor;
    }

    public void setTodayHeaderTextColor(int todayHeaderTextColor) {
        mTodayHeaderTextColor = todayHeaderTextColor;
        mTodayHeaderTextPaint.setColor(mTodayHeaderTextColor);
        invalidate();
    }

    public int getEventTextSize() {
        return mEventTextSize;
    }

    public void setEventTextSize(int eventTextSize) {
        mEventTextSize = eventTextSize;
        mEventTextPaint.setTextSize(mEventTextSize);
        invalidate();
    }

    public int getEventTextColor() {
        return mEventTextColor;
    }

    public void setEventTextColor(int eventTextColor) {
        mEventTextColor = eventTextColor;
        mEventTextPaint.setColor(mEventTextColor);
        invalidate();
    }

    public void setTextColorPicker(TextColorPicker textColorPicker) {
        this.textColorPicker = textColorPicker;
    }

    public TextColorPicker getTextColorPicker() {
        return textColorPicker;
    }

    public int getEventPadding() {
        return mEventPadding;
    }

    public void setEventPadding(int eventPadding) {
        mEventPadding = eventPadding;
        invalidate();
    }

    public int getHeaderColumnBackgroundColor() {
        return mHeaderColumnBackgroundColor;
    }

    public void setHeaderColumnBackgroundColor(int headerColumnBackgroundColor) {
        mHeaderColumnBackgroundColor = headerColumnBackgroundColor;
        mHeaderColumnBackgroundPaint.setColor(mHeaderColumnBackgroundColor);
        invalidate();
    }

    public int getDefaultEventColor() {
        return mDefaultEventColor;
    }

    public void setDefaultEventColor(int defaultEventColor) {
        mDefaultEventColor = defaultEventColor;
        invalidate();
    }

    public int getNewEventColor() {
        return mNewEventColor;
    }

    public void setNewEventColor(int defaultNewEventColor) {
        mNewEventColor = defaultNewEventColor;
        invalidate();
    }

    public String getNewEventIdentifier() {
        return mNewEventIdentifier;
    }

    @Deprecated
    public int getNewEventId() {
        return Integer.parseInt(mNewEventIdentifier);
    }

    public void setNewEventIdentifier(String newEventId) {
        this.mNewEventIdentifier = newEventId;
    }

    @Deprecated
    public void setNewEventId(int newEventId) {
        this.mNewEventIdentifier = String.valueOf(newEventId);
    }

    public int getNewEventLengthInMinutes() {
        return mNewEventLengthInMinutes;
    }

    public void setNewEventLengthInMinutes(int newEventLengthInMinutes) {
        this.mNewEventLengthInMinutes = newEventLengthInMinutes;
    }

    public int getNewEventTimeResolutionInMinutes() {
        return mNewEventTimeResolutionInMinutes;
    }

    public void setNewEventTimeResolutionInMinutes(int newEventTimeResolutionInMinutes) {
        this.mNewEventTimeResolutionInMinutes = newEventTimeResolutionInMinutes;
    }

    /**
     * <b>Note:</b> Use {@link #setDateTimeInterpreter(DateTimeInterpreter)} and
     * {@link #getDateTimeInterpreter()} instead.
     *
     * @return Either long or short day name is being used.
     */
    @Deprecated
    public int getDayNameLength() {
        return mDayNameLength;
    }

    /**
     * Set the length of the day name displayed in the header row. Example of short day names is
     * 'M' for 'Monday' and example of long day names is 'Mon' for 'Monday'.
     * <p>
     * <b>Note:</b> Use {@link #setDateTimeInterpreter(DateTimeInterpreter)} instead.
     * </p>
     *
     * @param length Supported values are {@link com.my_widgets.weekview.WeekView#LENGTH_SHORT} and
     *               {@link com.my_widgets.weekview.WeekView#LENGTH_LONG}.
     */
    @Deprecated
    public void setDayNameLength(int length) {
        if (length != LENGTH_LONG && length != LENGTH_SHORT) {
            throw new IllegalArgumentException("length parameter must be either LENGTH_LONG or LENGTH_SHORT");
        }
        this.mDayNameLength = length;
    }

    public int getOverlappingEventGap() {
        return mOverlappingEventGap;
    }

    /**
     * Set the gap between overlapping events.
     *
     * @param overlappingEventGap The gap between overlapping events.
     */
    public void setOverlappingEventGap(int overlappingEventGap) {
        this.mOverlappingEventGap = overlappingEventGap;
        invalidate();
    }

    public int getEventCornerRadius() {
        return mEventCornerRadius;
    }

    /**
     * Set corner radius for event rect.
     *
     * @param eventCornerRadius the radius in px.
     */
    public void setEventCornerRadius(int eventCornerRadius) {
        mEventCornerRadius = eventCornerRadius;
    }

    public int getEventMarginVertical() {
        return mEventMarginVertical;
    }

    /**
     * Set the top and bottom margin of the event. The event will release this margin from the top
     * and bottom edge. This margin is useful for differentiation consecutive events.
     *
     * @param eventMarginVertical The top and bottom margin.
     */
    public void setEventMarginVertical(int eventMarginVertical) {
        this.mEventMarginVertical = eventMarginVertical;
        invalidate();
    }

    /**
     * Returns the first visible day in the week view.
     *
     * @return The first visible day in the week view.
     */
    public Calendar getFirstVisibleDay() {
        return mFirstVisibleDay;
    }

    /**
     * Returns the last visible day in the week view.
     *
     * @return The last visible day in the week view.
     */
    public Calendar getLastVisibleDay() {
        return mLastVisibleDay;
    }

    /**
     * Get the scrolling speed factor in horizontal direction.
     *
     * @return The speed factor in horizontal direction.
     */
    public float getXScrollingSpeed() {
        return mXScrollingSpeed;
    }

    /**
     * Sets the speed for horizontal scrolling.
     *
     * @param xScrollingSpeed The new horizontal scrolling speed.
     */
    public void setXScrollingSpeed(float xScrollingSpeed) {
        this.mXScrollingSpeed = xScrollingSpeed;
    }

    /**
     * Get the earliest day that can be displayed. Will return null if no minimum date is set.
     *
     * @return the earliest day that can be displayed, null if no minimum date set
     */
    public Calendar getMinDate() {
        return mMinDate;
    }

    /**
     * Set the earliest day that can be displayed. This will determine the left horizontal scroll
     * limit. The default value is null (allow unlimited scrolling into the past).
     *
     * @param minDate The new minimum date (pass null for no minimum)
     */
    public void setMinDate(Calendar minDate) {
        if (minDate != null) {
            minDate.set(Calendar.HOUR_OF_DAY, 0);
            minDate.set(Calendar.MINUTE, 0);
            minDate.set(Calendar.SECOND, 0);
            minDate.set(Calendar.MILLISECOND, 0);
            if (mMaxDate != null && minDate.after(mMaxDate)) {
                throw new IllegalArgumentException("minDate cannot be later than maxDate");
            }
        }

        mMinDate = minDate;
        resetHomeDate();
        mCurrentOrigin.x = 0;
        invalidate();
    }

    /**
     * Get the latest day that can be displayed. Will return null if no maximum date is set.
     *
     * @return the latest day the can be displayed, null if no max date set
     */
    public Calendar getMaxDate() {
        return mMaxDate;
    }

    /**
     * Set the latest day that can be displayed. This will determine the right horizontal scroll
     * limit. The default value is null (allow unlimited scrolling in to the future).
     *
     * @param maxDate The new maximum date (pass null for no maximum)
     */
    public void setMaxDate(Calendar maxDate) {
        if (maxDate != null) {
            maxDate.set(Calendar.HOUR_OF_DAY, 0);
            maxDate.set(Calendar.MINUTE, 0);
            maxDate.set(Calendar.SECOND, 0);
            maxDate.set(Calendar.MILLISECOND, 0);
            if (mMinDate != null && maxDate.before(mMinDate)) {
                throw new IllegalArgumentException("maxDate has to be after minDate");
            }
        }

        mMaxDate = maxDate;
        resetHomeDate();
        mCurrentOrigin.x = 0;
        invalidate();
    }

    /**
     * Whether weekends should have a background color different from the normal day background
     * color. The weekend background colors are defined by the attributes
     * `futureWeekendBackgroundColor` and `pastWeekendBackgroundColor`.
     *
     * @return True if weekends should have different background colors.
     */
    public boolean isShowDistinctWeekendColor() {
        return mShowDistinctWeekendColor;
    }

    /**
     * Set whether weekends should have a background color different from the normal day background
     * color. The weekend background colors are defined by the attributes
     * `futureWeekendBackgroundColor` and `pastWeekendBackgroundColor`.
     *
     * @param showDistinctWeekendColor True if weekends should have different background colors.
     */
    public void setShowDistinctWeekendColor(boolean showDistinctWeekendColor) {
        this.mShowDistinctWeekendColor = showDistinctWeekendColor;
        invalidate();
    }

    /**
     * auto calculate limit time on events in visible days.
     */
    public void setAutoLimitTime(boolean isAuto) {
        this.mAutoLimitTime = isAuto;
        invalidate();
    }

    private void recalculateHourHeight() {
        int height = (int) ((getHeight() - (mHeaderHeight + mHeaderRowPadding * 2 + mTimeTextHeight / 2 + mHeaderMarginBottom)) / (this.mMaxTime - this.mMinTime));
        if (height > mHourHeight) {
            if (height > mMaxHourHeight) mMaxHourHeight = height;
            mNewHourHeight = height;
        }
    }

    /**
     * Set visible time span.
     *
     * @param startHour limit time display on top (between 0~24)
     * @param endHour   limit time display at bottom (between 0~24 and larger than startHour)
     */
    public void setLimitTime(int startHour, int endHour) {
        if (endHour <= startHour) {
            throw new IllegalArgumentException("endHour must larger startHour.");
        } else if (startHour < 0) {
            throw new IllegalArgumentException("startHour must be at least 0.");
        } else if (endHour > 24) {
            throw new IllegalArgumentException("endHour can't be higher than 24.");
        }
        this.mMinTime = startHour;
        this.mMaxTime = endHour;
        recalculateHourHeight();
        invalidate();
    }

    /**
     * Set minimal shown time
     *
     * @param startHour limit time display on top (between 0~24) and smaller than endHour
     */
    public void setMinTime(int startHour) {
        if (mMaxTime <= startHour) {
            throw new IllegalArgumentException("startHour must smaller than endHour");
        } else if (startHour < 0) {
            throw new IllegalArgumentException("startHour must be at least 0.");
        }
        this.mMinTime = startHour;
        recalculateHourHeight();
    }

    /**
     * Set highest shown time
     *
     * @param endHour limit time display at bottom (between 0~24 and larger than startHour)
     */
    public void setMaxTime(int endHour) {
        if (endHour <= mMinTime) {
            throw new IllegalArgumentException("endHour must larger startHour.");
        } else if (endHour > 24) {
            throw new IllegalArgumentException("endHour can't be higher than 24.");
        }
        this.mMaxTime = endHour;
        recalculateHourHeight();
        invalidate();
    }

    /**
     * Whether past and future days should have two different background colors. The past and
     * future day colors are defined by the attributes `futureBackgroundColor` and
     * `pastBackgroundColor`.
     *
     * @return True if past and future days should have two different background colors.
     */
    public boolean isShowDistinctPastFutureColor() {
        return mShowDistinctPastFutureColor;
    }

    /**
     * Set whether weekends should have a background color different from the normal day background
     * color. The past and future day colors are defined by the attributes `futureBackgroundColor`
     * and `pastBackgroundColor`.
     *
     * @param showDistinctPastFutureColor True if past and future should have two different
     *                                    background colors.
     */
    public void setShowDistinctPastFutureColor(boolean showDistinctPastFutureColor) {
        this.mShowDistinctPastFutureColor = showDistinctPastFutureColor;
        invalidate();
    }

    /**
     * Get whether "now" line should be displayed. "Now" line is defined by the attributes
     * `nowLineColor` and `nowLineThickness`.
     *
     * @return True if "now" line should be displayed.
     */
    public boolean isShowNowLine() {
        return mShowNowLine;
    }

    /**
     * Set whether "now" line should be displayed. "Now" line is defined by the attributes
     * `nowLineColor` and `nowLineThickness`.
     *
     * @param showNowLine True if "now" line should be displayed.
     */
    public void setShowNowLine(boolean showNowLine) {
        this.mShowNowLine = showNowLine;
        invalidate();
    }

    /**
     * Get the "now" line color.
     *
     * @return The color of the "now" line.
     */
    public int getNowLineColor() {
        return mNowLineColor;
    }

    /**
     * Set the "now" line color.
     *
     * @param nowLineColor The color of the "now" line.
     */
    public void setNowLineColor(int nowLineColor) {
        this.mNowLineColor = nowLineColor;
        invalidate();
    }

    /**
     * Get the "now" line thickness.
     *
     * @return The thickness of the "now" line.
     */
    public int getNowLineThickness() {
        return mNowLineThickness;
    }

    /**
     * Set the "now" line thickness.
     *
     * @param nowLineThickness The thickness of the "now" line.
     */
    public void setNowLineThickness(int nowLineThickness) {
        this.mNowLineThickness = nowLineThickness;
        invalidate();
    }

    /**
     * Get whether the week view should fling horizontally.
     *
     * @return True if the week view has horizontal fling enabled.
     */
    public boolean isHorizontalFlingEnabled() {
        return mHorizontalFlingEnabled;
    }

    /**
     * Set whether the week view should fling horizontally.
     *
     * @param enabled whether the week view should fling horizontally
     */
    public void setHorizontalFlingEnabled(boolean enabled) {
        mHorizontalFlingEnabled = enabled;
    }

    /**
     * Get whether the week view should fling vertically.
     *
     * @return True if the week view has vertical fling enabled.
     */
    public boolean isVerticalFlingEnabled() {
        return mVerticalFlingEnabled;
    }

    /**
     * Set whether the week view should fling vertically.
     *
     * @param enabled whether the week view should fling vertically
     */
    public void setVerticalFlingEnabled(boolean enabled) {
        mVerticalFlingEnabled = enabled;
    }

    /**
     * Get the height of AllDay-events.
     *
     * @return Height of AllDay-events.
     */
    public int getAllDayEventHeight() {
        return mAllDayEventHeight;
    }

    /**
     * Set the height of AllDay-events.
     *
     * @param height the new height of AllDay-events
     */
    public void setAllDayEventHeight(int height) {
        mAllDayEventHeight = height;
    }

    /**
     * Enable zoom focus point
     * If you set this to false the `zoomFocusPoint` won't take effect any more while zooming.
     * The zoom will always be focused at the center of your gesture.
     *
     * @param zoomFocusPointEnabled whether the zoomFocusPoint is enabled
     */
    public void setZoomFocusPointEnabled(boolean zoomFocusPointEnabled) {
        mZoomFocusPointEnabled = zoomFocusPointEnabled;
    }

    /*
     * Is focus point enabled
     * @return fixed focus point enabled?
     */
    public boolean isZoomFocusPointEnabled() {
        return mZoomFocusPointEnabled;
    }

    /*
     * Get focus point
     * 0 = top of view, 1 = bottom of view
     * The focused point (multiplier of the view height) where the week view is zoomed around.
     * This point will not move while zooming.
     * @return focus point
     */
    public float getZoomFocusPoint() {
        return mZoomFocusPoint;
    }

    /**
     * Set focus point
     * 0 = top of view, 1 = bottom of view
     * The focused point (multiplier of the view height) where the week view is zoomed around.
     * This point will not move while zooming.
     *
     * @param zoomFocusPoint the new zoomFocusPoint
     */
    public void setZoomFocusPoint(float zoomFocusPoint) {
        if (0 > zoomFocusPoint || zoomFocusPoint > 1)
            throw new IllegalStateException("The zoom focus point percentage has to be between 0 and 1");
        mZoomFocusPoint = zoomFocusPoint;
    }


    /**
     * Get scroll duration
     *
     * @return scroll duration
     */
    public int getScrollDuration() {
        return mScrollDuration;
    }

    /**
     * Set the scroll duration
     *
     * @param scrollDuration the new scrollDuraction
     */
    public void setScrollDuration(int scrollDuration) {
        mScrollDuration = scrollDuration;
    }

    public int getMaxHourHeight() {
        return mMaxHourHeight;
    }

    public void setMaxHourHeight(int maxHourHeight) {
        mMaxHourHeight = maxHourHeight;
    }

    public int getMinHourHeight() {
        return mMinHourHeight;
    }

    public void setMinHourHeight(int minHourHeight) {
        this.mMinHourHeight = minHourHeight;
    }

    public int getPastBackgroundColor() {
        return mPastBackgroundColor;
    }

    public void setPastBackgroundColor(int pastBackgroundColor) {
        this.mPastBackgroundColor = pastBackgroundColor;
        mPastBackgroundPaint.setColor(mPastBackgroundColor);
    }

    public int getFutureBackgroundColor() {
        return mFutureBackgroundColor;
    }

    public void setFutureBackgroundColor(int futureBackgroundColor) {
        this.mFutureBackgroundColor = futureBackgroundColor;
        mFutureBackgroundPaint.setColor(mFutureBackgroundColor);
    }

    public int getPastWeekendBackgroundColor() {
        return mPastWeekendBackgroundColor;
    }

    public void setPastWeekendBackgroundColor(int pastWeekendBackgroundColor) {
        this.mPastWeekendBackgroundColor = pastWeekendBackgroundColor;
        this.mPastWeekendBackgroundPaint.setColor(mPastWeekendBackgroundColor);
    }

    public int getFutureWeekendBackgroundColor() {
        return mFutureWeekendBackgroundColor;
    }

    public void setFutureWeekendBackgroundColor(int futureWeekendBackgroundColor) {
        this.mFutureWeekendBackgroundColor = futureWeekendBackgroundColor;
        this.mFutureWeekendBackgroundPaint.setColor(mFutureWeekendBackgroundColor);
    }

    public Drawable getNewEventIconDrawable() {
        return mNewEventIconDrawable;
    }

    public void setNewEventIconDrawable(Drawable newEventIconDrawable) {
        this.mNewEventIconDrawable = newEventIconDrawable;
    }

    public void enableDropListener() {
        this.mEnableDropListener = true;
        //set drag and drop listener, required Honeycomb+ Api level
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setOnDragListener(new DragListener());
        }
    }

    public void disableDropListener() {
        this.mEnableDropListener = false;
        //set drag and drop listener, required Honeycomb+ Api level
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setOnDragListener(null);
        }
    }

    public boolean isDropListenerEnabled() {
        return this.mEnableDropListener;
    }

    public void setMinOverlappingMinutes(int minutes) {
        this.mMinOverlappingMinutes = minutes;
    }

    public int getMinOverlappingMinutes() {
        return this.mMinOverlappingMinutes;
    }

    /////////////////////////////////////////////////////////////////
    //
    //      Functions related to scrolling.
    //
    /////////////////////////////////////////////////////////////////

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mScaleDetector.onTouchEvent(event);
        boolean val = mGestureDetector.onTouchEvent(event);

        // Check after call of mGestureDetector, so mCurrentFlingDirection and mCurrentScrollDirection are set.
        if (event.getAction() == MotionEvent.ACTION_UP && !mIsZooming && mCurrentFlingDirection == Direction.NONE) {
            if (mCurrentScrollDirection == Direction.RIGHT || mCurrentScrollDirection == Direction.LEFT) {
                goToNearestOrigin();
            }
            mCurrentScrollDirection = Direction.NONE;
        }

        return val;
    }

    /**
     * A lighter function to stop the current scroll animation
     */
    private void stopScrolling() {
        //force scroller animation stop
        mScroller.forceFinished(true);
        // Reset scrolling and fling direction.
        mCurrentScrollDirection = mCurrentFlingDirection = Direction.NONE;
    }

    private void goToNearestOrigin() {
        double leftDays = mCurrentOrigin.x / (mWidthPerDay + mColumnGap);

        if (mCurrentFlingDirection != Direction.NONE) {
            // snap to nearest day
            leftDays = Math.round(leftDays);
        } else if (mCurrentScrollDirection == Direction.LEFT) {
            // snap to last day
            leftDays = Math.floor(leftDays);
        } else if (mCurrentScrollDirection == Direction.RIGHT) {
            // snap to next day
            leftDays = Math.ceil(leftDays);
        } else {
            // snap to nearest day
            leftDays = Math.round(leftDays);
        }

        int nearestOrigin = (int) (mCurrentOrigin.x - leftDays * (mWidthPerDay + mColumnGap));
        boolean mayScrollHorizontal = mCurrentOrigin.x - nearestOrigin < getXMaxLimit() && mCurrentOrigin.x - nearestOrigin > getXMinLimit();

        if (mayScrollHorizontal) {
            mScroller.startScroll((int) mCurrentOrigin.x, (int) mCurrentOrigin.y, -nearestOrigin, 0);
            ViewCompat.postInvalidateOnAnimation(WeekView.this);
        }

        if (nearestOrigin != 0 && mayScrollHorizontal) {
            // Stop current animation.
            mScroller.forceFinished(true);
            // Snap to date.
            mScroller.startScroll((int) mCurrentOrigin.x, (int) mCurrentOrigin.y, -nearestOrigin, 0, (int) (Math.abs(nearestOrigin) / mWidthPerDay * mScrollDuration));
            ViewCompat.postInvalidateOnAnimation(WeekView.this);
        }
        // Reset scrolling and fling direction.
        mCurrentScrollDirection = mCurrentFlingDirection = Direction.NONE;
    }


    @Override
    public void computeScroll() {
        super.computeScroll();

        if (mScroller.isFinished()) {
            if (mCurrentFlingDirection != Direction.NONE) {
                // Snap to day after fling is finished.
                goToNearestOrigin();
            }
        } else {
            if (mCurrentFlingDirection != Direction.NONE && forceFinishScroll()) {
                goToNearestOrigin();
            } else if (mScroller.computeScrollOffset()) {
                mCurrentOrigin.y = mScroller.getCurrY();
                mCurrentOrigin.x = mScroller.getCurrX();
                ViewCompat.postInvalidateOnAnimation(this);
            }
        }
    }

    /**
     * Check if scrolling should be stopped.
     *
     * @return true if scrolling should be stopped before reaching the end of animation.
     */
    private boolean forceFinishScroll() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            // current velocity only available since api 14
            return mScroller.getCurrVelocity() <= mMinimumFlingVelocity;
        } else {
            return false;
        }
    }


    /////////////////////////////////////////////////////////////////
    //
    //      Public methods.
    //
    /////////////////////////////////////////////////////////////////

    /**
     * Show today on the week view.
     */
    public void goToToday() {
        Calendar today = Calendar.getInstance();
        goToDate(today);
    }

    /**
     * Show a specific day on the week view.
     *
     * @param date The date to show.
     */
    public void goToDate(Calendar date) {
        mScroller.forceFinished(true);
        mCurrentScrollDirection = mCurrentFlingDirection = Direction.NONE;

        date.set(Calendar.HOUR_OF_DAY, 0);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);

        if (mAreDimensionsInvalid) {
            mScrollToDay = date;
            return;
        }

        mRefreshEvents = true;

        mCurrentOrigin.x = -daysBetween(mHomeDate, date) * (mWidthPerDay + mColumnGap);
        invalidate();
    }

    /**
     * Refreshes the view and loads the events again.
     */
    public void notifyDatasetChanged() {
        mRefreshEvents = true;
        invalidate();
    }

    /**
     * Vertically scroll to a specific hour in the week view.
     *
     * @param hour The hour to scroll to in 24-hour format. Supported values are 0-24.
     */
    public void goToHour(double hour) {
        if (mAreDimensionsInvalid) {
            mScrollToHour = hour;
            return;
        }

        int verticalOffset = 0;
        if (hour > mMaxTime) verticalOffset = mHourHeight * (mMaxTime - mMinTime);
        else if (hour > mMinTime) verticalOffset = (int) (mHourHeight * hour);

        if (verticalOffset > mHourHeight * (mMaxTime - mMinTime) - getHeight() + mHeaderHeight + mHeaderRowPadding * 2 + mHeaderMarginBottom)
            verticalOffset = (int) (mHourHeight * (mMaxTime - mMinTime) - getHeight() + mHeaderHeight + mHeaderRowPadding * 2 + mHeaderMarginBottom);

        mCurrentOrigin.y = -verticalOffset;
        invalidate();
    }

    /**
     * Get the first hour that is visible on the screen.
     *
     * @return The first hour that is visible.
     */
    public double getFirstVisibleHour() {
        return -mCurrentOrigin.y / mHourHeight;
    }

    /**
     * Determine whether a given calendar day falls within the scroll limits set for this view.
     *
     * @param day the day to check
     * @return True if there are no limit or the date is within the limits.
     * @see #setMinDate(Calendar)
     * @see #setMaxDate(Calendar)
     */
    public boolean dateIsValid(Calendar day) {
        if (mMinDate != null && day.before(mMinDate)) {
            return false;
        }
        if (mMaxDate != null && day.after(mMaxDate)) {
            return false;
        }
        return true;
    }

    /////////////////////////////////////////////////////////////////
    //
    //      Interfaces.
    //
    /////////////////////////////////////////////////////////////////

    public interface DropListener {
        /**
         * Triggered when view dropped
         *
         * @param view: dropped view.
         * @param date: object set with the date and time of the dropped coordinates on the view.
         */
        void onDrop(View view, Calendar date);
    }

    public interface EventClickListener {
        /**
         * Triggered when clicked on one existing event
         *
         * @param event:     event clicked.
         * @param eventRect: view containing the clicked event.
         */
        void onEventClick(WeekViewEvent event, RectF eventRect);
    }

    public interface EventLongPressListener {
        /**
         * Similar to {@link com.my_widgets.weekview.WeekView.EventClickListener} but with a long press.
         *
         * @param event:     event clicked.
         * @param eventRect: view containing the clicked event.
         */
        void onEventLongPress(WeekViewEvent event, RectF eventRect);
    }

    public interface EmptyViewClickListener {
        /**
         * Triggered when the users clicks on a empty space of the calendar.
         *
         * @param date: {@link Calendar} object set with the date and time of the clicked position on the view.
         */
        void onEmptyViewClicked(Calendar date);

    }

    public interface EmptyViewLongPressListener {
        /**
         * Similar to {@link com.my_widgets.weekview.WeekView.EmptyViewClickListener} but with long press.
         *
         * @param time: {@link Calendar} object set with the date and time of the long pressed position on the view.
         */
        void onEmptyViewLongPress(Calendar time);
    }

    public interface ScrollListener {
        /**
         * Called when the first visible day has changed.
         * <p>
         * (this will also be called during the first draw of the weekview)
         *
         * @param newFirstVisibleDay The new first visible day
         * @param oldFirstVisibleDay The old first visible day (is null on the first call).
         */
        void onFirstVisibleDayChanged(Calendar newFirstVisibleDay, Calendar oldFirstVisibleDay);
    }

    public interface AddEventClickListener {
        /**
         * Triggered when the users clicks to create a new event.
         *
         * @param startTime The startTime of a new event
         * @param endTime   The endTime of a new event
         */
        void onAddEventClicked(Calendar startTime, Calendar endTime);
    }

    public interface ZoomEndListener {
        /**
         * Triggered when the user finishes a zoom action.
         *
         * @param hourHeight The final height of hours when the user finishes zoom.
         */
        void onZoomEnd(int hourHeight);
    }

    /**
     * A simple GestureListener that holds the focused hour while scaling.
     */
    private class WeekViewGestureListener implements ScaleGestureDetector.OnScaleGestureListener {
        float mFocusedPointY;

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            mIsZooming = false;
            if (mZoomEndListener != null) {
                mZoomEndListener.onZoomEnd(mHourHeight);
            }
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            mIsZooming = true;
            goToNearestOrigin();

            // Calculate focused point for scale action
            if (mZoomFocusPointEnabled) {
                // Use fractional focus, percentage of height
                mFocusedPointY = (getHeight() - mHeaderHeight - mHeaderRowPadding * 2 - mHeaderMarginBottom) * mZoomFocusPoint;
            } else {
                // Grab focus
                mFocusedPointY = detector.getFocusY();
            }

            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            final float scale = detector.getScaleFactor();

            mNewHourHeight = Math.round(mHourHeight * scale);

            // Calculating difference
            float diffY = mFocusedPointY - mCurrentOrigin.y;
            // Scaling difference
            diffY = diffY * scale - diffY;
            // Updating week view origin
            mCurrentOrigin.y -= diffY;

            invalidate();
            return true;
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    private class DragListener implements View.OnDragListener {
        @Override
        public boolean onDrag(View v, DragEvent e) {
            switch (e.getAction()) {
                case DragEvent.ACTION_DROP:
                    if (e.getX() > mHeaderColumnWidth && e.getY() > (mHeaderTextHeight + mHeaderRowPadding * 2 + mHeaderMarginBottom)) {
                        Calendar selectedTime = getTimeFromPoint(e.getX(), e.getY());
                        if (selectedTime != null) {
                            mDropListener.onDrop(v, selectedTime);
                        }
                    }
                    break;
            }
            return true;
        }
    }
}
