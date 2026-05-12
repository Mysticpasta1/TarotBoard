package com.mystic.tarotboard.utils;

import com.vladsch.flexmark.ast.*;
import com.vladsch.flexmark.ext.tables.*;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.ast.NodeVisitor;
import com.vladsch.flexmark.util.ast.VisitHandler;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.text.TextFlow;

import java.util.HashMap;
import java.util.Map;

public class MarkdownToGui {
    private static final Map<String, javafx.scene.Node> anchors = new HashMap<>();

    public static void render(Node document, VBox container, ScrollPane scrollPane) {
        anchors.clear();

        NodeVisitor visitor = new NodeVisitor(
                new VisitHandler<>(Heading.class, node -> {
                    String text = String.valueOf(node.getText());
                    Label l = new Label(text);
                    l.setStyle("-fx-font-weight: bold; -fx-text-fill: white; -fx-font-size: " + (28 - node.getLevel() * 2) + "pt;");
                    String slug = text.toLowerCase().replaceAll("\\s+", "-");
                    anchors.put(slug, l);
                    container.getChildren().add(l);
                }),
                new VisitHandler<>(Paragraph.class, node -> {
                    TextFlow flow = new TextFlow();
                    renderInline(node, flow, scrollPane, container);
                    container.getChildren().add(flow);
                }),
                new VisitHandler<>(TableBlock.class, node -> {
                    GridPane grid = new GridPane();
                    grid.setStyle("-fx-border-color: #444; -fx-border-width: 1; -fx-background-color: rgba(255,255,255,0.05);");

                    int rowIndex = 0;
                    Node section = node.getFirstChild();
                    while (section != null) {
                        if (section instanceof TableHead || section instanceof TableBody) {
                            Node row = section.getFirstChild();
                            while (row != null) {
                                int colIndex = 0;
                                Node cell = row.getFirstChild();
                                while (cell != null) {
                                    if (cell instanceof TableCell tableCell) {
                                        StackPane cellPane = new StackPane();
                                        TextFlow cellContent = new TextFlow();
                                        cellContent.setMaxWidth(400);
                                        Node cellChild = tableCell.getFirstChild();
                                        while (cellChild != null) {
                                            renderInline(cellChild, cellContent, scrollPane, container);
                                            cellChild = cellChild.getNext();
                                        }

                                        cellPane.getChildren().add(cellContent);
                                        cellPane.setPadding(new Insets(8));

                                        if (section instanceof TableHead) {
                                            cellPane.setStyle("-fx-background-color: #333; -fx-border-color: #444; -fx-border-width: 0.5;");
                                        } else {
                                            cellPane.setStyle("-fx-border-color: #333; -fx-border-width: 0.5;");
                                        }
                                        grid.add(cellPane, colIndex++, rowIndex);
                                    }
                                    cell = cell.getNext();
                                }
                                rowIndex++;
                                row = row.getNext();
                            }
                        }
                        section = section.getNext();
                    }
                    container.getChildren().add(grid);
                })
        );

        visitor.visit(document);
    }

    private static void renderInline(Node parent, TextFlow flow, ScrollPane sp, VBox container) {
        Node child = parent.getFirstChild();
        if (child == null && parent instanceof Text t) {
            addTextToFlow(String.valueOf(t.getChars()), flow, false);
            return;
        }

        while (child != null) {
            switch (child) {
                case Text t -> addTextToFlow(String.valueOf(t.getChars()), flow, false);
                case Link l -> flow.getChildren().add(createLink(l, sp, container));
                case StrongEmphasis s -> addTextToFlow(String.valueOf(s.getText()), flow, true);
                default -> {
                }
            }
            child = child.getNext();
        }
    }

    private static void addTextToFlow(String content, TextFlow flow, boolean bold) {
        javafx.scene.text.Text t = new javafx.scene.text.Text(content);
        String weight = bold ? "bold" : "normal";
        String color = bold ? "white" : "#cccccc";
        t.setStyle("-fx-fill: " + color + "; -fx-font-size: 12pt; -fx-font-weight: " + weight + ";");
        flow.getChildren().add(t);
    }

    private static Hyperlink createLink(Link node, ScrollPane scrollPane, VBox container) {
        Hyperlink link = new Hyperlink(String.valueOf(node.getText()));
        String url = String.valueOf(node.getUrl());
        link.setStyle("-fx-padding: 0; -fx-text-fill: #4da6ff; -fx-underline: true;");

        link.setOnAction(event -> {
            if (url.startsWith("#")) {
                scrollToAnchor(url.substring(1), scrollPane, container);
            }
        });
        return link;
    }

    private static void scrollToAnchor(String slug, ScrollPane scrollPane, VBox container) {
        javafx.scene.Node target = anchors.get(slug);
        if (target != null) {
            Platform.runLater(() -> {
                double contentHeight = container.getBoundsInLocal().getHeight();
                double nodeTop = target.getBoundsInParent().getMinY();
                double viewportHeight = scrollPane.getViewportBounds().getHeight();

                double scrollValue = nodeTop / (contentHeight - viewportHeight);
                scrollPane.setVvalue(Math.clamp(scrollValue, 0, 1));
            });
        }
    }
}