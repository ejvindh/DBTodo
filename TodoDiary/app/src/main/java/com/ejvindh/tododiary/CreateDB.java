package com.ejvindh.tododiary;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Calendar;
import java.util.Locale;

import static java.lang.String.*;

class CreateDB {

    static int WriteDB(Integer year, String db_name, String date_flag, String endline, String encoding, String filePath) {
        int returnResult = 0;
        try {
            File dbFile = new File(filePath + "/" + db_name);
            if (!dbFile.exists()) {
                if (!dbFile.createNewFile()) {
                    returnResult = 1;
                }
            }
            BufferedWriter bw = new BufferedWriter
                    (new OutputStreamWriter(new FileOutputStream(dbFile), encoding));
            if (year == 0) {
                bw.write("New Jumble-file" + endline);
            } else {
                String[] dayNames = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
                String parseDayName;
                Calendar parseDays = Calendar.getInstance();
                parseDays.set(Calendar.YEAR, year);
                Integer DayOfYear = 1;
                parseDays.set(Calendar.DAY_OF_YEAR, DayOfYear);
                while (parseDays.get(Calendar.YEAR) == year) {
                    parseDayName = dayNames[parseDays.get(Calendar.DAY_OF_WEEK) - 1];
                    bw.write(date_flag + " " + parseDayName + " "
                            + parseDays.get(Calendar.YEAR) + "-"
                            + format(Locale.US, "%02d", (parseDays.get(Calendar.MONTH) + 1))
                            + "-" + format(Locale.US, "%02d", parseDays.get(Calendar.DAY_OF_MONTH)) + endline);
                    bw.write(endline);
                    DayOfYear++;
                    parseDays.set(Calendar.DAY_OF_YEAR, DayOfYear);
                }
            }
            bw.flush();
            bw.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
            returnResult = 2;
        }
        return returnResult;
    }
}