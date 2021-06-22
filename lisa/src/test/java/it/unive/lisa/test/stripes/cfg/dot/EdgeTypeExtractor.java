package it.unive.lisa.test.stripes.cfg.dot;

/**
 * Description.
 *
 * @author DeB
 * @version 1.0 2021-06-20
 * @since version date
 */
@FunctionalInterface
public interface EdgeTypeExtractor {
    EdgeType extractEdgeType(String color, String style);
}
