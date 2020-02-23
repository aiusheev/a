import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

public class LogParser {

    private static final String SUCCESS = "успех";
    private static final String TOP_UP = "top up";
    private static final char SEPARATOR = ',';

    private Date startDate;
    private Date endDate;

    private int toTopUpAttempts;
    private int topUpFail;
    private int pouredWaterVolume;
    private int notPouredWaterVolume;

    private int toScoopAttempts;
    private int scoopFail;
    private int scoopedWaterVolume;
    private int notScoopedWaterVolume;

    private int currentV;
    private int startV;
    private int endV;
    private Status lineStatus;

    public static void main(String[] args) {
        try(BufferedReader fr = new BufferedReader(new FileReader(args[0]));
            FileWriter fw = new FileWriter("result.csv")) {
            LogParser logParser = new LogParser();
            logParser.prepareParams(args);
            logParser.parse(fr, fw);
        } catch (Exception e) {
            System.out.println("usage");
        }
    }

    private void prepareParams(String[] args) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        startDate = formatter.parse(args[1]);
        endDate = formatter.parse(args[2]);
    }

    private void parse(BufferedReader fr, FileWriter fw) throws Exception {
        fr.readLine();
        fr.readLine();
        currentV = Integer.parseInt(fr.readLine().replace(" (текущий объем воды в бочке)", ""));

        boolean inNeededPeriod = false;
        Iterator<String> it = fr.lines().iterator();
        while (it.hasNext()) {
            String line = it.next();

            int liters = parseVolume(line);
            setCurrentVolumeAndStatus(line, liters);

            Date lineDate = parseDate(line);
            if (lineDate.after(endDate)) {
                finishHim(fw);
                return;
            }

            inNeededPeriod = isInNeededPeriod(startDate, inNeededPeriod, lineDate);
            if (inNeededPeriod) {
                switch (lineStatus) {
                    case TOP_UP_SUCCESS:
                        toTopUpAttempts++;
                        pouredWaterVolume += liters;
                        break;
                    case TOP_UP_FAIL:
                        toTopUpAttempts++;
                        topUpFail++;
                        notPouredWaterVolume += liters;
                        break;
                    case SCOOP_SUCCESS:
                        toScoopAttempts++;
                        scoopedWaterVolume += liters;
                        break;
                    case SCOOP_FAIL:
                        toScoopAttempts++;
                        scoopFail++;
                        notScoopedWaterVolume += liters;
                        break;
                    default:
                        throw new Exception("Not found lineStatus");
                }
            }
        }
    }

    private void finishHim(FileWriter fw) throws IOException {
        endV = currentV;
        String cvs = writeResult();
        fw.append(cvs);
    }

    private String writeResult() {
        StringBuilder sb = new StringBuilder();

        sb.append("toTopUpAttempts").append(SEPARATOR).append(toTopUpAttempts).append("\n");
        sb.append("topUpFailPercent").append(SEPARATOR).append(toTopUpAttempts != 0 ? topUpFail * 100 / toTopUpAttempts : "0").append("\n");
        sb.append("pouredWaterVolume").append(SEPARATOR).append(pouredWaterVolume).append("\n");
        sb.append("notPouredWaterVolume").append(SEPARATOR).append(notPouredWaterVolume).append("\n");

        sb.append("toScoopAttempts").append(SEPARATOR).append(toScoopAttempts).append("\n");
        sb.append("scoopFailPercent").append(SEPARATOR).append(toScoopAttempts != 0 ? scoopFail * 100 / toScoopAttempts : "0").append("\n");
        sb.append("scoopedWaterVolume").append(SEPARATOR).append(scoopedWaterVolume).append("\n");
        sb.append("notScoopedWaterVolume").append(SEPARATOR).append(notScoopedWaterVolume).append("\n");

        sb.append("startVolume").append(SEPARATOR).append(startV).append("\n");
        sb.append("endVolume").append(SEPARATOR).append(endV).append("\n");
        return sb.toString();
    }

    private boolean isInNeededPeriod(Date startDate, boolean inNeededPeriod, Date lineDate) {
        if (!inNeededPeriod && lineDate.after(startDate)) {
            startV = currentV;
            inNeededPeriod = true;
        }
        return inNeededPeriod;
    }

    private Date parseDate(String line) throws ParseException {
        SimpleDateFormat formatter2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        String[] splitDate = line.split(" – ");
        return formatter2.parse(splitDate[0]);
    }

    private int parseVolume(String line) {
        String[] splitV1 = line.split("l \\(");
        String[] splitV2 = splitV1[0].split(" ");
        return Integer.parseInt(splitV2[splitV2.length - 1]);
    }

    private void setCurrentVolumeAndStatus(String line, int liters) {
        if (line.contains(TOP_UP)) {
            if (line.contains(SUCCESS)) {
                currentV += liters;
                lineStatus = Status.TOP_UP_SUCCESS;
            } else {
                lineStatus = Status.TOP_UP_FAIL;
            }
        } else {
            if (line.contains(SUCCESS)) {
                currentV -= liters;
                lineStatus = Status.SCOOP_SUCCESS;
            } else {
                lineStatus = Status.SCOOP_FAIL;
            }
        }
    }

    private enum Status {
        TOP_UP_SUCCESS, TOP_UP_FAIL, SCOOP_SUCCESS, SCOOP_FAIL
    }
}