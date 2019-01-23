package beam.lang.ast;

import beam.parser.antlr4.BeamParser;

import java.util.List;
import java.util.stream.Collectors;

public class ListNode extends Node {

    private final List<Node> items;

    public ListNode(BeamParser.ListValueContext context) {
        items = context.listItemValue()
                .stream()
                .map(c -> Node.create(c.getChild(0)))
                .collect(Collectors.toList());
    }

    public List<Node> getItems() {
        return items;
    }

    @Override
    public Object evaluate(Scope scope) {
        return items.stream()
                .map(n -> n.evaluate(scope))
                .collect(Collectors.toList());
    }

    @Override
    public void buildString(StringBuilder builder, int indentDepth) {
        builder.append('[');
        builder.append(items.stream().map(Node::toString).collect(Collectors.joining(", ")));
        builder.append(']');
    }
}
