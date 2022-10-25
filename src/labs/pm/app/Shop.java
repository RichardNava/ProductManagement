/*
 * Copyright (C) 2022 richa
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package labs.pm.app;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import labs.pm.data.Drink;
import labs.pm.data.Food;
import labs.pm.data.Product;
import labs.pm.data.ProductManager;
import labs.pm.data.ProductManagerException;
import labs.pm.data.Rating;

/**
 * {@code Shop} class represents an application that manages Products
 *
 * @version 4.0
 * @author richa
 */
public class Shop {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        ProductManager pm = ProductManager.getInstance();
        AtomicInteger clientCount = new AtomicInteger(0);
        Callable<String> client = () -> {
            String clientId = "Client " + clientCount.incrementAndGet();
            String threadName = Thread.currentThread().getName();
            int productId = ThreadLocalRandom.current().nextInt(9) + 101;
            String languageTag = ProductManager.getSupportedLocales()
                    .stream()
                    .skip(ThreadLocalRandom.current().nextInt(4))
                    .findFirst()
                    .get();
            StringBuilder log = new StringBuilder();
            log.append(clientId + " " + threadName + "\n-\tstart of log\t-\n");
            log.append("\n-\tend of log\t-\n");
            log.append(pm.getDiscounts(languageTag)
                    .entrySet()
                    .stream()
                    .map(entry -> entry.getKey() + "\t" + entry.getValue())
                    .collect(Collectors.joining("\n")));
            Product product = pm.reviewProduct(productId, Rating.THREE_STAR, "Yet another review");
            log.append((product != null)
                    ? "\nProduct " + productId + " reviewed\n"
                    : "\nProduct " + productId + " not reviewed\n");
            pm.printProductReport(productId, languageTag, clientId);
            log.append(clientId + " generated report for " + productId + " product");
            return log.toString();
        };
        List<Callable<String>> clients = Stream.generate(() -> client)
                .limit(5)
                .collect(Collectors.toList());
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        try {
            List<Future<String>> results = executorService.invokeAll(clients);
            executorService.shutdown();
            results.stream().forEach(result -> {
                try {
                    System.out.println(result.get());
                } catch (InterruptedException | ExecutionException ex) {
                    Logger.getLogger(Shop.class.getName()).log(Level.SEVERE, "Error retrieving client log", ex);
                }
            });
        } catch (InterruptedException ex) {
            Logger.getLogger(Shop.class.getName()).log(Level.SEVERE, "Error invoking clients ", ex);
        }
      
//        ProductManager pm = new ProductManager("es-ES");
//        pm.products.forEach((a,b)-> System.out.print(a+" "+b));
//        pm.restoreData();
//        pm.printProductReport(101);
//        pm.printProductReport(103);
//        pm.createProduct(164, "Kombucha", BigDecimal.valueOf(1.99), Rating.NOT_RATED);
//        pm.reviewProduct(164, Rating.TWO_STAR, "looks like tea but is it?");
//        pm.reviewProduct(164, Rating.FOUR_STAR, "Fine tea");
//        pm.reviewProduct(164, Rating.FOUR_STAR, "This is not tea");
//        pm.reviewProduct(164, Rating.FIVE_STAR, "Perfect!");
//pm.dumpData();
//pm.restoreData();
//        pm.printProductReport(164);
//        pm.printProducts(p -> p.getPrice().floatValue() < 2,
//                (pr1, pr2) -> pr2.getRating().ordinal() - pr1.getRating().ordinal());
//        pm.getDiscounts().forEach((rating, discount) -> System.out.println(rating + "\t" + discount));
// Creamos Producto 1
//Product p1 = pm.createProduct(101, "Tea", BigDecimal.valueOf(1.99), Rating.NOT_RATED);
//        pm.parseProduct("D,101,Tea,1.99,0,0");
//        pm.printProductReport(101);
//       pm.parseReview("101,4,Nice hot cup of tea");
//        pm.parseReview("101,2,Rather weak tea");
//        pm.parseReview("101,4,Fine tea");
//        pm.parseReview("101,4,Good tea");
//        pm.parseReview("101,5,Perfect tea");
//        pm.parseReview("101,3,Just add some lemon");
//        p1.setId(101);
//        p1.setName("Tea");
//        p1.setPrice(BigDecimal.valueOf(1.99));
//        pm.printProductReport(101);
//        p1 = pm.reviewProduct(101, Rating.FOUR_STAR, "Nice hot cup tea");
//        p1 = pm.reviewProduct(101, Rating.TWO_STAR, "Rather weak tea");
//        p1 = pm.reviewProduct(101, Rating.FOUR_STAR, "Fine tea");
//        p1 = pm.reviewProduct(101, Rating.TWO_STAR, "Good tea");
//        p1 = pm.reviewProduct(101, Rating.FIVE_STAR, "Perfect tea");
//        p1 = pm.reviewProduct(101, Rating.THREE_STAR, "Just add some lemon");

// Creamos producto 2
//        Product p2 = pm.createProduct(102, "Coffe", BigDecimal.valueOf(1.75), Rating.NOT_RATED);
//        p2 = pm.reviewProduct(102, Rating.THREE_STAR, "Coffe was ok");
//        p2 = pm.reviewProduct(102, Rating.ONE_STAR, "Where is the milk?");
//        p2 = pm.reviewProduct(102, Rating.FIVE_STAR, "It´s perfect with ten spoons of sugar!");
//        pm.printProductReport(102);
// Creamos producto 3
//        Product p3 = pm.createProduct(103, "Cake", BigDecimal.valueOf(3.99), Rating.NOT_RATED,
//                LocalDate.now().plusDays(2));
//        pm.parseProduct("F,103,Cake,3.99,0,2022-07-07");
//        p3 = pm.reviewProduct(103, Rating.FIVE_STAR, "Very nice cake");
//        p3 = pm.reviewProduct(103, Rating.FOUR_STAR, "It good, but I´ve expected more chocolate");
//        p3 = pm.reviewProduct(103, Rating.FIVE_STAR, "This cake is perfect!");
// Creamos producto 4
//        Product p4 = pm.createProduct(104, "Cookie", BigDecimal.valueOf(3.99), Rating.THREE_STAR,
//                LocalDate.now());
//        p4 = pm.reviewProduct(p4, Rating.THREE_STAR, "Just another cookie");
//        p4 = pm.reviewProduct(p4, Rating.THREE_STAR, "Ok");
//        pm.printProductReport(p4);
// Creamos producto 5
//        Product p5 = pm.createProduct(105, "Hot Chocolate", BigDecimal.valueOf(2.50), Rating.NOT_RATED);
//        p5 = pm.reviewProduct(p5, Rating.FOUR_STAR, "Tasty!");
//        p5 = pm.reviewProduct(p5, Rating.FOUR_STAR, "No bad at all");
//        pm.printProductReport(p5);
// Creamos producto 6
//        Product p6 = pm.createProduct(106, "Chocolate", BigDecimal.valueOf(2.99), Rating.NOT_RATED,
//                LocalDate.now().plusDays(3));
//        p6 = pm.reviewProduct(p6, Rating.TWO_STAR, "Too sweet!");
//        p6 = pm.reviewProduct(p6, Rating.THREE_STAR, "Beeter then cookie");
//        p6 = pm.reviewProduct(p6, Rating.TWO_STAR, "Too bitter");
//        p6 = pm.reviewProduct(p6, Rating.ONE_STAR, "I don´t get it!");
//        pm.printProductReport(p6);
//        Comparator<Product> ratingSorter = (pr1, pr2) -> pr2.getRating().ordinal() - pr1.getRating().ordinal();
//        Comparator<Product> priceSorter = (pr1, pr2) -> pr2.getPrice().compareTo(pr1.getPrice());
//pm.printProducts(ratingSorter);
//pm.printProducts(priceSorter);
//        pm.printProducts(ratingSorter.thenComparing(priceSorter));
//        pm.printProducts(ratingSorter.thenComparing(priceSorter).reversed());
//pm.printProductReport(46);
//        Product p7 = pm.createProduct(104,"Chocolate",BigDecimal.valueOf(2.99),Rating.FIVE_STAR,LocalDate.now().plusDays(2));
//        Product p8 = p4.applyRating(Rating.FIVE_STAR);
//        Product p9 = p1.applyRating(Rating.TWO_STAR);
//        System.out.println(p1.getId()+" "+p1.getName()
//                +" "+p1.getPrice()+" "+p1.getDiscount()
//                +" "+p1.getRating().getStars());
//        System.out.println(p1);
//        System.out.println(p2);
//        System.out.println(p3);
//        System.out.println(p4);
//        System.out.println(p5);
//        System.out.println(p6.equals(p7));
//        System.out.println(p8);
//        System.out.println(p9);
//        System.out.println(p1.getBestBefore());
//        if(p3 instanceof Food){
//            System.out.println(p3.getBestBefore());
//        }
    }

}
