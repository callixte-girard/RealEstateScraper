package parseCity;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.LinkedHashMap;

public class ExtractLocalInfos {
//    private final static boolean preprocessData = true;

    // might change on the website...
    // !!! IMPORTANT TO KEEP THIS VALUE UPDATED
    // !!! MUST TAKE FORMAT : "(YYYY-YYYY)"
    private final static String datesInterval = "(2006-2011)";
    // might probably not change on the website, but always check when big update.
    // ----- LABELS -----
    // 1) population
    public final static String label_population = "Population";
    public final static String label_rateDemographicGrowth = "Croissance démographique" + " " + datesInterval;
    public final static String label_medianAge = "Age médian";
    public final static String label_lt25yo = "Part des moins de 25 ans";
    public final static String label_mt25yo = "Part des plus de 25 ans";
    public final static String label_popDensity = "Densité de la population (nombre d'habitants au km²)";
    public final static String label_area = "Superficie (en km²)";
    // 2) homes
    public final static String label_totalNbHomes = "Nombre total de logements";
    public final static String label_rateMainHomes = "Part des résidences principales";
    public final static String label_rateSecondaryHomes = "Part des résidences secondaires";
    public final static String label_rateVacantHomes = "Part des logements vacants";
    public final static String label_rateSocialHomes = "Part des logements sociaux / HLM";
    public final static String label_rateMainHomeOwner = "Part des ménages propriétaires de leur résidence principale";
    public final static String label_rateMainHomeTenant = "Part des ménages locataires de leur résidence principale";
    public final static String label_rateMainHome1Room = "Part des résidences principales 1 pièce";
    public final static String label_rateMainHome2Rooms = "Part des résidences principales 2 pièces";
    public final static String label_rateMainHome3Rooms = "Part des résidences principales 3 pièces";
    public final static String label_rateMainHome4Rooms = "Part des résidences principales 4 pièces";
    public final static String label_rateMainHome5RoomsMore = "Part des résidences principales 5 pièces ou plus";
    // 3) revenue & employment
    public final static String label_medianAnnualRevenue = "Revenu annuel médian par ménage";
    public final static String label_employmentRate15To64 = "Taux d'activité des 15 à 64 ans";
    public final static String label_employmentRateEvol = "Evolution du taux d'activité" + " " + datesInterval;
    public final static String label_unemploymentRate15To64 = "Taux de chômage des 15 à 64 ans";
    public final static String label_unemploymentRateEvol = "Evolution du taux de chômage" + " " + datesInterval;


    public static LinkedHashMap<String, String> all(Document doc)
    {
        Element mainLocalContainer = doc
                .getElementById("local_stats");
//        Disp.anyTypeThenLine(mainLocalContainer);

        Elements localContainerSections = mainLocalContainer
                .getElementsByClass("accordion-panel container--row accordion-panel--price");
//        Disp.anyTypeThenLine(localContainerSections);

        LinkedHashMap<String, String> localInfos = new LinkedHashMap<>();
        for (Element el : localContainerSections) {
//            Disp.anyTypeThenLine(el);

            Element section = el.getElementsByTag("tbody").first();
//            Disp.anyTypeThenLine(section);

            Elements pairs = section.getElementsByTag("tr");

            // @TO-DO finish ;)
            for (Element info : pairs) {
//                Disp.anyTypeThenLine(info);

                Elements infoCells = info.getElementsByTag("td");
//                Disp.anyTypeThenLine(infoCells);

                String infoName_raw = infoCells.get(0).text().trim();
                String infoValue_raw = infoCells.get(1).text().trim();
//                Disp.anyTypeThenLine(infoName_raw + " | " + infoValue_raw);
//
                infoValue_raw = infoValue_raw.replaceAll("," , "."); // quick fix for , and .
                localInfos.put(infoName_raw, infoValue_raw);
            }
        }
        return localInfos;
    }


    private static LinkedHashMap<String, String> extractSelectedLabelsFromLocalInfos(
            LinkedHashMap<String, String> allLocalInfos,
            String[] labels
    ) {
        LinkedHashMap<String, String> infos = new LinkedHashMap<>();
        for (int i=0 ; i<labels.length ; i++) {
            String label = labels[i];
            String value = allLocalInfos.get(label);

            // pre-process data TODO: complete
            boolean preprocessData = false;
            if (preprocessData) {
                switch (label) {
                    // 1) population
                    case label_population:
                        value = value.replace(" habitants", "");
                        break;
                    case label_medianAge:
                        value = value.replace(" ans", "");
                        break;
                    case label_employmentRateEvol:
                    case label_unemploymentRateEvol:
                        value = value.replace(" pt.", "");
                        break;
                    case label_popDensity:
                        value = value.replace(" hab. / km²", "");
                        break;
                    // 2) homes
                    case label_totalNbHomes:
                        value = value.replace(" logements²", "");
                        break;
                    // 3) revenue & employment
                    case label_medianAnnualRevenue:
                        value = value.replace(" €", "");
                        break;
                }
                value = value.replaceAll(",", "."); // quick fix for , and .
            }

            infos.put(label, value);
        }
        return infos;
    }


    protected static LinkedHashMap<String, String> localPop(LinkedHashMap<String, String> allLocalInfos)
    {
        String[] labels_localPop = {
                label_population,
                label_rateDemographicGrowth,
                label_medianAge,
                label_lt25yo,
                label_mt25yo,
                label_popDensity,
                label_area,
        };
        LinkedHashMap<String, String> localPop = extractSelectedLabelsFromLocalInfos(allLocalInfos, labels_localPop);
        return localPop;
    }

    protected static LinkedHashMap<String, String> localHomes(LinkedHashMap<String, String> allLocalInfos)
    {
        String[] labels_localHomes = {
                label_totalNbHomes,
                label_rateMainHomes,
                label_rateSecondaryHomes,
                label_rateVacantHomes,
                label_rateSocialHomes,
                label_rateMainHomeOwner,
                label_rateMainHomeTenant,
                label_rateMainHome1Room,
                label_rateMainHome2Rooms,
                label_rateMainHome3Rooms,
                label_rateMainHome4Rooms,
                label_rateMainHome5RoomsMore,
        };
        LinkedHashMap<String, String> localHomes = extractSelectedLabelsFromLocalInfos(allLocalInfos, labels_localHomes);
        return localHomes;
    }

    protected static LinkedHashMap<String, String> localRevenueEmployment(LinkedHashMap<String, String> allLocalInfos)
    {
        String[] labels_localRevenueEmpl = {
                label_medianAnnualRevenue,
                label_employmentRate15To64,
                label_employmentRateEvol,
                label_unemploymentRate15To64,
                label_unemploymentRateEvol,
        };
        LinkedHashMap<String, String> localRevenueEmpl = extractSelectedLabelsFromLocalInfos(allLocalInfos, labels_localRevenueEmpl);
        return localRevenueEmpl;
    }


}
