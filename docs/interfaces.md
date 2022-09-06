Use the following interfaces according to your need.

- `mWeekView.setWeekViewLoader()` to provide events to the calendar
- `mWeekView.setMonthChangeListener()` to provide events to the calendar by months
- `mWeekView.setOnEventClickListener()` to get a callback when an event is clicked
- `mWeekView.setEventLongPressListener()` to get a callback when an event is long pressed
- `mWeekView.setEmptyViewClickListener()` to get a callback when any empty space is clicked
- `mWeekView.setEmptyViewLongPressListener()` to get a callback when any empty space is long pressed
- `mWeekView.setDateTimeInterpreter()` to set your own labels for the calendar header row and header column
- `mWeekView.setScrollListener()` to get an event every time the first visible day has changed
- `mWeekView.setAddEventClickListener()` to get the start and end time of an event to add
- `mWeekView.setTextColorPicker()` to change the textColor of an event, based on the event. You can, for example,
  set a different text color depending on the color of the event.
- `mWeekView.setDropListener()` to get a callback when a view is dropped at a certain date.