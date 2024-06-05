import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.MINUTES;

public class Main {
    public static void main(String[] args) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        InputStream in = Main.class.getResourceAsStream("tickets.json");
        Order order = mapper.readValue(in, Order.class);
        in.close();
        List<Ticket> tickets = order.getTickets().stream()
                .filter(t -> t.getOrigin().equals("VVO") && t.getDestination().equals("TLV")
                        || t.getOrigin().equals("TLV") && t.getDestination().equals("VVO"))
                .collect(Collectors.toList());

        String carrier;
        OffsetDateTime arrival = null;
        OffsetDateTime departure = null;
        long flightDuration;
        int averagePrice = 0;
        List<Integer> prices = new ArrayList<>();
        HashMap<String, Long> carrierToDuration = new HashMap<>();

        for (Ticket t : tickets) {
            carrier = t.getCarrier();
            switch (t.getOrigin()) {
                case "VVO":
                    departure = OffsetDateTime.of(t.getDepartureDate(),
                            t.getDepartureTime(), (ZoneOffset.ofHours(10)));
                    arrival = OffsetDateTime.of(t.getArrivalDate(),
                            t.getArrivalTime(), (ZoneOffset.ofHours(3)));
                    break;
                case "TLV":
                    departure = OffsetDateTime.of(t.getDepartureDate(),
                            t.getDepartureTime(), (ZoneOffset.ofHours(3)));
                    arrival = OffsetDateTime.of(t.getArrivalDate(),
                            t.getArrivalTime(), (ZoneOffset.ofHours(10)));
                    break;
            }

            flightDuration = MINUTES.between(departure, arrival);
            carrierToDuration.put(carrier,
                    Math.min(carrierToDuration.getOrDefault(carrier, flightDuration), flightDuration));
            averagePrice += t.getPrice();
            prices.add(t.getPrice());
        }

        for (String key : carrierToDuration.keySet()) {
            if (carrierToDuration.get(key) % 60 == 0) {
                System.out.println("Перевозчик: " + key + ", самая короткая длительность полета: "
                        + carrierToDuration.get(key) / 60 + " часов");
            } else {
                System.out.println("Перевозчик: " + key + ", самая короткая длительность полета: "
                        + carrierToDuration.get(key) / 60 + " часов "
                        + carrierToDuration.get(key) % 60 + " минут(ы)");
            }
        }

        averagePrice = averagePrice / tickets.size();
        prices.sort(Comparator.comparing(Integer::intValue));
        int medianPrice = prices.size() % 2 == 0
                ? (prices.get((prices.size() - 1) / 2) + prices.get(prices.size() / 2)) / 2
                : prices.get(prices.size() / 2);

        System.out.println("Средняя цена: " + averagePrice);
        System.out.println("Медианная цена: " + medianPrice);
        System.out.println("Разница между средней и медианной ценой: "
                + Math.abs(averagePrice - medianPrice));
    }
}
