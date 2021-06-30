package billboardControlPanel;

import helpers.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CalendarView extends JPanel {
    private String selectedBbID = "-1";
    private JLabel bbNamePre = new JLabel("Billboard:");
    private JLabel BbName = new JLabel();
    private JLabel schedulerPre = new JLabel("Scheduled By:");
    private JLabel schedulerName = new JLabel();
    private JLabel scheduledTimePre = new JLabel("At:");
    private JLabel scheduledTime = new JLabel();
    private JLabel recursPre = new JLabel("Recurs:");
    private JLabel recursnew = new JLabel();
    private final int minutesInDayAndThereforeLengthOfColumn = 1440;
    private final int widthOfColumn = 30;
    private JLabel[] entryLabels;
    private String[][] scheduleEntries;

    private void populateCalendarViewFields(String[] entry) {
        selectedBbID = entry[0];
        BbName.setText(entry[1]);
        schedulerName.setText(entry[2]);
        scheduledTime.setText(entry[3] + " on " + entry [4] + " for " + entry[5] + "minutes");
    }

    private class populateOnClickListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            JLabel selected = (JLabel)e.getComponent();
            populateCalendarViewFields(scheduleEntries[getIndex(selected)]);
        }

    }

    private int getIndex(JLabel selected) {
        int index;
        for(index = 0; index < entryLabels.length; index++){
            if(selected.getText().equals(entryLabels[index].getText())){
                return index;
            }
        }
        return -1;
    }
    /**
     * Class to create a week view style calendar of all the entries in the schedule
     * use code from lines 326 to 342 in GUISchedule table to get scheduleEntries[][] to pass
     * labelled there as billboard_schedule[][] and then call instead of createScheduleTableInScrollPane
     * to populate the JDialog box that comes up to remove the schedule. the function to remove schedule
     * in GUISchedulePanel would need to be called with CalendarView.selectedBbID and the other JLabel's text
     * assigned in populateCalendarViewFields() when "remove schedule" is clicked in the JDialog.
     * same goes for the preview if "preview" is clicked
     */
    public CalendarView(String[][] scheduleEntries){
        this.scheduleEntries = scheduleEntries;
        entryLabels = new JLabel[scheduleEntries.length];
        JLabel[] daysOfWeek = {new JLabel("Sunday"), new JLabel("Monday"), new JLabel("Tuesday"),
                new JLabel("Wednesday"), new JLabel("Thursday"), new JLabel("Friday"),
                new JLabel("Saturday")};
        setLayout(new GridBagLayout());
        GridBagConstraints con = new GridBagConstraints();
        //labels describing selected billboard on y = 0, 1, 2
        //TODO the above
        //calendar columns from y = 3
        Dimension headings = new Dimension(widthOfColumn, 20);
        int xStartColumn = 1;
        for (int index = 1; index < daysOfWeek.length; index ++){
            daysOfWeek[index].setPreferredSize(headings);
            gui.addToPanel(this, daysOfWeek[index], con, index, 2,1,1);
        }
        //add a column denoting the time in hour long intervals down x = 0, y = 3 to x = 0, y = 1440
        //TODO the above and using minutesInDayAndThereforeLengthOfColumn
        //add all the stuff from schedule entries as a label to the calendar and also to entryLabels[]
        int index = 0;
        int lengthOfArrayAtIndex = 7;
        while (index < scheduleEntries.length){
            for (int entryIndex = 0; entryIndex < 7; entryIndex++){
                //Create JLabel from schedule entry
                JLabel entry = new JLabel();
                //set unique text
                entry.setText(index + ". " + scheduleEntries[index][1]);
                //set height based on the amount of minutes it takes up and width based on widthOfColumn
                // Start Time + Duration or ((HH * 60) + MM) + Duration)
                int height = 0; //TODO replace zero with above
                Dimension entryD = new Dimension(widthOfColumn, height);
                //assign var column based on day of the week entry is scheduled on
                // (eg Sunday would be column 1, Saturday Column 7)
                int column = 0; //TODO replace 0 with above
                //assign var row based on the start time of entry ((HH*60)+MM)
                int row = 0; //TODO replace 0 with above
                //assign a colour to the entry that alternates from the last colour added
                Color color = Color.blue; //TODO replace with something that does the above
                entry.setBackground(color);
                //place in this JPanel and constraints based on these vars
                gui.addToPanel(this, entry, con, column, row, 1,1);
                //add the populate on click listener
                entry.addMouseListener(new populateOnClickListener());
                //add the entry to the list of entry labels
                entryLabels[index] = entry;
        }
            index++;
        }
    }
}
