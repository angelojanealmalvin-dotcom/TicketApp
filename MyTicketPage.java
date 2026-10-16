// MyTicketPage.java
// The "My Ticket" screen. It shows every ticket that was reserved, newest first.
// The tickets are read from the tickets file (see TicketData), so they are still
// here the next time the app is opened.
// If there are no tickets yet it shows a message and a "Browse Events" button.

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class MyTicketPage extends JFrame {

    Color blue = new Color(47, 91, 207);
    Color yellow = new Color(255, 212, 0);

    // builds the whole window
    public MyTicketPage() {
        // ----- basic window settings -----
        setTitle("My Tickets");
        setSize(900, 670);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);
        setLayout(null);
        getContentPane().setBackground(new Color(30, 28, 36));

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

        // "Events": go back to the events page
        JButton eventsBtn = navButton("Events", 535, 70, false);
        eventsBtn.addActionListener(e -> {
            new EventPage().setVisible(true);
            dispose();
        });
        nav.add(eventsBtn);

        // "My Ticket" is the page we are on now, so it has no click action (just yellow text)
        JButton ticketBtn = navButton("My Ticket", 620, 100, true);
        nav.add(ticketBtn);

        // "Log Out": ask first, then close the whole program
        JButton logoutBtn = navButton("Log Out", 740, 90, false);
        logoutBtn.addActionListener(e -> {
            int r = JOptionPane.showConfirmDialog(this, "Do you want to log out?", "Log Out", JOptionPane.YES_NO_OPTION);
            if (r == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });
        nav.add(logoutBtn);

        // ----- title -----
        JLabel title = new JLabel("My Tickets");
        title.setFont(new Font("Georgia", Font.PLAIN, 28));
        title.setForeground(Color.WHITE);
        title.setBounds(50, 92, 400, 40);
        add(title);

        JLabel sub = new JLabel("Seat format:  Section - Row - Seat");
        sub.setFont(new Font("Georgia", Font.ITALIC, 14));
        sub.setForeground(Color.LIGHT_GRAY);
        sub.setBounds(50, 132, 400, 22);
        add(sub);

        // ----- tickets -----
        // each ticket is a String[] (a small list of texts):
        // {0 id, 1 event name, 2 venue, 3 date, 4 seats, 5 total price, 6 number of tickets}
        ArrayList<String[]> tickets = TicketData.loadTickets();

        if (tickets.size() == 0) {
            // no tickets yet: show a message and a button to go back
            JLabel none = new JLabel("You don't have any tickets yet.", SwingConstants.CENTER);
            none.setFont(new Font("Georgia", Font.PLAIN, 22));
            none.setForeground(Color.WHITE);
            none.setBounds(50, 250, 800, 40);
            add(none);

            JButton browse = new JButton("Browse Events");
            browse.setBounds(375, 310, 150, 44);
            browse.setBackground(yellow);
            browse.setOpaque(true);
            browse.setBorderPainted(false);
            browse.setFocusPainted(false);
            browse.addActionListener(e -> {
                new EventPage().setVisible(true);
                dispose();
            });
            add(browse);
        } else {
            // there are tickets: put all the ticket cards in one panel, one under the other,
            // then put that panel inside a scroll pane so we can scroll when there are many
            JPanel list = new JPanel();
            list.setLayout(null);
            list.setBackground(new Color(30, 28, 36));
            // every card is 130 tall plus 14 of space, so the panel needs 144 of height for each ticket
            list.setPreferredSize(new Dimension(770, tickets.size() * 144));
            int y = 0;   // top position of the next card

            // loop from the LAST ticket to the first so the newest ticket is on top
            for (int i = tickets.size() - 1; i >= 0; i--) {
                String[] t = tickets.get(i);

                // the white-ish card for this ticket
                JPanel card = new JPanel();
                card.setLayout(null);
                card.setBackground(new Color(244, 244, 244));
                card.setBounds(0, y, 760, 130);   // x, y (how far down), width, height

                // yellow part on the left that shows how many tickets
                JPanel stub = new JPanel();
                stub.setLayout(null);
                stub.setBackground(yellow);
                stub.setBounds(0, 0, 120, 130);
                card.add(stub);

                JLabel count = new JLabel(t[6], SwingConstants.CENTER);
                count.setFont(new Font("Georgia", Font.BOLD, 40));
                count.setBounds(0, 30, 120, 50);
                stub.add(count);

                // "TICKET" for one, "TICKETS" for more than one
                String word = "TICKET";
                if (!t[6].equals("1")) {
                    word = "TICKETS";
                }
                JLabel countText = new JLabel(word, SwingConstants.CENTER);
                countText.setFont(new Font("Georgia", Font.PLAIN, 13));
                countText.setBounds(0, 80, 120, 20);
                stub.add(countText);

                // ticket details
                JLabel name = new JLabel(t[1]);   // event name
                name.setFont(new Font("Georgia", Font.BOLD, 22));
                name.setBounds(144, 12, 450, 30);
                card.add(name);

                JLabel where = new JLabel(t[2] + "   |   " + t[3]);   // venue | date
                where.setFont(new Font("Georgia", Font.PLAIN, 14));
                where.setBounds(144, 44, 450, 22);
                card.add(where);

                JLabel seats = new JLabel("Seats: " + t[4]);
                seats.setFont(new Font("Georgia", Font.BOLD, 14));
                seats.setBounds(144, 70, 450, 22);
                card.add(seats);

                JLabel id = new JLabel("Ticket ID: " + t[0]);
                id.setFont(new Font("Georgia", Font.PLAIN, 12));
                id.setBounds(144, 96, 450, 20);
                card.add(id);

                // total price on the right side of the card
                JLabel totalText = new JLabel("Total", SwingConstants.RIGHT);
                totalText.setFont(new Font("Georgia", Font.PLAIN, 12));
                totalText.setBounds(580, 14, 160, 20);
                card.add(totalText);

                JLabel total = new JLabel(t[5], SwingConstants.RIGHT);
                total.setFont(new Font("Georgia", Font.BOLD, 22));
                total.setBounds(540, 36, 200, 30);
                card.add(total);

                // add the finished card to the list, then move down for the next card
                list.add(card);
                y = y + 144;
            }

            // scroll bar appears when there are too many tickets to fit
            JScrollPane scroll = new JScrollPane(list);
            scroll.setBounds(50, 165, 800, 440);
            scroll.setBorder(null);
            scroll.getVerticalScrollBar().setUnitIncrement(24);   // how far the mouse wheel scrolls
            add(scroll);
        }
    }

    // makes one button for the blue bar (looks like plain text).
    // x = position, w = width, active = true for the page we are on (yellow text)
    JButton navButton(String text, int x, int w, boolean active) {
        JButton b = new JButton(text);
        b.setBounds(x, 20, w, 36);
        b.setMargin(new Insets(0, 0, 0, 0));   // remove the empty space inside the button, or long words get cut like "Ev..."
        b.setFont(new Font("Georgia", Font.PLAIN, 16));
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        if (active) {
            b.setForeground(yellow);
        } else {
            b.setForeground(Color.WHITE);
        }
        return b;
    }
}