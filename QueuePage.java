// QueuePage.java
// The waiting line screen. The user comes here after pressing "Reserve Ticket".
// Your position in line is based on how many OTHER copies of the app are also waiting
// at the same time. If you are the only one, you are first and only wait a few seconds.
// When it is your turn, this window closes and the SeatPage opens.
//
// How the shared line works (so different copies of the app can "see" each other):
//   - every copy that is waiting makes one small empty file in a folder (the queue folder)
//   - every second it "touches" its file (updates the time of the file) to say "I am still here"
//   - your position = how many waiting files were made before yours
//   - if an app is closed or crashes, its file stops being updated, so after 6 seconds
//     the others delete it and everyone behind moves up

import javax.swing.*;
import javax.swing.border.LineBorder;   // for the thick blue border
import java.awt.*;
import java.io.File;                    // to work with the queue files
import java.util.ArrayList;
import java.util.Collections;           // for sorting a list

public class QueuePage extends JFrame {

    Color blue = new Color(47, 91, 207);

    int eventIndex;     // which event was chosen (sent from EventPage)
    int qty;            // how many tickets (sent from EventPage)
    int secondsLeft = 8;   // total waiting time shown on the screen
    int serveLeft = 8;     // seconds left once you are at the front
    int position = 1;      // your place in line (1 = first)

    // every copy of the app that is in the queue makes one small file in this folder
    File queueFolder;
    File myFile;   // my own file in that folder

    JLabel positionLabel = new JLabel("", SwingConstants.CENTER);   // shows [1], [2]...
    JLabel timeLabel = new JLabel("", SwingConstants.CENTER);       // shows the wait time 00:00:08
    JLabel aheadLabel = new JLabel();                               // shows "People ahead of you"
    javax.swing.Timer timer;   // runs some code every second (full name used so it is not mixed up with java.util.Timer)

    // eventIndex and qty are given by the page that opened this one
    public QueuePage(int eventIndex, int qty) {
        this.eventIndex = eventIndex;   // "this." means the variable of the class, not the one in the ()
        this.qty = qty;

        // ----- basic window settings -----
        setTitle("Queue");
        setSize(700, 610);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);
        setLayout(null);
        getContentPane().setBackground(Color.WHITE);

        // ----- left side: position and wait time -----
        JLabel title = new JLabel("Current Position", SwingConstants.CENTER);
        title.setFont(new Font("Georgia", Font.BOLD, 19));
        title.setBounds(74, 40, 300, 32);
        add(title);

        positionLabel.setFont(new Font("Georgia", Font.BOLD, 26));
        positionLabel.setBounds(74, 70, 300, 36);
        add(positionLabel);

        JLabel wait = new JLabel("Wait Time", SwingConstants.CENTER);
        wait.setFont(new Font("Georgia", Font.BOLD, 15));
        wait.setBounds(90, 200, 268, 24);
        add(wait);

        // plain box with a thick blue border (this takes the place of the round ring)
        timeLabel.setFont(new Font("Georgia", Font.BOLD, 30));
        timeLabel.setBorder(new LineBorder(blue, 14));
        timeLabel.setBounds(90, 124, 268, 268);
        add(timeLabel);

        // ----- right side: message, warning, event info -----
        JLabel line1 = new JLabel("You are currently");
        line1.setFont(new Font("Georgia", Font.PLAIN, 27));
        line1.setBounds(395, 78, 280, 36);
        add(line1);

        JLabel line2 = new JLabel(" in a Queue");
        line2.setFont(new Font("Georgia", Font.PLAIN, 27));
        line2.setBounds(397, 112, 280, 36);
        add(line2);

        // pink warning box
        JLabel warning = new JLabel("DO NOT REFRESH THE PAGE !!", SwingConstants.CENTER);
        warning.setFont(new Font("Georgia", Font.PLAIN, 12));
        warning.setOpaque(true);
        warning.setBackground(new Color(255, 133, 133));
        warning.setForeground(Color.WHITE);
        warning.setBounds(395, 166, 235, 38);
        add(warning);

        // show which event and how many tickets were chosen
        JLabel ev = new JLabel("Event : " + TicketData.names[eventIndex]);
        ev.setFont(new Font("Georgia", Font.PLAIN, 15));
        ev.setBounds(395, 226, 280, 24);
        add(ev);

        JLabel tk = new JLabel("Tickets : " + qty);
        tk.setFont(new Font("Georgia", Font.PLAIN, 15));
        tk.setBounds(395, 250, 280, 24);
        add(tk);

        aheadLabel.setFont(new Font("Georgia", Font.PLAIN, 15));
        aheadLabel.setBounds(395, 274, 280, 24);
        add(aheadLabel);

        // Leave Queue button: ask first, then stop the timer, remove my file and go back to the events page
        JButton leave = new JButton("Leave Queue");
        leave.setBounds(395, 318, 140, 34);
        leave.setBackground(new Color(217, 217, 217));
        leave.setOpaque(true);
        leave.setBorderPainted(false);
        leave.setFocusPainted(false);
        leave.addActionListener(e -> {
            int r = JOptionPane.showConfirmDialog(this,
                    "Leave the queue? You will lose your place in line.",
                    "Leave Queue", JOptionPane.YES_NO_OPTION);
            if (r == JOptionPane.YES_OPTION) {
                timer.stop();
                leaveQueue();
                new EventPage().setVisible(true);
                dispose();
            }
        });
        add(leave);

        // ----- join the queue: make my own file in the queue folder -----
        queueFolder = new File(System.getProperty("user.home"), ".ticketapp/queue");   // folder inside the user's home folder
        queueFolder.mkdirs();   // create the folder if it does not exist yet
        // the file name starts with the current time (padded with zeros), so sorting the names
        // puts the people who came first at the top. The random number makes each name different.
        myFile = new File(queueFolder, String.format("%015d", System.currentTimeMillis())
                + "-" + (int) (Math.random() * 100000) + ".q");

        // if the window is closed with the X button, remove my file so I leave the queue
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                leaveQueue();
            }
        });

        // find my starting position and show it
        position = getPosition();
        secondsLeft = (position - 1) * 8 + serveLeft;   // 8 seconds for each person ahead + my own 8 seconds
        updateLabels();

        // this timer runs the code below once every second (1000 milliseconds)
        timer = new javax.swing.Timer(1000, e -> {
            position = getPosition();   // check the line again, people may have joined or left

            if (position > 1) {
                serveLeft = 8;          // still people ahead, so my own countdown has not started
            } else if (serveLeft > 0) {
                serveLeft--;            // I am first, count down my 8 seconds
            }
            secondsLeft = (position - 1) * 8 + serveLeft;
            updateLabels();

            // it is my turn: leave the line and open the seat page
            if (position <= 1 && serveLeft == 0) {
                timer.stop();
                leaveQueue();
                new SeatPage(eventIndex, qty).setVisible(true);
                dispose();
            }
        });
        timer.start();
    }

    // updates my file every second so the others know I am still here,
    // then counts how many people joined before me.
    // returns my place in line (1 = first)
    int getPosition() {
        // 1) make my file if it is not there, and set its "last modified" time to now (this is the "I am still here" signal)
        try {
            if (!myFile.exists()) {
                myFile.createNewFile();
            }
            myFile.setLastModified(System.currentTimeMillis());
        } catch (Exception ex) {
            // if it does not work we just act like the only one in line
        }

        // 2) look at every file in the queue folder
        long now = System.currentTimeMillis();
        ArrayList<String> names = new ArrayList<String>();
        File[] files = queueFolder.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.getName().endsWith(".q")) {
                    if (now - f.lastModified() > 6000) {
                        f.delete();   // not updated for 6 seconds: that person closed the app, remove them
                    } else {
                        names.add(f.getName());   // still waiting
                    }
                }
            }
        }

        // 3) sort the names (oldest first). My place = where my file name is in the sorted list
        Collections.sort(names);
        int index = names.indexOf(myFile.getName());
        if (index < 0) {
            return 1;   // my file was not found, so act like I am first
        }
        return index + 1;   // list starts at 0 but places start at 1
    }

    // removes my file so I am no longer counted in the line
    void leaveQueue() {
        myFile.delete();
    }

    // puts the current position, wait time and "people ahead" text on the screen
    void updateLabels() {
        positionLabel.setText("[" + position + "]");

        // turn the total seconds into hours:minutes:seconds
        int h = secondsLeft / 3600;
        int m = (secondsLeft % 3600) / 60;
        int s = secondsLeft % 60;
        timeLabel.setText(String.format("%02d:%02d:%02d", h, m, s));   // %02d = 2 digits, with a zero in front if needed

        if (position > 1) {
            aheadLabel.setText("People ahead of you : " + (position - 1));
        } else {
            aheadLabel.setText("You're next! Almost there");
        }
    }
}
