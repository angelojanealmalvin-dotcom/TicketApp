// SeatPage.java
// The seat selection screen. The user arrives here after the queue is finished.
// They have 5 minutes to pick their seat(s), then press Confirm.
// Confirming saves the ticket (see TicketData.saveTicket) and opens My Ticket.
//
// The seat map is made of small buttons: one button for every seat.
// Every seat has a code like "B-3-7" = Section B, Row 3, Seat 7.
// Section A and C: 9 rows x 2 seats.   Section B: 6 rows x 12 seats.

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class SeatPage extends JFrame {

    Color blue = new Color(47, 91, 207);
    Color yellow = new Color(255, 212, 0);
    Color gray = new Color(217, 217, 217);
    Color AVAIL = new Color(196, 196, 196);   // seat color: free to pick
    Color OCC = new Color(138, 138, 138);     // seat color: already taken
    Color SEL = new Color(60, 60, 60);        // seat color: picked by me

    int eventIndex;           // which event (sent from the queue page)
    int qty;                  // how many seats I must pick
    int secondsLeft = 300;    // 5 minutes to choose

    ArrayList<String> picked = new ArrayList<String>();         // codes of the seats I picked, like "B-1-1"
    ArrayList<JButton> seatButtons = new ArrayList<JButton>();  // every seat button, so I can recolor them later

    JLabel clockLabel = new JLabel("00:05:00");   // countdown shown on the screen
    JLabel selectedLabel = new JLabel();          // list of picked seats
    JLabel priceLabel = new JLabel();             // total price
    JLabel statusLabel = new JLabel(" ");         // red message line for errors
    JTextField search = new JTextField();         // box to type a seat code
    JButton confirm = new JButton("Confirm");
    javax.swing.Timer timer;                      // runs every second to count down

    public SeatPage(int eventIndex, int qty) {
        this.eventIndex = eventIndex;
        this.qty = qty;

        // ----- basic window settings -----
        setTitle("Select Your Seats");
        setSize(900, 670);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);
        setLayout(null);
        getContentPane().setBackground(Color.WHITE);

        // ----- blue bar on top -----
        // the buttons here call goTo() which asks "Leave seat selection?" first
        JPanel nav = new JPanel();
        nav.setLayout(null);
        nav.setBackground(blue);
        nav.setBounds(0, 0, 900, 76);
        add(nav);

        JLabel logo = new JLabel("Logo");
        logo.setFont(new Font("Georgia", Font.BOLD, 26));
        logo.setForeground(Color.WHITE);
        logo.setBounds(85, 20, 120, 36);
        nav.add(logo);

        JButton eventsBtn = navButton("Events", 535, 70, true);
        eventsBtn.addActionListener(e -> goTo(0));
        nav.add(eventsBtn);

        JButton ticketBtn = navButton("My Ticket", 620, 100, false);
        ticketBtn.addActionListener(e -> goTo(1));
        nav.add(ticketBtn);

        JButton logoutBtn = navButton("Log Out", 740, 90, false);
        logoutBtn.addActionListener(e -> goTo(2));
        nav.add(logoutBtn);

        // ----- left side: title, seat map, legend -----
        JLabel title = new JLabel("Secure your tickets");
        title.setFont(new Font("Georgia", Font.BOLD, 25));
        title.setBounds(50, 92, 420, 36);
        add(title);

        JLabel sub = new JLabel("Seat Selection", SwingConstants.CENTER);
        sub.setFont(new Font("Georgia", Font.PLAIN, 17));
        sub.setBounds(50, 130, 470, 26);
        add(sub);

        // gray box that holds the stage, section letters and all the seat buttons
        JPanel seatPanel = new JPanel();
        seatPanel.setLayout(null);
        seatPanel.setBackground(gray);
        seatPanel.setBounds(50, 162, 470, 300);
        add(seatPanel);

        JLabel stage = new JLabel("STAGE", SwingConstants.CENTER);
        stage.setOpaque(true);
        stage.setBackground(new Color(181, 181, 181));
        stage.setForeground(Color.WHITE);
        stage.setBounds(130, 28, 210, 56);
        seatPanel.add(stage);

        // section letters above each block of seats
        JLabel a = new JLabel("A");
        a.setBounds(14, 14, 20, 18);
        seatPanel.add(a);
        JLabel b = new JLabel("B");
        b.setBounds(232, 92, 20, 18);
        seatPanel.add(b);
        JLabel c = new JLabel("C");
        c.setBounds(410, 14, 20, 18);
        seatPanel.add(c);

        // make the seat buttons: A on the left, B in the middle (below the stage), C on the right.
        // the two numbers are where the first seat of the section is placed inside seatPanel
        makeSection(seatPanel, "A", 14, 36);
        makeSection(seatPanel, "B", 82, 114);
        makeSection(seatPanel, "C", 410, 36);

        // small color guide under the map
        legend(70, 476, OCC, "Occupied");
        legend(210, 476, AVAIL, "Available");
        legend(350, 476, SEL, "Selected");

        // price of each section for this event
        JLabel prices = new JLabel("Section A  " + TicketData.peso(TicketData.prices[eventIndex][0])
                + "     Section B  " + TicketData.peso(TicketData.prices[eventIndex][1])
                + "     Section C  " + TicketData.peso(TicketData.prices[eventIndex][2]), SwingConstants.CENTER);
        prices.setFont(new Font("Georgia", Font.PLAIN, 14));
        prices.setBounds(50, 514, 470, 24);
        add(prices);

        // ----- right side: timer, search, summary, buttons -----
        int x = 545;   // x position shared by most things on the right side

        JLabel tr = new JLabel("TIME REMAINING :");
        tr.setFont(new Font("Georgia", Font.BOLD, 15));
        tr.setBounds(535, 94, 160, 32);
        add(tr);

        clockLabel.setFont(new Font("Georgia", Font.BOLD, 20));
        clockLabel.setOpaque(true);
        clockLabel.setBackground(gray);
        clockLabel.setHorizontalAlignment(SwingConstants.CENTER);
        clockLabel.setBounds(697, 94, 110, 32);
        add(clockLabel);

        JLabel name = new JLabel(TicketData.names[eventIndex]);
        name.setFont(new Font("Georgia", Font.BOLD, 18));
        name.setBounds(x, 146, 315, 26);
        add(name);

        JLabel where = new JLabel(TicketData.venues[eventIndex] + "  |  " + TicketData.dates[eventIndex]);
        where.setFont(new Font("Georgia", Font.PLAIN, 14));
        where.setBounds(x, 172, 315, 22);
        add(where);

        // search box + button: type a seat code like B-3-7 and it will be picked for you
        search.setBounds(x, 206, 230, 34);
        add(search);

        JButton searchBtn = new JButton("Search");
        searchBtn.setBounds(x + 238, 206, 77, 34);
        searchBtn.addActionListener(e -> doSearch());
        add(searchBtn);

        JLabel hint = new JLabel("Search seat (e.g. B-3-7)");
        hint.setFont(new Font("Georgia", Font.ITALIC, 12));
        hint.setBounds(x, 240, 315, 18);
        add(hint);

        selectedLabel.setFont(new Font("Georgia", Font.PLAIN, 15));
        selectedLabel.setVerticalAlignment(SwingConstants.TOP);
        selectedLabel.setBounds(x, 266, 315, 100);
        add(selectedLabel);

        priceLabel.setFont(new Font("Georgia", Font.PLAIN, 15));
        priceLabel.setBounds(x, 370, 315, 26);
        add(priceLabel);

        // Confirm button: it stays disabled until I picked exactly qty seats (see updateSummary)
        confirm.setBounds(x, 404, 140, 38);
        confirm.setBackground(yellow);
        confirm.setOpaque(true);
        confirm.setBorderPainted(false);
        confirm.setFocusPainted(false);
        confirm.addActionListener(e -> confirmBooking());
        add(confirm);

        JButton cancel = new JButton("Cancel");
        cancel.setBounds(x + 152, 404, 110, 38);
        cancel.setBackground(gray);
        cancel.setOpaque(true);
        cancel.setBorderPainted(false);
        cancel.setFocusPainted(false);
        cancel.addActionListener(e -> goTo(0));
        add(cancel);

        // red text for messages like "That seat is already taken."
        statusLabel.setFont(new Font("Georgia", Font.PLAIN, 13));
        statusLabel.setForeground(new Color(192, 57, 43));
        statusLabel.setBounds(x, 452, 315, 22);
        add(statusLabel);

        // note at the bottom ("seat" or "seats" depending on the quantity)
        String s = "";
        if (qty > 1) {
            s = "s";
        }
        JLabel note = new JLabel("<html><i>Please select " + qty + " seat" + s
                + ". Your seats are held only while the timer is running.</i></html>");
        note.setFont(new Font("Georgia", Font.PLAIN, 13));
        note.setBounds(x, 480, 315, 56);
        add(note);

        // color the seats and fill the summary for the first time
        colorSeats();
        updateSummary();

        // start the 5 minute countdown. tick() runs every second
        timer = new javax.swing.Timer(1000, e -> tick());
        timer.start();
    }

    // makes one button for the blue bar (looks like plain text).
    // x = position, w = width, active = true for the page we are on (yellow text)
    JButton navButton(String text, int x, int w, boolean active) {
        JButton btn = new JButton(text);
        btn.setBounds(x, 20, w, 36);
        btn.setMargin(new Insets(0, 0, 0, 0));   // remove the empty space inside the button, or long words get cut like "Ev..."
        btn.setFont(new Font("Georgia", Font.PLAIN, 16));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        if (active) {
            btn.setForeground(yellow);
        } else {
            btn.setForeground(Color.WHITE);
        }
        return btn;
    }

    // draws one item of the color guide: a small colored square with a word next to it
    void legend(int x, int y, Color color, String text) {
        JPanel box = new JPanel();
        box.setBackground(color);
        box.setBounds(x, y + 3, 16, 16);
        add(box);
        JLabel l = new JLabel(text);
        l.setFont(new Font("Georgia", Font.PLAIN, 16));
        l.setBounds(x + 26, y, 100, 22);
        add(l);
    }

    // makes all the seat buttons of one section (A, B or C).
    // x0 and y0 = where the first seat is placed. Each seat is 20x20 with a 6 pixel gap (26 in total)
    void makeSection(JPanel panel, String sec, int x0, int y0) {
        for (int row = 1; row <= TicketData.rows(sec); row++) {
            for (int seat = 1; seat <= TicketData.cols(sec); seat++) {
                // the seat code, example "B-3-7"
                final String code = sec + "-" + row + "-" + seat;
                JButton btn = new JButton();
                btn.setBounds(x0 + (seat - 1) * 26, y0 + (row - 1) * 26, 20, 20);
                btn.setMargin(new Insets(0, 0, 0, 0));
                btn.setOpaque(true);
                btn.setBorderPainted(false);
                btn.setFocusPainted(false);
                btn.setActionCommand(code);   // store the code inside the button so colorSeats() can read it later
                btn.setToolTipText("Section " + sec + ", Row " + row + ", Seat " + seat);   // text shown when the mouse stays on the seat
                btn.addActionListener(e -> seatClicked(code));
                panel.add(btn);
                seatButtons.add(btn);   // remember it in the list
            }
        }
    }

    // goes through every seat button and sets its color:
    // dark gray = taken, darkest = picked by me, light gray = free
    void colorSeats() {
        ArrayList<String> booked = TicketData.getBookedSeats();   // read the tickets file only ONE time for all seats
        for (JButton btn : seatButtons) {
            String code = btn.getActionCommand();
            if (TicketData.isTaken(eventIndex, code, booked)) {
                btn.setBackground(OCC);
            } else if (picked.contains(code)) {
                btn.setBackground(SEL);
            } else {
                btn.setBackground(AVAIL);
            }
        }
    }

    // runs when a seat is clicked (or found by the search box)
    void seatClicked(String code) {
        // 1) cannot pick a seat that is already taken
        if (TicketData.isTaken(eventIndex, code)) {
            statusLabel.setText("That seat is already taken.");
            return;
        }
        if (picked.contains(code)) {
            // 2) clicking a seat I already picked will unpick it
            picked.remove(code);
        } else if (picked.size() >= qty) {
            // 3) I already picked all the seats I need
            if (qty == 1) {
                picked.clear();      // with 1 ticket, just swap to the new seat
                picked.add(code);
            } else {
                statusLabel.setText("You can only pick " + qty + " seats. Click one to unselect it.");
                return;
            }
        } else {
            // 4) normal case: add the seat
            picked.add(code);
        }
        statusLabel.setText(" ");   // clear any old error
        updateSummary();
        colorSeats();
    }

    // runs when the Search button is clicked. Reads text like "B-3-7", checks that the seat exists,
    // and picks it if it is free
    void doSearch() {
        String text = search.getText().trim().toUpperCase();   // remove spaces and make it capital letters
        String[] p = text.split("-");                          // "B-3-7" becomes {"B", "3", "7"}
        if (p.length != 3) {
            statusLabel.setText("Use Section-Row-Seat, for example B-3-7.");
            return;
        }
        String sec = p[0];
        int row;
        int seat;
        try {
            // turn the text into numbers. If the user typed letters here it will fail and go to catch
            row = Integer.parseInt(p[1].trim());
            seat = Integer.parseInt(p[2].trim());
        } catch (NumberFormatException ex) {
            statusLabel.setText("Use Section-Row-Seat, for example B-3-7.");
            return;
        }
        if (!(sec.equals("A") || sec.equals("B") || sec.equals("C"))) {
            statusLabel.setText("Section must be A, B or C.");
            return;
        }
        // check that the row and seat numbers are inside the section
        if (row < 1 || row > TicketData.rows(sec) || seat < 1 || seat > TicketData.cols(sec)) {
            statusLabel.setText("Section " + sec + " has rows 1-" + TicketData.rows(sec)
                    + " and seats 1-" + TicketData.cols(sec) + ".");
            return;
        }
        String code = sec + "-" + row + "-" + seat;
        if (TicketData.isTaken(eventIndex, code)) {
            statusLabel.setText("Seat " + code + " is already taken.");
        } else if (!picked.contains(code)) {
            seatClicked(code);   // same as clicking the seat
        }
    }

    // adds up the price of all the seats I picked.
    // the first letter of the code (A, B or C) tells which price to use
    int totalPrice() {
        int total = 0;
        for (String code : picked) {
            // 'A' - 'A' = 0, 'B' - 'A' = 1, 'C' - 'A' = 2  -> position in the prices list
            total = total + TicketData.prices[eventIndex][code.charAt(0) - 'A'];
        }
        return total;
    }

    // updates the "Selected Seats" text, the price, and turns the Confirm button on or off
    void updateSummary() {
        String text = "<html><b>Selected Seats :</b>";   // html lets us use bold and line breaks (<br>) in a label
        if (picked.isEmpty()) {
            text = text + " none yet";
        } else {
            for (String code : picked) {
                String[] p = code.split("-");
                text = text + "<br>Section " + p[0] + ", Row " + p[1] + ", Seat " + p[2];
            }
        }
        text = text + "<br>(" + picked.size() + " of " + qty + " selected)</html>";
        selectedLabel.setText(text);

        if (picked.isEmpty()) {
            priceLabel.setText("Price : -");
        } else {
            priceLabel.setText("Price : " + TicketData.peso(totalPrice()));
        }
        // Confirm only works when the number of picked seats is exactly the quantity
        confirm.setEnabled(picked.size() == qty);
    }

    // runs every second: counts down the 5 minutes, refreshes the seat colors,
    // and sends the user back to the events page when the time is over
    void tick() {
        secondsLeft--;
        int m = secondsLeft / 60;
        int s = secondsLeft % 60;
        clockLabel.setText(String.format("00:%02d:%02d", m, s));
        colorSeats();   // so seats taken by other people turn dark right away
        if (secondsLeft <= 0) {
            timer.stop();
            JOptionPane.showMessageDialog(this, "Your seat selection time has run out.\nPlease try again.",
                    "Time's Up", JOptionPane.WARNING_MESSAGE);
            new EventPage().setVisible(true);
            dispose();
        }
    }

    // runs when Confirm is clicked: asks to confirm, checks the seats one last time, saves the ticket
    void confirmBooking() {
        // build the message for the confirm box, and also a text list of the seats ("B-1-1, B-1-2")
        String msg = "Reserve these seats?\n\n" + TicketData.names[eventIndex] + "\n"
                + TicketData.venues[eventIndex] + " - " + TicketData.dates[eventIndex] + "\n\n";
        String seats = "";
        for (String code : picked) {
            msg = msg + code + "\n";
            if (seats.equals("")) {
                seats = code;
            } else {
                seats = seats + ", " + code;
            }
        }
        msg = msg + "\nTotal: " + TicketData.peso(totalPrice());

        int r = JOptionPane.showConfirmDialog(this, msg, "Confirm Reservation", JOptionPane.YES_NO_OPTION);
        if (r != JOptionPane.YES_OPTION) {
            return;   // user pressed No
        }

        // check again: maybe someone else reserved one of my seats while I was choosing
        // (another copy of the app saves to the same file, so we can see it here)
        ArrayList<String> gone = new ArrayList<String>();
        for (String code : picked) {
            if (TicketData.isTaken(eventIndex, code)) {
                gone.add(code);
            }
        }
        if (gone.size() > 0) {
            picked.removeAll(gone);   // remove the lost seats from my selection
            updateSummary();
            colorSeats();
            JOptionPane.showMessageDialog(this, "Sorry, one of your seats was just taken.\nPlease pick another seat.",
                    "Seat Unavailable", JOptionPane.WARNING_MESSAGE);
            return;   // nothing is saved, the user picks another seat
        }

        // all seats are still free: make a ticket number and save the ticket to the file
        String id = "TKT-" + (int) (Math.random() * 90000 + 10000);
        TicketData.saveTicket(id, eventIndex, seats, totalPrice(), picked.size());

        timer.stop();
        JOptionPane.showMessageDialog(this, "Your ticket has been saved!\n\nTicket ID: " + id
                + "\nYou can find it in My Ticket.", "Reservation Confirmed", JOptionPane.INFORMATION_MESSAGE);
        new MyTicketPage().setVisible(true);
        dispose();
    }

    // used by the blue bar buttons and the Cancel button.
    // asks "Leave seat selection?" and if Yes, goes to: 0 = events, 1 = my ticket, 2 = log out
    void goTo(int where) {
        int r = JOptionPane.showConfirmDialog(this,
                "Leave seat selection? Your seats will not be reserved.", "Leave", JOptionPane.YES_NO_OPTION);
        if (r != JOptionPane.YES_OPTION) {
            return;   // stay on this page
        }
        timer.stop();   // stop the countdown because we are leaving
        if (where == 0) {
            new EventPage().setVisible(true);
            dispose();
        } else if (where == 1) {
            new MyTicketPage().setVisible(true);
            dispose();
        } else {
            System.exit(0);
        }
    }
}