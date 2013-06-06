package cc.thiago;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.io.Files;

public class Timesheet {

    public static final int ONE_HOUR = 60;
    public static final int TARGET_PER_DAY = ONE_HOUR * 8;
    public static final char COMMA_SEPARATED = ';';
    public static int TOTAL_EXTRA_TIME = 0;
    public static final String CSV_HEADER = "Data" + COMMA_SEPARATED + "Entrada" + COMMA_SEPARATED + "Saida" + COMMA_SEPARATED + "Extra" + "\n"; 

    public static void main(String[] args) throws IOException {
        generateCSVReport();
    }

    public static void generateCSVReport() throws IOException {
        StringBuilder finalReport = new StringBuilder();
        finalReport.append(CSV_HEADER);

        List<String> reports = Files.readLines(new File("timesheet.txt"), Charsets.UTF_8);
        for (String report : reports) {
            if (Strings.isNullOrEmpty(report)) continue;
            try {
                String dailyReport = generateDailyReport(report);
                System.out.println(dailyReport);
                finalReport.append(dailyReport);
            } catch (IllegalArgumentException e) {
                System.out.println(report + " > Invalid!");
            }
        }
        System.out.println("Total extra time: " + TOTAL_EXTRA_TIME);

        Files.write(finalReport.toString().getBytes(), new File("timesheet.csv"));
    }

    public static GregorianCalendar buildCalendar(int day, int month, int year, int hour, int minutes) {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, (month -1));
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minutes);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    // 22/04/2013 11:30 12:40 13:30 20:30
    // 0123456789012345678901234567890123
    public static String generateDailyReport(String report) {
        if (report.length() < 34) throw new IllegalArgumentException("Invalid string to parsed");

        try {
            int day = Integer.parseInt(report.substring(0, 2));
            int month = Integer.parseInt(report.substring(3, 5));
            int year = Integer.parseInt(report.substring(6, 10));

            // period 1 start
            int p1Hour = Integer.parseInt(report.substring(11, 13));
            int p1Minutes = Integer.parseInt(report.substring(14, 16));

            /*
            period 2 start
            int p2Hour = Integer.parseInt(report.substring(17, 19));
            int p2Minutes = Integer.parseInt(report.substring(20, 22));

            period 3 start
            int p3Hour = Integer.parseInt(report.substring(23, 25));
            int p3Minutes = Integer.parseInt(report.substring(26, 28));
            */

            // period 4 start
            int p4Hour = Integer.parseInt(report.substring(29, 31));
            int p4Minutes = Integer.parseInt(report.substring(32, 34));

            GregorianCalendar start = buildCalendar(day, month, year, p1Hour, p1Minutes);
            GregorianCalendar end = buildCalendar(day, month, year, p4Hour, p4Minutes);

            long totalMinutesFromInterval = calculateTotalMinutesFromInterval(start, end);
            long total = totalMinutesFromInterval - ONE_HOUR; // lunch time
            Long extraTime = total - TARGET_PER_DAY;

            TOTAL_EXTRA_TIME += extraTime.intValue();

            return formatCSVReport(start, end, extraTime.intValue()); 
        } catch (NumberFormatException ne) {
            return null;
        }
    }

    private static String formatCSVReport(GregorianCalendar start, GregorianCalendar end, long totalMinutesFromInterval) {
        return new SimpleDateFormat("dd/MM/yyyy").format(start.getTime()) + COMMA_SEPARATED + new SimpleDateFormat("HH:mm:ss").format(start.getTime()) + COMMA_SEPARATED + new SimpleDateFormat("HH:mm:ss").format(end.getTime()) + COMMA_SEPARATED + totalMinutesFromInterval + "\n";
    }

    public static long calculateTotalMinutesFromInterval(GregorianCalendar start, GregorianCalendar end) {
        if (end.before(start)) throw new IllegalArgumentException("Invalid date range.");
        long totalInMilliseconds = end.getTimeInMillis() - start.getTimeInMillis();

        return TimeUnit.MILLISECONDS.toMinutes(totalInMilliseconds);
    }
}
