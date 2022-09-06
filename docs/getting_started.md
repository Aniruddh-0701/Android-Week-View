# Getting Started

1. Import the library into your project.

- Grab via maven

  ```xml
  <dependency>
    <groupId>io.github.aniruddh-0701</groupId>
    <artifactId>Android-Week-View</artifactId>
    <version>2.4.1.1</version>
    <type>aar</type>
  </dependency>
  ```
- Grab via gradle

  ```groovy
  implementation 'io.github.aniruddh-0701:Android-Week-View:2.4.1.1'
  ```

- The latest version can be found by checking [Maven Search](https://search.maven.org/artifact/io.github.
  aniruddh-0701/Android-Week-View)

2. Add WeekView in your xml layout. (see [Customization](./customization.md)
   for all possible attributes.)

    ```xml
        <com.my_widgets.weekview.WeekView
            android:id="@+id/week_view"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            app:eventTextColor="@android:color/white"
            app:textSize="12sp"
            app:hourHeight="60dp"
            app:headerColumnPadding="8dp"
            app:headerColumnTextColor="#8f000000"
            app:headerRowPadding="12dp"
            app:columnGap="8dp"
            app:noOfVisibleDays="3"
            app:headerRowBackgroundColor="#ffefefef"
            app:dayBackgroundColor="#05000000"
            app:todayBackgroundColor="#1848adff"
            app:headerColumnBackground="#ffffffff"/>
    ```

3. Write the following code in your java file.

    ```java
    // Get a reference for the week view in the layout.
    mWeekView = (WeekView) findViewById(R.id.week_view);

    // Set an action when any event is clicked.
    mWeekView.setOnEventClickListener(mEventClickListener);

    // The week view has infinite scrolling horizontally. We have to provide the events of a
    // month every time the month changes on the week view.
    mWeekView.setMonthChangeListener(mMonthChangeListener);

    // Set long press listener for events.
    mWeekView.setEventLongPressListener(mEventLongPressListener);
    ```
4. Implement the interfaces according to your need.
   See [Interfaces](./interfaces.md) for all possible interfaces.)

5. Provide the events for the `WeekView` in `WeekView.MonthChangeListener.onMonthChange()` callback.

    ```java
    MonthLoader.MonthChangeListener mMonthChangeListener = new MonthLoader.MonthChangeListener() {
        @Override
        public List<WeekViewEvent> onMonthChange(int newYear, int newMonth) {
            // Populate the week view with some events.
            List<WeekViewEvent> events = getEvents(newYear, newMonth);
            return events;
        }
    };
    ```