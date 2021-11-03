/**
 * Copyright 2021 - 2021 CMPUT301F21T03 (Alpha-Apps). All rights reserved. This document nor any
 * part of it may be reproduced, stored in a retrieval system or transmitted in any for or by any
 * means without prior permission of the members of CMPUT301F21T03 or by the professor and any
 * authorized TAs of the CMPUT301 class at the University of Alberta, fall term 2021.
 *
 * Class: HabitDetails
 *
 * Description: Handles the user interactions of the habit details page
 *
 * Changelog:
 * =|Version|=|User(s)|==|Date|========|Description|================================================
 *   1.0       Mathew    Oct-21-2021   Created
 *   1.1       Moe       Oct-29-2021   Added popup menu when more button is pressed
 *   1.2       Jesse     Oct-31-2021   Set up array adapter and on click listener for event list
 *   1.3       Mathew    Oct-31-2021   Fix imports, add dummy test data
 *   1.4       Eric      Oct-31-2021   Linked EditTexts with data from Habit object passed in Intent
 *   1.5       Eric      Oct-31-2021   Added edit functionality
 *   1.6       Mathew    Oct-31-2021   Added a more descriptive tag by which to get the intent info
 *   1.7       Moe       Nov-01-2021   Added passing event object when log habit is selected in the
 *                                         popup menu
 *   1.8       Moe       Nov-01-2021   Removed log habit in the popup menu
 * =|=======|=|======|===|====|========|===========|================================================
 */

package com.example.prototypehabitapp.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.method.KeyListener;
import android.os.Build;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import androidx.fragment.app.Fragment;

import com.example.prototypehabitapp.DataClasses.DaysOfWeek;
import com.example.prototypehabitapp.DataClasses.Habit;
import com.example.prototypehabitapp.DataClasses.Event;
import com.example.prototypehabitapp.Fragments.AllHabits;
import com.example.prototypehabitapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;

public class HabitDetails extends AppCompatActivity{

    // attributes
    private Habit habit;
    private boolean editing;

    private EditText title;
    private EditText reason;
    private TextView date_started;
    private TextView habit_events_title;
    private HorizontalScrollView habit_events_scoller;
    private Button done_editing;

    private ListView eventsListview;
    private ArrayList<Event> events;
    private ArrayAdapter<Event> eventsAdapter;
    PopupMenu popupMenu;

    ArrayList<CheckBox> weekButtons;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // set the display to be the main page
        setContentView(R.layout.habit_details);

        editing = false;

        // connect to view elements
        title = findViewById(R.id.habitdetails_title);
        reason = findViewById(R.id.habitdetails_reason_text);
        date_started = findViewById(R.id.habitdetails_date_started);
        habit_events_title = findViewById(R.id.habitdetails_habit_events_text);
        habit_events_scoller = findViewById(R.id.habitdetails_habit_event_list_scroll_view);
        done_editing = findViewById(R.id.habitdetails_button_done_editing);

        // disable title and reason editablity onCreate
        title.setEnabled(false);
        title.setTextColor(Color.BLACK);
        reason.setEnabled(false);
        reason.setTextColor(Color.BLACK);

        weekButtons = prepareDayOfWeekButtons();

        // if a selected habit was sent over in the intent
        Intent intent = getIntent();
        if (intent.getSerializableExtra(AllHabits.getTAG()) != null)  {
            // then we can work with it...
            // set the data to the proper fields in the activity
            habit = (Habit) intent.getSerializableExtra(AllHabits.getTAG());
            title.setText(habit.getTitle());
            reason.setText(habit.getReason());
            date_started.setText(habit.getDateStarted().toString());
            ArrayList<Boolean> weekOccurenceList = habit.getWeekOccurence().getAll();
            for (int i = 0; i < 7; i++) {
                setBoxesChecked(weekButtons.get(i), weekOccurenceList.get(i));
            }

        }

        for (int i = 0; i < 7; i++) {
            weekButtons.get(i).setClickable(false);
        }

        eventsListview = findViewById(R.id.habitdetails_habit_event_list);

        //add test habit data (remove later)
        habit = new Habit("title", "reason", LocalDateTime.now(), new DaysOfWeek());
        events = habit.getEventList();

        Event newEvent = new Event("title", LocalDateTime.now(), "comment", false, false);
        Event newEvent2 = new Event("title", LocalDateTime.now(), "comment", false, false);

        events.add(newEvent);
        events.add(newEvent2);
        habit.setEventList(events);

        eventsAdapter = new EventList(this, events);
        eventsListview.setAdapter(eventsAdapter);


        //set a listener for if the editHabit layout is pressed by the user
        eventsListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Event event = (Event) eventsListview.getItemAtPosition(i);
                habitDetailsHabitEventLayoutPressed(event);
            }
        });

        // most likely out of date code
        // set a listener for if the more button is pressed by the user
        Button moreButton = findViewById(R.id.habitdetails_more);
        moreButton.setOnClickListener(this::habitDetailsMoreButtonPressed);
    }

    private ArrayList<CheckBox> prepareDayOfWeekButtons(){
        ArrayList<CheckBox> weekButtons = new ArrayList<>();
        CheckBox sunday_button = findViewById(R.id.sunday_checkbox);
        weekButtons.add(sunday_button);
        CheckBox monday_button = findViewById(R.id.monday_checkbox);
        weekButtons.add(monday_button);
        CheckBox tuesday_button = findViewById(R.id.tuesday_checkbox);
        weekButtons.add(tuesday_button);
        CheckBox wednesday_button = findViewById(R.id.wednesday_checkbox);
        weekButtons.add(wednesday_button);
        CheckBox thursday_button = findViewById(R.id.thursday_checkbox);
        weekButtons.add(thursday_button);
        CheckBox friday_button = findViewById(R.id.friday_checkbox);
        weekButtons.add(friday_button);
        CheckBox saturday_button = findViewById(R.id.saturday_checkbox);
        weekButtons.add(saturday_button);
        return weekButtons;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void habitDetailsMoreButtonPressed(View view) {
        //TODO open a dialog as defined in the figma storyboard
        popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.habit_more_menu, popupMenu.getMenu());
        if (editing) {
            popupMenu.getMenu().removeItem(R.id.edit_habit);
        }
        popupMenu.show();
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {

                if (menuItem.getItemId() == R.id.mark_done) {
                    // TODO mark as done
                    Event event = new Event(habit.getTitle(), LocalDateTime.now(), "", false, false);
                    events.add(event);
                    habit.setEventList(events);

                    HabitEventDialog dialog = new HabitEventDialog(HabitDetails.this, event, habit);
                    dialog.show();

                } else if (menuItem.getItemId() == R.id.edit_habit) {
                    prepareForEdit();

                } else if (menuItem.getItemId() == R.id.delete_habit) {
                    // TODO delete habit in Firestore here
                }
                return true;
            }
        });
    }

    private void habitDetailsHabitEventLayoutPressed(Event event){
        Intent intent = new Intent(this, HabitEventDetails.class);
        intent.putExtra("EVENT", event);
        intent.putExtra("HABIT", habit);
        startActivity(intent);
    }

    public void habitDetailsDoneEditingPressed(View view) {
        prepareForFinishEditing();
        // TODO update information in Firestore here
    }

    private void setBoxesChecked(CheckBox button, boolean val) {
        if (val) {
            button.setChecked(true);
        } else {
            button.setChecked(false);
        }
    }

    private void prepareForEdit() {
        editing = true; // set editing flag to true (for popup menu)
        habit_events_title.setVisibility(View.GONE); // hide Habit Events
        habit_events_scoller.setVisibility(View.GONE);
        done_editing.setVisibility(View.VISIBLE); // show done editing button
        title.setEnabled(true); // enable title and reason EditTexts
        reason.setEnabled(true);
        for (int i = 0; i < 7; i++) { // enable frequency click boxes
            weekButtons.get(i).setClickable(true);
        }
    }

    private void prepareForFinishEditing() {
        editing = false; // set editing flag to false (for popup menu)
        habit_events_title.setVisibility(View.VISIBLE); // show Habit Events
        habit_events_scoller.setVisibility(View.VISIBLE);
        done_editing.setVisibility(View.GONE); // hide done editing button
        title.setEnabled(false); // disable title and reason EditTexts
        title.setTextColor(Color.BLACK);
        reason.setEnabled(false);
        reason.setTextColor(Color.BLACK);

        for (int i = 0; i < 7; i++) { // disable frequency click boxes
            weekButtons.get(i).setClickable(false);
        }
    }
}
