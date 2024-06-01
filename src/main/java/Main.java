import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
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
        List<Ticket> tickets = order.getTickets().stream()
                .filter(t -> t.getOrigin().equals("VVO") && t.getDestination().equals("TLV"))
                .collect(Collectors.toList());

        String carrier;
        LocalDateTime arrival;
        LocalDateTime departure;
        long flightDuration;
        int averagePrice = 0;
        List<Integer> prices = new ArrayList<>();
        HashMap<String, Long> carrierToDuration = new HashMap<>();

        for (Ticket t : tickets) {
            carrier = t.getCarrier();
            arrival = LocalDateTime.of(t.getArrival_date(), t.getArrival_time());
            departure = LocalDateTime.of(t.getDeparture_date(), t.getDeparture_time());
            flightDuration = MINUTES.between(departure, arrival);

            carrierToDuration.put(carrier,
                    Math.min(carrierToDuration.getOrDefault(carrier, flightDuration), flightDuration));

            averagePrice += t.getPrice();
            prices.add(t.getPrice());
        }

        averagePrice = averagePrice / tickets.size();
        prices.sort(Comparator.comparing(Integer::intValue));

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

        int medianPriceIndex;
        medianPriceIndex = prices.size() % 2 == 0 ? (prices.size() - 1) / 2 : prices.size() / 2;

        System.out.println("Средняя цена: " + averagePrice);
        System.out.println("Медианная цена: " + prices.get(medianPriceIndex));
        System.out.println("Разница между средней и медианной ценой: "
                + (averagePrice - prices.get(medianPriceIndex)));
    }
}
