// EventPage.java
// This is the FIRST screen of the app (the one you run). It shows the list of events,
// the picture, venue, date and description of the chosen event, lets the user pick how
// many tickets they want (1 to 4), and has the "Reserve Ticket" button that goes to the queue.
//
// How the screens connect:  EventPage -> QueuePage -> SeatPage -> MyTicketPage

import javax.swing.*;   // Swing = the library for windows, buttons, labels, etc.
import java.awt.*;      // Color, Font, Image, Insets...
import java.net.URL;    // used to find the picture files

// "extends JFrame" means this class IS a window
public class EventPage extends JFrame {

    // colors used on this page (the numbers are red, green, blue from 0 to 255)
    Color blue = new Color(47, 91, 207);
    Color yellow = new Color(255, 212, 0);
    Color gray = new Color(217, 217, 217);

    int selected = 0;   // which event is chosen right now (0 = Ben & Ben, 1 = SB19, ...)
    int qty = 1;        // how many tickets the user wants

    // these are saved up here (not inside the constructor) because other methods
    // like showEvent() need to change them later
    JButton[] tags = new JButton[4];                    // the 4 event name buttons
    JLabel photo = new JLabel("", SwingConstants.CENTER);   // the event picture
    JLabel artist = new JLabel("", SwingConstants.CENTER);  // the artist name under the picture
    JLabel venueLabel = new JLabel();                   // shows the venue
    JLabel dateLabel = new JLabel();                    // shows the date
    JTextArea desc = new JTextArea();                   // shows the event description
    JLabel qtyLabel = new JLabel("1", SwingConstants.CENTER);   // shows the ticket quantity

    // the constructor runs when we do "new EventPage()". It builds the whole window.
    public EventPage() {
        // ----- basic window settings -----
        setTitle("Live Performance");
        setSize(900, 670);
        setDefaultCloseOperation(EXIT_ON_CLOSE);   // closing the window stops the program
        setResizable(false);
        setLocationRelativeTo(null);               // puts the window in the middle of the screen
        setLayout(null);                           // null layout = we place everything ourselves with setBounds(x, y, width, height)
        getContentPane().setBackground(new Color(30, 28, 36));   // dark background

        // ----- blue bar on top -----
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

        // "Events" is the page we are already on, so it has no click action (just yellow text)
        JButton eventsBtn = navButton("Events", 535, 70, true);
        nav.add(eventsBtn);

        // "My Ticket": open the My Ticket window and close this one
        JButton ticketBtn = navButton("My Ticket", 620, 100, false);
        ticketBtn.addActionListener(e -> {
            new MyTicketPage().setVisible(true);
            dispose();   // dispose() closes only this window (not the whole program)
        });
        nav.add(ticketBtn);

        // "Log Out": ask first, and if the user says Yes, stop the whole program
        JButton logoutBtn = navButton("Log Out", 740, 90, false);
        logoutBtn.addActionListener(e -> {
            int r = JOptionPane.showConfirmDialog(this, "Do you want to log out?", "Log Out", JOptionPane.YES_NO_OPTION);
            if (r == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });
        nav.add(logoutBtn);

        // ----- choose event buttons -----
        JLabel pick = new JLabel("Choose event:");
        pick.setFont(new Font("Georgia", Font.ITALIC, 15));
        pick.setForeground(Color.WHITE);
        pick.setBounds(50, 90, 110, 30);
        add(pick);

        // make one button for each event, placed side by side
        int x = 160;   // x position of the next button
        for (int i = 0; i < 4; i++) {
            final int index = i;   // "final" copy of i, because the button click code below needs it
            int w = TicketData.names[i].length() * 9 + 30;   // longer names get wider buttons
            tags[i] = new JButton(TicketData.names[i]);
            tags[i].setBounds(x, 90, w, 30);
            tags[i].setFont(new Font("Georgia", Font.PLAIN, 14));
            tags[i].setMargin(new Insets(0, 0, 0, 0));   // no empty space inside, so the name is not cut
            tags[i].setOpaque(true);           // these 3 lines make the button show our color
            tags[i].setBorderPainted(false);   // and remove the default 3D look
            tags[i].setFocusPainted(false);
            tags[i].addActionListener(e -> showEvent(index));   // clicking shows that event's info
            add(tags[i]);
            x = x + w + 10;   // move to the right for the next button
        }

        // ----- left side: picture, name, reserve button, ticket quantity -----
        photo.setBounds(50, 130, 410, 280);
        photo.setOpaque(true);
        photo.setBackground(new Color(242, 183, 5));   // yellow box shown when the picture file is missing
        photo.setForeground(Color.WHITE);
        photo.setFont(new Font("Georgia", Font.BOLD, 28));
        add(photo);

        artist.setFont(new Font("Georgia", Font.PLAIN, 24));
        artist.setForeground(Color.WHITE);
        artist.setBounds(50, 420, 410, 36);
        add(artist);

        // Reserve button: go to the queue with the chosen event and ticket quantity
        JButton reserve = new JButton("Reserve Ticket");
        reserve.setBounds(50, 486, 180, 46);
        reserve.setBackground(yellow);
        reserve.setFont(new Font("Georgia", Font.BOLD, 16));
        reserve.setOpaque(true);
        reserve.setBorderPainted(false);
        reserve.setFocusPainted(false);
        reserve.addActionListener(e -> {
            new QueuePage(selected, qty).setVisible(true);   // we pass the event number and quantity to the next page
            dispose();
        });
        add(reserve);

        JLabel ticketsText = new JLabel("Tickets", SwingConstants.CENTER);
        ticketsText.setFont(new Font("Georgia", Font.PLAIN, 15));
        ticketsText.setForeground(Color.WHITE);
        ticketsText.setBounds(310, 466, 80, 22);
        add(ticketsText);

        // "<" button: lower the quantity, but never below 1
        JButton left = new JButton("<");
        left.setBounds(285, 497, 36, 30);
        left.setMargin(new Insets(0, 0, 0, 0));
        left.addActionListener(e -> {
            if (qty > 1) {
                qty--;
            }
            qtyLabel.setText("" + qty);   // "" + number turns the number into text
        });
        add(left);

        qtyLabel.setFont(new Font("Georgia", Font.BOLD, 16));
        qtyLabel.setOpaque(true);
        qtyLabel.setBackground(gray);
        qtyLabel.setBounds(325, 495, 50, 38);
        add(qtyLabel);

        // ">" button: raise the quantity, but never above 4
        JButton right = new JButton(">");
        right.setBounds(379, 497, 36, 30);
        right.setMargin(new Insets(0, 0, 0, 0));
        right.addActionListener(e -> {
            if (qty < 4) {
                qty++;
            }
            qtyLabel.setText("" + qty);
        });
        add(right);

        // ----- right side: title and the gray info box -----
        JLabel title = new JLabel("Upcoming Live Performance", SwingConstants.CENTER);
        title.setFont(new Font("Georgia", Font.PLAIN, 23));
        title.setForeground(Color.WHITE);
        title.setBounds(470, 160, 395, 40);
        add(title);

        // gray box that holds the venue, date and description
        // (the labels inside use positions relative to this box, not the window)
        JPanel info = new JPanel();
        info.setLayout(null);
        info.setBackground(gray);
        info.setBounds(480, 215, 375, 350);
        add(info);

        JLabel venueText = new JLabel("Venue :");
        venueText.setFont(new Font("Georgia", Font.PLAIN, 17));
        venueText.setBounds(16, 16, 90, 26);
        info.add(venueText);

        // the real venue text is filled in by showEvent()
        venueLabel.setFont(new Font("Georgia", Font.BOLD, 18));
        venueLabel.setBounds(112, 16, 255, 26);
        info.add(venueLabel);

        JLabel dateText = new JLabel("Date :");
        dateText.setFont(new Font("Georgia", Font.PLAIN, 17));
        dateText.setBounds(16, 74, 90, 26);
        info.add(dateText);

        dateLabel.setFont(new Font("Georgia", Font.BOLD, 18));
        dateLabel.setBounds(112, 74, 255, 26);
        info.add(dateLabel);

        JLabel descText = new JLabel("Event Description");
        descText.setFont(new Font("Georgia", Font.PLAIN, 19));
        descText.setBounds(16, 124, 300, 28);
        info.add(descText);

        desc.setEditable(false);      // the user cannot type in it
        desc.setLineWrap(true);       // long text goes to the next line...
        desc.setWrapStyleWord(true);  // ...without cutting words in half
        desc.setOpaque(false);        // see-through so the gray shows
        desc.setFont(new Font("Georgia", Font.PLAIN, 15));
        desc.setBounds(14, 158, 347, 180);
        info.add(desc);

        // show the first event when the page opens
        showEvent(0);
    }

    // makes one button for the blue bar (looks like plain text).
    // x = position, w = width, active = true for the page we are on (yellow text)
    JButton navButton(String text, int x, int w, boolean active) {
        JButton b = new JButton(text);
        b.setBounds(x, 20, w, 36);
        b.setMargin(new Insets(0, 0, 0, 0));   // remove the empty space inside the button, or long words get cut like "Ev..."
        b.setFont(new Font("Georgia", Font.PLAIN, 16));
        b.setContentAreaFilled(false);   // no gray button background
        b.setBorderPainted(false);       // no border
        b.setFocusPainted(false);        // no box around the text when clicked
        if (active) {
            b.setForeground(yellow);
        } else {
            b.setForeground(Color.WHITE);
        }
        return b;
    }

    // shows the info of event number i (0 to 3): colors the buttons, loads the picture, and fills the labels
    void showEvent(int i) {
        selected = i;   // remember it, the Reserve button uses this

        // the chosen button turns yellow, the other three turn gray
        for (int k = 0; k < 4; k++) {
            if (k == i) {
                tags[k].setBackground(yellow);
                tags[k].setForeground(Color.BLACK);
            } else {
                tags[k].setBackground(new Color(90, 88, 96));
                tags[k].setForeground(Color.WHITE);
            }
        }

        // try to load the picture. getResource gives null if the file is not found
        URL url = getClass().getResource(TicketData.images[i]);
        if (url != null) {
            ImageIcon icon = new ImageIcon(url);
            Image img = icon.getImage().getScaledInstance(410, 280, Image.SCALE_SMOOTH);   // resize to fit the box
            photo.setIcon(new ImageIcon(img));
            photo.setText("");
        } else {
            // no picture file: show the artist name on the yellow box instead
            photo.setIcon(null);
            photo.setText(TicketData.names[i]);
        }

        // fill in the text from the data in TicketData
        artist.setText(TicketData.names[i]);
        venueLabel.setText(TicketData.venues[i]);
        dateLabel.setText(TicketData.dates[i]);
        desc.setText(TicketData.descriptions[i]);
    }

    // the program starts here. Run THIS file to start the app.
    public static void main(String[] args) {
        new EventPage().setVisible(true);   // setVisible(true) shows the window
    }
}