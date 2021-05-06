package handleVPN;

import myJavaClasses.Disp;
import myJavaClasses.SaveManager;
import myJavaClasses.ShellWrapper;
import parseImmo.Main;

import java.io.Serializable;
import java.sql.Time;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class IP implements Serializable {
    // above which number of failures each IP is considered blocked
    public static final int nb_max_each_ip = 1; // should stay at 1, because an IP is blocked for 24H after one round (25 requests in few seconds)

    public static final String NO_IP = "NO IP";
//    public static final String NO_DATE = "°";

    private String address;
    private LocalDateTime lastTry;

    public IP(String address) {
        this.address = address;
        this.lastTry = LocalDateTime.now();
    }

    @Override
    public String toString() {
        String out = this.getAddress();
        if (this.isBlocked()) {
            String lastTry_raw = this.getLastTry().format(DateTimeFormatter.ofPattern("uuuu/MM/dd HH:mm:ss"));
            out += " *** BLOCKED *** on " + lastTry_raw;
        } else {
            out += " still not blocked :)";
        }
        return out;
    }

    public String getAddress() {
        return address;
    }
    public LocalDateTime getLastTry() {
        return lastTry;
    }
    public void setLastTry(LocalDateTime lastTry) {
        this.lastTry = lastTry;
    }

    public boolean isBlocked() {
        return this.lastTry.plusHours(24).isAfter(LocalDateTime.now());
    }

    ////////////////////////// STATIC //////////////////////////

    public static IP handleChange()
    {
        Disp.anyType(">>> Handling IP change on same region...");
        // 1) increment current ip counter
//        getCurrent().setTimesUsed(getCurrent().getTimesUsed() + 1);
        getCurrent().setLastTry(LocalDateTime.now());
        // 2) on/off : gives a new ip
        IP oldIP = getCurrent();
        ShellWrapper.execute("piactl disconnect");
        ShellWrapper.execute("piactl connect");
        IP newIP = getCurrent();
        // 3) save
        SaveManager.objectSave(Main.filename_vpn_state + Main.extension_save, Region.getRegions());
        // 4) if same, change again
//        if (newIP.getAddress().equals(oldIP.getAddress()))
//            return handleChange();
//        else
            return newIP;
    }

    public static IP getCurrent()
    {
        String s = NO_IP;
        while (s.equals(NO_IP)) {
            s = ShellWrapper.execute("piactl get vpnip").get(0);
        }
//        Disp.anyType(s);
        // now determine if the IP already exists or not
        return createOrGet(s);
    }

    private static IP getFromString(String s)
    {
        for (Region region : Region.getRegions()) {
            for (IP ip : region.getIpAddresses()) {
                if (ip.getAddress().equals(s)) return ip;
            }
        }
        return null;
    }

    private static IP createOrGet(String s)
    {
        IP ip = getFromString(s);
        if (ip == null) { // create it
            ip = new IP(s);
            Region.getCurrent().getIpAddresses().add(ip);
        }
        return ip;
    }

}
