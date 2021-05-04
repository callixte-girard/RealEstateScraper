package parseCity;

import myJavaClasses.ReadWriteFile;

import java.io.Serializable;

public class Prices implements Serializable {

    public static final String label_pricesMeanBuyFlat = "Prix d'achat moyen appartement";
    public static final String label_pricesMeanRentFlat = "Prix de location moyen appartement";
    public static final String label_pricesMeanBuyHouse = "Prix d'achat moyen maison";

    public static final String label_pricesTrends1y = "Evolution prix immobilier 1 an";
    public static final String label_pricesTrends2y = "Evolution prix immobilier 2 ans";
    public static final String label_pricesTrends5y = "Evolution prix immobilier 5 ans";


    // type : false == buy, true == rent
    private boolean rent;
    // type 2 : false == house, true == flat
    private boolean appt;
    // medium price
    private double mean;
    // upper and lower end of 95% items
    private double highest;
    private double lowest;

/*    @Override
    public String toString() {
        String[] arr = {
                String.valueOf(this.getHighest()) + " €",
                String.valueOf(this.getMean()) + " €",
                String.valueOf(this.getLowest() + " €"),
        };
        String out = "";
        // rent or buy ?
        if (this.isRent()) out += jsonifyArrayIntoString("RENT", arr, true);
        else out += jsonifyArrayIntoString("BUY", arr, true);
        // other attribs
        return out;
    }*/

    @Override
    public String toString() {
        String out = "Prices{";
        if (rent) out += "rent"; else out += "buy";
        if (appt) out += " flat"; else out += " house";
        out +=
                ": lowest=" + lowest +
                ", mean=" + mean +
                ", highest=" + highest +
                '}';
        return out;
    }

    public Prices(boolean rent, boolean appt, double mean, double highest, double lowest) {
        this.rent = rent;
        this.appt = appt;
        this.mean = mean;
        this.highest = highest;
        this.lowest = lowest;
    }

    public double getMean() {
        return this.mean;
    }
    public String getMeanAsAmount() {
        String out;
        try {
            out = String.valueOf(this.getMean()).replace(".0" , "");
        } catch (NullPointerException npe) {
            out = ReadWriteFile.NO_DATA;
        }
        return out;
    }
    public boolean isRent() {
        return this.rent;
    }
    public boolean isAppt() {
        return this.appt;
    }

}
