package it.unive.lisa.test.stripes.cfg.dot;

import it.unive.lisa.test.stripes.cfg.graph.Cfg;
import it.unive.lisa.test.stripes.cfg.graph.CfgNode;
import it.unive.lisa.test.stripes.cfg.graph.InvalidCfgException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Description.
 *
 * @author DeB
 * @version 1.0 2021-06-20
 * @since version date
 */
public class DotReader<T> {

    private static final int MAX_SIZE = 1024;

    private static final Pattern NODE_INFO_PATTERN = Pattern.compile(
        """
        "node(?<id>\\d+)" \\[shape="\\w+",color="(?<col>\\w+)"(,peripheries="(?<per>\\d+)")?,label=<(?<lab>.+)>];"""
    );

    private static final Pattern NODE_EDGE_PATTERN = Pattern.compile(
        """
        "node(?<index1>\\d+)" -> "node(?<index2>\\d+)" \\[color="(?<col>\\w+)"(,style="(?<sty>\\w+)")?];"""
    );

    @NotNull
    private final File fileToRead;

    @Nullable
    private CfgNode<T> root;

    @NotNull
    private String lastStringRead;

    public DotReader(@NotNull final String filePath) {
        this.fileToRead = new File(filePath);
        this.root = null;
        this.lastStringRead = "";
    }

    public Cfg<T> readFile(
        final Extractor<T> dataExtractor,
        final Extractor<String> identifierExtractor,
        final EdgeTypeExtractor edgeTypeExtractor,
        final Extractor<Boolean> isRoot
    ) throws IOException {
        try (final Scanner scanner = new Scanner(this.fileToRead, StandardCharsets.UTF_8)) {
            // Skip first line
            scanner.nextLine();
            final CfgNode<T>[] nodes =
                this.extractNodes(scanner, dataExtractor, identifierExtractor, isRoot);
            this.linkNodes(scanner, nodes, edgeTypeExtractor);
            this.checkNodes(nodes);
            assert this.root != null;
            return new Cfg<>(this.root);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private CfgNode<T>[] extractNodes(
        final Scanner scanner,
        final Extractor<T> dataExtractor,
        final Extractor<String> identifierExtractor,
        final Extractor<Boolean> isRoot
    ) {
        boolean stop = false;
        final CfgNode<T>[] nodes = new CfgNode[DotReader.MAX_SIZE];
        do {
            this.lastStringRead = scanner.nextLine();
            final Matcher matcher = DotReader.NODE_INFO_PATTERN.matcher(this.lastStringRead.trim());
            if (matcher.matches()) {
                final String color = matcher.group("col");
                final String peripheries = DotReader.getGroupOrDefault(matcher, "per", "0");
                final int convertedPeripheries = Integer.parseInt(peripheries);
                final String label = matcher.group("lab");
                final String convertedLabel = DotReader.convertHtml(label);
                final int index = Integer.parseInt(matcher.group("id"));
                DotReader.checkIndex(index);
                final T data = dataExtractor.extract(convertedLabel, color, convertedPeripheries);
                final String identifier = identifierExtractor.extract(
                    convertedLabel,
                    color,
                    convertedPeripheries
                );
                final CfgNode<T> newNode = new CfgNode<>(identifier, data);
                nodes[index] = newNode;
                if (isRoot.extract(convertedLabel, color, convertedPeripheries)) {
                    if (this.root != null) {
                        throw new InvalidCfgException("Multiple CFG roots defined");
                    }
                    this.root = newNode;
                }
            } else {
                stop = true;
            }
        } while (!stop);

        return nodes;
    }

    private void linkNodes(
        final Scanner scanner,
        final CfgNode<T>[] nodes,
        final EdgeTypeExtractor edgeTypeExtractor
    ) {
        boolean stop = false;
        do {
            final Matcher matcher = DotReader.NODE_EDGE_PATTERN.matcher(this.lastStringRead.trim());
            if (matcher.matches()) {
                final int firstNodeIndex = Integer.parseInt(matcher.group("index1"));
                final int secondNodeIndex = Integer.parseInt(matcher.group("index2"));
                DotReader.checkIndex(firstNodeIndex);
                DotReader.checkIndex(secondNodeIndex);
                final CfgNode<T> firstNode = nodes[firstNodeIndex];
                final CfgNode<T> secondNode = nodes[secondNodeIndex];
                if (firstNode == null) {
                    throw new InvalidCfgException("Unknown node with index " + firstNodeIndex);
                }
                if (secondNode == null) {
                    throw new InvalidCfgException("Unknown node with index " + secondNodeIndex);
                }
                final String color = matcher.group("col");
                final String style = DotReader.getGroupOrDefault(matcher, "sty", "");
                final EdgeType type = edgeTypeExtractor.extractEdgeType(color, style);
                if (type == EdgeType.FALSE) {
                    firstNode.setFalse(secondNode);
                } else {
                    firstNode.setTrue(secondNode);
                }
                this.lastStringRead = scanner.nextLine();
            } else {
                stop = true;
            }
        } while (!stop);
    }

    private void checkNodes(final CfgNode<T>[] nodes) {
        if (this.root == null) {
            throw new InvalidCfgException("CFG is missing a root");
        }
        int endNodes = 0;
        for (final CfgNode<T> node : nodes) {
            if (node == null) {
                break;
            }
            if ((node.getTrue() == null) && (node.getFalse() != null)) {
                throw new InvalidCfgException(
                    "Node with a null true branch and non-null false branch found"
                );
            }
            if ((node.getTrue() == null) && (node.getFalse() == null)) {
                endNodes++;
            }
        }
        if (endNodes == 0) {
            throw new InvalidCfgException("No return nodes in the CFG");
        }
    }

    private static void checkIndex(final int index) {
        if ((index < 0) || (index > DotReader.MAX_SIZE)) {
            throw new InvalidCfgException("Invalid node index " + index);
        }
    }

    private static String convertHtml(final String htmlString) {
        return htmlString
            .replace("&gt;", ">")
            .replace("&lt;", "<")
            .replace("&perp;", "⊥")
            .replace("&rarr;", "→")
            .replace("<BR/>", "\n");
    }

    private static String getGroupOrDefault(
        final Matcher matcher,
        final String groupName,
        final String defaultValue
    ) {
        String value = matcher.group(groupName);
        if (value == null) {
            value = defaultValue;
        }
        return value;
    }
}
