package com.fooddelivery.runner;

import com.fooddelivery.model.*;
import com.fooddelivery.service.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
@Component
public class AppRunner implements CommandLineRunner {

    @Autowired
    private PasswordEncoder encoder;

    private final UserService userService;
    private final RestaurantService restaurantService;
    private final OrderService orderService;
    private final ChatService chatService;

    public AppRunner(UserService userService,
                     RestaurantService restaurantService,
                     OrderService orderService,
                     ChatService chatService) {
        this.userService = userService;
        this.restaurantService = restaurantService;
        this.orderService = orderService;
        this.chatService = chatService;
    }

    @Override
    public void run(String... args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("=== Food Delivery Terminal App ===");

        seedSampleData();

        String currentUser = null;

        while (true) {
            try {
                System.out.println("\nMain menu:");
                System.out.println("1. Register user");
                System.out.println("2. Login user");
                System.out.println("3. Create restaurant");
                System.out.println("4. Add food to restaurant");
                System.out.println("5. View restaurants and menu");
                System.out.println("6. Place order (starts chat session)");
                System.out.println("7. Open chat session");
                System.out.println("8. View my orders (user)");
                System.out.println("9. Exit");
                System.out.println("10. View my restaurants (owner)");
                System.out.println("11. My orders (user) — table split Pending/Delivered");
                System.out.println("12. Orders received (restaurant) — table split Pending/Delivered");
                System.out.print("choice: ");
                String choice = sc.nextLine().trim();

                String finalCurrentUser = currentUser;
                switch (choice) {
                    case "1" -> {
                        System.out.print("username: ");
                        String username = sc.nextLine().trim();
                        System.out.print("password: ");
                        String passwordPlain = sc.nextLine().trim();
                        System.out.print("address: ");
                        String address = sc.nextLine().trim();

                        if (userService.findByUsername(username).isPresent()) {
                            System.out.println("username taken");
                        } else {
                            User u = User.builder()
                                    .username(username)
                                    .password(passwordPlain)
                                    .address(address)
                                    .build();
                            userService.register(u);
                            System.out.println("registered");
                        }
                    }
                    case "2" -> {
                        System.out.print("username: ");
                        String username = sc.nextLine().trim();
                        System.out.print("password: ");
                        String password = sc.nextLine().trim();
                        if (userService.authenticate(username, password)) {
                            currentUser = username;
                            System.out.println("login success as " + username);
                        }else {
                            System.out.println("invalid credentials");
                        }
                    }
                    case "3" -> {
                        if (currentUser == null) {
                            System.out.println("login first");
                            break;
                        }
                        System.out.print("restaurant name: ");
                        String name = sc.nextLine().trim();
                        System.out.print("restaurant address: ");
                        String addr = sc.nextLine().trim();

                        Restaurant r = Restaurant.builder()
                                .name(name)
                                .address(addr)
                                .ownerUsername(currentUser)
                                .build();

                        restaurantService.addRestaurant(r);
                        System.out.println("restaurant created id=" + r.getId() + " (owner=" + currentUser + ")");
                    }
                    case "4" -> {
                        System.out.print("restaurant id: ");
                        Long rid = Long.parseLong(sc.nextLine().trim());
                        Optional<Restaurant> or = restaurantService.findById(rid);
                        if (or.isEmpty()) {
                            System.out.println("restaurant not found");
                            break;
                        }
                        System.out.print("food name: ");
                        String fname = sc.nextLine().trim();
                        System.out.print("price: ");
                        double price = Double.parseDouble(sc.nextLine().trim());
                        System.out.print("delivery minutes (default 1): ");
                        String dt = sc.nextLine().trim();
                        int minutes = dt.isEmpty() ? 1 : Integer.parseInt(dt);
                        FoodItem f = FoodItem.builder().name(fname).price(price).deliveryTimeMinutes(minutes).build();
                        FoodItem saved = restaurantService.addFoodItem(rid, f);
                        System.out.println("added food id=" + saved.getId());
                    }
                    case "5" -> {
                        List<Restaurant> list = restaurantService.listAll();
                        for (Restaurant r : list) {
                            System.out.println("----");
                            System.out.println("id=" + r.getId() + " name=" + r.getName() + " addr=" + r.getAddress());
                            r.getMenu().forEach(fi -> System.out.printf("   foodId=%d name=%s price=%.2f dt=%dmin%n",
                                    fi.getId(), fi.getName(), fi.getPrice(), fi.getDeliveryTimeMinutes()));
                        }
                    }
                    case "6" -> {
                        if (currentUser == null) {
                            System.out.println("login first");
                            break;
                        }
                        System.out.print("restaurant id to order from: ");
                        Long rid = Long.parseLong(sc.nextLine().trim());
                        var or = restaurantService.findById(rid);
                        if (or.isEmpty()) {
                            System.out.println("restaurant not found");
                            break;
                        }
                        Restaurant r = or.get();
                        System.out.println("menu:");
                        r.getMenu().forEach(fi -> System.out.printf("   id=%d %s (%.2f) dt=%dmin%n",
                                fi.getId(), fi.getName(), fi.getPrice(), fi.getDeliveryTimeMinutes()));
                        System.out.print("enter food id: ");
                        Long fid = Long.parseLong(sc.nextLine().trim());
                        FoodItem chosen = r.getMenu().stream().filter(f -> f.getId().equals(fid)).findFirst().orElse(null);
                        if (chosen == null) {
                            System.out.println("food not found");
                            break;
                        }

                        // Ask whether to schedule
                        int deliveryMinutes;
                        System.out.print("Schedule for later? (y/n): ");
                        String scheduleChoice = sc.nextLine().trim().toLowerCase();
                        if ("y".equals(scheduleChoice) || "yes".equals(scheduleChoice)) {
                            System.out.println("Enter delivery time in one of these formats:");
                            System.out.println(" 1) yyyy-MM-dd HH:mm  (e.g. 2025-09-09 12:30)");
                            System.out.println(" 2) HH:mm  (e.g. 12:30) — interpreted as today at that time");
                            System.out.print("delivery time: ");
                            String input = sc.nextLine().trim();
                            LocalDateTime now = LocalDateTime.now();
                            LocalDateTime scheduled;
                            DateTimeFormatter dtfFull = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                            DateTimeFormatter dtfShort = DateTimeFormatter.ofPattern("HH:mm");
                            try {
                                if (input.contains("-")) {
                                    scheduled = LocalDateTime.parse(input, dtfFull);
                                } else {

                                    var localTime = java.time.LocalTime.parse(input, dtfShort);
                                    scheduled = LocalDateTime.of(now.toLocalDate(), localTime);

                                    if (!scheduled.isAfter(now)) scheduled = scheduled.plusDays(1);
                                }
                                deliveryMinutes = (int) ChronoUnit.MINUTES.between(now, scheduled);
                                if (deliveryMinutes <= 0) {
                                    System.out.println("Scheduled time must be in the future.");
                                    break;
                                }
                            } catch (Exception ex) {
                                System.out.println("Invalid time format. Aborting order.");
                                break;
                            }
                        } else {

                            deliveryMinutes = chosen.getDeliveryTimeMinutes();
                        }

                        User user = userService.findByUsername(currentUser).orElseThrow();
                        Order o = Order.builder()
                                .user(user)
                                .restaurant(r)
                                .foodName(chosen.getName())
                                .foodPrice(chosen.getPrice())
                                .deliveryTimeMinutes(chosen.getDeliveryTimeMinutes())
                                .orderTime(LocalDateTime.now())
                                .status("PENDING")
                                .build();
                        orderService.placeOrder(o);



                        int chatMinutes = Math.max(1, deliveryMinutes - 2);
                        ChatSession cs = chatService.createSession(currentUser, r.getId(), chatMinutes);

                        System.out.println("order placed id=" + o.getId() + " delivery estimate " + deliveryMinutes + " min");
                        System.out.println("chat session started id=" + cs.getId() + " (valid " + chatMinutes + " min). Use option 7 and enter session id.");
                    }

                    case "7" -> {
                        System.out.print("Act as (u)ser or (r)estaurant? ");
                        String role = sc.nextLine().trim().toLowerCase();
                        if ("u".equals(role) || "user".equals(role)) {
                            if (currentUser == null) { System.out.println("login first"); break; }
                            System.out.print("enter chat session id: ");
                            Long sessionId = Long.parseLong(sc.nextLine().trim());
                            var opt = chatService.findSession(sessionId);
                            if (opt.isEmpty()) { System.out.println("session not found"); break; }
                            ChatSession cs = opt.get();
                            if (!currentUser.equals(cs.getUsername())) {
                                System.out.println("you are not the owner of this session");
                                break;
                            }

                            System.out.println("Entering chat as user. Type 'exit' to leave, 'history' to show all messages.");
                            final long[] lastSeen = {0L};
                            Thread poller = new Thread(() -> {
                                try {
                                    while (!Thread.currentThread().isInterrupted()) {
                                        var newMsgs = chatService.historyAfter(sessionId, lastSeen[0]);
                                        if (!newMsgs.isEmpty()) {
                                            System.out.println();
                                            for (var m : newMsgs) {
                                                System.out.printf("[%s] %s -> %s: %s%n", m.getTimestamp(), m.getFromUser(), m.getToUser(), m.getMessage());
                                                lastSeen[0] = Math.max(lastSeen[0], m.getId());
                                            }
                                        }
                                        Thread.sleep(newMsgs.isEmpty() ? 1000L : 200L);
                                    }
                                } catch (InterruptedException ignored) {}
                            }, "chat-poller-user-" + sessionId);
                            poller.setDaemon(true);
                            poller.start();

                            while (true) {
                                System.out.print(currentUser + ": ");
                                String msg = sc.nextLine();
                                if ("exit".equalsIgnoreCase(msg)) { poller.interrupt(); break; }
                                if ("history".equalsIgnoreCase(msg)) {
                                    chatService.history(sessionId).forEach(m ->
                                            System.out.printf("[%s] %s -> %s: %s%n", m.getTimestamp(), m.getFromUser(), m.getToUser(), m.getMessage()));
                                    continue;
                                }
                                try {
                                    chatService.sendMessage(sessionId, "user:" + currentUser, "rest:" + cs.getRestaurantId(), msg);
                                } catch (IllegalStateException ex) {
                                    System.out.println("chat expired");
                                    poller.interrupt();
                                    break;
                                }
                            }
                        } else if ("r".equals(role) || "rest".equals(role) || "restaurant".equals(role)) {
                            System.out.print("enter restaurant id to act as: ");
                            Long rid = Long.parseLong(sc.nextLine().trim());

// check ownership
                            var owned = restaurantService.findByIdAndOwner(rid, currentUser);
                            if (owned.isEmpty()) {
                                System.out.println("you are not the owner of this restaurant or it does not exist");
                                break;
                            }

// only fetch sessions for this owner’s restaurant
                            var list = chatService.activeSessionsForRestaurant(rid);

                            if (list.isEmpty()) { System.out.println("no active sessions"); break; }

                            System.out.println("Active sessions:");
                            for (int i = 0; i < list.size(); i++) {
                                var s = list.get(i);
                                System.out.printf("%d) sessionId=%d user=%s expiresAt=%s%n", i + 1, s.getId(), s.getUsername(), s.getExpiresAt());
                            }

                            System.out.print("select session number: ");
                            int sel;
                            try {
                                sel = Integer.parseInt(sc.nextLine().trim()) - 1;
                            } catch (NumberFormatException ex) {
                                System.out.println("invalid input"); break;
                            }
                            if (sel < 0 || sel >= list.size()) { System.out.println("invalid selection"); break; }

                            Long sessionId = list.get(sel).getId();

                            System.out.println("Entering chat as restaurant. Type 'exit' to leave, 'history' to show all messages.");
                            final long[] lastSeen = {0L};
                            Thread poller = new Thread(() -> {
                                try {
                                    while (!Thread.currentThread().isInterrupted()) {
                                        var newMsgs = chatService.historyAfter(sessionId, lastSeen[0]);
                                        if (!newMsgs.isEmpty()) {
                                            System.out.println();
                                            for (var m : newMsgs) {
                                                System.out.printf("[%s] %s -> %s: %s%n", m.getTimestamp(), m.getFromUser(), m.getToUser(), m.getMessage());
                                                lastSeen[0] = Math.max(lastSeen[0], m.getId());
                                            }
                                        }
                                        Thread.sleep(newMsgs.isEmpty() ? 1000L : 200L);
                                    }
                                } catch (InterruptedException ignored) {}
                            }, "chat-poller-rest-" + sessionId);
                            poller.setDaemon(true);
                            poller.start();

                            while (true) {
                                System.out.print("rest:" + rid + ": ");
                                String msg = sc.nextLine();
                                if ("exit".equalsIgnoreCase(msg)) { poller.interrupt(); break; }
                                if ("history".equalsIgnoreCase(msg)) {
                                    chatService.history(sessionId).forEach(m ->
                                            System.out.printf("[%s] %s -> %s: %s%n", m.getTimestamp(), m.getFromUser(), m.getToUser(), m.getMessage()));
                                    continue;
                                }
                                try {
                                    String toUser = chatService.findSession(sessionId).map(ChatSession::getUsername).orElse("unknown");
                                    chatService.sendMessage(sessionId, "rest:" + rid, "user:" + toUser, msg);
                                } catch (IllegalStateException ex) {
                                    System.out.println("chat expired");
                                    poller.interrupt();
                                    break;
                                }
                            }
                        } else {
                            System.out.println("unknown role");
                        }
                    }

                    case "8" -> {
                        if (currentUser == null) {
                            System.out.println("login first");
                            break;
                        }
                        User u = userService.findByUsername(currentUser).orElseThrow();
                        List<Order> orders = orderService.listOrdersForUser(u.getId());
                        if (orders.isEmpty()) System.out.println("no orders");
                        orders.forEach(ord -> System.out.printf("id=%d food=%s rest=%s price=%.2f time=%s dt=%dmin%n",
                                ord.getId(), ord.getFoodName(), ord.getRestaurant().getName(), ord.getFoodPrice(), ord.getOrderTime(), ord.getDeliveryTimeMinutes()));
                    }
                    case "9" -> {
                        System.out.println("bye");
                        sc.close();
                        return;
                    }
                    case "10" -> {

                        if (currentUser == null) { System.out.println("login first"); break; }
                        List<Restaurant> all = restaurantService.listAll();
                        List<Restaurant> mine = all.stream()
                                .filter(r -> finalCurrentUser.equals(r.getOwnerUsername()))
                                .toList();
                        if (mine.isEmpty()) {
                            System.out.println("you don't own any restaurants");
                        } else {
                            System.out.println("Your restaurants:");
                            System.out.println("-----------------------------------------------------");
                            System.out.printf("%-6s %-20s %-25s%n", "id", "name", "address");
                            System.out.println("-----------------------------------------------------");
                            for (Restaurant r : mine) {
                                System.out.printf("%-6d %-20s %-25s%n", r.getId(), r.getName(), r.getAddress());
                            }
                        }
                    }

                    case "11" -> {
                        if (currentUser == null) { System.out.println("login first"); break; }
                        User u = userService.findByUsername(currentUser).orElseThrow();
                        List<Order> pending = orderService.listOrdersForUser(u.getId(), false);
                        List<Order> delivered = orderService.listOrdersForUser(u.getId(), true);

                        System.out.println("\n--- Pending orders (you placed) ---");
                        if (pending.isEmpty()) System.out.println("none");
                        else {
                            System.out.printf("%-6s %-20s %-15s %-8s %-20s%n", "id", "food", "restaurant", "price", "orderTime");
                            System.out.println("--------------------------------------------------------------------------");
                            for (Order o : pending) {
                                System.out.printf("%-6d %-20s %-15s %-8.2f %-20s%n",
                                        o.getId(), o.getFoodName(), o.getRestaurant().getName(), o.getFoodPrice(), o.getOrderTime());
                            }
                        }

                        System.out.println("\n--- Delivered orders (you placed) ---");
                        if (delivered.isEmpty()) System.out.println("none");
                        else {
                            System.out.printf("%-6s %-20s %-15s %-8s %-20s%n", "id", "food", "restaurant", "price", "orderTime");
                            System.out.println("--------------------------------------------------------------------------");
                            for (Order o : delivered) {
                                System.out.printf("%-6d %-20s %-15s %-8.2f %-20s%n",
                                        o.getId(), o.getFoodName(), o.getRestaurant().getName(), o.getFoodPrice(), o.getOrderTime());
                            }
                        }
                    }

                    case "12" -> {
                        System.out.print("enter restaurant id to view orders for: ");
                        Long rid = Long.parseLong(sc.nextLine().trim());
                        Optional<Restaurant> rOpt = restaurantService.findById(rid);
                        if (rOpt.isEmpty()) { System.out.println("restaurant not found"); break; }
                        Restaurant r = rOpt.get();
                        // check ownership if you want:
                        if (currentUser == null || !currentUser.equals(r.getOwnerUsername())) {
                            System.out.println("you must be logged in as the restaurant owner to view these orders");
                            break;
                        }

                        List<Order> pending = orderService.listOrdersForRestaurant(rid, false);
                        List<Order> delivered = orderService.listOrdersForRestaurant(rid, true);

                        System.out.println("\n--- Pending orders received by " + r.getName() + " ---");
                        if (pending.isEmpty()) System.out.println("none");
                        else {
                            System.out.printf("%-6s %-20s %-15s %-8s %-20s %-10s%n", "id", "food", "customer", "price", "orderTime", "delivMin");
                            System.out.println("--------------------------------------------------------------------------------");
                            for (Order o : pending) {
                                System.out.printf("%-6d %-20s %-15s %-8.2f %-20s %-10d%n",
                                        o.getId(), o.getFoodName(), o.getUser().getUsername(), o.getFoodPrice(), o.getOrderTime(), o.getDeliveryTimeMinutes());
                            }
                        }

                        System.out.println("\n--- Delivered orders received by " + r.getName() + " ---");
                        if (delivered.isEmpty()) System.out.println("none");
                        else {
                            System.out.printf("%-6s %-20s %-15s %-8s %-20s %-10s%n", "id", "food", "customer", "price", "orderTime", "delivMin");
                            System.out.println("--------------------------------------------------------------------------------");
                            for (Order o : delivered) {
                                System.out.printf("%-6d %-20s %-15s %-8.2f %-20s %-10d%n",
                                        o.getId(), o.getFoodName(), o.getUser().getUsername(), o.getFoodPrice(), o.getOrderTime(), o.getDeliveryTimeMinutes());
                            }
                        }
                    }

                    default -> System.out.println("invalid choice");
                }
            } catch (java.util.NoSuchElementException e) {
                // Scanner closed (e.g. ctrl-z): exit cleanly
                System.out.println("input closed, exiting");
                return;
            } catch (Exception e) {
                System.out.println("error: " + e.getMessage());
                e.printStackTrace(System.out);
            }
        }
    }

    private void seedSampleData() {
        if (restaurantService.listAll().isEmpty()) {
            Restaurant r = Restaurant.builder().name("Demo Pizza").address("Demo Street").build();
            restaurantService.addRestaurant(r);
            FoodItem f1 = FoodItem.builder().name("Margherita").price(120.0).deliveryTimeMinutes(1).build();
            FoodItem f2 = FoodItem.builder().name("Pepperoni").price(180.0).deliveryTimeMinutes(2).build();
            restaurantService.addFoodItem(r.getId(), f1);
            restaurantService.addFoodItem(r.getId(), f2);
        }
    }
}
