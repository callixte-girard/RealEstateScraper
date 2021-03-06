package parseCity;

import myJavaClasses.Misc;
import org.jsoup.nodes.Document;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static myJavaClasses.Misc.clean;


public class City implements Serializable {

    public static final String label_name = "Nom de la ville";
    public static final String label_dptNumber = "Département";


//    private Document doc;
    private String url, name, postalCode;
    private Prices pricesBuyAppt, pricesBuyHouse, pricesRentAppt, pricesRentHouse;
//    private String pricesBuyAppt, pricesBuyHouse, pricesRentAppt, pricesRentHouse;

    private LinkedHashMap<String, String> trends, allLocalInfos, localPop, localHomes, localRevenueEmpl;

    // construc
    public City(
            Document doc, // is now useless (but flemme de changer l'instantiation)
            String url,
            String name,
            String postalCode,
            Prices pricesBuyAppt,
            Prices pricesBuyHouse,
            Prices pricesRentAppt,
            Prices pricesRentHouse,
//            String pricesBuyAppt,
//            String pricesBuyHouse,
//            String pricesRentAppt,
//            String pricesRentHouse,
            LinkedHashMap<String, String> trends,
            LinkedHashMap<String, String> allLocalInfos,
            LinkedHashMap<String, String> localPop,
            LinkedHashMap<String, String> localHomes,
            LinkedHashMap<String, String> localRevenueEmpl
    ) {
//        this.doc = doc;
        this.url = url;
        this.name = name;
        this.postalCode = postalCode;
        this.pricesBuyAppt = pricesBuyAppt;
        this.pricesBuyHouse = pricesBuyHouse;
        this.pricesRentAppt = pricesRentAppt;
        this.pricesRentHouse = pricesRentHouse;
        this.trends = trends;
        this.allLocalInfos = allLocalInfos;
        this.localPop = localPop;
        this.localHomes = localHomes;
        this.localRevenueEmpl = localRevenueEmpl;
    }

    @Override
    public String toString() {
        return "City: " +
//                "doc=" + doc +
//                "   url='" + url + '\'' +
                "   postalCode='" + postalCode + '\'' +
                "   name='" + name + '\'' + "\n" +
                "   pricesBuyAppt=" + pricesBuyAppt + "\n" +
                "   pricesBuyHouse=" + pricesBuyHouse + "\n" +
                "   pricesRentAppt=" + pricesRentAppt + "\n" +
//                "   pricesRentHouse=" + pricesRentHouse + "\n" +
                "   trends=" + trends + "\n" +
//                 "  allLocalInfos=" + allLocalInfos +
                "   localPop=" + localPop + "\n" +
                "   localHomes=" + localHomes + "\n" +
                "   localRevenueEmpl=" + localRevenueEmpl;
    }

    public static boolean exists(String url, List<City> cities) {
//        if (cities != null) {
            for (City city : cities) {
                String url_city = city.getUrl();
                if (url_city.equals(url)) return true;
            }
            return false;
//        } else {
//            return false;
//        }
    }

    public String getUrl() {
        return url;
    }
    public String getName() {
        return name;
    }
    public Prices getPricesBuyAppt() {
        return pricesBuyAppt;
    }
    public Prices getPricesBuyHouse() {
        return pricesBuyHouse;
    }
    public Prices getPricesRentAppt() {
        return pricesRentAppt;
    }
    public Prices getPricesRentHouse() {
        return pricesRentHouse;
    }
    public LinkedHashMap<String, String> getLocalPop() {
        return localPop;
    }
    public LinkedHashMap<String, String> getLocalHomes() {
        return localHomes;
    }
    public LinkedHashMap<String, String> getLocalRevenueEmpl() {
        return localRevenueEmpl;
    }

    public String getPostalCodeAsDptNumber() {
        return this.postalCode.substring(0, 2);
    }

    public String getTrends(int years) {
        String yearsString = years + " an";
        if (years > 1) yearsString += "s";
        String trends = this.trends.get(yearsString);
        return trends;
    }

    public String getTrends(int years, int months) {
        String monthsString = months + " mois";
        String trends = this.trends.get(monthsString);
        return trends;
    }

    public String getMeanPrice(boolean rent, boolean appt)
    {
        if (rent && appt) return clean(getPricesRentAppt());
        else if (! rent && appt) return clean(getPricesBuyAppt());
        else if (! rent) return clean(getPricesBuyHouse());
//        else return clean(getPricesRentHouse());
        else return Misc.NO_DATA;
    }
}
