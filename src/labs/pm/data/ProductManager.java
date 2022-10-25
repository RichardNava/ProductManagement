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
package labs.pm.data;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 *
 * @author richa
 */
public class ProductManager {

//    private Product product; // current Product
//    //private Review review;
//    private Review[] reviews = new Review[5];
    private Map<Product, List<Review>> products = new ConcurrentHashMap<>();

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock writeLock = lock.writeLock();
    private final Lock readLock = lock.readLock();

//    private Locale locale;
//    private ResourceBundle resources;
//    private DateTimeFormatter dateFormat;
//    private NumberFormat moneyFormat;
    private final static Map<String, ResourceFormatter> formatters
            = Map.of("en-GB", new ResourceFormatter(Locale.UK),
                    "en-US", new ResourceFormatter(Locale.US),
                    "fr-FR", new ResourceFormatter(Locale.FRANCE),
                    "es-ES", new ResourceFormatter(new Locale("es", "ES")),
                    "zh-CN", new ResourceFormatter(Locale.CHINA));
    // private ResourceFormatter formatter;
    private static final Logger logger = Logger.getLogger(ProductManager.class.getName());

    private final ResourceBundle config = ResourceBundle.getBundle("labs.pm.data.config");
    private final MessageFormat reviewFormat = new MessageFormat(config.getString("review.data.format"));
    private final MessageFormat productFormat = new MessageFormat(config.getString("product.data.format"));
    private final Path reportsFolder = Path.of(config.getString("reports.folder"));
    private final Path dataFolder = Path.of(config.getString("data.folder"));
    private final Path tempFolder = Path.of(config.getString("temp.folder"));

    private static final ProductManager pm = new ProductManager();

//    public ProductManager(Locale locale) {
////        this.locale = locale;
////
////        resources = ResourceBundle.getBundle("labs.pm.data.resources", locale);
////        dateFormat = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).localizedBy(locale);
////        moneyFormat = NumberFormat.getCurrencyInstance(locale);
//        this(locale.toLanguageTag());
//        
//    }
    private ProductManager() {
//        changeLocale(languageTag);
        loadAllData();

    }

//    public void changeLocale(String languageTag) {
//        formatter = formatters.getOrDefault(languageTag, formatters.get("es-ES"));
//    }
    public static ProductManager getInstance() {
        return pm;
    }

    public static Set<String> getSupportedLocales() {
        return formatters.keySet();
    }

    public Product createProduct(int id, String name, BigDecimal price, Rating rating, LocalDate bestBefore) {
        Product product = null;
        try {
            writeLock.lock();
            product = new Food(id, name, price, rating, bestBefore);
            products.putIfAbsent(product, new ArrayList<>());
        } catch (Exception ex) {
            logger.log(Level.INFO, "Error adding product" + ex.getMessage());
        } finally {
            writeLock.unlock();
        }
        return product;
    }

    public Product createProduct(int id, String name, BigDecimal price, Rating rating) {
        Product product = null;
        try {
            writeLock.lock();
            product = new Drink(id, name, price, rating);
            products.putIfAbsent(product, new ArrayList<>());
        } catch (Exception ex) {
            logger.log(Level.INFO, "Error adding product" + ex.getMessage());
        } finally {
            writeLock.unlock();
        }
        return product;
    }

    private Product reviewProduct(Product product, Rating rating, String comments) {
//        if (reviews[reviews.length - 1] != null) {
//            reviews = Arrays.copyOf(reviews, reviews.length + 5);
//        }
        List<Review> reviews = products.get(product);
        products.remove(product, reviews);
        reviews.add(new Review(rating, comments));
//        int sum = 0;
////        int i = 0;
//        for (Review review : reviews) {
//            sum += review.getRating().ordinal();
//        }
////        boolean reviewed = false; // Leccion 7: implementamos lógica mediante array y loops
////        while (i < reviews.length && !reviewed) {
////            if (reviews[i] == null) {
////                reviews[i] = new Review(rating, comments);
////                reviewed = true;
////            }
////            sum += reviews[i].getRating().ordinal();
////            i++;
////        }
//        //review = new Review(rating, comments);
//        product = product.applyRating(Rateable.convert(Math.round((float) sum / reviews.size())));
        product = product.applyRating(
                Rateable.convert(
                        (int) Math.round(
                                reviews.stream()
                                        .mapToInt(r -> r.getRating().ordinal())
                                        .average()
                                        .orElse(0))));
        //System.out.println(product);
        products.put(product, reviews);
        return product;
    }

    public Product reviewProduct(int id, Rating rating, String comments) {
        try {
            writeLock.lock();
            return reviewProduct(findProduct(id), rating, comments);
        } catch (ProductManagerException ex) {
            logger.log(Level.SEVERE, ex.getMessage());
        } finally {
            writeLock.unlock();
        }
        return null;
    }

    public Product findProduct(int id) throws ProductManagerException {
//        Product result = null;
//        for (Product product : products.keySet()) {
//            if (product.getId() == id) {
//                result = product;
//                break;
//            }
//        }
//        return result;
        try {
            readLock.lock();
            return products.keySet()
                    .stream()
                    .filter(p -> p.getId() == id)
                    .findFirst()
                    .orElseThrow(() -> new ProductManagerException("Product with id " + id + " not found"));
            //.get();
            //.orElseGet(()->null);
        } finally {
            readLock.unlock();
        }

    }

    private void printProductReport(Product product, String languageTag, String client) throws IOException {
        ResourceFormatter formatter = formatters.getOrDefault(languageTag, formatters.get("es-ES"));
        List<Review> reviews = products.get(product);
        Collections.sort(reviews);
        StringBuilder txt = new StringBuilder();
//        txt.append(MessageFormat.format(resources.getString("product"),
//                product.getName(),
//                moneyFormat.format(product.getPrice()),
//                product.getRating().getStars(),
//                dateFormat.format(product.getBestBefore())));
        txt.append(formatter.formatProduct(product));
        txt.append('\n');
//        for (Review review : reviews) {
////            if (review == null) {
////                break;
////            }
////            txt.append(MessageFormat.format(resources.getString("review"),
////                    review.getRating().getStars(),
////                    review.getComments()));
//            txt.append(formatter.formatReview(review));
//            txt.append('\n');
//        }
//        if (reviews[0] == null) {
//            txt.append(resources.getString("no.reviews"));
//            txt.append('\n');
//        }
//        if (reviews.isEmpty()) {
////            txt.append(resources.getString("no.reviews"));
//            txt.append(formatter.getText("no.reviews"));
//            txt.append('\n');
//        }
//        if (review != null) {
//            txt.append(MessageFormat.format(resources.getString("review"),
//                    review.getRating().getStars(),
//                    review.getComments()));
//        } else {
//            txt.append(resources.getString("no.reviews"));
//        }

        Path productFile = reportsFolder.resolve(MessageFormat.format(
                config.getString("report.file"),
                product.getId(),
                client));
        try (OutputStreamWriter osw = new OutputStreamWriter(
                Files.newOutputStream(productFile, StandardOpenOption.CREATE),
                "UTF-8");
                PrintWriter out = new PrintWriter(osw)) {

            out.append(formatter.formatProduct(product) + System.lineSeparator());
            if (reviews.isEmpty()) {
                out.append(formatter.getText("no.reviews") + System.lineSeparator());
                txt.append(formatter.getText("no.reviews") + '\n');
            } else {
                var reviewsF = reviews.stream()
                        .map(r -> formatter.formatReview(r) + '\n')
                        .collect(Collectors.joining());
                out.append(reviewsF);
                txt.append(reviewsF);
//            reviews.stream()
//                    .forEach(r -> txt.append(formatter.formatReview(r)+'\n'));
            }
        }
        System.out.println(txt);
    }

    public void printProductReport(int id, String languageTag, String client) {
        try {
            writeLock.lock();
            printProductReport(findProduct(id), languageTag, client);
        } catch (ProductManagerException ex) {
            logger.log(Level.INFO, ex.getMessage());
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Error printing product report " + ex.getMessage(), ex);
        } finally {
            writeLock.unlock();
        }
    }

    public void printProducts(Predicate<Product> filter, Comparator<Product> sorter, String languageTag) {
        try {
            readLock.lock();
            ResourceFormatter formatter = formatters.getOrDefault(languageTag, formatters.get("es-ES"));
//        List<Product> productList = new ArrayList<>(products.keySet());
//        productList.sort(sorter);
            StringBuilder txt = new StringBuilder();
//        for (Product product : productList) {
//            txt.append(formatter.formatProduct(product));
//            txt.append('\n');
////            printProductReport(product);
//        }
            products.keySet()
                    .stream()
                    .sorted(sorter)
                    .filter(filter)
                    //.forEach(p ->  printProductReport(p));
                    .forEach(p -> txt.append(formatter.formatProduct(p) + '\n'));
            System.out.println(txt);
        } finally {
            readLock.unlock();
        }
    }

    private Review parseReview(String text) {
        Review review = null;
        try {
            Object[] values = reviewFormat.parse(text);
//            reviewProduct(Integer.parseInt((String) values[0]),
//                    Rateable.convert(Integer.parseInt((String) values[1])),
//                    (String) values[2]);
            review = new Review(Rateable.convert(Integer.parseInt((String) values[0])),
                    (String) values[1]);
        } catch (ParseException | NumberFormatException ex) {
            logger.log(Level.WARNING, "Error parsing review " + text, ex.getMessage());
            //throw new ProductManagerException("Unable to parse review", ex);
        }
        return review;
    }

    private Product loadProduct(Path file) {
        Product product = null;

        try {
            product = parseProduct(Files.lines(dataFolder.resolve(file), Charset.forName("UTF-8"))
                    .findFirst().orElseThrow());
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error loading product " + ex.getMessage());
        }

        return product;
    }

    private List<Review> loadReviews(Product product) {
        List<Review> reviews = null;

        Path file = reportsFolder.resolve(MessageFormat.format(config.getString("reviews.data.file"),
                product.getId()));
        if (Files.notExists(file)) {
            reviews = new ArrayList<>();
        } else {
            try {
                reviews = Files.lines(file, Charset.forName("UTF-8"))
                        .map(text -> parseReview(text))
                        .filter(review -> review != null)
                        .collect(Collectors.toList());
            } catch (IOException ex) {
                logger.log(Level.SEVERE, "Error loading reviews " + ex.getMessage());
            }
        }
        return reviews;
    }

    private Product parseProduct(String text) {
        Product product = null;
        try {
            Object[] values = productFormat.parse(text);
            int id = Integer.parseInt((String) values[1]);
            String name = (String) values[2];
            BigDecimal price = BigDecimal.valueOf(Double.parseDouble((String) values[3]));
            Rating rating = Rateable.convert(Integer.parseInt((String) values[4]));
            switch ((String) values[0]) {
                case "D":
                    //createProduct(id, name, price, rating);
                    product = new Drink(id, name, price, rating);
                    break;
                case "F":
                    LocalDate bestBefore = LocalDate.parse((CharSequence) values[5]);
                    //createProduct(id, name, price, rating,bestBefore);
                    product = new Food(id, name, price, rating, bestBefore);
                    break;
            }

        } catch (ParseException | NumberFormatException | DateTimeParseException ex) {
            Logger.getLogger(ProductManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return product;
    }

    public void dumpData() {
        try {
            if (Files.notExists(tempFolder)) {
                Files.createDirectory(tempFolder);
            }
            Path tempFile = tempFolder.resolve(MessageFormat.format(config.getString("temp.file"), Instant.now().getNano()));
            try (ObjectOutputStream out = new ObjectOutputStream(
                    Files.newOutputStream(tempFile, StandardOpenOption.CREATE))) {
                out.writeObject(products);
                products = new ConcurrentHashMap<>();
            }
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Error dumping data " + ex.getMessage(), ex);
        }
    }

    @SuppressWarnings("unchecked")
    public void restoreData() {
        try {
            Path tempFile = Files.list(tempFolder)
                    .filter(path -> path.getFileName().toString().endsWith("tmp"))
                    .findFirst().orElseThrow();
            try (ObjectInputStream in = new ObjectInputStream(
                    Files.newInputStream(tempFile, StandardOpenOption.DELETE_ON_CLOSE))) {
                products = (ConcurrentHashMap) in.readObject();
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error restoring data " + ex.getMessage(), ex);
        }
    }

    private void loadAllData() {
        try {
            products = Files.list(dataFolder)
                    .filter(file -> file.getFileName().toString().startsWith("product"))
                    .map(file -> loadProduct(file))
                    .filter(product -> product != null)
                    .map(product -> product.applyRating(
                    Rateable.convert(
                            (int) Math.round(
                                    loadReviews(product).stream()
                                            .mapToInt(r -> r.getRating().ordinal())
                                            .average()
                                            .orElse(0)))))
                    .collect(Collectors.toMap(product -> product,
                            product -> loadReviews(product)));
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Error loading data " + ex.getMessage(), ex);
        }
    }

    public Map<String, String> getDiscounts(String languageTag) {
        try {
            readLock.lock();
            ResourceFormatter formatter = formatters.getOrDefault(languageTag, formatters.get("es-ES"));
            return products.keySet()
                    .stream()
                    .collect(Collectors.groupingBy(
                            product -> product.getRating().getStars(),
                            Collectors.collectingAndThen(
                                    Collectors.summingDouble(
                                            product -> product.getDiscount().doubleValue()),
                                    discount -> formatter.moneyFormat.format(discount))));
        } finally {
            readLock.unlock();
        }
    }

    private static class ResourceFormatter {

        private Locale locale;
        private ResourceBundle resources;
        private DateTimeFormatter dateFormat;
        private NumberFormat moneyFormat;

        private ResourceFormatter(Locale locale) {
            this.locale = locale;
            resources = ResourceBundle.getBundle("labs.pm.data.resources", locale);
            dateFormat = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).localizedBy(locale);
            moneyFormat = NumberFormat.getCurrencyInstance(locale);

        }

        private String formatProduct(Product product) {
            return MessageFormat.format(resources.getString("product"),
                    product.getName(),
                    moneyFormat.format(product.getPrice()),
                    product.getRating().getStars(),
                    dateFormat.format(product.getBestBefore()));
        }

        private String formatReview(Review review) {
            return MessageFormat.format(resources.getString("review"),
                    review.getRating().getStars(),
                    review.getComments());
        }

        private String getText(String key) {
            return resources.getString(key);
        }
    }

}
