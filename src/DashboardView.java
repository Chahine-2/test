import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.util.List;
import java.util.function.BiConsumer;

public class DashboardView {
    private final String sidebarStyle;
    private final String panelStyle;
    private final BiConsumer<User, String> onNavigate;
    private final Runnable onLogout;
    private final BiConsumer<User, String> onAvatarChanged;

    public DashboardView(String sidebarStyle, String panelStyle, BiConsumer<User, String> onNavigate, Runnable onLogout, BiConsumer<User, String> onAvatarChanged) {
        this.sidebarStyle = sidebarStyle;
        this.panelStyle = panelStyle;
        this.onNavigate = onNavigate;
        this.onLogout = onLogout;
        this.onAvatarChanged = onAvatarChanged;
    }

    public BorderPane buildShell(User user, String activeSection) {
        VBox sidebar = buildSidebar(user, activeSection);
        BorderPane main = new BorderPane();
        main.setMinWidth(0);
        main.setMaxWidth(Double.MAX_VALUE);
        main.setStyle("-fx-background-color: #f8fafc;");
        main.setPadding(new Insets(28));
        main.setTop(buildHeader(user, activeSection));
        main.setCenter(buildContent(user, activeSection));

        BorderPane shell = new BorderPane();
        shell.setLeft(sidebar);
        shell.setCenter(main);
        shell.setStyle("-fx-background-color: #e5eefc;");
        return shell;
    }

    public List<String> allowedSections(Role role) {
        return switch (role) {
            case ADMIN -> List.of("User Management", "Departments", "Reports", "Analytics", "System Settings", "Messages", "Settings");
            case TEACHER -> List.of("Courses", "Evaluation", "Presence");
            case STUDENT -> List.of("My Courses", "Assignments", "Grades", "Schedule", "Achievements", "Messages", "Settings");
        };
    }

    private VBox buildSidebar(User user, String activeSection) {
        if (user.getRole() == Role.ADMIN) {
            return buildAdminSidebar(user, activeSection);
        }
        if (user.getRole() == Role.STUDENT) {
            return buildStudentSidebar(user, activeSection);
        }

        VBox sidebar = new VBox(18);
        sidebar.setPrefWidth(290);
        sidebar.setMinWidth(290);
        sidebar.setMaxWidth(290);
        sidebar.setPadding(new Insets(24));
        sidebar.setStyle(sidebarStyle);

        sidebar.getChildren().addAll(
                sidebarBrand(),
                dividerDark(),
                navButton("Dashboard", "Dashboard".equals(activeSection), () -> onNavigate.accept(user, "Dashboard"))
        );

        for (String section : allowedSections(user.getRole())) {
            sidebar.getChildren().add(navButton(section, activeSection.equals(section), () -> onNavigate.accept(user, section)));
        }

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        VBox profile = new VBox(4,
                text(displayName(user), "#ffffff", 17, true),
                text(user.getRole().name(), "#cbd5e1", 13, false)
        );
        profile.setPadding(new Insets(16));
        profile.setStyle("-fx-background-color: rgba(255,255,255,0.06); -fx-background-radius: 18;");

        Button logout = new Button("Logout");
        logout.setMaxWidth(Double.MAX_VALUE);
        logout.setPrefHeight(46);
        logout.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 14; -fx-cursor: hand;");
        logout.setOnAction(e -> onLogout.run());

        sidebar.getChildren().addAll(spacer, profile, logout);
        return sidebar;
    }

    private VBox buildAdminSidebar(User user, String activeSection) {
        VBox sidebar = new VBox(16);
        sidebar.setPrefWidth(300);
        sidebar.setMinWidth(300);
        sidebar.setMaxWidth(300);
        sidebar.setPadding(new Insets(20, 14, 20, 14));
        sidebar.setStyle(sidebarStyle);

        sidebar.getChildren().addAll(
                sidebarBrand(),
                dividerDark(),
                navButton("Dashboard", "Dashboard".equals(activeSection), () -> onNavigate.accept(user, "Dashboard"))
        );

        for (String section : allowedSections(user.getRole())) {
            sidebar.getChildren().add(navButton(section, activeSection.equals(section), () -> onNavigate.accept(user, section)));
        }

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        VBox profile = new VBox(2,
                text(displayName(user), "#ffffff", 16, true),
                text("Administrator", "#93c5fd", 12, false)
        );
        profile.setPadding(new Insets(16));
        profile.setStyle("-fx-background-color: rgba(30,41,59,0.65); -fx-background-radius: 14;");

        Button logout = new Button("Logout");
        logout.setMaxWidth(Double.MAX_VALUE);
        logout.setPrefHeight(46);
        logout.setStyle("-fx-background-color: transparent; -fx-border-color: #334155; -fx-border-width: 1; -fx-text-fill: #e2e8f0; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 12; -fx-border-radius: 12; -fx-cursor: hand;");
        logout.setOnAction(e -> onLogout.run());

        sidebar.getChildren().addAll(spacer, dividerDark(), profile, logout);
        return sidebar;
    }

    private VBox buildStudentSidebar(User user, String activeSection) {
        VBox sidebar = new VBox(16);
        sidebar.setPrefWidth(300);
        sidebar.setMinWidth(300);
        sidebar.setMaxWidth(300);
        sidebar.setPadding(new Insets(20, 14, 20, 14));
        sidebar.setStyle(sidebarStyle);

        sidebar.getChildren().addAll(
                sidebarBrand(),
                dividerDark(),
                navButton("Dashboard", "Dashboard".equals(activeSection), () -> onNavigate.accept(user, "Dashboard"))
        );

        for (String section : allowedSections(user.getRole())) {
            sidebar.getChildren().add(navButton(section, activeSection.equals(section), () -> onNavigate.accept(user, section)));
        }

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        VBox profile = new VBox(2,
                text(displayName(user), "#ffffff", 16, true),
                text("Student", "#93c5fd", 12, false)
        );
        profile.setPadding(new Insets(16));
        profile.setStyle("-fx-background-color: rgba(30,41,59,0.65); -fx-background-radius: 14;");

        Button logout = new Button("Logout");
        logout.setMaxWidth(Double.MAX_VALUE);
        logout.setPrefHeight(46);
        logout.setStyle("-fx-background-color: transparent; -fx-border-color: #334155; -fx-border-width: 1; -fx-text-fill: #e2e8f0; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 12; -fx-border-radius: 12; -fx-cursor: hand;");
        logout.setOnAction(e -> onLogout.run());

        sidebar.getChildren().addAll(spacer, dividerDark(), profile, logout);
        return sidebar;
    }

    private Node buildHeader(User user, String section) {
        VBox header = new VBox(8);
        header.setPadding(new Insets(0, 4, 24, 4));

        String titleText = user.getRole() == Role.STUDENT && "Dashboard".equals(section)
                ? "Welcome back, " + displayFirstName(user) + "!"
                : sectionTitle(section, user);
        Label title = new Label(titleText);
        title.setStyle("-fx-font-size: 48px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        String subtitleText;
        if (user.getRole() == Role.ADMIN && "Dashboard".equals(section)) {
            subtitleText = "System overview and key metrics";
        } else if (user.getRole() == Role.STUDENT && "Dashboard".equals(section)) {
            subtitleText = "Here's what's happening with your studies today";
        } else {
            subtitleText = roleContent(section, user);
        }
        Label subtitle = new Label(subtitleText);
        subtitle.setStyle("-fx-font-size: 15px; -fx-text-fill: #64748b;");

        HBox chips = new HBox(10, chip("Role: " + user.getRole()), chip(section));
        header.getChildren().addAll(title, subtitle, chips);
        return header;
    }

    private Node buildContent(User user, String section) {
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setPannable(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setMaxWidth(Double.MAX_VALUE);

        VBox content = new VBox(18);
        content.setFillWidth(true);
        content.setMinWidth(0);
        content.setMaxWidth(Double.MAX_VALUE);

        if ("Dashboard".equals(section) && user.getRole() == Role.ADMIN) {
            content.getChildren().add(buildAdminDashboardContent());
        } else if ("Dashboard".equals(section) && user.getRole() == Role.STUDENT) {
            content.getChildren().add(buildStudentDashboardContent());
        } else if ("Dashboard".equals(section)) {
            content.getChildren().addAll(
                    statsGrid(
                            statCard("Students", "2,847", "+12% from last month", "#2563eb", "S"),
                            statCard("Teachers", "184", "+5% from last month", "#9333ea", "T"),
                            statCard("Courses", "156", "+8% from last month", "#10b981", "C"),
                            statCard("Revenue", "$284K", "+15% from last month", "#f97316", "$")
                    ),
                    twoColumnPanels(
                            panel("Department Overview", List.of(
                                    listRow("Computer Science", "486 students", "28 teachers", "35 courses"),
                                    listRow("Mathematics", "542 students", "32 teachers", "28 courses"),
                                    listRow("Engineering", "611 students", "40 teachers", "41 courses")
                            )),
                            panel("Recent Activity", List.of(
                                    activity("15 new students registered", "2 hours ago"),
                                    activity("New course 'Data Science' created", "4 hours ago"),
                                    activity("Server maintenance scheduled", "5 hours ago"),
                                    activity("Backup completed successfully", "1 day ago")
                            ))
                    )
            );
        } else {
            content.getChildren().add(createSectionView(section, user));
        }

        scroll.setContent(content);
        return scroll;
    }

    private Node buildAdminDashboardContent() {
        VBox root = new VBox(18);
        root.setMinWidth(0);
        root.setMaxWidth(Double.MAX_VALUE);

        root.getChildren().addAll(
                statsGrid(
                        statCard("Total Students", "2,847", "+12% from last month", "#2563eb", "S"),
                        statCard("Total Teachers", "184", "+5% from last month", "#9333ea", "T"),
                        statCard("Active Courses", "156", "+8% from last month", "#16a34a", "C"),
                        statCard("Revenue (Monthly)", "$284K", "+15% from last month", "#ea580c", "$")
                ),
                adminMiddleRow(),
                adminBottomMetrics()
        );
        return root;
    }

    private Node adminMiddleRow() {
        VBox departments = panel("Department Overview", List.of(
                departmentRow("Computer Science", "486", "28", "35", "#2563eb"),
                departmentRow("Mathematics", "542", "32", "28", "#9333ea"),
                departmentRow("Engineering", "624", "38", "42", "#16a34a"),
                departmentRow("Business", "458", "26", "31", "#f97316"),
                departmentRow("Arts & Humanities", "387", "30", "20", "#ec4899"),
                departmentRow("Sciences", "350", "30", "25", "#06b6d4")
        ));

        VBox rightPanels = new VBox(16,
                panel("Recent Activity", List.of(
                        adminActivity("+", "#2563eb", "15 new students registered", "2 hours ago"),
                        adminActivity("+", "#16a34a", "New course 'Data Science' created", "4 hours ago"),
                        adminActivity("!", "#ea580c", "Server maintenance scheduled", "5 hours ago"),
                        adminActivity("v", "#16a34a", "Backup completed successfully", "1 day ago")
                )),
                panel("System Health", List.of(
                        healthLine("Server Status", "Operational", "99.8% uptime", 0.998, "#16a34a"),
                        healthLine("Database", "Operational", "98.5% uptime", 0.985, "#16a34a"),
                        healthLine("API Services", "Operational", "99.2% uptime", 0.992, "#16a34a"),
                        healthLine("Storage", "Warning", "87.3% uptime", 0.873, "#d97706")
                ))
        );

        rightPanels.setMinWidth(380);
        rightPanels.setPrefWidth(420);
        rightPanels.setMaxWidth(460);

        HBox row = new HBox(18, departments, rightPanels);
        row.setMinWidth(0);
        row.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(departments, Priority.ALWAYS);
        return row;
    }

    private Node adminBottomMetrics() {
        HBox row = new HBox(18,
                summaryCard("Enrollment Rate", "94.5%", "Above target of 90%", "linear-gradient(to right, #2563eb, #1d4ed8)"),
                summaryCard("Student Satisfaction", "4.7/5.0", "Based on 1,234 reviews", "linear-gradient(to right, #9333ea, #a21caf)"),
                summaryCard("Completion Rate", "87.2%", "Improved by 5.3%", "linear-gradient(to right, #16a34a, #059669)")
        );
        row.setMinWidth(0);
        row.setMaxWidth(Double.MAX_VALUE);
        for (Node node : row.getChildren()) {
            HBox.setHgrow(node, Priority.ALWAYS);
        }
        return row;
    }

    private Node departmentRow(String name, String students, String teachers, String courses, String accent) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(14));
        card.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 14;");

        StackPane badge = new StackPane(text("D", "#ffffff", 11, true));
        badge.setPrefSize(26, 26);
        badge.setStyle("-fx-background-color: " + accent + "; -fx-background-radius: 8;");

        HBox header = new HBox(10, badge, text(name, "#0f172a", 16, true));

        GridPane metrics = new GridPane();
        metrics.setHgap(26);
        metrics.add(text("Students", "#64748b", 12, false), 0, 0);
        metrics.add(text("Teachers", "#64748b", 12, false), 1, 0);
        metrics.add(text("Courses", "#64748b", 12, false), 2, 0);
        metrics.add(text(students, "#0f172a", 16, true), 0, 1);
        metrics.add(text(teachers, "#0f172a", 16, true), 1, 1);
        metrics.add(text(courses, "#0f172a", 16, true), 2, 1);

        card.getChildren().addAll(header, metrics);
        return card;
    }

    private Node adminActivity(String icon, String color, String title, String time) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.TOP_LEFT);

        StackPane dot = new StackPane(text(icon, "#ffffff", 10, true));
        dot.setPrefSize(18, 18);
        dot.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 9;");

        VBox detail = new VBox(2,
                text(title, "#0f172a", 13, true),
                text(time, "#64748b", 12, false)
        );

        row.getChildren().addAll(dot, detail);
        return row;
    }

    private Node healthLine(String label, String status, String uptime, double progress, String color) {
        VBox box = new VBox(4);

        HBox head = new HBox(8,
                text(label, "#0f172a", 13, true),
                new Region(),
                chip(status)
        );
        HBox.setHgrow(head.getChildren().get(1), Priority.ALWAYS);

        ProgressBar bar = new ProgressBar(progress);
        bar.setPrefWidth(Double.MAX_VALUE);
        bar.setStyle("-fx-accent: " + color + ";");

        box.getChildren().addAll(head, bar, text(uptime, "#64748b", 12, false));
        return box;
    }

    private Node summaryCard(String title, String value, String subtitle, String gradient) {
        VBox box = new VBox(8,
                text(title, "#ffffff", 15, true),
                text(value, "#ffffff", 22, true),
                text(subtitle, "#e2e8f0", 13, false)
        );
        box.setPadding(new Insets(20));
        box.setMinHeight(150);
        box.setStyle("-fx-background-color: " + gradient + "; -fx-background-radius: 18;");
        return box;
    }

    private Node buildStudentDashboardContent() {
        VBox root = new VBox(18);
        root.setMinWidth(0);
        root.setMaxWidth(Double.MAX_VALUE);

        root.getChildren().addAll(
                statsGrid(
                        statCard("Active Courses", "4", "", "#2563eb", "C"),
                        statCard("Pending Tasks", "8", "", "#9333ea", "T"),
                        statCard("Average Grade", "A-", "+5% from last month", "#16a34a", "G"),
                        statCard("Study Hours", "28h", "This week", "#f97316", "H")
                ),
                twoColumnPanels(
                        panel("Today's Schedule", List.of(
                                studentScheduleRow("9:00", "Mathematics", "9:00 AM - 10:30 AM", "Room 201", "Dr. Smith"),
                                studentScheduleRow("11:00", "Physics", "11:00 AM - 12:30 PM", "Lab 3", "Prof. Johnson"),
                                studentScheduleRow("2:00", "English Literature", "2:00 PM - 3:30 PM", "Room 105", "Ms. Williams")
                        )),
                        panel("Recent Assignments", List.of(
                                studentAssignmentRow("Calculus Problem Set 5", "Mathematics", "Due: Apr 10, 2026", "high", "Not Started"),
                                studentAssignmentRow("Physics Lab Report", "Physics", "Due: Apr 12, 2026", "medium", "In Progress"),
                                studentAssignmentRow("Essay on Shakespeare", "English", "Due: Apr 15, 2026", "low", "Not Started")
                        ))
                ),
                studentCourseProgressPanel()
        );
        return root;
    }

    private Node studentCourseProgressPanel() {
        VBox panel = new VBox(18);
        panel.setPadding(new Insets(22));
        panel.setMinWidth(0);
        panel.setMaxWidth(Double.MAX_VALUE);
        panel.setStyle(panelStyle);
        panel.getChildren().addAll(
                text("Course Progress", "#0f172a", 22, true),
                studentCourseProgressRow()
        );
        return panel;
    }

    private Node studentCourseProgressRow() {
        GridPane grid = new GridPane();
        grid.setHgap(22);
        grid.setVgap(12);
        grid.setMinWidth(0);
        grid.setMaxWidth(Double.MAX_VALUE);

        for (int i = 0; i < 4; i++) {
            ColumnConstraints c = new ColumnConstraints();
            c.setPercentWidth(25);
            c.setHgrow(Priority.ALWAYS);
            grid.getColumnConstraints().add(c);
        }

        grid.add(studentCourseProgressItem("Mathematics", "A", 0.75, "#2563eb"), 0, 0);
        grid.add(studentCourseProgressItem("Physics", "B+", 0.68, "#9333ea"), 1, 0);
        grid.add(studentCourseProgressItem("English Literature", "A-", 0.82, "#16a34a"), 2, 0);
        grid.add(studentCourseProgressItem("History", "B", 0.70, "#f97316"), 3, 0);

        return grid;
    }

    private Node studentCourseProgressItem(String course, String grade, double value, String color) {
        VBox item = new VBox(10);
        item.setMinWidth(0);
        item.setMaxWidth(Double.MAX_VALUE);

        Label courseLabel = text(course, "#0f172a", 16, true);
        Label gradeLabel = text(grade, "#0f172a", 16, true);

        HBox header = new HBox(8, courseLabel, new Region(), gradeLabel);
        HBox.setHgrow(header.getChildren().get(1), Priority.ALWAYS);

        ProgressBar bar = new ProgressBar(value);
        bar.setPrefWidth(Double.MAX_VALUE);
        bar.setMaxWidth(Double.MAX_VALUE);
        bar.setStyle("-fx-accent: " + color + ";");

        item.getChildren().addAll(header, bar, text((int) (value * 100) + "% Complete", "#64748b", 12, false));
        return item;
    }

    private Node studentScheduleRow(String time, String title, String range, String room, String teacher) {
        HBox row = new HBox(16);
        row.setPadding(new Insets(14));
        row.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 14;");

        StackPane badge = new StackPane(text(time, "#ffffff", 12, true));
        badge.setPrefSize(56, 56);
        badge.setStyle("-fx-background-color: linear-gradient(to right, #2563eb, #9333ea); -fx-background-radius: 14;");

        VBox info = new VBox(2,
                text(title, "#0f172a", 16, true),
                text(range, "#334155", 13, false),
                text(room + "  -  " + teacher, "#64748b", 12, false)
        );

        row.getChildren().addAll(badge, info);
        return row;
    }

    private Node studentAssignmentRow(String title, String subject, String due, String priority, String status) {
        VBox row = new VBox(6);
        row.setPadding(new Insets(14));
        row.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 14;");

        String priorityColor = switch (priority.toLowerCase()) {
            case "high" -> "#dc2626";
            case "medium" -> "#b45309";
            default -> "#059669";
        };

        HBox header = new HBox(10,
                text(title, "#0f172a", 16, true),
                new Region(),
                text(priority, priorityColor, 12, true)
        );
        HBox.setHgrow(header.getChildren().get(1), Priority.ALWAYS);

        HBox footer = new HBox(10,
                text(due, "#64748b", 12, false),
                new Region(),
                chip(status)
        );
        HBox.setHgrow(footer.getChildren().get(1), Priority.ALWAYS);

        row.getChildren().addAll(header, text(subject, "#334155", 13, false), footer);
        return row;
    }

    private Node createSectionView(String section, User user) {
        return switch (section) {
            case "My Courses" -> panel("My Courses", List.of(
                    courseCard("Mathematics", "Room 201", "Mon/Wed", "9:00 AM"),
                    courseCard("Physics", "Lab 3", "Tue/Thu", "11:00 AM"),
                    courseCard("English Literature", "Room 105", "Fri", "2:00 PM")
            ));
            case "Assignments" -> panel("Assignments", List.of(
                    studentAssignmentRow("Calculus Problem Set 5", "Mathematics", "Due: Apr 10, 2026", "high", "Not Started"),
                    studentAssignmentRow("Physics Lab Report", "Physics", "Due: Apr 12, 2026", "medium", "In Progress"),
                    studentAssignmentRow("Essay on Shakespeare", "English", "Due: Apr 15, 2026", "low", "Not Started")
            ));
            case "Grades" -> panel("Grades", List.of(
                    progressItem("Mathematics", 0.88, "A"),
                    progressItem("Physics", 0.84, "B+"),
                    progressItem("English", 0.91, "A-")
            ));
            case "Schedule" -> panel("Schedule", List.of(
                    studentScheduleRow("9:00", "Mathematics", "9:00 AM - 10:30 AM", "Room 201", "Dr. Smith"),
                    studentScheduleRow("11:00", "Physics", "11:00 AM - 12:30 PM", "Lab 3", "Prof. Johnson"),
                    studentScheduleRow("2:00", "English Literature", "2:00 PM - 3:30 PM", "Room 105", "Ms. Williams")
            ));
            case "Achievements" -> panel("Achievements", List.of(
                    activity("Honor Roll - Semester 1", "Excellent academic performance"),
                    activity("Perfect Attendance", "30-day attendance streak"),
                    activity("Top 5 in Mathematics", "Latest monthly assessment")
            ));
            case "Users", "User Management" -> panel("User Management", List.of(
                    tableHeader("Name", "Role", "Department", "Status"),
                    tableRow("Emily Johnson", "Teacher", "Computer Science", "Active"),
                    tableRow("Michael Chen", "Student", "Engineering", "Active"),
                    tableRow("Sarah Williams", "Teacher", "Mathematics", "Active")
            ));
            case "Departments" -> panel("Departments", List.of(
                    listRow("Computer Science", "486 students", "28 teachers", "35 courses"),
                    listRow("Mathematics", "542 students", "32 teachers", "28 courses"),
                    listRow("Engineering", "624 students", "38 teachers", "42 courses")
            ));
            case "Reports" -> panel("Reports", List.of(
                    activity("Monthly attendance report generated", "1 hour ago"),
                    activity("Performance report exported", "Today"),
                    activity("Financial summary completed", "Yesterday")
            ));
            case "Analytics" -> panel("Analytics", List.of(
                    progressItem("Enrollment Growth", 0.94, "94%"),
                    progressItem("Satisfaction Index", 0.89, "89%"),
                    progressItem("Course Completion", 0.87, "87%")
            ));
            case "System Settings", "Settings" -> buildSettingsPage(user);
            case "Messages" -> panel("Messages", List.of(
                    activity("New ticket from Mathematics department", "20 minutes ago"),
                    activity("Parent inquiry about attendance", "2 hours ago"),
                    activity("Broadcast prepared for all students", "Today")
            ));
            case "Courses" -> panel("Courses", List.of(
                    courseCard("Advanced Mathematics", "32 students", "Room 301", "10:00 AM"),
                    courseCard("Physics 101", "28 students", "Lab A", "2:00 PM"),
                    courseCard("Chemistry Basics", "25 students", "Room 205", "4:00 PM")
            ));
            case "Evaluation" -> panel("Evaluation", List.of(
                    progressItem("Advanced Mathematics", 0.92, "A"),
                    progressItem("Physics", 0.87, "B+"),
                    progressItem("Chemistry", 0.89, "A-")
            ));
            case "Presence" -> panel("Presence", List.of(
                    attendanceCard("Present Today", "92%", "+4% this week"),
                    attendanceCard("Late Arrivals", "5", "Today"),
                    attendanceCard("Absences", "3", "This week")
            ));
            default -> panel(section + " Overview", List.of(text("You are viewing " + section + " as " + user.getRole() + ".", "#475569", 14, false)));
        };
    }

    private VBox panel(String titleText, List<Node> children) {
        VBox panel = new VBox(14);
        panel.setPadding(new Insets(22));
        panel.setMinWidth(0);
        panel.setMaxWidth(Double.MAX_VALUE);
        panel.setStyle(panelStyle);

        Label title = new Label(titleText);
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        panel.getChildren().add(title);
        panel.getChildren().add(new Separator());
        panel.getChildren().addAll(children);
        return panel;
    }

    private Node buildSettingsPage(User user) {
        VBox menuCard = new VBox(10,
                settingsMenuItem("Profile", true),
                settingsMenuItem("Notifications", false),
                settingsMenuItem("Security", false),
                settingsMenuItem("Appearance", false),
                settingsMenuItem("Language", false)
        );
        menuCard.setPadding(new Insets(18));
        menuCard.setPrefWidth(300);
        menuCard.setMinWidth(280);
        menuCard.setStyle(panelStyle);

        VBox details = new VBox(14);
        details.setPadding(new Insets(22));
        details.setStyle(panelStyle);

        Label title = text("Profile Settings", "#0f172a", 36 / 2, true);

        String initials = initialsFor(user);
        Label initialsLabel = text(initials, "#ffffff", 16, true);
        StackPane avatar = new StackPane(initialsLabel);
        avatar.setPrefSize(96, 96);
        avatar.setStyle("-fx-background-color: linear-gradient(to right, #4f46e5, #9333ea); -fx-background-radius: 48;");
        applyAvatarFromPath(avatar, initialsLabel, user.getAvatarPath());

        Button changePhoto = new Button("Change Photo");
        changePhoto.setPrefHeight(40);
        changePhoto.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold; -fx-background-radius: 12;");
        changePhoto.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Choose Profile Picture");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"));

            String picturesPath = System.getProperty("user.home") + "\\Pictures";
            File picturesDir = new File(picturesPath);
            if (picturesDir.exists() && picturesDir.isDirectory()) {
                chooser.setInitialDirectory(picturesDir);
            }

            File selected = chooser.showOpenDialog(changePhoto.getScene().getWindow());
            if (selected == null) {
                return;
            }

            String descriptor = openAvatarEditor(selected, changePhoto.getScene().getWindow());
            if (descriptor == null) {
                return;
            }

            user.setAvatarPath(descriptor);
            onAvatarChanged.accept(user, descriptor);
            applyAvatarFromPath(avatar, initialsLabel, descriptor);
        });

        Button remove = new Button("Remove");
        remove.setPrefHeight(40);
        remove.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #cbd5e1; -fx-text-fill: #0f172a; -fx-font-size: 13px; -fx-background-radius: 12; -fx-border-radius: 12;");
        remove.setOnAction(e -> {
            user.setAvatarPath(null);
            onAvatarChanged.accept(user, null);
            applyAvatarFromPath(avatar, initialsLabel, null);
        });

        HBox avatarRow = new HBox(12, avatar, changePhoto, remove);
        avatarRow.setAlignment(Pos.CENTER_LEFT);

        TextField firstName = settingsField("First Name", splitName(user.getUsername())[0]);
        TextField lastName = settingsField("Last Name", splitName(user.getUsername())[1]);
        HBox nameRow = new HBox(14,
                labeledField("First Name", firstName),
                labeledField("Last Name", lastName)
        );
        HBox.setHgrow(nameRow.getChildren().get(0), Priority.ALWAYS);
        HBox.setHgrow(nameRow.getChildren().get(1), Priority.ALWAYS);

        TextField email = settingsField("Email", user.getEmail() == null ? "" : user.getEmail());

        ComboBox<String> role = new ComboBox<>();
        role.getItems().addAll("Administrator", "Teacher", "Student", "Instructor");
        role.setValue(mapRole(user.getRole()));
        role.setPrefHeight(46);
        role.setMaxWidth(Double.MAX_VALUE);
        role.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #cbd5e1; -fx-border-radius: 12; -fx-background-radius: 12; -fx-font-size: 13px;");

        TextArea bio = new TextArea();
        bio.setWrapText(true);
        bio.setPrefRowCount(4);
        bio.setText("Passionate educator with 10+ years of experience in mathematics and sciences.");
        bio.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #cbd5e1; -fx-border-radius: 12; -fx-background-radius: 12; -fx-font-size: 13px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button cancel = new Button("Cancel");
        cancel.setPrefHeight(44);
        cancel.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #cbd5e1; -fx-text-fill: #0f172a; -fx-font-size: 14px; -fx-background-radius: 12; -fx-border-radius: 12;");

        Button save = new Button("Save Changes");
        save.setPrefHeight(44);
        save.setStyle("-fx-background-color: linear-gradient(to right, #2563eb, #9333ea); -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 12;");

        HBox actions = new HBox(10, spacer, cancel, save);

        details.getChildren().addAll(
                title,
                avatarRow,
                nameRow,
                labeledField("Email", email),
                labeledField("Role", role),
                labeledField("Bio", bio),
                new Separator(),
                actions
        );

        HBox wrapper = new HBox(18, menuCard, details);
        HBox.setHgrow(details, Priority.ALWAYS);
        return wrapper;
    }

    private VBox settingsMenuItem(String text, boolean active) {
        Label label = new Label(text);
        label.setStyle(active
                ? "-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;"
                : "-fx-text-fill: #334155; -fx-font-size: 14px;");
        VBox box = new VBox(label);
        box.setPadding(new Insets(12, 14, 12, 14));
        box.setStyle(active
                ? "-fx-background-color: linear-gradient(to right, #2563eb, #9333ea); -fx-background-radius: 12;"
                : "-fx-background-color: transparent; -fx-background-radius: 12;");
        return box;
    }

    private VBox labeledField(String label, Node field) {
        VBox box = new VBox(6, text(label, "#334155", 14, true), field);
        if (field instanceof Region region) {
            region.setMaxWidth(Double.MAX_VALUE);
        }
        VBox.setVgrow(field, Priority.NEVER);
        return box;
    }

    private TextField settingsField(String prompt, String value) {
        TextField field = new TextField(value == null ? "" : value);
        field.setPromptText(prompt);
        field.setPrefHeight(46);
        field.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #cbd5e1; -fx-border-radius: 12; -fx-background-radius: 12; -fx-font-size: 13px;");
        return field;
    }

    private String[] splitName(String username) {
        String safe = username == null ? "" : username.replace('.', ' ').replace('_', ' ').trim();
        if (safe.isBlank()) {
            return new String[]{"John", "Doe"};
        }
        String[] parts = safe.split("\\s+");
        String first = parts[0];
        String last = parts.length > 1 ? parts[parts.length - 1] : "";
        return new String[]{capitalize(first), capitalize(last)};
    }

    private String mapRole(Role role) {
        return switch (role) {
            case ADMIN -> "Administrator";
            case TEACHER -> "Teacher";
            case STUDENT -> "Student";
        };
    }

    private String initialsFor(User user) {
        String[] names = splitName(user.getUsername());
        String first = names[0].isBlank() ? "J" : names[0].substring(0, 1).toUpperCase();
        String last = names[1].isBlank() ? "D" : names[1].substring(0, 1).toUpperCase();
        return first + last;
    }

    private String capitalize(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.substring(0, 1).toUpperCase() + value.substring(1).toLowerCase();
    }

    private void applyAvatarFromPath(StackPane avatar, Label initialsLabel, String path) {
        if (path == null || path.isBlank()) {
            avatar.getChildren().setAll(initialsLabel);
            avatar.setStyle("-fx-background-color: linear-gradient(to right, #4f46e5, #9333ea); -fx-background-radius: 48;");
            return;
        }

        AvatarConfig config = parseAvatarConfig(path);
        File file = new File(config.path());
        if (!file.exists() || !file.isFile()) {
            avatar.getChildren().setAll(initialsLabel);
            avatar.setStyle("-fx-background-color: linear-gradient(to right, #4f46e5, #9333ea); -fx-background-radius: 48;");
            return;
        }

        Image image = new Image(file.toURI().toString(), 0, 0, true, true);
        if (image.isError()) {
            avatar.getChildren().setAll(initialsLabel);
            avatar.setStyle("-fx-background-color: linear-gradient(to right, #4f46e5, #9333ea); -fx-background-radius: 48;");
            return;
        }

        double baseScale = Math.max(96.0 / image.getWidth(), 96.0 / image.getHeight());
        double fitWidth = image.getWidth() * baseScale * config.zoom();
        double fitHeight = image.getHeight() * baseScale * config.zoom();

        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(fitWidth);
        imageView.setFitHeight(fitHeight);
        imageView.setPreserveRatio(true);
        imageView.setTranslateX(clampOffset(config.offsetX(), fitWidth));
        imageView.setTranslateY(clampOffset(config.offsetY(), fitHeight));
        imageView.setRotate(config.rotation());
        imageView.setClip(new Circle(48, 48, 48));

        avatar.getChildren().setAll(imageView);
        avatar.setStyle("-fx-background-color: transparent; -fx-background-radius: 48;");
    }

    private String openAvatarEditor(File selected, Window owner) {
        Image image = new Image(selected.toURI().toString(), 0, 0, true, true);
        if (image.isError()) {
            return null;
        }

        double baseScale = Math.max(96.0 / image.getWidth(), 96.0 / image.getHeight());

        ImageView preview = new ImageView(image);
        preview.setPreserveRatio(true);
        preview.setFitWidth(image.getWidth() * baseScale);
        preview.setFitHeight(image.getHeight() * baseScale);

        StackPane previewPane = new StackPane(preview);
        previewPane.setPrefSize(96, 96);
        previewPane.setMinSize(96, 96);
        previewPane.setMaxSize(96, 96);
        previewPane.setClip(new Circle(48, 48, 48));
        previewPane.setStyle("-fx-background-color: #e2e8f0; -fx-background-radius: 48;");

        Slider zoomSlider = new Slider(1.0, 3.0, 1.0);
        zoomSlider.setBlockIncrement(0.1);
        zoomSlider.setMajorTickUnit(0.5);
        zoomSlider.setMinorTickCount(4);
        zoomSlider.setShowTickLabels(true);
        zoomSlider.setShowTickMarks(true);

        Slider rotationSlider = new Slider(-180, 180, 0);
        rotationSlider.setBlockIncrement(1);
        rotationSlider.setMajorTickUnit(90);
        rotationSlider.setMinorTickCount(8);
        rotationSlider.setShowTickLabels(true);
        rotationSlider.setShowTickMarks(true);

        final double[] dragStartX = new double[1];
        final double[] dragStartY = new double[1];
        final double[] startOffsetX = new double[1];
        final double[] startOffsetY = new double[1];

        Runnable applySizing = () -> {
            double fitWidth = image.getWidth() * baseScale * zoomSlider.getValue();
            double fitHeight = image.getHeight() * baseScale * zoomSlider.getValue();
            preview.setFitWidth(fitWidth);
            preview.setFitHeight(fitHeight);
            preview.setTranslateX(clampOffset(preview.getTranslateX(), fitWidth));
            preview.setTranslateY(clampOffset(preview.getTranslateY(), fitHeight));
            preview.setRotate(rotationSlider.getValue());
        };

        zoomSlider.valueProperty().addListener((obs, oldVal, newVal) -> applySizing.run());
        rotationSlider.valueProperty().addListener((obs, oldVal, newVal) -> applySizing.run());

        preview.setOnMousePressed(e -> {
            dragStartX[0] = e.getSceneX();
            dragStartY[0] = e.getSceneY();
            startOffsetX[0] = preview.getTranslateX();
            startOffsetY[0] = preview.getTranslateY();
        });

        preview.setOnMouseDragged(e -> {
            double fitWidth = preview.getFitWidth();
            double fitHeight = preview.getFitHeight();
            double nextX = startOffsetX[0] + (e.getSceneX() - dragStartX[0]);
            double nextY = startOffsetY[0] + (e.getSceneY() - dragStartY[0]);
            preview.setTranslateX(clampOffset(nextX, fitWidth));
            preview.setTranslateY(clampOffset(nextY, fitHeight));
        });

        Button cancel = new Button("Cancel");
        cancel.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #cbd5e1; -fx-text-fill: #0f172a; -fx-background-radius: 10; -fx-border-radius: 10;");
        Button resetCrop = new Button("Reset Crop");
        resetCrop.setStyle("-fx-background-color: #eef2ff; -fx-text-fill: #3730a3; -fx-font-weight: bold; -fx-background-radius: 10;");
        Button apply = new Button("Apply");
        apply.setStyle("-fx-background-color: linear-gradient(to right, #2563eb, #9333ea); -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10;");

        final String[] result = new String[1];

        Stage editor = new Stage();
        editor.initOwner(owner);
        editor.initModality(Modality.APPLICATION_MODAL);
        editor.setTitle("Adjust Profile Picture");

        cancel.setOnAction(e -> editor.close());
        resetCrop.setOnAction(e -> {
            zoomSlider.setValue(1.0);
            rotationSlider.setValue(0.0);
            preview.setTranslateX(0.0);
            preview.setTranslateY(0.0);
            applySizing.run();
        });
        apply.setOnAction(e -> {
            String descriptor = selected.getAbsolutePath() + "\t" + zoomSlider.getValue() + "\t" + preview.getTranslateX() + "\t" + preview.getTranslateY() + "\t" + rotationSlider.getValue();
            result[0] = descriptor;
            editor.close();
        });

        VBox root = new VBox(12,
                text("Adjust your photo", "#0f172a", 15, true),
                previewPane,
                text("Zoom", "#334155", 12, true),
                zoomSlider,
                text("Rotation", "#334155", 12, true),
                rotationSlider,
                new HBox(10, cancel, resetCrop, apply)
        );
        root.setPadding(new Insets(16));
        root.setStyle(panelStyle);

        applySizing.run();

        editor.setScene(new javafx.scene.Scene(root));
        editor.showAndWait();
        return result[0];
    }

    private AvatarConfig parseAvatarConfig(String value) {
        if (value == null) {
            return new AvatarConfig("", 1.0, 0.0, 0.0, 0.0);
        }
        String[] parts = value.split("\\t", 5);
        if (parts.length < 4) {
            return new AvatarConfig(value, 1.0, 0.0, 0.0, 0.0);
        }
        try {
            double rotation = parts.length >= 5 ? Double.parseDouble(parts[4]) : 0.0;
            return new AvatarConfig(parts[0], Double.parseDouble(parts[1]), Double.parseDouble(parts[2]), Double.parseDouble(parts[3]), rotation);
        } catch (NumberFormatException ex) {
            return new AvatarConfig(parts[0], 1.0, 0.0, 0.0, 0.0);
        }
    }

    private double clampOffset(double value, double fitSize) {
        double max = Math.max(0, (fitSize - 96.0) / 2.0);
        if (value > max) {
            return max;
        }
        if (value < -max) {
            return -max;
        }
        return value;
    }

    private record AvatarConfig(String path, double zoom, double offsetX, double offsetY, double rotation) {
    }

    private Node statsGrid(Node... cards) {
        GridPane grid = new GridPane();
        grid.setHgap(18);
        grid.setVgap(18);
        for (int i = 0; i < 4; i++) {
            ColumnConstraints c = new ColumnConstraints();
            c.setPercentWidth(25);
            grid.getColumnConstraints().add(c);
        }
        for (int i = 0; i < cards.length; i++) {
            grid.add(cards[i], i, 0);
        }
        return grid;
    }

    private Node statCard(String label, String value, String delta, String accent, String iconText) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(22));
        card.setMinWidth(0);
        card.setMaxWidth(Double.MAX_VALUE);
        card.setStyle(panelStyle);

        Label labelNode = text(label, "#334155", 14, false);
        Label valueNode = text(value, "#0f172a", 32, true);
        Label deltaNode = text(delta, "#16a34a", 13, false);

        StackPane icon = new StackPane();
        icon.setPrefSize(62, 62);
        icon.setStyle("-fx-background-color: " + accent + "; -fx-background-radius: 18;");
        Label iconLabel = new Label(iconText);
        iconLabel.setStyle("-fx-font-size: 22px; -fx-text-fill: white; -fx-font-weight: bold;");
        icon.getChildren().add(iconLabel);

        HBox top = new HBox(10, new VBox(6, labelNode, valueNode, deltaNode), new Region(), icon);
        HBox.setHgrow(top.getChildren().get(1), Priority.ALWAYS);
        card.getChildren().add(top);
        return card;
    }

    private Node twoColumnPanels(Node left, Node right) {
        HBox row = new HBox(18, left, right);
        row.setMinWidth(0);
        row.setMaxWidth(Double.MAX_VALUE);
        if (left instanceof Region leftRegion) {
            leftRegion.setMinWidth(0);
            leftRegion.setMaxWidth(Double.MAX_VALUE);
        }
        if (right instanceof Region rightRegion) {
            rightRegion.setMinWidth(0);
            rightRegion.setMaxWidth(Double.MAX_VALUE);
        }
        HBox.setHgrow(left, Priority.ALWAYS);
        HBox.setHgrow(right, Priority.ALWAYS);
        return row;
    }

    private Node listRow(String title, String one, String two, String three) {
        HBox row = new HBox(14);
        row.setPadding(new Insets(18));
        row.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 18;");
        row.getChildren().addAll(
                sectionText(title, "#0f172a", 16, true),
                stretch(),
                sectionText(one, "#334155", 14, false),
                stretch(),
                sectionText(two, "#334155", 14, false),
                stretch(),
                sectionText(three, "#334155", 14, false)
        );
        return row;
    }

    private Node activity(String title, String time) {
        VBox box = new VBox(4);
        box.setPadding(new Insets(14, 0, 14, 0));
        box.getChildren().addAll(text(title, "#0f172a", 14, false), text(time, "#64748b", 12, false));
        return box;
    }

    private Node tableHeader(String... columns) {
        GridPane row = new GridPane();
        row.setHgap(18);
        for (int i = 0; i < columns.length; i++) {
            row.add(text(columns[i], "#334155", 13, true), i, 0);
        }
        return row;
    }

    private Node tableRow(String name, String role, String department, String status) {
        GridPane row = new GridPane();
        row.setHgap(18);
        row.setPadding(new Insets(16, 0, 0, 0));
        row.add(text(name, "#0f172a", 14, true), 0, 0);
        row.add(text(role, "#334155", 14, false), 1, 0);
        row.add(text(department, "#334155", 14, false), 2, 0);
        row.add(chip(status), 3, 0);
        return row;
    }

    private Node courseCard(String title, String students, String location, String time) {
        VBox box = new VBox(4);
        box.setPadding(new Insets(16));
        box.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 18;");
        box.getChildren().addAll(
                text(title, "#0f172a", 16, true),
                text(students + " - " + location + " - " + time, "#64748b", 13, false)
        );
        return box;
    }

    private Node progressItem(String title, double value, String grade) {
        VBox box = new VBox(8);
        box.setPadding(new Insets(16));
        box.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 18;");
        HBox header = new HBox(10, text(title, "#0f172a", 15, true), new Region(), chip(grade));
        HBox.setHgrow(header.getChildren().get(1), Priority.ALWAYS);
        ProgressBar bar = new ProgressBar(value);
        bar.setPrefWidth(Double.MAX_VALUE);
        bar.setStyle("-fx-accent: #2563eb;");
        box.getChildren().addAll(header, bar);
        return box;
    }

    private Node attendanceCard(String label, String value, String delta) {
        VBox box = new VBox(6);
        box.setPadding(new Insets(16));
        box.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 18;");
        box.getChildren().addAll(text(label, "#334155", 14, false), text(value, "#0f172a", 28, true), text(delta, "#10b981", 13, false));
        return box;
    }

    private Label chip(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-background-color: #e0e7ff; -fx-text-fill: #4338ca; -fx-background-radius: 999; -fx-padding: 7 12 7 12; -fx-font-size: 12px; -fx-font-weight: bold;");
        return label;
    }

    private Label text(String value, String color, int size, boolean bold) {
        Label label = new Label(value);
        label.setStyle("-fx-text-fill: " + color + "; -fx-font-size: " + size + "px;" + (bold ? " -fx-font-weight: bold;" : ""));
        return label;
    }

    private Node sectionText(String text, String color, int size, boolean bold) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: " + color + "; -fx-font-size: " + size + "px;" + (bold ? " -fx-font-weight: bold;" : ""));
        return label;
    }

    private Region stretch() {
        Region region = new Region();
        HBox.setHgrow(region, Priority.ALWAYS);
        return region;
    }

    private VBox sidebarBrand() {
        Label name = new Label("EduCore");
        name.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label sub = new Label("Learning Platform");
        sub.setStyle("-fx-font-size: 13px; -fx-text-fill: #cbd5e1;");
        StackPane logo = new StackPane(new Label("*"));
        logo.setPrefSize(52, 52);
        logo.setStyle("-fx-background-color: linear-gradient(to bottom right, #2563eb, #9333ea); -fx-background-radius: 16;");
        ((Label) logo.getChildren().get(0)).setStyle("-fx-font-size: 20px; -fx-text-fill: white;");
        HBox row = new HBox(12, logo, new VBox(2, name, sub));
        row.setAlignment(Pos.CENTER_LEFT);
        return new VBox(row);
    }

    private Region dividerDark() {
        Region region = new Region();
        region.setPrefHeight(1);
        region.setStyle("-fx-background-color: rgba(255,255,255,0.08);");
        return region;
    }

    private Button navButton(String text, boolean active, Runnable action) {
        Button button = new Button(text);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setPrefHeight(46);
        button.setAlignment(Pos.CENTER_LEFT);
        button.setStyle(active
                ? "-fx-background-color: linear-gradient(to right, #2563eb, #9333ea); -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 14; -fx-cursor: hand; -fx-padding: 0 16 0 16;"
                : "-fx-background-color: transparent; -fx-text-fill: #e2e8f0; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 14; -fx-cursor: hand; -fx-padding: 0 16 0 16;");
        button.setOnAction(e -> action.run());
        return button;
    }

    private String sectionTitle(String section, User user) {
        return switch (section) {
            case "Dashboard" -> switch (user.getRole()) {
                case ADMIN -> "Administrator Dashboard";
                case TEACHER -> "Teacher Dashboard";
                case STUDENT -> "Student Dashboard";
            };
            case "My Courses" -> "My Courses";
            case "Assignments" -> "Assignments";
            case "Grades" -> "Grades";
            case "Schedule" -> "Schedule";
            case "Achievements" -> "Achievements";
            case "Users", "User Management" -> "User Management";
            case "Departments" -> "Departments";
            case "Reports" -> "Reports";
            case "Analytics" -> "Analytics";
            case "System Settings", "Settings" -> "Settings";
            case "Messages" -> "Messages";
            case "Courses" -> "Courses";
            case "Evaluation" -> "Evaluation";
            case "Presence" -> "Presence";
            default -> section;
        };
    }

    private String roleContent(String section, User user) {
        return switch (user.getRole()) {
            case ADMIN -> "You are ADMIN. Manage every area from this dashboard.";
            case TEACHER -> "You are TEACHER. Manage your teaching sections and student progress.";
            case STUDENT -> "You are STUDENT. View your courses, evaluation, and presence.";
        };
    }

    private String displayName(User user) {
        return user.getEmail() != null && !user.getEmail().isBlank() ? user.getEmail() : user.getUsername();
    }

    private String displayFirstName(User user) {
        String source = user.getUsername();
        if (source == null || source.isBlank()) {
            source = displayName(user);
        }
        String clean = source.contains("@") ? source.substring(0, source.indexOf('@')) : source;
        clean = clean.replace('.', ' ').replace('_', ' ').trim();
        if (clean.isBlank()) {
            return "Student";
        }
        String[] parts = clean.split("\\s+");
        String first = parts[0];
        return first.substring(0, 1).toUpperCase() + first.substring(1).toLowerCase();
    }
}

