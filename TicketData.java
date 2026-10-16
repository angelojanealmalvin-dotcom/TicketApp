// TicketData.java
// This file has no window. It only keeps the DATA of the app, and the other pages use it:
//   - the list of events (name, venue, date, description, picture, prices)
//   - the seat layout (how many rows and seats in each section)
//   - saving tickets to a file, and reading them back
//   - checking if a seat is already taken
//
// Everything is "static" so we can use it directly without making an object,
// example: TicketData.names[0]  or  TicketData.isTaken(0, "B-1-1")

import java.io.BufferedReader;   // for reading a file line by line
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;       // for writing to a file
import java.util.ArrayList;

public class TicketData {

    // two sample descriptions. Replace the text with the real descriptions of your events
    static String lorem = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. "
            + "Etiam convallis quam ut ante sodales, sed mollis risus cursus. "
            + "Praesent nec lectus urna. Sed est mauris, aliquam sed posuere non, "
            + "luctus ut nibh. Praesent sit amet mi ut leo egestas auctor.";

    static String sample = "An unforgettable night of live music. Doors open at 5:00 PM and the show "
            + "starts at 7:00 PM. Replace this text with the real event description.";

    // ----- the events -----
    // these lists go together: item 0 of every list is Ben & Ben, item 1 is SB19, and so on
    static String[] names = {"Ben & Ben", "SB19", "Moira Dela Torre", "December Avenue"};
    static String[] venues = {"Philippine Arena", "SM Mall of Asia Arena", "Smart Araneta Coliseum", "New Frontier Theater"};
    static String[] dates = {"October 12, 2026", "November 21, 2026", "December 5, 2026", "January 16, 2027"};
    static String[] descriptions = {lorem, sample, sample, sample};
    static String[] images = {"/images/benandben.jpg", "/images/sb19.jpg", "/images/moira.jpg", "/images/decemberavenue.jpg"};

    // prices[event][section]  -> section A = 0, B = 1, C = 2
    // example: prices[1][2] = price of Section C for SB19
    static int[][] prices = {
        {4500, 6000, 4500},
        {4000, 5500, 4000},
        {3500, 5000, 3500},
        {3000, 4500, 3000}
    };

    // all tickets are saved in this file, so every copy of the app can see them
    // (this is what stops two people from reserving the same seat)
    // each line:  id|eventIndex|seats|total|count
    static File ticketFile = new File(System.getProperty("user.home"), ".ticketapp/mytickets.txt");

    // ----- the seat layout -----
    // how many rows a section has: B has 6, A and C have 9
    static int rows(String section) {
        if (section.equals("B")) {
            return 6;
        }
        return 9;
    }

    // how many seats are in each row: B has 12, A and C have 2
    static int cols(String section) {
        if (section.equals("B")) {
            return 12;
        }
        return 2;
    }

    // reads the tickets file and returns every reserved seat, like "0:B-3-7"
    // (the number before the ":" is the event number, so the same seat in another event is not counted)
    static ArrayList<String> getBookedSeats() {
        ArrayList<String> booked = new ArrayList<String>();
        if (!ticketFile.exists()) {
            return booked;   // no file yet = nothing reserved yet
        }
        try {
            BufferedReader br = new BufferedReader(new FileReader(ticketFile));
            String line;
            while ((line = br.readLine()) != null) {   // read one line at a time until there are no more
                String[] p = line.split("\\|");        // cut the line at every "|"
                if (p.length == 5) {
                    String[] seats = p[2].split(", ");   // "B-1-1, B-1-2" becomes {"B-1-1", "B-1-2"}
                    for (String s : seats) {
                        booked.add(p[1] + ":" + s);
                    }
                }
            }
            br.close();
        } catch (Exception ex) {
            ex.printStackTrace();   // prints the error in the Output window
        }
        return booked;
    }

    // true if the seat can not be picked. This one reads the file by itself
    static boolean isTaken(int event, String code) {
        return isTaken(event, code, getBookedSeats());
    }

    // same thing, but you give it the list so the file is not read again and again
    // (colorSeats() in SeatPage uses this one because it checks about 70 seats at once)
    static boolean isTaken(int event, String code, ArrayList<String> booked) {
        // 1) a seat is taken if it was reserved and saved in the file
        if (booked.contains(event + ":" + code)) {
            return true;
        }
        // 2) pretend some seats are already sold, so the map does not look empty.
        // this just uses a simple formula on the row and seat numbers
        String[] p = code.split("-");
        int row = Integer.parseInt(p[1]);
        int seat = Integer.parseInt(p[2]);
        if ((row * 3 + seat * 2 + event) % 4 == 0) {
            return true;
        }
        return false;
    }

    // adds one ticket at the end of the file (true in FileWriter = add, do not erase the old tickets)
    static void saveTicket(String id, int event, String seats, int total, int count) {
        try {
            ticketFile.getParentFile().mkdirs();   // make the folder if it is not there yet
            FileWriter fw = new FileWriter(ticketFile, true);
            fw.write(id + "|" + event + "|" + seats + "|" + total + "|" + count + "\n");
            fw.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // reads the file and returns the saved tickets, oldest first.
    // each ticket is a String[] = {id, event name, venue, date, seats, total price, count}
    static ArrayList<String[]> loadTickets() {
        ArrayList<String[]> list = new ArrayList<String[]>();
        if (!ticketFile.exists()) {
            return list;   // no file yet = no tickets
        }
        try {
            BufferedReader br = new BufferedReader(new FileReader(ticketFile));
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split("\\|");
                if (p.length == 5) {
                    int ev = Integer.parseInt(p[1]);   // the file only keeps the event NUMBER...
                    // ...so we look up the name, venue and date from the lists above
                    String[] t = {p[0], names[ev], venues[ev], dates[ev], p[2],
                        peso(Integer.parseInt(p[3])), p[4]};
                    list.add(t);
                }
            }
            br.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return list;
    }

    // turns a number into a price text. Example: 4500 becomes "PHP 4,500"
    static String peso(int amount) {
        return "PHP " + String.format("%,d", amount);   // %,d puts the comma in big numbers
    }
}
